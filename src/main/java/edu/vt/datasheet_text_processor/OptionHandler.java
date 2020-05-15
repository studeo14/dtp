package edu.vt.datasheet_text_processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.Errors.Context.TokenizerContext;
import edu.vt.datasheet_text_processor.Errors.*;
import edu.vt.datasheet_text_processor.classification.DatasheetBOW;
import edu.vt.datasheet_text_processor.cli.Application;
import edu.vt.datasheet_text_processor.input.AllMappings;
import edu.vt.datasheet_text_processor.input.AllMappingsRaw;
import edu.vt.datasheet_text_processor.intermediate_representation.IRFinder;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticExpression;
import edu.vt.datasheet_text_processor.signals.Acronym;
import edu.vt.datasheet_text_processor.signals.AcronymFinder;
import edu.vt.datasheet_text_processor.signals.Signal;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization.AcronymNormalizer;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization.BitAccessNormalizer;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.commons.io.FilenameUtils;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OptionHandler {
    private static final Logger logger = LoggerFactory.getLogger( OptionHandler.class );

    private static boolean warningsReset = false;

    private static void resetWarnings(Sentence sentence) {
        if (sentence.getWarnings() != null) {
            sentence.getWarnings().clear();
        } else {
            sentence.setWarnings(new ArrayList<>());
        }
    }

    /**
     * Utility method for "compiling" the tokens mappings from "Raw" text format to a wordId format
     * that is used in the generic Mappings data structure.
     *
     * @param mappingFile
     * @throws IOException
     * @throws SerializerException
     */
    public static void compileTokens ( File mappingFile ) throws IOException, SerializerException {
        var rawMappings = new ObjectMapper().readValue( mappingFile, AllMappingsRaw.class );
        rawMappings.init();
        var exportFileBase = FilenameUtils.removeExtension( mappingFile.getName() );
        var exportFile = new File( exportFileBase + "_compiled.json" );
        rawMappings.export( exportFile );
        logger.info( "Exported compiled tokens to {}", exportFile.getName() );
        logger.info( "exiting." );
    }

    /**
     * Process the signal names from the signal names input file and attempt to add them to the project as signal names
     * and acronyms.
     *
     * @param project
     * @param signalNames File
     * @throws IOException
     */
    public static void processSignalNames ( Project project, File signalNames ) throws IOException {
        // add signal names to the project
        // first check if the list is already present
        var db = project.getDB();
        var collection = db.getRepository( Signal.class ); // creates if not already present
        for ( var signal : collection.find() ) {
            logger.debug( "Existing Signal: {}", signal.getName() );
        }
        // read in signal names
        for ( String line : Files.readAllLines( signalNames.toPath() ) ) {
            // split into name and acronyms
            var split = line.split( "::" );
            try {
                if ( split.length == 2 ) {
                    var acronyms = Arrays.stream( split[ 1 ].split( "," ) )
                            .map( String::toLowerCase )
                            .collect( Collectors.toList() );
                    var aliases = Arrays.stream( split[ 0 ].split( "/" ) )
                            .map( String::toLowerCase )
                            .collect( Collectors.toList() );
                    for ( var alias : aliases ) {
                        collection.insert( new Signal( alias, acronyms ) );
                    }
                } else {
                    var aliases = Arrays.stream( split[ 0 ].split( "/" ) )
                            .map( String::toLowerCase )
                            .collect( Collectors.toList() );
                    for ( var alias : aliases ) {
                        collection.insert( new Signal( alias ) );
                    }
                }
                logger.info( "Signal added: {}", split[ 0 ] );
            } catch ( UniqueConstraintException | InvalidIdException r ) {
                logger.debug( "Skipping {}. Already added.", split[ 0 ] );
            }
        }
    }

    /**
     * Classify the sentences into comment and questionable. The method used aims to be greedy and possible overcapture
     * the given sentences.
     *
     * @param project
     * @param classificationScheme
     */
    public static void classifySentences ( Project project, Application.ExperimentalOptions.ClassificationScheme classificationScheme ) {
        // create classify table
        var db = project.getDB();
        var repo = db.getRepository( Sentence.class );
        var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
        for ( Sentence s : documents ) {
            if ( s.getType() == Sentence.Type.META ) {
                continue;
            }
            // do classify
            var isComment = true;
            switch ( classificationScheme ) {
                case nosig:
                    isComment = DatasheetBOW.is_questionable( s.getText() );
                    break;
                case withsig:
                    isComment = DatasheetBOW.is_questionable__with_signal_names__( project, s.getText() );
                    break;
                case sigonly:
                    isComment = DatasheetBOW.is_questionable__only_signal_names__( project, s.getText() );
                    break;
            }
            if ( isComment ) {
                s.setType( Sentence.Type.NONCOMMENT );
            } else {
                s.setType( Sentence.Type.COMMENT );
            }
            repo.update( s );
        }
    }

    /**
     * Serialize the sentences into streams of wordIds (Integers). Possibly interact with the user when unknown words are
     * encountered.
     *
     * @param project
     * @param addNew
     * @param mappingFile
     * @param allMappings
     * @throws IOException
     */
    public static void processWordIds ( Project project, boolean addNew, File mappingFile, AllMappings allMappings ) throws IOException {
        logger.info( "Doing wid" );
        if ( addNew ) {
            logger.info( "Looking for unmapped words." );
            logger.info( "Categories are:\n\t0. Junk\n\t1. Objects\n\t2. Verb\n\t3. Number\n\t4. " +
                    "Modifier\n\t5. Adjective\n\t6. Pronoun\n\t7. Conditions" );
        }
        var db = project.getDB();
        var repo = db.getRepository( Sentence.class );
        var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
        for ( Sentence s : documents ) {
            // do serialize
            if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                if (!warningsReset) {
                    resetWarnings( s );
                }
                List< Integer > wordIds = null;
                try {
                    wordIds = allMappings.getSerializer().serialize( s.getText(), addNew );
                    s.setWordIds( wordIds );
                    repo.update( s );
                } catch ( SerializerException e ) {
                    logger.warn( e.getMessage() );
                    s.getWarnings().add( new Warning( e ) );
                    repo.update( s );
                }
            }
        }
        if (!warningsReset) {
            warningsReset = true;
        }
        if ( addNew ) {
            allMappings.export( mappingFile );
        }
    }

    /**
     * Chunk the wordIds into tokens of 1 or more wordIds. Many wordId combinations are aliased to a single token.
     *
     * @param project
     * @param allMappings
     * @param normalize
     * @param mappingFile
     * @param preferShorterTokens
     * @throws IOException
     */
    public static void processTokens ( Project project, AllMappings allMappings, boolean normalize, File mappingFile, boolean preferShorterTokens ) throws IOException {
        logger.info( "Doing Tokenization" );
        // tokenize
        var db = project.getDB();
        var repo = db.getRepository( Sentence.class );
        var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
        for ( Sentence s : documents ) {
            // do serialize
            if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                if (!warningsReset) {
                    resetWarnings( s );
                }
                try {
                    List< TokenInstance > tokens = allMappings.getTokenizer().tokenize( s.getWordIds(), preferShorterTokens );
                    s.setTokens( tokens );
                    repo.update( s );
                } catch ( TokenizerException te ) {
                    logger.warn( te.getMessage() );
                    var context = ( TokenizerContext ) te.getContext();
                    logger.warn( "Unable to tokenize \"{}\" at **{}({})**", s.getText(), allMappings.getSerializer().unconvert( context.getCurrentWord() ), context.getWordIndex() );
                    var warning = new Warning( te );
                    s.getWarnings().add( warning );
                    repo.update( s );
                }
            }
        }
        if ( normalize ) {
            logger.info( "Normalizing." );
            // find and add acronyms
            logger.info( "Finding Acronyms." );
            AcronymFinder.initializeAcronyms( project );
            AcronymFinder.findAcronyms( project, allMappings.getSerializer() );
            var added = AcronymFinder.addAcronymsToMapping( project, allMappings.getSerializer() );
            if ( added ) {
                allMappings.export( mappingFile );
            }
            // normalize acronyms
            logger.info( "Normalizing Acronyms." );
            AcronymNormalizer.normalizeAcronyms( project, allMappings.getSerializer() );
            // normalize bit accesses
            logger.info( "Normalizing Bit Accesses." );
            BitAccessNormalizer.normalizeBitAccesses( project, allMappings.getFrameFinder(), allMappings.getSerializer() );
        }
        if (!warningsReset) {
            warningsReset = true;
        }
    }

    /**
     * Process the tokens converting them to a bag of semantic frames.
     *  @param project
     * @param allMappings
     * @param preferShorterFrames
     */
    public static void processSemanticExpressions ( Project project, AllMappings allMappings, boolean preferShorterFrames ) {
        logger.info( "Finding Semantic Expressions" );
        var db = project.getDB();
        var repo = db.getRepository( Sentence.class );
        var documents = repo.find( ObjectFilters.eq( "type", Sentence.Type.NONCOMMENT ), FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
        for ( Sentence s : documents ) {
            if (!warningsReset) {
                resetWarnings( s );
            }
            try {
                var semexpr = allMappings.getSemanticParser().findSemanticExpression( s.getTokens(), allMappings.getFrameFinder(), preferShorterFrames);
                if ( semexpr.isPresent() ) {
                    var se = semexpr.get();
                    var seTTo = getSemanticExpressionTokenText( se, allMappings );
                    List< List< String > > seTT = null;
                    if ( seTTo.isPresent() ) {
                        seTT = seTTo.get();
                        se.setTokenText( seTT );
                    }
                    s.setSemanticExpression( se );
                    if ( logger.isDebugEnabled() ) {
                        if ( seTT == null ) {
                            logger.warn( "Sentence {} has no semantic expression!", s.getSentenceId() );
                        } else {
                            logger.debug( "{} -> {} ({})", s.getText(), s.getSemanticExpression(), seTT );
                            var test = s.getSemanticExpression().getAllFrames()
                                    .stream()
                                    .map( frame -> frame.getTokens().toString() )
                                    .collect( Collectors.toList() );
                            logger.info( "{} -> {} ({})", s.getText(), s.getSemanticExpression(), test );
                        }
                    }
                    repo.update( s );
                }
            } catch ( FrameException e ) {
                logger.warn( s.getText() );
                logger.warn( e.getMessage() );
                s.getWarnings().add( new Warning( e ) );
                s.setSemanticExpression( null );
                repo.update( s );
            }
        }
        if (!warningsReset) {
            warningsReset = true;
        }
    }

    /**
     * Further process the semantic expression to reduce compound expressions, temporal opereators, and other complex
     * cases into unified frames. Then convert the frames into IR.
     *  @param project
     * @param allMappings
     * @param doShowIRCounts
     */
    public static void processIR ( Project project, AllMappings allMappings, boolean doShowIRCounts ) {
        logger.info( "Finding Intermediate Representation" );
        var db = project.getDB();
        var repo = db.getRepository( Sentence.class );
        var documents = repo.find( ObjectFilters.eq( "type", Sentence.Type.NONCOMMENT ), FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
        for ( Sentence s : documents ) {
            if (!warningsReset) {
                resetWarnings( s );
            }
            var se = s.getSemanticExpression();
            if ( se != null ) {
                try {
                    var ir = IRFinder.findIR( se, allMappings );
                    s.setIr( ir );
                    repo.update( s );
                } catch ( ProcessorException e ) {
                    logger.warn( "For Sentence: {}", s.getText() );
                    logger.warn( e.getMessage() );
                    s.getWarnings().add( new Warning( e ) );
                    repo.update( s );
                }
            } else {
                logger.info( "No SE for sentence {}", s.getSentenceId() );
            }
        }
        IRFinder.addCountersToProject(project);
        if (doShowIRCounts) {
            IRFinder.showCounters();
        }
        if (!warningsReset) {
            warningsReset = true;
        }
    }

    /**
     * Handle the CLI options by exporting to the other specialized processing functions. Debugging is not exported.
     *
     * @param options
     * @throws IOException
     * @throws ProcessorException
     */
    public static void handle ( Application options ) throws IOException, ProcessorException {
        // compile tokens or process project
        if ( options.inPointOptions.compileTokens ) {
            compileTokens( options.mappingFile );
        } else {
            // open and process
            var project = ProjectUtils.openProject( options.inPointOptions.inputFile );
            if ( project.getDB() == null ) {
                logger.error( "Unable to open project" );
            } else {
                // init mappings
                var allMappings = new ObjectMapper().readValue( options.mappingFile, AllMappings.class );
                // init experimental options to default values (disabled) if not specified
                if ( options.experimentalOptions == null ) {
                    options.experimentalOptions = new Application.ExperimentalOptions();
                }
                allMappings.init();
                // handle processing options
                if ( options.signalNames != null ) {
                    processSignalNames( project, options.signalNames );
                }
                if ( options.doClassify ) {
                    classifySentences( project, options.experimentalOptions.classificationScheme );
                }
                if ( options.wordIDOptions != null ) {
                    if ( options.wordIDOptions.doWordId ) {
                        processWordIds( project, options.wordIDOptions.addNew, options.mappingFile, allMappings );
                    }
                }
                if ( options.tokenOptions != null ) {
                    if ( options.tokenOptions.doToken ) {
                        processTokens( project, allMappings, options.tokenOptions.normalize, options.mappingFile, options.experimentalOptions.preferShorterTokens );
                    }
                }
                if ( options.semanticExpressionOptions != null ) {
                    if ( options.semanticExpressionOptions.doSemanticExpression ) {
                        processSemanticExpressions( project, allMappings, options.experimentalOptions.preferShorterFrames );
                    }
                }
                if ( options.irOptions != null ) {
                    if (options.irOptions.doResetMetrics) {
                        project.removeMetric("avgIrMetrics");
                        project.removeMetric("currentIrMetrics");
                    }
                    if ( options.irOptions.doGetIr ) {
                        processIR( project, allMappings, options.experimentalOptions.doShowIRCounts );
                    }
                }
                if ( options.debugOptions != null ) {
                    if ( options.debugOptions.doPrint ) {
                        // print all from database
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        for ( Sentence s : documents ) {
                            var o = String.format( "%d(%d) :: %s :: %s", s.getSentenceId(), s.getPriority(), s.getType(), s.getText() );
                            logger.info( o );
                        }
                    }
                    if ( options.debugOptions.doPercentage ) {
                        var results = DatasheetBOW.count_questionable( project );
                        double percentage = results.getRight().doubleValue() / ( results.getLeft().doubleValue() );
                        System.out.printf( "%%Q: %.2f%%\n", percentage * 100 );
                    }
                    if ( options.debugOptions.doShowComments ) {
                        DatasheetBOW.debug_file_comments( project );
                    }
                    if ( options.debugOptions.doShowNonComments ) {
                        DatasheetBOW.debug_file_noncomments( project );
                    }
                    if ( options.debugOptions.doPrintNonComments != null ) {
                        DatasheetBOW.debug_file_noncomments( project, options.debugOptions.doPrintNonComments );
                    }
                    if ( options.debugOptions.doShowNonCommentsMatches ) {
                        DatasheetBOW.debug_file_questionable( project );
                    }
                    if ( options.debugOptions.doShowMatches ) {
                        DatasheetBOW.debug_file_matches( project );
                    }
                    if ( options.debugOptions.doShowMostUsed ) {
                        DatasheetBOW.most_used( project );
                    }
                    if ( options.debugOptions.doShowWordIds ) {
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        for ( Sentence s : documents ) {
                            // do serializee
                            if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                                logger.info( "{}\n->\n{}", s.getText(), s.getWordIds() );
                            }
                        }
                    }
                    if ( options.debugOptions.doShowTokens ) {
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        for ( Sentence s : documents ) {
                            // do serialize
                            if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                                var tokens = s.getTokens().stream()
                                        .map( TokenInstance::toString )
                                        .collect( Collectors.toList() );
                                logger.info( "{}\n->\n{}", s.getText(), tokens );
                            }
                        }
                    }
                    if ( options.debugOptions.doShowTokenText ) {
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        for ( Sentence s : documents ) {
                            // do serialize
                            if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                                var tokens = s.getTokens().stream()
                                        .map( t -> {
                                            if ( t.getType() == TokenInstance.Type.ACCESS ) {
                                                return t.toString();
                                            } else if ( t.getType() == TokenInstance.Type.COMPOUND ) {
                                                return t.getCompoundToken().getOriginalTokens().stream()
                                                        .map( ti -> {
                                                            if ( ti.getType() == TokenInstance.Type.ACCESS ) {
                                                                return ti.toString();
                                                            } else {
                                                                return Serializer.mergeWords( allMappings.getSerializer().deserialize( ti.getStream() ) );
                                                            }
                                                        } )
                                                        .collect( Collectors.joining( " " ) );
                                            } else {
                                                return Serializer.mergeWords( allMappings.getSerializer().deserialize( t.getStream() ) );
                                            }
                                        } )
                                        .collect( Collectors.toList() );
                                logger.info( "{}\n->\n{}", s.getText(), tokens );
                            }
                        }
                    }
                    if ( options.debugOptions.doShowAcronyms ) {
                        var db = project.getDB();
                        if ( db.hasRepository( Sentence.class ) ) {
                            var acronyms = db.getRepository( Acronym.class );
                            for ( var acronym : acronyms.find() ) {
                                logger.info( "{} -> {}", acronym.getAcronym(), acronym.getExpanded() );
                            }
                        }
                    }
                    if ( options.debugOptions.doShowFrameSearchTree ) {
                        System.out.println( allMappings.getFrameFinder().getFrameSearchTree().toString() );
                    }
                    if ( options.debugOptions.doShowTokenSearchTree ) {
                        System.out.println( allMappings.getTokenizer().getTokenSearchTree().toString() );
                    }
                    if ( options.debugOptions.doShowSemanticExpressions ) {
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find( ObjectFilters.eq( "type", Sentence.Type.NONCOMMENT ), FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        for ( var s : documents ) {
                            var se = s.getSemanticExpression();
                            if ( se == null ) {
                                logger.warn( "Sentence {} has no semantic expression!", s.getSentenceId() );
                            } else {
//                                var seTT = s.getSemanticExpression().getTokenText();
                                var seTT = s.getSemanticExpression().getAllFrames()
                                        .stream()
                                        .map( frame -> frame.getTokens().toString() )
                                        .collect( Collectors.toList() );
                                logger.info( "{} -> {} ({})", s.getText(), s.getSemanticExpression(), seTT );
                                logger.info( "{} -> {} ({})", s.getText(), s.getSemanticExpression(), s.getSemanticExpression().getTokenText() );
                            }
                        }
                    }
                    if ( options.debugOptions.doShowIR ) {
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find(
                                ObjectFilters.and(
                                        ObjectFilters.eq( "type", Sentence.Type.NONCOMMENT ),
                                        ObjectFilters.not( ObjectFilters.eq( "ir", null ) )
                                ),
                                FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        for ( var s : documents ) {
                            logger.info( "{} -> {}", s.getText(), s.getIr() );
                        }
                    }
                    if ( options.debugOptions.doShowIRP ) {
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find(
                                ObjectFilters.and(
                                        ObjectFilters.eq( "type", Sentence.Type.NONCOMMENT )
                                ),
                                FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        var count = 0.0;
                        var total = 0.0;
                        for ( var s : documents ) {
                            total += 1;
                            if (s.getIr() != null && !s.getIr().isBlank() && !s.getIr().isEmpty()) {
                                count += 1;
                            }
                        }
                        logger.info("Total Q: {}, Q With IR: {}, %: {}%", total, count, (count/total) * 100);
                    }
                    if (options.debugOptions.doShowAverageTokens) {
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find(
                                ObjectFilters.and(
                                        ObjectFilters.eq( "type", Sentence.Type.NONCOMMENT ),
                                        ObjectFilters.not( ObjectFilters.eq( "tokens", null ) )
                                ),
                                FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        var avg = documents.toList().stream()
                                .map(Sentence::getTokens)
                                .mapToInt(List::size)
                                .average();
                        logger.info("Average number of tokens: {}", avg);
                    }
                    if (options.debugOptions.doShowAverageFrames) {
                        var db = project.getDB();
                        var repo = db.getRepository( Sentence.class );
                        var documents = repo.find(
                                ObjectFilters.and(
                                        ObjectFilters.eq( "type", Sentence.Type.NONCOMMENT ),
                                        ObjectFilters.not( ObjectFilters.eq( "semanticExpression", null ) )
                                ),
                                FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                        var avgAll = documents.toList().stream()
                                .map(Sentence::getSemanticExpression)
                                .map(SemanticExpression::getAllFrames)
                                .mapToInt(List::size)
                                .average();
                        var avgA = documents.toList().stream()
                                .map(Sentence::getSemanticExpression)
                                .map(SemanticExpression::getAntecedents)
                                .mapToInt(List::size)
                                .average();
                        var avgC = documents.toList().stream()
                                .map(Sentence::getSemanticExpression)
                                .map(SemanticExpression::getConsequents)
                                .mapToInt(List::size)
                                .average();
                        logger.info("Average number of frames: All: {}, Antecedents: {}, Consequents: {}", avgAll, avgA, avgC);
                    }
                    if (options.debugOptions.doShowMetrics) {
                        var metrics = project.getMetrics();
                        for (var metric: metrics) {
                            for (var kvp: metric) {
                                logger.info("Value: {} :: {}", kvp.getKey(), kvp.getValue());
                            }
                        }
                    }
                }
                project.close();
            }
        }
    }

    /**
     * Helper method to convert a semantic expression into a printable format for debugging.
     *
     * @param se
     * @param allMappings
     * @return
     */
    private static Optional< List< List< String > > > getSemanticExpressionTokenText ( SemanticExpression se, AllMappings allMappings ) {
        if ( se != null ) {
            var tokens = se.getAllFrames().stream()
                    .map( FrameInstance::getTokens )
                    .map( tlist -> tlist.stream()
                            .map( t -> {
                                if ( t.getType() == TokenInstance.Type.ACCESS ) {
                                    return t.toString();
                                } else if ( t.getType() == TokenInstance.Type.COMPOUND ) {
                                    return t.getCompoundToken().getOriginalTokens().stream()
                                            .map( ti -> {
                                                if ( ti.getType() == TokenInstance.Type.ACCESS ) {
                                                    return ti.toString();
                                                } else {
                                                    return Serializer.mergeWords( allMappings.getSerializer().deserialize( ti.getStream() ) );
                                                }
                                            } )
                                            .collect( Collectors.joining( " " ) );
                                } else {
                                    return Serializer.mergeWords( allMappings.getSerializer().deserialize( t.getStream() ) );
                                }
                            } )
                            .collect( Collectors.toList() )
                    )
                    .collect( Collectors.toList() );
            return Optional.of( tokens );
        } else {
            return Optional.empty();
        }
    }
}
