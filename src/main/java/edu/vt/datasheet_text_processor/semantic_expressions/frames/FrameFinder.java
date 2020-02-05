package edu.vt.datasheet_text_processor.semantic_expressions.frames;

import edu.vt.datasheet_text_processor.Errors.Context.FrameFinderContext;
import edu.vt.datasheet_text_processor.Errors.FrameException;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree.FrameSearchTree;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree.FrameSearchTreeLeafNode;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree.FrameSearchTreeNode;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.CompoundToken;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class FrameFinder {
    private static final Logger logger = LoggerFactory.getLogger(FrameFinder.class);

    private enum State {BEGIN, INSIDE, FAIL, END}

    // used for finding frames in sentences
    private FrameSearchTree frameSearchTree;
    private FrameModel frameModel;

    public FrameFinder(FrameModel frameModel) {
        this.frameModel = frameModel;
        this.frameSearchTree = new FrameSearchTree(frameModel);
    }


    public List<FrameInstance> findAllFrames(List<TokenInstance> tokens) throws FrameException {
        ArrayList<FrameInstance> retVal = new ArrayList<>();
        logger.debug("Start new sentence frame finding.");
        logger.debug("Tokens: {}", tokens.stream().map(TokenInstance::getId).collect(Collectors.toList()));
        var iter = tokens.listIterator();
        while(iter.hasNext()) {
            var frame = getNextFrame(iter);
            if (frame.isPresent()) {
                var numLit = frame.get().getLiterals().size();
                var expected = frameModel.getGeneric().numLiterals(frame.get().getId());
                if (numLit == expected) {
                    // check if any of the literals are empty
                    for (var lit: frame.get().getLiterals()) {
                        if (lit.isEmpty()) {
                            var message = "Given literal is empty.";
                            throw new FrameException(message, new FrameFinderContext(message, tokens, frame.get(), null));
                        }
                    }
                    retVal.add(frame.get());
                    logger.debug("Frame Added: {}", frame.get().getId());
                } else {
                    var stream = frame.get().getTokens()
                            .stream()
                            .map(TokenInstance::getStream)
//                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    var message = String.format("Invalid number of literals. Id: %d, Expected: %d, Actual: %d. Frames: %s", frame.get().getId(), expected, numLit, stream);
                    throw new FrameException(message, new FrameFinderContext(message, tokens, frame.get(), null));
                }
            }
        }
        return retVal;
    }

    private enum FindState {BEGIN, INSIDE, END, END_ON_LITERAL}

    private Optional<FrameInstance> getNextFrame(ListIterator<TokenInstance> iter) throws FrameException {
        var current = new FrameInstance();
        var tokenList = new ArrayList<TokenInstance>();
        var literalList = new ArrayList<TokenInstance>();
        var currentNode = new FrameSearchTreeNode();
        var state = FindState.BEGIN;
        while(iter.hasNext()) {
            logger.debug("STATE: {}", state);
            switch (state) {
                case BEGIN: {
                    var currentToken = iter.next();
                    // check token to see if it is a valid starting token for a frame
                    if (frameSearchTree.getRootNode().getChildren().containsKey(currentToken.getId())) {
                        // check for literal starts
                        if (currentToken.getId().equals(Constants.LITERAL_TOKEN_ID)) {
                            // if there is not a next token then this cannot be a valid start token
                            if (iter.hasNext()) {
                                // need to peek to next token to see if this is a valid starting literal
                                var next = iter.next();
                                // reset
                                iter.previous();
                                // check
                                if (!frameSearchTree.getRootNode().getChildren().get(Constants.LITERAL_TOKEN_ID).getChildren().containsKey(next.getId())) {
                                    logger.debug("No Frame. Adding literal to start.");
                                    literalList.add(currentToken);
                                    break;
                                }
                                // if there is a valid next token
                                var cToken = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
                                literalList.add(currentToken);
                                cToken.setCompoundToken(new CompoundToken(literalList));
                                tokenList.add(cToken);
                                logger.debug("Starting Frame at {} from literal token start", currentToken.getId());
                            } else {
                                break;
                            }
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
                        logger.debug("No Frame. Adding literal {} to start.", currentToken.getId());
                        literalList.add(currentToken);
                    }
                    break;
                }
                case INSIDE: {
                    // get current
                    var currentToken = iter.next();
                    logger.debug("Inside Frame: CN: {}, CT:{}", currentNode.getTokenId(), currentToken.getId());
                    if (currentNode.getChildren().containsKey(currentToken.getId())) { // if has direct child
                        if (currentToken.getId().equals(Constants.LITERAL_TOKEN_ID)) {
                            logger.debug("Direct Placeholder Child");
                        } else {
                            logger.debug("Direct Child");
                        }
                        tokenList.add(currentToken);
                        currentNode = currentNode.getChildren().get(currentToken.getId());
                    } else if (currentNode.getChildren().containsKey(Constants.LITERAL_TOKEN_ID)) { // if has a "placeholder" child
                        logger.debug("Placeholder Child");
                        literalList = new ArrayList<>();
                        literalList.add(currentToken);
                        var cToken = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
                        cToken.setCompoundToken(new CompoundToken(literalList));
                        tokenList.add(cToken);
                        currentNode = currentNode.getChildren().get(Constants.LITERAL_TOKEN_ID);
                    } else if (currentNode.getChildren().containsKey(Constants.SEARCH_TREE_LEAF_NODE_ID)) { // if has no more children
                        iter.previous();
                        var id = ((FrameSearchTreeLeafNode)currentNode.getChildren().get(Constants.SEARCH_TREE_LEAF_NODE_ID)).getFrameId();
                        logger.debug("Leaf Child with ID: {}", id);
                        current.setId(id);
                        // check if ending on a "placeholder" token
                        if (currentNode.getTokenId().equals(Constants.LITERAL_TOKEN_ID)) {
                            // remove earlier literal
                            tokenList.remove(tokenList.size() - 1);
                            iter.previous();
                            state = FindState.END_ON_LITERAL;
                        } else { // if not
                            state = FindState.END;
                        }
                    } else { // this means that no frame can be found here.
                        iter.previous();
                        var validOptions = currentNode.getChildren().keySet();
                        var message = String.format("No valid child found. There is supposed to be something at token #%d. Valid options: %d:%s.", iter.nextIndex(), currentNode.getTokenId(), validOptions);
                        throw new FrameException(message, new FrameFinderContext(message, tokenList, current, currentNode));
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
                    literalList = new ArrayList<>();
                    while(iter.hasNext()) {
                        var currentToken = iter.next();
                        if (punctuationToken(currentToken)) {
                            logger.debug("End Literal on Punctuation Found");
                            var cToken = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
                            cToken.setCompoundToken(new CompoundToken(literalList));
                            tokenList.add(cToken);
                            current.setTokensAndLiterals(tokenList);
                            return Optional.of(current);
                        } else if (!currentToken.getId().equals(Constants.LITERAL_TOKEN_ID) && frameSearchTree.getRootNode().getChildren().containsKey(currentToken.getId())) {
                            logger.debug("End Literal on New Frame Found");
                            iter.previous();
                            var cToken = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
                            cToken.setCompoundToken(new CompoundToken(literalList));
                            tokenList.add(cToken);
                            current.setTokensAndLiterals(tokenList);
                            return Optional.of(current);
                        }
                        logger.debug("Adding literal '{}' to end.", currentToken.getId());
                        literalList.add(currentToken);
                    }
                    logger.debug("End Literal on Iter End");
                    var cToken = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
                    cToken.setCompoundToken(new CompoundToken(literalList));
                    tokenList.add(cToken);
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
            var endingToken = iter.previous();
            if (punctuationToken(endingToken)) {
                logger.debug("Has punctuation.");
                // prevent loop
                iter.next();
                return Optional.empty();
            } else {
                var message = String.format("Ending semantic expression on unexpected token. Non-literal, non-punctuation: %d", endingToken.getId());
                throw new FrameException(message, new FrameFinderContext(message, tokenList, current, currentNode));
            }
        }
    }

    private boolean punctuationToken(TokenInstance currentToken) {
        switch (currentToken.getId()) {
            case 60: // comma
            case 72: // period
            case -20:// DEFAULT
                return true;
            default:
                return false;
        }
    }

    public Optional<FrameInstance> findBitAccessFrame(List<TokenInstance> tokens) {
        for (var bat: frameModel.getBitAccess().entrySet()) {
            var template = bat.getValue();
            var id = bat.getKey();
            var templateRes = findSingleFrameRestricted(tokens.listIterator(), template.getTemplate(), id);
            if (templateRes.isPresent()) {
                return templateRes;
            } else {
                for (var alias: template.getAliases()) {
                    var aliasRes = findSingleFrameRestricted(tokens.listIterator(), alias, id);
                    if (aliasRes.isPresent()) {
                        return aliasRes;
                    }
                }
            }
        }
        return Optional.empty();
    }

    public Optional<FrameInstance> findSingleFrameRestricted(ListIterator<TokenInstance> tokenIter, List<Integer> template, Integer frameId) {
        FindState state = FindState.BEGIN;
        var current = new FrameInstance();
        ListIterator<Integer> templateIter = template.listIterator();
        logger.debug("Single Frame Begin. Looking for: {}", frameId);
        List<TokenInstance> tokens = new ArrayList<>();
        while(tokenIter.hasNext() && templateIter.hasNext()) {
            logger.debug("Single Frame::STATE: {}", state);
            switch (state) {
                case BEGIN:
                {
                    // reset
                    templateIter = template.listIterator();
                    tokens.clear();
                    var currentToken = tokenIter.next();
                    var startTemplate = templateIter.next();
                    logger.debug("BEGIN: {} vs {}", currentToken.getId(), startTemplate);
                    if (currentToken.getId().equals(startTemplate)) {
                        logger.debug("Found start of single token at: {}", currentToken);
                        state = FindState.INSIDE;
                        tokens.add(currentToken);
                    }
                    break;
                }
                case INSIDE:
                {
                    var currentToken = tokenIter.next();
                    var templateId = templateIter.next();
                    logger.debug("INSIDE: {} vs {}", currentToken.getId(), templateId);
                    if (currentToken.getId().equals(templateId)) {
                        if (templateId.equals(Constants.LITERAL_TOKEN_ID)) {
                            logger.debug("Direct Placeholder Child");
                        } else {
                            logger.debug("Direct Child");
                        }
                        tokens.add(currentToken);
                    } else if (templateId.equals(Constants.LITERAL_TOKEN_ID)) {
                        logger.debug("Placeholder Child");
                        tokens.add(currentToken);
                    } else {
                        state = FindState.BEGIN;
                    }
                    break;
                }
            }
        }
        logger.debug("Iter Ended");
        // one of the iters has ended
        if (!templateIter.hasNext()) {
            logger.debug("Frame Iter Ended. Returning token of: {}", frameId);
            current.setTokensAndLiterals(tokens);
            current.setId(frameId);
            return Optional.of(current);
        } else {
            logger.debug("Token Iter Ended");
            return Optional.empty();
        }
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
