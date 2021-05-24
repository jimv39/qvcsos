/*
 * Copyright 2014-2021 JimVoris.
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
package com.qumasoft.server.filehistory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Identify the commit.
 *
 * @author Jim Voris
 */
public class CommitIdentifier implements ToFromStreamInterface {

    private Integer serverId;
    private Integer projectId;
    private Integer branchId;
    private Integer commitId;

    /**
     * Default constructor.
     */
    public CommitIdentifier() {
        serverId = -1;
        projectId = -1;
        branchId = -1;
        commitId = -1;
    }

    /**
     * Constructor.
     *
     * @param sId server id.
     * @param pId project id.
     * @param bId branch id.
     * @param cId commit id.
     */
    public CommitIdentifier(Integer sId, Integer pId, Integer bId, Integer cId) {
        this.serverId = sId;
        this.projectId = pId;
        this.branchId = bId;
        this.commitId = cId;
    }

    @Override
    public void toStream(DataOutputStream o) throws IOException {
        o.writeInt(serverId);
        o.writeInt(projectId);
        o.writeInt(branchId);
        o.writeInt(commitId);
    }

    @Override
    public void fromStream(DataInputStream i) throws IOException {
        serverId = i.readInt();
        projectId = i.readInt();
        branchId = i.readInt();
        commitId = i.readInt();
    }

    /**
     * Get the server id.
     *
     * @return the server id.
     */
    public Integer getServerId() {
        return serverId;
    }

    /**
     * Set the server id.
     *
     * @param sId the server id.
     */
    public void setServerId(Integer sId) {
        this.serverId = sId;
    }

    /**
     * Get the project Id.
     *
     * @return the project Id.
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Set the project Id.
     *
     * @param pId the project Id.
     */
    public void setProjectId(Integer pId) {
        this.projectId = pId;
    }

    /**
     * Get the branch id.
     *
     * @return the branch id.
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * Set the branch id.
     *
     * @param bId the branch id.
     */
    public void setBranchId(Integer bId) {
        this.branchId = bId;
    }

    /**
     * Get the commit id.
     *
     * @return the commit id.
     */
    public Integer getCommitId() {
        return commitId;
    }

    /**
     * Set the commit id.
     *
     * @param cId the commit id.
     */
    public void setCommitId(Integer cId) {
        this.commitId = cId;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.serverId);
        hash = 43 * hash + Objects.hashCode(this.projectId);
        hash = 43 * hash + Objects.hashCode(this.branchId);
        hash = 43 * hash + Objects.hashCode(this.commitId);
        // </editor-fold>
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CommitIdentifier other = (CommitIdentifier) obj;
        if (!Objects.equals(this.serverId, other.serverId)) {
            return false;
        }
        if (!Objects.equals(this.projectId, other.projectId)) {
            return false;
        }
        if (!Objects.equals(this.branchId, other.branchId)) {
            return false;
        }
        return Objects.equals(this.commitId, other.commitId);
    }
}
