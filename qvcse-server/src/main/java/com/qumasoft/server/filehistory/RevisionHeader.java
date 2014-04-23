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
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Jim Voris
 */
public class RevisionHeader implements ToFromStreamInterface {
    private Integer id;
    private Integer ancestorRevisionId;
    private Integer reverseDeltaRevisionId;
    private CommitIdentifier commitIdentifier;
    private Date commitDate;
    private Date workfileEditDate;
    private WorkfileAttributes attributes;
    private CompressionType compressionType;
    private Integer dataSize;
    private String description;
    private String author;

    /**
     * Get the revision id.
     * @return the revision id.
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer revId) {
        this.id = revId;
    }

    public Integer getAncestorRevisionId() {
        return ancestorRevisionId;
    }

    public void setAncestorRevisionId(Integer aId) {
        this.ancestorRevisionId = aId;
    }

    public Integer getReverseDeltaRevisionId() {
        return reverseDeltaRevisionId;
    }

    public void setReverseDeltaRevisionId(Integer rdId) {
        this.reverseDeltaRevisionId = rdId;
    }

    public CommitIdentifier getCommitIdentifier() {
        return commitIdentifier;
    }

    public void setCommitIdentifier(CommitIdentifier cId) {
        this.commitIdentifier = cId;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date date) {
        this.commitDate = date;
    }

    public Date getWorkfileEditDate() {
        return workfileEditDate;
    }

    public void setWorkfileEditDate(Date date) {
        this.workfileEditDate = date;
    }

    public WorkfileAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(WorkfileAttributes a) {
        this.attributes = a;
    }

    public CompressionType getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(CompressionType c) {
        this.compressionType = c;
    }

    public Integer getDataSize() {
        return dataSize;
    }

    public void setDataSize(Integer size) {
        this.dataSize = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String a) {
        this.author = a;
    }

    @Override
    public void toStream(DataOutputStream o) throws IOException {
        o.writeInt(id);
        o.writeInt(ancestorRevisionId);
        o.writeInt(reverseDeltaRevisionId);
        commitIdentifier.toStream(o);
        o.writeLong(commitDate.getTime());
        o.writeLong(workfileEditDate.getTime());
        attributes.toStream(o);
        o.writeUTF(compressionType.name());
        o.writeInt(dataSize);
        o.writeUTF(description);
        o.writeUTF(author);
    }

    @Override
    public void fromStream(DataInputStream i) throws IOException {
        id = i.readInt();
        ancestorRevisionId = i.readInt();
        reverseDeltaRevisionId = i.readInt();
        commitIdentifier = new CommitIdentifier();
        commitIdentifier.fromStream(i);
        long d = i.readLong();
        commitDate = new Date(d);
        d = i.readLong();
        workfileEditDate = new Date(d);
        attributes = new WorkfileAttributes();
        attributes.fromStream(i);
        String compressionTypeString = i.readUTF();
        compressionType = CompressionType.valueOf(compressionTypeString);
        dataSize = i.readInt();
        description = i.readUTF();
        author = i.readUTF();
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.ancestorRevisionId);
        hash = 53 * hash + Objects.hashCode(this.reverseDeltaRevisionId);
        hash = 53 * hash + Objects.hashCode(this.commitIdentifier);
        hash = 53 * hash + Objects.hashCode(this.commitDate);
        hash = 53 * hash + Objects.hashCode(this.workfileEditDate);
        hash = 53 * hash + Objects.hashCode(this.attributes);
        hash = 53 * hash + Objects.hashCode(this.compressionType);
        hash = 53 * hash + Objects.hashCode(this.dataSize);
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + Objects.hashCode(this.author);
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
        final RevisionHeader other = (RevisionHeader) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.ancestorRevisionId, other.ancestorRevisionId)) {
            return false;
        }
        if (!Objects.equals(this.reverseDeltaRevisionId, other.reverseDeltaRevisionId)) {
            return false;
        }
        if (!Objects.equals(this.commitIdentifier, other.commitIdentifier)) {
            return false;
        }
        if (!Objects.equals(this.commitDate, other.commitDate)) {
            return false;
        }
        if (!Objects.equals(this.workfileEditDate, other.workfileEditDate)) {
            return false;
        }
        if (!Objects.equals(this.attributes, other.attributes)) {
            return false;
        }
        if (this.compressionType != other.compressionType) {
            return false;
        }
        if (!Objects.equals(this.dataSize, other.dataSize)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return Objects.equals(this.author, other.author);
    }
}
