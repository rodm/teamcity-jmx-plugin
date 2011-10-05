package teamcity.jmx;

public interface AgentMBean {
    String getHostName();
    String getHostAddress();
    int getPort();
    String getOperatingSystemName();
}
