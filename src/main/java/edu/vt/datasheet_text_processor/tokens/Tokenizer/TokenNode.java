package edu.vt.datasheet_text_processor.tokens.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class TokenNode {

    private Integer id;
    private List<TokenNode> children;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<TokenNode> getChildren() {
        return children;
    }

    public void setChildren(List<TokenNode> children) {
        this.children = children;
    }

    public TokenNode(Integer id) {
        this.id = id;
        this.children = new ArrayList<>();
    }
}
