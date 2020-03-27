package edu.vt.datasheet_text_processor.Errors.Context;

import edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree.SearchTreeNode;

public class TokenizerContext extends Context {
    private Integer currentWord;
    private Integer wordIndex;
    private SearchTreeNode searchTreeNode;

    public TokenizerContext ( String message, Integer currentWord, Integer wordIndex, SearchTreeNode searchTreeNode ) {
        super( message );
        this.currentWord = currentWord;
        this.wordIndex = wordIndex;
        this.searchTreeNode = searchTreeNode;
    }

    public TokenizerContext ( Context ctx, Integer currentWord, Integer wordIndex, SearchTreeNode searchTreeNode ) {
        super( ctx );
        this.currentWord = currentWord;
        this.wordIndex = wordIndex;
        this.searchTreeNode = searchTreeNode;
    }

    public TokenizerContext() {}


    public Integer getCurrentWord() {
        return currentWord;
    }

    public Integer getWordIndex() {
        return wordIndex;
    }

    public SearchTreeNode getSearchTreeNode () {
        return searchTreeNode;
    }
}
