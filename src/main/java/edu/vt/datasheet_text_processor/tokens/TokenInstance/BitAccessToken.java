package edu.vt.datasheet_text_processor.tokens.TokenInstance;

import java.util.List;

public class BitAccessToken {
    private BitIndex bits;
    private String registerName;
    private List<TokenInstance> originalTokens;

    public BitAccessToken() { }

    public BitAccessToken(String registerName, List<TokenInstance> originalToken) {
        this.originalTokens = originalToken;
        this.registerName = registerName;
    }

    public BitAccessToken(String registerName, BitIndex bits, List<TokenInstance> originalToken) {
        this.bits = bits;
        this.originalTokens = originalToken;
        this.registerName = registerName;
    }

    public BitAccessToken(String registerName, Integer bit, List<TokenInstance> originalToken) {
        this.bits = new BitIndex(bit);
        this.originalTokens = originalToken;
        this.registerName = registerName;
    }

    public BitAccessToken(String registerName, Integer bitx, Integer bity, List<TokenInstance> originalToken) {
        this.bits = new BitIndex(bitx, bity);
        this.originalTokens = originalToken;
        this.registerName = registerName;
    }

    private boolean checkBits(BitAccessToken other){
        if (bits == null) {
            return other.getBits() == null;
        } else {
            return bits.equals(other.getBits());
        }
    }

    private boolean checkRegisterName(BitAccessToken other){
        if (registerName == null) {
            return other.getRegisterName() == null;
        } else {
            return registerName.equals(other.getRegisterName());
        }
    }

    private boolean checkOriginalTokens(BitAccessToken other){
        if (originalTokens == null) {
            return other.getOriginalTokens() == null;
        } else {
            return originalTokens.equals(other.getOriginalTokens());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BitAccessToken) {
            var newO = (BitAccessToken)obj;
            return
                    checkBits(newO) &&
                            checkRegisterName(newO) &&
                            checkOriginalTokens(newO);

        } else {
            return false;
        }
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public BitIndex getBits() {
        return bits;
    }

    public void setBits(BitIndex bits) {
        this.bits = bits;
    }

    public List<TokenInstance> getOriginalTokens() {
        return originalTokens;
    }

    public void setOriginalTokens(List<TokenInstance> originalToken) {
        this.originalTokens = originalToken;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(getRegisterName());
        sb.append('[');
        sb.append(bits.toString());
        sb.append(']');
        return sb.toString();
    }
}
