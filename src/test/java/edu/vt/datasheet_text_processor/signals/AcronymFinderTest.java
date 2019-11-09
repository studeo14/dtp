package edu.vt.datasheet_text_processor.signals;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.input.AllMappings;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class AcronymFinderTest {
    private final static Logger logger = LogManager.getLogger(AcronymFinderTest.class);
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
    public void getAcronymFailNoAcronym() {
        String test = "logic pll register (lcr).";
        var wordIds = allMappings.getSerializer().serialize(test, new AddNewWrapper(false));
        TokenInstance token = new TokenInstance(TokenInstance.Type.LITERAL, wordIds);
        var acronymPair = AcronymFinder.getAcronym(token, allMappings.getSerializer());
        logger.debug(acronymPair.isEmpty());
        acronymPair.ifPresent(logger::debug);
        assertTrue(acronymPair.isEmpty());
    }

    @Test
    public void getAcronym() {
        // test sentence
        String test = "logic core register (lcr).";
        var wordIds = allMappings.getSerializer().serialize(test, new AddNewWrapper(false));
        TokenInstance token = new TokenInstance(TokenInstance.Type.LITERAL, wordIds);
        var acronymPair = AcronymFinder.getAcronym(token, allMappings.getSerializer());
        logger.debug(acronymPair.isEmpty());
        acronymPair.ifPresent(logger::debug);
        assertTrue(acronymPair.isPresent());
    }
}
