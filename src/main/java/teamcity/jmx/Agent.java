
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

    @Override
    public String getHostName() {
        return agent.getHostName();
    }

    @Override
    public String getHostAddress() {
        return agent.getHostAddress();
    }

    @Override
    public int getPort() {
        return agent.getPort();
    }

    @Override
    public String getOperatingSystemName() {
        return agent.getOperatingSystemName();
    }

    @Override
    public Date getRegistrationTimestamp() {
        return agent.getRegistrationTimestamp();
    }

    @Override
    public Date getLastCommunicationTimestamp() {
        return agent.getLastCommunicationTimestamp();
    }

    @Override
    public int getCpuBenchmarkIndex() {
        return agent.getCpuBenchmarkIndex();
    }

    @Override
    public int getNumberOfCompatibleConfigurations() {
        return agentManager.getNumberOfCompatibleConfigurations(agent);
    }

    @Override
    public int getNumberOfIncompatibleConfigurations() {
        return agentManager.getNumberOfIncompatibleConfigurations(agent);
    }
}
