//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.qvcslib;

/**
 * Server response promote file. This response contains the data that the client needs in order to promote a file.
 *
 * @author Jim Voris
 */
public class ServerResponsePromoteFile implements ServerResponseInterface {
    private static final long serialVersionUID = 2762365762014455335L;

    private String projectName = null;
    private String branchName = null;
    private String appendedPath = null;
    private String shortWorkfileName = null;
    private MergeType mergeType = null;
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
     * @return the shortWorkfileName
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the shortWorkfileName to set
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
        this.mergedResultBuffer = mergedBuffer;
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
        this.commonAncestorBuffer = ancestorBuffer;
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
        this.branchParentTipRevisionBuffer = branchParentTipBuffer;
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
        this.branchTipRevisionBuffer = branchTipBuffer;
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
     * @param fileInfo the logfile info.
     */
    public void setLogfileInfo(LogfileInfo fileInfo) {
        this.logfileInfo = fileInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        StringBuilder message = new StringBuilder("Promote file [").append(getShortWorkfileName()).append("] from project directory: [").append(getAppendedPath()).append("]");
        LogFileProxy logFileProxy = (LogFileProxy) directoryManagerProxy.getArchiveInfo(getShortWorkfileName());
        if (logFileProxy != null) {
            PromoteFileResults promoteFileResults = new PromoteFileResults(getProjectName(), getBranchName(), getAppendedPath(), getShortWorkfileName(), getMergeType(),
                    getSkinnyLogfileInfo(), getMergedResultBuffer(), getCommonAncestorBuffer(), getBranchParentTipRevisionBuffer(), getBranchTipRevisionBuffer(), getLogfileInfo());
            Object dirManagerSyncObject = directoryManagerProxy.getSynchronizationObject();
            synchronized (dirManagerSyncObject) {
                logFileProxy.setPromoteFileResults(promoteFileResults);
                if (getLogfileInfo() != null) {
                    logFileProxy.setLogfileInfo(getLogfileInfo());
                }
                dirManagerSyncObject.notifyAll();
            }
        }
        directoryManagerProxy.updateInfo(message.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_PROMOTE_FILE;
    }

    /**
     * Get the merge type.
     * @return the merge type.
     */
    public MergeType getMergeType() {
        return mergeType;
    }

    /**
     * Set the merge type.
     * @param type the merge type.
     */
    public void setMergeType(MergeType type) {
        this.mergeType = type;
    }
}
