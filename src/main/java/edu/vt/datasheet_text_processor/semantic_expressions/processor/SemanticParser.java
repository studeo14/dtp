package edu.vt.datasheet_text_processor.semantic_expressions.processor;

import edu.vt.datasheet_text_processor.Errors.Context.SemanticExpressionContext;
import edu.vt.datasheet_text_processor.Errors.FrameException;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameFinder;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SemanticParser {
    private final static Logger logger = LoggerFactory.getLogger(SemanticParser.class);

    private SemanticModel semanticModel;

    public SemanticParser(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public Optional<SemanticExpression> findSemanticExpression ( List< TokenInstance > tokens, FrameFinder frameFinder, boolean preferShorterFrames ) throws FrameException {
        var frames = frameFinder.findAllFrames(tokens, preferShorterFrames );
        if (frames.isEmpty()) {
            return Optional.empty();
        } else {
            var semexpr = new SemanticExpression();
            semexpr.setAllFrames(frames);
            var modifiers = new ArrayList<Integer>();
            for (var frame: frames) {
                if (semanticModel.getAntecedents().contains(frame.getId())) {
                    semexpr.getAntecedents().add(frame);
                } else if (semanticModel.getConsequents().contains(frame.getId())) {
                    semexpr.getConsequents().add(frame);
                } else  if (semanticModel.getModifiers().contains( frame.getId() ) ) {
                    modifiers.add(frame.getId());
                }
            }
            if (semexpr.getConsequents().isEmpty() && semexpr.getAntecedents().isEmpty() && modifiers.isEmpty() ) {
                var message = "No Antecedents or Consequents Found.";
                throw new FrameException(message, new SemanticExpressionContext(message, tokens, frames, semexpr));
            } else if (semexpr.getConsequents().isEmpty() && !semexpr.getAntecedents().isEmpty()) {
                var message = String.format("Uneven frames. Has %d antecedents and 0 consequents.", semexpr.getAntecedents().size());
                throw new FrameException(message, new SemanticExpressionContext(message, tokens, frames, semexpr));
            } else {
                return Optional.of(semexpr);
            }
        }
    }

    public SemanticModel getSemanticModel() {
        return semanticModel;
    }
}
