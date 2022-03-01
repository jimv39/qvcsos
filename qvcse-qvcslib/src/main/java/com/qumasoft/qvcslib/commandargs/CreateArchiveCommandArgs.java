/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.qvcslib.commandargs;

import com.qumasoft.qvcslib.ArchiveAttributes;
import java.util.Date;

/**
 * Create archive command arguments.
 * @author Jim Voris
 */
public final class CreateArchiveCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = -5652683904753498132L;

    private String userName;
    private String workfilename;         // the name of the workfile from which we derive the name of the archive file.
    private String archiveDescription;
    private Date inputFileTimestamp;
    private Date checkInTimestamp;     // this is the time we did the check in.  By default, this is null, which means NOW.
    private ArchiveAttributes attributes = null;    // null if we use the server's notion of what the attributes should be, otherwise use the
    // attributes defined by this object.

    /**
     * Creates a new instance of LogFileOperationCreateArchiveCommandArgs.
     */
    public CreateArchiveCommandArgs() {
    }

    /**
     * Get the user name.
     * @return the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name.
     * @param user the user name.
     */
    public void setUserName(String user) {
        userName = user;
    }

    /**
     * Get the archive description.
     * @return the archive description.
     */
    public String getArchiveDescription() {
        return archiveDescription;
    }

    /**
     * Set the archive description.
     * @param archiveDesc the archive description.
     */
    public void setArchiveDescription(String archiveDesc) {
        archiveDescription = archiveDesc;
    }

    /**
     * Get the input file timestamp.
     * @return the input file timestamp.
     */
    public Date getInputfileTimeStamp() {
        return inputFileTimestamp;
    }

    /**
     * Set the input file timestamp.
     * @param timestamp the input file timestamp.
     */
    public void setInputfileTimeStamp(Date timestamp) {
        inputFileTimestamp = timestamp;
    }

    /**
     * Get the checkin timestamp.
     * @return the checkin timestamp.
     */
    public Date getCheckInTimestamp() {
        return checkInTimestamp;
    }

    /**
     * Set the checkin timestamp.
     * @param timestamp the checkin timestamp.
     */
    public void setCheckInTimestamp(Date timestamp) {
        checkInTimestamp = timestamp;
    }

    /**
     * Get the workfile name.
     * @return the workfile name.
     */
    public String getWorkfileName() {
        return workfilename;
    }

    /**
     * Set the workfile name.
     * @param workName the workfile name.
     */
    public void setWorkfileName(String workName) {
        workfilename = workName;
    }

    /**
     * Get the QVCS archive attributes.
     * @return the QVCS archive attributes.
     */
    public ArchiveAttributes getAttributes() {
        return attributes;
    }

    /**
     * Set the QVCS archive attributes.
     * @param attribs the QVCS archive attributes.
     */
    public void setAttributes(ArchiveAttributes attribs) {
        attributes = attribs;
    }
}
