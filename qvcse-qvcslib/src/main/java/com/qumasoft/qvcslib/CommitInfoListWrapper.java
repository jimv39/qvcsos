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
package com.qumasoft.qvcslib;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public class CommitInfoListWrapper implements Serializable {

    private List<CommitInfo> commitInfoList;
    private Integer tagCommitId;

    /**
     * @return the commitInfoList
     */
    public List<CommitInfo> getCommitInfoList() {
        return commitInfoList;
    }

    /**
     * @param list the commitInfoList to set
     */
    public void setCommitInfoList(List<CommitInfo> list) {
        this.commitInfoList = list;
    }

    /**
     * @return the tagCommitId
     */
    public Integer getTagCommitId() {
        return tagCommitId;
    }

    /**
     * @param id the tagCommitId to set
     */
    public void setTagCommitId(Integer id) {
        this.tagCommitId = id;
    }
}
