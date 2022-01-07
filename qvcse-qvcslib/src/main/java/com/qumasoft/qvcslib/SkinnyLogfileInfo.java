/*   Copyright 2004-2019 Jim Voris
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
 * A skinny version of the information about file. This is usually the information that we send from the server to the client to describe a given archive file.
 * @author Jim Voris
 */
public class SkinnyLogfileInfo implements java.io.Serializable {

    private static final long serialVersionUID = 9L;
    // This is the stuff that gets serialized.
    private String shortWorkfileName = null;
    private Date lastCheckInDate = null;
    private String lastEditByString = null;
    private String defaultRevisionString = null;
    private String separator = null;
    private ArchiveAttributes archiveAttributes = null;
    private byte[] defaultRevisionDigest = null;
    private Integer branchId = null;
    private Integer commitId = null;
    private Integer fileRevisionId = null;
    private int cacheIndex = -1;
    private int revisionCount = -1;
    private int fileID = -1;
    private boolean overlapFlag = false;

    /**
     * This ctor is used by vanilla serialization.
     */
    public SkinnyLogfileInfo() {
        archiveAttributes = new ArchiveAttributes();
    }

    /**
     * This ctor is used when we only need the short workfile name.
     * @param shortName the short workfile name.
     */
    public SkinnyLogfileInfo(String shortName) {
        archiveAttributes = new ArchiveAttributes();
        this.shortWorkfileName = shortName;
    }

    /**
     * Get the last checkin date.
     * @return the last checkin date.
     */
    public Date getLastCheckInDate() {
        return lastCheckInDate;
    }

    /**
     * Get the last edit by String.
     * @return the last edit by String.
     */
    public String getLastEditBy() {
        return getLastEditByString();
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Get the default revision String.
     * @return the default revision String.
     */
    public String getDefaultRevisionString() {
        return defaultRevisionString;
    }

    /**
     * Get the file ID.
     * @return the file ID.
     */
    public int getFileID() {
        return fileID;
    }

    /**
     * Get the archive attributes.
     * @return the archive attributes.
     */
    public ArchiveAttributes getAttributes() {
        return archiveAttributes;
    }

    /**
     * Get the separator String.
     * @return the separator String.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Get the default revision's digest.
     * @return the default revision's digest.
     */
    public byte[] getDefaultRevisionDigest() {
        return defaultRevisionDigest;
    }

    /**
     * Get the cache index for the workfile associated with this instance.
     * @return the cache index for the workfile associated with this instance.
     */
    public int getCacheIndex() {
        return cacheIndex;
    }

    /**
     * Set the cache index so we can associate this with the workfile's entry in the workfile cache.
     * @param index the cache index for the workfile associated with this instance.
     */
    public void setCacheIndex(int index) {
        cacheIndex = index;
    }

    /**
     * Get the revision count.
     * @return the revision count.
     */
    public int getRevisionCount() {
        return revisionCount;
    }

    /**
     * Set the revision count.
     * @param revCount the revision count.
     */
    public void setRevisionCount(int revCount) {
        revisionCount = revCount;
    }

    /**
     * Get the overlap flag.
     * @return the overlap flag.
     */
    public boolean getOverlapFlag() {
        return overlapFlag;
    }

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

    /**
     * @param shortName the shortWorkfileName to set
     */
    public void setShortWorkfileName(String shortName) {
        this.shortWorkfileName = shortName;
    }

    /**
     * @param lastCheckin the lastCheckInDate to set
     */
    public void setLastCheckInDate(Date lastCheckin) {
        this.lastCheckInDate = lastCheckin;
    }

    /**
     * @return the lastEditByString
     */
    public String getLastEditByString() {
        return lastEditByString;
    }

    /**
     * @param lastEditorName the lastEditByString to set
     */
    public void setLastEditByString(String lastEditorName) {
        this.lastEditByString = lastEditorName;
    }

    /**
     * @param revisionString the defaultRevisionString to set
     */
    public void setDefaultRevisionString(String revisionString) {
        this.defaultRevisionString = revisionString;
    }

    /**
     * @param digest the defaultRevisionDigest to set
     */
    public void setDefaultRevisionDigest(byte[] digest) {
        this.defaultRevisionDigest = digest;
    }

    /**
     * @param id the fileID to set
     */
    public void setFileID(int id) {
        this.fileID = id;
    }

    /**
     * @return the branchId
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * @param id the branchId to set
     */
    public void setBranchId(Integer id) {
        this.branchId = id;
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
     * @return the fileRevisionId
     */
    public Integer getFileRevisionId() {
        return fileRevisionId;
    }

    /**
     * @param id the fileRevisionId to set
     */
    public void setFileRevisionId(Integer id) {
        this.fileRevisionId = id;
    }
}
