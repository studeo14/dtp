package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;

import java.util.List;

public class IRCompoundContext extends Context {

    private FrameInstance problemFrame;
    private List<FrameInstance> otherFrames;

    public IRCompoundContext () {}

    public IRCompoundContext(String message, FrameInstance problemFrame, List<FrameInstance> otherFrames) {
        super(message);
        this.problemFrame = problemFrame;
        this.otherFrames = otherFrames;
    }

    public IRCompoundContext(Context ctx, FrameInstance problemFrame, List<FrameInstance> otherFrames) {
        super(ctx);
        this.problemFrame = problemFrame;
        this.otherFrames = otherFrames;
    }

    public FrameInstance getProblemFrame() {
        return problemFrame;
    }

    public void setProblemFrame(FrameInstance problemFrame) {
        this.problemFrame = problemFrame;
    }

    public List<FrameInstance> getOtherFrames() {
        return otherFrames;
    }

    public void setOtherFrames(List<FrameInstance> otherFrames) {
        this.otherFrames = otherFrames;
    }
}
