package edu.vt.datasheet_text_processor.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.tokens.TokenModel.RawTokenModel;
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
@JsonPropertyOrder({"wordIdMapping", "tokenMapping"})
public class AllMappingsRaw {
    // need a serializer, tokenizer
    private Mapping wordIdMapping;
    private RawTokenModel tokenMapping;
    private Serializer serializer;
    private Tokenizer tokenizer;

    public AllMappingsRaw() {}

    public void init() {
        this.serializer = new Serializer(wordIdMapping);
        this.tokenizer = new Tokenizer(tokenMapping, serializer);
    }

    public void export(File outputFile) throws IOException {
        // create regular all mappings
        var allMappings = new AllMappings();
        allMappings.setWordIdMapping(wordIdMapping);
        allMappings.setTokenMapping(tokenizer.getTokenModel());
        new ObjectMapper().writeValue(outputFile, allMappings);
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
    public RawTokenModel getTokenMapping() {
        return tokenMapping;
    }

    @JsonProperty("tokenMapping")
    public void setTokenMapping(RawTokenModel tokenMapping) {
        this.tokenMapping = tokenMapping;
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
}
