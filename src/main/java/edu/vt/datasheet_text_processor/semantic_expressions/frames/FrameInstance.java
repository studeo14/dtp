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

    private boolean checkId(FrameInstance other) {
        if (id == null) {
            return other.getId() == null;
        } else {
            return id.equals(other.getId());
        }
    }

    private boolean checkTokens(FrameInstance other) {
        if (tokens == null) {
            return other.getTokens() == null;
        } else {
            return tokens.equals(other.getTokens());
        }
    }

    private boolean checkLiterals(FrameInstance other) {
        if (literals == null) {
            return other.getLiterals() == null;
        } else {
            return literals.equals(other.getLiterals());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FrameInstance) {
            var newO = (FrameInstance) obj;
            return
                    checkId(newO) &&
                            checkTokens(newO) &&
                            checkLiterals(newO);
        } else {
            return false;
        }
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
                .filter(t -> t.getId().equals(Constants.LITERAL_TOKEN_ID))
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
