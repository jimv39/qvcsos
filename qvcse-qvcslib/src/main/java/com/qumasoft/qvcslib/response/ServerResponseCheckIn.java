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

import com.qumasoft.qvcslib.AddRevisionData;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LogFileProxy;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.WorkfileDirectoryManagerInterface;
import com.qumasoft.qvcslib.WorkfileInfo;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checkin response.
 * @author Jim Voris
 */
public class ServerResponseCheckIn extends AbstractServerResponse {
    private static final long serialVersionUID = 1695701430998444307L;

    // These are serialized:
    private String clientWorkfileName = null;
    private String shortWorkfileName = null;
    private String appendedPath = null;
    private String projectName = null;
    private String branchName = null;
    private String newRevisionString = null;
    private boolean protectWorkfileFlag = false;
    private int index = -1;
    // Send back the skinny logfile info
    private SkinnyLogfileInfo skinnyLogfileInfo = null;
    // The following elements are optionally sent back.
    private LogfileInfo logfileInfo = null;
    private AddRevisionData addedRevisionData = null;
    private Integer syncToken;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResponseCheckIn.class);

    /**
     * Creates new ServerResponseCheckOut.
     */
    public ServerResponseCheckIn() {
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
     * Get the added revision data.
     * @return the added revision data.
     */
    public AddRevisionData getAddedRevisionData() {
        return addedRevisionData;
    }

    /**
     * Set the added revision data.
     * @param addedRevData the added revision data.
     */
    public void setAddedRevisionData(AddRevisionData addedRevData) {
        addedRevisionData = addedRevData;
    }

    /**
     * Get the client workfile name.
     * @return the client workfile name.
     */
    public String getClientWorkfileName() {
        return clientWorkfileName;
    }

    /**
     * Set the client workfile name.
     * @param workfileName the client workfile name.
     */
    public void setClientWorkfileName(String workfileName) {
        clientWorkfileName = workfileName;
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
     * Get the index. (To correlate with the client workfile cache index.).
     * @return the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the index.
     * @param idx the index to correlate with the client workfile cache index.
     */
    public void setIndex(int idx) {
        index = idx;
    }

    /**
     * Get the protect workfile flag.
     * @return the protect workfile flag.
     */
    public boolean getProtectWorkfileFlag() {
        return protectWorkfileFlag;
    }

    /**
     * Set the protect workfile flag.
     * @param flag the protect workfile flag.
     */
    public void setProtectWorkfileFlag(boolean flag) {
        protectWorkfileFlag = flag;
    }

    /**
     * Get the new revision string.
     * @return the new revision string.
     */
    public String getNewRevisionString() {
        return newRevisionString;
    }

    /**
     * Set the new revision string.
     * @param revisionString the new revision string.
     */
    public void setNewRevisionString(String revisionString) {
        newRevisionString = revisionString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        StringBuilder message = new StringBuilder("Checked in ");
        message.append(getShortWorkfileName());
        message.append(" from project directory: ");
        message.append(getAppendedPath());

        WorkfileDirectoryManagerInterface workfileDirManager = directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager();
        if (workfileDirManager != null) {
            try {
                String fullWorkfileName = workfileDirManager.getWorkfileDirectory() + File.separator + getShortWorkfileName();

                // We only need to update the workfile directory manager if the workfile
                // actually created a new revision.
                if ((getNewRevisionString() != null) && (getNewRevisionString().length() > 0)) {
                    WorkfileInfo workfileInfo = new WorkfileInfo(fullWorkfileName, getSkinnyLogfileInfo().getAttributes().getIsBinaryfile(), getProjectName(), getBranchName());

                    // Set the archiveInfo on the workfileInfo object.
                    MergedInfoInterface mergedInfo = directoryManagerProxy.getDirectoryManager().getMergedInfo(getShortWorkfileName());
                    ArchiveInfoInterface archiveInfo = mergedInfo.getArchiveInfo();
                    workfileInfo.setArchiveInfo(archiveInfo);

                    // Need to test that the workfile actually exists, since
                    // we may have deleted it before getting here, if the
                    // delete workfile attribute had been set.
                    if (workfileInfo.getWorkfile().exists()) {
                        workfileInfo.setFetchedDate(workfileInfo.getWorkfile().lastModified());
                        workfileInfo.setWorkfileRevisionString(getNewRevisionString());
                    }

                    // Update the logfile info on the logFileProxy object before
                    // we update the workfile digest (which happens via the
                    // updateWorkfileInfo() call...
                    if (getLogfileInfo() != null) {
                        LogFileProxy logFileProxy = (LogFileProxy) archiveInfo;
                        synchronized (logFileProxy) {
                            logFileProxy.setLogfileInfo(getLogfileInfo());

                            // Notify the other thread that it can continue.
                            logFileProxy.notifyAll();
                        }
                    }

                    workfileDirManager.updateWorkfileInfo(workfileInfo);
                }
            } catch (IOException | QVCSException e) {
                LOGGER.warn("Caught exception trying to update workfile info: " + e.getLocalizedMessage());
            }
        }

        // This goes to the status line
        directoryManagerProxy.updateInfo(message.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_CHECK_IN;
    }
}
