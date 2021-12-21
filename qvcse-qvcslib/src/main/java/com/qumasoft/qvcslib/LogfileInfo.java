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
    private int fileID = -1;
    // This stuff does not get serialized.
    private transient String shortWorkfileName;

    /**
     * Default constructor.
     */
    public LogfileInfo() {
    }

    /**
     * Build an instance using the header, and revision information.
     * @param headerInfo the header info.
     * @param revisionInfo the revision info.
     * @param id the file ID.
     * @param workfileName the short workfile name.
     */
    public LogfileInfo(LogFileHeaderInfo headerInfo, RevisionInformation revisionInfo, int id, String workfileName) {
        logfileHeaderInfo = headerInfo;
        revisionInformation = revisionInfo;
        shortWorkfileName = workfileName;
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
            lastEditBy = defaultRevision.getCreator();
        }
        return lastEditBy;
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
        return "TODO";
    }

    /**
     * Get the revision header of the default revision.
     * @return the revision header of the default revision.
     */
    private RevisionHeader getDefaultRevision() {
        RevisionHeader returnHeader = null;

        if (null != logfileHeaderInfo) {
            // The default branch is the trunk.  Things are simple here.
            returnHeader = revisionInformation.getRevisionHeader(0);
        } else {
            returnHeader = null;
        }

        return returnHeader;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    String getShortWorkfileName() {
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
    public void setLogFileHeaderInfo(LogFileHeaderInfo logFileHeaderInfo) {
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
     * Set the revision information.
     * @param info the revision information.
     */
    public void setRevisionInformation(RevisionInformation info) {
        revisionInformation = info;
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
