package edu.vt.datasheet_text_processor.classification;

import edu.vt.datasheet_text_processor.Project;
import edu.vt.datasheet_text_processor.Sentence;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatasheetBOW {
    private static final Logger logger = LogManager.getLogger(DatasheetBOW.class);
    private static final String actions = "(assert|asserts|reassert|deassert|set|clear|start|stop|enabled|disable|reset|activate|generate)";
    private static final String conditions = "(when|before|after|during|when|at|upon|on|while|if)";
    private static final String values = "(zero|one|0|1|high|low|enabled|disabled|asserted|deasserted)";
    private static final String signals = "(input|output|register|signal|bit|bus|value)";

    private static final Pattern[] patterns = {
            Pattern.compile("writing .+? to .+?"),
            Pattern.compile("reset value"),
            Pattern.compile(String.format("(occur|occurs).+?%s", conditions)),
            Pattern.compile("(are|must be) (sampled|captured)"),
            Pattern.compile("default value"),
            Pattern.compile("\\w+? (can|cannot|can.t) be accessed"),
            Pattern.compile("it (does|doesn.t|does not) (clear|set)"),
            Pattern.compile("(overwrite|overwrites)"),
            Pattern.compile("waits for "),
            Pattern.compile("should be equal to"),
            Pattern.compile("(upon|on|during) reset"),
            Pattern.compile("(can|cannot|can.t) be (cleared|set|asserted|deasserted)"),
            Pattern.compile("sampled (at|when|if|during)"),
            Pattern.compile("allowed (value|values|range)"),
            Pattern.compile("(read|write)-only"),
            Pattern.compile("(read|write) only"),
            Pattern.compile("is ignored"),
            Pattern.compile("written to"),
            Pattern.compile("not written"),
            Pattern.compile("will reset .+?"),
            Pattern.compile("(set|asserted|driven) (low|high)"),
            Pattern.compile("must be (high|set|low|asserted|enabled|deasserted|disabled)"),
            Pattern.compile("(update|reset).+(upon|on|when|if|during|after|before)"),
            Pattern.compile("synchronous (to|with)"),
            Pattern.compile("causes a hard reset"),
            Pattern.compile("must take place (after|during|before|while)"),
            Pattern.compile("(asserted|deasserted|enabled|disabled|set|cleared) fo"),
            Pattern.compile("valid values"),
            Pattern.compile("(tied|held|kept) (high|low|asserted|deasserted)"),
            Pattern.compile("(maximum|max|minimum|min) (number|value)"),
            Pattern.compile("by (setting|clearing|asserting|deasserting)"),
            Pattern.compile("when .+? (true|false)"),
            Pattern.compile("setting .+? to"),
            Pattern.compile("per clock cycle"),
            Pattern.compile("wired to"),
            Pattern.compile("values range (from|between)"),
            Pattern.compile("value (on|of) .+? is"),
            Pattern.compile("will clear "),
            Pattern.compile("is unaffected by"),
            Pattern.compile("restrictions .+? be able to"),
            Pattern.compile("can go active"),
            Pattern.compile(".+? is active"),
            Pattern.compile("possible values"),
            Pattern.compile(".+? defaults to .+?"),
            Pattern.compile("(during|while|before|after) .+? (transfers|interactions|exchanges|transactions)"),
            Pattern.compile("(at|when|after|before|if) .+? (zero|one|0|1|high|low|enabled|disabled|asserted|deasserted)"),
            Pattern.compile("(bit|signal|register|bus|value) .+? (asserts|deasserts|enables|disables|activates|sends|starts|stops)"),
            Pattern.compile("padding .+? (bits|bus|register|value)"),
            Pattern.compile("(asserts|reassert|reasserts|assert)"),
            Pattern.compile("(is|is not) (cleared|set|asserted|deasserted|enabled|disabled)"),
            Pattern.compile(String.format("%s set to", conditions)),
            Pattern.compile(String.format("set .+?%s", conditions)),
            Pattern.compile("(hardwired|tied-off|tie-off|constant) to"),
            Pattern.compile("active (low|high)"),
            Pattern.compile("active-(low|high)"),
    };

    public enum Action {
        PERCENTAGE, CLASSIFY, CLASSIFY_CAS, CLASSIFY_CAS2TXT, MOST_USED, DEBUG_COMMENTS, DEBUG_QUESTIONABLE, NONE
    }

    // TODO: update for current flow
    private static void most_used(String filename) throws IOException {
        // init
        Map<Pattern, Integer> matchCount = new HashMap<>();
        for (Pattern p : patterns) {
            matchCount.put(p, 0);
        }
        // read and process file
        Files
                // preprocessing
                .lines(Paths.get(filename))
                .filter(line -> !line.isEmpty())
                .map(String::toLowerCase)
                // non-comments only
                .map(line -> Pair.of(line, is_questionable_debug(line)))
                .filter(line -> line.getRight().getLeft())
                // count each
                .forEach(line -> {
                    var pat = line.getRight().getRight();
                    matchCount.put(pat, matchCount.get(pat) + 1);
                });

        // sort and print
        matchCount.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue))
                .forEach(entry -> System.out.println(String.format("%d :: %s", entry.getValue(), entry.getKey().pattern())));
    }

    /**
     * Finds is a sentence matches any of the patterns
     *
     * @param sentence
     * @return true or false
     */
    public static boolean is_questionable(String sentence) {
        for (Pattern p : patterns) {
            Matcher tempMatcher = p.matcher(sentence);
            if (tempMatcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds if a sentence matches any of the patterns and gives the first pattern matched.
     *
     * @param sentence
     * @return Pair of (match?, pattern)
     */
    public static Pair<Boolean, Pattern> is_questionable_debug(String sentence) {
        // return first pattern matched as a pair
        for (Pattern p : patterns) {
            Matcher tempMatcher = p.matcher(sentence);
            if (tempMatcher.find()) {
                return Pair.of(true, p);
            }
        }
        return Pair.of(false, null);
    }

    /**
     * Finds all matches for a sentence.
     *
     * @param sentence
     * @return Pair of (match?, patterns)
     */
    public static Pair<Boolean, List<Pattern>> is_questionable_matches(String sentence) {
        boolean retBool = false;
        List<Pattern> retList = new ArrayList<>();
        // add to list of patterns that match and set bool if matched at all
        for (Pattern p : patterns) {
            Matcher tempMatcher = p.matcher(sentence);
            if (tempMatcher.find()) {
                retBool = true;
                retList.add(p);
            }
        }
        return Pair.of(retBool, retList);
    }

    // TODO: update for current flow
    public static Pair<Integer, Integer> count_questionable(final Project project) {
        var wrapper = new Object() {
            int total = 0;
            int quest = 0;
        };
        project.getSentences()
                .forEach(s -> {
                    wrapper.total++;
                    if (s.getType() == Sentence.Type.NONCOMMENT)
                        wrapper.quest++;
                });
        return Pair.of(wrapper.total, wrapper.quest);
    }

    // TODO: update for current flow
    public static void debug_file_questionable(final Project project) {
        project.getSentences()
                .toList().stream()
                .filter(s -> s.getType() == Sentence.Type.NONCOMMENT)
                .map(s -> Pair.of(s.getText(), is_questionable_debug(s.getText()).getRight()))
                .forEach(p -> System.out.printf("%s :: %s\n", p.getLeft(), p.getRight().pattern()));
    }

    // TODO: update for current flow
    public static void debug_file_comments(final Project project) {
        project.getSentences()
                .toList().stream()
                .filter(s -> s.getType() == Sentence.Type.COMMENT)
                .forEach(s -> System.out.println(s.getText()));
    }

    public static void debug_file_matches(final Project project) {
        project.getSentences()
                .toList().stream()
                .filter(s -> s.getType() == Sentence.Type.NONCOMMENT)
                .map(s -> Pair.of(s.getText(), is_questionable_matches(s.getText()).getRight()))
                .forEach(p -> logger.info("{} :: {}", p.getLeft(), p.getRight()));
    }

}
