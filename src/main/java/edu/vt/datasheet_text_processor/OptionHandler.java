package edu.vt.datasheet_text_processor;

import edu.vt.datasheet_text_processor.classification.DatasheetBOW;
import edu.vt.datasheet_text_processor.cli.Application;
import edu.vt.datasheet_text_processor.signals.Signal;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.Tokenizer;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenizerException;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import edu.vt.datasheet_text_processor.Sentence;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.dizitart.no2.Document.createDocument;

public class OptionHandler {
    private static final Logger logger = LogManager.getLogger( OptionHandler.class );

    public static void handle ( Project project, Application options ) throws IOException, TokenizerException {
        if (options.signalNames != null ) {
            // add signal names to the project
            // first check if the list is already present
            var db = project.getDB();
            var collection = db.getRepository(Signal.class); // creates if not already present
            // read in signal names
            for (String line: Files.readAllLines(options.signalNames.toPath())) {
                // split into name and acronyms
                var split = line.split("::");
                if (split.length == 2) {
                    var acronyms = Arrays.stream(split[1].split(","))
                            .map(String::toLowerCase)
                            .collect(Collectors.toList());
                    collection.insert(new Signal(split[0].toLowerCase(), acronyms));
                } else {
                    collection.insert(new Signal(split[0].toLowerCase()));
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
                var serializer = new Serializer( options.wordIDOptions.mappingFile );
                var db = project.getDB();
                var repo = db.getRepository( Sentence.class );
                var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                var t = new AddNewWrapper(options.wordIDOptions.addNew);
                for ( Sentence s : documents ) {
                    // do serialize
                    if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                        var wordIds = serializer.serialize( s.getText(), t );
                        s.setWordIds( wordIds );
                        repo.update( s );
                    }
                }
                if ( options.wordIDOptions.addNew ) {
                    serializer.exportMapping( options.wordIDOptions.mappingFile );
                }
            }
        }
        if ( options.tokenOptions != null && options.wordIDOptions != null) {
            logger.info("Doing Tokenization");
            if ( options.tokenOptions.doToken ) {
                var serializer = new Serializer( options.wordIDOptions.mappingFile );
                var tokenizer = new Tokenizer(options.tokenOptions.mappingFile, options.tokenOptions.compileTokens, serializer);
                System.out.print(tokenizer.getTokenSearchTree().toString());
                if (options.tokenOptions.compileTokens) {
                    var exportFileBase = FilenameUtils.removeExtension(options.tokenOptions.mappingFile.getName());
                    var exportFile = new File(exportFileBase + "_compiled.json");
                    tokenizer.exportMapping( exportFile );
                    logger.info("Exported compiled tokens to {}", exportFile.getName());
                }
                // tokenize
                var db = project.getDB();
                var repo = db.getRepository( Sentence.class );
                var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                for ( Sentence s : documents ) {
                    // do serialize
                    if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                        logger.info( "{}\n->\n{}", s.getText(), s.getWordIds() );
                        var tokens = tokenizer.tokenize(s.getWordIds());
                        s.setTokens( tokens );
                        repo.update( s );
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
                    var o = String.format( "%d :: %s :: %s", s.getSentenceId(), s.getType(), s.getText() );
                    System.out.println( o );
                }
            } else if ( options.debugOptions.doPercentage ) {
                var results = DatasheetBOW.count_questionable( project );
                double percentage = results.getRight().doubleValue() / ( results.getLeft().doubleValue() );
                System.out.printf( "%%Q: %.2f%%\n", percentage * 100 );
            } else if ( options.debugOptions.doShowComments ) {
                DatasheetBOW.debug_file_comments( project );
            } else if ( options.debugOptions.doShowNonComments ) {
                DatasheetBOW.debug_file_questionable( project );
            } else if ( options.debugOptions.doShowMatches ) {
                DatasheetBOW.debug_file_matches( project );
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
                var repo = db.getRepository( Sentence.class );
                var documents = repo.find( FindOptions.sort( "sentenceId", SortOrder.Ascending ) );
                for ( Sentence s : documents ) {
                    // do serialize
                    if ( s.getType() == Sentence.Type.NONCOMMENT ) {
                        var tokens = s.getTokens().stream()
                                .map(t -> String.format("%s::%s", t.getType(), t.getId()))
                                .collect(Collectors.toList());
                        logger.info( "{}\n->\n{}", s.getText(), tokens );
                    }
                }
            }
        }
    }
}
