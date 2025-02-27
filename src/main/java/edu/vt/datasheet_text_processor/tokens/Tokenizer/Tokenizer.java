package edu.vt.datasheet_text_processor.tokens.Tokenizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.Errors.Context.TokenizerContext;
import edu.vt.datasheet_text_processor.Errors.SerializerException;
import edu.vt.datasheet_text_processor.Errors.TokenizerException;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.tokens.TokenModel.RawTokenModel;
import edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree.SearchTreeLeafNode;
import edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree.SearchTreeNode;
import edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree.TokenSearchTree;
import edu.vt.datasheet_text_processor.tokens.TokenModel.TokenModel;
import edu.vt.datasheet_text_processor.util.Constants;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import edu.vt.datasheet_text_processor.wordid.WordIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Tokenizer {
    private final static Logger logger = LoggerFactory.getLogger(Tokenizer.class);

    private TokenModel tokenModel;
    private TokenSearchTree tokenSearchTree;

    public Tokenizer(File mappingFile, boolean useRaw, Serializer serializer) throws IOException, SerializerException {
        if (useRaw) {
            logger.info("Using string token inputs. Will serialize to wordids before tokeniztion.");
            var rawTokenModel = new ObjectMapper().readValue(mappingFile, RawTokenModel.class);
            this.tokenModel = rawTokenModel.toTokenModel(serializer);
        } else {
            this.tokenModel = new ObjectMapper().readValue(mappingFile, TokenModel.class);
        }
        this.tokenSearchTree = new TokenSearchTree(this.tokenModel);
    }

    public Tokenizer(TokenModel tokenModel) {
        this.tokenModel = tokenModel;
        this.tokenSearchTree = new TokenSearchTree(this.tokenModel);
    }

    public Tokenizer(RawTokenModel rawTokenModel, Serializer serializer) throws SerializerException {
        logger.info("Using string token inputs. Will serialize to wordids before tokeniztion.");
        this.tokenModel = rawTokenModel.toTokenModel(serializer);
        this.tokenSearchTree = new TokenSearchTree(this.tokenModel);
    }

    public TokenModel getTokenModel() {
        return tokenModel;
    }

    public TokenSearchTree getTokenSearchTree() {
        return tokenSearchTree;
    }

    public void exportMapping(File exportFile) throws IOException {
        new ObjectMapper().writeValue(exportFile, tokenModel);
    }

    public List<TokenInstance> tokenize ( List< Integer > sentence, boolean preferShorterTokens ) throws TokenizerException {
        var iter = sentence.listIterator();
        return processWordIdStream(iter, preferShorterTokens);
    }

    private enum ProcessState {BEGIN, INSIDE, LITERAL, END}

    private List<TokenInstance> processWordIdStream(ListIterator<Integer> iter, boolean preferShorterTokens) throws TokenizerException {
        ArrayList<TokenInstance> retVal = new ArrayList<>();
        logger.debug("Start new sentence tokenization");
        while(iter.hasNext()) {
            retVal.add(getNextToken(iter, preferShorterTokens));
        }
        return retVal;
    }

    private TokenInstance getNextToken(ListIterator<Integer> iter, boolean preferShorterTokens) throws TokenizerException {
        var current = new TokenInstance(TokenInstance.Type.NA);
        SearchTreeNode currentSearchTreeNode = new SearchTreeNode();
        ProcessState state = ProcessState.BEGIN;
        var originalIter = iter;
        while(iter.hasNext()) {
            logger.debug("STATE: {}", state.toString());
            switch (state) {
                case BEGIN:
                {
                    var currentWord = iter.next();
                    if (WordIdUtils.getWordIdClass(currentWord) == Serializer.WordIDClass.JUNK) {
                        break;
                    }
                    if (tokenSearchTree.getRootNode().getChildren().contains(currentWord)) {
                        logger.debug("Start token: {}", currentWord);
                        current.setType(TokenInstance.Type.TOKEN);
                        state = ProcessState.INSIDE;
                        currentSearchTreeNode = tokenSearchTree.getRootNode().getChildren().get(currentWord);
                    } else {
                        current.setType(TokenInstance.Type.LITERAL);
                        current.setId(Constants.LITERAL_TOKEN_ID);
                        state = ProcessState.LITERAL;
                        logger.debug("Start literal: {}", currentWord);
                    }
                    current.getStream().add(currentWord);
                    break;
                }
                case INSIDE:
                {
                    // is the current word a child of the current search tree node?
                    // yes - continue
                    // no
                    //  if leaf?
                    //      yes - GOTO end
                    //      no  - ERROR
                    var currentWord = iter.next();
                    logger.debug("Current Node: {}, CW: {}", currentSearchTreeNode.getWordId(), currentWord);
                    // prefer shorter tokens
                    // if so then end on first available option
                    if (preferShorterTokens) {
                        // if leaf
                        if (currentSearchTreeNode.getChildren().containsKey(Constants.SEARCH_TREE_LEAF_NODE_ID)){
                            current.setId(((SearchTreeLeafNode)currentSearchTreeNode.getChildren().get(Constants.SEARCH_TREE_LEAF_NODE_ID)).getTokenId());
                            state = ProcessState.END;
                            iter.previous();
                        } else if (currentSearchTreeNode.getChildren().contains(currentWord)) { // if has child
                            current.getStream().add(currentWord);
                            currentSearchTreeNode = currentSearchTreeNode.getChildren().get(currentWord);
                        } else { // no option available
                            var message = String.format("Unknown token found at word %d", currentWord);
                            throw new TokenizerException(message, new TokenizerContext(message, currentWord, iter.previousIndex(), currentSearchTreeNode));
                        }
                    } else {
                        // if has child
                        if (currentSearchTreeNode.getChildren().contains(currentWord)) {
                            current.getStream().add(currentWord);
                            currentSearchTreeNode = currentSearchTreeNode.getChildren().get(currentWord);
                        } else if (currentSearchTreeNode.getChildren().containsKey(Constants.SEARCH_TREE_LEAF_NODE_ID)){ // if has leaf
                            current.setId(((SearchTreeLeafNode)currentSearchTreeNode.getChildren().get(Constants.SEARCH_TREE_LEAF_NODE_ID)).getTokenId());
                            state = ProcessState.END;
                            iter.previous();
                        } else { // no options available
                            var message = String.format("Unknown token found at word %d", currentWord);
                            throw new TokenizerException(message, new TokenizerContext(message, currentWord, iter.previousIndex(), currentSearchTreeNode));
                        }
                    }
                    break;
                }
                case LITERAL:
                {
                    // add until new token begin found
                    // peek ahead
                    var peek = iter.next();
                    if (WordIdUtils.getWordIdClass(peek) == Serializer.WordIDClass.JUNK) {
                        break;
                    }
                    if (tokenSearchTree.getRootNode().getChildren().contains(peek)) {
                        state = ProcessState.END;
                        iter.previous();
                    } else {
                        current.getStream().add(peek);
                    }
                    break;
                }
                case END:
                {
                    // wrap up
                    logger.debug("Token end");
                    return current;
                }
            }
        }
        if (currentSearchTreeNode.getChildren().containsKey(Constants.SEARCH_TREE_LEAF_NODE_ID)) {
            current.setId(((SearchTreeLeafNode) currentSearchTreeNode.getChildren().get(Constants.SEARCH_TREE_LEAF_NODE_ID)).getTokenId());
            logger.debug("Token end on valid token.");
            return current;
        } else if (state == ProcessState.LITERAL) {
            current.setId(Constants.LITERAL_TOKEN_ID);
            logger.debug("Token end on valid literal token.");
            return current;
        } else {
            var currentWord = iter.previous();
            var message = String.format("Unknown token found at end of sentence: %d", currentWord);
            throw new TokenizerException(message, new TokenizerContext(message, currentWord, iter.previousIndex(), currentSearchTreeNode));
        }
    }
}
