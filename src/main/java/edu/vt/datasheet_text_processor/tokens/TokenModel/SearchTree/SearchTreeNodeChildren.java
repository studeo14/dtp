package edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree;

import edu.vt.datasheet_text_processor.wordid.WordIdUtils;

import java.util.HashMap;

public class SearchTreeNodeChildren extends HashMap<Integer, SearchTreeNode> {

    public boolean contains(Integer key) {
        return this.keySet().stream()
                .anyMatch(i -> WordIdUtils.getBase(i).equals(WordIdUtils.getBase(key)));
    }
}
