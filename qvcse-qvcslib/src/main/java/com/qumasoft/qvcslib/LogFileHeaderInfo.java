/*   Copyright 2004-2021 Jim Voris
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

import java.util.Date;

/**
 * Date serialized from server to client with details about the history of a file.
 * @author Jim Voris
 */
public final class LogFileHeaderInfo implements java.io.Serializable {
    private static final long serialVersionUID = 4972338412531779081L;

    /*
     * Data members that get serialized
     */
    private String commentPrefix = null;
    private String lastModifierName = "";
    private String moduleDescription = "";
    private Date lastArchiveUpdateDate = null;
    private int lastWorkfileSize = -1;
    private int branchId;
    private int latestRevisionId;
    private int fileID = -1;
    private ArchiveAttributes archiveAttributes = null;
    private int revisionCount = -1;

    /**
     * Default constructor.
     */
    public LogFileHeaderInfo() {
        archiveAttributes = new ArchiveAttributes();
    }

    /**
     * For use in branches only.
     * @param attributes
     */
    public LogFileHeaderInfo(ArchiveAttributes attributes) {
        archiveAttributes = attributes;
    }

    /**
     * Get the comment prefix as a String.
     * @return the comment prefix.
     */
    public String getCommentPrefix() {
        return commentPrefix;
    }

    /**
     * Set the comment prefix as a String.
     * @param commentPfx the comment prefix as a String.
     */
    public void setCommentPrefix(String commentPfx) {
        commentPrefix = commentPfx;
    }

    /**
     * Get the revision count... i.e. the number of revisions in this archive file.
     * @return the revision count.
     */
    public int getRevisionCount() {
        return revisionCount;
    }

    /**
     * Get the archive attributes.
     * @return the archive attributes..
     */
    public ArchiveAttributes getAttributes() {
        return getArchiveAttributes();
    }

    /**
     * Get the latest trunk revision.
     * @return the latest trunk revision.
     */
    public String getLatestTrunkRevision() {
        return String.format("%d.%d", getBranchId(), getLatestRevisionId());
    }

    /**
     * Supply a useful String representation of the logfile header.
     * @return a useful String representation of the logfile header.
     */
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();

        // show the revision count
        returnString.append("\nRevision Count:\t").append(getRevisionCount());

        // show the latest trunk revision
        returnString.append("\nLatest trunk revision:\t").append(getLatestTrunkRevision());

        // show the comment prefix
        returnString.append("\nComment prefix:\t'").append(getCommentPrefix()).append("'");

        // show the attributes
        returnString.append(getAttributes());

        return returnString.toString();
    }

    /**
     * @return the lastModifierName
     */
    public String getLastModifierName() {
        return lastModifierName;
    }

    /**
     * @param name the lastModifierName to set
     */
    public void setLastModifierName(String name) {
        this.lastModifierName = name;
    }

    /**
     * @return the lastArchiveUpdateDate
     */
    public Date getLastArchiveUpdateDate() {
        return lastArchiveUpdateDate;
    }

    /**
     * @param date the lastArchiveUpdateDate to set
     */
    public void setLastArchiveUpdateDate(Date date) {
        this.lastArchiveUpdateDate = date;
    }

    /**
     * @return the lastWorkfileSize
     */
    public long getLastWorkfileSize() {
        return lastWorkfileSize;
    }

    /**
     * @param size the lastWorkfileSize to set
     */
    public void setLastWorkfileSize(int size) {
        this.lastWorkfileSize = size;
    }

    /**
     * @return the branchId
     */
    public int getBranchId() {
        return branchId;
    }

    /**
     * @param id the branchId to set
     */
    public void setBranchId(int id) {
        this.branchId = id;
    }

    /**
     * @return the latestRevisionId
     */
    public int getLatestRevisionId() {
        return latestRevisionId;
    }

    /**
     * @param id the latestRevisionId to set
     */
    public void setLatestRevisionId(int id) {
        this.latestRevisionId = id;
    }

    /**
     * @return the fileID
     */
    public int getFileID() {
        return fileID;
    }

    /**
     * @param id the fileID to set
     */
    public void setFileID(int id) {
        this.fileID = id;
    }

    /**
     * @return the archiveAttributes
     */
    public ArchiveAttributes getArchiveAttributes() {
        return archiveAttributes;
    }

    /**
     * @param attributes the archiveAttributes to set
     */
    public void setArchiveAttributes(ArchiveAttributes attributes) {
        this.archiveAttributes = attributes;
    }

    /**
     * @param count the revisionCount to set
     */
    public void setRevisionCount(int count) {
        this.revisionCount = count;
    }

    /**
     * @return the moduleDescription
     */
    public String getModuleDescription() {
        return moduleDescription;
    }

    /**
     * @param description the moduleDescription to set
     */
    public void setModuleDescription(String description) {
        this.moduleDescription = description;
    }
}
