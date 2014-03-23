/*   Copyright 2004-2014 Jim Voris
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
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ResolveConflictResults;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import com.qumasoft.qvcslib.WorkfileDirectoryManagerInterface;
import com.qumasoft.qvcslib.WorkfileInfo;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server response resolve conflict from parent branch. This response contains the data that the client needs in order to resolve conflicts from the parent branch.
 *
 * @author Jim Voris
 */
public final class ServerResponseResolveConflictFromParentBranch implements ServerResponseInterface {
    private static final long serialVersionUID = 4853646602120264135L;

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
    private String projectName = null;
    private String branchName = null;
    private String appendedPath = null;
    private String shortWorkfileName = null;
    // Send back the skinny logfile info
    private SkinnyLogfileInfo skinnyLogfileInfo = null;
    /**
     * The merged result buffer
     */
    private byte[] mergedResultBuffer = null;
    /**
     * The common ancestor buffer (optional)
     */
    private byte[] commonAncestorBuffer = null;
    /**
     * The branch parent tip revision (optional)
     */
    private byte[] branchParentTipRevisionBuffer = null;
    /**
     * The branch tip revision (optional)
     */
    private byte[] branchTipRevisionBuffer = null;
    /**
     * Optionally sent back if needed to expand keywords.
     */
    private LogfileInfo logfileInfo = null;

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
        this.shortWorkfileName = shortName;
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
        this.appendedPath = path;
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
        this.projectName = project;
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
    public void setBranchName(String branch) {
        this.branchName = branch;
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
        this.skinnyLogfileInfo = skinnyInfo;
    }

    /**
     * Get the merged result buffer.
     * @return the merged result buffer.
     */
    public byte[] getMergedResultBuffer() {
        return mergedResultBuffer;
    }

    /**
     * Set the merged result buffer.
     * @param mergedBuffer the merged result buffer.
     */
    public void setMergedResultBuffer(byte[] mergedBuffer) {
        this.mergedResultBuffer = new byte[mergedBuffer.length];
        System.arraycopy(mergedBuffer, 0, this.mergedResultBuffer, 0, mergedBuffer.length);
    }

    /**
     * Get the common ancestor buffer.
     * @return the common ancestor buffer.
     */
    public byte[] getCommonAncestorBuffer() {
        return commonAncestorBuffer;
    }

    /**
     * Set the common ancestor buffer.
     * @param ancestorBuffer the common ancestor buffer.
     */
    public void setCommonAncestorBuffer(byte[] ancestorBuffer) {
        this.commonAncestorBuffer = new byte[ancestorBuffer.length];
        System.arraycopy(ancestorBuffer, 0, this.commonAncestorBuffer, 0, ancestorBuffer.length);
    }

    /**
     * Get the branch parent tip revision buffer.
     * @return the branch parent tip revision buffer.
     */
    public byte[] getBranchParentTipRevisionBuffer() {
        return branchParentTipRevisionBuffer;
    }

    /**
     * Set the branch parent tip revision buffer.
     * @param branchParentTipBuffer the branch parent tip revision buffer.
     */
    public void setBranchParentTipRevisionBuffer(byte[] branchParentTipBuffer) {
        this.branchParentTipRevisionBuffer = new byte[branchParentTipBuffer.length];
        System.arraycopy(branchParentTipBuffer, 0, this.branchParentTipRevisionBuffer, 0, branchParentTipBuffer.length);
    }

    /**
     * Get the branch tip revision buffer.
     * @return the branch tip revision buffer.
     */
    public byte[] getBranchTipRevisionBuffer() {
        return branchTipRevisionBuffer;
    }

    /**
     * Set the branch tip revision buffer.
     * @param branchTipBuffer the branch tip revision buffer.
     */
    public void setBranchTipRevisionBuffer(byte[] branchTipBuffer) {
        this.branchTipRevisionBuffer = new byte[branchTipBuffer.length];
        System.arraycopy(branchTipBuffer, 0, this.branchTipRevisionBuffer, 0, branchTipBuffer.length);
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
        this.logfileInfo = info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        StringBuilder message = new StringBuilder("Resolve conflict for [").append(getShortWorkfileName()).append("] from project directory: [")
                .append(getAppendedPath()).append("]");
        WorkfileDirectoryManagerInterface workfileDirManager = directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager();
        LogFileProxy logFileProxy = (LogFileProxy) directoryManagerProxy.getArchiveInfo(getShortWorkfileName());
        if (logFileProxy != null) {
            ResolveConflictResults resolveConflictResults = new ResolveConflictResults(getProjectName(), getBranchName(), getAppendedPath(), getShortWorkfileName(),
                    getSkinnyLogfileInfo(), getMergedResultBuffer(), getCommonAncestorBuffer(), getBranchParentTipRevisionBuffer(), getBranchTipRevisionBuffer(), getLogfileInfo());
            synchronized (logFileProxy) {
                logFileProxy.setResolveConflictResults(resolveConflictResults);
                if (getLogfileInfo() != null) {
                    logFileProxy.setLogfileInfo(getLogfileInfo());
                }
                logFileProxy.notifyAll();
            }

            if (workfileDirManager != null) {
                try {
                    String fullWorkfileName = workfileDirManager.getWorkfileDirectory() + File.separator + getShortWorkfileName();
                    WorkfileInfo workfileInfo = new WorkfileInfo(fullWorkfileName, getSkinnyLogfileInfo().getAttributes().getIsExpandKeywords(),
                            getSkinnyLogfileInfo().getAttributes().getIsBinaryfile(), getProjectName());
                    Date now = new Date();
                    workfileInfo.setFetchedDate(now.getTime());
                    workfileInfo.setWorkfileRevisionString(getSkinnyLogfileInfo().getDefaultRevisionString());

                    // Update the Workfile digest manager.
                    WorkfileDigestManager.getInstance().updateWorkfileDigestForMerge(branchParentTipRevisionBuffer, workfileInfo,
                            directoryManagerProxy.getProjectProperties());
                } catch (QVCSException e) {
                    LOGGER.log(Level.WARNING, "Caught QVCSException trying to update workfile info: " + e.getLocalizedMessage());
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Caught IOException trying to update workfile info: " + e.getLocalizedMessage());
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
        directoryManagerProxy.updateInfo(message.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_RESOLVE_CONFLICT_FROM_PARENT_BRANCH;
    }
}
