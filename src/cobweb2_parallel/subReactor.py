import xml.etree.ElementTree as et
et.register_namespace('', 'http://cobweb.ca/schema/cobweb2/population')

class subReactor:
    def __init__(self, typeNumberA, typeNumberB, xmlA, xmlB, reactRule):
        """
        A represents sender. B represents receiver.
        """
        self.typeNumberA = typeNumberA
        self.typeNumberB = typeNumberB
        self.xmlA = xmlA
        self.xmlB = xmlB
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
        for child in root:
            agentType = int(child.attrib["type"])
            energy = int(child[1].text) # gives the content of energy
            newEnergy = energy + reactResult[agentType]
            child[1].text = str(newEnergy)
        tree.write(self.xmlB)
    
if __name__ == "__main__":
    a = subReactor(2, 2, "", "2d.xml", {})
    reactResult = {2: 5, 4: -1}
    a.apply(reactResult)