package edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree;

import edu.vt.datasheet_text_processor.util.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class FrameSearchTreeNode {
    private Integer tokenId;
    private Map<Integer, FrameSearchTreeNode> children;

    public FrameSearchTreeNode() {
        this.tokenId = Constants.DEFAULT_TOKEN_ID;
        this.children = new HashMap<>();
    }

    public FrameSearchTreeNode(Integer tokenId) {
        this.tokenId = tokenId;
        this.children = new HashMap<>();
    }

    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public Map<Integer, FrameSearchTreeNode> getChildren() {
        return children;
    }

    public void setChildren(Map<Integer, FrameSearchTreeNode> children) {
        this.children = children;
    }


    public String getString(int index) {
        var str = new StringBuilder();
        // add spaces
        str.append(StringUtils.repeat('.', index ));
        // add self
        str.append(tokenId);
        str.append('\n');
        // add children
        for (var child: children.values()) {
            str.append(child.getString(index + 1));
        }
        // new line
        return str.toString();
    }
}
