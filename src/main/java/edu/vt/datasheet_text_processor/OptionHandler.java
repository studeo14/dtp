package edu.vt.datasheet_text_processor;

import edu.vt.datasheet_text_processor.classification.DatasheetBOW;
import edu.vt.datasheet_text_processor.cli.Application;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;

import java.io.IOException;
import java.sql.SQLException;

public class OptionHandler {
    private static final Logger logger = LogManager.getLogger(OptionHandler.class);

    public static void handle(Project project, Application options) throws IOException {
        if (options.doClassify) {
            // create classify table
            var db = project.getDB();
            var repo = db.getRepository(Sentence.class);
            var documents = repo.find(FindOptions.sort("sentenceId", SortOrder.Ascending));
            for (Sentence s : documents) {
                // do classify
                var isComment = DatasheetBOW.is_questionable(s.getText());
                if (isComment) {
                    s.setType(Sentence.Type.NONCOMMENT);
                } else {
                    s.setType(Sentence.Type.COMMENT);
                }
                repo.update(s);
            }
        }
        if (options.wordIDOptions != null) {
            if (options.wordIDOptions.doWordId) {
                logger.info("Doing wid");
                var serializer = new Serializer(options.wordIDOptions.mappingFile);
                var db = project.getDB();
                var repo = db.getRepository(Sentence.class);
                var documents = repo.find(FindOptions.sort("sentenceId", SortOrder.Ascending));
                for (Sentence s : documents) {
                    // do serializee
                    if (s.getType() == Sentence.Type.NONCOMMENT) {
                        var wordIds = serializer.serialize(s.getText());
                        s.setWordIds(wordIds);
                        repo.update(s);
                        logger.info("{}\n->\n{}", s.getText(), wordIds);
                    }
                }

            }
        }
        if (options.doToken) {

        }
        if (options.debugOptions != null) {
            if (options.debugOptions.doPrint) {
                // print all from database
                var db = project.getDB();
                var repo = db.getRepository(Sentence.class);
                var documents = repo.find(FindOptions.sort("sentenceId", SortOrder.Ascending));
                for (Sentence s : documents) {
                    var o = String.format("%d :: %s :: %s", s.getSentenceId(), s.getType(), s.getText());
                    System.out.println(o);
                }
            } else if (options.debugOptions.doPercentage) {
                var results = DatasheetBOW.count_questionable(project);
                double percentage = results.getRight().doubleValue() / (results.getLeft().doubleValue());
                System.out.printf("%%Q: %.2f%%\n", percentage * 100);
            } else if (options.debugOptions.doShowComments) {
                DatasheetBOW.debug_file_comments(project);
            } else if (options.debugOptions.doShowNonComments) {
                DatasheetBOW.debug_file_questionable(project);
            } else if (options.debugOptions.doShowMatches) {
                DatasheetBOW.debug_file_matches(project);
            } else if (options.debugOptions.doShowWordIds) {
                var db = project.getDB();
                var repo = db.getRepository(Sentence.class);
                var documents = repo.find(FindOptions.sort("sentenceId", SortOrder.Ascending));
                for (Sentence s : documents) {
                    // do serializee
                    if (s.getType() == Sentence.Type.NONCOMMENT) {
                        logger.info("{}\n->\n{}", s.getText(), s.getWordIds());
                    }
                }

            }
        }
    }
}
