package edu.vt.datasheet_text_processor.semantic_expressions.frames;

import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Programmatic representation of a semantic frame
 *
 * These can be full sentences or sentence fragments
 *
 * Made up of 1+ tokens
 */
public class FrameInstance {
    private Integer id;
    private List<TokenInstance> tokens;
    private List<List<Integer>> literals;

    public FrameInstance(Integer id, List<TokenInstance> tokens, List<List<Integer>> literals) {
        this.id = id;
        this.tokens = tokens;
        this.literals = literals;
    }

    public FrameInstance(Integer id, List<TokenInstance> tokens) {
        this.id = id;
        this.tokens = tokens;
        this.literals = tokens.stream()
                .filter(t -> t.getId().equals(Constants.LITERAL_TOKEN_ID))
                .map(TokenInstance::getStream)
                .collect(Collectors.toList());
    }

    public FrameInstance(Integer id) {
        this.id = id;
        this.tokens = new ArrayList<>();
        this.literals = new ArrayList<>();
    }

    public FrameInstance() {
        this.tokens = new ArrayList<>();
        this.literals = new ArrayList<>();
    }

    public List<Integer> get(Integer i) {
        return literals.get(i);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<TokenInstance> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenInstance> tokens) {
        this.tokens = tokens;
    }

    public void setTokensAndLiterals(List<TokenInstance> tokens) {
        this.tokens = tokens;
        this.literals = tokens.stream()
                .filter(t -> t.getType() == TokenInstance.Type.LITERAL || t.getType() == TokenInstance.Type.COMPOUND)
                .map(TokenInstance::getStream)
                .collect(Collectors.toList());
    }

    public List<List<Integer>> getLiterals() {
        return literals;
    }

    public void setLiterals(List<List<Integer>> literals) {
        this.literals = literals;
    }
}
