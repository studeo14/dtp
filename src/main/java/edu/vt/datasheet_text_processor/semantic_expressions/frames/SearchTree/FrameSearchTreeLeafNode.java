package edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree;

import edu.vt.datasheet_text_processor.util.Constants;
import org.apache.commons.lang3.StringUtils;

public class FrameSearchTreeLeafNode extends FrameSearchTreeNode {
    private Integer frameId;

    public FrameSearchTreeLeafNode(Integer frameId) {
        super(Constants.SEARCH_TREE_LEAF_NODE_ID);
        this.frameId = frameId;
    }

    public Integer getFrameId() {
        return frameId;
    }
    @Override
    public String getString(int index) {
        var str = new StringBuilder();
        // add spaces
        str.append(StringUtils.repeat('.', index ));
        // add self
        str.append(getTokenId());
        str.append(" :: ");
        // add tokenid
        str.append(frameId);
        // new line
        str.append('\n');
        return str.toString();
    }
}
