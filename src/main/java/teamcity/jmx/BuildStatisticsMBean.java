package teamcity.jmx;

public interface BuildStatisticsMBean {
    long getBuildsStarted();
    long getBuildsFinished();
    long getBuildsInterrupted();
}
