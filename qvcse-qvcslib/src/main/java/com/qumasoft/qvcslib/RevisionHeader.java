/*   Copyright 2004-2015 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib;

import java.text.DateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Revision Header. Wrap the revision header that is stored for each revision within a QVCS archive file.
 * @author Jim Voris
 */
public final class RevisionHeader implements java.io.Serializable {
    private static final long serialVersionUID = -3233746302839526442L;
    /** Create our logger object */
    private static final Logger LOGGER = LoggerFactory.getLogger(RevisionHeader.class);

    // Revision header info that is stored in the archive file.
    private RevisionCompressionHeader compressionInformation;
    private String revisionDescription;
    private int descriptionSize;
    private int revisionSize;
    private Date editDate;
    private Date checkInDate;
    private byte newLineCharacter;
    private byte newLineFlag;
    private byte compressFlag;
    private byte isTipFlag;
    private int childCount;
    private int branchId;
    private int fileRevisionId;
    private int commitId;
    private String revisionCreator;
    private transient int revisionIndex;
    private transient RevisionHeader parentRevisionHeader = null;

    /**
     * Create a revision header.
     */
    public RevisionHeader() {
    }

    /**
     * A copy constructor.
     * @param copyThisRevisionHeader the revision header to copy.
     */
    public RevisionHeader(RevisionHeader copyThisRevisionHeader) {
        this.compressFlag = copyThisRevisionHeader.compressFlag;
        if (compressFlag != 0) {
            this.compressionInformation = new RevisionCompressionHeader(copyThisRevisionHeader.compressionInformation);
        } else {
            this.compressionInformation = null;
        }
        this.revisionDescription = copyThisRevisionHeader.revisionDescription;
        this.descriptionSize = copyThisRevisionHeader.descriptionSize;
        this.revisionCreator = copyThisRevisionHeader.getCreator();
        this.revisionSize = copyThisRevisionHeader.revisionSize;
        this.editDate = new Date(copyThisRevisionHeader.editDate.getTime());
        this.checkInDate = new Date(copyThisRevisionHeader.checkInDate.getTime());
        this.newLineCharacter = copyThisRevisionHeader.newLineCharacter;
        this.newLineFlag = copyThisRevisionHeader.newLineFlag;
        this.isTipFlag = copyThisRevisionHeader.isTipFlag;
        this.childCount = copyThisRevisionHeader.childCount;
        this.branchId = copyThisRevisionHeader.branchId;
        this.fileRevisionId = copyThisRevisionHeader.fileRevisionId;
        this.revisionIndex = copyThisRevisionHeader.revisionIndex;
        this.parentRevisionHeader = null;
    }

    /**
     * Convenience implementation of toString.
     * @return convenient String representation of this revision header.
     */
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();

        // Report the revision
        returnString.append("Revision ");
        returnString.append(String.format("%d.%d", getBranchId(), getFileRevisionId()));
        returnString.append(" created by ");
        returnString.append(getCreator());
        Date localEditDate = new Date(this.editDate.getTime());
        returnString.append("\nLast File edit: ");
        returnString.append(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(localEditDate));
        Date localCheckInDate = new Date(this.checkInDate.getTime());
        returnString.append("\nCheck-in date: ");
        returnString.append(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(localCheckInDate));
        if (compressFlag != 0) {
            returnString.append("\nRevision storage compressed from: ");
            returnString.append(compressionInformation.getInputSize());
            returnString.append(" bytes to: ");
            returnString.append(compressionInformation.getCompressedSize());
            returnString.append(" bytes\n");
        } else {
            returnString.append("\nRevision storage requires: ");
            returnString.append(revisionSize);
            returnString.append(" bytes\n");
        }
        returnString.append("Revision description:\n");
        returnString.append(revisionDescription).append("\n");

        return returnString.toString();
    }

    /**
     * Get the revision string.
     * @return the revision string.
     */
    public String getRevisionString() {
        return String.format("%d.%d", getBranchId(), getFileRevisionId());
    }

    /**
     * Get the revision size. The is the number of bytes it takes to store this revision on disk.
     * @return the revision size. The is the number of bytes it takes to store this revision on disk.
     */
    public int getRevisionSize() {
        return revisionSize;
    }

    /**
     * Set the revision size.
     * @param size the revision size.
     */
    public void setRevisionSize(int size) {
        revisionSize = size;
    }

    /**
     * Set the parent revision header.
     * @param parent the parent revision header.
     */
    public void setParentRevisionHeader(RevisionHeader parent) {
        parentRevisionHeader = parent;
    }

    /**
     * Get the parent revision header.
     * @return the parent revision header.
     */
    public RevisionHeader getParentRevisionHeader() {
        return parentRevisionHeader;
    }

    /**
     * Get the revision compression header. Meaningful only if the revision is compressed.
     * @return the revision compression header.
     */
    public RevisionCompressionHeader getCompressionHeader() {
        return compressionInformation;
    }

    /**
     * Flag to indicate if this is a tip revision.
     * @return true if this is a tip revision; false otherwise.
     */
    public boolean isTip() {
        boolean isTip = false;
        if (isTipFlag != 0) {
            isTip = true;
        }
        return isTip;
    }

    /**
     * Set the 'is tip' flag.
     * @param flag the 'is tip' flag.
     */
    public void setIsTip(boolean flag) {
        if (flag) {
            isTipFlag = 1;
        } else {
            isTipFlag = 0;
        }
    }

    /**
     * Flag to indicate if the revision is compressed.
     * @return true if this revision is compressed; false otherwise.
     */
    public boolean isCompressed() {
        boolean isCompressed = false;
        if (compressFlag != 0) {
            isCompressed = true;
        }
        return isCompressed;
    }

    /**
     * Set the 'is compressed' flag.
     * @param flag the 'is compressed' flag.
     */
    public void setIsCompressed(boolean flag) {
        if (flag) {
            compressFlag = 1;
        } else {
            compressFlag = 0;
        }
    }

    /**
     * Get the number of branch revisions... i.e. the number of branches that are anchored by this revision.
     * @return the number of branch revisions anchored by this revision.
     */
    public int getChildCount() {
        return childCount;
    }

    /**
     * Increment the child count.
     */
    public void incrementChildCount() {
        childCount++;
    }

    /**
     * Decrement the child count.
     */
    public void decrementChildCount() {
        if (childCount > 0) {
            childCount--;
        }
    }

    /**
     * Get the creator.
     * @return the revision creator.
     */
    public String getCreator() {
        return this.revisionCreator;
    }

    /**
     * Set the creator.
     * @param creator the QVCS user name of the creator of this revision.
     */
    public void setCreator(String creator) {
        this.revisionCreator = creator;
    }

    /**
     * Get the checkin data for this revision.
     * @return the checkin data for this revision.
     */
    public java.util.Date getCheckInDate() {
        return new java.util.Date(checkInDate.getTime());
    }

    /**
     * Set the checkin data for this revision.
     * @param time the checkin data for this revision.
     */
    public void setCheckInDate(java.util.Date time) {
        checkInDate = new Date(time.getTime());
    }

    /**
     * Get the edit date for this revision. This is the last edit time of the workfile used to create this revision.
     * @return the edit date for this revision.
     */
    public java.util.Date getEditDate() {
        return new java.util.Date(editDate.getTime());
    }

    /**
     * Set the edit date for this revision.
     * @param time the edit date for this revision.
     */
    public void setEditDate(java.util.Date time) {
        editDate = new Date(time.getTime());
    }

    /**
     * Get the revision description (i.e. the checkin comment).
     * @return the revision description (i.e. the checkin comment).
     */
    public String getRevisionDescription() {
        return revisionDescription;
    }

    /**
     * Set the revision description (i.e. the checkin comment).
     * @param description the revision description (i.e. the checkin comment).
     */
    public void setRevisionDescription(String description) {
        revisionDescription = description;
        descriptionSize = 1 + description.getBytes().length;
    }

    /**
     * Get the revision's major revision number.
     * @return the revision's major revision number.
     */
    public int getFileRevisionId() {
        return fileRevisionId;
    }

    /**
     * Set the revision's major revision number.
     * @param number the revision's major revision number.
     */
    public void setFileRevisionId(int number) {
        fileRevisionId = number;
    }

    /**
     * Get the revision's minor revision number.
     * @return the revision's minor revision number.
     */
    public int getBranchId() {
        return branchId;
    }

    /**
     * Set the revision's minor revision number.
     * @param number the revision's minor revision number.
     */
    public void setBranchId(int number) {
        branchId = number;
    }

    /**
     * Get the revision's revision index... i.e. its position within the QVCS archive file. An index of 0 is the newest revision.
     * @return the revison's revision index.
     */
    public int getRevisionIndex() {
        return this.revisionIndex;
    }

    /**
     * Set the revision's revision index.
     * @param index the revision's revision index.
     */
    public void setRevisionIndex(int index) {
        this.revisionIndex = index;
    }

    /**
     * @return the commitId
     */
    public int getCommitId() {
        return commitId;
    }

    /**
     * @param id the commitId to set
     */
    public void setCommitId(int id) {
        this.commitId = id;
    }
}
