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

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.BasePluginStatePersister;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.log.Loggers.SERVER_CATEGORY;

public class JMXSupport extends BasePluginStatePersister {

    private static final Logger LOGGER = Logger.getLogger(SERVER_CATEGORY + "." + JMXSupport.class.getSimpleName());

    private static final String JMX_DOMAIN = "com.jetbrains.teamcity";

    private SBuildServer server;

    private String name;

    private BuildServer buildServer;
    private BuildStatistics serverBuildStatistics;
    private Map<String, Project> projectMBeans = new HashMap<>();
    private Map<String, BuildStatistics> projectBuildStatisticsMBeans = new HashMap<>();

    @SuppressWarnings("WeakerAccess")
    public JMXSupport(@NotNull SBuildServer server, @NotNull ServerPaths serverPaths) {
        super(server, serverPaths);
        this.server = server;
        this.name = this.getClass().getSimpleName();
    }

    @Override
    public void serverStartup() {
        LOGGER.info(name + " plugin started");

        buildServer = new BuildServer(server);
        registerMBean(JMX_DOMAIN, "type=BuildServer", buildServer);
        serverBuildStatistics = new BuildStatistics(server);
        registerMBean(JMX_DOMAIN, "type=BuildServer,stats=BuildStatistics", serverBuildStatistics);

        for (SProject project : server.getProjectManager().getProjects()) {
            projectCreated(project.getProjectId());
        }
        super.serverStartup();
    }

    @Override
    public void serverShutdown() {
        super.serverShutdown();
        LOGGER.info(name + " plugin stopped");
    }

    @NotNull
    @Override
    protected String getPluginName() {
        return "jmx-support";
    }

    @NotNull
    @Override
    protected String getStateName() {
        return "state";
    }

    @Override
    protected void writeExternal(@NotNull Element root) {
        final Element server = new Element("server");
        server.setAttribute("cleanup-starttime", Long.toString(buildServer.getCleanupStartTime()));
        server.setAttribute("cleanup-duration", Long.toString(buildServer.getCleanupDuration()));
        root.addContent(server);
    }

    @Override
    protected void readExternal(@NotNull Element root) {
        Element server = root.getChild("server");
        if (server != null) {
            String cleanupStartTime = server.getAttributeValue("cleanup-starttime", "0");
            String cleanupDuration = server.getAttributeValue("cleanup-duration", "0");
            buildServer.setCleanupStartTime(Long.parseLong(cleanupStartTime));
            buildServer.setCleanupDuration(Long.parseLong(cleanupDuration));
        }
    }

    @Override
    public void agentRegistered(@NotNull SBuildAgent agent, long currentlyRunningBuildId) {
        AgentMBean agentMBean = new Agent(agent, server.getBuildAgentManager());
        registerMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()), agentMBean);
        BuildStatisticsMBean agentBuildStatistics = new BuildStatistics(server, new AgentBuildFilter(agent));
        registerMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()) + ",stats=BuildStatistics", agentBuildStatistics);
    }

    @Override
    public void agentUnregistered(@NotNull SBuildAgent agent) {
    }

    @Override
    public void agentRemoved(@NotNull SBuildAgent agent) {
        unregisterMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()) + ",stats=BuildStatistics");
        unregisterMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()));
    }

    @Override
    public void projectCreated(@NotNull String projectId, SUser user) {
        projectCreated(projectId);
    }

    private void projectCreated(String projectId) {
        ProjectManager projectManager = server.getProjectManager();
        SProject project = projectManager.findProjectById(projectId);
        if (project != null) {
            Project projectMBean = new Project(project);
            projectMBeans.put(projectId, projectMBean);
            BuildStatistics buildStatisticsMBean = new BuildStatistics(server, new ProjectBuildFilter(project));
            projectBuildStatisticsMBeans.put(projectId, buildStatisticsMBean);
            registerMBean(JMX_DOMAIN, createProjectTypeName(project.getName()), projectMBean);
            registerMBean(JMX_DOMAIN, createProjectTypeName(project.getName()) + ",stats=BuildStatistics", buildStatisticsMBean);
        }
    }

    @Override
    public void projectRemoved(@NotNull SProject project) {
        String projectId = project.getProjectId();
        Project projectMBean = projectMBeans.get(projectId);
        if (projectMBean != null) {
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(projectMBean.getName()) + ",stats=BuildStatistics");
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(projectMBean.getName()));
            projectMBeans.remove(projectId);
            projectBuildStatisticsMBeans.remove(projectId);
        }
    }

    @Override
    public void projectPersisted(@NotNull String projectId) {
        Project projectMBean = projectMBeans.get(projectId);
        BuildStatistics buildStatisticsMBean = projectBuildStatisticsMBeans.get(projectId);
        SProject project = server.getProjectManager().findProjectById(projectId);
        if (project != null && projectMBean != null && !project.getName().equals(projectMBean.getName())) {
            registerMBean(JMX_DOMAIN, createProjectTypeName(project.getName()), projectMBean);
            registerMBean(JMX_DOMAIN, createProjectTypeName(project.getName()) + ",stats=BuildStatistics", buildStatisticsMBean);
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(projectMBean.getName()));
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(projectMBean.getName()) + ",stats=BuildStatistics");
            projectMBean.setName(project.getName());
        }
    }

    @Override
    public void buildTypeRegistered(@NotNull SBuildType buildType) {
        updateProject(buildType.getProjectId());
    }

    @Override
    public void buildTypeUnregistered(@NotNull SBuildType buildType) {
        updateProject(buildType.getProjectId());
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        updateProject(build.getProjectId());
    }

    @Override
    public void buildTypeActiveStatusChanged(@NotNull SBuildType buildType) {
        updateProject(buildType.getProjectId());
    }

    private void updateProject(String projectId) {
        Project project = projectMBeans.get(projectId);
        project.update();
    }

    private String createAgentTypeName(String agentName) {
        return "type=Agent,name=" + agentName;
    }

    private String createProjectTypeName(String projectName) {
        return "type=Project,name=" + projectName;
    }

    private void registerMBean(String domain, String name, Object mbean) {
        MBeanServer mBeanServer = getMBeanServer();

        String jmxName = domain + ":" + name;
        try {
            ObjectName objectName = new ObjectName(jmxName);
            if (!mBeanServer.isRegistered(objectName)) {
                mBeanServer.registerMBean(mbean, objectName);
            } else {
                LOGGER.warn("MBean already registered: " + jmxName);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register MBean: " + jmxName, e);
        }
    }

    private void unregisterMBean(String domain, String name) {
        MBeanServer mBeanServer = getMBeanServer();

        String jmxName = domain + ":" + name;
        try {
            ObjectName objectName = new ObjectName(jmxName);
            if (mBeanServer.isRegistered(objectName)) {
                mBeanServer.unregisterMBean(objectName);
            } else {
                LOGGER.warn("MBean not registered: " + jmxName);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to unregister MBean: " + jmxName, e);
        }
    }

    MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
}
