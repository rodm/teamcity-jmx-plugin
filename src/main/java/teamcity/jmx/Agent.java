
package teamcity.jmx;

import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.SBuildAgent;

import java.util.Date;

public class Agent implements AgentMBean {

    private SBuildAgent buildAgent;

    private BuildAgentManager buildAgentManager;

    public Agent(SBuildAgent buildAgent, BuildAgentManager buildAgentManager) {
        this.buildAgent = buildAgent;
        this.buildAgentManager = buildAgentManager;
    }

    @Override
    public String getHostName() {
        return buildAgent.getHostName();
    }

    @Override
    public String getHostAddress() {
        return buildAgent.getHostAddress();
    }

    @Override
    public int getPort() {
        return buildAgent.getPort();
    }

    @Override
    public String getOperatingSystemName() {
        return buildAgent.getOperatingSystemName();
    }

    @Override
    public Date getRegistrationTimestamp() {
        return buildAgent.getRegistrationTimestamp();
    }

    @Override
    public Date getLastCommunicationTimestamp() {
        return buildAgent.getLastCommunicationTimestamp();
    }

    @Override
    public int getCpuBenchmarkIndex() {
        return buildAgent.getCpuBenchmarkIndex();
    }

    @Override
    public int getNumberOfCompatibleConfigurations() {
        return buildAgentManager.getNumberOfCompatibleConfigurations(buildAgent);
    }

    @Override
    public int getNumberOfIncompatibleConfigurations() {
        return buildAgentManager.getNumberOfIncompatibleConfigurations(buildAgent);
    }
}
