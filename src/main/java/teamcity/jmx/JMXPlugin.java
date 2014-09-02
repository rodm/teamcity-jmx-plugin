package teamcity.jmx;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.users.SUser;
import org.apache.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class JMXPlugin extends BuildServerAdapter {

    private static Logger log = Logger.getLogger("jetbrains.buildServer.SERVER");

    private static String JMX_DOMAIN = "com.jetbrains.teamcity";

    private SBuildServer server;

    private String name;

    private Map<String, Project> projectMBeans = new HashMap<String, Project>();

    public JMXPlugin(/* @NotNull */ SBuildServer server) {
        this.server = server;
        this.name = this.getClass().getSimpleName();
        server.addListener(this);
    }

    @Override
    public void serverStartup() {
        log.info(name + " started");

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
        log.info(name + " stopped");
    }

    @Override
    public void agentRegistered(SBuildAgent agent, long currentlyRunningBuildId) {
        AgentMBean agentMBean = new Agent(agent, server.getBuildAgentManager());
        registerMBean(JMX_DOMAIN, "type=Agent,name=" + agent.getName(), agentMBean);
        BuildStatisticsMBean agentBuildStatistics = new BuildStatistics(server, new AgentBuildFilter(agent));
        registerMBean(JMX_DOMAIN, "type=Agent,name=" + agent.getName() + ",stats=BuildStatistics", agentBuildStatistics);
    }

    @Override
    public void agentUnregistered(SBuildAgent agent) {
        unregisterMBean(JMX_DOMAIN, "type=Agent,name=" + agent.getName() + ",stats=BuildStatistics");
        unregisterMBean(JMX_DOMAIN, "type=Agent,name=" + agent.getName());
    }

    @Override
    public void agentRemoved(SBuildAgent agent) {
        unregisterMBean(JMX_DOMAIN, "type=Agent,name=" + agent.getName());
    }

    public void projectCreated(String projectId, SUser user) {
        projectCreated(projectId);
    }

    public void projectCreated(String projectId) {
        ProjectManager projectManager = server.getProjectManager();
        SProject project = projectManager.findProjectById(projectId);
        if (project != null) {
            Project projectMBean = new Project(project);
            projectMBeans.put(projectId, projectMBean);
            registerMBean(JMX_DOMAIN, "type=Project,name=" + project.getName(), projectMBean);
        }
    }

    @Override
    public void projectRemoved(String projectId) {
        Project projectMBean = projectMBeans.get(projectId);
        if (projectMBean != null) {
            unregisterMBean(JMX_DOMAIN, "type=Project,name=" + projectMBean.getName());
            projectMBeans.remove(projectId);
        }
    }

    @Override
    public void projectPersisted(String projectId) {
        SProject project = server.getProjectManager().findProjectById(projectId);
        Project projectMBean = projectMBeans.get(projectId);
        if (project != null && projectMBean != null) {
            if (!project.getName().equals(projectMBean.getName())) {
                registerMBean(JMX_DOMAIN, "type=Project,name=" + project.getName(), projectMBean);
                unregisterMBean(JMX_DOMAIN, "type=Project,name=" + projectMBean.getName());
                projectMBean.setName(project.getName());
            }
        }
    }

    @Override
    public void buildTypeRegistered(SBuildType buildType) {
        updateProject(buildType.getProjectId());
    }

    @Override
    public void buildTypeUnregistered(SBuildType buildType) {
        updateProject(buildType.getProjectId());
    }

    @Override
    public void buildFinished(SRunningBuild build) {
        updateProject(build.getProjectId());
    }

    @Override
    public void buildTypeActiveStatusChanged(SBuildType buildType) {
        updateProject(buildType.getProjectId());
    }

    private void updateProject(String projectId) {
        Project project = projectMBeans.get(projectId);
        project.update();
    }

    private void registerMBean(String domain, String name, Object mbean) {
        MBeanServer server = getMBeanServer();

        String jmxName = domain + ":" + name;
        try {
            ObjectName objectName = new ObjectName(jmxName);
            if (!server.isRegistered(objectName)) {
                server.registerMBean(mbean, objectName);
            } else {
                log.warn("MBean already registered: " + jmxName);
            }
        } catch (Throwable t) {
            log.error("Failed to register MBean: " + jmxName, t);
        }
    }

    private void unregisterMBean(String domain, String name) {
        MBeanServer server = getMBeanServer();

        String jmxName = domain + ":" + name;
        try {
            ObjectName objectName = new ObjectName(jmxName);
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            } else {
                log.warn("MBean not registered: " + jmxName);
            }
        } catch (Throwable t) {
            log.error("Failed to unregister MBean: " + jmxName, t);
        }
    }

    MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
}
