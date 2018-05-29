package teamcity.jmx;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.users.SUser;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class JMXPlugin extends BuildServerAdapter {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.SERVER");

    private static final String JMX_DOMAIN = "com.jetbrains.teamcity";

    private SBuildServer server;

    private String name;

    private Map<String, Project> projectMBeans = new HashMap<>();

    @SuppressWarnings("WeakerAccess")
    public JMXPlugin(@NotNull SBuildServer server) {
        this.server = server;
        this.name = this.getClass().getSimpleName();
        server.addListener(this);
    }

    @Override
    public void serverStartup() {
        LOGGER.info(name + " started");

        BuildServerMBean buildServer = new BuildServer(server);
        registerMBean(JMX_DOMAIN, "type=BuildServer", buildServer);
        BuildStatistics serverBuildStatistics = new BuildStatistics(server);
        registerMBean(JMX_DOMAIN, "type=BuildServer,stats=BuildStatistics", serverBuildStatistics);

        for (SProject project : server.getProjectManager().getProjects()) {
            projectCreated(project.getProjectId());
        }
    }

    @Override
    public void serverShutdown() {
        LOGGER.info(name + " stopped");
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
        unregisterMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()) + ",stats=BuildStatistics");
        unregisterMBean(JMX_DOMAIN, createAgentTypeName(agent.getName()));
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

    public void projectCreated(String projectId) {
        ProjectManager projectManager = server.getProjectManager();
        SProject project = projectManager.findProjectById(projectId);
        if (project != null) {
            Project projectMBean = new Project(project);
            projectMBeans.put(projectId, projectMBean);
            registerMBean(JMX_DOMAIN, createProjectTypeName(project.getName()), projectMBean);
        }
    }

    @Override
    public void projectRemoved(@NotNull String projectId) {
        Project projectMBean = projectMBeans.get(projectId);
        if (projectMBean != null) {
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(projectMBean.getName()));
            projectMBeans.remove(projectId);
        }
    }

    @Override
    public void projectPersisted(@NotNull String projectId) {
        SProject project = server.getProjectManager().findProjectById(projectId);
        Project projectMBean = projectMBeans.get(projectId);
        if (project != null && projectMBean != null && !project.getName().equals(projectMBean.getName())) {
            registerMBean(JMX_DOMAIN, createProjectTypeName(project.getName()), projectMBean);
            unregisterMBean(JMX_DOMAIN, createProjectTypeName(projectMBean.getName()));
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
