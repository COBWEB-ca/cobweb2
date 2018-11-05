package org.cobweb.cobweb2.plugins.fusion;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;
import org.cobweb.util.CloneHelper;

public class FusionAgentParams implements ParameterSerializable {

    @ConfDisplayName("Fusion Enabled")
    @ConfXMLTag("fusionEnabled")
    public boolean fusionEnabled = false;

    @ConfDisplayName("Fusion Efficiency")
    @ConfXMLTag("fusionEfficiency")
    public float efficiency = 1f;

    @ConfDisplayName("Fusion probability")
    @ConfXMLTag("fusionProb")
    public float probability = 0f;

    @ConfDisplayName("Fusion Same Type")
    @ConfXMLTag("fusionSameType")
    public boolean sameType = false;

    @Override
    protected FusionAgentParams clone() {
        try {
            FusionAgentParams copy = (FusionAgentParams)super.clone();
            CloneHelper.resetMutatable(copy);
            return copy;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final long serialVersionUID = 1L;
}
