package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuild;

public class AcceptAllBuildFilter implements BuildFilter {
    @Override
    public boolean accept(SBuild build) {
        return true;
    }
}
