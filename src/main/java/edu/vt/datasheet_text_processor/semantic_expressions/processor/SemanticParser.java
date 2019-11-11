package edu.vt.datasheet_text_processor.semantic_expressions.processor;

import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameException;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameFinder;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class SemanticParser {
    private final static Logger logger = LogManager.getLogger(SemanticParser.class);

    private SemanticModel semanticModel;

    public SemanticParser(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public Optional<SemanticExpression> findSemanticExpression(List<TokenInstance> tokens, FrameFinder frameFinder) throws FrameException {
        var frames = frameFinder.findAllFrames(tokens);
        if (frames.isEmpty()) {
            return Optional.empty();
        } else {
            var semexpr = new SemanticExpression();
            semexpr.setAllFrames(frames);
            for (var frame: frames) {
                if (semanticModel.getAntecedents().contains(frame.getId())) {
                    semexpr.getAntecedents().add(frame);
                } else if (semanticModel.getConsequents().contains(frame.getId())) {
                    semexpr.getConsequents().add(frame);
                }
            }
            if (semexpr.getConsequents().isEmpty() && semexpr.getAntecedents().isEmpty()) {
                throw new FrameException("No Antecedents Or Consequents Found.");
            } else if (semexpr.getConsequents().isEmpty() && !semexpr.getAntecedents().isEmpty()) {
                throw new FrameException(String.format("Uneven frames. Has %d antecedents and 0 consequents.", semexpr.getAntecedents().size()));
            } else {
                return Optional.of(semexpr);
            }
        }
    }

    public SemanticModel getSemanticModel() {
        return semanticModel;
    }
}
