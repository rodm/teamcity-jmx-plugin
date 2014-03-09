package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildAgent;

public class AgentBuildFilter implements BuildFilter {

    private SBuildAgent agent;

    public AgentBuildFilter(SBuildAgent agent) {
        this.agent = agent;
    }

    @Override
    public boolean accept(SBuild build) {
        return agent.getId() == build.getAgent().getId();
    }
}
