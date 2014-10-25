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
 * The revision header for {@link FileHistory}.
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

    /**
     * Set the revision id.
     * @param revId the revision id.
     */
    public void setId(Integer revId) {
        this.id = revId;
    }

    /**
     * Get the ancestor revision Id. An 'ancestor' revision is the revision that historically immediately preceeded this revision, for this branch. Given a 'tip' revision (i.e.
     * one that is the most recent revision for a given branch, we can discover the full history of that file for that branch by 'walking' backward through the revisions of a file
     * until we get to the revision whose ancestor revision id is -1 -- meaning it was the 1st revision of the file.
     * @return the ancestor revision id.
     */
    public Integer getAncestorRevisionId() {
        return ancestorRevisionId;
    }

    /**
     * Set the ancestor revision id.
     * @param aId the ancestor revision id.
     */
    public void setAncestorRevisionId(Integer aId) {
        this.ancestorRevisionId = aId;
    }

    /**
     * Get the reverse delta revision id. A 'reverse delta revision id' identifies the revision that must be hydrated so that the current revision's reverse delta can be applied
     * to it in order to hydrate the current revision. If the reverse delta revision id is -1, it means that the revision data for this revision is not a reverse delta, and that
     * no other revisions need to be hydrated in order to construct this revision.
     * @return Get the reverse delta revision id.
     */
    public Integer getReverseDeltaRevisionId() {
        return reverseDeltaRevisionId;
    }

    /**
     * Set the reverse delta revision id.
     * @param rdId the reverse delta revision id.
     */
    public void setReverseDeltaRevisionId(Integer rdId) {
        this.reverseDeltaRevisionId = rdId;
    }

    /**
     * Get the commit identifier.
     * @return the commit identifier.
     */
    public CommitIdentifier getCommitIdentifier() {
        return commitIdentifier;
    }

    /**
     * Set the commit identifier.
     * @param cId the commit identifier.
     */
    public void setCommitIdentifier(CommitIdentifier cId) {
        this.commitIdentifier = cId;
    }

    /**
     * Get the commit date.
     * @return the commit date.
     */
    public Date getCommitDate() {
        return new Date(commitDate.getTime());
    }

    /**
     * Set the commit date.
     * @param date the commit date.
     */
    public void setCommitDate(Date date) {
        this.commitDate = new Date(date.getTime());
    }

    /**
     * Get the workfile edit date.
     * @return the workfile edit date.
     */
    public Date getWorkfileEditDate() {
        return new Date(workfileEditDate.getTime());
    }

    /**
     * Set the workfile edit date.
     * @param date the workfile edit date.
     */
    public void setWorkfileEditDate(Date date) {
        this.workfileEditDate = new Date(date.getTime());
    }

    /**
     * Get the workfile attributes.
     * @return the workfile attributes.
     */
    public WorkfileAttributes getAttributes() {
        return attributes;
    }

    /**
     * Set the workfile attributes.
     * @param a the workfile attributes.
     */
    public void setAttributes(WorkfileAttributes a) {
        this.attributes = a;
    }

    /**
     * Get the compression type.
     * @return the compression type.
     */
    public CompressionType getCompressionType() {
        return compressionType;
    }

    /**
     * Set the compression type.
     * @param c the compression type.
     */
    public void setCompressionType(CompressionType c) {
        this.compressionType = c;
    }

    /**
     * Get the size of the revision data.
     * @return the size of the revision data.
     */
    public Integer getDataSize() {
        return dataSize;
    }

    /**
     * Set the size of the revision data.
     * @param size the size of the revision data.
     */
    public void setDataSize(Integer size) {
        this.dataSize = size;
    }

    /**
     * Get the revision description.
     * @return the revision description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the revision description.
     * @param d the revision description.
     */
    public void setDescription(String d) {
        this.description = d;
    }

    /**
     * Get the revision author.
     * @return the revision author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the revision author.
     * @param a the revision author.
     */
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
