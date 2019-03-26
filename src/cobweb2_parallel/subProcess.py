import os
import xml.etree.ElementTree as et
et.register_namespace('', 'http://cobweb.ca/schema/cobweb2/population')

JARFILE = "cobweb2_load_save_pop.jar"
class subProcess:
    def __init__(self, environmentXML, subReactors):
        self.subReactors = subReactors
        self.environmentXML = environmentXML
        self.xmlA = subReactors[0].xmlA
        self.xmlAtemp = subReactors[0].xmlAtemp

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
        for agentType in previousTotalEnergy:
            difference[agentType] = currentTotalEnergy.get(agentType, 0) - previousTotalEnergy[agentType]
        return difference

    def getEnergyChange(self):
        """
        Executed after we run the program using xmlA, and saved new data to xmlATemp
        """
        previousTotalEnergy = self.getAgentsEnergy(self.xmlA)
        currentTotalEnergy = self.getAgentsEnergy(self.xmlAtemp)
        return self.compare(previousTotalEnergy, currentTotalEnergy)

    def run(self, timeInterval, logIndex):
        logFileName = self.xmlA + str(logIndex) + ".xls"
        os.system(f"java -jar {JARFILE} -autorun {timeInterval} -hide --save-pop {self.xmlAtemp} --load-pop {self.xmlA} -open {self.environmentXML} -log {logFileName}")