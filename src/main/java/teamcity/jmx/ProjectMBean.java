package teamcity.jmx;

public interface ProjectMBean {

    int getNumberOfBuildTypes();

    int getNumberOfSuccessfulBuildTypes();

    int getNumberOfFailedBuildTypes();

    int getNumberOfPausedBuildTypes();

    int getSuccessPercentage();
}
