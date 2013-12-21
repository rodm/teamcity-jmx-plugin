# JMX Plugin for TeamCity

The JMX Plugin makes attributes of the BuildServer and BuildAgents available using JMX.

## How to install

Download the plugin archive from [Bintray](https://bintray.com/rodm/teamcity-plugins/teamcity-jmx-plugin) and copy the `teamcity-jmx-plugin.zip` file to the `.BuildServer/plugins` directory and restart the TeamCity server.

## How to use the plugin

* Run jvisualvm
* Select the TeamCity server process, it will appear as Tomcat
* Select MBean tab and then expand the node 'com.jetbrains.teamcity'

## How to build the plugin

1. [Download](http://www.jetbrains.com/teamcity/download/index.html) and install TeamCity version 4.5 or later.
2. Copy the `example.build.properties` file to `build.properties`
3. Edit the `build.properties` file to set the properties teamcity.home, teamcity.version and teamcity.java.home
4. Run the Ant build, the default is to build and package the plugin, the plugin is output to `dist/jmx-plugin.zip`

The Ant build script provides a target to deploy the plugin to a local configuration directory, deploy-plugin. The
TeamCity server can be started using the start-teamcity-server target. The TEAMCITY_DATA_PATH is set by default to use
a local directory and not the `~/.BuildServer` directory.
