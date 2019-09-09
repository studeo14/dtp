package edu.vt.datasheet_text_processor.wordid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;

@JsonPropertyOrder({"aliases", "baseMapping"})
public class Mapping {
    private Map<String, String> aliases;
    private Map<String, Integer> baseMapping;

    @JsonProperty("aliases")
    public Map<String, String> getAliases() {
        return aliases;
    }

    @JsonProperty("aliases")
    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases;
    }

    @JsonProperty("baseMapping")
    public Map<String, Integer> getBaseMapping() {
        return baseMapping;
    }

    @JsonProperty("baseMapping")
    public void setBaseMapping(Map<String, Integer> baseMapping) {
        this.baseMapping = baseMapping;
    }
}
