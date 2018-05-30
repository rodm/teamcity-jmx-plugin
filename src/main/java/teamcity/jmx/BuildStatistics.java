package teamcity.jmx;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

public class BuildStatistics extends BuildServerAdapter implements BuildStatisticsMBean {

    private BuildFilter filter;

    private AtomicLong buildsStarted = new AtomicLong();
    private AtomicLong buildsFinished = new AtomicLong();
    private AtomicLong buildsInterrupted = new AtomicLong();
    private AtomicLong successfulBuilds = new AtomicLong();
    private AtomicLong failedBuilds = new AtomicLong();
    private AtomicLong ignoredBuilds = new AtomicLong();

    public BuildStatistics(SBuildServer server) {
        this(server, new AcceptAllBuildFilter());
    }

    public BuildStatistics(SBuildServer server, BuildFilter filter) {
        server.addListener(this);
        this.filter = filter;
    }

    @Override
    public long getBuildsStarted() {
        return buildsStarted.get();
    }

    @Override
    public long getBuildsFinished() {
        return buildsFinished.get();
    }

    @Override
    public long getBuildsInterrupted() {
        return buildsInterrupted.get();
    }

    @Override
    public long getSuccessfulBuilds() {
        return successfulBuilds.get();
    }

    @Override
    public long getFailedBuilds() {
        return failedBuilds.get();
    }

    @Override
    public long getIgnoredBuilds() {
        return ignoredBuilds.get();
    }

    @Override
    public void buildStarted(@NotNull SRunningBuild build) {
        if (filter.accept(build)) {
            buildsStarted.incrementAndGet();
        }
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        if (filter.accept(build)) {
            buildsFinished.incrementAndGet();
            Status status = build.getBuildStatus();
            if (status.isSuccessful()) {
                successfulBuilds.incrementAndGet();
            }
            if (status.isFailed()) {
                failedBuilds.incrementAndGet();
            }
            if (status.isIgnored()) {
                ignoredBuilds.incrementAndGet();
            }
        }
    }

    @Override
    public void buildInterrupted(@NotNull SRunningBuild build) {
        if (filter.accept(build)) {
            buildsInterrupted.incrementAndGet();
        }
    }
}
