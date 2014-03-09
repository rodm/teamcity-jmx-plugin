package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildAgent;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AgentBuildFilterTest {

    private SBuildAgent buildAgent;
    private BuildFilter filter;
    private SBuild build;

    @Before
    public void setup() {
        SBuildAgent statsAgent = mock(SBuildAgent.class);
        when(statsAgent.getId()).thenReturn(123);
        filter = new AgentBuildFilter(statsAgent);

        buildAgent = mock(SBuildAgent.class);
        build = mock(SBuild.class);
        when(build.getAgent()).thenReturn(buildAgent);
    }

    @Test
    public void shouldAcceptBuildsWhenAgentIdMatches() {
        when(buildAgent.getId()).thenReturn(123);

        assertThat(filter.accept(build), is(true));
    }

    @Test
    public void shouldRejectBuildsWhenAgentIdDiffers() {
        when(buildAgent.getId()).thenReturn(124);

        assertThat(filter.accept(build), is(false));
    }
}
