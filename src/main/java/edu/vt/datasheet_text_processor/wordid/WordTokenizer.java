package edu.vt.datasheet_text_processor.wordid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WordTokenizer {
    private final static Logger logger = LogManager.getLogger(WordTokenizer.class);
    public final static Pattern WORD = Pattern.compile("\\w");
    public final static Pattern WHITESPACE = Pattern.compile("\\s");
    public final static Pattern PUNCTUATION = Pattern.compile("[.,;:!?]");

    public enum State {
        WORD, WHITESPACE, NONWORD, PUNCTUACTION
    }

    public static List<String> tokenize(String sentence) {
        var retList = new ArrayList<String>();
        var sb = new StringBuilder();
        var firstChar = sentence.substring(0,1);
        sb.append(firstChar);
        State state;
        if (WORD.matcher(firstChar).find()) {
            state = State.WORD;
        } else if (WHITESPACE.matcher(firstChar).find()) {
            state = State.WHITESPACE;
        } else if (PUNCTUATION.matcher(firstChar).find()) {
            state = State.PUNCTUACTION;
        } else {
            state = State.NONWORD;
        }
        logger.debug("Starting State: {}", state);
        for(var ix = 1; ix < sentence.length(); ix++) {
            var cur = sentence.substring(ix, ix+1);
            switch (state) {
                case WORD:
                    if (WORD.matcher(cur).find()) {
                        sb.append(cur);
                    } else if (WHITESPACE.matcher(cur).find()) {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        state = State.WHITESPACE;
                    } else if (PUNCTUATION.matcher(cur).find()) {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(cur);
                        state = State.PUNCTUACTION;
                    } else {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(cur);
                        state = State.NONWORD;
                    }
                    break;
                case WHITESPACE:
                    if (WORD.matcher(cur).find()) {
                        sb.append(cur);
                        state = State.WORD;
                    } else if (WHITESPACE.matcher(cur).find()) {
                        // keep ignoring
                    } else if (PUNCTUATION.matcher(cur).find()) {
                        sb.append(cur);
                        state = State.PUNCTUACTION;
                    } else {
                        sb.append(cur);
                        state = State.NONWORD;
                    }
                    break;
                case NONWORD:
                    if (WORD.matcher(cur).find()) {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(cur);
                        state = State.WORD;
                    } else if (WHITESPACE.matcher(cur).find()) {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        state = State.WHITESPACE;
                    } else if (PUNCTUATION.matcher(cur).find()) {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(cur);
                        state = State.PUNCTUACTION;
                    } else {
                        sb.append(cur);
                    }
                    break;
                case PUNCTUACTION:
                    if (WORD.matcher(cur).find()) {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(cur);
                        state = State.WORD;
                    } else if (WHITESPACE.matcher(cur).find()) {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        state = State.WHITESPACE;
                    } else if (PUNCTUATION.matcher(cur).find()) {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(cur);
                        state = State.PUNCTUACTION;
                    } else {
                        retList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(cur);
                        state = State.NONWORD;
                    }
                    break;
            }
            logger.debug("New State: {}", state);
        }
        // add whatever is left
        if (!sb.toString().isBlank()) {
            retList.add(sb.toString());
        }
        return retList;
    }
}
