package edu.vt.datasheet_text_processor.classification;

import edu.vt.datasheet_text_processor.Project;
import edu.vt.datasheet_text_processor.Sentence;
import edu.vt.datasheet_text_processor.signals.Signal;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DatasheetBOW {
    private static final Logger logger = LoggerFactory.getLogger(DatasheetBOW.class);
    private static final String actions = "(assert|asserts|reassert|deassert|set|clear|start|stop|enabled|disable|reset|activate|generate)";
    private static final String conditions = "(when|before|after|during|when|at|upon|on|while|if)";
    private static final String values = "(zero|one|0|1|high|low|enabled|disabled|asserted|deasserted)";
    private static final String signals = "(input|output|register|signal|bit|bus|value|fifo|latch|name)";

    // regexes sorted by most used
    private static final Pattern[] patterns = {
            Pattern.compile("writing .+? to .+?"),
            Pattern.compile("(^| )the (port|bit) .+? is "),
            Pattern.compile(" has a width of"),
            Pattern.compile(String.format("(^| )%s .*?%s .*?is ", conditions, signals)),
            Pattern.compile("reset value"),
            Pattern.compile(String.format("(occur|occurs).+?%s", conditions)),
            Pattern.compile("(^| )(are|must be) (sampled|captured)"),
            Pattern.compile("default value"),
            Pattern.compile("\\w+? (can|cannot|can.t) be accessed"),
            Pattern.compile("(^| )it (does|doesn.t|does not) (clear|set)"),
            Pattern.compile("(overwrite|overwrites)"),
            Pattern.compile("waits for "),
            Pattern.compile("should be equal to"),
            Pattern.compile("(^| )(upon|on|during) reset"),
            Pattern.compile("(^| )(can|cannot|can.t) be (cleared|set|asserted|deasserted)"),
            Pattern.compile("sampled (at|when|if|during)"),
            Pattern.compile("allowed (value|values|range)"),
            Pattern.compile("(read|write)-only"),
            Pattern.compile("(read|write) only"),
            Pattern.compile("is ignored"),
            Pattern.compile("written to"),
            Pattern.compile("not written"),
            Pattern.compile("will reset .+?"),
            Pattern.compile("(^| )(set|asserted|driven) (low|high)"),
            Pattern.compile("must be (high|set|low|asserted|enabled|deasserted|disabled)"),
            Pattern.compile("(update|reset).+(upon|on|when|if|during|after|before)"),
            Pattern.compile("synchronous (to|with)"),
            Pattern.compile("causes a hard reset"),
            Pattern.compile("must take place (after|during|before|while)"),
            Pattern.compile("(^| )(asserted|deasserted|enabled|disabled|set|cleared) to"),
            Pattern.compile("valid values"),
            Pattern.compile("(^| )(tied|held|kept) (high|low|asserted|deasserted)"),
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
            Pattern.compile("(^| )(at|when|after|before|if) .+? (is|is set to|equals) (zero|one|0|1|high|low|enabled|disabled|asserted|deasserted)( |$|\\.)"),
            Pattern.compile("(^| )(bit|signal|register|bus|value) .+? (asserts|deasserts|enables|disables|activates|sends|starts|stops)"),
            Pattern.compile("padding .+? (bits|bus|register|value)"),
            Pattern.compile("(^| |-)(asserts|reassert|reasserts|assert|deassert|deasserts)"),
            Pattern.compile("(^| )(is|is not|are|are not) (cleared|set|asserted|deasserted|enabled|disabled|sent|transmitted|checked)"),
            Pattern.compile(String.format("(^| )%s set to", conditions)),
            Pattern.compile(String.format("(^| )set .*? %s", conditions)),
            Pattern.compile("(hardwired|tied-off|tie-off|constant) to"),
            Pattern.compile("active (low|high)"),
            Pattern.compile("active-(low|high)"),
            Pattern.compile(String.format("(^| )%s .+? (is|must be|shall be) ", signals)),
    };

    public enum Action {
        PERCENTAGE, CLASSIFY, CLASSIFY_CAS, CLASSIFY_CAS2TXT, MOST_USED, DEBUG_COMMENTS, DEBUG_QUESTIONABLE, NONE
    }

    public static void most_used(final Project project) {
        // init
        Map<Pattern, Integer> matchCount = new HashMap<>();
        for (Pattern p : patterns) {
            matchCount.put(p, 0);
        }
        // read and process file
        project.getSentences()
                .toList().stream()
                .filter(s -> s.getType() == Sentence.Type.NONCOMMENT)
                .forEach(s -> {
                    var match = is_questionable_debug(s.getText());
                    match.ifPresent(patternListPair -> matchCount.put(patternListPair.getLeft(), matchCount.get(patternListPair.getLeft()) + 1));
                });
        // sort and print
        matchCount.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue))
                .forEach(entry -> System.out.println(String.format("%d :: %s", entry.getValue(), entry.getKey().pattern())));
    }

    /**
     * Utility method to construct a regex from the current signals in the project.
     * @param project
     * @return
     */
    public static Optional<Pattern> getSignalRegex(Project project) {
        // create search regex for signal names
        var db = project.getDB();
        var signalRepo = db.getRepository(Signal.class);
        var signals = signalRepo.find().toList();
        var signalNames = signals.stream()
                .map(Signal::getName)
                .collect(Collectors.joining("|"));
        var acronyms = signals.stream()
                .map(Signal::getAcronyms)
                .map(a -> String.join("|", a))
                .collect(Collectors.joining("|"));
        if (!signalNames.isEmpty() && !signalNames.isBlank())  {
            var regexString = "(" + signalNames;
            if (!acronyms.isEmpty() && !acronyms.isBlank())  {
                regexString += "|" + acronyms;
            }
            regexString += ")";
            return Optional.of(Pattern.compile(regexString));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Experimental function.
     *
     * First checks if a signal name is present in the sentence. If so then automatically acccepts the sentence as
     * questionable. Otherwise uses normal classification methods.
     * @param project
     * @param sentence
     * @return
     */
    public static boolean is_questionable__with_signal_names__(Project project, String sentence) {
        var signalRegex = getSignalRegex(project);
        if (signalRegex.isPresent()) {
            var signalRegexActual = signalRegex.get();
            var match = signalRegexActual.matcher(sentence);
            if (match.find()) {
                return true;
            } else {
                return is_questionable(sentence);
            }
        } else {
            return is_questionable(sentence);
        }
    }

    /**
     * Experimental function.
     *
     * Only checks if a signal name is present in the sentence. If so then automatically acccepts the sentence as
     * questionable.
     * @param project
     * @param sentence
     * @return
     */
    public static boolean is_questionable__only_signal_names__(Project project, String sentence) {
        var signalRegex = getSignalRegex(project);
        if (signalRegex.isPresent()) {
            var signalRegexActual = signalRegex.get();
            var match = signalRegexActual.matcher(sentence);
            return match.find();
        }
        return false;
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
     * Helper fuction to extract the pattern and group matches from a matcher object
     * @param matcher
     * @return
     */
    public static Pair<Pattern, List<String>> extractMatches(Matcher matcher) {
        var groups = new ArrayList<String>();
        for (var i = 1; i <= matcher.groupCount(); i++) {
            groups.add(matcher.group(i));
        }
        return Pair.of(matcher.pattern(), groups);
    }

    /**
     * Finds if a sentence matches any of the patterns and gives the first pattern matched.
     *
     * @param sentence
     * @return Optional of the matched pattern, empty if no matches found
     */
    public static Optional<Pair<Pattern, List<String>>> is_questionable_debug(String sentence) {
        // return first pattern matched as a pair
        for (Pattern p : patterns) {
            Matcher tempMatcher = p.matcher(sentence);
            if (tempMatcher.find()) {
                return Optional.of(extractMatches(tempMatcher));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all matches for a sentence.
     *
     * @param sentence
     * @return Pair of (match?, patterns)
     */
    public static Optional<List<Pattern>> is_questionable_matches(String sentence) {
        boolean retBool = false;
        List<Pattern> retList = new ArrayList<>();
        // add to list of patterns that match and set bool if matched at all
        for (Pattern p : patterns) {
            Matcher tempMatcher = p.matcher(sentence);
            if (tempMatcher.find()) {
                retList.add(p);
            }
        }
        return Optional.empty();
    }

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

    public static void debug_file_questionable(final Project project) {
        project.getSentences()
                .toList().stream()
                .filter(s -> s.getType() == Sentence.Type.NONCOMMENT)
                .map(s -> Pair.of(s.getText(), is_questionable_debug(s.getText())))
                .filter(s -> s.getRight().isPresent())
                .forEach(p -> System.out.printf("%s :: %s\n", p.getLeft(), p.getRight().get()));
    }

    public static void debug_file_noncomments(final Project project) {
        project.getSentences()
                .toList().stream()
                .filter(s -> s.getType() == Sentence.Type.NONCOMMENT)
                .forEach(s -> System.out.println(s.getText()));
    }

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
                .map(s -> Pair.of(s.getText(), is_questionable_matches(s.getText())))
                .filter(s -> s.getRight().isPresent())
                .forEach(p -> logger.info("{} :: {}", p.getLeft(), p.getRight().get()));
    }

}
