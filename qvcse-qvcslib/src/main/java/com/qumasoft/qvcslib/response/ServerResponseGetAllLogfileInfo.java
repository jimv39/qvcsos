/*
 * Copyright 2021 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.LogfileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class ServerResponseGetAllLogfileInfo extends AbstractServerResponse {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResponseGetAllLogfileInfo.class);

    // These are serialized:
    private String projectName = null;
    private String branchName = null;
    private String appendedPath = null;
    private String shortWorkfileName = null;
    // Send back the full logfile info
    private LogfileInfo logfileInfo = null;

    /**
     * Creates new ServerResponseGetAllLogfileInfo.
     */
    public ServerResponseGetAllLogfileInfo() {
    }

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_ALL_LOGFILE_INFO;
    }

    /**
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param name the projectName to set
     */
    public void setProjectName(String name) {
        this.projectName = name;
    }

    /**
     * @return the branchName
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * @param name the branchName to set
     */
    public void setBranchName(String name) {
        this.branchName = name;
    }

    /**
     * @return the appendedPath
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * @param path the appendedPath to set
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
    }

    /**
     * @return the shortWorkfileName
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * @param name the shortWorkfileName to set
     */
    public void setShortWorkfileName(String name) {
        this.shortWorkfileName = name;
    }

    /**
     * @return the logfileInfo
     */
    public LogfileInfo getLogfileInfo() {
        return logfileInfo;
    }

    /**
     * @param info the logfileInfo to set
     */
    public void setLogfileInfo(LogfileInfo info) {
        this.logfileInfo = info;
    }

}
