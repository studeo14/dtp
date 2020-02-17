#!/usr/bin/env python3

import sys, os, json

def main():
    if len(sys.argv) != 3:
        print("Error. Expected <Mapping File> <Token Text File>")
    else:
        with open(sys.argv[1]) as inFile, open(sys.argv[2], "w+") as outFile:
            mappings = json.load(inFile)
            tokenMappings = mappings["tokenMapping"]
            for mapping in tokenMappings:
                mId = tokenMappings[mapping]["id"]
                mAliases = tokenMappings[mapping]["aliases"]
                mStream = tokenMappings[mapping]["stream"]
                aliasS = "||".join(mAliases)
                if len(mAliases) > 0:
                    outFile.write("{}::{}\n".format(mStream, aliasS))
                else:
                    outFile.write("{}\n".format(mStream))

if __name__ == "__main__":
    main()
