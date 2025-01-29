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

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuildStatisticsTest {

    private static final long BUILD_ID = 1234;

    private SBuildServer server;
    private BuildHistory history;
    private BuildStatistics stats;
    private SRunningBuild DUMMY_BUILD;
    private SRunningBuild SUCCESSFUL_BUILD;
    private SRunningBuild FAILED_BUILD;
    private SRunningBuild RUNNING_BUILD;

    @BeforeEach
    public void setup() {
        server = mock(SBuildServer.class);
        history = mock(BuildHistory.class);
        stats = new BuildStatistics();

        when(server.getHistory()).thenReturn(history);

        DUMMY_BUILD = mock(SRunningBuild.class);
        SUCCESSFUL_BUILD = mock(SRunningBuild.class);
        when(SUCCESSFUL_BUILD.getBuildStatus()).thenReturn(Status.NORMAL);
        FAILED_BUILD = mock(SRunningBuild.class);
        when(FAILED_BUILD.getBuildStatus()).thenReturn(Status.FAILURE);
        RUNNING_BUILD = mock(SRunningBuild.class);
        when(RUNNING_BUILD.getBuildId()).thenReturn(BUILD_ID);
        when(RUNNING_BUILD.getBuildStatus()).thenReturn(Status.NORMAL);
    }

    @Test
    public void shouldRecordNumberOfBuildsStarted() {
        stats.buildStarted();

        assertEquals(1, stats.getBuildsStarted());
    }

    @Test
    public void shouldRecordNumberOfBuildsFinished() {
        stats.buildFinished(SUCCESSFUL_BUILD, server);

        assertEquals(1, stats.getBuildsFinished());
    }

    @Test
    public void shouldRecordNumberOfBuildsInterrupted() {
        stats.buildInterrupted(DUMMY_BUILD, server);

        assertEquals(1, stats.getBuildsInterrupted());
    }

    @Test
    public void shouldRecordSuccessfulBuilds() {
        stats.buildFinished(SUCCESSFUL_BUILD, server);

        assertEquals(1, stats.getSuccessfulBuilds());
    }

    @Test
    public void shouldNotRecordUnsuccessfulBuilds() {
        stats.buildFinished(FAILED_BUILD, server);

        assertEquals(0, stats.getSuccessfulBuilds());
    }

    @Test
    public void shouldRecordFailedBuilds() {
        stats.buildFinished(FAILED_BUILD, server);

        assertEquals(1, stats.getFailedBuilds());
    }

    @Test
    public void shouldNotRecordSuccessfulBuildsAsFailed() {
        stats.buildFinished(SUCCESSFUL_BUILD, server);

        assertEquals(0, stats.getFailedBuilds());
    }

    @Test
    public void recordQueueTimeOfFinishedBuild() {
        SFinishedBuild finishedBuild = createFinishedBuild(15, 10);
        when(history.findEntry(BUILD_ID)).thenReturn(finishedBuild);

        stats.buildFinished(RUNNING_BUILD, server);

        assertEquals(15, stats.getQueueTime());
    }

    @Test
    public void recordBuildTimeOfFinishedBuild() {
        SFinishedBuild finishedBuild = createFinishedBuild(15, 10);
        when(history.findEntry(BUILD_ID)).thenReturn(finishedBuild);

        stats.buildFinished(RUNNING_BUILD, server);

        assertEquals(10, stats.getBuildTime());
    }

    @Test
    public void recordQueueTimeOfInterruptedBuild() {
        SFinishedBuild finishedBuild = createFinishedBuild(10, 5);
        when(history.findEntry(BUILD_ID)).thenReturn(finishedBuild);

        stats.buildInterrupted(RUNNING_BUILD, server);

        assertEquals(10, stats.getQueueTime());
    }

    @Test
    public void recordBuildTimeOfInterruptedBuild() {
        SFinishedBuild finishedBuild = createFinishedBuild(10, 5);
        when(history.findEntry(BUILD_ID)).thenReturn(finishedBuild);

        stats.buildInterrupted(RUNNING_BUILD, server);

        assertEquals(5, stats.getBuildTime());
    }

    private SFinishedBuild createFinishedBuild(int queueTime, int buildTime) {
        long time = System.currentTimeMillis();
        Date queuedDate = new Date(time);
        Date startDate = new Date(time + (queueTime * 1000L));
        Date finishDate = new Date(time + ((queueTime + buildTime) * 1000L));
        SFinishedBuild finishedBuild = mock((SFinishedBuild.class));
        when(finishedBuild.getQueuedDate()).thenReturn(queuedDate);
        when(finishedBuild.getStartDate()).thenReturn(startDate);
        when(finishedBuild.getFinishDate()).thenReturn(finishDate);
        return finishedBuild;
    }
}
