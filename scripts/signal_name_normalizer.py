#!/usr/bin/env python3

import os, sys, re

def check_for_alias(line, aliases):
    rets = []
    for alias in aliases:
        rets.append([m.start() for m in re.finditer(alias, line)])
    # flatten
    return [item for sublist in rets for item in sublist]

def check_for_signal(line, sig):
    rets = [m.start() for m in re.finditer(sig, line)]
    return rets

def main():
    if len(sys.argv) != 3:
        print("Error: Expecting 2 arguments: <text file> <signals file>")
    else:
        with open(sys.argv[1]) as textFile, open(sys.argv[2]) as sigFile:
            # parse sigs
            sigs = {}
            for line in sigFile:
                line = line.strip()
                if line.startswith("#") or len(line) == 0:
                    pass
                else:
                    # split into ACRONYMS and NAMES
                    lr = line.split("::")
                    # aliases
                    names = lr[0].split("/")
                    # get longest name as the 'true' name
                    names.sort(key=len, reverse=True)
                    name = names[0]
                    aliases = names[1:]
                    sigs[name] = aliases
            textFile = textFile.readlines()
            # process sigs
            with open(sys.argv[1], "w") as newText:
                for line in textFile:
                    if line.startswith("#") or len(line) == 0:
                        pass
                    else:
                        # search for signals
                        for sig in sigs:
                            sigInLine = sig in line
                            if sigInLine:
                                pass
                            else:
                                for alias in sigs[sig]:
                                    if alias in line:
                                        print("Replacing '{}' in '{}'".format(alias, line.strip()))
                                        line = line.replace(alias, sig)
                    newText.write(line)
                newText.flush()

if __name__ == "__main__":
    main()

