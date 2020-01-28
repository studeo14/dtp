#!/usr/bin/env python3

import json, sys, os

def main():
    if len(sys.argv) != 2:
        print("Error. Need file name. <INPUTFILE>")
    else:
        with open(sys.argv[1]) as mappingsFile:
            mappings = json.load(mappingsFile)
            for mapping in mappings["wordIdMapping"]["baseMapping"]:
                current = mappings["wordIdMapping"]["baseMapping"][mapping]
                info = current%10000
                newValue = ((current//10000)*1000000) + info
                mappings["wordIdMapping"]["baseMapping"][mapping] = newValue
            with open(sys.argv[1], "w") as newFile:
                json.dump(mappings, newFile, indent=2)

if __name__ == "__main__":
    main()
