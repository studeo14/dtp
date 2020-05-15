package edu.vt.datasheet_text_processor;

import org.dizitart.no2.*;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.objects.Cursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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

    /**
     * Update a sentence in the repository.
     * @param sentence
     * @return true if the sentence was updated
     */
    public boolean updateSentence(Sentence sentence) {
        var wr = db.getRepository(Sentence.class).update(sentence);
        return wr.getAffectedCount() > 0;
    }

    /**
     * Get the raw collection of metrics
     * @return
     */
    public NitriteCollection getMetricCollection() {
        return db.getCollection("metrics");
    }

    /**
     * Get Cursor of Metrics
     * @return
     */
    public org.dizitart.no2.Cursor getMetrics() {
        return db.getCollection("metrics").find();
    }

    /**
     * Get a single metric by name
     * @param name
     * @return
     */
    public Optional<Document> getMetric(String name) {
        var c = getMetricCollection().find(Filters.eq("name", name)).toList();
        if (c.size() > 0) {
            return Optional.of(c.get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Update a metric by name
     * @param name
     * @param updated
     * @return
     */
    public boolean updateMetric(String name, Document updated) {
        var wr = getMetricCollection().update(Filters.eq("name", name), updated);
        return wr.getAffectedCount() > 0;
    }

    /**
     * Update with upsert support
     * @param name
     * @param updated
     * @param upsert
     * @return
     */
    public boolean updateMetric(String name, Document updated, boolean upsert) {
        var wr = getMetricCollection().update(Filters.eq("name", name), updated, UpdateOptions.updateOptions(upsert));
        return wr.getAffectedCount() > 0;
    }

    /**
     * Add Given Metric
     * @param metric
     * @return
     */
    public boolean addMetric(Document metric) {
        var wr = getMetricCollection().insert(metric);
        return wr.getAffectedCount() > 0;
    }

    /**
     * Add Given Metric and give a name
     * @param metric
     * @return
     */
    public boolean addMetric(Document metric, String name) {
        metric.put("name", name);
        var wr = getMetricCollection().insert(metric);
        return wr.getAffectedCount() > 0;
    }

    public boolean removeMetric(String name) {
        var we = getMetricCollection().remove(Filters.eq("name", name));
        return we.getAffectedCount() > 0;
    }

    /**
     * Saves changes to the database and closes the file connections.
     */
    public void close() {
        if (getDB().hasUnsavedChanges()) {
            getDB().commit();
        }
        getDB().close();
    }

}
