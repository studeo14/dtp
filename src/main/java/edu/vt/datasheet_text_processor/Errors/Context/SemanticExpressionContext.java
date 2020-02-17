package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticExpression;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;

import java.util.List;

public class SemanticExpressionContext extends Context {
    private List<TokenInstance> tokens;
    private List<FrameInstance> frames;
    private SemanticExpression semanticExpression;

    public SemanticExpressionContext() {
    }

    public SemanticExpressionContext(String message, List<TokenInstance> tokens, List<FrameInstance> frames, SemanticExpression semanticExpression) {
        super(message);
        this.tokens = tokens;
        this.frames = frames;
        this.semanticExpression = semanticExpression;
    }

    public SemanticExpressionContext(Context ctx, List<TokenInstance> tokens, List<FrameInstance> frames, SemanticExpression semanticExpression) {
        super(ctx);
        this.tokens = tokens;
        this.frames = frames;
        this.semanticExpression = semanticExpression;
    }

    public List<TokenInstance> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenInstance> tokens) {
        this.tokens = tokens;
    }

    public List<FrameInstance> getFrames() {
        return frames;
    }

    public void setFrames(List<FrameInstance> frames) {
        this.frames = frames;
    }

    public SemanticExpression getSemanticExpression() {
        return semanticExpression;
    }

    public void setSemanticExpression(SemanticExpression semanticExpression) {
        this.semanticExpression = semanticExpression;
    }
}
