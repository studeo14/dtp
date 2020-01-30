package edu.vt.datasheet_text_processor.Errors.Context;

public class SerializerContext extends Context {
    private String word;
    private String sentence;

    public SerializerContext(String message, String word, String sentence) {
        super(message);
        this.word = word;
        this.sentence = sentence;
    }

    public SerializerContext(Context ctx, String word, String sentence) {
        super(ctx);
        this.word = word;
        this.sentence = sentence;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
}
