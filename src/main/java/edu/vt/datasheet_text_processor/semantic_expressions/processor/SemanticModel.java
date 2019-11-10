package edu.vt.datasheet_text_processor.semantic_expressions.processor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"antecedents","consequents"})
public class SemanticModel {

    private List<Integer> antecedents;
    private List<Integer> consequents;

    public SemanticModel() {
    }

    public SemanticModel(List<Integer> antecedents, List<Integer> consequents) {
        this.antecedents = antecedents;
        this.consequents = consequents;
    }

    @JsonProperty("antecedents")
    public List<Integer> getAntecedents() {
        return antecedents;
    }

    @JsonProperty("antecedents")
    public void setAntecedents(List<Integer> antecedents) {
        this.antecedents = antecedents;
    }

    @JsonProperty("consequents")
    public List<Integer> getConsequents() {
        return consequents;
    }

    @JsonProperty("consequents")
    public void setConsequents(List<Integer> consequents) {
        this.consequents = consequents;
    }
}
