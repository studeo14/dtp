package edu.vt.datasheet_text_processor.intermediate_representation;

import edu.vt.datasheet_text_processor.input.AllMappings;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticExpression;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import edu.vt.datasheet_text_processor.wordid.WordIdUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Take in semantic expressions and determine type of IR to generate
 *
 * Then convert each frame or give an error/warning
 */
public class IRFinder {
    private final static Logger logger = LogManager.getLogger(IRFinder.class);

    public static String findIR(SemanticExpression se, AllMappings allMappings) throws IRException {
        // first need to determine which type of se we are dealing with
        if (se.getAntecedents().isEmpty() && !se.getConsequents().isEmpty()) {
            logger.debug("Declarative Sentence");
            return processDeclarative(se, allMappings);
        } else if (!se.getAntecedents().isEmpty() && !se.getConsequents().isEmpty()) {
            logger.debug("Implicative Sentence");
            return processImplicative(se, allMappings);
        } else if (se.getAllFrames().isEmpty()) {
            throw new IRException("Invalid SE. 0 frames.");
        } else {
            throw new IRException("Invalid SE. 0 consequents.");
        }
    }

    private static String processImplicative(SemanticExpression se, AllMappings allMappings) {
        return "()";
    }

    private static String processDeclarative(SemanticExpression se, AllMappings allMappings) throws IRException {
        var sb = new StringBuilder();
        var flag = false;
        for (var frame: se.getConsequents()) {
            if (flag) {
                sb.append(" && ");
            }
            flag = true;
            sb.append('(');
            // process frame
            sb.append(processConsequentFrame(frame, allMappings));
            sb.append(')');
        }
        return sb.toString();
    }

    private static String processConsequentFrame(FrameInstance frame, AllMappings allMappings) throws IRException {
        logger.debug("CF: {}, {}, {}", frame.getId(), frame.getTokens().size(), frame.getLiterals().size());
        switch (frame.getId()) {
            case 12: {
                // descriptive type
                // get first
                var name = frame.get(0);
                var desc = frame.get(1);
                var sb = new StringBuilder();
                sb.append("DESC(");
                sb.append(Serializer.mergeWords(allMappings.getSerializer().deserialize(name)));
                sb.append(", ");
                sb.append(Serializer.mergeWords(allMappings.getSerializer().deserialize(desc)));
                sb.append(')');
                return sb.toString();
            }
            case 14: {
                // property type
                // get first
                var name = frame.get(0);
                var prop = frame.get(1);
                return getProperty(name, prop, allMappings);
            }
            default:
                throw new IRException(String.format("Unknown Consesquent frame %d.", frame.getId()));
        }
    }

    private static String getProperty(List<Integer> name, List<Integer> prop, AllMappings allMappings) throws IRException {
        var nameS = Serializer.mergeWords(allMappings.getSerializer().deserialize(name));
        var propS = Serializer.mergeWords(allMappings.getSerializer().deserialize(prop));
        if (WordIdUtils.getWordIdClass(prop.get(0)) == Serializer.WordIDClass.VERB) {
            // TODO: handle "has <verb>" frames
            return "STATE(" + nameS + ", " + propS + ")";
        } else {
            if (propS.contains("of")) {
                var split = propS.split("of");
                try {
                    return "PROP(" + nameS + ", " + split[0] + ", " + split[1] + ")";
                } catch (IndexOutOfBoundsException e) {
                    throw new IRException("Malformed 'of' expression.");
                }
            } else {
                throw new IRException("Unknown 'has' expression.");
            }
        }
    }
}
