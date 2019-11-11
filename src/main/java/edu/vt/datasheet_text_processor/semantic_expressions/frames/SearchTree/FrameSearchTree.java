package edu.vt.datasheet_text_processor.semantic_expressions.frames.SearchTree;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameModel;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameTemplateModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ListIterator;

public class FrameSearchTree {
    private final static Logger logger = LogManager.getLogger(FrameSearchTree.class);

    private FrameSearchTreeNode rootNode;

    public FrameSearchTree(FrameModel frameModel) {
        this.rootNode = new FrameSearchTreeNode();
        // add frame templates
        for (var frameTemplate: frameModel.getGeneric().values()) {
            var frameId = frameTemplate.getId();
            // add base
            addTemplate(frameTemplate.getTemplate().listIterator(), rootNode, frameId);
            // add aliases
            for (var alias: frameTemplate.getAliases()) {
                addTemplate(alias.listIterator(), rootNode, frameId);
            }
        }
    }

    private void addTemplate(ListIterator<Integer> listIterator, FrameSearchTreeNode currentNode, Integer frameId) {
        // recursive case
        if (listIterator.hasNext()) {
            var tokenId = listIterator.next();
            // create new node
            FrameSearchTreeNode newNode;
            if (currentNode.getChildren().containsKey(tokenId)) {
                // ignore, already added
                newNode = currentNode.getChildren().get(tokenId);
            } else {
                // add
                newNode = new FrameSearchTreeNode(tokenId);
                currentNode.getChildren().put(tokenId, newNode);
            }
            // check if leaf node
            if (!listIterator.hasNext()) {
                var leaf = new FrameSearchTreeLeafNode(frameId);
                newNode.getChildren().put(leaf.getTokenId(), leaf);
            } else {
                // recurse
                addTemplate(listIterator, newNode, frameId);
            }
        }
        // base case
        // else just return (break recursive loop)
    }

    public FrameSearchTreeNode getRootNode() {
        return rootNode;
    }

    public String toString() {
        var str = new StringBuilder();
        for (var child: rootNode.getChildren().values()) {
            str.append(child.getString(0));
        }
        return str.toString();
    }
}
