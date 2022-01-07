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
package com.qvcsos.server.datamodel;

import java.sql.Timestamp;

/**
 *
 * @author Jim Voris
 */
public class Commit {
    private Integer commitId;
    private Integer userId;
    private Timestamp commitDate;
    private String commitMessage;

    /**
     * @return the id
     */
    public Integer getId() {
        return commitId;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.commitId = id;
    }

    /**
     * @return the commitDate
     */
    public Timestamp getCommitDate() {
        return commitDate;
    }

    /**
     * @param date the commitDate to set
     */
    public void setCommitDate(Timestamp date) {
        this.commitDate = date;
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

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param id the userId to set
     */
    public void setUserId(Integer id) {
        this.userId = id;
    }
}
