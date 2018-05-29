package teamcity.jmx;

public interface ProjectMBean {

    int getNumberOfBuildTypes();

    int getNumberOfBuildTypeTemplates();

    int getNumberOfSubProjects();

    int getNumberOfVcsRoots();

    int getNumberOfSuccessfulBuildTypes();

    int getNumberOfFailedBuildTypes();

    int getNumberOfPausedBuildTypes();

    int getSuccessPercentage();
}
