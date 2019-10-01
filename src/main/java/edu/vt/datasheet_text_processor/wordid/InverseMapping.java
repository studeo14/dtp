package edu.vt.datasheet_text_processor.wordid;

import java.util.Map;

public class InverseMapping {
    private Map<Integer, String> mapping;

    public InverseMapping() {
    }

    public InverseMapping(Map<Integer, String> mapping) {
        this.mapping = mapping;
    }

    public Map<Integer, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<Integer, String> mapping) {
        this.mapping = mapping;
    }
}
