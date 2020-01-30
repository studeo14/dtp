package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticExpression;

public class IRContext extends Context {
    private SemanticExpression semanticExpression;
    private FrameInstance problemFrame;

    public IRContext(String message, SemanticExpression semanticExpression, FrameInstance problemFrame) {
        super(message);
        this.semanticExpression = semanticExpression;
        this.problemFrame = problemFrame;
    }

    public IRContext(Context ctx, SemanticExpression semanticExpression, FrameInstance problemFrame) {
        super(ctx);
        this.semanticExpression = semanticExpression;
        this.problemFrame = problemFrame;
    }

    public SemanticExpression getSemanticExpression() {
        return semanticExpression;
    }

    public void setSemanticExpression(SemanticExpression semanticExpression) {
        this.semanticExpression = semanticExpression;
    }

    public FrameInstance getProblemFrame() {
        return problemFrame;
    }

    public void setProblemFrame(FrameInstance problemFrame) {
        this.problemFrame = problemFrame;
    }
}
