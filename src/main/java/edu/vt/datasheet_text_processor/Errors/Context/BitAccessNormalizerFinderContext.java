package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;

import java.util.List;

public class BitAccessNormalizerFinderContext extends Context {
    private List<TokenInstance> tokens;
    private FrameInstance frameInstance;
    public BitAccessNormalizerFinderContext(String message, List<TokenInstance> tokens, FrameInstance frame) {
        super(message);
        this.tokens = tokens;
        this.frameInstance = frame;
    }

    public List<TokenInstance> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenInstance> tokens) {
        this.tokens = tokens;
    }

    public FrameInstance getFrameInstance() {
        return frameInstance;
    }

    public void setFrameInstance(FrameInstance frameInstance) {
        this.frameInstance = frameInstance;
    }
}
