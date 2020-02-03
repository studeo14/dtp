package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.tokens.TokenInstance.BitAccessToken;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;

import java.util.List;

public class BitAccessNormalizerContext extends Context {

    private BitAccessToken bat;
    private List<TokenInstance> tokens;

    public BitAccessNormalizerContext() {
    }

    public BitAccessNormalizerContext(Context ctx, List<TokenInstance> tokens, BitAccessToken bat) {
        super(ctx);
        this.tokens = tokens;
        this.bat = bat;
    }

    public BitAccessNormalizerContext(String message, List<TokenInstance> tokens, BitAccessToken bat) {
        super(message);
        this.tokens = tokens;
        this.bat = bat;
    }

    public BitAccessToken getBat() {
        return bat;
    }

    public void setBat(BitAccessToken bat) {
        this.bat = bat;
    }

    public List<TokenInstance> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenInstance> tokens) {
        this.tokens = tokens;
    }
}
