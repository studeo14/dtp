package edu.vt.datasheet_text_processor.wordid;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.vt.datasheet_text_processor.util.Constants;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.vt.datasheet_text_processor.wordid.WordIdUtils.*;

/**
 * Class that takes in a wordid mapping dataset and sentence
 * and returns a stream (List) of word ids (integers) instead
 */
public class Serializer {
    private final static Logger logger = LogManager.getLogger(Serializer.class);
    public enum WordIDClass {
        JUNK(0), OBJECT(1), VERB(2), NUMBER(3), MODIFIER(4), ADJECTIVE(5), PRONOUN(6), CONDITION(7);
        private Integer number;
        public Integer getNumber() {
            return number;
        }
        WordIDClass(Integer number) {
            this.number = number;
        }
    }

    public enum VerbEndings {
        NONE, S, ED, ING, EN
    }

    private Mapping mapping;
    private InverseMapping inverseMapping;
    private Map< WordIDClass, Integer > currentNumber;
    private static final Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

    public Serializer( Mapping mapping) {
        this.mapping = mapping;
        this.currentNumber = new HashMap<>();
        this.mapping.getBaseMapping().values()
                .forEach( i -> {
                    var num = getWordIdNumber( i );
                    var c = getWordIdClass( i );
                    if ( currentNumber.getOrDefault( c, 0 ) < num ) {
                        currentNumber.put( c, num );
                    }
                } );
        var t = this.mapping.getBaseMapping().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        this.inverseMapping = new InverseMapping(t);
    }

    public Serializer(File mappingFile ) throws IOException {
        // try to read in the file
        this.mapping = new ObjectMapper().readValue( mappingFile, Mapping.class );
        this.currentNumber = new HashMap<>();
        this.mapping.getBaseMapping().values()
                .forEach( i -> {
                    var num = getWordIdNumber( i );
                    var c = getWordIdClass( i );
                    if ( currentNumber.getOrDefault( c, 0 ) < num ) {
                        currentNumber.put( c, num );
                    }
                } );
        var t = this.mapping.getBaseMapping().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        this.inverseMapping = new InverseMapping(t);
    }

    public List< Integer > serialize (String sentence, AddNewWrapper addNew ) {

        // convert to list of base words (stemming)
        return Arrays.stream( tokenizer.tokenize( sentence ) )
                .map( this::stem )
                .map( s -> {
                    try {
                        return convert( s, addNew.isValue() );
                    } catch (StopAddNewException e) {
                        addNew.setValue(false);
                        return 0;
                    }
                })
                .collect( Collectors.toList() );

    }

    public List<String> deserialize( List<Integer> sentence) {
        return sentence.stream()
                .map(this::unconvert)
                .collect(Collectors.toList());
    }

    public static String mergeWords(List<String> sentence) {
        return String.join(" ", sentence);
    }

    public String unconvert( Integer wordId ) {
        var baseMapping = WordIdUtils.getBase(wordId);
        return inverseMapping.getMapping().getOrDefault(baseMapping, "");
    }

    public void exportMapping ( File outputFile ) throws IOException {
        new ObjectMapper().writer( new DefaultPrettyPrinter() ).writeValue( outputFile, mapping );
    }

    private String stem ( String input ) {
        return mapping.getAliases().getOrDefault( input, input );
    }

    public Integer addWordToMapping(String baseWord, WordIDClass catClass ) {
        // convert to number id
        var cat = catClass.getNumber();
        // get next available number in category
        var num = currentNumber.getOrDefault( catClass, 0 ) + 1;
        currentNumber.put( catClass, num );
        // realign integer
        var mappingInteger = ( cat * 10000 ) + ( num * 10 );
        // add
        mapping.getBaseMapping().put( baseWord, mappingInteger );
        // need to add to inverse mapping
        inverseMapping.getMapping().put(mappingInteger, baseWord);
        //
        return mappingInteger;
    }

    private Integer addNewMapping ( String input ) throws StopAddNewException {
        Integer cat;
        do {
            var res = System.console().readLine( "%s is unmapped. Enter category to add to mapping (0/1/2/3/4/5/6/7) 'q' to stop adding new (this may result in unmapped words!): ", input );
            if (res.toLowerCase().equals("q")) {
                throw new StopAddNewException();
            }
            try {
                cat = Integer.parseInt(res);
            } catch (NumberFormatException e) {
                cat = -1;
            }
        } while ( cat < 0 || cat > 7 );
        var catClass = classNumToClass( cat );
        var stem = input;
        if ( catClass == WordIDClass.VERB ) {
            if ( !System.console().readLine( "Is %s a stem? (y/n)", input ).toLowerCase().equals( "y" ) ) {
                // get stem
                stem = System.console().readLine( "Enter Stem: " );
            }
            // get aliases
            var aliases = System.console().readLine( "Enter aliases separated by commas: " ).split( "," );
            // add aliases
            for ( var alias : aliases ) {
                mapping.getAliases().put( alias, stem );
            }
        }
        // add base mapping
        return addWordToMapping(stem, catClass);
    }

    public Integer convert ( String input, boolean addNew ) throws StopAddNewException {
        // get base convertion
        var base = mapping.getBaseMapping().getOrDefault( input, Constants.DEFAULT_WORD_ID );
        if (base.equals(Constants.DEFAULT_WORD_ID)) {
            if (addNew) {
                base = addNewMapping( input );
            } else {
                logger.info("No existing mapping found for {}", input);
            }
        }
        var baseClass = getWordIdClass( base );
        if ( baseClass == WordIDClass.VERB ) {
            // look at ending
            if ( input.endsWith( "ing" ) ) {
                base += 1;
            } else if ( input.endsWith( "ed" ) ) {
                base += 2;
            } else if ( input.endsWith( "s" ) ) {
                base += 3;
            } else if ( input.endsWith( "en" ) ) {
                base += 4;
            }
        } else if ( baseClass == WordIDClass.OBJECT ) {
            if ( input.endsWith( "s" ) ) {
                base += 3;
            }
        }
        return base;
    }
}
