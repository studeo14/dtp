package edu.vt.datasheet_text_processor.tokens.TokenModel;

import edu.vt.datasheet_text_processor.Errors.SerializerException;
import edu.vt.datasheet_text_processor.wordid.Serializer;

import java.util.HashMap;

public class RawTokenModel extends HashMap<Integer, RawToken> {

    public TokenModel toTokenModel(Serializer serializer) throws SerializerException {
        var newTokenModel = new TokenModel();
        for (RawToken rt : values()) {
            Token t = rt.toToken(serializer);
            newTokenModel.put(t.getId(), t);
        }
        return newTokenModel;
    }
}
