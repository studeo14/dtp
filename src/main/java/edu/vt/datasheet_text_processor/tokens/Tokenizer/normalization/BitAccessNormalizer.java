package edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization;

import edu.vt.datasheet_text_processor.Project;
import edu.vt.datasheet_text_processor.Sentence;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameFinder;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.BitAccessToken;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenizerException;
import edu.vt.datasheet_text_processor.util.Constants;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Takes in a LiteralToken and converts it to a standard BitAccessToken format
 */
public class BitAccessNormalizer {
    private static final Logger logger = LoggerFactory.getLogger(BitAccessNormalizer.class);

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern[] NUMBER_WORD_PATTERNS = {
            Pattern.compile("first"),
            Pattern.compile("second"),
            Pattern.compile("third"),
            Pattern.compile("fourth"),
            Pattern.compile("fifth"),
            Pattern.compile("sixth"),
            Pattern.compile("seventh"),
            Pattern.compile("eighth"),
            Pattern.compile("ninth"),
            Pattern.compile("tenth"),
            Pattern.compile("eleventh"),
            Pattern.compile("twelfth")
    };

    public static void normalizeBitAccesses(Project project, FrameFinder frameFinder, Serializer serializer) throws TokenizerException {
        var db = project.getDB();
        if (db.hasRepository(Sentence.class)) {
            var sentences = db.getRepository(Sentence.class);
            var nonComments = sentences.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT));
            for (var sentence: nonComments) {
                List<TokenInstance> tokens = sentence.getTokens();
                var res = getNormalizedBitAccess(tokens, frameFinder, serializer);
                if (res.isPresent()) {
                    // replace tokens with new tokenInstance
                    var bat = res.get();
                    var sublistIndex = Collections.indexOfSubList(tokens, bat.getOriginalTokens());
                    if (sublistIndex == -1) {
                        throw new TokenizerException("Unable to replace sublist of tokens, not found");
                    }
                    // else
                    // clear original tokens from token list
                    var window = tokens.subList(sublistIndex, sublistIndex + bat.getOriginalTokens().size());
                    window.clear();
                    // create new token replacement
                    var newToken = new TokenInstance(TokenInstance.Type.ACCESS, null, Constants.LITERAL_TOKEN_ID);
                    // add bat and make sure that the stream is created
                    newToken.setBitAccessToken(bat, serializer);
                    // add in place of old tokens
                    window.add(newToken);
                    sentences.update(sentence);
                }
            }
        }
    }

    /**
     * Search tokens for frames 0-3 (BAT frames)
     * If present break out literals and place into BAT
     * @param tokens
     * @param frameFinder
     * @return possible FrameInstance
     */
    static Optional<BitAccessToken> getNormalizedBitAccess(List<TokenInstance> tokens, FrameFinder frameFinder, Serializer serializer) throws TokenizerException {
        // check if present
        var frameRes = findBitAccess(tokens, frameFinder);
        if (frameRes.isPresent()) {
            var frame = frameRes.get();
            switch (frame.getId()) {
                // two literals
                // first is bit
                // second is reg
                case 0:
                {
                    var bitx_str = Serializer.mergeWords(serializer.deserialize(frame.get(0)));
                    var bitx = normalizeBit(bitx_str);
                    var reg = Serializer.mergeWords(serializer.deserialize(frame.get(1)));
                    var bat = new BitAccessToken(reg, bitx, frame.getTokens());
                    return Optional.of(bat);
                }
                // two literals
                // first is reg
                // second is bit
                case 1:
                {
                    var reg = Serializer.mergeWords(serializer.deserialize(frame.get(0)));
                    var bitx_str = Serializer.mergeWords(serializer.deserialize(frame.get(1)));
                    var bitx = normalizeBit(bitx_str);
                    var bat = new BitAccessToken(reg, bitx, frame.getTokens());
                    return Optional.of(bat);
                }
                // three literals
                // first is reg
                // second is bitx
                // third is bity
                case 2:
                {
                    var reg = Serializer.mergeWords(serializer.deserialize(frame.get(0)));
                    var bitx_str = Serializer.mergeWords(serializer.deserialize(frame.get(1)));
                    var bitx = normalizeBit(bitx_str);
                    var bity_str = Serializer.mergeWords(serializer.deserialize(frame.get(2)));
                    var bity = normalizeBit(bity_str);
                    var bat = new BitAccessToken(reg, bitx, bity, frame.getTokens());
                    return Optional.of(bat);
                }
                // three literals
                // first is bitx
                // second is bity
                // third is reg
                case 3:
                {
                    var bitx_str = Serializer.mergeWords(serializer.deserialize(frame.get(0)));
                    var bitx = normalizeBit(bitx_str);
                    var bity_str = Serializer.mergeWords(serializer.deserialize(frame.get(1)));
                    var bity = normalizeBit(bity_str);
                    var reg = Serializer.mergeWords(serializer.deserialize(frame.get(2)));
                    var bat = new BitAccessToken(reg, bitx, bity, frame.getTokens());
                    return Optional.of(bat);
                }
            }
        }
        return Optional.empty();
    }

    private static Integer normalizeBit(String literal) throws TokenizerException {
        var digitMatch = NUMBER_PATTERN.matcher(literal);
        if (digitMatch.find()) {
            return Integer.parseInt(digitMatch.group(0));
        } else {
            for (int i = 0, number_word_patternsLength = NUMBER_WORD_PATTERNS.length; i < number_word_patternsLength; i++) {
                Pattern pattern = NUMBER_WORD_PATTERNS[i];
                var wordMatch = pattern.matcher(literal);
                if (wordMatch.find()) {
                    return i + 1;
                }
            }
        }
        throw new TokenizerException(String.format("Unable to parse literal '%s' into integer", literal));
    }


    /**
     * Helper method to see if a BAT is present in the sentence
     */
    static Optional<FrameInstance> findBitAccess(List<TokenInstance> tokens, FrameFinder frameFinder) {
        for (int i = 0; i < 4; i++) {
            var temp = frameFinder.findFrame(tokens, i);
            if (temp.isPresent()) {
                return temp;
            }
        }
        return Optional.empty();
    }

}
