/*
 * Copyright 2022 Jim Voris.
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
import com.qumasoft.qvcslib.BriefCommitInfo;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public class ServerResponseGetBriefCommitInfoList implements ServerResponseInterface {
    private String projectName;
    private String branchName;
    private Integer syncToken;
    private List<BriefCommitInfo> briefCommitInfoList;
    private List<Integer> fileIdList;

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_BRIEF_COMMIT_INFO_LIST;
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
     * @return the briefCommitInfoList
     */
    public List<BriefCommitInfo> getBriefCommitInfoList() {
        return briefCommitInfoList;
    }

    /**
     * @param infoList the briefCommitInfoList to set
     */
    public void setBriefCommitInfoList(List<BriefCommitInfo> infoList) {
        this.briefCommitInfoList = infoList;
    }

    /**
     * @return the fileIdList
     */
    public List<Integer> getFileIdList() {
        return fileIdList;
    }

    /**
     * @param list the fileIdList to set
     */
    public void setFileIdList(List<Integer> list) {
        this.fileIdList = list;
    }

}
