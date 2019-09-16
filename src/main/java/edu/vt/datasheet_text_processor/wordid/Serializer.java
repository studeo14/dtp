package edu.vt.datasheet_text_processor.wordid;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

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
    public enum WordIDClass {
        JUNK, OBJECT, VERB, NUMBER, MODIFIER, ADJECTIVE, PRONOUN, CONDITION
    }

    public enum VerbEndings {
        NONE, S, ED, ING
    }

    private Mapping mapping;
    private Map< WordIDClass, Integer > currentNumber;
    private static final Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

    public Serializer ( File mappingFile ) throws IOException {
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
    }

    public List< Integer > serialize ( String sentence, boolean addNew ) {
        // convert to list of base words (stemming)
        return Arrays.stream( tokenizer.tokenize( sentence ) )
                .map( this::stem )
                .map( s -> convert( s, addNew ) )
                .collect( Collectors.toList() );

    }

    public void exportMapping ( File outputFile ) throws IOException {
        new ObjectMapper().writer( new DefaultPrettyPrinter() ).writeValue( outputFile, mapping );
    }

    private String stem ( String input ) {
        return mapping.getAliases().getOrDefault( input, input );
    }


    private Integer addNewMapping ( String input ) {
        Integer cat;
        do {
            cat = Integer.parseInt( System.console().readLine( "%s is unmapped. Enter category to add to mapping (0/1/2/3/4/5/6/7): ", input ) );
        } while ( cat < 0 || cat > 7 );
        var catClass = classNumToClass( cat );
        var num = currentNumber.getOrDefault( catClass, 0 ) + 1;
        currentNumber.put( catClass, num );
        var mappingInteger = ( cat * 10000 ) + ( num * 10 );
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
        mapping.getBaseMapping().put( stem, mappingInteger );
        return mappingInteger;
    }

    private Integer convert ( String input, boolean addNew ) {
        // get base convertion
        var base = mapping.getBaseMapping().getOrDefault( input, 0 );
        if ( base == 0 && addNew ) {
            base = addNewMapping( input );
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
            }
        } else if ( baseClass == WordIDClass.OBJECT ) {
            if ( input.endsWith( "s" ) ) {
                base += 3;
            }
        }
        return base;
    }
}
