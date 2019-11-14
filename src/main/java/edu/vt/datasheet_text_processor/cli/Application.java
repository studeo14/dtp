package edu.vt.datasheet_text_processor.cli;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Command(name = "dtp")
public class Application {
    @Option(names = {"-f", "--file"}, required = true, paramLabel = "INPUTFILE", description = "the input cas/project/text file")
    public File inputFile;

    @ArgGroup(exclusive = true, heading="In Point options%n")
    public InPointOptions inPointOptions;

    // options that tell the program if it is creating a project or processing one
    public static class InPointOptions {
        @Option(names = {"-n", "--new-project"}, description = "create a new project", required = true)
        public boolean newProject;
        @Option(names = {"-m", "--mappings"}, description = "mapping file (JSON)", required = true, paramLabel = "MAPPINGFILE")
        public File mappingFile;
    }

    // new project flags
    @Option(names = {"--text"}, description = "input file only has raw text, use implied sentence ids")
    public boolean text;

    @Option(names = {"-s", "--signal-names"}, description = "a list of the signal names in the document", paramLabel = "SIGNALFILE")
    public File signalNames;
    @Option(names = {"--compile-tokens"}, description = "indicates that the mapping file is a raw string token and needs to be compiled", required = false)
    public boolean compileTokens;

    // processing steps
    // print
    // classification
    // word id
    // tokenization
    @Option(names = {"-c", "--classify"}, description = "classify the sentences")
    public boolean doClassify;

    // word id options
    @ArgGroup(exclusive = false, heading = "Word id serialization options%n")
    public WordIDOptions wordIDOptions;

    public static class WordIDOptions {
        @Option(names = {"-w", "--word-id"}, description = "translate to word ids", required = true)
        public boolean doWordId;
        @Option(names = {"--add-new"}, description = "add new mappings from unmapped words", required = false)
        public boolean addNew;
    }

    @ArgGroup(exclusive = false, heading = "Tokenizer options%n")
    public TokenOptions tokenOptions;
    public static class TokenOptions {
        @Option(names = {"-t", "--tokenize"}, description = "tokenize sentences")
        public boolean doToken;
        @Option(names = {"-N", "--normalize"}, description = "normalize the tokens", paramLabel = "NORMALIZATIONFILE")
        public boolean normalize;
    }

    @ArgGroup(exclusive = false, heading = "Semantic Expression Options%n")
    public SemanticExpressionOptions semanticExpressionOptions;
    public static class SemanticExpressionOptions {
        @Option(names={"-e", "--semantic-expressions"}, description = "find the semantic expressions in the token streams")
        public boolean doSemanticExpression;
    }

    @ArgGroup(exclusive = false, heading = "IR Options%n")
    public IROptions irOptions;
    public static class IROptions {
        @Option(names={"-i", "--get-ir"}, description = "find the IR of the semantic expressiosn")
        public boolean doGetIr;
    }

    // debugging options
    @ArgGroup(exclusive = true, heading = "Debugging options for classification%n")
    public DebugOptions debugOptions;

    public static class DebugOptions {
        @Option(names = {"-p", "--print"}, description = "print the sentences from a project")
        public boolean doPrint;
        @Option(names = {"--show-percentage"}, description = "print out the percentage of the file that is non-comments")
        public boolean doPercentage;
        @Option(names = {"--show-matches"}, description = "print out the matches for each sentences")
        public boolean doShowMatches;
        @Option(names = {"--show-most-used"}, description = "print out the matches sorted by most commonly matched")
        public boolean doShowMostUsed;
        @Option(names = {"--show-comments"}, description = "print out only the comments")
        public boolean doShowComments;
        @Option(names = {"--show-non-comments"}, description = "print out only the non-comments")
        public boolean doShowNonComments;
        @Option(names = {"--show-non-comments-matches"}, description = "print out only the non-comments with their pattern matches")
        public boolean doShowNonCommentsMatches;
        @Option(names = {"--show-wordids"}, description = "print out the word id streams")
        public boolean doShowWordIds;
        @Option(names = {"--show-tokens"}, description = "print out the token streams")
        public boolean doShowTokens;
        @Option(names = {"--show-token-text"}, description = "print out the tokenized sentence in text form")
        public boolean doShowTokenText;
        @Option(names = {"--show-acronyms"}, description = "print out the generated acronyms")
        public boolean doShowAcronyms;
        @Option(names = {"--show-fst"}, description = "print out the frame search tree")
        public boolean doShowFrameSearchTree;
        @Option(names = {"--show-tst"}, description = "print out the token search tree")
        public boolean doShowTokenSearchTree;
        @Option(names = {"--show-semexpr"}, description = "print out the semantic expressions")
        public boolean doShowSemanticExpressions;
        @Option(names = {"--show-ir"}, description = "print out the ir")
        public boolean doShowIR;
    }

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;
}
