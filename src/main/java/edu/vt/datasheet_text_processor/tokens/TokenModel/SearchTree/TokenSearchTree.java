package edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree;

import edu.vt.datasheet_text_processor.tokens.TokenModel.TokenModel;
import edu.vt.datasheet_text_processor.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ListIterator;

public class TokenSearchTree {
    private final static Logger logger = LogManager.getLogger(TokenSearchTree.class);

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
            // add
            SearchTreeNode newNode;
            if (currentNode.getChildren().containsKey(wordId)) {
                // ignore
                newNode = currentNode.getChildren().get(wordId);
            } else {
                // add
                newNode = new SearchTreeNode(wordId);
                currentNode.getChildren().put(wordId, newNode);
            }
            // if there is a leaf node
            if (!listIterator.hasNext()) {
                var leaf = new SearchTreeLeafNode(tokenId);
                newNode.getChildren().put(leaf.getWordId(), leaf);
            }
            // recurse
            addStreamToTree(listIterator, newNode, tokenId);
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
