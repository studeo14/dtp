package edu.vt.datasheet_text_processor.semantic_expressions.frames;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree.FrameSearchTree;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree.FrameSearchTreeLeafNode;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree.FrameSearchTreeNode;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.CompoundToken;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

public class FrameFinder {
    private static final Logger logger = LogManager.getLogger(FrameFinder.class);

    private enum State {BEGIN, INSIDE, FAIL, END};

    // used for finding frames in sentences
    private FrameSearchTree frameSearchTree;
    private FrameModel frameModel;

    public FrameFinder(FrameModel frameModel) {
        this.frameModel = frameModel;
        this.frameSearchTree = new FrameSearchTree(frameModel);
    }


    public List<FrameInstance> findAllFrames(List<TokenInstance> tokens) throws FrameException {
        ArrayList<FrameInstance> retVal = new ArrayList<>();
        logger.debug("Start new sentence tokenization");
        logger.debug("Tokens: {}", tokens.stream().map(TokenInstance::getId).collect(Collectors.toList()));
        var iter = tokens.listIterator();
        while(iter.hasNext()) {
            var frame = getNextFrame(iter);
            if (frame.isPresent()) {
                retVal.add(frame.get());
                logger.debug("Frame Added: {}", frame.get().getId());
            }
        }
        return retVal;
    }

    private enum FindState {BEGIN, INSIDE, END, END_ON_LITERAL};

    private Optional<FrameInstance> getNextFrame(ListIterator<TokenInstance> iter) throws FrameException {
        var current = new FrameInstance();
        var tokenList = new ArrayList<TokenInstance>();
        var literalList = new ArrayList<TokenInstance>();
        var currentNode = new FrameSearchTreeNode();
        var state = FindState.BEGIN;
        while(iter.hasNext()) {
            switch (state) {
                case BEGIN: {
                    var currentToken = iter.next();
                    // check token to see if it is a valid starting token for a frame
                    if (frameSearchTree.getRootNode().getChildren().containsKey(currentToken.getId())) {
                        // check for literal starts
                        if (currentToken.getId().equals(Constants.LITERAL_TOKEN_ID)) {
                            var cToken = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
                            cToken.setCompoundToken(new CompoundToken(literalList));
                            tokenList.add(cToken);
                            tokenList.add(currentToken);
                            logger.debug("Starting Frame at {} from literal token start", currentToken.getId());
                        } else { // regular starts
                            tokenList.add(currentToken);
                            logger.debug("Starting Frame at {}({})", currentToken.getId(), currentToken.getStream());
                        }
                        state = FindState.INSIDE;
                        currentNode = frameSearchTree.getRootNode().getChildren().get(currentToken.getId());
                        // literal start using a non-literal token (not id == -21) as a placeholder token
                    } else if (frameSearchTree.getRootNode().getChildren().get(Constants.LITERAL_TOKEN_ID).getChildren().containsKey(currentToken.getId())) {
                        // make compount token
                        var cToken = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
                        cToken.setCompoundToken(new CompoundToken(literalList));
                        tokenList.add(cToken);
                        tokenList.add(currentToken);
                        state = FindState.INSIDE;
                        currentNode = frameSearchTree.getRootNode().getChildren().get(Constants.LITERAL_TOKEN_ID).getChildren().get(currentToken.getId());
                        logger.debug("Starting Frame at {} from literal token start", currentToken.getId());
                    // not a starting token. add to literal list
                    } else {
                        logger.debug("No Frame. Adding literal to start.");
                        literalList.add(currentToken);
                    }
                    break;
                }
                case INSIDE: {
                    // get current
                    var currentToken = iter.next();
                    logger.debug("Inside Frame: CN: {}, CT:{}", currentNode.getTokenId(), currentToken.getId());
                    if (currentNode.getChildren().containsKey(currentToken.getId())) { // if has direct child
                        logger.debug("Direct Child");
                        tokenList.add(currentToken);
                        currentNode = currentNode.getChildren().get(currentToken.getId());
                    } else if (currentNode.getChildren().containsKey(Constants.LITERAL_TOKEN_ID)) { // if has a "placeholder" child
                        logger.debug("Placeholder Child");
                        tokenList.add(currentToken);
                        currentNode = currentNode.getChildren().get(Constants.LITERAL_TOKEN_ID);
                    } else if (currentNode.getChildren().containsKey(Constants.SEARCH_TREE_LEAF_NODE_ID)) { // if has no more children
                        logger.debug("Leaf Child");
                        iter.previous();
                        current.setId(((FrameSearchTreeLeafNode)currentNode.getChildren().get(Constants.SEARCH_TREE_LEAF_NODE_ID)).getFrameId());
                        // check if ending on a "placeholder" token
                        if (currentNode.getTokenId().equals(Constants.LITERAL_TOKEN_ID)) {
                            state = FindState.END_ON_LITERAL;
                        } else { // if not
                            state = FindState.END;
                        }
                    } else { // this means that no frame can be found here.
                        //iter.previous();
                        return Optional.empty();
                    }
                    break;
                }
                case END: {
                    logger.debug("End");
                    current.setTokensAndLiterals(tokenList);
                    return Optional.of(current);
                }
                case END_ON_LITERAL: {
                    logger.debug("End On Literal");
                    while(iter.hasNext()) {
                        var currentToken = iter.next();
                        if (!currentToken.getId().equals(Constants.LITERAL_TOKEN_ID) && frameSearchTree.getRootNode().getChildren().containsKey(currentToken.getId())) {
                            logger.debug("End Literal on New Frame Found");
                            iter.previous();
                            current.setTokensAndLiterals(tokenList);
                            return Optional.of(current);
                        } else if (currentToken.getId().equals(64)) {
                            logger.debug("End Literal on Punctuation Found");
                            current.setTokensAndLiterals(tokenList);
                            return Optional.of(current);
                        }
                        tokenList.add(currentToken);
                    }
                    logger.debug("End Literal on Iter End");
                    iter.previous();
                    current.setTokensAndLiterals(tokenList);
                    return Optional.of(current);
                }
            }
        }
        logger.debug("End On Empty.");
        if (currentNode.getChildren().containsKey(Constants.SEARCH_TREE_LEAF_NODE_ID)) { // if has no more children
            logger.debug("Has leaf.");
            current.setId(((FrameSearchTreeLeafNode) currentNode.getChildren().get(Constants.SEARCH_TREE_LEAF_NODE_ID)).getFrameId());
            current.setTokensAndLiterals(tokenList);
            return Optional.of(current);
        } else {
            logger.debug("No leaf.");
            return Optional.empty();
        }
    }

    /**
     * Helper method to use a frameid instead of a frametemplate
     */
    public Optional<FrameInstance> findFrame(List<TokenInstance> tokens, Integer frameId){
        // get frameTemplate
        if (frameModel.getGeneric().containsKey(frameId)) {
            return findFrame(tokens, frameModel.getGeneric().get(frameId));
        } else if (frameModel.getBitAccess().containsKey(frameId)) {
            return findFrame(tokens, frameModel.getBitAccess().get(frameId));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Method to look for a single frame in a sentence rather than any frame
     * @param tokens
     * @param frameTemplate
     * @return
     */
    public Optional<FrameInstance> findFrame(List<TokenInstance> tokens, FrameTemplate frameTemplate) {
        // try base template
        var tempRes = findFrame(tokens, frameTemplate.getTemplate(), frameTemplate.getId());
        if (tempRes.isPresent()) {
            return tempRes;
        } else {
            // try aliases
            for (var alias: frameTemplate.getAliases()) {
                tempRes = findFrame(tokens, alias, frameTemplate.getId());
                if (tempRes.isPresent()) {
                    return tempRes;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Helper method to be able to use a raw template list + id
     * This helps break out the processing of aliases and base templates into a single common algorithm
     * @param tokens
     * @param template
     * @param frameId
     * @return
     */
    public Optional<FrameInstance> findFrame(List<TokenInstance> tokens, List<Integer> template, Integer frameId) {
        for (int i = 0, tokensSize = tokens.size(); i < tokensSize; i++) {
            var tempRes = findFrameOnePass(tokens.listIterator(i), template.listIterator(), frameId);
            if (tempRes.isPresent()) {
                return tempRes;
            }
        }
        return Optional.empty();
    }

    /**
     * Helper method to look for a single frame from a specific starting place in a sentence
     * @param tokenIter
     * @param frameIter
     * @param frameId
     * @return
     */
    public Optional<FrameInstance> findFrameOnePass(ListIterator<TokenInstance> tokenIter, ListIterator<Integer> frameIter, Integer frameId) {
        var state = State.BEGIN;
        List<TokenInstance> frameTokens = new ArrayList<>();
        while(tokenIter.hasNext()) {
            switch (state) {
                case BEGIN:
                {
                    var current = tokenIter.next();
                    var currentId = frameIter.next();
                    // if ids are equal then add and move on
                    // if not then reset the frametemplate iterator, throw away token
                    if (current.getId().equals(currentId)) {
                        frameTokens.add(current);
                        state = State.INSIDE;
                    } else {
                        state = State.FAIL;
                    }
                    break;
                }
                case INSIDE:
                {
                    var current = tokenIter.next();
                    if (!frameIter.hasNext()) {
                        state = State.END;
                        break;
                    }
                    var currentId = frameIter.next();
                    // if ids are equal then add and move on
                    // if not then reset the frametemplate iterator, throw away token
                    if (current.getId().equals(currentId)) {
                        frameTokens.add(current);
                    } else {
                        state = State.FAIL;
                    }
                    break;
                }
                case FAIL:
                {
                    return Optional.empty();
                }
                case END:
                {
                    var frame = new FrameInstance(frameId, frameTokens);
                    return Optional.of(frame);
                }
            }
        }
        return Optional.empty();
    }

    public FrameSearchTree getFrameSearchTree() {
        return frameSearchTree;
    }

    public void setFrameSearchTree(FrameSearchTree frameSearchTree) {
        this.frameSearchTree = frameSearchTree;
    }

    public FrameModel getFrameModel() {
        return frameModel;
    }

    public void setFrameModel(FrameModel frameModel) {
        this.frameModel = frameModel;
    }
}
