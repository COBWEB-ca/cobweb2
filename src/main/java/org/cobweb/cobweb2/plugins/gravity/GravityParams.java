package org.cobweb.cobweb2.plugins.gravity;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerTypeParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;

import java.util.Arrays;

public class GravityParams implements PerTypeParam<GravityTypeParams> {

    /*
     * Whether gravity is activated
     */
    @ConfDisplayName("Enable gravity")
    @ConfXMLTag("gravityEnabled")
    public boolean gravityEnabled = false;

    /*
     * Gravitational constant - how strong gravity will be
     */
    @ConfDisplayName("Gravity Strength")
    @ConfXMLTag("gravityStrength")
    public float gravitationalConstant = 0.667f;

    @ConfXMLTag("GravityParams")
    @ConfList(indexName = "Gravity", startAtOne = true)
    public GravityTypeParams[] gravityParams = new GravityTypeParams[0];

    public GravityParams(AgentFoodCountable initialSize) {
        resize(initialSize);
    }

    @Override
    public void resize(AgentFoodCountable envParams) {
        GravityTypeParams[] n = Arrays.copyOf(gravityParams, envParams.getAgentTypes());

        for (int i = gravityParams.length; i < envParams.getAgentTypes(); i++) {
            n[i] = new GravityTypeParams();
        }
        gravityParams = n;
    }

    @Override
    public GravityTypeParams[] getPerTypeParams() {
        return gravityParams;
    }

    private static final long serialVersionUID = 1L;
}
