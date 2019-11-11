package edu.vt.datasheet_text_processor.semantic_expressions.frames;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"bitAccess", "generic"})
public class FrameModel {
    private FrameTemplateModel bitAccess;
    private FrameTemplateModel generic;

    public FrameModel() {
        bitAccess = new FrameTemplateModel();
        generic = new FrameTemplateModel();
    }

    public FrameModel(FrameTemplateModel bitAccess, FrameTemplateModel generic) {
        this.bitAccess = bitAccess;
        this.generic = generic;
    }

    @JsonProperty("bitAccess")
    public FrameTemplateModel getBitAccess() {
        return bitAccess;
    }

    @JsonProperty("bitAccess")
    public void setBitAccess(FrameTemplateModel bitAccess) {
        this.bitAccess = bitAccess;
    }

    @JsonProperty("generic")
    public FrameTemplateModel getGeneric() {
        return generic;
    }

    @JsonProperty("generic")
    public void setGeneric(FrameTemplateModel generic) {
        this.generic = generic;
    }
}
