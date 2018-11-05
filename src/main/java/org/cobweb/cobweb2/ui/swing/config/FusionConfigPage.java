package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.fusion.FusionAgentParams;
import org.cobweb.cobweb2.plugins.fusion.FusionParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;

/**
 * Configuration page for Fusion
 */
public class FusionConfigPage extends TableConfigPage<FusionAgentParams> {

    public FusionConfigPage(FusionParams params, ChoiceCatalog catalog, ColorLookup agentColors) {
        super(params.agentParams, "Fusion Parameters", agentColors, catalog);
    }
}
