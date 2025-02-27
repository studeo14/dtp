package edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree;

import edu.vt.datasheet_text_processor.util.Constants;
import org.apache.commons.lang3.StringUtils;

public class SearchTreeNode {

    private Integer wordId;
    private SearchTreeNodeChildren children;

    public Integer getWordId() {
        return wordId;
    }

    public void setWordId(Integer wordId) {
        this.wordId = wordId;
    }

    public SearchTreeNodeChildren getChildren() {
        return children;
    }

    public void setChildren(SearchTreeNodeChildren children) {
        this.children = children;
    }

    public SearchTreeNode() {
        this.wordId = Constants.DEFAULT_WORD_ID;
        this.children = new SearchTreeNodeChildren();
    }

    public SearchTreeNode(Integer wordId) {
        this.wordId = wordId;
        this.children = new SearchTreeNodeChildren();
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
        return str.toString();
    }
}
