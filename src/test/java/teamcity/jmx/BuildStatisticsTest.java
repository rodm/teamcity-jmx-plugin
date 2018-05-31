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
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BuildStatisticsTest {

    private static final long BUILD_ID = 1234;

    private SBuildServer server;
    private BuildHistory history;
    private BuildStatistics stats;
    private SRunningBuild DUMMY_BUILD;
    private SRunningBuild SUCCESSFUL_BUILD;
    private SRunningBuild FAILED_BUILD;
    private SRunningBuild IGNORED_BUILD;
    private SRunningBuild RUNNING_BUILD;

    @Before
    public void setup() {
        server = mock(SBuildServer.class);
        history = mock(BuildHistory.class);
        stats = new BuildStatistics(server);

        when(server.getHistory()).thenReturn(history);

        DUMMY_BUILD = mock(SRunningBuild.class);
        SUCCESSFUL_BUILD = mock(SRunningBuild.class);
        when(SUCCESSFUL_BUILD.getBuildStatus()).thenReturn(Status.NORMAL);
        FAILED_BUILD = mock(SRunningBuild.class);
        when(FAILED_BUILD.getBuildStatus()).thenReturn(Status.FAILURE);
        IGNORED_BUILD = mock(SRunningBuild.class);
        when(IGNORED_BUILD.getBuildStatus()).thenReturn(Status.UNKNOWN);
        RUNNING_BUILD = mock(SRunningBuild.class);
        when(RUNNING_BUILD.getBuildId()).thenReturn(BUILD_ID);
        when(RUNNING_BUILD.getBuildStatus()).thenReturn(Status.NORMAL);
    }

    @Test
    public void shouldRegisterWithBuildServerToReceiveEvents() {
        verify(server).addListener(stats);
    }

    @Test
    public void shouldRecordNumberOfBuildsStarted() {
        stats.buildStarted(DUMMY_BUILD);

        assertEquals(1, stats.getBuildsStarted());
    }

    @Test
    public void shouldRecordNumberOfBuildsFinished() {
        stats.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(1, stats.getBuildsFinished());
    }

    @Test
    public void shouldRecordNumberOfBuildsInterrupted() {
        stats.buildInterrupted(DUMMY_BUILD);

        assertEquals(1, stats.getBuildsInterrupted());
    }

    @Test
    public void shouldRecordSuccessfulBuilds() {
        stats.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(1, stats.getSuccessfulBuilds());
    }

    @Test
    public void shouldNotRecordUnsuccessfulBuilds() {
        stats.buildFinished(FAILED_BUILD);

        assertEquals(0, stats.getSuccessfulBuilds());
    }

    @Test
    public void shouldRecordFailedBuilds() {
        stats.buildFinished(FAILED_BUILD);

        assertEquals(1, stats.getFailedBuilds());
    }

    @Test
    public void shouldNotRecordSuccessfulBuildsAsFailed() {
        stats.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(0, stats.getFailedBuilds());
    }

    @Test
    public void shouldRecordIgnoredBuilds() {
        stats.buildFinished(IGNORED_BUILD);

        assertEquals(1, stats.getIgnoredBuilds());
    }

    @Test
    public void shouldNotRecordSuccessfulBuildsAsIgnored() {
        stats.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(0, stats.getIgnoredBuilds());
    }

    @Test
    public void shouldNotRecordFailedBuildsAsIgnored() {
        stats.buildFinished(FAILED_BUILD);

        assertEquals(0, stats.getIgnoredBuilds());
    }

    @Test
    public void shouldDelegateRecordingBuildStartedToFilter() {
        BuildFilter filter = mock(BuildFilter.class);
        stats = new BuildStatistics(server, filter);

        stats.buildStarted(SUCCESSFUL_BUILD);

        verify(filter).accept(eq(SUCCESSFUL_BUILD));
    }

    @Test
    public void shouldNotRecordBuildStartedWhenRejectedByFilter() {
        BuildFilter filter = mock(BuildFilter.class);
        when(filter.accept(any(SBuild.class))).thenReturn(false);
        stats = new BuildStatistics(server, filter);

        stats.buildStarted(DUMMY_BUILD);

        assertEquals(0, stats.getBuildsStarted());
    }

    @Test
    public void shouldDelegateRecordingBuildFinishedToFilter() {
        BuildFilter filter = mock(BuildFilter.class);
        stats = new BuildStatistics(server, filter);

        stats.buildFinished(SUCCESSFUL_BUILD);

        verify(filter).accept(eq(SUCCESSFUL_BUILD));
    }


    @Test
    public void shouldNotRecordBuildFinishedWhenRejectedByFilter() {
        BuildFilter filter = mock(BuildFilter.class);
        when(filter.accept(any(SBuild.class))).thenReturn(false);
        stats = new BuildStatistics(server, filter);

        stats.buildFinished(SUCCESSFUL_BUILD);

        assertEquals(0, stats.getBuildsFinished());
    }

    @Test
    public void shouldDelegateRecordingBuildInterruptedToFilter() {
        BuildFilter filter = mock(BuildFilter.class);
        stats = new BuildStatistics(server, filter);

        stats.buildInterrupted(SUCCESSFUL_BUILD);

        verify(filter).accept(eq(SUCCESSFUL_BUILD));
    }

    @Test
    public void shouldNotRecordBuildInterruptedWhenRejectedByFilter() {
        BuildFilter filter = mock(BuildFilter.class);
        when(filter.accept(any(SBuild.class))).thenReturn(false);
        stats = new BuildStatistics(server, filter);

        stats.buildInterrupted(SUCCESSFUL_BUILD);

        assertEquals(0, stats.getBuildsInterrupted());
    }

    @Test
    public void recordQueueTimeOfFinishedBuild() {
        SFinishedBuild finishedBuild = createFinishedBuild(15, 10);
        when(history.findEntry(BUILD_ID)).thenReturn(finishedBuild);

        stats.buildFinished(RUNNING_BUILD);

        assertEquals(15, stats.getQueueTime());
    }

    @Test
    public void recordBuildTimeOfFinishedBuild() {
        SFinishedBuild finishedBuild = createFinishedBuild(15, 10);
        when(history.findEntry(BUILD_ID)).thenReturn(finishedBuild);

        stats.buildFinished(RUNNING_BUILD);

        assertEquals(10, stats.getBuildTime());
    }

    @Test
    public void recordQueueTimeOfInterruptedBuild() {
        SFinishedBuild finishedBuild = createFinishedBuild(10, 5);
        when(history.findEntry(BUILD_ID)).thenReturn(finishedBuild);

        stats.buildInterrupted(RUNNING_BUILD);

        assertEquals(10, stats.getQueueTime());
    }

    @Test
    public void recordBuildTimeOfInterruptedBuild() {
        SFinishedBuild finishedBuild = createFinishedBuild(10, 5);
        when(history.findEntry(BUILD_ID)).thenReturn(finishedBuild);

        stats.buildInterrupted(RUNNING_BUILD);

        assertEquals(5, stats.getBuildTime());
    }

    private SFinishedBuild createFinishedBuild(int queueTime, int buildTime) {
        long time = System.currentTimeMillis();
        Date queuedDate = new Date(time);
        Date startDate = new Date(time + (queueTime * 1000));
        Date finishDate = new Date(time + ((queueTime + buildTime) * 1000));
        SFinishedBuild finishedBuild = mock((SFinishedBuild.class));
        when(finishedBuild.getQueuedDate()).thenReturn(queuedDate);
        when(finishedBuild.getStartDate()).thenReturn(startDate);
        when(finishedBuild.getFinishDate()).thenReturn(finishDate);
        return finishedBuild;
    }
}
