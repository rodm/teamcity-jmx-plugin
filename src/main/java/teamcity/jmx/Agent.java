
package teamcity.jmx;

import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.SBuildAgent;

import java.util.Date;

public class Agent implements AgentMBean {

    private SBuildAgent agent;

    private BuildAgentManager agentManager;

    public Agent(SBuildAgent agent, BuildAgentManager agentManager) {
        this.agent = agent;
        this.agentManager = agentManager;
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

    public Date getRegistrationTimestamp() {
        return agent.getRegistrationTimestamp();
    }

    public Date getLastCommunicationTimestamp() {
        return agent.getLastCommunicationTimestamp();
    }

    public int getCpuBenchmarkIndex() {
        return agent.getCpuBenchmarkIndex();
    }

    public int getNumberOfCompatibleConfigurations() {
        return agentManager.getNumberOfCompatibleConfigurations(agent);
    }

    public int getNumberOfIncompatibleConfigurations() {
        return agentManager.getNumberOfIncompatibleConfigurations(agent);
    }
}
