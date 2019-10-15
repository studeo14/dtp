package edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance;

import org.apache.commons.lang3.tuple.Pair;

public class BitAccessToken {
    private Pair<Integer, Integer> bits;
    private String registerName;
    private TokenInstance originalToken;

    public BitAccessToken(String registerName, TokenInstance originalToken) {
        this.originalToken = originalToken;
        this.registerName = registerName;
    }

    public BitAccessToken(String registerName, Pair<Integer, Integer> bits, TokenInstance originalToken) {
        this.bits = bits;
        this.originalToken = originalToken;
        this.registerName = registerName;
    }

    public BitAccessToken(String registerName, Integer bit, TokenInstance originalToken) {
        this.bits = Pair.of(bit, -1);
        this.originalToken = originalToken;
        this.registerName = registerName;
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public Pair<Integer, Integer> getBits() {
        return bits;
    }

    public void setBits(Pair<Integer, Integer> bits) {
        this.bits = bits;
    }

    public TokenInstance getOriginalToken() {
        return originalToken;
    }

    public void setOriginalToken(TokenInstance originalToken) {
        this.originalToken = originalToken;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(getRegisterName());
        sb.append('[');
        sb.append(getBits().getLeft());
        if (getBits().getRight() != -1) {
            sb.append(':');
            sb.append(getBits().getRight());
        }
        sb.append(']');
        return sb.toString();
    }
}
