package edu.vt.datasheet_text_processor.tokens.Tokenizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.tokens.TokenModel.RawTokenModel;
import edu.vt.datasheet_text_processor.tokens.TokenModel.TokenModel;
import edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree.TokenSearchTree;
import edu.vt.datasheet_text_processor.wordid.Serializer;

import java.io.File;
import java.io.IOException;

public class Tokenizer {

    private TokenModel tokenModel;
    private TokenSearchTree tokenSearchTree;
    private Serializer serializer;

    public Tokenizer(File mappingFile, boolean useRaw, Serializer serializer) throws IOException {
        this.serializer = serializer;
        if (useRaw) {
            var rawTokenModel = new ObjectMapper().readValue(mappingFile, RawTokenModel.class);
            this.tokenModel = rawTokenModel.toTokenModel(serializer);
        } else {
            this.tokenModel = new ObjectMapper().readValue(mappingFile, TokenModel.class);
        }
        this.tokenSearchTree = new TokenSearchTree(this.tokenModel);
    }

    public TokenModel getTokenModel() {
        return tokenModel;
    }

    public TokenSearchTree getTokenSearchTree() {
        return tokenSearchTree;
    }

    public Serializer getSerializer() {
        return serializer;
    }
}
