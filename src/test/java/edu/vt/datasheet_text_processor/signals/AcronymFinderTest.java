package edu.vt.datasheet_text_processor.signals;

import edu.vt.datasheet_text_processor.tokens.TokenModel.Token;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.Tokenizer;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenizerException;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

public class AcronymFinderTest {
    private final static Logger logger = LogManager.getLogger(AcronymFinderTest.class);
    private Serializer serializer;

    @Before
    public void setUp() throws Exception {
        serializer = new Serializer(new File("Mappings.json"));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAcronymFailNonLiteral() {
        TokenInstance token = new TokenInstance(TokenInstance.Type.TOKEN, null);
        assertTrue(AcronymFinder.getAcronym(token, serializer).isEmpty());
    }

    @Test
    public void getAcronymFailNoAcronym() {
        String test = "logic pll register (lcr).";
        var wordIds = serializer.serialize(test, new AddNewWrapper(false));
        TokenInstance token = new TokenInstance(TokenInstance.Type.LITERAL, wordIds);
        var acronymPair = AcronymFinder.getAcronym(token, serializer);
        logger.debug(acronymPair.isEmpty());
        acronymPair.ifPresent(logger::debug);
        assertTrue(acronymPair.isEmpty());
    }

    @Test
    public void getAcronym() {
        // test sentence
        String test = "logic core register (lcr).";
        var wordIds = serializer.serialize(test, new AddNewWrapper(false));
        TokenInstance token = new TokenInstance(TokenInstance.Type.LITERAL, wordIds);
        var acronymPair = AcronymFinder.getAcronym(token, serializer);
        logger.debug(acronymPair.isEmpty());
        acronymPair.ifPresent(logger::debug);
        assertTrue(acronymPair.isPresent());
    }
}
