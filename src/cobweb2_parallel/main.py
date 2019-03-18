from subReactor import subReactor
from subProcess import subProcess
from masterReactor import masterReactor

def main():
    reactRule1 = {(1,  1): -1, (2, 1): -2}
    reactRule2 = {(1, 1): 4, (1, 2): 3}
    subReactor1 = subReactor(2, 1, "first.xml", "second.xml", reactRule1)
    subReactor2 = subReactor(1, 2, "second.xml", "first.xml", reactRule2)

    subProcess1 = subProcess("firstEnvironment.xml", [subReactor1])
    subProcess2 = subProcess("secondEnvironment.xml", [subReactor2])

    master = masterReactor([subProcess1, subProcess2])
    master.run(3, 1)



if __name__ == "__main__":
    main()