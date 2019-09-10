package edu.vt.datasheet_text_processor.wordid;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.SimpleTokenizer;
/**
 * Class that takes in a wordid mapping dataset and sentence
 * and returns a stream (List) of word ids (integers) instead
 */
public class Serializer {
    public enum WordIDClass {
        JUNK, OBJECT, VERB, NUMBER, MODIFIER, ADJECTIVE, PRONOUN, CONDITION
    }
    public enum VerbEndings {
        NONE, S, ED, ING
    }
    private Mapping mapping;
    private static final Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

    public Serializer(File mappingFile) throws IOException {
        // try to read in the file
        this.mapping = new ObjectMapper().readValue(mappingFile, Mapping.class);
    }

    public List<Integer> serialize(String sentence) {
        // convert to list of base words (stemming)
        return Arrays.stream(tokenizer.tokenize(sentence))
                .map(this::stem)
                .map(this::convert)
                .collect(Collectors.toList());

    }

    private String stem(String input) {
        return mapping.getAliases().getOrDefault(input, input);
    }

    public WordIDClass getWordIdClass(Integer wordId) {
        var classNum = (wordId / 10000);

        switch (classNum) {
            default:
            case 0:
                return WordIDClass.JUNK;
            case 1:
                return WordIDClass.OBJECT;
            case 2:
                return WordIDClass.VERB;
            case 3:
                return WordIDClass.NUMBER;
            case 4:
                return WordIDClass.MODIFIER;
            case 5:
                return WordIDClass.ADJECTIVE;
            case 6:
                return WordIDClass.PRONOUN;
            case 7:
                return WordIDClass.CONDITION;
        }
    }

    public Integer getWordIdNumber(Integer wordId) {
        return (wordId / 10) % 1000;
    }

    public Integer getWordIdOption(Integer wordId) {
        return (wordId % 10);
    }

    public VerbEndings getVerbEnding(Integer wordId) {
        if (getWordIdClass(wordId) == WordIDClass.VERB) {
            switch (getWordIdOption(wordId)) {
                default:
                case 0:
                    return VerbEndings.NONE;
                case 1:
                    return VerbEndings.ING;
                case 2:
                    return VerbEndings.ED;
                case 3:
                    return VerbEndings.S;
            }
        }
        return VerbEndings.NONE;
    }

    private Integer convert(String input) {
        // get base convertion
        var base = mapping.getBaseMapping().getOrDefault(input, 0);
        var baseClass = getWordIdClass(base);
        if (baseClass == WordIDClass.VERB) {
            // look at ending
            if (input.endsWith("ing")) {
                base += 1;
            } else if (input.endsWith("ed")) {
                base += 2;
            } else if (input.endsWith("s")) {
                base += 3;
            }
        } else if (baseClass == WordIDClass.OBJECT) {
            if (input.endsWith("s")) {
                base += 3;
            }
        }
        return base;
    }
}
