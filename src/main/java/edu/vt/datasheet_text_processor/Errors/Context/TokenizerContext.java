package edu.vt.datasheet_text_processor.Errors.Context;

public class TokenizerContext extends Context {
    private Integer currentWord;
    private Integer wordIndex;

    public TokenizerContext(String message, Integer currentWord, Integer wordIndex) {
        super(message);
        this.currentWord = currentWord;
        this.wordIndex = wordIndex;
    }

    public Integer getCurrentWord() {
        return currentWord;
    }

    public Integer getWordIndex() {
        return wordIndex;
    }
}
