package edu.vt.datasheet_text_processor.semantic_expressions.frames;

import edu.vt.datasheet_text_processor.util.Constants;

import java.util.HashMap;

public class FrameTemplateModel extends HashMap<Integer, FrameTemplate> {

    public long numLiterals(Integer frameId) {
        if (containsKey(frameId)) {
            return get(frameId).getTemplate().stream()
                    .filter(id -> id.equals(Constants.LITERAL_TOKEN_ID))
                    .count();
        } else {
            return 0;
        }
    }
}
