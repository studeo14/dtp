package edu.vt.datasheet_text_processor;


import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticExpression;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Indices({
        @Index(value = "sentenceId", type = IndexType.NonUnique),
        @Index(value = "type", type = IndexType.NonUnique)
})
public class Sentence implements Serializable {
    public enum Type {
        COMMENT, NONCOMMENT, META, NA;
    }

    @Id
    private NitriteId empId;

    private Integer sentenceId;
    private Integer priority;
    private String text;
    private Type type;
    private List<Integer> wordIds;
    private List<TokenInstance> tokens;
    private SemanticExpression semanticExpression;

    public Sentence() {

    }

    public Sentence(Integer sentenceId, Integer priority, String text) {
        this.sentenceId = sentenceId;
        this.priority = priority;
        this.text = text;
        this.type = Type.NA;
        this.wordIds = new ArrayList<>();
        this.tokens = new ArrayList<>();
    }

    public NitriteId getEmpId() {
        return empId;
    }

    public void setEmpId(NitriteId empId) {
        this.empId = empId;
    }

    public Integer getSentenceId() {
        return sentenceId;
    }

    public void setSentenceId(Integer sentenceId) {
        this.sentenceId = sentenceId;
    }

    public Integer getPriority () {
        return priority;
    }

    public void setPriority ( Integer priority ) {
        this.priority = priority;
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

    public List<TokenInstance> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenInstance> tokens) {
        this.tokens = tokens;
    }

    public SemanticExpression getSemanticExpression() {
        return semanticExpression;
    }

    public void setSemanticExpression(SemanticExpression semanticExpression) {
        this.semanticExpression = semanticExpression;
    }
}
