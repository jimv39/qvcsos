/*   Copyright 2004-2022 Jim Voris
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

import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import java.util.Date;

/**
 * Archive information interface. Used to describe those methods that must be implemented in order to represent a QVCS archive file. There are two basic implementors of this
 * interface. One implementation 'lives' on the server, and is used to actually make changes to the archive files; the other implementation 'lives' on the client, and proxies
 * the operations described here so that the operations are performed on the server.
 * @author Jim Voris
 */
public interface ArchiveInfoInterface {

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    String getShortWorkfileName();

    /**
     * Get the revision count.
     * @return the number of revisions. Note that this may vary depending on the branch.
     */
    int getRevisionCount();

    /**
     * Get the Date of the most recent checkin.
     * @return the Date of the most recent checkin.
     */
    Date getLastCheckInDate();

    /**
     * Get the name of the most recent user to edit the file.
     * @return the name of the most recent user to edit the file.
     */
    String getLastEditBy();

    /**
     * Get the digest for the default revision.
     *
     * @return the digest of the default revision.
     */
    byte[] getDefaultRevisionDigest();

    /**
     * Get the file id.
     * @return the file id.
     */
    int getFileID();

    /**
     * Get the QVCS archive attributes.
     * @return the QVCS archive attributes.
     */
    ArchiveAttributes getAttributes();

    /**
     * Get the logfile info.
     * @return the logfile info.
     */
    LogfileInfo getLogfileInfo();

    /**
     * Get the revision information.
     * @return the revision information.
     */
    RevisionInformation getRevisionInformation();

    /**
     * Get the revision description for the given revision.
     * @param revString the revision whose description we want.
     * @return the checkin comment for the given revision.
     */
    String getRevisionDescription(String revString);

    /**
     * Return a buffer that contains the requested revision. This method is synchronous.
     *
     * @param revisionString the revision that should be fetched
     * @return a byte array containing the copy of the requested revision, or
     * null if the revision cannot be retrieved.
     */
    byte[] getRevisionAsByteArray(String revisionString);

    /**
     * Get the default revision string.
     * @return the default revision string.
     */
    String getDefaultRevisionString();

    /**
     * Checkin a revision. This is asynchronous.
     *
     * @param commandArgs the command arguments.
     * @param checkInFilename the name of the file to checkin.
     * @param ignoreLocksToEnableBranchCheckinFlag flag indicating if we can ignore locks for a checkin on a branch that doesn't support locks.
     * @return true if the checkin succeeded; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean checkInRevision(CheckInCommandArgs commandArgs, String checkInFilename, boolean ignoreLocksToEnableBranchCheckinFlag) throws QVCSException;

    /**
     * Checkin a revision. This is synchronous.
     *
     * @param commandArgs the command arguments.
     * @param checkInFilename the name of the file to checkin.
     * @param ignoreLocksToEnableBranchCheckinFlag flag indicating if we can
     * ignore locks for a checkin on a branch that doesn't support locks.
     * @return true if the checkin succeeded; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean checkInRevisionSynchronous(CheckInCommandArgs commandArgs, String checkInFilename, boolean ignoreLocksToEnableBranchCheckinFlag) throws QVCSException;

    /**
     * Get a file revision. This is asynchronous.
     *
     * @param commandLineArgs the command arguments.
     * @param fetchToFileName the file that we write the bit to.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean getRevision(GetRevisionCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException;

    /**
     * Get a file revision. This is synchronous.
     *
     * @param commandLineArgs the command arguments.
     * @param fetchToFileName the file that we write the bit to.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean getRevisionSynchronous(GetRevisionCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException;

    /**
     * Get for a visual compare.
     * @param commandLineArgs the command arguments.
     * @param outputFileName the output filename (probably a temp file).
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean getForVisualCompare(GetRevisionCommandArgs commandLineArgs, String outputFileName) throws QVCSException;

    /**
     * Delete the file (really 'delete' the file by moving it to the cemetery).
     * @param user the user name.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean deleteArchive(String user) throws QVCSException;

    /**
     * Set the archive attributes.
     * @param user the user name.
     * @param attributes the new attributes.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean setAttributes(String user, ArchiveAttributes attributes) throws QVCSException;

    /**
     * Is there overlap.
     * @return true if overlap has been detected between trunk and any branch.
     */
    boolean getIsOverlap();

    /**
     * Get the commit id of the newest revision.
     *
     * @return the commit id of the newest revision.
     */
    Integer getCommitId();
}
