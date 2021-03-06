/*
 * Copyright 2019 Rod MacKenzie.
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
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BuildStatisticsListener extends BuildServerAdapter {

    private SBuildServer server;

    private BuildStatistics serverBuildStatistics = new BuildStatistics();
    private Map<Integer, BuildStatistics> agentBuildStatistics = new HashMap<>();
    private Map<String, BuildStatistics> projectBuildStatistics = new HashMap<>();

    public BuildStatisticsListener(@NotNull SBuildServer server) {
        this.server = server;
        this.server.addListener(this);
    }

    @Override
    public void serverShutdown() {
        this.server.removeListener(this);
    }

    BuildStatistics getServerBuildStatistics() {
        return serverBuildStatistics;
    }

    void reset() {
        serverBuildStatistics.reset();
        agentBuildStatistics.values().forEach(BuildStatistics::reset);
        projectBuildStatistics.values().forEach(BuildStatistics::reset);
    }

    boolean hasAgentBuildStatistics(int agentId) {
        return agentBuildStatistics.containsKey(agentId);
    }

    BuildStatistics getAgentBuildStatistics(int agentId) {
        return agentBuildStatistics.computeIfAbsent(agentId, id -> new BuildStatistics());
    }

    void removeAgentBuildStatistics(int agentId) {
        agentBuildStatistics.remove(agentId);
    }

    boolean hasProjectBuildStatistics(String projectId) {
        return projectBuildStatistics.containsKey(projectId);
    }

    BuildStatistics getProjectBuildStatistics(String projectId) {
        return projectBuildStatistics.computeIfAbsent(projectId, id -> new BuildStatistics());
    }

    void removeProjectBuildStatistics(String projectId) {
        projectBuildStatistics.remove(projectId);
    }

    @Override
    public void buildStarted(@NotNull SRunningBuild build) {
        serverBuildStatistics.buildStarted();
        if (agentBuildStatistics.containsKey(build.getAgentId())) {
            agentBuildStatistics.get(build.getAgentId()).buildStarted();
        }
        if (projectBuildStatistics.containsKey(build.getProjectId())) {
            projectBuildStatistics.get(build.getProjectId()).buildStarted();
        }
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        serverBuildStatistics.buildFinished(build, server);
        if (agentBuildStatistics.containsKey(build.getAgentId())) {
            agentBuildStatistics.get(build.getAgentId()).buildFinished(build, server);
        }
        if (projectBuildStatistics.containsKey(build.getProjectId())) {
            projectBuildStatistics.get(build.getProjectId()).buildFinished(build, server);
        }
    }

    @Override
    public void buildInterrupted(@NotNull SRunningBuild build) {
        serverBuildStatistics.buildInterrupted(build, server);
        if (agentBuildStatistics.containsKey(build.getAgentId())) {
            agentBuildStatistics.get(build.getAgentId()).buildInterrupted(build, server);
        }
        if (projectBuildStatistics.containsKey(build.getProjectId())) {
            projectBuildStatistics.get(build.getProjectId()).buildInterrupted(build, server);
        }
    }
}
