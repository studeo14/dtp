package edu.vt.datasheet_text_processor.wordid;

public class WordIdUtils {
    public static Serializer.WordIDClass getWordIdClass (Integer wordId ) {
        var classNum = ( wordId / 10000 );
        return classNumToClass( classNum );
    }

    public static Serializer.WordIDClass classNumToClass (Integer classNum ) {
        switch ( classNum ) {
            default:
            case 0:
                return Serializer.WordIDClass.JUNK;
            case 1:
                return Serializer.WordIDClass.OBJECT;
            case 2:
                return Serializer.WordIDClass.VERB;
            case 3:
                return Serializer.WordIDClass.NUMBER;
            case 4:
                return Serializer.WordIDClass.MODIFIER;
            case 5:
                return Serializer.WordIDClass.ADJECTIVE;
            case 6:
                return Serializer.WordIDClass.PRONOUN;
            case 7:
                return Serializer.WordIDClass.CONDITION;
        }
    }

    public static Integer getWordIdNumber ( Integer wordId ) {
        return ( wordId / 10 ) % 1000;
    }

    public static Integer getWordIdOption ( Integer wordId ) {
        return ( wordId % 10 );
    }

    public static Serializer.VerbEndings getVerbEnding (Integer wordId ) {
        if ( getWordIdClass( wordId ) == Serializer.WordIDClass.VERB ) {
            switch ( getWordIdOption( wordId ) ) {
                default:
                case 0:
                    return Serializer.VerbEndings.NONE;
                case 1:
                    return Serializer.VerbEndings.ING;
                case 2:
                    return Serializer.VerbEndings.ED;
                case 3:
                    return Serializer.VerbEndings.S;
            }
        }
        return Serializer.VerbEndings.NONE;
    }

    public static Integer removeOptions(Integer in) {
        return in - (in % 10);
    }

    public static boolean equal(Integer a, Integer b) {
        // remove options for both
        return removeOptions(a).equals(removeOptions(b));
    }

    public static int compare(Integer a, Integer b) {
        return removeOptions(a).compareTo(removeOptions(b));
    }
}
