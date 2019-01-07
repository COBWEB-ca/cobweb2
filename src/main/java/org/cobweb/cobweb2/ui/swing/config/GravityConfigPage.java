package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.gravity.GravityParams;
import org.cobweb.cobweb2.plugins.gravity.GravityTypeParams;
import org.cobweb.swingutil.ColorLookup;

public class GravityConfigPage extends TwoTableConfigPage<GravityParams, GravityTypeParams> {

    public GravityConfigPage(GravityParams params, ColorLookup agentColors) {
        super(GravityParams.class, params, "Gravity Parameters", agentColors, "Value", null, "Gravity Type Parameters", "Agent");
        setMainPanelHeight(100);
    }
}
