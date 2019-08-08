package edu.vt.datasheet_text_processor;

import edu.vt.datasheet_text_processor.cli.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.sql.SQLException;

class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String... args) {
        var options = new Application();
        var cli = new CommandLine(options);
        try {
            cli.parseArgs(args);
            handleCli(cli, options);
        } catch (CommandLine.MissingParameterException | IOException | SQLException e) {
            logger.error(e.getMessage());
        } catch (RuntimeException e) {
            logger.fatal("Unknown runtime exception!: {}", e.getMessage());
            logger.fatal(e.getCause());
            e.printStackTrace();
        }
    }

    private static void handleCli(CommandLine cli, Application options) throws IOException, SQLException {
        if (cli.isUsageHelpRequested()) {
            cli.usage(System.out);
        } else {
            Project project;
            if (options.newProject) {
                // import new project
                project = ProjectUtils.createNewProject(options.inputFile, options.text);
            } else {
                project = ProjectUtils.openProject(options.inputFile);
            }
            if (project.getDB() == null) {
                logger.error("Unable to open project");
            } else {
                OptionHandler.handle(project, options);
                if (project.getDB().hasUnsavedChanges()) {
                    project.getDB().commit();
                }
                project.getDB().close();
            }
        }
    }
}
