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
package com.qumasoft.server;

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.CommentPrefixProperties;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.ExtensionAttributeProperties;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.ZlibCompressor;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create an archive.
 * @author Jim Voris
 */
class LogFileOperationCreateArchive extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final AccessList accessList;
    private final String filename;
    private final String userName;
    private final CreateArchiveCommandArgs commandLineArgs;
    private final String archiveDescription;
    private final boolean lockFlag;
    private RandomAccessFile newArchiveStream;
    private final AbstractProjectProperties projectProperties;
    private ArchiveAttributes archiveAttributes;
    private final String workfileName;
    private final String commentPrefix;

    /**
     * Creates a new instance of LogFileOperationCreateArchive.
     * @param args arguments for the operation. a[0] is the logfileImpl; a[1] is filename; a[2] is the project properties; a[3] are the command arguments object.
     */
    LogFileOperationCreateArchive(Object[] args) {
        super(args, (LogFileImpl) args[0]);
        filename = (String) args[1];
        projectProperties = (AbstractProjectProperties) args[2];
        // <editor-fold>
        commandLineArgs = (CreateArchiveCommandArgs) args[3];
        // </editor-fold>
        userName = commandLineArgs.getUserName();
        archiveDescription = commandLineArgs.getArchiveDescription();
        lockFlag = commandLineArgs.getLockFlag();
        workfileName = commandLineArgs.getWorkfileName();
        commentPrefix = getCommentPrefix();
        accessList = projectProperties.getInitialArchiveAccessList();
    }

    @Override
    public boolean execute() throws QVCSException {
        boolean retVal = false;

        // Make sure the creator is on the access list.
        accessList.addUser(commandLineArgs.getUserName());
        if (commandLineArgs.getAttributes() == null) {
            // Get the attributes based on file extension.
            archiveAttributes = ExtensionAttributeProperties.getInstance().getAttributes(workfileName);
        } else {
            // The user defined the attributes for us.
            archiveAttributes = commandLineArgs.getAttributes();
        }

        try {
            // Make sure a valid archive file does not already exist.
            if (getLogFileImpl().getFile().exists()) {
                if (getLogFileImpl().isValidArchive()) {
                    throw new QVCSException("Archive already exists for " + getLogFileImpl().getFile().getAbsolutePath());
                }
            }

            // Create the archive file.
            newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");

            // And populate it with data.
            createNewLogfileCreatingNewTrunkTip();
            retVal = true;
        } catch (QVCSException | IOException e) {
            LOGGER.log(Level.WARNING, "LogFileOperationCreateArchive exception: " + e.toString() + ": " + e.getMessage());
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            retVal = false;
        } finally {
            try {
                if (newArchiveStream != null) {
                    newArchiveStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                retVal = false;
            } finally {
                newArchiveStream = null;
            }
        }

        // Remove any old archives.
        if (retVal) {
            // It is safe to delete these since we don't have a valid archive file.
            getLogFileImpl().getOldFile().delete();
            getLogFileImpl().getFile().delete();

            if (getLogFileImpl().getTempFile().renameTo(getLogFileImpl().getFile())) {
                retVal = true;
            } else {
                throw new QVCSException("Unable to rename temporary copy of archive for " + getLogFileImpl().getShortWorkfileName());
            }
        } else {
            getLogFileImpl().getTempFile().delete();
        }
        return retVal;
    }

    private void createNewLogfileCreatingNewTrunkTip() throws QVCSException, IOException {
        // We need to know how big the origin workfile is for the header.
        File workfile = new File(filename);
        Date now = new Date();

        // Create the archive header
        LogFileHeaderInfo archiveHeader = new LogFileHeaderInfo();
        populateArchiveHeader(archiveHeader, workfile, now);
        getLogFileImpl().setHeaderInfo(archiveHeader);

        // Increment the number of revisions.
        archiveHeader.getLogFileHeader().incrementRevisionCount();

        // Increment the lock count if there is lock checking for this archive
        if (lockFlag && archiveHeader.getLogFileHeader().attributes().getIsCheckLock()) {
            archiveHeader.getLogFileHeader().incrementLockCount();
        }

        // Write the header.
        archiveHeader.write(newArchiveStream);

        // Create the revision header for this new revision.
        RevisionHeader newRevisionHeader = new RevisionHeader(new AccessList(archiveHeader.getAccessList()),
                new AccessList(archiveHeader.getModifierList()));
        newRevisionHeader.setCreator(userName);
        if (commandLineArgs.getCheckInTimestamp() != null) {
            newRevisionHeader.setCheckInDate(commandLineArgs.getCheckInTimestamp());
        } else {
            newRevisionHeader.setCheckInDate(now);
        }
        newRevisionHeader.setEditDate(commandLineArgs.getInputfileTimeStamp());
        newRevisionHeader.setRevisionDescription("Initial Revision");

        newRevisionHeader.setIsTip(true);
        if (lockFlag && archiveHeader.getLogFileHeader().attributes().getIsCheckLock()) {
            newRevisionHeader.setIsLocked(true);
            newRevisionHeader.setLocker(userName);
        }
        newRevisionHeader.setMajorNumber(1);
        newRevisionHeader.setMinorNumber(0);
        Compressor compressor = new ZlibCompressor();
        newRevisionHeader.setRevisionSize(getCompressedRevisionSizeForWorkfile(workfile, compressor));
        newRevisionHeader.setIsCompressed(compressor.getBufferIsCompressedFlag());
        newRevisionHeader.write(newArchiveStream);

        // Copy the workfile as the first revision.
        copyFromOneOpenFileToAnotherOpenFile(compressor.getCompressedStream(), newArchiveStream, newRevisionHeader.getRevisionSize());
    }

    private void populateArchiveHeader(LogFileHeaderInfo header, File workfile, Date now) throws QVCSException {
        header.setAccessList(accessList.getAccessListAsCommaSeparatedString());
        header.setModifierList(accessList.getAccessListAsCommaSeparatedString());
        header.setCommentPrefix(commentPrefix);
        header.setOwner(userName);

        // If no description is supplied, we'll just use the short workfile name
        // as the module description.
        if ((archiveDescription == null) || (archiveDescription.length() == 0)) {
            header.setModuleDescription(getLogFileImpl().getShortWorkfileName());
        } else {
            header.setModuleDescription(archiveDescription);
        }
        header.getLogFileHeader().getMajorNumber().setValue(1);
        header.getLogFileHeader().getMinorNumber().setValue(0);

        // Set this file's attributes.
        header.getLogFileHeader().setAttributes(archiveAttributes);

        // Set the stuff that goes in the supplemental info.
        header.setWorkfileName(workfileName);
        header.getSupplementalHeaderInfo().setLastWorkfileSize(workfile.length());
        header.getSupplementalHeaderInfo().setLastArchiveUpdateDate(now);
        header.getSupplementalHeaderInfo().setLastModifierIndex(accessList.userToIndex(userName));
        header.getSupplementalHeaderInfo().setFileID(FileIDManager.getInstance().getNewFileID());
    }

    private String getCommentPrefix() {
        String derivedCommentPrefix = commandLineArgs.getCommentPrefix();
        if ((derivedCommentPrefix != null) && (derivedCommentPrefix.length() > 0)) {
            return derivedCommentPrefix;
        } else {
            derivedCommentPrefix = CommentPrefixProperties.getInstance().getCommentPrefix(workfileName);
        }
        return derivedCommentPrefix;
    }
}
