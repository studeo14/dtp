package edu.vt.datasheet_text_processor.intermediate_representation;

import edu.vt.datasheet_text_processor.Errors.Context.IRCompoundContext;
import edu.vt.datasheet_text_processor.Errors.Context.IRConsequentContext;
import edu.vt.datasheet_text_processor.Errors.Context.IRContext;
import edu.vt.datasheet_text_processor.Errors.Context.IRPropertyContext;
import edu.vt.datasheet_text_processor.Errors.FrameException;
import edu.vt.datasheet_text_processor.Errors.IRException;
import edu.vt.datasheet_text_processor.Errors.ProcessorException;
import edu.vt.datasheet_text_processor.Project;
import edu.vt.datasheet_text_processor.input.AllMappings;
import edu.vt.datasheet_text_processor.semantic_expressions.frames.FrameInstance;
import edu.vt.datasheet_text_processor.semantic_expressions.processor.SemanticExpression;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.CompoundToken;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.util.Constants;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import edu.vt.datasheet_text_processor.wordid.WordIdUtils;
import org.dizitart.no2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Take in semantic expressions and determine type of IR to generate
 *
 * Then convert each frame or give an error/warning
 */
public class IRFinder {
    private final static Logger logger = LoggerFactory.getLogger(IRFinder.class);
    private static Double declarative = 0.0;
    private static Double implicative = 0.0;
    private static Double modifier = 0.0;
    private static Double invalidInput = 0.0;
    private static Double compoundAfterNone = 0.0;
    private static Double compoundAfterA = 0.0;
    private static Double compoundAfterC = 0.0;
    private static Double compoundWithValidFrame = 0.0;
    private static Double compoundWithoutValidFrame = 0.0;
    private static Double invalidCompound = 0.0;
    private static Double singluarCompound = 0.0;
    private static Double genericCompound = 0.0;
    private static Double compoundWithFactoring = 0.0;
    private static Double invalidTemporal = 0.0;
    private static Double genericTemporal = 0.0;
    private static Double eventAntecedent = 0.0;
    private static Double logicHighAntecedent = 0.0;
    private static Double logicLowAntecedent = 0.0;
    private static Double valueAntecedent = 0.0;
    private static Double recursiveTemporal = 0.0;
    private static Double literalTemporal = 0.0;
    private static Double then = 0.0;
    private static Double actionAsState = 0.0;
    private static Double actionAsValue = 0.0;
    private static Double descriptionAsState = 0.0;
    private static Double descriptionAsValue = 0.0;
    private static Double descriptionAsDescription = 0.0;
    private static Double stateConsequent = 0.0;
    private static Double modConsequent = 0.0;
    private static Double descriptionConsequent = 0.0;
    private static Double propertyConsequent = 0.0;
    private static Double propertyAsState = 0.0;
    private static Double propertyAsProperty = 0.0;
    private static Double normalizedSignalName = 0.0;

    public static void showCounters() {
        logger.info("{} declarative", declarative);
        logger.info("{} implicative", implicative);
        logger.info("{} modifier", modifier);
        logger.info("{} invalidInput", invalidInput);
        logger.info("{} compoundAfterNone", compoundAfterNone);
        logger.info("{} compoundAfterA", compoundAfterA);
        logger.info("{} compoundAfterC", compoundAfterC);
        logger.info("{} compoundWithValidFrame", compoundWithValidFrame);
        logger.info("{} compoundWithoutValidFrame", compoundWithoutValidFrame);
        logger.info("{} invalidCompound", invalidCompound);
        logger.info("{} singluarCompound", singluarCompound);
        logger.info("{} genericCompound", genericCompound);
        logger.info("{} compoundWithFactoring", compoundWithFactoring);
        logger.info("{} invalidTemporal", invalidTemporal);
        logger.info("{} genericTemporal", genericTemporal);
        logger.info("{} eventAntecedent", eventAntecedent);
        logger.info("{} logicHighAntecedent", logicHighAntecedent);
        logger.info("{} logicLowAntecedent", logicLowAntecedent);
        logger.info("{} valueAntecedent", valueAntecedent);
        logger.info("{} recursiveTemporal", recursiveTemporal);
        logger.info("{} literalTemporal", literalTemporal);
        logger.info("{} then", then);
        logger.info("{} actionAsState", actionAsState);
        logger.info("{} actionAsValue", actionAsValue);
        logger.info("{} descriptionAsState", descriptionAsState);
        logger.info("{} descriptionAsValue", descriptionAsValue);
        logger.info("{} descriptionAsDescription", descriptionAsDescription);
        logger.info("{} stateConsequent", stateConsequent);
        logger.info("{} modConsequent", modConsequent);
        logger.info("{} descriptionConsequent", descriptionConsequent);
        logger.info("{} propertyConsequent", propertyConsequent);
        logger.info("{} propertyAsState", propertyAsState);
        logger.info("{} propertyAsProperty", propertyAsProperty);
        logger.info("{} normalizedSignalName", normalizedSignalName);
    }

    public static String[] metricNames = {
            "declarative",
            "implicative",
            "modifier",
            "invalidInput",
            "compoundAfterNone",
            "compoundAfterA",
            "compoundAfterC",
            "compoundWithValidFrame",
            "compoundWithoutValidFrame",
            "invalidCompound",
            "singluarCompound",
            "genericCompound",
            "compoundWithFactoring",
            "invalidTemporal",
            "genericTemporal",
            "eventAntecedent",
            "logicHighAntecedent",
            "logicLowAntecedent",
            "valueAntecedent",
            "recursiveTemporal",
            "literalTemporal",
            "then",
            "actionAsState",
            "actionAsValue",
            "descriptionAsState",
            "descriptionAsValue",
            "descriptionAsDescription",
            "stateConsequent",
            "modConsequent",
            "descriptionConsequent",
            "propertyConsequent",
            "propertyAsState",
            "propertyAsProperty",
            "normalizedSignalName"
    };

    private static Document metricsToDocument(Project project) {
        var metric = project.getMetric("currentIrMetric");
        if (metric.isPresent()) {
            var doc = metric.get();
            doc.put("declarative", declarative)
                    .put("implicative", implicative)
                    .put("modifier", modifier)
                    .put("invalidInput", invalidInput)
                    .put("compoundAfterNone", compoundAfterNone)
                    .put("compoundAfterA", compoundAfterA)
                    .put("compoundAfterC", compoundAfterC)
                    .put("compoundWithValidFrame", compoundWithValidFrame)
                    .put("compoundWithoutValidFrame", compoundWithoutValidFrame)
                    .put("invalidCompound", invalidCompound)
                    .put("singluarCompound", singluarCompound)
                    .put("genericCompound", genericCompound)
                    .put("compoundWithFactoring", compoundWithFactoring)
                    .put("invalidTemporal", invalidTemporal)
                    .put("genericTemporal", genericTemporal)
                    .put("eventAntecedent", eventAntecedent)
                    .put("logicHighAntecedent", logicHighAntecedent)
                    .put("logicLowAntecedent", logicLowAntecedent)
                    .put("valueAntecedent", valueAntecedent)
                    .put("recursiveTemporal", recursiveTemporal)
                    .put("literalTemporal", literalTemporal)
                    .put("then", then)
                    .put("actionAsState", actionAsState)
                    .put("actionAsValue", actionAsValue)
                    .put("descriptionAsState", descriptionAsState)
                    .put("descriptionAsValue", descriptionAsValue)
                    .put("descriptionAsDescription", descriptionAsDescription)
                    .put("stateConsequent", stateConsequent)
                    .put("modConsequent", modConsequent)
                    .put("descriptionConsequent", descriptionConsequent)
                    .put("propertyConsequent", propertyConsequent)
                    .put("propertyAsState", propertyAsState)
                    .put("propertyAsProperty", propertyAsProperty)
                    .put("normalizedSignalName", normalizedSignalName);
            return doc;
        } else {
            return Document.createDocument("name", "currentIrMetrics")
                    .put("declarative", declarative)
                    .put("implicative", implicative)
                    .put("modifier", modifier)
                    .put("invalidInput", invalidInput)
                    .put("compoundAfterNone", compoundAfterNone)
                    .put("compoundAfterA", compoundAfterA)
                    .put("compoundAfterC", compoundAfterC)
                    .put("compoundWithValidFrame", compoundWithValidFrame)
                    .put("compoundWithoutValidFrame", compoundWithoutValidFrame)
                    .put("invalidCompound", invalidCompound)
                    .put("singluarCompound", singluarCompound)
                    .put("genericCompound", genericCompound)
                    .put("compoundWithFactoring", compoundWithFactoring)
                    .put("invalidTemporal", invalidTemporal)
                    .put("genericTemporal", genericTemporal)
                    .put("eventAntecedent", eventAntecedent)
                    .put("logicHighAntecedent", logicHighAntecedent)
                    .put("logicLowAntecedent", logicLowAntecedent)
                    .put("valueAntecedent", valueAntecedent)
                    .put("recursiveTemporal", recursiveTemporal)
                    .put("literalTemporal", literalTemporal)
                    .put("then", then)
                    .put("actionAsState", actionAsState)
                    .put("actionAsValue", actionAsValue)
                    .put("descriptionAsState", descriptionAsState)
                    .put("descriptionAsValue", descriptionAsValue)
                    .put("descriptionAsDescription", descriptionAsDescription)
                    .put("stateConsequent", stateConsequent)
                    .put("modConsequent", modConsequent)
                    .put("descriptionConsequent", descriptionConsequent)
                    .put("propertyConsequent", propertyConsequent)
                    .put("propertyAsState", propertyAsState)
                    .put("propertyAsProperty", propertyAsProperty)
                    .put("normalizedSignalName", normalizedSignalName);
        }
    }

    public static void addCountersToProject(Project project) {
        // update current metrics
        var currentMetrics = metricsToDocument(project);
        project.updateMetric("currentIrMetrics", currentMetrics, true);
        // need to update average
        var avgMetrics = project.getMetric("avgIrMetrics");
        if (avgMetrics.isPresent()) {
            var avgMetricsDoc = avgMetrics.get();
            var count = (Double) avgMetricsDoc.get("count");
            for (var key: metricNames) {
                // get old and new metrics value
                logger.debug("{}, {}", count, key);
                var orig = (Double) avgMetricsDoc.get(key);
                var newValue = (Double) currentMetrics.get(key);
                // get cumulative mean
                logger.debug("{}, {}", orig, newValue);
                newValue = (newValue + (count * orig)) / (count + 1);
                // update
                avgMetricsDoc.put(key, newValue);
            }
            // update count
            avgMetricsDoc.put("count", count + 1);
            // update db
            project.updateMetric("avgIrMetrics", avgMetricsDoc);
        } else { // need to create
            var newAverage = metricsToDocument(project);
            newAverage.put("name", "avgIrMetrics");
            newAverage.put("count", 1.0);
            project.addMetric(newAverage, false);
        }
    }

    public static String findIR(SemanticExpression se, AllMappings allMappings) throws ProcessorException {
        logger.debug("Starting new findIR");
        // first need to determine which type of se we are dealing with
        if (se.getAntecedents().isEmpty() && !se.getConsequents().isEmpty()) {
            logger.debug("Declarative Sentence");
            declarative += 1;
            return processFrames(se.getAllFrames(), allMappings);
        } else if (!se.getAntecedents().isEmpty() && !se.getConsequents().isEmpty()) {
            logger.debug( "Implicative Sentence" );
            implicative += 1;
            return processFrames( se.getAllFrames(), allMappings );
        } else if (!se.getAllFrames().isEmpty()) {
            // compound only sentence
            logger.debug( "Modifier (compound) only Sentence" );
            modifier += 1;
            return processFrames(se.getAllFrames(), allMappings);
        } else {
            var message = "Invalid SE, 0 frames.";
            invalidInput += 1;
            throw new IRException(message, new IRContext(message, se, null));
        }
    }

    private enum PREVIOUS_FRAME {NONE, A, C};
    private static String processFrames(List<FrameInstance> frames, AllMappings allMappings) throws IRException, FrameException {
        logger.debug("Processing frames");
        // separate frames into proper antecedents and consequents
        List<FrameInstance> antecedents = new ArrayList<>();
        List<FrameInstance> consequents = new ArrayList<>();
        PREVIOUS_FRAME previous_frame = PREVIOUS_FRAME.NONE;
        for (var frame : frames) {
            var id = frame.getId();
            if (allMappings.getSemanticModel().getAntecedents().contains(id)) {
                antecedents.add(frame);
                previous_frame = PREVIOUS_FRAME.A;
            } else if (allMappings.getSemanticModel().getConsequents().contains(id)) {
                consequents.add(frame);
                previous_frame = PREVIOUS_FRAME.C;
            } else if (allMappings.getSemanticModel().getModifiers().contains(id)) {
                switch (previous_frame) {
                    case NONE:
                    {
                        compoundAfterNone += 1;
                        // in this case we have a possible compound-only frame
                        // if can be factored then process
                        // if not then this is an error
                        if (frame.getMiscTokens() != null && !frame.getMiscTokens().isEmpty()) {
                            var compoundRes = processCompound( frame, allMappings, frames );
                            if (compoundRes.isPresent()) {
                                return compoundRes.get();
                            } else {
                                // skip
                                break;
                            }
                        } else {
                            var message = "Semantic expression starting with non-factorable compound token.";
                            throw new IRException( message, new IRCompoundContext( message, frame, frames) );
                        }
                    }
                    case A:
                        compoundAfterA += 1;
                        antecedents.add(frame);
                        break;
                    case C:
                        compoundAfterC += 1;
                        consequents.add(frame);
                        break;
                }
            }
        }
        logger.debug("Found antecedents: {}", antecedents);
        logger.debug("Found consequents: {}", consequents);
        var sb = new StringBuilder();
        // process antecedent
        sb.append(processFrameGroup(antecedents, allMappings));
        sb.append(" -> ");
        // process consequent
        sb.append(processFrameGroup(consequents, allMappings));
        return sb.toString();
    }

    private static String processFrameGroup(List<FrameInstance> frameGroup, AllMappings allMappings) throws IRException, FrameException {
        var res = process(frameGroup, allMappings);
        return res.map(s -> "(" + s + ")").orElse("");
    }

    public enum COMPOUND_PROCESS_STATE {IDENTIFY, ADD_NORMAL, ADD_COMPOUND, ADD_TEMPORAL, END}

    private static Optional<String> process(List<FrameInstance> antecedents, AllMappings allMappings) throws IRException, FrameException {
        var antecedentIter = antecedents.listIterator();
        COMPOUND_PROCESS_STATE state = COMPOUND_PROCESS_STATE.IDENTIFY;
        List<String> irTokens = new ArrayList<>();
        FrameInstance currentFrame = null;
        logger.debug("Begin compound. {}", antecedents.stream().map(FrameInstance::getId).collect(Collectors.toList()));
        var flag = true;
        while(flag) {
            logger.debug("STATE: {}", state);
            switch (state) {
                case IDENTIFY:
                {
                    if (!antecedentIter.hasNext()) {
                        state = COMPOUND_PROCESS_STATE.END;
                        break;
                    }
                    currentFrame = antecedentIter.next();
                    if (currentFrame == null) {
                        state = COMPOUND_PROCESS_STATE.END;
                        break;
                    }
                    if (isCompound(currentFrame)) {
                        state = COMPOUND_PROCESS_STATE.ADD_COMPOUND;
                    } else if (isTemporal(currentFrame)){
                        state = COMPOUND_PROCESS_STATE.ADD_TEMPORAL;
                    } else {
                        state = COMPOUND_PROCESS_STATE.ADD_NORMAL;

                    }
                    break;
                }
                case ADD_COMPOUND:
                {
                    logger.debug("IN Compound");
                    if (irTokens.isEmpty()) {
                        var message = "Found compound frame at the beginning of antecedents.";
                        invalidCompound += 1;
                        throw new IRException(message, new IRCompoundContext(message, currentFrame, antecedents));
                    } else {
                        logger.debug("Not First");
                        // check type of compound
                        if (currentFrame.getTokens().size() == 1) {
                            singluarCompound += 1;
                            irTokens.add(compoundOperator( currentFrame.getTokens().get(0) ));
                        } else {
                            genericCompound += 1;
                            var compundFrameRes = processCompound( currentFrame, allMappings, antecedents );
                            // if failed to find a valid frame in the compound (action)
                            if (compundFrameRes.isEmpty()) { // if failed
                                compoundWithoutValidFrame += 1;
                                // TODO: try to see if factoring is available
                                // else
                                // add back to previous if it ended in a literal
                                // remove compound frame from iter
                                antecedentIter.previous();
                                antecedentIter.remove();
                                // get previous frame
                                var previousFrame = antecedentIter.previous();
                                logger.debug("Add to previous literal frame: {}", previousFrame.getId());
                                // get tokens and ending token
                                var previousFrameTokens = previousFrame.getTokens();
                                var lastToken = previousFrameTokens.get(previousFrameTokens.size() - 1);
                                // check for literal
                                switch (lastToken.getType()) {
                                    case LITERAL:
                                    case ACCESS:
                                    case COMPOUND:
                                    {
                                        // build new literal token list
                                        List<TokenInstance> newTokens = new ArrayList<>();
                                        // old
                                        newTokens.add(lastToken);
                                        // new
                                        newTokens.addAll(currentFrame.getTokens());
                                        // compound them
                                        var newLastToken = new TokenInstance(TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID);
                                        var newCompound = new CompoundToken(newTokens);
                                        newLastToken.setCompoundToken(newCompound);
                                        // remove old
                                        previousFrameTokens.remove(lastToken);
                                        // add new
                                        previousFrameTokens.add(newLastToken);
                                        // reprocess
                                        previousFrame.setTokensAndLiterals(previousFrameTokens);
                                        // remove old IR
                                        irTokens.remove(irTokens.size() - 1);
                                        // reprocess previous frame with the new literal token
                                        state = COMPOUND_PROCESS_STATE.IDENTIFY;
                                        break;
                                    }
                                    default:
                                        state = COMPOUND_PROCESS_STATE.IDENTIFY;
                                        break;
                                }
                            } else {
                                compoundWithValidFrame += 1;
                                irTokens.add(compundFrameRes.get());
                            }
                            state = COMPOUND_PROCESS_STATE.IDENTIFY;
                        }
                    }
                    break;
                }
                case ADD_TEMPORAL:
                {
                    logger.debug("IN Temporal");
                    if (irTokens.isEmpty()) {
                        invalidTemporal += 1;
                        var message = "Found temporal frame at the beginning of antecedents.";
                        throw new IRException(message, new IRCompoundContext(message, currentFrame, antecedents));
                    } else {
                        logger.debug("Not First");
                        genericTemporal += 1;
                        // get previous frame (and remove it)
                        var previousFrameRes = irTokens.remove(irTokens.size() - 1);
                        var temporalRes = processFrame(currentFrame, allMappings);
                        irTokens.add(String.format("((%s) -> (%s))", temporalRes.orElse("()"), previousFrameRes));
                        state = COMPOUND_PROCESS_STATE.IDENTIFY;
                    }
                    break;
                }
                case ADD_NORMAL:
                {
                    logger.debug("IN Normal");
                    irTokens.add(processFrame(currentFrame, allMappings).orElse(""));
                    state = COMPOUND_PROCESS_STATE.IDENTIFY;
                    break;
                }
                case END:
                    flag = false;
                    break;
            }
        }
        if (irTokens.isEmpty()) {
            logger.debug("Returning Empty");
            return Optional.empty();
        } else {
            logger.debug("Returning IR");
            return Optional.of(
                    "(" +
                    String.join(" ", irTokens) +
                    ")"
            );

        }
    }

    private static Optional< String > processCompound ( FrameInstance compoundFrame, AllMappings allMappings, List< FrameInstance > antecedents ) throws IRException {
        var frameFailed = true;
        var sb = new StringBuilder();
        try {
            // try to see if the next is a valid frame
            var tempTokens = new ArrayList<>(compoundFrame.getTokens());
            // remove first (compound token) and identify
            logger.debug("Trying Frame: {}", tempTokens);
            TokenInstance compoundToken = tempTokens.remove(0);
            var originalNonCompoundTokens = tempTokens.get(0).getCompoundToken().getOriginalTokens();
            logger.debug("Trying Frame: {}", originalNonCompoundTokens);
            // try to find a valid frame in the compound literals
            var tryFrame = allMappings.getFrameFinder().getNextFrame(originalNonCompoundTokens.listIterator(), false );
            frameFailed = tryFrame.isEmpty();
            // if found
            if (!frameFailed) {
                var nonCompoundToken = tryFrame.get();
                // get IR
                var compoundTokenRes = processFrame(nonCompoundToken, allMappings);
                // see if factoring needs to be done
                if (compoundFrame.getMiscTokens() != null && !compoundFrame.getMiscTokens().isEmpty()) {
                    compoundWithFactoring += 1;
                    // change first literal in original to be new literal and reprocess
                    var originalFirstToken = nonCompoundToken.getTokens().get(0);
                    TokenInstance newFirstToken = new TokenInstance( TokenInstance.Type.COMPOUND, null, Constants.LITERAL_TOKEN_ID );
                    var compound = new CompoundToken(compoundFrame.getMiscTokens());
                    newFirstToken.setCompoundToken( compound );
                    // remove and replace
                    nonCompoundToken.getTokens().remove( originalFirstToken );
                    nonCompoundToken.getTokens().add(0, newFirstToken);
                    nonCompoundToken.setTokensAndLiterals( nonCompoundToken.getTokens() );
                    // reprocess
                    var factoredTokenRes = processFrame(nonCompoundToken, allMappings);
                    factoredTokenRes.ifPresent( sb::append );
                }
                // add operator
                sb.append(compoundOperator( compoundToken ) );
                // add right frame
                compoundTokenRes.ifPresent(sb::append);
            }
        } catch (FrameException | IndexOutOfBoundsException e){
            frameFailed = true;
        }
        if (frameFailed) {
            return Optional.empty();
        } else {
            return Optional.of(sb.toString());
        }
    }

    private static String compoundOperator(TokenInstance operator) throws IRException {
        switch (operator.getId()) {
            case 62:
            case 64:
                return "&&";
            case 65:
                return "||";
            default: {
                var message = String.format("Found unknown compound id: %s", operator.getId());
                throw new IRException(message, new IRCompoundContext(message, null, null));
            }
        }
    }

    private static boolean isCompound(FrameInstance frame) {
        return frame.getId().equals(15) || frame.getId().equals(16);
    }

    private static boolean isTemporal(FrameInstance frame) {
        return frame.getId().equals(17);
    }

    private static Optional<String> processFrame(FrameInstance frame, AllMappings allMappings) throws IRException {
        switch (frame.getId()) {
            case 6:
            case 7:
            case 8:
            case 9:
            case 17:
                return processAntecedentFrame(frame, allMappings);
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                return processConsequentFrame(frame, allMappings);
            case 16:
                return Optional.empty();
            default:
                var message = String.format("Unknown frame %d.", frame.getId());
                throw new IRException(message, new IRContext(message, null, frame));
        }
    }

    private static Optional<String> processAntecedentFrame(FrameInstance frame, AllMappings allMappings) throws IRException {
        logger.debug("AF: {}, {}, {}", frame.getId(), frame.getTokens().size(), frame.getLiterals().size());
        switch (frame.getId()) {
            case 6: { // event
                eventAntecedent += 1;
                var name = frame.get(0);
                var sb = new StringBuilder();
                sb.append("STATE(");
                sb.append(normalizeSignalName(Serializer.mergeWords(allMappings.getSerializer().deserialize(name))));
                sb.append(')');
                return Optional.of(sb.toString());
            }
            case 7: { // when logic high
                logicHighAntecedent += 1;
                var name = frame.get(0);
                var sb = new StringBuilder();
                sb.append("STATE(");
                sb.append(normalizeSignalName(Serializer.mergeWords(allMappings.getSerializer().deserialize(name))));
                sb.append(" == '1'");
                sb.append(')');
                return Optional.of(sb.toString());
            }
            case 8: { // when logic low
                logicLowAntecedent += 1;
                var name = frame.get(0);
                var sb = new StringBuilder();
                sb.append("(");
                sb.append(normalizeSignalName(Serializer.mergeWords(allMappings.getSerializer().deserialize(name))));
                sb.append(" == '0'");
                sb.append(')');
                return Optional.of(sb.toString());
            }
            case 9: { // when specified value
                valueAntecedent += 1;
                var name = frame.get(0);
                var value = frame.get(1);
                return Optional.of(getPossibleAction(name, value, allMappings));
            }
            case 17:
            {
                var sb = new StringBuilder();
                // try to get frame from rest of frame
                var tempTokens = new ArrayList<>(frame.getTokens());
                // remove first (compound token) and identify
                TokenInstance temporalToken = tempTokens.remove(0);
                // add temporal modifier
                switch (temporalToken.getId()) {
                    case 72: // after
                    case 74: // during
                        break;
                    case 73: // before
                    case 68: // until
                        sb.append("!");
                        break;
                    default: {
                        var message = String.format("Found unknown temporal id: %s", temporalToken.getId());
                        throw new IRException(message, new IRCompoundContext(message, frame, null));
                    }
                }
                // extract other frame
                var originalNonTemporalTokens = tempTokens.get(0).getCompoundToken().getOriginalTokens();
                logger.debug("Trying Frame: {}", originalNonTemporalTokens);
                var frameFailed = false;
                try {
                    var tryFrame = allMappings.getFrameFinder().getNextFrame(originalNonTemporalTokens.listIterator(), false );
                    frameFailed = tryFrame.isEmpty();
                    if (!frameFailed) {
                        recursiveTemporal += 1;
                        sb.append("(");
                        var tempRes = processFrame(tryFrame.get(), allMappings);
                        tempRes.ifPresent(sb::append);
                        sb.append(")");
                    }
                } catch (FrameException e){
                    frameFailed = true;
                }
                // if failed just add literal
                if (frameFailed) {
                    literalTemporal += 1;
                    sb.append("(STATE(");
                    sb.append(Serializer.mergeWords(allMappings.getSerializer().deserialize(frame.get(0))));
                    sb.append("))");
                }
                return Optional.of(sb.toString());
            }
            case 13:
                then += 1;
                return Optional.empty();
            default:
                var message = String.format("Unknown Antecedent frame %d.", frame.getId());
                throw new IRException(message, new IRContext(message, null, frame));
        }
    }

    private static String getPossibleAction(List<Integer> name, List<Integer> value, AllMappings allMappings) {
        var nameS = normalizeSignalName(Serializer.mergeWords(allMappings.getSerializer().deserialize(name)));
        var valueS = Serializer.mergeWords(allMappings.getSerializer().deserialize(value));
        if (value.size() == 1 && WordIdUtils.getWordIdClass(value.get(0)) == Serializer.WordIDClass.VERB) { // possible action
            actionAsState += 1;
            return "STATE(" + nameS + ", " + valueS + ")";
        } else {
            actionAsValue += 1;
            return "(" + nameS + " == " + valueS + ")";
        }
    }
    private static final Pattern[] LOGIC_PATTERNS= {
            Pattern.compile("high"),
            Pattern.compile("logic high"),
            Pattern.compile("asserted"),
            Pattern.compile("low"),
            Pattern.compile("logic low"),
            Pattern.compile("desserted"),
            Pattern.compile("\\d+")
    };

    private static String getPossibleDescription(List<Integer> name, List<Integer> value, AllMappings allMappings) {
        var nameS = normalizeSignalName(Serializer.mergeWords(allMappings.getSerializer().deserialize(name)));
        var valueS = Serializer.mergeWords(allMappings.getSerializer().deserialize(value));
        logger.debug("DESC: {}, {}", nameS, valueS);
        if (WordIdUtils.getWordIdClass(value.get(0)) == Serializer.WordIDClass.VERB) { // possible action
            descriptionAsState += 1;
            return "STATE(" + nameS + ", " + valueS + ")";
        } else if (Stream.of(LOGIC_PATTERNS).anyMatch(pattern -> pattern.matcher(valueS).find())) {
            descriptionAsValue += 1;
            return "(" + nameS + " == " + valueS + ")";
        } else {
            descriptionAsDescription += 1;
            return "DESC(" + nameS + ", " + valueS + ")";
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
                        stateConsequent += 1;
                        logger.debug("State CF.");
                        sb.append("STATE(");
                        sb.append(names);
                        sb.append(", ");
                        sb.append(descs);
                        sb.append(')');
                        return Optional.of(sb.toString());
                    }
                    case MOD_STATE:
                    {
                        modConsequent += 1;
                        logger.debug("Mod State CF.");
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
                        logger.debug("Desc CF.");
                        descriptionConsequent += 1;
                        return Optional.of(getPossibleDescription(name, desc, allMappings));
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
                propertyConsequent += 1;
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
            propertyAsState += 1;
            return "STATE(" + nameS + ", " + propS + ")";
        } else {
            if (propS.contains("of")) {
                var split = propS.split("of");
                try {
                    propertyAsProperty += 1;
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
            Pattern.compile("port (\\w+( )?(\\[.*?])?)"),
            Pattern.compile("signal (\\w+( )?(\\[.*?])?)"),
            Pattern.compile("register (\\w+( )?(\\[.*?])?)"),
            Pattern.compile("name (\\w+( )?(\\[.*?])?)"),
    };

    /**
     * Helper method to extract the actual signal name from a phrase like
     *  "port x", "signal y", "register q"
     * @param signalName
     * @return
     */
    private static String normalizeSignalName(String signalName) {
        normalizedSignalName += 1;
        // check patterns
        for (var pattern: signalNamePatterns) {
            var t = pattern.matcher(signalName);
            if (t.find()) {
                logger.debug("Using pattern: {} for {}", pattern.pattern(), signalName);
                return t.group(1);
            }
        }
        return signalName;
    }
}
