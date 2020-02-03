package edu.vt.datasheet_text_processor.tokens.TokenInstance;

import edu.vt.datasheet_text_processor.Errors.SerializerException;
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
        this.id = Constants.UNINITIALIZED_TOKEN_ID;
    }

    public TokenInstance(Type type) {
        this.type = type;
        this.id = Constants.UNINITIALIZED_TOKEN_ID;
        this.stream = new ArrayList<>();
    }

    public TokenInstance(Type type, List<Integer> stream) {
        this.type = type;
        this.id = Constants.UNINITIALIZED_TOKEN_ID;
        this.stream = stream;
    }

    public TokenInstance(Type type, List<Integer> stream, Integer id) {
        this.type = type;
        this.stream = stream;
        this.id = id;
    }

    private boolean checkType(TokenInstance other) {
        if (type == null) {
            return other.getType() == null;
        } else {
            return type.equals(other.getType());
        }
    }

    private boolean checkId(TokenInstance other) {
        if (id == null) {
            return other.getId() == null;
        } else {
            return id.equals(other.getId());
        }
    }

    private boolean checkStream(TokenInstance other) {
        if (getStream() == null) {
            return other.getStream() == null;
        } else {
            return getStream().equals(other.getStream());
        }
    }

    private boolean checkBitAccessToken(TokenInstance other) {
        if (bitAccessToken == null) {
            return other.getBitAccessToken() == null;
        } else {
            return bitAccessToken.equals(other.getBitAccessToken());
        }
    }

    private boolean checkCompoundToken(TokenInstance other) {
        if (compoundToken == null) {
            return other.getCompoundToken() == null;
        } else {
            return compoundToken.equals(other.getCompoundToken());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TokenInstance) {
            var other = (TokenInstance) obj;
            return
                    checkType(other) &&
                            checkId(other) &&
                            checkStream(other) &&
                            checkBitAccessToken(other) &&
                            checkCompoundToken(other);
        } else {
            return false;
        }
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

    public void setBitAccessToken(BitAccessToken bitAccessToken, Serializer serializer) throws SerializerException {
        this.stream = serializer.serialize(bitAccessToken.toString(), false);
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
