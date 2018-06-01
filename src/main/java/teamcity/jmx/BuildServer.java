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

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;

public class BuildServer extends BuildServerAdapter implements BuildServerMBean {

    private SBuildServer server;

    private long cleanupStartTime = 0;

    private long cleanupDuration = 0;

    public BuildServer(SBuildServer server) {
        this.server = server;
        this.server.addListener(this);
    }

    @Override
    public int getRegisteredAgents() {
        return server.getBuildAgentManager().getRegisteredAgents().size();
    }

    @Override
    public int getUnregisteredAgents() {
        return server.getBuildAgentManager().getUnregisteredAgents().size();
    }

    @Override
    public int getUnauthorizedAgents() {
        int allAgents = server.getBuildAgentManager().getRegisteredAgents(true).size();
        return allAgents - getRegisteredAgents();
    }

    @Override
    public int getNumberOfRunningBuilds() {
        return server.getNumberOfRunningBuilds();
    }

    @Override
    public int getBuildQueueSize() {
        return server.getQueue().getNumberOfItems();
    }

    @Override
    public String getFullServerVersion() {
        return server.getFullServerVersion();
    }

    @Override
    public int getNumberOfRegisteredUsers() {
        return server.getUserModel().getNumberOfRegisteredUsers();
    }

    @Override
    public int getNumberOfProjects() {
        return server.getProjectManager().getNumberOfProjects();
    }

    @Override
    public int getNumberOfBuildTypes() {
        return server.getProjectManager().getNumberOfBuildTypes();
    }

    @Override
    public long getCleanupDuration() {
        return cleanupDuration;
    }

    @Override
    public void cleanupStarted() {
        cleanupStartTime = System.currentTimeMillis();
    }

    @Override
    public void cleanupFinished() {
        cleanupDuration = (System.currentTimeMillis() - cleanupStartTime) / 1000;
    }

    long getCleanupStartTime() {
        return cleanupStartTime;
    }

    void setCleanupStartTime(long startTime) {
        cleanupStartTime = startTime;
    }

    void setCleanupDuration(long duration) {
        cleanupDuration = duration;
    }
}
