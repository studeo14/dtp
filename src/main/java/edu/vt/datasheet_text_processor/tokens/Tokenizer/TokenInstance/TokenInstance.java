package edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance;

import java.util.ArrayList;
import java.util.List;

public class TokenInstance {
    public enum Type {TOKEN, LITERAL};

    private Type type;
    private List<Integer> stream;
    private Integer id;

    public TokenInstance() {
        this.stream = new ArrayList<>();
    }

    public TokenInstance(Type type) {
        this.type = type;
        this.stream = new ArrayList<>();
    }

    public TokenInstance(Type type, List<Integer> stream) {
        this.type = type;
        this.stream = stream;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Integer> getStream() {
        return stream;
    }

    public void setStream(List<Integer> stream) {
        this.stream = stream;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TokenInstance(Type type, List<Integer> stream, Integer id) {
        this.type = type;
        this.stream = stream;
        this.id = id;
    }
}
