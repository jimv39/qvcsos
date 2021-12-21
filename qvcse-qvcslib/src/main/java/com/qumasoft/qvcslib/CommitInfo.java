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
import java.util.Date;

/**
 *
 * @author Jim Voris
 */
public class CommitInfo implements Serializable {

    private Integer commitId;
    private Date commitDate;
    private String commitMessage;

    @Override
    public String toString() {
        String commitInfo = String.format("%d %s %s", commitId, commitDate.toString(), commitMessage);
        return commitInfo;
    }

    /**
     * @return the commitId
     */
    public Integer getCommitId() {
        return commitId;
    }

    /**
     * @param id the commitId to set
     */
    public void setCommitId(Integer id) {
        this.commitId = id;
    }

    /**
     * @return the commitDate
     */
    public Date getCommitDate() {
        return commitDate;
    }

    /**
     * @param date the commitDate to set
     */
    public void setCommitDate(Date date) {
        Date ourDate = new Date(date.getTime());
        this.commitDate = ourDate;
    }

    /**
     * @return the commitMessage
     */
    public String getCommitMessage() {
        return commitMessage;
    }

    /**
     * @param message the commitMessage to set
     */
    public void setCommitMessage(String message) {
        this.commitMessage = message;
    }

}
