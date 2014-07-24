## Configuring Munin to monitor TeamCity using the JMX Plugin

Configure TeamCity to enable remote JMX, the following properties can be added to one of the following environment
variables TEAMCITY_SERVER_OPTS, CATALINA_OPTS or JAVA_OPTS in the teamcity-server.sh script. Change `PORT` to a port
number to use for remote JMX access, e.g. 8117

    -Dcom.sun.management.jmxremote
    -Dcom.sun.management.jmxremote.port=<PORT>
    -Dcom.sun.management.jmxremote.ssl=false
    -Dcom.sun.management.jmxremote.authenticate=false

Install the munin package

    apt-get install -y munin-node

Download the jmxquery.jar from [https://raw.githubusercontent.com/munin-monitoring/contrib/master/plugins/java/jmx/plugin/jmxquery.jar]

Create the directory `/usr/local/munin/plugins` and copy the jar file to it.

Copy the files jmx_, teamcity_cleanup and teamcity_load to the directory `/usr/local/munin/plugins`

Edit the jmx_ file to set the JAVA_HOME variable to a Java installation.

Add the following configuration to the file `/etc/munin/plugin-conf.d/munin-node` 

    [jmx_*]
    user root
    timeout 30
    env.jmxurl service:jmx:rmi:///jndi/rmi://localhost:<PORT>/jmxrmi

Change the value `PORT` in the above jmxurl to match the port that TeamCity remote JMX is configured to use.

Create the following symbolic links to the jmx_ script in /usr/local/munin/plugins

    ln -s /usr/local/munin/plugins/jmx_ /etc/munin/plugins/jmx_teamcity_cleanup
    ln -s /usr/local/munin/plugins/jmx_ /etc/munin/plugins/jmx_teamcity_load

Restart the munin-node service

    service munin-node restart

## Examples

![TeamCity load for a day](https://bitbucket.org/rodm/teamcity-jmx-plugin/raw/default/config/munin/teamcity-queue-day.png "TeamCity load")

![TeamCity load for a week](https://bitbucket.org/rodm/teamcity-jmx-plugin/raw/default/config/munin/teamcity-queue-week.png "TeamCity load")

![TeamCity cleanup times for a month](https://bitbucket.org/rodm/teamcity-jmx-plugin/raw/default/config/munin/teamcity-cleanup-month.png "TeamCity cleanup")
