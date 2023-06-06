/*
 * Copyright 2020 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcity.jmx;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.FileUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static jetbrains.buildServer.log.Loggers.SERVER_CATEGORY;

public abstract class PluginStateStore extends BuildServerAdapter {

    private static final Logger LOGGER = Logger.getLogger(SERVER_CATEGORY + "." + PluginStateStore.class.getSimpleName());

    private final File stateFile;

    protected PluginStateStore(@NotNull SBuildServer server, @NotNull ServerPaths serverPaths) {
        stateFile = new File(getPluginDataDir(serverPaths), "state.xml");
        server.addListener(this);
    }

    protected abstract String getPluginName();

    protected abstract void writeExternal(@NotNull Element element);

    protected abstract void readExternal(@NotNull Element element);

    @Override
    public void serverStartup() {
        loadState();
    }

    @Override
    public void serverShutdown() {
        saveState();
    }

    protected void loadState() {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new FileReader(stateFile));
            Element root = document.getRootElement();
            if (root != null) {
                readExternal(root);
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to read \"" + getPluginName() + "\" plugin state from the \"" + stateFile.getAbsolutePath() + "\" file: " + e, e);
        }
    }

    protected void saveState() {
        try {
            Element root = new Element("root");
            writeExternal(root);
            XMLOutputter xmlWriter = new XMLOutputter(Format.getPrettyFormat());
            xmlWriter.output(root, new FileWriter(stateFile));
        }
        catch (Exception e) {
            LOGGER.error("Failed to write \"" + getPluginName() + "\" plugin state to the \"" + stateFile.getAbsolutePath() + "\" file: " + e, e);
        }
    }

    private File getPluginDataDir(ServerPaths serverPaths) {
        try {
            return FileUtil.createDir(new File(serverPaths.getPluginDataDirectory(), getPluginName()));
        }
        catch (IOException e) {
            throw new PluginStateException("Failed to create plugin data directory", e);
        }
    }
}
