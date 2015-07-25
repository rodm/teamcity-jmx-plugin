package teamcity.jmx;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;

public class BuildServer extends BuildServerAdapter implements BuildServerMBean {

    private SBuildServer server;

    private long cleanupStartTime = 0;

    private long cleanupDuration = 0;

    public BuildServer(SBuildServer server) {
        this.server = server;
        this.server.addListener(this);
    }

    @Override
    public int getRegisteredAgents() {
        return server.getBuildAgentManager().getRegisteredAgents().size();
    }

    @Override
    public int getUnregisteredAgents() {
        return server.getBuildAgentManager().getUnregisteredAgents().size();
    }

    @Override
    public int getUnauthorizedAgents() {
        int allAgents = server.getBuildAgentManager().getRegisteredAgents(true).size();
        return allAgents - getRegisteredAgents();
    }

    @Override
    public int getNumberOfRunningBuilds() {
        return server.getNumberOfRunningBuilds();
    }

    @Override
    public int getBuildQueueSize() {
        return server.getQueue().getNumberOfItems();
    }

    @Override
    public String getFullServerVersion() {
        return server.getFullServerVersion();
    }

    @Override
    public int getNumberOfRegisteredUsers() {
        return server.getUserModel().getNumberOfRegisteredUsers();
    }

    @Override
    public int getNumberOfProjects() {
        return server.getProjectManager().getNumberOfProjects();
    }

    @Override
    public int getNumberOfBuildTypes() {
        return server.getProjectManager().getNumberOfBuildTypes();
    }

    @Override
    public long getCleanupDuration() {
        return cleanupDuration;
    }

    @Override
    public void cleanupStarted() {
        cleanupStartTime = System.currentTimeMillis();
    }

    @Override
    public void cleanupFinished() {
        cleanupDuration = (System.currentTimeMillis() - cleanupStartTime) / 1000;
    }
}
