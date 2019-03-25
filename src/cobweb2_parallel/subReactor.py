import xml.etree.ElementTree as et
et.register_namespace('', 'http://cobweb.ca/schema/cobweb2/population')
from shutil import copyfile
from math import ceil

class subReactor:
    def __init__(self, typeNumberA, typeNumberB, xmlA, xmlB, reactRule):
        """
        A represents sender. B represents receiver.
        """
        self.typeNumberA = typeNumberA
        self.typeNumberB = typeNumberB
        self.xmlA = xmlA
        self.xmlAtemp = 'temp_' + xmlA
        copyfile(self.xmlA, self.xmlAtemp)
        self.xmlB = xmlB
        self.xmlBtemp = 'temp_' + xmlB
        copyfile(self.xmlB, self.xmlBtemp)
        self.reactRule = reactRule
        # Key: (Type a, Type b) Value: react factor

    def react(self, comparisonDifference):
        reactResult = {}
        for i in range(1, self.typeNumberA+1):
            for j in range(1, self.typeNumberB+1):
                reactResult[j] = reactResult.get(j, 0) + comparisonDifference[i] * self.reactRule[(i, j)]
        return reactResult
        
    def apply(self, reactResult):
        tree = et.parse(self.xmlB)
        root = tree.getroot()
        totalNum = {}
        for child in root:
            agentType = int(child.attrib["type"])
            totalNum[agentType] = totalNum.get(agentType, 0) + 1
        for child in root:
            agentType = int(child.attrib["type"])
            energy = int(child[1].text) # gives the content of energy
            newEnergy = int(ceil(energy + reactResult[agentType]/totalNum[agentType]))
            child[1].text = str(newEnergy)
        tree.write(self.xmlB)

    def wrappedApply(self, comparisonDifference):
        reactResult = self.react(comparisonDifference)
        self.apply(reactResult)