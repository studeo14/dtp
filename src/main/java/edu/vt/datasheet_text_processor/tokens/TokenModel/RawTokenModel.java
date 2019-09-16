package edu.vt.datasheet_text_processor.tokens.TokenModel;

import edu.vt.datasheet_text_processor.wordid.Serializer;

import java.util.HashMap;

public class RawTokenModel extends HashMap<Integer, RawToken> {

    public TokenModel toTokenModel(Serializer serializer) {
        var newTokenModel = new TokenModel();
        values().stream()
                .map(rt -> rt.toToken(serializer))
                .forEach(t -> newTokenModel.put(t.getId(), t));
        return newTokenModel;
    }
}
