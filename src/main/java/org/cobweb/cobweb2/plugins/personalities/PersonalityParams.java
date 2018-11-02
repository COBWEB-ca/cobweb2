package org.cobweb.cobweb2.plugins.personalities;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;

public class PersonalityParams extends PerAgentParams<PersonalityAgentParams> {

    @ConfDisplayName("Personality enabled")
    @ConfXMLTag("enable")
    public boolean personalitiesEnabled = true;

    // PD will be enabled so long as personalities are enabled

    @ConfDisplayName("Temptation (PD comes with personalities, KEEP PD OFF IN OTHER TAB)")
    @ConfXMLTag("temptation")
    public int temptation = 20;

    /**
     * Reward for mutual cooperation.
     */
    @ConfDisplayName("Reward")
    @ConfXMLTag("reward")
    public int reward = 10;

    /**
     * Punishment for mutual defection.
     */
    @ConfDisplayName("Punishment")
    @ConfXMLTag("punishment")
    public int punishment = 0;

    /**
     * Sucker's payoff
     */
    @ConfDisplayName("Sucker's payoff")
    @ConfXMLTag("sucker")
    public int sucker = -5;



    public PersonalityParams(AgentFoodCountable size) {
        super(PersonalityAgentParams.class, size);
    }

    @Override
    protected PersonalityAgentParams newAgentParam() {
        return new PersonalityAgentParams();
    }

    private static final long serialVersionUID = 2L;
}
