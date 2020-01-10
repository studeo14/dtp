package edu.vt.datasheet_text_processor.wordid;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class WordTokenizerTest {
    static String[] tests = {
            "in addition",
            "this is a, test",
            "'1' is a value.",
            "when bit # is 23-16, then access is r."
    };

    static int[] resSizes = {
            2,
            5,
            7,
            13
    };

    @Test
    public void tokenize() {
        var res = Arrays.stream(tests)
                .map(WordTokenizer::tokenize)
                .map(List::size)
                .collect(Collectors.toList());
        for (int i = 0, resSize = res.size(); i < resSize; i++) {
            int r = res.get(i);
            assertEquals(r, resSizes[i]);
        }
    }
}