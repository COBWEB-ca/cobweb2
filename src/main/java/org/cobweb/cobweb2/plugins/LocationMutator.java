package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.LocationDirection;

public interface LocationMutator {

    public LocationDirection getNewLocation(Agent agent, LocationDirection from, LocationDirection originalTo);
}
