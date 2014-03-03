//   Copyright 2004-2014 Jim Voris
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
     * Get the lock count.
     * @return the number of locked revisions.
     */
    int getLockCount();

    /**
     * Get the revision count.
     * @return the number of revisions. Note that this may vary depending on the view.
     */
    int getRevisionCount();

    /**
     * Get the locked by string.
     * @return a string showing the names of any users who hold locks on any revisions.
     */
    String getLockedByString();

    /**
     * Get the Date of the most recent checkin.
     * @return the Date of the most recent checkin.
     */
    Date getLastCheckInDate();

    /**
     * Get the workfile in location.
     * @return the workfile in location.
     */
    String getWorkfileInLocation();

    /**
     * Get the name of the most recent user to edit the file.
     * @return the name of the most recent user to edit the file.
     */
    String getLastEditBy();

    /**
     * Get the digest (an MD5 digest of the non-keyword expanded flavor of the revision) for the default revision.
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
     * @param revisionString the revision whose description we want.
     * @return the checkin comment for the given revision.
     */
    String getRevisionDescription(final String revisionString);

    /**
     * Return a buffer that contains the requested revision. This method is synchronous.
     *
     * @param revisionString the revision that should be fetched
     * @return a byte array containing the non-keyword expanded copy of the requested revision, or null if the revision cannot be retrieved.
     */
    byte[] getRevisionAsByteArray(String revisionString);

    /**
     * Return the string for the locked revision for the given userName. If the user does not have any locked revisions, return null.
     * @param userName the user name.
     * @return the revisions locked by the given user. null if the user holds no locks.
     */
    String getLockedRevisionString(String userName);

    /**
     * Get the default revision string.
     * @return the default revision string.
     */
    String getDefaultRevisionString();

    /**
     * Checkin a revision.
     * @param commandArgs the command arguments.
     * @param checkInFilename the name of the file to checkin.
     * @param ignoreLocksToEnableBranchCheckinFlag flag indicating if we can ignore locks for a checkin on a branch that doesn't support locks.
     * @return true if the checkin succeeded; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean checkInRevision(LogFileOperationCheckInCommandArgs commandArgs, String checkInFilename, boolean ignoreLocksToEnableBranchCheckinFlag) throws QVCSException;

    /**
     * Checkout a revision.
     * @param commandLineArgs the command arguments.
     * @param fetchToFileName the file that we write the bits to.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean checkOutRevision(LogFileOperationCheckOutCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException;

    /**
     * Get a file revision.
     * @param commandLineArgs the command arguments.
     * @param fetchToFileName the file that we write the bit to.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean getRevision(LogFileOperationGetRevisionCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException;

    /**
     * Get for a visual compare.
     * @param commandLineArgs the command arguments.
     * @param outputFileName the output filename (probably a temp file).
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean getForVisualCompare(LogFileOperationGetRevisionCommandArgs commandLineArgs, String outputFileName) throws QVCSException;

    /**
     * Lock a file revision.
     * @param commandArgs the command arguments.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean lockRevision(LogFileOperationLockRevisionCommandArgs commandArgs) throws QVCSException;

    /**
     * Unlock a file revision.
     * @param commandArgs the command arguments.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean unlockRevision(LogFileOperationUnlockRevisionCommandArgs commandArgs) throws QVCSException;

    /**
     * Break a file revision lock.
     * @param commandArgs the command arguments.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean breakLock(LogFileOperationUnlockRevisionCommandArgs commandArgs) throws QVCSException;

    /**
     * Label a file revision.
     * @param commandArgs the command arguments.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean labelRevision(LogFileOperationLabelRevisionCommandArgs commandArgs) throws QVCSException;

    /**
     * Remove a label.
     * @param commandArgs the command arguments.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean unLabelRevision(LogFileOperationUnLabelRevisionCommandArgs commandArgs) throws QVCSException;

    /**
     * Is the file obsolete.
     * @return true if obsolete; false otherwise.
     * @deprecated we don't use the obsolete label anymore, so don't use this.
     */
    boolean getIsObsolete();

    /**
     * Set the file obsolete.
     * @param userName the user name.
     * @param flag true means mark obsolete; false means not obsolete.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     * @deprecated don't use this any more. use the delete operation instead.
     */
    boolean setIsObsolete(String userName, boolean flag) throws QVCSException;

    /**
     * Set the archive attributes.
     * @param userName the user name.
     * @param attributes the new attributes.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean setAttributes(String userName, ArchiveAttributes attributes) throws QVCSException;

    /**
     * Set the comment prefix string. This is used for keyword expansion of the Log and LogX keywords.
     * @param userName the user name.
     * @param commentPrefix the comment prefix string.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean setCommentPrefix(String userName, String commentPrefix) throws QVCSException;

    /**
     * Set the module description.
     * @param userName the user name.
     * @param moduleDescription the module description (i.e. what is the purpose of the file).
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean setModuleDescription(String userName, String moduleDescription) throws QVCSException;

    /**
     * Set a revision description. This allows you to change the checkin comment for a given revision.
     * @param commandArgs the command arguments.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something went wrong.
     */
    boolean setRevisionDescription(LogFileOperationSetRevisionDescriptionCommandArgs commandArgs) throws QVCSException;

    /**
     * Is there overlap.
     * @return true if overlap has been detected between trunk and any branch.
     */
    boolean getIsOverlap();
}
