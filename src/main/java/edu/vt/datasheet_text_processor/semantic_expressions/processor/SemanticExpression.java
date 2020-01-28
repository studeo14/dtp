package edu.vt.datasheet_text_processor.semantic_expressions.processor;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SemanticExpression {
    private List<FrameInstance> antecedents;
    private List<FrameInstance> consequents;
    private List<FrameInstance> allFrames;
    private List<List<String>> tokenText;

    public SemanticExpression() {
        this.antecedents = new ArrayList<>();
        this.consequents = new ArrayList<>();
        this.allFrames = new ArrayList<>();
    }

    public SemanticExpression(List<FrameInstance> antecedents, List<FrameInstance> consequents, List<FrameInstance> allFrames) {
        this.antecedents = antecedents;
        this.consequents = consequents;
        this.allFrames = allFrames;
    }

    private boolean checkAntecedents(SemanticExpression other) {
        if (antecedents == null) {
            return other.getAntecedents() == null;
        } else {
            return antecedents.equals(other.getAntecedents());
        }
    }

    private boolean checkConsequents(SemanticExpression other) {
        if (consequents == null) {
            return other.getConsequents() == null;
        } else {
            return consequents.equals(other.getConsequents());
        }
    }

    private boolean checkAllFrames(SemanticExpression other) {
        if (allFrames == null) {
            return other.getAllFrames() == null;
        } else {
            return allFrames.equals(other.getAllFrames());
        }
    }

    private boolean checkTokenText(SemanticExpression other) {
        if (tokenText == null) {
            return other.getTokenText() == null;
        } else {
            return tokenText.equals(other.getTokenText());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SemanticExpression) {
            var newO = (SemanticExpression) obj;
            return
                    checkAntecedents(newO) &&
                            checkConsequents(newO) &&
                            checkAllFrames(newO) &&
                            checkTokenText(newO);
        } else {
            return false;
        }
    }

    public void setAllFrames(List<FrameInstance> allFrames) {
        this.allFrames = allFrames;
    }

    public List<FrameInstance> getAllFrames() {
        return allFrames;
    }

    public List<FrameInstance> getAntecedents() {
        return antecedents;
    }

    public void setAntecedents(List<FrameInstance> antecedents) {
        this.antecedents = antecedents;
    }

    public List<FrameInstance> getConsequents() {
        return consequents;
    }

    public void setConsequents(List<FrameInstance> consequents) {
        this.consequents = consequents;
    }

    public void setTokenText(List<List<String>> seTT) {
        this.tokenText = seTT;
    }

    public List<List<String>> getTokenText() {
        return tokenText;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        var af = allFrames.stream()
                .map(FrameInstance::getId)
                .collect(Collectors.toList());
        var a = antecedents.stream()
                .map(FrameInstance::getId)
                .collect(Collectors.toList());
        var c = consequents.stream()
                .map(FrameInstance::getId)
                .collect(Collectors.toList());
        sb.append("AF: ");
        sb.append(af);
        sb.append("\tA: ");
        sb.append(a);
        sb.append("\tC: ");
        sb.append(c);
        return sb.toString();
    }
}
