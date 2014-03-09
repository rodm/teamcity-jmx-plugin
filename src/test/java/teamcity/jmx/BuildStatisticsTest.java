package teamcity.jmx;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BuildStatisticsTest {

    private SBuildServer server;
    private BuildStatistics stats;
    private SRunningBuild DUMMY_BUILD = null;
    private SRunningBuild SUCCESSFUL_BUILD;
    private SRunningBuild FAILED_BUILD;
    private SRunningBuild IGNORED_BUILD;

    @Before
    public void setup() {
        server = mock(SBuildServer.class);
        stats = new BuildStatistics(server);

        SUCCESSFUL_BUILD = mock(SRunningBuild.class);
        when(SUCCESSFUL_BUILD.getBuildStatus()).thenReturn(Status.NORMAL);
        FAILED_BUILD = mock(SRunningBuild.class);
        when(FAILED_BUILD.getBuildStatus()).thenReturn(Status.FAILURE);
        IGNORED_BUILD = mock(SRunningBuild.class);
        when(IGNORED_BUILD.getBuildStatus()).thenReturn(Status.UNKNOWN);
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
}
