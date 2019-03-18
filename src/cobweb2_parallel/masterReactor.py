import os
import xml.etree.ElementTree as et
import subProcess

class masterReactor:
    def __init__(self, subProcesses):
        self.subProcesses = subProcesses

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

    def run(self):
        energyChange = []
        for process in self.subProcesses:
            process.run()
            for subreactor in process.subReactors:
                energyChange.append(subreactor.getEnergyChange())
        for i in range(len(self.subProcesses)):
            for subreactor in self.subProcesses[i].subReactors:
                subreactor.wrappedApply(energyChange[i])



def main(xml_configs):
    pass
    # numOfCobweb = len(xml_configs)
    # for i in range(numOfCobweb):
    #     cur_xml = xml_configs[i]
    #     # -save {temp_xml} means the name of the xml after one tick of execution
    #     temp_xml = "temp" + cur_xml
    #     os.system(f"java -jar {jarfile} -autorun 1 -hide --save-pop {temp_xml} --load-pop {temp_xml2} -open {cur_xml}")