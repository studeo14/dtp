package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree.FrameSearchTreeNode;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;

import java.util.List;

public class FrameFinderContext extends Context {
    private List<TokenInstance> tokens;
    private FrameInstance frame;
    private FrameSearchTreeNode currentNode;

    public FrameFinderContext() {
    }

    public FrameFinderContext(String message, List<TokenInstance> tokens, FrameInstance frame, FrameSearchTreeNode currentNode) {
        super(message);
        this.tokens = tokens;
        this.frame = frame;
        this.currentNode = currentNode;
    }

    public FrameFinderContext(Context ctx, List<TokenInstance> tokens, FrameInstance frame, FrameSearchTreeNode currentNode) {
        super(ctx);
        this.tokens = tokens;
        this.frame = frame;
        this.currentNode = currentNode;
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

    public FrameSearchTreeNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(FrameSearchTreeNode currentNode) {
        this.currentNode = currentNode;
    }
}
