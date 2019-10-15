package edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization;

import edu.vt.datasheet_text_processor.Project;
import edu.vt.datasheet_text_processor.Sentence;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance.BitAccessToken;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes in a LiteralToken and converts it to a standard BitAccessToken format
 */
public class BitAccessNormalizer {
    private static final Logger logger = LogManager.getLogger(BitAccessNormalizer.class);

    private static final Pattern[] accessPatterns = {
            Pattern.compile("bit (.+?) of (.+)"),
            Pattern.compile("(.+?) bit of (.+)"),
            Pattern.compile("(.+?)\\[(.+?)\\]"),
            Pattern.compile("(.+?)\\[(.+?):(.+?)\\]"),
            Pattern.compile("from bit (.+) to (.+?) of (.+)"),
            Pattern.compile("from the (.+) bit to the (.+?) bit of (.+)"),
            Pattern.compile("bits (.+) through (.+?) of (.+)"),
    };

    public static void normalizeBitAccesses(Project project, Serializer serializer) {
        var db = project.getDB();
        if (db.hasRepository(Sentence.class)) {
            var sentences = db.getRepository(Sentence.class);
            var nonComments = sentences.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT));
            for (var sentence: nonComments) {
                List<TokenInstance> tokens = sentence.getTokens();
                for (int i = 0, tokensSize = tokens.size(); i < tokensSize; i++) {
                    final var iRef = i;
                    TokenInstance token = tokens.get(i);
                    if (token.getType() == TokenInstance.Type.LITERAL) {
                        var bat = getNormalizedBitAccess(token, serializer);
                        bat.ifPresent(token::setBitAccessToken);
                    }
                }
            }
        }
    }

    static Optional<BitAccessToken> getNormalizedBitAccess(TokenInstance token, Serializer serializer) {
        // first find if this token is applicable
        if (token.getType() == TokenInstance.Type.LITERAL) {
            var bitAccessMatch = hasBitAccess(token, serializer);
            if (bitAccessMatch.isPresent()) {
                var matchPair = bitAccessMatch.get();
                var matcher = matchPair.getLeft();
                var patternIndex = matchPair.getRight();
                logger.debug("Match found: {}, {}", matcher.pattern(), patternIndex);
                // extract match data
                switch (matchPair.getRight()) {
                    // simple 2 case
                    case 0:
                    case 1: {
                        // get vars
                        var bit = Integer.parseInt(matcher.group(1));
                        var reg = matcher.group(2);
                        logger.debug("Groups: {}[{}]", reg, bit);
                        var retVal = new BitAccessToken(reg, bit, token);
                        return Optional.of(retVal);
                    }
                    // index 2 case
                    case 2: {
                        // get vars
                        var reg = matcher.group(1);
                        var bit = Integer.parseInt(matcher.group(2));
                        logger.debug("Groups: {}[{}]", reg, bit);
                        var retVal = new BitAccessToken(reg, bit, token);
                        return Optional.of(retVal);
                    }
                    // index 3 case
                    case 3: {
                        // get vars
                        var reg = matcher.group(1);
                        var bitx = Integer.parseInt(matcher.group(2));
                        var bity = Integer.parseInt(matcher.group(3));
                        logger.debug("Groups: {}[{}:{}]", reg, bitx, bity);
                        var retVal = new BitAccessToken(reg, Pair.of(bitx, bity), token);
                        return Optional.of(retVal);
                    }
                    // 3 case
                    case 4:
                    case 5:
                    case 6: {
                        // get vars
                        var bitx = Integer.parseInt(matcher.group(1));
                        var bity = Integer.parseInt(matcher.group(2));
                        var reg = matcher.group(3);
                        logger.debug("Groups: {}[{}:{}]", reg, bitx, bity);
                        var retVal = new BitAccessToken(reg, Pair.of(bitx, bity), token);
                        return Optional.of(retVal);
                    }
                    default:
                        logger.error("Unknown access pattern matched {},{}", matcher.pattern(), patternIndex);
                        return Optional.empty();
                }
            } else {
                logger.debug("No match found");
            }
        }
        return Optional.empty();
    }

    private static Optional<Pair<Matcher, Integer>> hasBitAccess(TokenInstance token, Serializer serializer) {
        var stream = Serializer.mergeWords(serializer.deserialize(token.getStream()));
        logger.debug("Literal Token string: {}", stream);
        for (int i = 0, accessPatternsLength = accessPatterns.length; i < accessPatternsLength; i++) {
            Pattern pattern = accessPatterns[i];
            var tempMatcher = pattern.matcher(stream);
            if (tempMatcher.find()) {
                return Optional.of(Pair.of(tempMatcher, i));
            }
        }
        return Optional.empty();
    }
}
