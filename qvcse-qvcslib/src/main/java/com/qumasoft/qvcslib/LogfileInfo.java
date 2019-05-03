/**   Copyright 2004-2019 Jim Voris
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
 * Hold all the log file information in a single place.
 * @author Jim Voris
 */
public class LogfileInfo implements java.io.Serializable {
    private static final long serialVersionUID = -7116731826437585515L;

    // This is the stuff that gets serialized.
    private LogFileHeaderInfo logfileHeaderInfo;
    private RevisionInformation revisionInformation;
    private String fullLogfileName;
    private int fileID = -1;
    // This stuff does not get serialized.
    private transient String shortLogfileName;
    private transient String shortWorkfileName;
    private transient AccessList modifierList;
    private transient AccessList accessList;

    /**
     * Default constructor.
     */
    LogfileInfo() {
    }

    /**
     * Build an instance using the header, and revision information.
     * @param headerInfo the header info.
     * @param revisionInfo the revision info.
     * @param id the file ID.
     * @param fullArchiveName the full archive name.
     */
    public LogfileInfo(LogFileHeaderInfo headerInfo, RevisionInformation revisionInfo, int id, String fullArchiveName) {
        logfileHeaderInfo = headerInfo;
        revisionInformation = revisionInfo;
        fullLogfileName = fullArchiveName;
        fileID = id;
    }

    /**
     * Get the most recent checkin date.
     * @return the most recent checkin date.
     */
    public Date getLastCheckInDate() {
        Date lastCheckInDate = null;
        RevisionHeader defaultRevision = getDefaultRevision();
        if (defaultRevision != null) {
            lastCheckInDate = defaultRevision.getCheckInDate();
        }
        return lastCheckInDate;
    }

    /**
     * Get the QVCS user name of the user who made the most recent edit to the archive.
     * @return the QVCS user name of the user who made the most recent edit to the archive.
     */
    String getLastEditBy() {
        String lastEditBy = null;
        RevisionHeader defaultRevision = getDefaultRevision();
        if (defaultRevision != null) {
            lastEditBy = indexToUsername(defaultRevision.getCreatorIndex());
        }
        return lastEditBy;
    }

    /**
     * Get the access list.
     * @return the access list.
     */
    AccessList getAccessList() {
        return accessList;
    }

    /**
     * Get the modifier list.
     * @return the modifier list.
     */
    AccessList getModifierList() {
        return modifierList;
    }

    /**
     * Get the 'workfile in' location.
     * @return the 'workfile in' location.
     */
    String getWorkfileInLocation() {
        String returnString = "";
        if (logfileHeaderInfo != null) {
            if (logfileHeaderInfo.getLogFileHeader().lockCount() > 0) {
                returnString = logfileHeaderInfo.getWorkfileName();
            }
        }
        return returnString;
    }

    /**
     * Get the lock count.
     * @return the lock count.
     */
    public int getLockCount() {
        int retVal = -1;
        if (logfileHeaderInfo != null) {
            retVal = logfileHeaderInfo.getLogFileHeader().lockCount();
        }
        return retVal;
    }

    /**
     * Get the file id.
     * @return the file id.
     */
    public int getFileID() {
        return fileID;
    }

    /**
     * Get the default revision string.
     * @return the default revision string.
     */
    String getDefaultRevisionString() {
        String retVal = null;

        if (revisionInformation != null) {
            RevisionHeader revisionHeader = getDefaultRevision();
            if (revisionHeader != null) {
                retVal = revisionHeader.getRevisionString();
            }
        }
        return retVal;
    }

    /**
     * Convert a modifier index into a QVCS user name.
     * @param index the index into the modifier list.
     * @return the associated QVCS user name.
     */
    private String indexToUsername(int index) {
        String userName = null;
        if (modifierList == null) {
            if (logfileHeaderInfo != null) {
                modifierList = new AccessList(logfileHeaderInfo.getModifierList());
            }
        }
        if (modifierList != null) {
            userName = modifierList.indexToUser(index);
        }
        return userName;
    }

    /**
     * Get the revision header of the default revision.
     * @return the revision header of the default revision.
     */
    private RevisionHeader getDefaultRevision() {
        RevisionHeader returnHeader = null;

        if (null != logfileHeaderInfo) {
            // If the default branch isn't the trunk, then we have some work to do...
            if (logfileHeaderInfo.getLogFileHeader().defaultDepth() > 0) {
                int revisionCount = logfileHeaderInfo.getRevisionCount();
                RevisionDescriptor defaultDescriptor = logfileHeaderInfo.getDefaultRevisionDescriptor();
                String defaultBranchString = defaultDescriptor.toString();
                for (int i = 0; i < revisionCount; i++) {
                    RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
                    if (revHeader.getDepth() == defaultDescriptor.getElementCount() - 1) {
                        if (revHeader.isTip()) {
                            String revisionString = revHeader.getRevisionString();
                            if (revisionString.compareToIgnoreCase(defaultBranchString) == 0) {
                                returnHeader = revHeader;
                                break;
                            }
                        }
                    }
                }
            } else {
                // The default branch is the trunk.  Things are simple here.
                returnHeader = revisionInformation.getRevisionHeader(0);
            }
        } else {
            returnHeader = null;
        }

        return returnHeader;
    }

    /**
     * Get the user name(s) that hold any locks on this archive. This method just delegates to {@link #getLockedByUser()}.
     * @return a string showing who holds locks on this archive.
     */
    public String getLockedByString() {
        return getLockedByUser();
    }

    /**
     * Return the user name(s) that hold any locks on this archive. The format of the returned string is for use within the GUI. If there are multiple lockers,
     * they are all returned.
     * @return a string showing who holds locks on this archive.
     */
    public String getLockedByUser() {
        String returnString;

        if (null != logfileHeaderInfo) {
            if (logfileHeaderInfo.getLogFileHeader().lockCount() > 0) {
                StringBuilder lockerString = new StringBuilder();
                int revisionCount = logfileHeaderInfo.getRevisionCount();
                int lockCount = logfileHeaderInfo.getLogFileHeader().lockCount();
                for (int i = 0, j = 0; (i < revisionCount) && (j < lockCount); i++) {
                    RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
                    if (revHeader.isLocked()) {
                        // Put commas between the user names.
                        if (j > 0) {
                            lockerString.append(",");
                        }
                        j++;
                        lockerString.append(indexToUsername(revHeader.getLockerIndex()));
                    }
                }
                returnString = lockerString.toString();
            } else {
                returnString = "";
            }
        } else {
            returnString = "";
        }
        return returnString;
    }

    /**
     * Set the full archive file name.
     * @param fullArchiveName the full archive file name.
     */
    void setFullLogfileName(String fullArchiveName) {
        fullLogfileName = fullArchiveName;
    }

    /**
     * Get the full archive file name.
     * @return the full archive file name.
     */
    String getFullLogfileName() {
        return fullLogfileName;
    }

    /**
     * Get the short archive name.
     * @return the short archive name.
     */
    String getShortArchiveName() {
        if (shortLogfileName == null) {
            byte separator = Utility.deducePathSeparator(fullLogfileName);
            shortLogfileName = fullLogfileName.substring(1 + fullLogfileName.lastIndexOf(separator));
        }
        return shortLogfileName;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    String getShortWorkfileName() {
        if (shortWorkfileName == null) {
            shortWorkfileName = Utility.convertArchiveNameToShortWorkfileName(fullLogfileName);
        }
        return shortWorkfileName;
    }

    /**
     * Get the logfile header info.
     * @return the logfile header info.
     */
    public LogFileHeaderInfo getLogFileHeaderInfo() {
        return logfileHeaderInfo;
    }

    /**
     * Set the logfile header info.
     * @param logFileHeaderInfo the logfile header info.
     */
    void setLogFileHeaderInfo(LogFileHeaderInfo logFileHeaderInfo) {
        logfileHeaderInfo = logFileHeaderInfo;
    }

    /**
     * Get the revision information.
     * @return the revision information.
     */
    public RevisionInformation getRevisionInformation() {
        return revisionInformation;
    }

    /**
     * Supply a useful representation of the logfile info.
     * @return a useful representation of the logfile info.
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(logfileHeaderInfo.toString());
        string.append(revisionInformation.toString());
        return string.toString();
    }
}
