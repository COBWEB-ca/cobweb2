##How gravity works

Algorithm:
- Perform "floor plan" algorithm to determine each contiguous set 
of agents, compiling all the locations that make up that set of agents,
then determine the epicenter of the agents, which is the point mass used
to determine whether an agent will be attracted to it.
- For each of the agents, when it is its turn to move, it will 
find the strongest gravitational force and move towards it.