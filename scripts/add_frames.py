#!/usr/bin/env python3

import sys, os, json
import pprint
import readline

def template_to_string(tokenMappings, template):
    tokenStreamArray = []
    for i in template:
        if i == -21:
            tokenStreamArray.append("X")
        else:
            tokenStreamArray.append(tokenMappings[str(i)]["stream"])
    return " ".join(tokenStreamArray)

class InverseLookup:

    def __init__(self, tokens):
        self.inverseTokens = {}
        for token in tokens:
            self.inverseTokens[tokens[token]["stream"]] = tokens[token]["id"]
            for alias in tokens[token]["aliases"]:
                self.inverseTokens[alias] = tokens[token]["id"]

        self.options = sorted(self.inverseTokens.keys())

    def lookup(self, word):
        if word in self.inverseTokens:
            return self.inverseTokens[word]
        else:
            return None

    def complete(self, text, state):
        if state == 0:
            if text:
                self.matches = [
                    s for s in self.options if text in s
                ]
            else:
                self.matches = self.options[:]
        try:
            return self.matches[state]
        except IndexError:
            return None

def getNextId(frameMappings):
    return sorted([int(x) for x in frameMappings.keys()])[-1] + 1

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
                inverseLookup = InverseLookup(tokenMappings)
                readline.set_completer(inverseLookup.complete)
                readline.parse_and_bind('tab: complete')
                flag = True
                while flag:
                    # get input
                    templates = input("Enter in stream(s) (||) ('quit' to quit): ")
                    if templates == "quit":
                        break;
                    else:
                        templates = templates.split("||")
                    newTemplate = {
                        "id": getNextId(frameMappings["generic"]),
                        "aliases": [],
                        "template": []
                    }
                    first = True
                    for template in templates:
                        print("ENTERED TEMPLATE: {}".format(template))
                        template = template.split()
                        template = [-21 if token == "X" else inverseLookup.lookup(token) for token in template]
                        if None in template:
                            print("Error in template! Token not found")
                        else:
                            print(template)
                            if first:
                                newTemplate["template"] = template
                                first = False
                            else:
                                newTemplate["aliases"].append(template)
                    print("New Template Complete")
                    print(newTemplate)
                    frameMappings["generic"][newTemplate["id"]] = newTemplate
                # write back out
                with open(sys.argv[2], "w") as mappingFileOut:
                    json.dump(mappings, mappingFileOut, indent=2)

            else:
                print("Error. Invalid option {}. Pleas use [s (show) | i (insert)]".format(sys.argv[1]))

if __name__ == "__main__":
    main()
