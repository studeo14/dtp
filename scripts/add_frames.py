#!/usr/bin/env python3

import sys, os, json
import pprint

def template_to_string(tokenMappings, template):
    tokenStreamArray = []
    for i in template:
        if i == -21:
            tokenStreamArray.append("X")
        else:
            tokenStreamArray.append(tokenMappings[str(i)]["stream"])
    return " ".join(tokenStreamArray)

def main():
    if len(sys.argv) != 3:
        print("Error. Need [s (show) | i (insert)] [<MAPPINGFILE>]")
    else:
        with open(sys.argv[2]) as mappingFileIn:
            mappings = json.load(mappingFileIn)
            tokenMappings = mappings["tokenMapping"]
            frameMappings = mappings["frameMapping"]
            if sys.argv[1] == "s":
                for mapping in frameMappings["generic"]:
                    template = frameMappings["generic"][mapping]
                    templateString = template_to_string(tokenMappings, template["template"])
                    print("ID: {}, Template: {}".format(mapping, templateString))
                    if len(template["aliases"]) > 0:
                        for alias in template["aliases"]:
                            aliasString = template_to_string(tokenMappings, alias)
                            print("\t{}".format(aliasString))
                for mapping in frameMappings["bitAccess"]:
                    template = frameMappings["bitAccess"][mapping]
                    templateString = template_to_string(tokenMappings, template["template"])
                    print("ID: {}, Template: {}".format(mapping, templateString))
                    if len(template["aliases"]) > 0:
                        for alias in template["aliases"]:
                            aliasString = template_to_string(tokenMappings, alias)
                            print("\t{}".format(aliasString))
            elif sys.argv[1] == "i":
                pass
            else:
                print("Error. Invalid option {}. Pleas use [s (show) | i (insert)]".format(sys.argv[1]))

if __name__ == "__main__":
    main()
