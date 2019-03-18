import xml.etree.ElementTree as et
et.register_namespace('', 'http://cobweb.ca/schema/cobweb2/population')
from shutil import copyfile

class subReactor:
    def __init__(self, typeNumberA, typeNumberB, xmlA, xmlB, reactRule):
        """
        A represents sender. B represents receiver.
        """
        self.typeNumberA = typeNumberA
        self.typeNumberB = typeNumberB
        self.xmlA = xmlA
        self.xmlATemp = 'temp_' + xmlA
        copyfile(self.xmlA, self.xmlATemp)
        self.xmlB = xmlB
        self.xmlBTemp = 'temp_' + xmlB
        copyfile(self.xmlB, self.xmlBTemp)
        self.reactRule = reactRule
        # Key: (Type a, Type b) Value: react factor

    def getAgentsEnergy(self, xmlPath):
        """
        Input: the path of the xml file
        Output: a dictionary whose key is the agent type, value is the total energy of the corresponding agent type.
        """
        totalEnergy = {}
        tree = et.parse(xmlPath)
        root = tree.getroot()
        for child in root:
            agentType = int(child.attrib["type"])
            energy = int(child[1].text) # gives the content of energy
            totalEnergy[agentType] = totalEnergy.get(agentType, 0) + energy
        return totalEnergy

    def compare(self, previousTotalEnergy, currentTotalEnergy):
        """
        Return the energy change between previous state and current state
        """
        difference = {}
        agentTypes = len(currentTotalEnergy)
        assert agentTypes == len(previousTotalEnergy)
        for i in range(1, agentTypes+1):
            difference[i] = currentTotalEnergy[i] - previousTotalEnergy[i]
        return difference

    def foo(self):
        """
        Executed after we run the program using xmlA, and saved new data to xmlATemp
        """
        previousTotalEnergy = self.getAgentsEnergy(self.xmlA)
        currentTotalEnergy = self.getAgentsEnergy(self.xmlATemp)
        return self.react(self.compare(previousTotalEnergy, currentTotalEnergy))

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
    a = subReactor(2, 2, "2d.1.xml", "2d.xml", {})
    reactResult = {2: 5, 4: -1}
    a.apply(reactResult)