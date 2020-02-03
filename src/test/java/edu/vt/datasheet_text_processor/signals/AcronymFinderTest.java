package edu.vt.datasheet_text_processor.signals;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.Errors.SerializerException;
import edu.vt.datasheet_text_processor.input.AllMappings;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class AcronymFinderTest {
    private final static Logger logger = LoggerFactory.getLogger(AcronymFinderTest.class);
    private AllMappings allMappings;

    @Before
    public void setUp() throws Exception {
        allMappings = new ObjectMapper().readValue(new File("Mappings_compiled.json"), AllMappings.class);
        allMappings.init();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAcronymFailNonLiteral() {
        TokenInstance token = new TokenInstance(TokenInstance.Type.TOKEN, null);
        assertTrue(AcronymFinder.getAcronym(token, allMappings.getSerializer()).isEmpty());
    }

    @Test
    public void getAcronymFailNoAcronym() throws SerializerException {
        String test = "logic pll register (lcr).";
        var wordIds = allMappings.getSerializer().serialize(test, false);
        TokenInstance token = new TokenInstance(TokenInstance.Type.LITERAL, wordIds);
        var acronymPair = AcronymFinder.getAcronym(token, allMappings.getSerializer());
        logger.debug("{}", acronymPair.isEmpty());
        acronymPair.ifPresent(b -> logger.debug("{}", b));
        assertTrue(acronymPair.isEmpty());
    }

    @Test
    public void getAcronym() throws SerializerException {
        // test sentence
        String test = "logic core register (lcr).";
        var wordIds = allMappings.getSerializer().serialize(test, false);
        TokenInstance token = new TokenInstance(TokenInstance.Type.LITERAL, wordIds);
        var acronymPair = AcronymFinder.getAcronym(token, allMappings.getSerializer());
        logger.debug("{}", acronymPair.isEmpty());
        acronymPair.ifPresent(b -> logger.debug("{}", b));
        assertTrue(acronymPair.isPresent());
    }
}
