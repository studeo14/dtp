package edu.vt.datasheet_text_processor.tokens.Tokenizer;

public class LiteralTokenNode extends TokenNode {

    private String literal;

    public LiteralTokenNode(Integer id, String literal) {
        super(id);
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }
}
