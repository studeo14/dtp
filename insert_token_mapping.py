#!/usr/bin/env python3

import json
import sys
import os

def main():
    if len(sys.argv) != 3:
        print("Error. Need file names. <INPUTFILE> <DESTINATION>")
    else:
        with open(sys.argv[1]) as tFile, open(sys.argv[2]) as dFile:
            tokenMappings = json.load(tFile)
            destination = json.load(dFile)
            destination['tokenMapping'] = tokenMappings
            with open(sys.argv[2], 'w') as dFileO:
                json.dump(destination, dFileO, indent=2)

if __name__ == '__main__':
    main()
