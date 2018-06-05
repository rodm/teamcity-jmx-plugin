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
        filter = new AgentBuildFilter(123);

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
