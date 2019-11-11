package edu.vt.datasheet_text_processor.semantic_expressions.processor;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemanticExpression {
    private List<FrameInstance> antecedents;
    private List<FrameInstance> consequents;
    private List<FrameInstance> allFrames;

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
