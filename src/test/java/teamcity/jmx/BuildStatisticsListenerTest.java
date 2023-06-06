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

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BuildStatisticsListenerTest {

    private static final int AGENT_ID = 123;
    private static final String PROJECT_ID = "project123";

    private SBuildServer server;
    private BuildStatisticsListener listener;

    private SRunningBuild DUMMY_BUILD;
    private SRunningBuild SUCCESSFUL_BUILD;

    @Before
    public void setup() {
        server = mock(SBuildServer.class);
        when(server.getHistory()).thenReturn(mock(BuildHistory.class));
        listener = new BuildStatisticsListener(server);

        DUMMY_BUILD = mock(SRunningBuild.class);
        when(DUMMY_BUILD.getAgentId()).thenReturn(AGENT_ID);
        when(DUMMY_BUILD.getProjectId()).thenReturn(PROJECT_ID);

        SUCCESSFUL_BUILD = mock(SRunningBuild.class);
        when(SUCCESSFUL_BUILD.getAgentId()).thenReturn(AGENT_ID);
        when(SUCCESSFUL_BUILD.getProjectId()).thenReturn(PROJECT_ID);
        when(SUCCESSFUL_BUILD.getBuildStatus()).thenReturn(Status.NORMAL);
    }

    @Test
    public void registersWithBuildServer() {
        verify(server).addListener(listener);
    }

    @Test
    public void createsAndReturnsAgentBuildStatistics() {
        BuildStatistics stats = listener.getAgentBuildStatistics(AGENT_ID);

        assertNotNull(stats);
        assertTrue(listener.hasAgentBuildStatistics(AGENT_ID));
    }

    @Test
    public void returnsPreviouslyCreatedBuildStatisticsForAgent() {
        BuildStatistics stats = listener.getAgentBuildStatistics(AGENT_ID);

        assertSame(stats, listener.getAgentBuildStatistics(AGENT_ID));
    }

    @Test
    public void removeAgentBuildStatistics() {
        listener.getAgentBuildStatistics(AGENT_ID);

        listener.removeAgentBuildStatistics(AGENT_ID);

        assertFalse(listener.hasAgentBuildStatistics(AGENT_ID));
    }

    @Test
    public void createsAndReturnsProjectBuildStatistics() {
        BuildStatistics stats = listener.getProjectBuildStatistics(PROJECT_ID);

        assertNotNull(stats);
        assertTrue(listener.hasProjectBuildStatistics(PROJECT_ID));
    }

    @Test
    public void returnsPreviouslyCreatedBuildStatisticsForProject() {
        BuildStatistics stats = listener.getProjectBuildStatistics(PROJECT_ID);

        assertSame(stats, listener.getProjectBuildStatistics(PROJECT_ID));
    }

    @Test
    public void removeProjectBuildStatistics() {
        listener.getProjectBuildStatistics(PROJECT_ID);

        listener.removeProjectBuildStatistics(PROJECT_ID);

        assertFalse(listener.hasProjectBuildStatistics(PROJECT_ID));
    }

    @Test
    public void recordBuildStartedInServerBuildStatistics() {
        listener.buildStarted(DUMMY_BUILD);

        assertEquals(1, listener.getServerBuildStatistics().getBuildsStarted());
    }

    @Test
    public void recordBuildStartedInAgentBuildStatistics() {
        listener.getAgentBuildStatistics(AGENT_ID);

        listener.buildStarted(DUMMY_BUILD);

        assertEquals(1, listener.getAgentBuildStatistics(AGENT_ID).getBuildsStarted());
    }

    @Test
    public void recordBuildStartedInProjectBuildStatistics() {
        listener.getProjectBuildStatistics(PROJECT_ID);

        listener.buildStarted(DUMMY_BUILD);

        assertEquals(1, listener.getProjectBuildStatistics(PROJECT_ID).getBuildsStarted());
    }

    @Test
    public void recordBuildFinishedInServerBuildStatistics() {
        listener.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(1, listener.getServerBuildStatistics().getBuildsFinished());
    }

    @Test
    public void recordBuildFinishedInAgentBuildStatistics() {
        listener.getAgentBuildStatistics(AGENT_ID);

        listener.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(1, listener.getAgentBuildStatistics(AGENT_ID).getBuildsFinished());
    }

    @Test
    public void recordBuildFinishedInProjectBuildStatistics() {
        listener.getProjectBuildStatistics(PROJECT_ID);

        listener.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(1, listener.getProjectBuildStatistics(PROJECT_ID).getBuildsFinished());
    }

    @Test
    public void recordBuildInterruptedInServerBuildStatistics() {
        listener.buildInterrupted(DUMMY_BUILD);

        assertEquals(1, listener.getServerBuildStatistics().getBuildsInterrupted());
    }

    @Test
    public void recordBuildInterruptedInAgentBuildStatistics() {
        listener.getAgentBuildStatistics(AGENT_ID);

        listener.buildInterrupted(DUMMY_BUILD);

        assertEquals(1, listener.getAgentBuildStatistics(AGENT_ID).getBuildsInterrupted());
    }

    @Test
    public void recordBuildInterruptedInProjectBuildStatistics() {
        listener.getProjectBuildStatistics(PROJECT_ID);

        listener.buildInterrupted(DUMMY_BUILD);

        assertEquals(1, listener.getProjectBuildStatistics(PROJECT_ID).getBuildsInterrupted());
    }

    @Test
    public void resetAllBuildStatistics() {
        listener.getAgentBuildStatistics(AGENT_ID);
        listener.getProjectBuildStatistics(PROJECT_ID);
        listener.buildFinished(SUCCESSFUL_BUILD);
        listener.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(2, listener.getServerBuildStatistics().getBuildsFinished());
        assertEquals(2, listener.getAgentBuildStatistics(AGENT_ID).getBuildsFinished());
        assertEquals(2, listener.getProjectBuildStatistics(PROJECT_ID).getBuildsFinished());

        listener.reset();

        assertEquals(0, listener.getServerBuildStatistics().getBuildsFinished());
        assertEquals(0, listener.getAgentBuildStatistics(AGENT_ID).getBuildsFinished());
        assertEquals(0, listener.getProjectBuildStatistics(PROJECT_ID).getBuildsFinished());
    }
}
