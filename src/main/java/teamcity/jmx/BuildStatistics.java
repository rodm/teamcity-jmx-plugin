package teamcity.jmx;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;

public class BuildStatistics extends BuildServerAdapter implements BuildStatisticsMBean {

    private BuildFilter filter;

    private long buildsStarted = 0;
    private long buildsFinished = 0;
    private long buildsInterrupted = 0;
    private long successfulBuilds = 0;
    private long failedBuilds = 0;
    private long ignoredBuilds = 0;

    public BuildStatistics(SBuildServer server) {
        this(server, new AcceptAllBuildFilter());
    }

    public BuildStatistics(SBuildServer server, BuildFilter filter) {
        server.addListener(this);
        this.filter = filter;
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
    public long getSuccessfulBuilds() {
        return successfulBuilds;
    }

    @Override
    public long getFailedBuilds() {
        return failedBuilds;
    }

    @Override
    public long getIgnoredBuilds() {
        return ignoredBuilds;
    }

    @Override
    public void buildStarted(SRunningBuild build) {
        if (filter.accept(build)) {
            buildsStarted++;
        }
    }

    @Override
    public void buildFinished(SRunningBuild build) {
        if (filter.accept(build)) {
            buildsFinished++;
            Status status = build.getBuildStatus();
            if (status.isSuccessful()) {
                successfulBuilds++;
            }
            if (status.isFailed()) {
                failedBuilds++;
            }
            if (status.isIgnored()) {
                ignoredBuilds++;
            }
        }
    }

    @Override
    public void buildInterrupted(SRunningBuild build) {
        if (filter.accept(build)) {
            buildsInterrupted++;
        }
    }
}
