package edu.vt.datasheet_text_processor.tokens.TokenInstance;

import java.util.ArrayList;
import java.util.List;

public class CompoundToken {
    private List<TokenInstance> originalTokens;

    public CompoundToken() {
        this.originalTokens = new ArrayList<>();
    }

    public CompoundToken(List<TokenInstance> originalTokens) {
        this.originalTokens = originalTokens;
    }

    private boolean checkOriginalTokens(CompoundToken other){
        if (originalTokens == null) {
            return other.getOriginalTokens() == null;
        } else {
            return originalTokens.equals(other.getOriginalTokens());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompoundToken) {
            var newO = (CompoundToken) obj;
            return checkOriginalTokens(newO);
        } else {
            return false;
        }
    }

    public List<TokenInstance> getOriginalTokens() {
        return originalTokens;
    }

    public void setOriginalTokens(List<TokenInstance> originalTokens) {
        this.originalTokens = originalTokens;
    }
}
