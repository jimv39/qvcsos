//   Copyright 2004-2019 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.qvcslib;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * A skinny version of the information about a QVCS archive. This is usually the information that we send from the server to the client to describe a given archive file.
 * @author Jim Voris
 */
public class SkinnyLogfileInfo implements java.io.Serializable {

    private static final long serialVersionUID = 9L;
    // This is the stuff that gets serialized.
    private String shortWorkfileName = null;
    private String lockedByString = null;
    private Date lastCheckInDate = null;
    private String lastEditByString = null;
    private String defaultRevisionString = null;
    private String workfileInLocation = null;
    private String separator = null;
    private ArchiveAttributes archiveAttributes = null;
    private byte[] defaultRevisionDigest = null;
    private final Map<String, String> lockedRevisionMap = Collections.synchronizedMap(new TreeMap<>());
    private int lockCount = 0;
    private boolean isObsoleteFlag = false;
    private int cacheIndex = -1;
    private int revisionCount = -1;
    private int fileID = -1;
    private boolean overlapFlag = false;

    /**
     * Create a skinny logfile info instance using the supplied information.
     * @param logfileInfo the archive's associated logFileInfo object.
     * @param sepStr separator string.
     * @param digest the digest for the tip revision.
     * @param shortName the short workfile name.
     * @param ovrlapFlag true if we have detected overlap for a prospective merge.
     */
    public SkinnyLogfileInfo(LogfileInfo logfileInfo, String sepStr, byte[] digest, String shortName, boolean ovrlapFlag) {
        LogFileHeaderInfo logfileHeaderInfo = logfileInfo.getLogFileHeaderInfo();

        defaultRevisionString = logfileInfo.getDefaultRevisionString();
        separator = sepStr;
        isObsoleteFlag = false;
        shortWorkfileName = shortName;
        lockedByString = logfileInfo.getLockedByString();
        lastCheckInDate = logfileInfo.getLastCheckInDate();
        lastEditByString = logfileInfo.getLastEditBy();
        fileID = logfileInfo.getFileID();
        lockCount = initGetLockCount(logfileHeaderInfo);
        workfileInLocation = initGetWorkfileInLocation(logfileHeaderInfo);
        archiveAttributes = initArchiveAttributes(logfileHeaderInfo);
        defaultRevisionDigest = digest;
        revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
        overlapFlag = ovrlapFlag;
        initLockedRevisionsMap(logfileInfo);
    }

    /**
     * This ctor is used by vanilla serialization.
     */
    public SkinnyLogfileInfo() {
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
        return lastEditByString;
    }

    /**
     * Get the lock count.
     * @return the lock count.
     */
    public int getLockCount() {
        return lockCount;
    }

    /**
     * Get the locked by String.
     * @return the locked by String.
     */
    public String getLockedByString() {
        return lockedByString;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Get the workfile in location String.
     * @return the workfile in location String.
     */
    public String getWorkfileInLocation() {
        return workfileInLocation;
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
     * Get the locked revision String for the given user.
     * @param userName the user we are interested in.
     * @return the locked revision String for the given user.
     */
    public String getLockedRevisionString(String userName) {
        return lockedRevisionMap.get(userName);
    }

    /**
     * Get the separator String.
     * @return the separator String.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Does this describe a deleted archive?
     * @return true if this describes a deleted archive
     */
    public boolean getIsObsolete() {
        return isObsoleteFlag;
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
    private int initGetLockCount(LogFileHeaderInfo logfileHeaderInfo) {
        int retVal = -1;
        if (logfileHeaderInfo != null) {
            retVal = logfileHeaderInfo.getLogFileHeader().lockCount();
        }
        return retVal;
    }

    private String initGetWorkfileInLocation(LogFileHeaderInfo logfileHeaderInfo) {
        String returnString = "";
        if (logfileHeaderInfo != null) {
            if (logfileHeaderInfo.getLogFileHeader().lockCount() > 0) {
                returnString = logfileHeaderInfo.getWorkfileName();
            }
        }
        return returnString;
    }

    private ArchiveAttributes initArchiveAttributes(LogFileHeaderInfo logfileHeaderInfo) {
        ArchiveAttributes retVal = null;
        if (logfileHeaderInfo != null) {
            retVal = new ArchiveAttributes(logfileHeaderInfo.getLogFileHeader().attributes().getAttributesAsInt());
        }
        return retVal;
    }

    private void initLockedRevisionsMap(LogfileInfo logfileInfo) {
        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
        AccessList modifierList = revisionInformation.getModifierList();
        int revCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
        for (int i = 0; i < revCount; i++) {
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
            if (revHeader.isLocked()) {
                String lockerName = modifierList.indexToUser(revHeader.getLockerIndex());
                lockedRevisionMap.put(lockerName, revHeader.getRevisionString());
            }
        }
    }
}
