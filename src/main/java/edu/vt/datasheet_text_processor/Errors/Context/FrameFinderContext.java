package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;

import java.util.List;

public class FrameFinderContext extends Context {
    private List<TokenInstance> tokens;
    private FrameInstance frame;

    public FrameFinderContext(String message, List<TokenInstance> tokens, FrameInstance frame) {
        super(message);
        this.tokens = tokens;
        this.frame = frame;
    }

    public FrameFinderContext(Context ctx, List<TokenInstance> tokens, FrameInstance frame) {
        super(ctx);
        this.tokens = tokens;
        this.frame = frame;
    }

    public List<TokenInstance> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenInstance> tokens) {
        this.tokens = tokens;
    }

    public FrameInstance getFrame() {
        return frame;
    }

    public void setFrame(FrameInstance frame) {
        this.frame = frame;
    }
}
