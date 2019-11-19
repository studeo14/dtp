package edu.vt.datasheet_text_processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.classification.DatasheetBOW;
import edu.vt.datasheet_text_processor.cli.Application;
import edu.vt.datasheet_text_processor.input.AllMappings;
import edu.vt.datasheet_text_processor.input.AllMappingsRaw;
import edu.vt.datasheet_text_processor.intermediate_representation.IRException;
import edu.vt.datasheet_text_processor.intermediate_representation.IRFinder;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameException;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticExpression;
import edu.vt.datasheet_text_processor.signals.Acronym;
import edu.vt.datasheet_text_processor.signals.AcronymFinder;
import edu.vt.datasheet_text_processor.signals.Signal;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenizerException;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization.AcronymNormalizer;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization.BitAccessNormalizer;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OptionHandler {
    private static final Logger logger = LogManager.getLogger( OptionHandler.class );

    public static void handle ( Project project, Application options ) throws IOException, TokenizerException, FrameException {
        if (options.compileTokens) {
            var rawMappings = new ObjectMapper().readValue(options.inPointOptions.mappingFile, AllMappingsRaw.class);
            rawMappings.init();
            var exportFileBase = FilenameUtils.removeExtension(options.inPointOptions.mappingFile.getName());
            var exportFile = new File(exportFileBase + "_compiled.json");
            rawMappings.export(exportFile);
            logger.info("Exported compiled tokens to {}", exportFile.getName());
            logger.info("exiting.");
        } else {
            var allMappings = new ObjectMapper().readValue(options.inPointOptions.mappingFile, AllMappings.class);
            allMappings.init();
            if (options.signalNames != null ) {
                // add signal names to the project
                // first check if the list is already present
                var db = project.getDB();
                var collection = db.getRepository(Signal.class); // creates if not already present
                for (var signal: collection.find()) {
                    logger.debug("Existing Signal: {}", signal.getName());
                }
                // read in signal names
                for (String line: Files.readAllLines(options.signalNames.toPath())) {
                    // split into name and acronyms
                    var split = line.split("::");
                    try {
                        if (split.length == 2) {
                            var acronyms = Arrays.stream(split[1].split(","))
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toList());
                            var aliases = Arrays.stream(split[0].split("/"))
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toList());
                            for (var alias: aliases) {
                                collection.insert(new Signal(alias, acronyms));
                            }
                        } else {
                            var aliases = Arrays.stream(split[0].split("/"))
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toList());
                            for (var alias: aliases) {
                                collection.insert(new Signal(alias));
                            }
                        }
                        logger.info("Signal added: {}", split[0]);
                    } catch (UniqueConstraintException | InvalidIdException r) {
                        logger.debug("Skipping {}. Already added.", split[0]);
                    }
                }
            }
            if ( options.doClassify ) {
                // create classify table
                var db = project.getDB();
                var repo = db.getRepository( Sentence.class );
                var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                for ( Sentence s : documents ) {
                    if (s.getType() == Sentence.Type.META) {
                        continue;
                    }
                    // do classify
                    var isComment = DatasheetBOW.is_questionable( s.getText() );
                    if ( isComment ) {
                        s.setType( Sentence.Type.NONCOMMENT );
                    } else {
                        s.setType( Sentence.Type.COMMENT );
                    }
                    repo.update( s );
                }
            }
            if ( options.wordIDOptions != null ) {
                if ( options.wordIDOptions.doWordId ) {
                    logger.info( "Doing wid" );
                    if ( options.wordIDOptions.addNew ) {
                        logger.info("Looking for unmapped words.");
                        logger.info( "Categories are:\n\t0. Junk\n\t1. Objects\n\t2. Verb\n\t3. Number\n\t4. " +
                                "Modifier\n\t5. Adjective\n\t6. Pronoun\n\t7. Conditions" );
                    }
                    var db = project.getDB();
                    var repo = db.getRepository( Sentence.class );
                    var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                    var t = new AddNewWrapper(options.wordIDOptions.addNew);
                    for ( Sentence s : documents ) {
                        // do serialize
                        if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                            var wordIds = allMappings.getSerializer().serialize( s.getText(), t );
                            s.setWordIds( wordIds );
                            repo.update( s );
                        }
                    }
                    if ( options.wordIDOptions.addNew ) {
                        allMappings.export(options.inPointOptions.mappingFile);
                    }
                }
            }
            if ( options.tokenOptions != null ) {
                if ( options.tokenOptions.doToken ) {
                    logger.info("Doing Tokenization");
                    // tokenize
                    var db = project.getDB();
                    var repo = db.getRepository( Sentence.class );
                    var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                    for ( Sentence s : documents ) {
                        // do serialize
                        if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                            try {
                                List<TokenInstance> tokens = allMappings.getTokenizer().tokenize(s.getWordIds());
                                s.setTokens( tokens );
                                repo.update( s );
                            } catch (TokenizerException te) {
                                logger.error("Unable to tokenize \"{}\" at **{}({})**", s.getText(),allMappings.getSerializer().unconvert(te.getCurrentWord()), te.getWordIndex());
                            }
                        }
                    }
                    if (options.tokenOptions.normalize) {
                        logger.info("Normalizing.");
                        // find and add acronyms
                        logger.info("Finding Acronyms.");
                        AcronymFinder.initializeAcronyms(project);
                        AcronymFinder.findAcronyms(project, allMappings.getSerializer());
                        var added = AcronymFinder.addAcronymsToMapping(project, allMappings.getSerializer());
                        if (added) {
                            allMappings.export(options.inPointOptions.mappingFile);
                        }
                        // normalize acronyms
                        logger.info("Normalizing Acronyms.");
                        AcronymNormalizer.normalizeAcronyms(project, allMappings.getSerializer());
                        // normalize bit accesses
                        logger.info("Normalizing Bit Accesses.");
                        BitAccessNormalizer.normalizeBitAccesses(project, allMappings.getFrameFinder(), allMappings.getSerializer());
                    }
                }
            }
            if (options.semanticExpressionOptions != null) {
                if (options.semanticExpressionOptions.doSemanticExpression) {
                    logger.info("Finding Semantic Expressions");
                    var db = project.getDB();
                    var repo = db.getRepository(Sentence.class);
                    var documents = repo.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT), FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                    for ( Sentence s : documents ) {
                        try {
                            var semexpr = allMappings.getSemanticParser().findSemanticExpression(s.getTokens(), allMappings.getFrameFinder());
                            if (semexpr.isPresent()) {
                                var se = semexpr.get();
                                s.setSemanticExpression(se);
                                if (logger.getLevel().equals(Level.DEBUG)) {
                                    printSemanticExpression(s, allMappings);
                                }
                                repo.update(s);
                            }
                        } catch (FrameException e) {
                            logger.warn("For The Sentence: {}", s.getText());
                            logger.warn(e.getMessage());
                        }
                    }
                }
            }
            if (options.irOptions != null) {
                if (options.irOptions.doGetIr) {
                    logger.info("Finding Intermediate Representation");
                    var db = project.getDB();
                    var repo = db.getRepository(Sentence.class);
                    var documents = repo.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT), FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                    for ( Sentence s : documents ) {
                        var se = s.getSemanticExpression();
                        if (se != null) {
                            try {
                                var ir = IRFinder.findIR(se, allMappings);
                                s.setIr(ir);
                                repo.update(s);
                            } catch (IRException e) {
                                logger.warn("For sentence: {}", s.getText());
                                logger.warn(e.getMessage());
                            }
                        } else {
                            logger.info("No SE for sentence {}", s.getSentenceId());
                        }
                    }
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
                        System.out.println( o );
                    }
                } else if ( options.debugOptions.doPercentage ) {
                    var results = DatasheetBOW.count_questionable( project );
                    double percentage = results.getRight().doubleValue() / ( results.getLeft().doubleValue() );
                    System.out.printf( "%%Q: %.2f%%\n", percentage * 100 );
                } else if ( options.debugOptions.doShowComments ) {
                    DatasheetBOW.debug_file_comments(project);
                } else if (options.debugOptions.doShowNonComments) {
                    var db = project.getDB();
                    var repo = db.getRepository( Sentence.class );
                    var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                    for ( Sentence s : documents ) {
                        // do serializee
                        if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                            logger.info( "{}", s.getText());
                        }
                    }
                } else if ( options.debugOptions.doShowNonCommentsMatches ) {
                    DatasheetBOW.debug_file_questionable( project );
                } else if ( options.debugOptions.doShowMatches ) {
                    DatasheetBOW.debug_file_matches( project );
                } else if ( options.debugOptions.doShowMostUsed ) {
                    DatasheetBOW.most_used( project );
                } else if ( options.debugOptions.doShowWordIds ) {
                    var db = project.getDB();
                    var repo = db.getRepository( Sentence.class );
                    var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                    for ( Sentence s : documents ) {
                        // do serializee
                        if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                            logger.info( "{}\n->\n{}", s.getText(), s.getWordIds() );
                        }
                    }
                } else if (options.debugOptions.doShowTokens) {
                    var db = project.getDB();
                    var repo = db.getRepository(Sentence.class);
                    var documents = repo.find(FindOptions.sort("sentenceId", SortOrder.Ascending));
                    for (Sentence s : documents) {
                        // do serialize
                        if (s.getType() == Sentence.Type.NONCOMMENT) {
                            var tokens = s.getTokens().stream()
                                    .map(TokenInstance::toString)
                                    .collect(Collectors.toList());
                            logger.info("{}\n->\n{}", s.getText(), tokens);
                        }
                    }
                } else if (options.debugOptions.doShowTokenText){
                    var db = project.getDB();
                    var repo = db.getRepository(Sentence.class);
                    var documents = repo.find(FindOptions.sort("sentenceId", SortOrder.Ascending));
                    for (Sentence s : documents) {
                        // do serialize
                        if (s.getType() == Sentence.Type.NONCOMMENT) {
                            var tokens = s.getTokens().stream()
                                    .map(t -> {
                                        if(t.getType() == TokenInstance.Type.ACCESS) {
                                            return t.toString();
                                        } else if (t.getType() == TokenInstance.Type.COMPOUND) {
                                            return t.getCompoundToken().getOriginalTokens().stream()
                                                    .map(ti -> {
                                                        if(ti.getType() == TokenInstance.Type.ACCESS) {
                                                            return ti.toString();
                                                        } else {
                                                            return Serializer.mergeWords(allMappings.getSerializer().deserialize(ti.getStream()));
                                                        }
                                                    })
                                                    .collect(Collectors.joining(" "));
                                        } else {
                                            return Serializer.mergeWords(allMappings.getSerializer().deserialize(t.getStream()));
                                        }
                                    })
                                    .collect(Collectors.toList());
                            logger.info("{}\n->\n{}", s.getText(), tokens);
                        }
                    }
                } else if (options.debugOptions.doShowAcronyms) {
                    var db = project.getDB();
                    if (db.hasRepository(Sentence.class)) {
                        var acronyms = db.getRepository(Acronym.class);
                        for (var acronym: acronyms.find()) {
                            logger.info("{} -> {}", acronym.getAcronym(), acronym.getExpanded());
                        }
                    }
                } else if (options.debugOptions.doShowFrameSearchTree) {
                    System.out.println(allMappings.getFrameFinder().getFrameSearchTree().toString());
                } else if (options.debugOptions.doShowTokenSearchTree) {
                    System.out.println(allMappings.getTokenizer().getTokenSearchTree().toString());
                } else if (options.debugOptions.doShowSemanticExpressions) {
                    var db = project.getDB();
                    var repo = db.getRepository(Sentence.class);
                    var documents = repo.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT), FindOptions.sort("sentenceId", SortOrder.Ascending));
                    for (var s: documents) {
                        printSemanticExpression(s, allMappings);
                    }
                } else if (options.debugOptions.doShowIR) {
                    var db = project.getDB();
                    var repo = db.getRepository(Sentence.class);
                    var documents = repo.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT), FindOptions.sort("sentenceId", SortOrder.Ascending));
                    for (var s: documents) {
                        logger.info("{} -> {}", s.getText(), s.getIr());
                    }
                }
            }
        }
    }

    public static void printSemanticExpression(Sentence s, AllMappings allMappings) {
        var se = s.getSemanticExpression();
        if (se != null) {
            var tokens = se.getAllFrames().stream()
                    .map(FrameInstance::getTokens)
                    .map(tlist -> tlist.stream()
                            .map(t -> {
                                if (t.getType() == TokenInstance.Type.ACCESS) {
                                    return t.toString();
                                } else if (t.getType() == TokenInstance.Type.COMPOUND) {
                                    return t.getCompoundToken().getOriginalTokens().stream()
                                            .map(ti -> {
                                                if (ti.getType() == TokenInstance.Type.ACCESS) {
                                                    return ti.toString();
                                                } else {
                                                    return Serializer.mergeWords(allMappings.getSerializer().deserialize(ti.getStream()));
                                                }
                                            })
                                            .collect(Collectors.joining(" "));
                                } else {
                                    return Serializer.mergeWords(allMappings.getSerializer().deserialize(t.getStream()));
                                }
                            })
                            .collect(Collectors.toList())
                    )
                    .collect(Collectors.toList());
            logger.info("{} -> {} ({})", s.getText(), se, tokens);
        } else {
            logger.warn("Sentence {} has no semantic expression!", s.getSentenceId());
        }
    }
}
