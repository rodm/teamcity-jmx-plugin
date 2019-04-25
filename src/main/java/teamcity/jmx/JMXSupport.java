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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static jetbrains.buildServer.log.Loggers.SERVER_CATEGORY;

public class JMXSupport extends BasePluginStatePersister implements StateSaver, Runnable {

    private static final Logger LOGGER = Logger.getLogger(SERVER_CATEGORY + "." + JMXSupport.class.getSimpleName());

    private static final String JMX_DOMAIN = "com.jetbrains.teamcity";
    private static final String BUILD_SERVER_NAME = "type=BuildServer";
    private static final String AGENT_NAME = "type=Agent,name=";
    private static final String PROJECT_NAME = "type=Project,name=";
    private static final String BUILD_STATISTICS_NAME = ",stats=BuildStatistics";

    private SBuildServer buildServer;

    private BuildStatisticsListener statisticsListener;

    private BuildServer buildServerMBean;
    private Map<Integer, Agent> agentMBeans = new HashMap<>();
    private Map<String, Project> projectMBeans = new HashMap<>();

    private LocalDate date;
    private ScheduledExecutorService executor;

    @SuppressWarnings("WeakerAccess")
    public JMXSupport(@NotNull SBuildServer buildServer,
                      @NotNull ServerPaths serverPaths,
                      @NotNull BuildStatisticsListener statisticsListener)
    {
        super(buildServer, serverPaths);
        this.buildServer = buildServer;
        this.statisticsListener = statisticsListener;
    }

    @Override
    public void serverStartup() {
        LOGGER.info("JMX Support plugin started");

        buildServerMBean = new BuildServer(this, buildServer);
        registerMBean(JMX_DOMAIN, BUILD_SERVER_NAME, buildServerMBean);
        BuildStatistics serverBuildStatistics = statisticsListener.getServerBuildStatistics();
        registerMBean(JMX_DOMAIN, BUILD_SERVER_NAME + BUILD_STATISTICS_NAME, serverBuildStatistics);
        super.serverStartup();

        BuildAgentManager agentManager = buildServer.getBuildAgentManager();
        agentManager.getRegisteredAgents(true).forEach(agent -> agentRegistered(agent, -1));
        agentManager.getUnregisteredAgents().forEach(agent -> agentRegistered(agent, -1));

        date = LocalDate.now();
        executor = Executors.newSingleThreadScheduledExecutor();
        scheduleCounterReset();
    }

    @Override
    public void serverShutdown() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn("Scheduled executor shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
        super.serverShutdown();

        unregisterMBean(JMX_DOMAIN, BUILD_SERVER_NAME);
        unregisterMBean(JMX_DOMAIN, BUILD_SERVER_NAME + BUILD_STATISTICS_NAME);
        agentMBeans.values().forEach(agent -> {
            unregisterMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()));
            unregisterMBean(JMX_DOMAIN, createAgentBuildStatisticsName(agent.getName()));
        });
        projectMBeans.values().forEach(project -> {
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(project.getName()));
            unregisterMBean(JMX_DOMAIN, createProjectBuildStatisticsName(project.getName()));
        });
        LOGGER.info("JMX Support plugin stopped");
    }

    @Override
    public void run() {
        LOGGER.info("Resetting build statistics");
        LocalDate now = LocalDate.now();
        if (!date.equals(now)) {
            date = now;
            statisticsListener.reset();
        }
        scheduleCounterReset();
    }

    private void scheduleCounterReset() {
        LocalDateTime startOfNextDay = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        Duration durationUntilMidnight = Duration.between(LocalDateTime.now(), startOfNextDay);
        executor.schedule(this, durationUntilMidnight.getSeconds() + 5, TimeUnit.SECONDS);
    }

    public void saveState() {
        LOGGER.info("Saving plugin state");
        super.saveState();
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
        writeServer(root);
        writeAgents(root);
        writeProjects(root);
    }

    private void writeServer(@NotNull Element root) {
        final Element server = new Element("server");
        server.setAttribute("cleanup-starttime", Long.toString(buildServerMBean.getCleanupStartTime()));
        server.setAttribute("cleanup-duration", Long.toString(buildServerMBean.getCleanupDuration()));
        root.addContent(server);
        statisticsListener.getServerBuildStatistics().writeExternal(server);
    }

    private void writeAgents(@NotNull Element root) {
        final Element agents = new Element("agents");
        for (Integer agentId : agentMBeans.keySet()) {
            final Element agent = new Element("agent");
            agent.setAttribute("id", Integer.toString(agentId));
            statisticsListener.getAgentBuildStatistics(agentId).writeExternal(agent);
            agents.addContent(agent);
        }
        root.addContent(agents);
    }

    private void writeProjects(@NotNull Element root) {
        final Element projects = new Element("projects");
        for (String projectId : projectMBeans.keySet()) {
            final Element project = new Element("project");
            project.setAttribute("id", projectId);
            statisticsListener.getProjectBuildStatistics(projectId).writeExternal(project);
            projects.addContent(project);
        }
        root.addContent(projects);
    }

    @Override
    protected void readExternal(@NotNull Element root) {
        readServer(root);
        readAgents(root);
        readProjects(root);
    }

    private void readServer(@NotNull Element root) {
        Element server = root.getChild("server");
        if (server != null) {
            String cleanupStartTime = server.getAttributeValue("cleanup-starttime", "0");
            String cleanupDuration = server.getAttributeValue("cleanup-duration", "0");
            buildServerMBean.setCleanupStartTime(Long.parseLong(cleanupStartTime));
            buildServerMBean.setCleanupDuration(Long.parseLong(cleanupDuration));
            statisticsListener.getServerBuildStatistics().readExternal(server);
        }
    }

    private void readAgents(@NotNull Element root) {
        Element agents = root.getChild("agents");
        for (Object object : agents.getChildren("agent")) {
            Element agent = (Element) object;
            Integer agentId = Integer.parseInt(agent.getAttributeValue("id", "0"));
            BuildStatistics buildStatistics = statisticsListener.getAgentBuildStatistics(agentId);
            buildStatistics.readExternal(agent);
        }
    }

    private void readProjects(@NotNull Element root) {
        Element projects = root.getChild("projects");
        for (Object object : projects.getChildren("project")) {
            Element project = (Element) object;
            String projectId = project.getAttributeValue("id");
            BuildStatistics buildStatistics = statisticsListener.getProjectBuildStatistics(projectId);
            buildStatistics.readExternal(project);
        }
    }

    @Override
    public void agentRegistered(@NotNull SBuildAgent agent, long currentlyRunningBuildId) {
        int agentId = agent.getId();
        Agent agentMBean = agentMBeans.computeIfAbsent(agentId, key -> new Agent(agent, buildServer.getBuildAgentManager()));
        BuildStatisticsMBean agentBuildStatistics = statisticsListener.getAgentBuildStatistics(agentId);
        registerMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()), agentMBean);
        registerMBean(JMX_DOMAIN, createAgentBuildStatisticsName(agent.getName()), agentBuildStatistics);
        if (!agentMBean.getName().equals(agent.getName())) {
            unregisterMBean(JMX_DOMAIN, createAgentTypeName(agentMBean.getName()));
            unregisterMBean(JMX_DOMAIN, createAgentBuildStatisticsName(agentMBean.getName()));
            agentMBean.setName(agent.getName());
        }
    }

    @Override
    public void agentRemoved(@NotNull SBuildAgent agent) {
        unregisterMBean(JMX_DOMAIN, createAgentBuildStatisticsName(agent.getName()));
        unregisterMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()));
        agentMBeans.remove(agent.getId());
        statisticsListener.removeAgentBuildStatistics(agent.getId());
    }

    @Override
    public void projectCreated(@NotNull String projectId, SUser user) {
        projectCreated(projectId);
    }

    private void projectCreated(String projectId) {
        ProjectManager projectManager = buildServer.getProjectManager();
        SProject project = projectManager.findProjectById(projectId);
        if (project != null) {
            Project projectMBean = new Project(project);
            projectMBeans.put(projectId, projectMBean);
            BuildStatistics buildStatisticsMBean = statisticsListener.getProjectBuildStatistics(projectId);
            registerMBean(JMX_DOMAIN, createProjectTypeName(project.getFullName()), projectMBean);
            registerMBean(JMX_DOMAIN, createProjectBuildStatisticsName(project.getFullName()), buildStatisticsMBean);
        }
    }

    @Override
    public void projectRemoved(@NotNull SProject project) {
        String projectId = project.getProjectId();
        Project projectMBean = projectMBeans.get(projectId);
        if (projectMBean != null) {
            unregisterMBean(JMX_DOMAIN, createProjectBuildStatisticsName(projectMBean.getName()));
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(projectMBean.getName()));
            projectMBeans.remove(projectId);
            statisticsListener.removeProjectBuildStatistics(projectId);
        }
    }

    @Override
    public void projectPersisted(@NotNull String projectId) {
        Project projectMBean = projectMBeans.get(projectId);
        BuildStatistics buildStatisticsMBean = statisticsListener.getProjectBuildStatistics(projectId);
        SProject project = buildServer.getProjectManager().findProjectById(projectId);
        if (project != null && projectMBean != null && !project.getFullName().equals(projectMBean.getName())) {
            registerMBean(JMX_DOMAIN, createProjectTypeName(project.getFullName()), projectMBean);
            registerMBean(JMX_DOMAIN, createProjectBuildStatisticsName(project.getFullName()), buildStatisticsMBean);
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(projectMBean.getName()));
            unregisterMBean(JMX_DOMAIN, createProjectBuildStatisticsName(projectMBean.getName()));
            projectMBean.setName(project.getFullName());
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

    private String createAgentBuildStatisticsName(String agentName) {
        return createAgentTypeName(agentName) + BUILD_STATISTICS_NAME;
    }

    private String createProjectBuildStatisticsName(String projectName) {
        return createProjectTypeName(projectName) + BUILD_STATISTICS_NAME;
    }

    private String createAgentTypeName(String agentName) {
        return AGENT_NAME + agentName;
    }

    private String createProjectTypeName(String projectName) {
        return PROJECT_NAME + projectName;
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
