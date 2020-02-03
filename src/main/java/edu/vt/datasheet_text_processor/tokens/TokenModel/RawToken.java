package edu.vt.datasheet_text_processor.tokens.TokenModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import edu.vt.datasheet_text_processor.Errors.SerializerException;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonPropertyOrder({"id", "stream", "aliases"})
public class RawToken {
    private Integer id;
    private String stream;
    private List<String> aliases;

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("stream")
    public String getStream() {
        return stream;
    }

    @JsonProperty("stream")
    public void setStream(String stream) {
        this.stream = stream;
    }

    @JsonProperty("aliases")
    public List<String> getAliases() {
        return aliases;
    }

    @JsonProperty("aliases")
    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public RawToken(Integer id, String stream, List<String> aliases) {
        this.id = id;
        this.stream = stream;
        this.aliases = aliases;
    }

    public RawToken(Integer id, String stream) {
        this.id = id;
        this.stream = stream;
        this.aliases = new ArrayList<>();
    }

    public RawToken(){

    }

    public Token toToken(Serializer serializer) throws SerializerException {
        var convertedStream = serializer.serialize(stream, false);
        var convertedAliases = new ArrayList<List<Integer>>();
        for (String a : aliases) {
            List<Integer> serialize = serializer.serialize(a, false);
            convertedAliases.add(serialize);
        }
        return new Token(id, convertedStream, convertedAliases);
    }
}
