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

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Project implements ProjectMBean {

    private SProject serverProject;

    private String name;

    private AtomicInteger successfulBuildTypes = new AtomicInteger();

    private AtomicInteger failedBuildTypes = new AtomicInteger();

    private AtomicInteger pausedBuildTypes = new AtomicInteger();

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
        return successfulBuildTypes.get();
    }

    @Override
    public int getNumberOfFailedBuildTypes() {
        return failedBuildTypes.get();
    }

    @Override
    public int getNumberOfPausedBuildTypes() {
        return pausedBuildTypes.get();
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
        this.successfulBuildTypes.set(successCount);
        this.failedBuildTypes.set(failedCount);
        this.pausedBuildTypes.set(pausedCount);
    }
}
