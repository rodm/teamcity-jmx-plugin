package teamcity.jmx;

import java.util.Date;

public interface AgentMBean {
    String getHostName();

    String getHostAddress();

    int getPort();

    String getOperatingSystemName();

    boolean isRegistered();

    String getUnregistrationComment();

    Date getRegistrationTimestamp();

    Date getLastCommunicationTimestamp();

    boolean isEnabled();

    String getStatusComment();

    boolean isAuthorized();

    String getAuthorizeComment();

    int getCpuBenchmarkIndex();

    int getNumberOfCompatibleConfigurations();

    int getNumberOfIncompatibleConfigurations();
}
