import os
import xml.etree.ElementTree as et
import subProcess

class masterReactor:
    def __init__(self, subProcesses):
        self.subProcesses = subProcesses


    def run(self, stopAfter, timeInterval):
        for _ in range(stopAfter):
            energyChange = []
            for process in self.subProcesses:
                process.run(timeInterval)
                for subreactor in process.subReactors:
                    energyChange.append(subreactor.getEnergyChange())
            for i in range(len(self.subProcesses)):
                for subreactor in self.subProcesses[i].subReactors:
                    subreactor.wrappedApply(energyChange[i])