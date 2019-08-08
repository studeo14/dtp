package edu.vt.datasheet_text_processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.Nitrite;

import java.sql.Connection;

public class Project {
    private Logger logger;
    private String name;
    private Nitrite db;

    public Project(String name) {
        this.name = name;
        this.logger = LogManager.getLogger(String.format("%s:%s", Project.class, name));
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
}
