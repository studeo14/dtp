package edu.vt.datasheet_text_processor.tokens.TokenInstance;

import edu.vt.datasheet_text_processor.util.Constants;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TokenInstance {
    public enum Type {NA, TOKEN, LITERAL, ACCESS, COMPOUND}

    private Type type;
    private Integer id;
    private List<Integer> stream;
    private BitAccessToken bitAccessToken;
    private CompoundToken compoundToken;

    public TokenInstance () {
        this.stream = new ArrayList<>();
        this.id = Constants.DEFAULT_TOKEN_ID;
    }

    public TokenInstance(Type type) {
        this.type = type;
        this.id = Constants.DEFAULT_TOKEN_ID;
        this.stream = new ArrayList<>();
    }

    public TokenInstance(Type type, List<Integer> stream) {
        this.type = type;
        this.id = Constants.DEFAULT_TOKEN_ID;
        this.stream = stream;
    }

    public TokenInstance(Type type, List<Integer> stream, Integer id) {
        this.type = type;
        this.stream = stream;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Integer> getStream() {
        if (type == Type.COMPOUND) {
            return compoundToken.getOriginalTokens().stream()
                    .map(TokenInstance::getStream)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            return stream;
        }
    }

    public void setStream(List<Integer> stream) {
        this.stream = stream;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BitAccessToken getBitAccessToken() {
        return bitAccessToken;
    }

    public void setBitAccessToken(BitAccessToken bitAccessToken) {
        this.bitAccessToken = bitAccessToken;
    }

    public void setBitAccessToken(BitAccessToken bitAccessToken, Serializer serializer) {
        var t = new AddNewWrapper(false);
        this.stream = serializer.serialize(bitAccessToken.toString(), t);
        this.bitAccessToken = bitAccessToken;
    }

    public CompoundToken getCompoundToken() {
        return compoundToken;
    }

    public void setCompoundToken(CompoundToken compoundToken) {
        this.compoundToken = compoundToken;
    }

    @Override
    public String toString() {
        if (getType() == Type.ACCESS) {
            return getBitAccessToken().toString();
        } else {
            return String.format("%s::%d", getType(), getId());
        }
    }
}
