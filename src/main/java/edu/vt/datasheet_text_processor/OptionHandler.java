package edu.vt.datasheet_text_processor;

import edu.vt.datasheet_text_processor.cli.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;

import java.sql.SQLException;

public class OptionHandler {
    private static final Logger logger = LogManager.getLogger(OptionHandler.class);

    public static void handle(Project project, Application options) throws SQLException {
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
        if (options.doWordId) {

        }
        if (options.doToken) {

        }
        if (options.doPrint) {
            // print all from database
            var db = project.getDB();
            var repo = db.getRepository(Sentence.class);
            var documents = repo.find(FindOptions.sort("sentenceId", SortOrder.Ascending));
            for (Sentence s : documents) {
                var o = String.format("%d :: %s :: %s", s.getSentenceId(), s.getType(), s.getText());
                System.out.println(o);
            }
        }
    }
}
