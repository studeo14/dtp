package edu.vt.datasheet_text_processor.cli;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Command(name = "dtp")
public class Application {
    @Option(names = {"-m", "--mappings"}, description = "mapping file (JSON)", required = true, paramLabel = "MAPPINGFILE")
    public File mappingFile;

    // new project flags
    @Option(names = {"-v","--verbose"}, description = "display debugging info")
    public boolean verbose;

    @ArgGroup(exclusive = true, heading="In Point options%n")
    public InPointOptions inPointOptions;

    // options that tell the program if it is creating a project or processing one
    public static class InPointOptions {
        @Option(names = {"-f", "--file"}, required = true, paramLabel = "INPUTFILE", description = "the input cas/project/text file")
        public File inputFile;
        @Option(names = {"--compile-tokens"}, required = true, description = "indicates that the mapping file is a raw string token and needs to be compiled")
        public boolean compileTokens;
    }

    @Option(names = {"-s", "--signal-names"}, description = "a list of the signal names in the document", paramLabel = "SIGNALFILE")
    public File signalNames;

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
        @Option(names={"--reset-metrics"}, description = "reset metric counter in db")
        public boolean doResetMetrics;
    }

    // experimental optimizations ect (all off by default)
    @ArgGroup(exclusive = false, heading = "Experimental Options%n")
    public ExperimentalOptions experimentalOptions;
    public static class ExperimentalOptions {
        public enum ClassificationScheme {nosig, withsig, sigonly};
        @Option(names={"--classification-scheme"}, defaultValue = "nosig", description = "define the classification scheme to use. Valid Options: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}")
        public ClassificationScheme classificationScheme = ClassificationScheme.nosig;

        @Option(names="--prefer-shorter-tokens", description = "prefer shorter tokens when tokenizing")
        public boolean preferShorterTokens;
        @Option(names="--prefer-shorter-frames", description = "prefer shorter frames")
        public boolean preferShorterFrames;
        @Option(names={"--show-ir-counts"}, description = "show execution time")
        public boolean doShowIRCounts;
    }

    // debugging options
    @ArgGroup(exclusive = false, heading = "Debugging options for classification%n")
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
        @Option(names = {"--print-non-comments"}, description = "print out only the non-comments to the given file")
        public File doPrintNonComments;
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
        @Option(names = {"--show-irp"}, description = "print out the %% ir")
        public boolean doShowIRP;
        @Option(names={"--time"}, description = "show execution time")
        public boolean doShowTime;
        @Option(names={"--show-average-tokens"}, description = "show execution time")
        public boolean doShowAverageTokens;
        @Option(names={"--show-average-frames"}, description = "show execution time")
        public boolean doShowAverageFrames;
        @Option(names={"--show-metrics"}, description = "show stored metrics")
        public boolean doShowMetrics;
    }

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;
}
