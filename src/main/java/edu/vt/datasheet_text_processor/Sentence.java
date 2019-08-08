package edu.vt.datasheet_text_processor;


import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Indices({
        @Index(value = "sentenceId", type = IndexType.Unique),
        @Index(value = "type", type = IndexType.NonUnique)
})
public class Sentence implements Serializable {
    public enum Type {
        COMMENT, NONCOMMENT, NA;
    }

    @Id
    private NitriteId empId;

    private long sentenceId;
    private String text;
    private Type type;
    private List<Integer> wordIds;
//    private List<Token> tokens;

    public Sentence() {

    }

    public Sentence(long sentenceId, String text) {
        this.sentenceId = sentenceId;
        this.text = text;
        this.type = Type.NA;
        this.wordIds = new ArrayList<>();
    }

    public NitriteId getEmpId() {
        return empId;
    }

    public void setEmpId(NitriteId empId) {
        this.empId = empId;
    }

    public long getSentenceId() {
        return sentenceId;
    }

    public void setSentenceId(long sentenceId) {
        this.sentenceId = sentenceId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Integer> getWordIds() {
        return wordIds;
    }

    public void setWordIds(List<Integer> wordIds) {
        this.wordIds = wordIds;
    }
}
