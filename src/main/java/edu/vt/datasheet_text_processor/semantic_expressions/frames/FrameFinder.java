package edu.vt.datasheet_text_processor.semantic_expressions.frames;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree.FrameSearchTree;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

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

    public List<FrameInstance> findAllFrames(List<TokenInstance> tokens) {
        var retList = new ArrayList<FrameInstance>();
        for (var frame: frameModel.values()) {
            findFrame(tokens, frame).ifPresent(retList::add);
        }
        return retList;
    }

    /**
     * Helper method to use a frameid instead of a frametemplate
     */
    public Optional<FrameInstance> findFrame(List<TokenInstance> tokens, Integer frameId){
        // get frameTemplate
        if (frameModel.containsKey(frameId)) {
            return findFrame(tokens, frameModel.get(frameId));
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
    public static Optional<FrameInstance> findFrame(List<TokenInstance> tokens, FrameTemplate frameTemplate) {
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
    public static Optional<FrameInstance> findFrame(List<TokenInstance> tokens, List<Integer> template, Integer frameId) {
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
    public static Optional<FrameInstance> findFrameOnePass(ListIterator<TokenInstance> tokenIter, ListIterator<Integer> frameIter, Integer frameId) {
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
