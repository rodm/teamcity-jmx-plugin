package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BuildStatisticsTest {

    private SBuildServer server;
    private BuildStatistics stats;
    private SRunningBuild DUMMY_BUILD = null;

    @Before
    public void setup() {
        server = mock(SBuildServer.class);
        stats = new BuildStatistics(server);
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
        stats.buildFinished(DUMMY_BUILD);

        assertEquals(1, stats.getBuildsFinished());
    }

    @Test
    public void shouldRecordNumberOfBuildsInterrupted() {
        stats.buildInterrupted(DUMMY_BUILD);

        assertEquals(1, stats.getBuildsInterrupted());
    }
}
