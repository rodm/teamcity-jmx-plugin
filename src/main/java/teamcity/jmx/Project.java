package teamcity.jmx;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

import java.util.List;

public class Project implements ProjectMBean {

    private SProject serverProject;

    private String name;

    private int successfulBuildTypes = 0;

    private int failedBuildTypes = 0;

    private int pausedBuildTypes = 0;

    public Project(final SProject project) {
        this.serverProject = project;
        this.name = project.getName();
        update();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getNumberOfBuildTypes() {
        return serverProject.getOwnBuildTypes().size();
    }

    @Override
    public int getNumberOfBuildTypeTemplates() {
        return serverProject.getOwnBuildTypeTemplates().size();
    }

    @Override
    public int getNumberOfSubProjects() {
        return serverProject.getOwnProjects().size();
    }

    @Override
    public int getNumberOfVcsRoots() {
        return serverProject.getOwnVcsRoots().size();
    }

    @Override
    public int getNumberOfSuccessfulBuildTypes() {
        return successfulBuildTypes;
    }

    @Override
    public int getNumberOfFailedBuildTypes() {
        return failedBuildTypes;
    }

    @Override
    public int getNumberOfPausedBuildTypes() {
        return pausedBuildTypes;
    }

    @Override
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
        List<SBuildType> buildTypes = serverProject.getOwnBuildTypes();
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
