package edu.vt.datasheet_text_processor;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.Nitrite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ProjectUtils {
    private static final Logger logger = LogManager.getLogger(ProjectUtils.class);

    public static Project createNewProject(File inputFile, boolean textOnly) throws IOException {
        var projectName = FilenameUtils.removeExtension(inputFile.getName());
        var project = new Project(projectName);
        var db = Nitrite.builder()
                .compressed()
                .filePath(projectName + ".project")
                .openOrCreate();
        project.setDB(db);
        if (db.hasRepository(Sentence.class)) {
            logger.info("Found existing project when trying to create a new one at {}.project", projectName);
            return project;
        } else {
            var repo = db.getRepository(Sentence.class);
            Integer index = 1;
            for (String line : Files.readAllLines(inputFile.toPath())) {
                line = line.toLowerCase();
                if (textOnly) {
                    var result = repo.insert(new Sentence(index, line));
                    index += 1;
                } else {
                    var splits = line.split("::");
                    var id = Integer.parseInt(splits[0]);
                    var newSentence = new Sentence(id, splits[2].toLowerCase());
                    // check for meta tag
                    if (splits[1].equals("M") || splits[1].equals("m")) {
                        newSentence.setType(Sentence.Type.META);
                    }
                    var result = repo.insert(newSentence);
                }
            }
            logger.info("Created new project at {}.project", projectName);
        }
        return project;
    }

    public static Project openProject(File inputFile) {
        var projectName = FilenameUtils.removeExtension(inputFile.getName());
        var project = new Project(projectName);
        var db = Nitrite.builder()
                .compressed()
                .filePath(project.getName() + ".project")
                .openOrCreate();
        if (!db.hasRepository(Sentence.class)) {
            logger.error("Unable to open project. Not initialized or available in {}.project", project.getName());
        } else {
            logger.info("Opened project {}.", project.getName());
            project.setDB(db);
        }
        return project;
    }
}
