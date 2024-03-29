/*   Copyright 2004-2019 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.LogFileProxy;
import com.qumasoft.qvcslib.LogfileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get logfile info response.
 * @author Jim Voris
 */
public class ServerResponseGetLogfileInfo extends AbstractServerResponse {
    private static final long serialVersionUID = 8974102664326046228L;

    // These are serialized:
    private String projectName = null;
    private String branchName = null;
    private String appendedPath = null;
    private String shortWorkfileName = null;
    private int correlationID = 0;
    // Send back the full logfile info
    private LogfileInfo logfileInfo = null;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResponseGetLogfileInfo.class);

    /**
     * Creates new ServerResponseGetLogfileInfo.
     */
    public ServerResponseGetLogfileInfo() {
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     * @param project the project name.
     */
    public void setProjectName(String project) {
        projectName = project;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Set the branch name.
     * @param branch the branch name.
     */
    public void setBranchName(final String branch) {
        branchName = branch;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setAppendedPath(String path) {
        appendedPath = path;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setShortWorkfileName(String shortName) {
        shortWorkfileName = shortName;
    }

    /**
     * Get the correlation ID.
     * @return the correlation ID.
     */
    public int getCorrelationID() {
        return correlationID;
    }

    /**
     * Set the correlation ID.
     * @param corrID the correlation ID.
     */
    public void setCorrelationID(int corrID) {
        correlationID = corrID;
    }

    /**
     * Get the logfile info.
     * @return the logfile info.
     */
    public LogfileInfo getLogfileInfo() {
        return logfileInfo;
    }

    /**
     * Set the logfile info.
     * @param info the logfile info.
     */
    public void setLogfileInfo(LogfileInfo info) {
        logfileInfo = info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        StringBuilder message = new StringBuilder("Got detailed info for ");
        message.append(getShortWorkfileName());
        message.append(" from project directory: ");
        message.append(getAppendedPath());

        LogFileProxy logFileProxy = (LogFileProxy) directoryManagerProxy.getArchiveInfo(getShortWorkfileName());
        logFileProxy.setLogfileInfo(getLogfileInfo());

        // Log this.
        LOGGER.trace(message.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_LOGFILE_INFO;
    }
}
