package edu.vt.datasheet_text_processor;

import org.dizitart.no2.FindOptions;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.Cursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Project {
    private Logger logger;
    private String name;
    private Nitrite db;

    public Project(String name) {
        this.name = name;
        this.logger = LoggerFactory.getLogger(String.format("%s:%s", Project.class, name));
    }

    public String getName() {
        return name;
    }

    public void setDB(Nitrite db) {
        this.db = db;
    }

    public Nitrite getDB() {
        return db;
    }

    public Cursor<Sentence> getSentences() {
        return db.getRepository(Sentence.class).find(FindOptions.sort("sentenceId", SortOrder.Ascending));
    }
}
