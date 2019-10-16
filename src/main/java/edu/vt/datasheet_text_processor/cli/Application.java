package edu.vt.datasheet_text_processor.cli;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Command(name = "dtp")
public class Application {
    @Option(names = {"-f", "--file"}, required = true, paramLabel = "INPUTFILE", description = "the input cas/project/text file")
    public File inputFile;

    // new project flags
    @Option(names = {"-n", "--new-project"}, description = "create a new project")
    public boolean newProject;
    @Option(names = {"--text"}, description = "input file only has raw text, use implied sentence ids")
    public boolean text;

    @Option(names = {"-s", "--signal-names"}, description = "a list of the signal names in the document", paramLabel = "SIGNALFILE")
    public File signalNames;
    @Option(names = {"-m", "--mappings"}, description = "mapping file (JSON)", required = true, paramLabel = "MAPPINGFILE")
    public File mappingFile;
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

    @ArgGroup(exclusive = false, heading = "Tokenizer options")
    public TokenOptions tokenOptions;
    public static class TokenOptions {
        @Option(names = {"-t", "--tokenize"}, description = "tokenize sentences")
        public boolean doToken;
        @Option(names = {"-N", "--normalize"}, description = "normalize the tokens", paramLabel = "NORMALIZATIONFILE")
        public boolean normalize;
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
        @Option(names = {"--show-comments"}, description = "print out only the comments")
        public boolean doShowComments;
        @Option(names = {"--show-non-comments"}, description = "print out only the non-comments")
        public boolean doShowNonComments;
        @Option(names = {"--show-wordids"}, description = "print out the word id streams")
        public boolean doShowWordIds;
        @Option(names = {"--show-tokens"}, description = "print out the token streams")
        public boolean doShowTokens;
        @Option(names = {"--show-token-text"}, description = "print out the tokenized sentence in text form")
        public boolean doShowTokenText;
        @Option(names = {"--show-acronyms"}, description = "print out the generated acronyms")
        public boolean doShowAcronyms;
    }

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;
}
