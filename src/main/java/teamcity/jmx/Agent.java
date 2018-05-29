
package teamcity.jmx;

import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.SBuildAgent;

import java.util.Date;

public class Agent implements AgentMBean {

    private int agentId;

    private BuildAgentManager buildAgentManager;

    public Agent(SBuildAgent buildAgent, BuildAgentManager buildAgentManager) {
        this.agentId = buildAgent.getId();
        this.buildAgentManager = buildAgentManager;
    }

    @Override
    public String getHostName() {
        return getBuildAgent().getHostName();
    }

    @Override
    public String getHostAddress() {
        return getBuildAgent().getHostAddress();
    }

    @Override
    public int getPort() {
        return getBuildAgent().getPort();
    }

    @Override
    public String getOperatingSystemName() {
        return getBuildAgent().getOperatingSystemName();
    }

    @Override
    public Date getRegistrationTimestamp() {
        return getBuildAgent().getRegistrationTimestamp();
    }

    @Override
    public Date getLastCommunicationTimestamp() {
        return getBuildAgent().getLastCommunicationTimestamp();
    }

    @Override
    public int getCpuBenchmarkIndex() {
        return getBuildAgent().getCpuBenchmarkIndex();
    }

    @Override
    public int getNumberOfCompatibleConfigurations() {
        return buildAgentManager.getNumberOfCompatibleConfigurations(getBuildAgent());
    }

    @Override
    public int getNumberOfIncompatibleConfigurations() {
        return buildAgentManager.getNumberOfIncompatibleConfigurations(getBuildAgent());
    }

    private SBuildAgent getBuildAgent() {
        return buildAgentManager.findAgentById(agentId, true);
    }
}
