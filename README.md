# JMX Plugin for TeamCity

The JMX Plugin makes attributes of the BuildServer and BuildAgents available using JMX.

[![Build Status](https://travis-ci.org/rodm/teamcity-jmx-plugin.svg)](https://travis-ci.org/rodm/teamcity-jmx-plugin)
[![Download](https://api.bintray.com/packages/rodm/teamcity-plugins/teamcity-jmx-plugin/images/download.svg)](https://bintray.com/rodm/teamcity-plugins/teamcity-jmx-plugin/_latestVersion)

## How to install

Download the plugin using the link above and follow the instructions from the TeamCity documentation, [Installing Additional Plugins](https://confluence.jetbrains.com/display/TCD9/Installing+Additional+Plugins)

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

## How to configure Munin to monitor TeamCity

See the README in the config/munin directory.
