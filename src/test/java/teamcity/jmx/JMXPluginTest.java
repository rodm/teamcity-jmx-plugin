package teamcity.jmx;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import org.junit.Before;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JMXPluginTest {

    private static final String JMX_DOMAIN = "com.jetbrains.teamcity";
    private static final String AGENT_NAME = "TestAgent";

    private MBeanServer mbeanServer;
    private SBuildServer server;
    private JMXPlugin plugin;

    @Before
    public void setup() {
        mbeanServer = mock(MBeanServer.class);
        server = mock(SBuildServer.class);

        plugin = new JMXPlugin(server) {
            @Override
            MBeanServer getMBeanServer() {
                return mbeanServer;
            }
        };
    }

    @Test
    public void shouldRegisterBuildStatisticsMBeanOnServerStartup() throws Exception {
        final ObjectName name = new ObjectName(JMX_DOMAIN + ":type=BuildServer,stats=BuildStatistics");
        ProjectManager projectManager = mock(ProjectManager.class);
        when(projectManager.getProjects()).thenReturn(Collections.<SProject>emptyList());
        when(server.getProjectManager()).thenReturn(projectManager);

        plugin.serverStartup();

        verify(mbeanServer).registerMBean(any(BuildStatistics.class), eq(name));
    }

    @Test
    public void onNewAgentRegisteringRegisterAgentBuildStatisticsMBean() throws Exception {
        final ObjectName name = new ObjectName(JMX_DOMAIN + ":type=Agent,name=" + AGENT_NAME + ",stats=BuildStatistics");
        SBuildAgent agent = mock(SBuildAgent.class);
        when(agent.getName()).thenReturn(AGENT_NAME);

        plugin.agentRegistered(agent, 0);

        verify(mbeanServer).registerMBean(any(BuildStatistics.class), eq(name));
    }

    @Test
    public void onAgentUnregisteringAgentBuildStatisticsRemainRegistered() throws Exception {
        final ObjectName name = new ObjectName(JMX_DOMAIN + ":type=Agent,name=" + AGENT_NAME + ",stats=BuildStatistics");
        SBuildAgent agent = mock(SBuildAgent.class);
        when(agent.getName()).thenReturn(AGENT_NAME);
        when(mbeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

        plugin.agentUnregistered(agent);

        verify(mbeanServer, never()).unregisterMBean(eq(name));
    }

    @Test
    public void unregisterAgentBuildStatisticsMBeanOnAgentRemove() throws Exception {
        final ObjectName name = new ObjectName(JMX_DOMAIN + ":type=Agent,name=" + AGENT_NAME + ",stats=BuildStatistics");
        SBuildAgent agent = mock(SBuildAgent.class);
        when(agent.getName()).thenReturn(AGENT_NAME);
        when(mbeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

        plugin.agentRemoved(agent);

        verify(mbeanServer).unregisterMBean(eq(name));
    }
}
