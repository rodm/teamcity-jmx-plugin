package teamcity.jmx;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;

public class BuildStatistics extends BuildServerAdapter implements BuildStatisticsMBean {

    private long buildsStarted = 0;
    private long buildsFinished = 0;
    private long buildsInterrupted = 0;

    public BuildStatistics(SBuildServer server) {
        server.addListener(this);
    }

    @Override
    public long getBuildsStarted() {
        return buildsStarted;
    }

    @Override
    public long getBuildsFinished() {
        return buildsFinished;
    }

    @Override
    public long getBuildsInterrupted() {
        return buildsInterrupted;
    }

    @Override
    public void buildStarted(SRunningBuild build) {
        buildsStarted++;
    }

    @Override
    public void buildFinished(SRunningBuild build) {
        buildsFinished++;
    }

    @Override
    public void buildInterrupted(SRunningBuild build) {
        buildsInterrupted++;
    }
}
