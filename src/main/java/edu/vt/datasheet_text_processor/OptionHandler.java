package edu.vt.datasheet_text_processor;

import edu.vt.datasheet_text_processor.cli.CLIOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OptionHandler {
    private static final Logger logger = LogManager.getLogger(OptionHandler.class);

    public static void handle(Project project, CLIOptions options) throws SQLException {
        if (options.doPrint) {
            // print all from database
            var result = project.getConnection()
                    .createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                    .executeQuery("SELECT sentence FROM sentences ORDER BY id");
            logger.info("{} Columns", result.getMetaData().getColumnCount());
            while(result.next()) {
                System.out.println(result.getString(1));
            }
        }
        if (options.doClassify) {
            // create classify table
            project.getConnection().createStatement().executeUpdate("CREATE TABLE comments (id INTEGER, )");
        }
        if (options.doWordId) {

        }
        if (options.doToken) {

        }
    }
}
