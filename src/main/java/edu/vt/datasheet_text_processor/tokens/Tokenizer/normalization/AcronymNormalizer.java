package edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization;

import edu.vt.datasheet_text_processor.Project;
import edu.vt.datasheet_text_processor.Sentence;
import edu.vt.datasheet_text_processor.signals.Acronym;
import edu.vt.datasheet_text_processor.tokens.Tokenizer.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AcronymNormalizer {
    private static final Logger logger = LogManager.getLogger(AcronymNormalizer.class);

    public static void normalizeAcronyms(Project project, Serializer serializer) {
        var db = project.getDB();
        if (db.hasRepository(Sentence.class)) {
            var sentences = db.getRepository(Sentence.class);
            var nonComments = sentences.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT));
            for (var sentence: nonComments) {
                var tokensString = sentence.getTokens().stream()
                        .map(t -> Serializer.mergeWords(serializer.deserialize(t.getStream())))
                        .collect(Collectors.toList());
                var didChange = false;
                List<TokenInstance> tokens = sentence.getTokens();
                for (var token: tokens) {
                    if (token.getType() == TokenInstance.Type.LITERAL) {
                        // see if it contains an acronym
                        var stream = token.getStream();
                        var newStream = hasAcronym(project, stream, serializer);
                        if (newStream.isPresent()) {
                            token.setStream(newStream.get());
                            didChange = true;
                        }
                    }
                }
                if (didChange) {
                    logger.debug(tokensString);
                    sentences.update(sentence);
                    tokensString = sentence.getTokens().stream()
                            .map(t -> Serializer.mergeWords(serializer.deserialize(t.getStream())))
                            .collect(Collectors.toList());
                    logger.debug("{}",tokensString);
                }
            }
        }
    }

    static Optional<List<Integer>> hasAcronym(Project project, List<Integer> stream, Serializer serializer) {
        var text = Serializer.mergeWords(serializer.deserialize(stream));
        var db = project.getDB();
        // search through acronyms for match
        if (db.hasRepository(Acronym.class)) {
            var acronyms = db.getRepository(Acronym.class).find();
            for (var acronym: acronyms) {
                if (text.contains(acronym.getExpanded())) {
                    logger.debug("Found.");
                    var newText = text.replace(acronym.getExpanded(), acronym.getAcronym());
                    logger.debug("New Text: {}", newText);
                    var newStream = serializer.serialize(newText, new AddNewWrapper(false));
                    logger.debug(newStream);
                    return Optional.of(newStream);
                }
            }
        }
        return Optional.empty();
    }
}
