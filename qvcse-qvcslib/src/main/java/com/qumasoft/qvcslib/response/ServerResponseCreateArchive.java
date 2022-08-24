/*   Copyright 2004-2022 Jim Voris
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
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.ClientWorkfileCache;
import com.qumasoft.qvcslib.LogFileProxy;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.WorkfileDirectoryManagerInterface;
import com.qumasoft.qvcslib.WorkfileInfo;
import com.qumasoft.qvcslib.WorkfileInfoInterface;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create archive response.
 * @author Jim Voris
 */
public class ServerResponseCreateArchive extends AbstractServerResponse {
    private static final long serialVersionUID = 7665852584665421137L;

    // This is what gets serialized.
    private String appendedPath;
    private String projectName;
    private String branchName;
    // Send back the full logfile info and skinny info too.
    private LogfileInfo logfileInfo = null;
    private SkinnyLogfileInfo skinnyLogfileInfo = null;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResponseCreateArchive.class);

    /**
     * Creates a new instance of ServerResponseCreateArchive.
     */
    public ServerResponseCreateArchive() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        directoryManagerProxy.updateArchiveInfo(getSkinnyLogfileInfo().getShortWorkfileName(), getSkinnyLogfileInfo());

        // Move the workfile buffer into the size limited workfile cache.
        byte[] buffer = ClientWorkfileCache.getInstance().getContractedBuffer(getSkinnyLogfileInfo().getCacheIndex(), getSkinnyLogfileInfo().getDefaultRevisionString());

        WorkfileDirectoryManagerInterface workfileDirectoryManager = directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager();
        WorkfileInfoInterface workfileInfo = workfileDirectoryManager.lookupWorkfileInfo(getSkinnyLogfileInfo().getShortWorkfileName());
        File workfile = workfileInfo.getWorkfile();

        try {
            // Update the workfileInfo in the directory manager so we'll be able to compute
            // a useful status.
            String fullWorkfileName = workfileDirectoryManager.getWorkfileDirectory() + File.separator + getSkinnyLogfileInfo().getShortWorkfileName();
            WorkfileInfo newWorkfileInfo = new WorkfileInfo(fullWorkfileName, getSkinnyLogfileInfo()
                    .getAttributes().getIsBinaryfile(), getProjectName(), getBranchName());
            newWorkfileInfo.setFetchedDate(workfile.lastModified());
            newWorkfileInfo.setWorkfileRevisionString(getSkinnyLogfileInfo().getDefaultRevisionString());

            // Set the archiveInfo on the workfileInfo object.
            ArchiveInfoInterface archiveInfo = directoryManagerProxy.getArchiveInfo(getSkinnyLogfileInfo().getShortWorkfileName());
            LogFileProxy logFileProxy = (LogFileProxy) archiveInfo;
            logFileProxy.setLogfileInfo(getLogfileInfo());
            newWorkfileInfo.setArchiveInfo(archiveInfo);

            workfileDirectoryManager.updateWorkfileInfo(newWorkfileInfo);

            // Set workfile attributes.
            if (getSkinnyLogfileInfo().getAttributes().getIsProtectWorkfile()) {
                workfile.setReadOnly();
            }
        } catch (IOException | QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
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
     * Get the skinny logfile info.
     * @return the skinny logfile info.
     */
    public SkinnyLogfileInfo getSkinnyLogfileInfo() {
        return skinnyLogfileInfo;
    }

    /**
     * Set the skinny logfile info.
     * @param skinnyInfo the skinny logfile info.
     */
    public void setSkinnyLogfileInfo(SkinnyLogfileInfo skinnyInfo) {
        skinnyLogfileInfo = skinnyInfo;
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
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_CREATE_ARCHIVE;
    }
}
