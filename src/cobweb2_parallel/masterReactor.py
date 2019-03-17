import os
import xml.etree.ElementTree as et

class masterReactor:
    def __init__(self, subReactors):
        self.subReactors = subReactors

    def parse(self, xmlPath):
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

