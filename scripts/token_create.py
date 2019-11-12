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
                    # split line into main + aliases
                    lr = line.split("::")
                    mainText = lr[0]
                    if len(lr) == 2:
                        aliases = lr[1].split("||")
                    elif len(lr) == 1:
                        aliases = []
                    if mainText in tokenStrings:
                        pass
                    elif any(alias in tokenStrings for alias in aliases):
                        pass
                    else:
                        tokenStrings.add(mainText)
                        for alias in aliases:
                            tokenStrings.add(alias)
                        tokens[index] = {
                            "id": index,
                            "stream": mainText,
                            "aliases": aliases
                        }
                        index+=1;
            # done
            # export
            baseName = os.path.splitext(sys.argv[1])[0]
            exportFileName = baseName + "_mappings.json"
            json.dump(tokens, open(exportFileName, "w+"), sort_keys=True, indent=4)

if __name__ == "__main__":
    main()
