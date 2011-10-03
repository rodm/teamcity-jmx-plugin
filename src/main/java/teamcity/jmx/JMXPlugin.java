
package teamcity.jmx;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class JMXPlugin extends BuildServerAdapter {

    private static String JMX_DOMAIN = "com.jetbrains.teamcity";

    private SBuildServer server;

    private String name;

    public JMXPlugin(/* @NotNull */ SBuildServer server) {
        this.server = server;
        this.name = this.getClass().getSimpleName();
        server.addListener(this);
    }

    @Override
    public void serverStartup() {
        Loggers.SERVER.info(name + " started");

        BuildServerMBean buildServer = new BuildServer(server);
        registerMBean(JMX_DOMAIN, "type=BuildServer", buildServer);
    }

    @Override
    public void serverShutdown() {
        Loggers.SERVER.info(name + " stopped");
    }

    private static void registerMBean(String domain, String name, Object mbean) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        String jmxName = domain + ":" + name;
        try {
            ObjectName objectName = new ObjectName(jmxName);
            if (!server.isRegistered(objectName)) {
                server.registerMBean(mbean, objectName);
            } else {
                Loggers.SERVER.warn("MBean already registered: " + jmxName);
            }
        } catch (Throwable t) {
            Loggers.SERVER.error("Failed to register MBean: " + jmxName, t);
        }
    }
}
