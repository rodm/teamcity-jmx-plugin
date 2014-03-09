package teamcity.jmx;

public interface BuildStatisticsMBean {
    long getBuildsStarted();
    long getBuildsFinished();
    long getBuildsInterrupted();

    long getSuccessfulBuilds();
    long getFailedBuilds();
    long getIgnoredBuilds();
}
