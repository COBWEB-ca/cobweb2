package org.cobweb.cobweb2.plugins.gravity;

import javafx.util.Pair;
import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.*;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.cobweb2.plugins.EnvironmentMutator;
import org.cobweb.cobweb2.plugins.MoveMutator;

import java.util.ArrayList;

public class GravityMutator implements MoveMutator, EnvironmentMutator {

    public GravityMutator() {
    }

    private GravityParams params;
    private SimulationTimeSpace sim;

    private int[][] massArray; // Array holding the positions of all the point masses

    public void setParams(SimulationTimeSpace sim, GravityParams params) {
        this.sim = sim;
        this.params = params;

        int w = sim.getTopology().width;
        int h = sim.getTopology().height;

        massArray = new int[w][h];

    }

    private int calculateContiguousArea(int a, int b, boolean[][] checked, ComplexEnvironment env, ArrayList<Location> result) {
        Location thisLoc = new Location(a, b);
        if (!checked[a][b] && env.hasAgent(thisLoc)) {
            int total = params.gravityParams[env.getAgent(thisLoc).getType()].mass.getValue();
            checked[a][b] = true;
            for (int i = 0; i < total; i++) {
                result.add(thisLoc);
            }

            Location northLoc = env.topology.getAdjacent(thisLoc, Topology.NORTH);
            Location eastLoc = env.topology.getAdjacent(thisLoc, Topology.EAST);
            Location southLoc = env.topology.getAdjacent(thisLoc, Topology.SOUTH);
            Location westLoc = env.topology.getAdjacent(thisLoc, Topology.WEST);

            if (northLoc != null && !checked[northLoc.x][northLoc.y] && env.hasAgent(northLoc)) {
                total += calculateContiguousArea(northLoc.x, northLoc.y, checked, env, result);
            }
            if (eastLoc != null && !checked[eastLoc.x][eastLoc.y] && env.hasAgent(eastLoc)) {
                total += calculateContiguousArea(eastLoc.x, eastLoc.y, checked, env, result);
            }
            if (southLoc != null && !checked[southLoc.x][southLoc.y] && env.hasAgent(southLoc)) {
                total += calculateContiguousArea(southLoc.x, southLoc.y, checked, env, result);
            }
            if (westLoc != null && !checked[westLoc.x][westLoc.y] && env.hasAgent(westLoc)) {
                total += calculateContiguousArea(westLoc.x, westLoc.y, checked, env, result);
            }
            return total;
        }
        return 0;
    }

    private Location centerOfMass(ArrayList<Location> locations) {
        int centerX = 0;
        int centerY = 0;

        for (Location loc: locations) {
            centerX += loc.x;
            centerY += loc.y;
        }

        return new Location((int) (centerX / locations.size()), (int) (centerY / locations.size()));
    }

    private void calculateMasses() {
        ComplexEnvironment env = ((Simulation) sim).theEnvironment;

        boolean[][] checked = new boolean[env.data.width][env.data.height];

        for (int i = 0; i < env.data.width; i++) {
            for (int j = 0; j < env.data.height; j++) {
                if (!checked[i][j]) {
                    ArrayList<Location> locations = new ArrayList<>(); // The locations where this mass occurs, weighted for each agent's mass
                    // i.e. if an agent has mass 4, its location will appear 4 times in <locations>
                    int mass = calculateContiguousArea(i, j, checked, env, locations);
                    if (mass == 0) {
                        massArray[i][j] = 0;
                    } else {
                        Location center = centerOfMass(locations);
                        for (Location loc : locations) {
                            massArray[loc.x][loc.y] = 0;
                        }
                        massArray[center.x][center.y] = mass;
                    }
                }
            }
        }
    }

    private Pair<Double, Double> calculateGravityProbability(Agent agent) {
        double forceX = 0; // + right, - left
        double forceY = 0; // + down, - up

        Location agentLoc = agent.getPosition();
        for (int a = 0; a < sim.getTopology().width; a++) {
            for (int b = 0; b < sim.getTopology().height; b++) {
                Location ab = new Location(a, b);
                double dis = sim.getTopology().getDistanceSquared(agentLoc, ab);
                if (!agentLoc.equals(ab)) {
                    double force = (params.gravitationalConstant * massArray[a][b] * params.gravityParams[agent.getType()].mass.getValue()) / dis;
                    Direction toPoint = sim.getTopology().getDirectionBetween8way(agentLoc, ab);
                    forceX += (toPoint.x / Math.sqrt(toPoint.x * toPoint.x + toPoint.y * toPoint.y)) * force;
                    forceY += (toPoint.y / Math.sqrt(toPoint.x * toPoint.x + toPoint.y * toPoint.y)) * force;
                }
            }
        }

        return new Pair<>(forceX, forceY);
    }

    @Override
    public boolean overrideMove(Agent agent) {

        if (params.gravityEnabled) {
            Pair<Double, Double> res = calculateGravityProbability(agent);
            double forceX = res.getKey();
            double forceY = res.getValue();

            if (forceX == 0 && forceY == 0) {
                return false;
            }

            Direction to;
            if (Math.abs(forceX) > Math.abs(forceY)) {
                to = forceX > 0 ? Topology.EAST : Topology.WEST;
            } else {
                to = forceY > 0 ? Topology.SOUTH : Topology.NORTH;
            }
            ComplexAgent ag = (ComplexAgent) agent;
            if (sim.getRandom().nextFloat() < Math.max(Math.abs(forceX), Math.abs(forceY))) {
                Direction agentDir = agent.getPosition().direction;
                if (sim.getTopology().getRotationBetween(agentDir, to).equals(Rotation.None)) {
                    ag.step();
                } else if (sim.getTopology().getRotationBetween(agentDir, to).equals(Rotation.Right)) {
                    ag.turnRight();
                } else if (sim.getTopology().getRotationBetween(agentDir, to).equals(Rotation.Left)) {
                    ag.turnLeft();
                } else if (Math.abs(forceX) > Math.abs(forceY)) {
                    if (forceY > 0 && agentDir.equals(Topology.EAST)) {
                        ag.turnRight();
                    } else if (forceY > 0 && agentDir.equals(Topology.WEST)) {
                        ag.turnLeft();
                    } else if (forceY < 0 && agentDir.equals(Topology.EAST)) {
                        ag.turnLeft();
                    } else if (forceY < 0 && agentDir.equals(Topology.WEST)) {
                            ag.turnRight();
                    }
                } else {
                    if (forceX > 0 && agentDir.equals(Topology.NORTH)) {
                        ag.turnRight();
                    } else if (forceX > 0 && agentDir.equals(Topology.SOUTH)) {
                        ag.turnLeft();
                    } else if (forceX < 0 && agentDir.equals(Topology.NORTH)) {
                        ag.turnLeft();
                    } else if (forceX < 0 && agentDir.equals(Topology.SOUTH)) {
                        ag.turnRight();
                    }
                }
                return true;

            }
        }
        return false;
    }

    @Override
    public void loadNew() {
        calculateMasses(); // Create the initial array of point masses
    }

    @Override
    public void update() {
        calculateMasses();
    }

    @Override
    public <T extends AgentState> boolean acceptsState(Class<T> type, T value) {
        return false;
    }
}
