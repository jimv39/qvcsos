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
import com.qumasoft.qvcslib.InfoForMerge;
import com.qumasoft.qvcslib.LogFileProxy;

/**
 * Get info for merge response.
 * @author Jim Voris
 */
public class ServerResponseGetInfoForMerge implements ServerResponseInterface {
    private static final long serialVersionUID = -5316809025281316529L;

    private String projectName;
    private String branchName;
    private String appendedPath;
    private String shortWorkfileName;
    private String parentAppendedPath;
    private String parentShortWorkfileName;
    private boolean parentRenamedFlag;
    private boolean branchRenamedFlag;
    private boolean parentMovedFlag;
    private boolean branchMovedFlag;
    private boolean createdOnBranchFlag;

    /**
     * Default constructor.
     */
    public ServerResponseGetInfoForMerge() {
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return this.projectName;
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
        return this.branchName;
    }

    /**
     * Set the branch name.
     * @param branch the branch name.
     */
    public void setBranchName(final String branch) {
        this.branchName = branch;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return this.appendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return this.shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setShortWorkfileName(final String shortName) {
        this.shortWorkfileName = shortName;
    }

    /**
     * Get the parent appended path.
     * @return the parent appended path.
     */
    public String getParentAppendedPath() {
        return this.parentAppendedPath;
    }

    /**
     * Set the parent appended path.
     * @param parentPath the parent appended path.
     */
    public void setParentAppendedPath(final String parentPath) {
        this.parentAppendedPath = parentPath;
    }

    /**
     * Get the parent short workfile name.
     * @return the parent short workfile name.
     */
    public String getParentShortWorkfileName() {
        return this.parentShortWorkfileName;
    }

    /**
     * Set the parent short workfile name.
     * @param parentShortName the parent short workfile name.
     */
    public void setParentShortWorkfileName(final String parentShortName) {
        this.parentShortWorkfileName = parentShortName;
    }

    /**
     * Get parent renamed flag.
     * @return parent renamed flag.
     */
    public boolean getParentRenamedFlag() {
        return this.parentRenamedFlag;
    }

    /**
     * Set parent renamed flag.
     * @param flag the parent renamed flag.
     */
    public void setParentRenamedFlag(boolean flag) {
        this.parentRenamedFlag = flag;
    }

    /**
     * Get parent moved flag.
     * @return parent moved flag.
     */
    public boolean getParentMovedFlag() {
        return this.parentMovedFlag;
    }

    /**
     * Set parent moved flag.
     * @param flag parent moved flag.
     */
    public void setParentMovedFlag(boolean flag) {
        this.parentMovedFlag = flag;
    }

    /**
     * Get branch renamed flag.
     * @return branch renamed flag.
     */
    public boolean getBranchRenamedFlag() {
        return this.branchRenamedFlag;
    }

    /**
     * Set branch renamed flag.
     * @param flag branch renamed flag.
     */
    public void setBranchRenamedFlag(boolean flag) {
        this.branchRenamedFlag = flag;
    }

    /**
     * Get branch moved flag.
     * @return branch moved flag.
     */
    public boolean getBranchMovedFlag() {
        return this.branchMovedFlag;
    }

    /**
     * Set branch moved flag.
     * @param flag branch moved flag.
     */
    public void setBranchMovedFlag(boolean flag) {
        this.branchMovedFlag = flag;
    }

    /**
     * Get the created on branch flag.
     * @return the created on branch flag.
     */
    public boolean getCreatedOnBranchFlag() {
        return createdOnBranchFlag;
    }

    /**
     * Set the created on branch flag.
     * @param flag the created on branch flag.
     */
    public void setCreatedOnBranchFlag(boolean flag) {
        this.createdOnBranchFlag = flag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        LogFileProxy logFileProxy = (LogFileProxy) directoryManagerProxy.getArchiveInfo(getShortWorkfileName());
        if (logFileProxy != null) {
            InfoForMerge infoForMerge = new InfoForMerge(getParentAppendedPath(), getParentShortWorkfileName(),
                    getParentRenamedFlag(), getBranchRenamedFlag(), getParentMovedFlag(), getBranchMovedFlag(), getCreatedOnBranchFlag());
            synchronized (logFileProxy) {
                logFileProxy.setInfoForMerge(infoForMerge);
                logFileProxy.notifyAll();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_INFO_FOR_MERGE;
    }
}
