/*
 * Copyright 2014 JimVoris.
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
 *
 * @author Jim Voris
 */
public class CommitIdentifier implements ToFromStreamInterface {
    private Integer serverId;
    private Integer branchId;
    private Integer commitId;

    public CommitIdentifier() {
        serverId = -1;
        branchId = -1;
        commitId = -1;
    }

    public CommitIdentifier(Integer sId, Integer bId, Integer cId) {
        this.serverId = sId;
        this.branchId = bId;
        this.commitId = cId;
    }

    @Override
    public void toStream(DataOutputStream o) throws IOException {
        o.writeInt(serverId);
        o.writeInt(branchId);
        o.writeInt(commitId);
    }

    @Override
    public void fromStream(DataInputStream i) throws IOException {
        serverId = i.readInt();
        branchId = i.readInt();
        commitId = i.readInt();
    }

    /**
     * Get the server id.
     * @return the server id.
     */
    public Integer getServerId() {
        return serverId;
    }

    /**
     * Set the server id.
     * @param serverId the server id.
     */
    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    /**
     * Get the branch id.
     * @return the branch id.
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * Set the branch id.
     * @param branchId the branch id.
     */
    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    /**
     * Get the commit id.
     * @return the commit id.
     */
    public Integer getCommitId() {
        return commitId;
    }

    /**
     * Set the commit id.
     * @param commitId the commit id.
     */
    public void setCommitId(Integer commitId) {
        this.commitId = commitId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.serverId);
        hash = 43 * hash + Objects.hashCode(this.branchId);
        hash = 43 * hash + Objects.hashCode(this.commitId);
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
        if (!Objects.equals(this.branchId, other.branchId)) {
            return false;
        }
        return Objects.equals(this.commitId, other.commitId);
    }
}
