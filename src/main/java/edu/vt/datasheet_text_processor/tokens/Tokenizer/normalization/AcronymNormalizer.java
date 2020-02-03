package edu.vt.datasheet_text_processor.tokens.Tokenizer.normalization;

import edu.vt.datasheet_text_processor.Errors.SerializerException;
import edu.vt.datasheet_text_processor.Errors.Warning;
import edu.vt.datasheet_text_processor.Project;
import edu.vt.datasheet_text_processor.Sentence;
import edu.vt.datasheet_text_processor.signals.Acronym;
import edu.vt.datasheet_text_processor.tokens.TokenInstance.TokenInstance;
import edu.vt.datasheet_text_processor.wordid.AddNewWrapper;
import edu.vt.datasheet_text_processor.wordid.Serializer;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AcronymNormalizer {
    private static final Logger logger = LoggerFactory.getLogger(AcronymNormalizer.class);

    public static void normalizeAcronyms(Project project, Serializer serializer) {
        var db = project.getDB();
        if (db.hasRepository(Sentence.class)) {
            var sentences = db.getRepository(Sentence.class);
            var nonComments = sentences.find(ObjectFilters.eq("type", Sentence.Type.NONCOMMENT));
            for (var sentence: nonComments) {
                try {
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
                        logger.debug("{}", tokensString);
                        sentences.update(sentence);
                        tokensString = sentence.getTokens().stream()
                                .map(t -> Serializer.mergeWords(serializer.deserialize(t.getStream())))
                                .collect(Collectors.toList());
                        logger.debug("{}",tokensString);
                    }
                } catch (SerializerException e) {
                    var warning = new Warning(e);
                    sentence.getWarnings().add(warning);
                    sentences.update(sentence);
                }
            }
        }
    }

    static Optional<List<Integer>> hasAcronym(Project project, List<Integer> stream, Serializer serializer) throws SerializerException {
        var text = Serializer.mergeWords(serializer.deserialize(stream));
        var db = project.getDB();
        // search through acronyms for match
        if (db.hasRepository(Acronym.class)) {
            var acronyms = db.getRepository(Acronym.class).find();
            var returnCandidates = new ArrayList<List<Integer>>();
            for (var acronym: acronyms) {
                if (text.contains(acronym.getExpanded())) {
                    logger.debug("Found.");
                    var newText = text.replace(acronym.getExpanded(), acronym.getAcronym());
                    logger.debug("New Text: {}", newText);
                    var newStream = serializer.serialize(newText, false);
                    logger.debug("{}", newStream);
                    returnCandidates.add(newStream);
                }
            }
            // sort by acronym length and choose the largest match
            if (!returnCandidates.isEmpty()) {
                returnCandidates.sort(Comparator.comparingInt(List::size));
                return Optional.of(returnCandidates.get(0));
            }
        }
        return Optional.empty();
    }
}
