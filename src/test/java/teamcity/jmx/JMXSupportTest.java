/*
 * Copyright 2018 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcity.jmx;

import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JMXSupportTest {

    private static final String JMX_DOMAIN = "com.jetbrains.teamcity";
    private static final String AGENT_NAME = "TestAgent";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private MBeanServer mbeanServer;
    private SBuildServer server;
    private JMXSupport plugin;

    @Before
    public void setup() {
        mbeanServer = mock(MBeanServer.class);
        server = mock(SBuildServer.class);
        BuildStatisticsListener statisticsListener = new BuildStatisticsListener(server);

        plugin = new JMXSupport(server, new ServerPaths(folder.getRoot()), statisticsListener) {
            @Override
            MBeanServer getMBeanServer() {
                return mbeanServer;
            }
        };
    }

    @Test
    public void shouldRegisterBuildStatisticsMBeanOnServerStartup() throws Exception {
        BuildAgentManager agentManager = mock(BuildAgentManager.class);
        when(server.getBuildAgentManager()).thenReturn(agentManager);
        ProjectManager projectManager = mock(ProjectManager.class);
        when(server.getProjectManager()).thenReturn(projectManager);

        final ObjectName serverMBeanName = new ObjectName(JMX_DOMAIN + ":type=BuildServer");
        final ObjectName serverStatsMBeanName = new ObjectName(JMX_DOMAIN + ":type=BuildServer,stats=BuildStatistics");

        plugin.serverStartup();

        verify(mbeanServer).registerMBean(any(BuildServer.class), eq(serverMBeanName));
        verify(mbeanServer).registerMBean(any(BuildStatistics.class), eq(serverStatsMBeanName));
    }

    @Test
    public void shouldUnregisterBuildStatisticsMBeanOnServerShut() throws Exception {
        BuildAgentManager agentManager = mock(BuildAgentManager.class);
        when(server.getBuildAgentManager()).thenReturn(agentManager);
        ProjectManager projectManager = mock(ProjectManager.class);
        when(server.getProjectManager()).thenReturn(projectManager);
        plugin.serverStartup();

        final ObjectName serverMBeanName = new ObjectName(JMX_DOMAIN + ":type=BuildServer");
        final ObjectName serverStatsMBeanName = new ObjectName(JMX_DOMAIN + ":type=BuildServer,stats=BuildStatistics");
        when(mbeanServer.isRegistered(serverMBeanName)).thenReturn(true);
        when(mbeanServer.isRegistered(serverStatsMBeanName)).thenReturn(true);

        plugin.serverShutdown();

        verify(mbeanServer).unregisterMBean(serverMBeanName);
        verify(mbeanServer).unregisterMBean(serverStatsMBeanName);
    }

    @Test
    public void onNewAgentRegisteringRegisterAgentBuildStatisticsMBean() throws Exception {
        final ObjectName name = new ObjectName(JMX_DOMAIN + ":type=Agent,name=" + ObjectName.quote(AGENT_NAME) + ",stats=BuildStatistics");
        SBuildAgent agent = mock(SBuildAgent.class);
        when(agent.getName()).thenReturn(AGENT_NAME);

        plugin.agentRegistered(agent, 0);

        verify(mbeanServer).registerMBean(any(BuildStatistics.class), eq(name));
    }

    @Test
    public void onAgentUnregisteringAgentBuildStatisticsRemainRegistered() throws Exception {
        final ObjectName name = new ObjectName(JMX_DOMAIN + ":type=Agent,name=" + ObjectName.quote(AGENT_NAME) + ",stats=BuildStatistics");
        SBuildAgent agent = mock(SBuildAgent.class);
        when(agent.getName()).thenReturn(AGENT_NAME);
        when(mbeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

        plugin.agentUnregistered(agent);

        verify(mbeanServer, never()).unregisterMBean(name);
    }

    @Test
    public void unregisterAgentBuildStatisticsMBeanOnAgentRemove() throws Exception {
        final ObjectName name = new ObjectName(JMX_DOMAIN + ":type=Agent,name=" + ObjectName.quote(AGENT_NAME) + ",stats=BuildStatistics");
        SBuildAgent agent = mock(SBuildAgent.class);
        when(agent.getName()).thenReturn(AGENT_NAME);
        when(mbeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

        plugin.agentRemoved(agent);

        verify(mbeanServer).unregisterMBean(name);
    }
}
