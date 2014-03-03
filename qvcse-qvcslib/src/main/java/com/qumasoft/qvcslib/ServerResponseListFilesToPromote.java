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

import java.util.ArrayList;
import java.util.List;

/**
 * Server response list files to promote.
 *
 * @author Jim Voris
 */
public class ServerResponseListFilesToPromote implements ServerResponseInterface {
    private static final long serialVersionUID = -2897039061951310383L;

    // This is what gets serialized.
    private String projectName;
    private String parentBranchName;
    private String branchName;
    private final List<FilePromotionInfo> filesToPromoteList = new ArrayList<>();

    /**
     * Get the project name.
     *
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     *
     * @param project the projectName to set
     */
    public void setProjectName(String project) {
        this.projectName = project;
    }

    /**
     * Get the parent branch name. This is the name of the branch that we are merging to. It will almost always be the trunk.
     *
     * @return the parentBranchName the parent branch name.
     */
    public String getParentBranchName() {
        return parentBranchName;
    }

    /**
     * Set the parent branch name.
     *
     * @param parentBranch the parentBranchName to set
     */
    public void setParentBranchName(String parentBranch) {
        this.parentBranchName = parentBranch;
    }

    /**
     * Get the branch name. This is the name of the branch that we are merging from.
     *
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Set the branch name.
     *
     * @param branch the branch name
     */
    public void setBranchName(String branch) {
        this.branchName = branch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_LIST_FILES_TO_PROMOTE;
    }

    /**
     * Get the list of files eligible for promotion.
     *
     * @return the filesToPromoteList the list of files eligible for promotion.
     */
    public List<FilePromotionInfo> getFilesToPromoteList() {
        return filesToPromoteList;
    }

    /**
     * Add a file to the list of files eligible for promotion.
     *
     * @param filePromotionInfo the file to add to the list of eligible files.
     */
    public void addToList(FilePromotionInfo filePromotionInfo) {
        filesToPromoteList.add(filePromotionInfo);
    }
}
