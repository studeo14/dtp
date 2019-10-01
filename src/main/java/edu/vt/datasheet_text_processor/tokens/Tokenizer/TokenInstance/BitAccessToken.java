package edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class BitAccessToken extends TokenInstance {
    private Pair<Integer, Integer> bits;

    public BitAccessToken(Type type, List<Integer> stream, Integer id, Pair<Integer, Integer> bits) {
        super(type, stream, id);
        this.bits = bits;
    }

    public Pair<Integer, Integer> getBits() {
        return bits;
    }

    public void setBits(Pair<Integer, Integer> bits) {
        this.bits = bits;
    }
}
