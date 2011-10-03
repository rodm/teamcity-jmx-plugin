package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuildServer;

public class BuildServer implements BuildServerMBean {

    private SBuildServer server;

    public BuildServer(SBuildServer server) {
        this.server = server;
    }

    public int getNumberOfAgents() {
        return server.getBuildAgentManager().getRegisteredAgents(true).size();
    }

    public int getAgentsRunning() {
        return server.getNumberOfRunningBuilds();
    }

    public int getBuildQueueSize() {
        return server.getQueue().getNumberOfItems();
    }
}
