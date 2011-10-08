package teamcity.jmx;

import java.util.Date;

public interface AgentMBean {
    String getHostName();

    String getHostAddress();

    int getPort();

    String getOperatingSystemName();

    Date getRegistrationTimestamp();

    Date getLastCommunicationTimestamp();

    int getCpuBenchmarkIndex();

    int getNumberOfCompatibleConfigurations();

    int getNumberOfIncompatibleConfigurations();
}
