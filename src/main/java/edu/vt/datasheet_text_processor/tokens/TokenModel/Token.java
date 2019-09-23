package edu.vt.datasheet_text_processor.tokens.TokenModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"id", "stream", "aliases"})
public class Token {

    private Integer id;
    private List<Integer> stream;
    private List<List<Integer>> aliases;

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("stream")
    public List<Integer> getStream() {
        return stream;
    }

    @JsonProperty("stream")
    public void setStream(List<Integer> stream) {
        this.stream = stream;
    }

    @JsonProperty("aliases")
    public List<List<Integer>> getAliases() {
        return aliases;
    }

    @JsonProperty("aliases")
    public void setAliases(List<List<Integer>> aliases) {
        this.aliases = aliases;
    }

    public Token() {

    }

    public Token(Integer id, List<Integer> stream, List<List<Integer>> aliases) {
        this.id = id;
        this.stream = stream;
        this.aliases = aliases;
    }

    public Token(Integer id, List<Integer> stream) {
        this.id = id;
        this.stream = stream;
        this.aliases = new ArrayList<>();
    }
}
