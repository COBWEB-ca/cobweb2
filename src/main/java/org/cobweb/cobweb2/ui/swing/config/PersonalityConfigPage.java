package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.personalities.PersonalityAgentParams;
import org.cobweb.cobweb2.plugins.personalities.PersonalityParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;

public class PersonalityConfigPage extends TwoTableConfigPage<PersonalityParams, PersonalityAgentParams> {

    public PersonalityConfigPage(PersonalityParams params, ColorLookup agentColors) {
        super(PersonalityParams.class, params, "Personality Parameters", agentColors);
    }
}
