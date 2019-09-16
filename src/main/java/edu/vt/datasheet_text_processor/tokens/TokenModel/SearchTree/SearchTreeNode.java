package edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class SearchTreeNode {

    private Integer wordId;
    private Map<Integer, SearchTreeNode> children;

    public Integer getWordId() {
        return wordId;
    }

    public void setWordId(Integer wordId) {
        this.wordId = wordId;
    }

    public Map<Integer, SearchTreeNode> getChildren() {
        return children;
    }

    public void setChildren(Map<Integer, SearchTreeNode> children) {
        this.children = children;
    }

    public SearchTreeNode(Integer wordId) {
        this.wordId = wordId;
        this.children = new HashMap<>();
    }

    public String getString(int index) {
        var str = new StringBuilder();
        // add spaces
        str.append(StringUtils.repeat('.', index ));
        // add self
        str.append(wordId);
        str.append('\n');
        // add children
        for (var child: children.values()) {
            str.append(child.getString(index + 1));
        }
        // new line
        str.append('\n');
        return str.toString();
    }
}
