package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;

import java.util.List;

public class IRConsequentContext extends Context {

    private FrameInstance frame;
    private String nameS;
    private String descS;

    public IRConsequentContext() {
    }

    public IRConsequentContext(String message, FrameInstance frame, String nameS, String descS) {
        super(message);
        this.frame = frame;
        this.nameS = nameS;
        this.descS = descS;
    }

    public IRConsequentContext(Context ctx, FrameInstance frame, String nameS, String descS) {
        super(ctx);
        this.frame = frame;
        this.nameS = nameS;
        this.descS = descS;
    }

    public FrameInstance getFrame() {
        return frame;
    }

    public void setFrame(FrameInstance frame) {
        this.frame = frame;
    }

    public String getNameS() {
        return nameS;
    }

    public void setNameS(String nameS) {
        this.nameS = nameS;
    }

    public String getDescS() {
        return descS;
    }

    public void setDescS(String descS) {
        this.descS = descS;
    }
}
