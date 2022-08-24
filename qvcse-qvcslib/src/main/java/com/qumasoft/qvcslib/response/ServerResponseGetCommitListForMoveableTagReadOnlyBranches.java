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
import com.qumasoft.qvcslib.CommitInfoListWrapper;

/**
 *
 * @author Jim Voris
 */
public class ServerResponseGetCommitListForMoveableTagReadOnlyBranches extends AbstractServerResponse {
    private String projectName;
    private String branchName;
    private Integer syncToken;
    private CommitInfoListWrapper commitInfoListWrapper;

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_COMMIT_LIST_FOR_MOVEABLE_TAG_READ_ONLY_BRANCHES;
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
     * @return the syncToken
     */
    public Integer getSyncToken() {
        return syncToken;
    }

    /**
     * @param token the syncToken to set
     */
    public void setSyncToken(Integer token) {
        this.syncToken = token;
    }

    /**
     * @return the commitInfoListWrapper
     */
    public CommitInfoListWrapper getCommitInfoListWrapper() {
        return commitInfoListWrapper;
    }

    /**
     * @param wrapper the commitInfoListWrapper to set
     */
    public void setCommitInfoListWrapper(CommitInfoListWrapper wrapper) {
        this.commitInfoListWrapper = wrapper;
    }

}
