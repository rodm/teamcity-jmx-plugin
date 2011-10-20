package teamcity.jmx;

public interface BuildServerMBean {
    int getRegisteredAgents();

    int getUnregisteredAgents();

    int getUnauthorizedAgents();

    int getNumberOfRunningBuilds();

    int getBuildQueueSize();

    String getFullServerVersion();

    int getNumberOfRegisteredUsers();

    int getNumberOfProjects();

    int getNumberOfBuildTypes();

    long getCleanupDuration();
}
