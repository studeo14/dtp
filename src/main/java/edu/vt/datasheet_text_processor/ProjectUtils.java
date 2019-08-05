package edu.vt.datasheet_text_processor;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ProjectUtils {
    private static final Logger logger = LogManager.getLogger(ProjectUtils.class);

    public static Project createNewProject(File inputFile) throws IOException, SQLException {
        var projectName = FilenameUtils.removeExtension(inputFile.getName());
        var project = new Project(projectName);
        // create project dir
        var dir = new File(projectName);
        if (dir.exists()) {
            return openProject(dir);
        } else if (!dir.mkdir()) {
            logger.error("Unable to create project directory: {}", projectName);
        } else {
            // read in file to hypersql database
            var connection = DriverManager
                .getConnection(
                        String.format("jdbc:hsqldb:file:./%s/%s", projectName, projectName),
                        "SA", ""
                );
            // create table
            connection.createStatement().executeUpdate("CREATE TABLE sentences (id INTEGER, sentence VARCHAR(1024));");
            for (String line : Files.readAllLines(inputFile.toPath())) {
                var splits = line.split("::");
                var id = Integer.parseInt(splits[0]);
                var statement = connection.prepareStatement("INSERT INTO sentences(id, sentence) VALUES (?,?);");
                statement.clearParameters();
                statement.setInt(1, id);
                statement.setString(2, splits[1]);
                statement.execute();
                statement.close();
            }
            connection.commit();
            project.setConnection(connection);
            logger.info("New Project {} created in ./{}", projectName, projectName);
        }
        return project;
    }

    public static Project openProject(File inputFile) throws SQLException {
        var project = new Project(inputFile.getName());
        var connection = DriverManager
            .getConnection(
                    String.format("jdbc:hsqldb:file:./%s/%s;ifexists=true", project.getName(), project.getName()),
                    "SA", ""
            );
        project.setConnection(connection);
        logger.info("Opened project {}.", project.getName());
        return project;
    }
}
