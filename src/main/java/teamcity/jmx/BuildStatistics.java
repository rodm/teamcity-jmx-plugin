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
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class BuildStatistics extends BuildServerAdapter implements BuildStatisticsMBean {

    private SBuildServer server;
    private BuildFilter filter;

    private LocalDate date = LocalDate.now();
    private AtomicLong buildsStarted = new AtomicLong();
    private AtomicLong buildsFinished = new AtomicLong();
    private AtomicLong buildsInterrupted = new AtomicLong();
    private AtomicLong successfulBuilds = new AtomicLong();
    private AtomicLong failedBuilds = new AtomicLong();
    private AtomicLong ignoredBuilds = new AtomicLong();
    private AtomicLong queueTime = new AtomicLong();
    private AtomicLong buildTime = new AtomicLong();

    public BuildStatistics(SBuildServer server) {
        this(server, new AcceptAllBuildFilter());
    }

    public BuildStatistics(SBuildServer server, BuildFilter filter) {
        this.server = server;
        this.filter = filter;
        this.server.addListener(this);
    }

    @Override
    public long getBuildsStarted() {
        return buildsStarted.get();
    }

    @Override
    public long getBuildsFinished() {
        return buildsFinished.get();
    }

    @Override
    public long getBuildsInterrupted() {
        return buildsInterrupted.get();
    }

    @Override
    public long getSuccessfulBuilds() {
        return successfulBuilds.get();
    }

    @Override
    public long getFailedBuilds() {
        return failedBuilds.get();
    }

    @Override
    public long getIgnoredBuilds() {
        return ignoredBuilds.get();
    }

    @Override
    public long getQueueTime() {
        return queueTime.get();
    }

    @Override
    public long getBuildTime() {
        return buildTime.get();
    }

    @Override
    public void buildStarted(@NotNull SRunningBuild build) {
        if (filter.accept(build)) {
            buildsStarted.incrementAndGet();
        }
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        if (filter.accept(build)) {
            buildsFinished.incrementAndGet();
            Status status = build.getBuildStatus();
            if (status.isSuccessful()) {
                successfulBuilds.incrementAndGet();
            }
            if (status.isFailed()) {
                failedBuilds.incrementAndGet();
            }
            if (status.isIgnored()) {
                ignoredBuilds.incrementAndGet();
            }
            recordTimes(build);
        }
    }

    @Override
    public void buildInterrupted(@NotNull SRunningBuild build) {
        if (filter.accept(build)) {
            buildsInterrupted.incrementAndGet();
            recordTimes(build);
        }
    }

    private void recordTimes(@NotNull SRunningBuild build) {
        SFinishedBuild finishedBuild = server.getHistory().findEntry(build.getBuildId());
        if (finishedBuild != null) {
            Date queuedDate = finishedBuild.getQueuedDate();
            Date startDate = finishedBuild.getStartDate();
            Date finishDate = finishedBuild.getFinishDate();
            queueTime.getAndAdd((startDate.getTime() - queuedDate.getTime()) / 1000);
            buildTime.getAndAdd((finishDate.getTime() - startDate.getTime()) / 1000);
        }
    }

    void writeExternal(@NotNull Element parent) {
        final Element statistics = new Element("build-statistics");
        statistics.setAttribute("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        statistics.setAttribute("started", Long.toString(getBuildsStarted()));
        statistics.setAttribute("finished", Long.toString(getBuildsFinished()));
        statistics.setAttribute("interrupted", Long.toString(getBuildsInterrupted()));
        statistics.setAttribute("successful", Long.toString(getSuccessfulBuilds()));
        statistics.setAttribute("failed", Long.toString(getFailedBuilds()));
        statistics.setAttribute("ignored", Long.toString(getIgnoredBuilds()));
        statistics.setAttribute("queue-time", Long.toString(getQueueTime()));
        statistics.setAttribute("build-time", Long.toString(getBuildTime()));
        parent.addContent(statistics);
    }

    void readExternal(@NotNull Element parent) {
        Element statistics = parent.getChild("build-statistics");
        if (statistics != null) {
            LocalDate savedDate = getDate(statistics);
            if (savedDate != null && savedDate.equals(date)) {
                buildsStarted.set(getValue(statistics, "started"));
                buildsFinished.set(getValue(statistics, "finished"));
                buildsInterrupted.set(getValue(statistics, "interrupted"));
                successfulBuilds.set(getValue(statistics, "successful"));
                failedBuilds.set(getValue(statistics, "failed"));
                ignoredBuilds.set(getValue(statistics, "ignored"));
                queueTime.set(getValue(statistics, "queue-time"));
                buildTime.set(getValue(statistics, "build-time"));
            }
        }
    }

    private LocalDate getDate(@NotNull Element element) {
        String value = element.getAttributeValue("date");
        if (value != null) {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return null;
    }

    private long getValue(@NotNull Element element, String attributeName) {
        String value = element.getAttributeValue(attributeName, "0");
        return Long.parseLong(value);
    }

    void reset() {
        synchronized (this) {
            date = LocalDate.now();
            buildsStarted.set(0);
            buildsFinished.set(0);
            buildsInterrupted.set(0);
            successfulBuilds.set(0);
            failedBuilds.set(0);
            ignoredBuilds.set(0);
            queueTime.set(0);
            buildTime.set(0);
        }
    }
}
