import os
JARFILE = "cobweb2_load_save_pop.jar"
class subProcess:
    def __init__(self, environmentXML, subReactors):
        self.subReactors = subReactors
        self.environmentXML = environmentXML
        self.xmlA = subReactors[0].xmlA
        self.xmlAtemp = subReactors[0].xmlAtemp

    def run(self, timeInterval):
        os.system(f"java -jar {JARFILE} -autorun {timeInterval} -hide --save-pop {self.xmlAtemp} --load-pop {self.xmlA} -open {self.environmentXML}")