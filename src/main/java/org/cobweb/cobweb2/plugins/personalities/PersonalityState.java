package org.cobweb.cobweb2.plugins.personalities;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;

public class PersonalityState implements AgentState {

    @ConfXMLTag("pdCheater")
    public boolean pdCheater;

    @ConfXMLTag("AgentParams")
    public PersonalityAgentParams agentParams;

    @Deprecated
    public PersonalityState() {
    }

    public PersonalityState(PersonalityAgentParams personalityAgentParams) {
        this.agentParams = personalityAgentParams;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    private static final long serialVersionUID = 1L;

}
