package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuildServer;

public class BuildServer implements BuildServerMBean {

    private SBuildServer server;

    public BuildServer(SBuildServer server) {
        this.server = server;
    }

    public int getRegisteredAgents() {
        return server.getBuildAgentManager().getRegisteredAgents().size();
    }

    public int getUnregisteredAgents() {
        return server.getBuildAgentManager().getUnregisteredAgents().size();
    }

    public int getUnauthorizedAgents() {
        int allAgents = server.getBuildAgentManager().getRegisteredAgents(true).size();
        return allAgents - getRegisteredAgents();
    }

    public int getNumberOfRunningBuilds() {
        return server.getNumberOfRunningBuilds();
    }

    public int getBuildQueueSize() {
        return server.getQueue().getNumberOfItems();
    }

    public String getFullServerVersion() {
        return server.getFullServerVersion();
    }

    public int getNumberOfRegisteredUsers() {
        return server.getUserModel().getNumberOfRegisteredUsers();
    }

    public int getNumberOfProjects() {
        return server.getProjectManager().getNumberOfProjects();
    }

    public int getNumberOfBuildTypes() {
        return server.getProjectManager().getNumberOfBuildTypes();
    }
}
