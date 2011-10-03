package teamcity.jmx;

public interface BuildServerMBean {
    int getNumberOfAgents();

    int getAgentsRunning();

    int getBuildQueueSize();
}
