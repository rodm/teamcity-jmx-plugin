package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

import java.util.List;

public class Project implements ProjectMBean {

    private SProject project;

    private String name;

    private int successfulBuildTypes = 0;

    private int failedBuildTypes = 0;

    private int pausedBuildTypes = 0;

    public Project(final SProject project) {
        this.project = project;
        this.name = project.getName();
        update();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int getNumberOfBuildTypes() {
        return project.getBuildTypes().size();
    }

    public int getNumberOfSuccessfulBuildTypes() {
        return successfulBuildTypes;
    }

    public int getNumberOfFailedBuildTypes() {
        return failedBuildTypes;
    }

    public int getNumberOfPausedBuildTypes() {
        return pausedBuildTypes;
    }

    public int getSuccessPercentage() {
        if (getNumberOfBuildTypes() > 0) {
            return (successfulBuildTypes / getNumberOfBuildTypes()) * 100;
        }
        return 0;
    }

    public void update() {
        int successCount = 0;
        int failedCount = 0;
        int pausedCount = 0;
        List<SBuildType> buildTypes = project.getBuildTypes();
        for (SBuildType buildType : buildTypes) {
            if (buildType.isPaused()) {
                pausedCount++;
            } else {
                if (buildType.getStatus().isSuccessful()) {
                    successCount++;
                } else {
                    failedCount++;
                }
            }
        }
        this.successfulBuildTypes = successCount;
        this.failedBuildTypes = failedCount;
        this.pausedBuildTypes = pausedCount;
    }
}
