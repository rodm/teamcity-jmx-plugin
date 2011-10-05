
package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuildAgent;

public class Agent implements AgentMBean {

    private SBuildAgent agent;

    public Agent(SBuildAgent agent) {
        this.agent = agent;
    }

    public String getHostName() {
        return agent.getHostName();
    }

    public String getHostAddress() {
        return agent.getHostAddress();
    }

    public int getPort() {
        return agent.getPort();
    }

    public String getOperatingSystemName() {
        return agent.getOperatingSystemName();
    }
}
