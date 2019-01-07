package org.cobweb.cobweb2.plugins.gravity;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;
import org.cobweb.util.MutatableInt;

public class GravityTypeParams implements ParameterSerializable {

    private static final long serialVersionUID = 4935757387466603476L;

    /*
     * The mass of this agent type
     */
    @ConfDisplayName("Agent mass")
    @ConfXMLTag("mass")
    public MutatableInt mass = new MutatableInt(1);

    public GravityTypeParams() {
    }
}
