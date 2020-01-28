package edu.vt.datasheet_text_processor.wordid;

public class WordIdUtils {
    public static final int CLASS_NUM = 1000000;
    public static final int ID_NUMBER = CLASS_NUM/10;
    public static final int OPTIONS = 10;

    public static Serializer.WordIDClass getWordIdClass (Integer wordId ) {
        var classNum = ( wordId / CLASS_NUM );
        return classNumToClass( classNum );
    }

    public static Integer getBase(Integer wordId) {
        return wordId - (wordId%OPTIONS);
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
        return ( wordId / OPTIONS ) % ID_NUMBER;
    }

    public static Integer getWordIdOption ( Integer wordId ) {
        return ( wordId % OPTIONS );
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
                case 4:
                    return Serializer.VerbEndings.EN;
            }
        }
        return Serializer.VerbEndings.NONE;
    }

    public static boolean equal(Integer a, Integer b) {
        // remove options for both
        return getBase(a).equals(getBase(b));
    }

    public static int compare(Integer a, Integer b) {
        return getBase(a).compareTo(getBase(b));
    }
}
