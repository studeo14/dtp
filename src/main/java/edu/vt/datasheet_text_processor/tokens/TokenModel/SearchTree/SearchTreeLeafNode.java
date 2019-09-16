package edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree;

import org.apache.commons.lang3.StringUtils;

public class SearchTreeLeafNode extends SearchTreeNode{
    private Integer tokenId;

    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public SearchTreeLeafNode(Integer wordId, Integer tokenId) {
        super(wordId);
        this.tokenId = tokenId;
    }

    @Override
    public String getString(int index) {
        var str = new StringBuilder();
        // add spaces
        str.append(StringUtils.repeat('.', index ));
        // add self
        str.append(getWordId());
        str.append(" :: ");
        // add tokenid
        str.append(tokenId);
        // new line
        str.append('\n');
        return str.toString();
    }
}
