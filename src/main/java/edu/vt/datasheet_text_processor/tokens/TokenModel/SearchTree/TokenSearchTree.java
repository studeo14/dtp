package edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree;

import edu.vt.datasheet_text_processor.tokens.TokenModel.TokenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ListIterator;

public class TokenSearchTree {
    private final static Logger logger = LoggerFactory.getLogger(TokenSearchTree.class);

    private SearchTreeNode rootNode;

    public SearchTreeNode getRootNode() {
        return rootNode;
    }

    public TokenSearchTree(TokenModel model) {
        this.rootNode = new SearchTreeNode();
        // go through and each token stream and add to tree
        for ( var token: model.values() ) {
            var stream = token.getStream();
            // for each word id
            // do for base stream
            logger.debug("Main token: {}", stream);
            addStreamToTree(stream.listIterator(), rootNode, token.getId());
            for (var alias: token.getAliases()) {
                logger.debug("Alias: {}", alias);
                addStreamToTree(alias.listIterator(), rootNode, token.getId());
            }
        }
    }

    private void addStreamToTree(ListIterator<Integer> listIterator, SearchTreeNode currentNode, Integer tokenId) {
        // recursive case
        if (listIterator.hasNext()) {
            var wordId = listIterator.next();
            logger.debug("Current wordid: {}", wordId);
            // add
            SearchTreeNode newNode;
            if (currentNode.getChildren().containsKey(wordId)) {
                // ignore and grab existing node
                newNode = currentNode.getChildren().get(wordId);
                logger.debug("Found existing");
            } else {
                // add
                newNode = new SearchTreeNode(wordId);
                currentNode.getChildren().put(wordId, newNode);
                logger.debug("Existing not found. Creating new.");
            }
            // if there is a leaf node
            if (!listIterator.hasNext()) {
                var leaf = new SearchTreeLeafNode(tokenId);
                newNode.getChildren().put(leaf.getWordId(), leaf);
                logger.debug("Adding leaf");
            } else {
                // recurse
                addStreamToTree(listIterator, newNode, tokenId);
            }
        }
        // base case
        // else just return (break recursive loop)
    }

    public String toString() {
        var str = new StringBuilder();
        for (var child: rootNode.getChildren().values()) {
            str.append(child.getString(0));
        }
        return str.toString();
    }
}
