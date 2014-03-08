package teamcity.jmx;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JMXPluginTest {

    @Test
    public void shouldRegisterBuildStatisticsMBeanOnServerStartup() throws Exception {
        final ObjectName name = new ObjectName("com.jetbrains.teamcity:type=BuildServer,stats=BuildStatistics");
        final MBeanServer mbeanServer = mock(MBeanServer.class);
        SBuildServer server = mock(SBuildServer.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        when(projectManager.getProjects()).thenReturn(Collections.<SProject>emptyList());
        when(server.getProjectManager()).thenReturn(projectManager);

        final JMXPlugin plugin = new JMXPlugin(server) {
            @Override
            MBeanServer getMBeanServer() {
                return mbeanServer;
            }
        };

        plugin.serverStartup();

        verify(mbeanServer).registerMBean(any(BuildStatistics.class), eq(name));
    }
}
