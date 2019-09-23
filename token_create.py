#!/usr/bin/env python3

import json
import sys
import os

def main():
    if len(sys.argv) != 2:
        print("Error. Need a file name.")
    else:
        with open(sys.argv[1]) as tokenFile:
            index = 0
            tokens = {}
            tokenStrings = set()
            # read each
            # ignore if start with #
            for line in tokenFile.readlines():
                line = line.strip()
                if line.startswith("#") or len(line) == 0:
                    pass
                else:
                    if line in tokenStrings:
                        pass
                    else:
                        tokenStrings.add(line)
                        tokens[index] = {
                            "id": index,
                            "stream": line,
                            "aliases": []
                        }
                        index+=1;
            # done
            # export
            baseName = os.path.splitext(sys.argv[1])[0]
            exportFileName = baseName + "_mappings.json"
            json.dump(tokens, open(exportFileName, "w+"), sort_keys=True, indent=4)

if __name__ == "__main__":
    main()
