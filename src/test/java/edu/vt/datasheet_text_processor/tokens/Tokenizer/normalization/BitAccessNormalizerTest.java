package edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization;

import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class BitAccessNormalizerTest {
    private final static Logger logger = LogManager.getLogger(BitAccessNormalizerTest.class);
    private Serializer serializer;

    @Before
    public void setUp() throws Exception {
        serializer = new Serializer(new File("Mappings.json"));
    }

    @Test
    public void normalizeBitAccessNonLiteral() {
        var tokenInstance = new TokenInstance(TokenInstance.Type.TOKEN);
        var test = BitAccessNormalizer.getNormalizedBitAccess(tokenInstance, serializer);
        logger.debug(test.isPresent());
        assertTrue(test.isEmpty());
    }

    @Test
    public void normalizeBitAccessNoBitAccess() {
        var testString = "random literal token";
        var testStringStream = serializer.serialize(testString, new AddNewWrapper(false));
        var tokenInstance = new TokenInstance(TokenInstance.Type.LITERAL, testStringStream);
        var test = BitAccessNormalizer.getNormalizedBitAccess(tokenInstance, serializer);
        logger.debug(test.isPresent());
        assertTrue(test.isEmpty());
    }

    @Test
    public void normalizeBitAccess() {
        var testString = "bit 7 of LCR";
        var testStringStream = serializer.serialize(testString, new AddNewWrapper(false));
        var tokenInstance = new TokenInstance(TokenInstance.Type.LITERAL, testStringStream);
        var test = BitAccessNormalizer.getNormalizedBitAccess(tokenInstance, serializer);
        logger.debug(test.isPresent());
        test.ifPresent(token -> logger.debug("{}[{}:{}]", token.getRegisterName(), token.getBits().getLeft(), token.getBits().getRight()));
        assertTrue(test.isPresent());
    }
}