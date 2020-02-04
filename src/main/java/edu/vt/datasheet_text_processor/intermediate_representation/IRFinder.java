package edu.vt.datasheet_text_processor.intermediate_representation;

import edu.vt.datasheet_text_processor.Errors.Context.IRConsequentContext;
import edu.vt.datasheet_text_processor.Errors.Context.IRContext;
import edu.vt.datasheet_text_processor.Errors.Context.IRPropertyContext;
import edu.vt.datasheet_text_processor.Errors.IRException;
import edu.vt.datasheet_text_processor.input.AllMappings;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticExpression;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import edu.vt.datasheet_text_processor.wordid.WordIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Take in semantic expressions and determine type of IR to generate
 *
 * Then convert each frame or give an error/warning
 */
public class IRFinder {
    private final static Logger logger = LoggerFactory.getLogger(IRFinder.class);

    public static String findIR(SemanticExpression se, AllMappings allMappings) throws IRException {
        // first need to determine which type of se we are dealing with
        if (se.getAntecedents().isEmpty() && !se.getConsequents().isEmpty()) {
            logger.debug("Declarative Sentence");
            return processDeclarative(se, allMappings);
        } else if (!se.getAntecedents().isEmpty() && !se.getConsequents().isEmpty()) {
            logger.debug("Implicative Sentence");
            return processImplicative(se, allMappings);
        } else if (se.getAllFrames().isEmpty()) {
            var message = "Invalid SE, 0 frames.";
            throw new IRException(message, new IRContext(message, se, null));
        } else {
            var message = "Invalid SE. 0 consequents.";
            throw new IRException(message, new IRContext(message, se, null));
        }
    }

    private static String processImplicative(SemanticExpression se, AllMappings allMappings) throws IRException {
        var sb = new StringBuilder();
        // process antecedent
        sb.append(processAntecedent(se, allMappings));
        sb.append(" -> ");
        // process consequent
        sb.append(processDeclarative(se, allMappings));
        return sb.toString();
    }

    private static String processAntecedent(SemanticExpression se, AllMappings allMappings) throws IRException {
        var sb = new StringBuilder();
        if (se.getAntecedents().size() > 1) {
            var message = "Cannot handle compound antecedents (1+).";
            throw new IRException(message, new IRContext(message, se, null));
        } else {
            var flag = false;
            for (var frame: se.getAntecedents()) {
                var frameRes = processAntecedentFrame(frame, allMappings);
                if (frameRes.isPresent()) {
                    flag = true;
                    sb.append('(');
                    // process frame
                    sb.append(frameRes.get());
                    sb.append(')');
                }
            }
        }
        return sb.toString();
    }

    private static Optional<String> processAntecedentFrame(FrameInstance frame, AllMappings allMappings) throws IRException {
        logger.debug("AF: {}, {}, {}", frame.getId(), frame.getTokens().size(), frame.getLiterals().size());
        switch (frame.getId()) {
            case 6: { // event
                var name = frame.get(0);
                var sb = new StringBuilder();
                sb.append("STATE(");
                sb.append(Serializer.mergeWords(allMappings.getSerializer().deserialize(name)));
                sb.append(')');
                return Optional.of(sb.toString());
            }
            case 7: { // when logic high
                var name = frame.get(0);
                var sb = new StringBuilder();
                sb.append("STATE(");
                sb.append(Serializer.mergeWords(allMappings.getSerializer().deserialize(name)));
                sb.append(" == '1'");
                sb.append(')');
                return Optional.of(sb.toString());
            }
            case 8: { // when logic low
                var name = frame.get(0);
                var sb = new StringBuilder();
                sb.append("(");
                sb.append(Serializer.mergeWords(allMappings.getSerializer().deserialize(name)));
                sb.append(" == '0'");
                sb.append(')');
                return Optional.of(sb.toString());
            }
            case 9: { // when specified value
                var name = frame.get(0);
                var value = frame.get(1);
                return Optional.of(getPossibleAction(name, value, allMappings));
            }
            case 13:
                return Optional.empty();
            default:
                var message = String.format("Unknown Antecedent frame %d.", frame.getId());
                throw new IRException(message, new IRContext(message, null, frame));
        }
    }

    private static String getPossibleAction(List<Integer> name, List<Integer> value, AllMappings allMappings) {
        var nameS = Serializer.mergeWords(allMappings.getSerializer().deserialize(name));
        var valueS = Serializer.mergeWords(allMappings.getSerializer().deserialize(value));
        if (value.size() == 1 && WordIdUtils.getWordIdClass(value.get(0)) == Serializer.WordIDClass.VERB) { // possible action
            return "STATE(" + nameS + ", " + valueS + ")";
        } else {
            return "(" + nameS + " == " + valueS + ")";
        }
    }

    private static String processDeclarative(SemanticExpression se, AllMappings allMappings) throws IRException {
        var sb = new StringBuilder();
        var flag = false;
        for (var frame: se.getConsequents()) {
            if (flag) {
                sb.append(" && ");
            }
            var frameRes = processConsequentFrame(frame, allMappings);
            if (frameRes.isPresent()) {
                flag = true;
                sb.append('(');
                // process frame
                sb.append(frameRes.get());
                sb.append(')');
            }
        }
        return sb.toString();
    }

    private static Optional<String> processConsequentFrame(FrameInstance frame, AllMappings allMappings) throws IRException {
        logger.debug("CF: {}, {}, {}", frame.getId(), frame.getTokens().size(), frame.getLiterals().size());
        switch (frame.getId()) {
            case 12: {
                // descriptive type
                // get first
                var name = frame.get(0);
                var names = normalizeSignalName(Serializer.mergeWords(allMappings.getSerializer().deserialize(name)));
                var desc = frame.get(1);
                var descs = Serializer.mergeWords(allMappings.getSerializer().deserialize(desc));
                var sb = new StringBuilder();
                switch (isStateAction(frame.get(1), allMappings)) {
                    case STATE:
                    {
                        sb.append("STATE(");
                        sb.append(names);
                        sb.append(", ");
                        sb.append(descs);
                        sb.append(')');
                        return Optional.of(sb.toString());
                    }
                    case MOD_STATE:
                    {
                        List<String> descsL = new LinkedList<>(Arrays.asList(descs.split(" ")));
                        String descMod = descsL.remove(0);
                        var descsRest = String.join(" ", descsL);
                        sb.append("STATE(");
                        sb.append(names);
                        sb.append(", ");
                        sb.append(descMod);
                        sb.append(", ");
                        sb.append(descsRest);
                        sb.append(')');
                        return Optional.of(sb.toString());
                    }
                    case DESC:
                        sb.append("DESC(");
                        sb.append(names);
                        sb.append(", ");
                        sb.append(descs);
                        sb.append(')');
                        return Optional.of(sb.toString());
                    case NONE:
                    {
                        var message = "No consequent phrase found";
                        throw new IRException(message, new IRConsequentContext(message, frame, names, descs));
                    }
                    case UNKNOWN:
                    {
                        var message = String.format("Unknown consequent phrase: %s", descs);
                        throw new IRException(message, new IRConsequentContext(message, frame, names, descs));
                    }
                }
            }
            case 14: {
                // property type
                // get first
                var name = frame.get(0);
                var prop = frame.get(1);
                return Optional.of(getProperty(name, prop, allMappings));
            }
            case 13:
                return Optional.empty();
            default:
                var message = String.format("Unknown Consequent frame %d.", frame.getId());
                throw new IRException(message, new IRContext(message, null, frame));
        }
    }

    public enum ConsequentActionPhrase {STATE, MOD_STATE, DESC, NONE, UNKNOWN};

    private static ConsequentActionPhrase isStateAction(List<Integer> phrase, AllMappings allMappings) {
        if (phrase.size() > 1) {
            var classA = WordIdUtils.getWordIdClass(phrase.get(0));
            var classB = WordIdUtils.getWordIdClass(phrase.get(1));
            if (classA.equals(Serializer.WordIDClass.MODIFIER) && classB.equals(Serializer.WordIDClass.VERB)) {
                return ConsequentActionPhrase.MOD_STATE;
            } else {
                return ConsequentActionPhrase.DESC;
            }
        } else if (phrase.size() == 1){
            var target = phrase.get(0);
            switch (WordIdUtils.getWordIdClass(target)) {
                case PRONOUN:
                case NUMBER:
                case OBJECT:
                    return ConsequentActionPhrase.DESC;
                case VERB:
                case ADJECTIVE:
                    return ConsequentActionPhrase.STATE;
                case JUNK:
                case CONDITION:
                case MODIFIER:
                default:
                    return ConsequentActionPhrase.UNKNOWN;
            }
        } else {
            return ConsequentActionPhrase.NONE;
        }
    }

    private static String getProperty(List<Integer> name, List<Integer> prop, AllMappings allMappings) throws IRException {
        var nameS = Serializer.mergeWords(allMappings.getSerializer().deserialize(name));
        nameS = normalizeSignalName(nameS);
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
                    var message = "Malformed 'of' expression.";
                    throw new IRException(message, new IRPropertyContext(message, nameS, propS));
                }
            } else {
                var message = "Unknown 'has' expression.";
                throw new IRException(message, new IRPropertyContext(message, nameS, propS));
            }
        }
    }

    private final static Pattern[] signalNamePatterns = {
            Pattern.compile("port (\\w+)"),
            Pattern.compile("signal (\\w+)"),
            Pattern.compile("register (\\w+)"),
            Pattern.compile("name (\\w+)"),
    };

    /**
     * Helper method to extract the actual signal name from a phrase like
     *  "port x", "signal y", "register q"
     * @param signalName
     * @return
     */
    private static String normalizeSignalName(String signalName) {
        // check patterns
        for (var pattern: signalNamePatterns) {
            var t = pattern.matcher(signalName);
            if (t.find()) {
                return t.group(1);
            }
        }
        return signalName;
    }
}
