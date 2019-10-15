package edu.vt.datasheet_text_processor.signals;

import edu.vt.datasheet_text_processor.Project;
import edu.vt.datasheet_text_processor.Sentence;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class that looks through a project and finds possible acronyms for a given signal name list.
 *
 * It also tries to find potential new acronyms by looking at literal tokens (for now) and making possible acronyms
 *  from the words that are present and seeing if there are any matches.
 */
public class AcronymFinder {
    private final static Logger logger = LogManager.getLogger(AcronymFinder.class);

    /**
     * Initialize acronym collection with the signals collection
     * @param project
     */
    public static void initializeAcronyms(Project project) {
        // create collection for acronyms
        var db = project.getDB();
        var collection = db.getRepository(Acronym.class);
        if (!db.hasRepository(Signal.class)){
            logger.info("Signals collection not available. Skipping...");
        } else {
            var signals = db.getRepository(Signal.class);
            for (var signal: signals.find()) {
                // if there are acronyms then add them
                if (signal.getAcronyms() != null ){
                    for (var acronym: signal.getAcronyms()) {
                        collection.insert(new Acronym(acronym, signal.getName()));
                    }
                } else { // if not then convert signal name to an acronym (simple first letter of each word)
                    var lowered = signal.getName();
                    var acronym = "";
                    for (var word: lowered.split(" ")) {
                        acronym += word.charAt(0);
                    }
                    collection.insert(new Acronym(acronym, signal.getName()));
                }

            }
        }
    }

    /**
     * Finds acronyms from the literal tokens in the project
     * @param project
     */
    public static void findAcronyms(Project project, Serializer serializer) {
        var db = project.getDB();
        if (db.hasRepository(Sentence.class)) {
            var sentences = db.getRepository(Sentence.class);
            var acronyms = db.getRepository(Acronym.class);
            var nonComments = sentences.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT));
            for (var sentence: nonComments) {
                // find acronyms and add to project
                sentence.getTokens().stream()
                        .filter(t -> t.getType() == TokenInstance.Type.LITERAL)
                        .map(literal -> getAcronym(literal, serializer))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(pair -> new Acronym(pair.getLeft(), pair.getRight()))
                        .forEach(acronym -> acronyms.insert(acronym));

            }
        }
    }

    /**
     *
     * @param literal
     * @return
     */
    public static Optional<Pair<String, String>> getAcronym(TokenInstance literal, Serializer serializer){
        if (literal.getType() == TokenInstance.Type.LITERAL) {
            // get words
            logger.debug("Tokens: {}", literal.getStream());
            var words = serializer.deserialize(literal.getStream());
            logger.debug("Words: {}", words);
            var firstLetters = words.stream()
                    .filter(word -> !word.isEmpty())
                    .map(word -> word.charAt(0))
                    .collect(Collectors.toList());
            logger.debug("Letters: {}", firstLetters);
            // get all possible acronyms (sequences of first letters from len = 2 to len = words.length)
            var allAcronyms = new ArrayList<Pair<String, String>>();
            for (var i = 2; i <= firstLetters.size(); i++ ) {
                var begin = 0;
                while (begin < firstLetters.size() - i) {
                    // get sublist
                    var lettersSublist = firstLetters.subList(begin, begin + i);
                    var wordsSublist = words.subList(begin, begin + i);
                    var acronym = lettersSublist.stream().map(String::valueOf).collect(Collectors.joining());
                    var expanded = String.join(" ", wordsSublist);
                    // add to list
                    allAcronyms.add(Pair.of(acronym, expanded));
                    begin++;
                }
            }
            // search words
            for (var word: words) {
                for (var acronymPair: allAcronyms) {
                    if (acronymPair.getLeft().equals(word)) {
                        return Optional.of(acronymPair);
                    }
                }
            }
        }
        // default
        return Optional.empty();
    }
}
