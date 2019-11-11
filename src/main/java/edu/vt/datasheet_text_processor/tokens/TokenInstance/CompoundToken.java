package edu.vt.datasheet_text_processor.tokens.TokenInstance;

import java.util.List;

public class CompoundToken {
    private List<TokenInstance> originalTokens;

    public CompoundToken(List<TokenInstance> originalTokens) {
        this.originalTokens = originalTokens;
    }

    public List<TokenInstance> getOriginalTokens() {
        return originalTokens;
    }

    public void setOriginalTokens(List<TokenInstance> originalTokens) {
        this.originalTokens = originalTokens;
    }
}
