#!/usr/bin/env python3

import json
import sys
import os

def main():
    if len(sys.argv) != 3:
        print("Error. Need file names. <INPUTFILE> <DESTINATION>")
    else:
        with open(sys.argv[1]) as cFile, open(sys.argv[2]) as dFile:
            compiledMappings = json.load(cFile)
            nonCompiledMappings = json.load(dFile)
            nonCompiledMappings['wordIdMapping'] = compiledMappings['wordIdMapping']
            with open(sys.argv[2], 'w') as dFileO:
                json.dump(nonCompiledMappings, dFileO, indent=2)

if __name__ == '__main__':
    main()
