/*
 * Copyright 2019 JimVoris.
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
package com.qumasoft.server.datamodel;

import java.util.Date;

/**
 * Commit history db model class.
 * @author JimVoris
 */
public class CommitHistory {
    /*
     * The SQL snippet used to create the CommitHistory table:
     * CREATE TABLE QVCSE.COMMIT_HISTORY (
     * ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT ID_PK PRIMARY KEY,
     * INSERT_DATE TIMESTAMP NOT NULL,
     * COMMIT_MESSAGE VARCHAR(2048) NOT NULL)
     */
    private Integer id;
    private Date commitDate;
    private String commitMessage;

    /**
     * Get the primary key.
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set the primary key.
     * @param i the id to set
     */
    public void setId(Integer i) {
        this.id = i;
    }

    /**
     * Get the commit date.
     * @return the commitDate
     */
    public Date getCommitDate() {
        return commitDate;
    }

    /**
     * Set the commit date.
     * @param d the commitDate to set
     */
    public void setCommitDate(Date d) {
        this.commitDate = d;
    }

    /**
     * Get the commit message.
     * @return the commitMessage
     */
    public String getCommitMessage() {
        return commitMessage;
    }

    /**
     * Set the commit message.
     * @param m the commitMessage to set
     */
    public void setCommitMessage(String m) {
        this.commitMessage = m;
    }

}
