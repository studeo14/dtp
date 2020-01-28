package edu.vt.datasheet_text_processor.tokens.TokenInstance;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.input.AllMappings;
import edu.vt.datasheet_text_processor.signals.AcronymFinderTest;
import edu.vt.datasheet_text_processor.util.Constants;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TokenInstanceTest {
    private final static Logger logger = LoggerFactory.getLogger(TokenInstanceTest.class);
    private AllMappings allMappings;
    private TokenInstance
            literalTokenA, literalTokenA_, literalTokenB,
            tokenTokenA, tokenTokenA_, tokenTokenB, tokenTokenC,
            batTokenA, batTokenA_, batTokenB, batTokenC, batTokenD,
            cTokenA, cTokenA_, cTokenB
            ;

    @Before
    public void setUp() throws Exception {
        allMappings = new ObjectMapper().readValue(new File("Mappings_compiled.json"), AllMappings.class);
        allMappings.init();

        String test = "logic pll register (lcr).";
        String test2 = "logic pll register (lcr) but is not pll.";
        var wordIds = allMappings.getSerializer().serialize(test, new AddNewWrapper(false));
        var wordIds2 = allMappings.getSerializer().serialize(test2, new AddNewWrapper(false));
        literalTokenA = new TokenInstance(TokenInstance.Type.LITERAL, wordIds);
        literalTokenA_ = new TokenInstance(TokenInstance.Type.LITERAL, wordIds);
        literalTokenB = new TokenInstance(TokenInstance.Type.LITERAL, wordIds2);

        tokenTokenA = new TokenInstance(TokenInstance.Type.TOKEN, wordIds, Constants.DEFAULT_TOKEN_ID);
        tokenTokenA_ = new TokenInstance(TokenInstance.Type.TOKEN, wordIds, Constants.DEFAULT_TOKEN_ID);
        tokenTokenB = new TokenInstance(TokenInstance.Type.TOKEN, wordIds2, Constants.DEFAULT_TOKEN_ID);
        tokenTokenC = new TokenInstance(TokenInstance.Type.TOKEN, wordIds, Constants.DEFAULT_TOKEN_ID + 1);

        List<TokenInstance> listOfTokens = new ArrayList<>();
        listOfTokens.add(literalTokenA);
        listOfTokens.add(tokenTokenA);
        listOfTokens.add(literalTokenB);
        listOfTokens.add(tokenTokenB);
        List<TokenInstance> listOfTokensB = new ArrayList<>();
        listOfTokens.add(literalTokenA);
        listOfTokens.add(tokenTokenA);
        listOfTokens.add(literalTokenB);
        listOfTokens.add(tokenTokenC);

        var batA = new BitAccessToken("lcr", 0, 1, listOfTokens);
        var batB = new BitAccessToken("lcr", 0, 2, listOfTokens);
        var batC = new BitAccessToken("lcr", 1, 1, listOfTokens);
        var batD = new BitAccessToken("lcr", 0, 1, listOfTokensB);

        batTokenA = new TokenInstance(TokenInstance.Type.ACCESS, null, Constants.LITERAL_TOKEN_ID);
        batTokenA.setBitAccessToken(batA, allMappings.getSerializer());
        batTokenA_ = new TokenInstance(TokenInstance.Type.ACCESS, null, Constants.LITERAL_TOKEN_ID);
        batTokenA_.setBitAccessToken(batA, allMappings.getSerializer());
        batTokenB = new TokenInstance(TokenInstance.Type.ACCESS, null, Constants.LITERAL_TOKEN_ID);
        batTokenB.setBitAccessToken(batB, allMappings.getSerializer());
        batTokenC = new TokenInstance(TokenInstance.Type.ACCESS, null, Constants.LITERAL_TOKEN_ID);
        batTokenC.setBitAccessToken(batC, allMappings.getSerializer());
        batTokenD = new TokenInstance(TokenInstance.Type.ACCESS, null, Constants.LITERAL_TOKEN_ID);
        batTokenD.setBitAccessToken(batD, allMappings.getSerializer());

        var ctA = new CompoundToken(listOfTokens);
        var ctB = new CompoundToken(listOfTokensB);

        cTokenA = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
        cTokenA_ = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
        cTokenB = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
        cTokenA.setCompoundToken(ctA);
        cTokenA_.setCompoundToken(ctA);
        cTokenB.setCompoundToken(ctB);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void equalityPass() {
        assertTrue(literalTokenA.equals(literalTokenA_));
        assertTrue(tokenTokenA.equals(tokenTokenA_));
        assertTrue(batTokenA.equals(batTokenA_));
        assertTrue(cTokenA.equals(cTokenA_));
    }

    @Test
    public void notEqualsCheck() {
        assertFalse(literalTokenA.equals(literalTokenB));
        assertFalse(tokenTokenA.equals(tokenTokenB));
        assertFalse(tokenTokenA.equals(tokenTokenC));
        assertFalse(batTokenA.equals(batTokenB));
        assertFalse(batTokenA.equals(batTokenC));
        assertFalse(batTokenA.equals(batTokenD));
        assertFalse(cTokenA.equals(cTokenB));
    }
}

