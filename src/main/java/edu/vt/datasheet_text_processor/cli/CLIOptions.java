package edu.vt.datasheet_text_processor.cli;

import picocli.CommandLine.Option;

import java.io.File;

public class CLIOptions {
    @Option(names={"-f", "--file"}, required=true, paramLabel="INPUTFILE", description="the input cas file")
    public File inputFile;

    @Option(names={"-n", "--new-project"}, description="create a new project")
    public boolean newProject;

    // processing steps
    // print
    // classification
    // word id
    // tokenization
    @Option(names={"-p", "--print"}, description="print the sentences from a project")
    public boolean doPrint;
    @Option(names={"-c", "--classify"}, description="classify the sentences")
    public boolean doClassify;
    @Option(names={"-w", "--word-id"}, description="translate to word ids")
    public boolean doWordId;
    @Option(names={"-t", "--tokenize"}, description="tokenize sentences")
    public boolean doToken;

    @Option(names={"-h", "--help"}, usageHelp=true, description="display this help message")
    private boolean usageHelpRequested;
}
