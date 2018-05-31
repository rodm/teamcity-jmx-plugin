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
import jetbrains.buildServer.serverSide.SBuildAgent;

import java.util.Date;

public class Agent implements AgentMBean {

    private int agentId;

    private BuildAgentManager buildAgentManager;

    public Agent(SBuildAgent buildAgent, BuildAgentManager buildAgentManager) {
        this.agentId = buildAgent.getId();
        this.buildAgentManager = buildAgentManager;
    }

    @Override
    public String getHostName() {
        return getBuildAgent().getHostName();
    }

    @Override
    public String getHostAddress() {
        return getBuildAgent().getHostAddress();
    }

    @Override
    public int getPort() {
        return getBuildAgent().getPort();
    }

    @Override
    public String getOperatingSystemName() {
        return getBuildAgent().getOperatingSystemName();
    }

    @Override
    public boolean isRegistered() {
        return getBuildAgent().isRegistered();
    }

    @Override
    public String getUnregistrationComment() {
        return getBuildAgent().getUnregistrationComment();
    }

    @Override
    public Date getRegistrationTimestamp() {
        return getBuildAgent().getRegistrationTimestamp();
    }

    @Override
    public Date getLastCommunicationTimestamp() {
        return getBuildAgent().getLastCommunicationTimestamp();
    }

    @Override
    public boolean isEnabled() {
        return getBuildAgent().isEnabled();
    }

    @Override
    public String getStatusComment() {
        return getBuildAgent().getStatusComment().getComment();
    }

    @Override
    public boolean isAuthorized() {
        return getBuildAgent().isAuthorized();
    }

    @Override
    public String getAuthorizeComment() {
        return getBuildAgent().getAuthorizeComment().getComment();
    }

    @Override
    public int getCpuBenchmarkIndex() {
        return getBuildAgent().getCpuBenchmarkIndex();
    }

    @Override
    public int getNumberOfCompatibleConfigurations() {
        return buildAgentManager.getNumberOfCompatibleConfigurations(getBuildAgent());
    }

    @Override
    public int getNumberOfIncompatibleConfigurations() {
        return buildAgentManager.getNumberOfIncompatibleConfigurations(getBuildAgent());
    }

    private SBuildAgent getBuildAgent() {
        return buildAgentManager.findAgentById(agentId, true);
    }
}
