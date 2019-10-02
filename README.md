# Datasheet Text Processor
Project that is in charge of:

1. Creating project from text files.
2. Processing the document project sentences via:
    1. Comment classification,
    2. Word Id serialization,
    3. Tokenization,
    4. Normalization and semantic grouping, (WIP)
    5. Semantic expression generation, (TODO)
    6. and Intermediate Representation (IR) generation. (TODO)
    
# How to use
This project either takes a `.project` file of a document or a text file as input.

The input text file is either one generate by `cas2text` or a line-delimited sentences plain-text file.

Once again, the main way to run the code is via the `./run.sh` script.

## Order of usage
In order to properly use this code please follow the following sequence:
### 1) Create a project
You can create a project from a text file (from `cas2text` or line-delimited plain text) using the `-n` option.

Example:
```text
./run.sh -nf uart_spec.txt
```

### 2) Processing
This step assumes that a project is already created from step 1).

Note: *You can run each of the steps in the proper order with one command by putting all of the command line switches in one command.*
#### 2.1) Classification
This step classifies the sentences in the project into either `COMMENTS` or `NONCOMMENTS`.
#### 2.2) Word Id Serialization
This step serializes the sentences into a word id stream. This step assumes classified sentences.
#### 2.3) Tokenization
Groups wordids into tokens. The result is a token stream. This step assumes serialized sentences.
#### 2.x) TODO: Next steps

## Command Line Options
### -h (Help message)
From `./run.sh -h`:
```text
Usage: ./run.sh [-w -m=MAPPINGFILE [--add-new]] [[-t] -q=MAPPINGFILE
           [--compile-tokens]] [[-p] | [--show-percentage] | [--show-matches] |
           [--show-comments] | [--show-non-comments] | [--show-wordids] |
           [--show-tokens]] [-chn] [--text] -f=INPUTFILE [-s=SIGNALFILE]
  -c, --classify            classify the sentences
  -f, --file=INPUTFILE      the input cas/project/text file
  -h, --help                display this help message
  -n, --new-project         create a new project
  -s, --signal-names=SIGNALFILE
                            a list of the signal names in the document
      --text                input file only has raw text, use implied sentence
                              ids
Word id serialization options
      --add-new             add new mappings from unmapped words
  -m, --word-id-mappings=MAPPINGFILE
                            mapping file (JSON)
  -w, --word-id             translate to word ids
Tokenizer options      --compile-tokens      input token mapping file contains raw text rather
                              than wordid streams
  -q, --token-mappings=MAPPINGFILE
                            mapping file (JSON)
  -t, --tokenize            tokenize sentences
Debugging options for classification
  -p, --print               print the sentences from a project
      --show-comments       print out only the comments
      --show-matches        print out the matches for each sentences
      --show-non-comments   print out only the non-comments
      --show-percentage     print out the percentage of the file that is
                              non-comments
      --show-tokens         print out the token streams
      --show-wordids        print out the word id streams
```
### Top Level Options
#### -c, --classify
Tells the code to classify the sentences.
#### -f, --file
The input project or text file.
#### -n, --new-project
Tell the code to interpret the input file as a text file to be turned into a new project. The new project file will be `INPUTFILE.project`.
#### -s, --signal-names
This tells the code to add the signal names in `SIGNALFILE` to the project. This input file follows the following line-delimited format:

```text
<Signal Name>::<ACRONYM 1>,<ACRONYM 2>,...
```
#### --text
Tells the new project code to interpret the input text file as a line-delimited version (not from cas2text)
### Word Id Options
#### --add-new
When a word is found that has no existing mapping the code will interactively ask the user to classify the word.
The categories are given to the user to input. If a verb is chosen then the stem and alias will be asked of the user.
The new mappings are exported to the given mapping file from the `-m` option.
#### -m, --word-id-mappings
Mapping json file that specifies verb aliases and base mappings.
#### -w, --word-id
Tell the code to serialize the sentences
### Tokenizser Options
#### -t, --tokenize
Tell the code to tokenize the word id streams. When this option is given, the `-w` and `-m` options are needed as well in order to interpret the wordids.
#### -q, --token-mappings
Mappings file for the token and token aliases. (JSON)
#### --compile-tokens
Tells the code that the token mapping file uses plain text sentences to represent the tokens. Tells the code to also serialize the token mappings and use them instead.

Exports the serialized token mappings to `MAPPINGFILE_compiled.json`
### Debugging options
#### -p, --print
Prints out the current state of the project
#### --show-comments
Prints out only the comments from the project. This assumes that the project has been classified.
#### --show-matches
Shows what patterns were matched during classification
#### --show-non-comments
Prints out only the non-comments
#### --show-percentage
Prints out the percentage of the project of non-comments:comments
#### --show-wordids
Shows the wordid streams of the sentences
#### --show-tokens
Shown the token streams.

## Examples
Input file from cas2text: `uart_spec.txt`.

### Creating the project
Command: `./run.sh -nf uart_spec.txt`

#### Raw text input
Command: `./run.sh -nf uart_spec.txt --text`

A file `uart_spec.project` is creates.
### Classification
Command: `./run.sh -f uart_spec.project -c`

### Word Id Serialization
Mappings File: `Mappings.json`

Command: `./run.sh -f uart_spec.project -wm Mappings.json`
#### Adding new mappings
Command: `./run.sh -f uart_spec.project -wm Mappings.json --add-new`

### Tokenization
Mappings File (Raw Sentences): `Tokens_mappings.json`
Mappings File (Compiled): `Tokens_mappings_compiled.json`

Command: `./run.sh -f uart_spec.project -wm Mappings.json -tq Tokens_mappings_compiled.json`

#### Compiling token mappings
Command: `./run.sh -f uart_spec.project -wm Mappings.json -tq Tokens_mappings.json --compile-tokens`

### Combining options
This command does everything at once (in the proper order internally)

Command: `./run.sh -nf uart_spec.txt -cwm Mappings.json -tw Tokens_mappings_compiled.json`
