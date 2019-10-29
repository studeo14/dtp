package edu.vt.datasheet_text_processor.tokens.Tokenizer;

public class TokenizerException extends Throwable {
    private Integer currentWord;
    public TokenizerException(String s) {
        super(s);
    }
    public TokenizerException(String s, Integer currentWord) {
        super(s);
        this.currentWord = currentWord;
    }

    public Integer getCurrentWord() {
        return currentWord;
    }
}
