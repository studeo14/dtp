package edu.vt.datasheet_text_processor.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameFinder;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameModel;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticModel;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticParser;
import edu.vt.datasheet_text_processor.tokens.TokenModel.TokenModel;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.Tokenizer;
import edu.vt.datasheet_text_processor.wordid.Mapping;
import edu.vt.datasheet_text_processor.wordid.Serializer;

import java.io.File;
import java.io.IOException;

/**
 * Input POJO that has all of the mappings in one object. This makes it easier to share the mapping objects
 * across operations rather than tying them to a single operation.
 *
 */
@JsonPropertyOrder({"wordIdMapping", "tokenMapping", "frameMapping", "semanticModel"})
@JsonIgnoreProperties({"serializer", "tokenizer", "frameFinder", "semanticParser"})
public class AllMappings {
    // need a serializer, tokenizer
    private Mapping wordIdMapping;
    private TokenModel tokenMapping;
    private FrameModel frameMapping;
    private SemanticModel semanticModel;
    // tools
    private Serializer serializer;
    private Tokenizer tokenizer;
    private FrameFinder frameFinder;
    private SemanticParser semanticParser;

    public AllMappings() {}

    public void init() {
        this.serializer = new Serializer(wordIdMapping);
        this.tokenizer = new Tokenizer(tokenMapping);
        this.frameFinder = new FrameFinder(frameMapping);
        this.semanticParser = new SemanticParser(semanticModel);
    }

    public void export(File outputFile) throws IOException {
        new ObjectMapper().writeValue(outputFile, this);
    }

    @JsonProperty("wordIdMapping")
    public Mapping getWordIdMapping() {
        return wordIdMapping;
    }

    @JsonProperty("wordIdMapping")
    public void setWordIdMapping(Mapping wordIdMapping) {
        this.wordIdMapping = wordIdMapping;
    }

    @JsonProperty("tokenMapping")
    public TokenModel getTokenMapping() {
        return tokenMapping;
    }

    @JsonProperty("tokenMapping")
    public void setTokenMapping(TokenModel tokenMapping) {
        this.tokenMapping = tokenMapping;
    }

    @JsonProperty("frameMapping")
    public FrameModel getFrameMapping() {
        return frameMapping;
    }

    @JsonProperty("frameMapping")
    public void setFrameMapping(FrameModel frameMapping) {
        this.frameMapping = frameMapping;
    }

    @JsonProperty("semanticModel")
    public SemanticModel getSemanticModel() {
        return semanticModel;
    }

    @JsonProperty("semanticModel")
    public void setSemanticModel(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    public void setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public FrameFinder getFrameFinder() {
        return frameFinder;
    }

    public void setFrameFinder(FrameFinder frameFinder) {
        this.frameFinder = frameFinder;
    }

    public SemanticParser getSemanticParser() {
        return semanticParser;
    }

    public void setSemanticParser(SemanticParser semanticParser) {
        this.semanticParser = semanticParser;
    }
}
