package edu.vt.datasheet_text_processor.tokens.TokenModel.SearchTree;

import edu.vt.datasheet_text_processor.tokens.TokenModel.TokenModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ListIterator;

public class TokenSearchTree {
    private final static Logger logger = LogManager.getLogger(TokenSearchTree.class);

    private SearchTreeNode rootNode;

    public TokenSearchTree(TokenModel model) {
        this.rootNode = new SearchTreeNode(0);
        // go through and each token stream and add to tree
        for ( var token: model.values() ) {
            var stream = token.getStream();
            // for each word id
            // do for base stream
            addStreamToTree(stream.listIterator(), rootNode, token.getId());
            for (var alias: token.getAliases()) {
                logger.info("Alias: {}", alias);
                addStreamToTree(alias.listIterator(), rootNode, token.getId());
            }
        }
    }

    private void addStreamToTree(ListIterator<Integer> listIterator, SearchTreeNode currentNode, Integer tokenId) {
        // base case
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
                // if there is a leaf node
                if (!listIterator.hasNext()) {
                    var leaf = new SearchTreeLeafNode(-1, tokenId);
                    newNode.getChildren().put(leaf.getWordId(), leaf);
                }
                currentNode.getChildren().put(wordId, newNode);
            }
            // recurse
            addStreamToTree(listIterator, newNode, tokenId);
        }
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
