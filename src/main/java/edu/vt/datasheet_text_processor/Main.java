package edu.vt.datasheet_text_processor;

import edu.vt.datasheet_text_processor.cli.CLIOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String... args) {
        var options = new CLIOptions();
        var cli = new CommandLine(options);
        try {
            cli.parseArgs(args);
            handleCli(cli, options);
        } catch (CommandLine.MissingParameterException | IOException | SQLException e) {
            logger.error(e.getMessage());
        }
    }

    private static void handleCli(CommandLine cli, CLIOptions options) throws IOException, SQLException {
        if (cli.isUsageHelpRequested()) {
            cli.usage(System.out);
        } else {
            Project project;
            if (options.newProject) {
                // import new project
                project = ProjectUtils.createNewProject(options.inputFile);
            } else {
                project = ProjectUtils.openProject(options.inputFile);
            }
            OptionHandler.handle(project, options);
            project.getConnection().close();
        }
    }
}
