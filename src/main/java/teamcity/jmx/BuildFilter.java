package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuild;

public interface BuildFilter {
    boolean accept(SBuild build);
}
