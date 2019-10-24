package edu.vt.datasheet_text_processor.semantic_expressions.frames;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"id", "template", "aliases"})
public class FrameTemplate {
    private Integer id;
    private List<Integer> template;
    private List<List<Integer>> aliases;

    public FrameTemplate() {
    }

    public FrameTemplate(Integer id, List<Integer> template) {
        this.id = id;
        this.template = template;
        this.aliases = new ArrayList<>();
    }

    public FrameTemplate(Integer id, List<Integer> template, List<List<Integer>> aliases) {
        this.id = id;
        this.template = template;
        this.aliases = aliases;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("template")
    public List<Integer> getTemplate() {
        return template;
    }

    @JsonProperty("template")
    public void setTemplate(List<Integer> template) {
        this.template = template;
    }

    @JsonProperty("aliases")
    public List<List<Integer>> getAliases() {
        return aliases;
    }

    @JsonProperty("aliases")
    public void setAliases(List<List<Integer>> aliases) {
        this.aliases = aliases;
    }
}
