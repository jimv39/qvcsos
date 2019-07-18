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
package com.qumasoft.server;

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.CompareFilesEditInformation;
import com.qumasoft.qvcslib.CompressionFactory;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogFileReadException;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.QumaAssert;
import com.qumasoft.qvcslib.RevisionDescriptor;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The logfile implementation class.
 *
 * @author Jim Voris
 */
public final class LogFileImpl {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileImpl.class);
    private static final String BRACKET_TO = "] to [";
    private RandomAccessFile inStream;
    /**
     * Flag to indicate if file is already open
     */
    private boolean isOpen;
    /**
     * Flag to indicate whether the header information has been read
     */
    private boolean isHeaderInfoReadFlag;
    private boolean isRevisionInfoReadFlag;
    private RevisionInformation revisionInfo;
    private final String fullArchiveFileName;
    private final String shortArchiveName;
    private final java.io.File archiveFile;
    private final String tempFileName;
    private final java.io.File tempFile;
    private final String oldArchiveFileName;
    private final java.io.File oldArchiveFile;
    private final String shortWorkfileName;
    private LogFileHeaderInfo headerInfo;
    private AccessList accessList = null;
    private AccessList modifierList = null;
    private boolean mustReadArchiveFileFlag = true;
    private LogfileInfo logfileInfo = null;

    RevisionHeader getRevisionHeader(int index) {
        RevisionHeader revisionHeader = null;
        if (index < getRevisionCount()) {
            revisionHeader = revisionInfo.getRevisionHeader(index);
        }
        return revisionHeader;
    }

    RevisionInformation getRevisionInformation() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }
        return revisionInfo;
    }

    int getFileID() {
        int fileID = -1;
        try {
            if (!isArchiveInformationRead()) {
                readInformation();
            }
            fileID = getLogFileHeaderInfo().getSupplementalHeaderInfo().getFileID();
        } catch (NullPointerException e) {
            LOGGER.warn("Failed to get fileID for: [{}]", getFileName());
            throw e;
        }
        return fileID;
    }

    void setFileIDAndRemoveViewAndBranchLabels(int fileID) throws QVCSException {
        try {
            if (!isArchiveInformationRead()) {
                readInformation();
            }
            getLogFileHeaderInfo().getSupplementalHeaderInfo().setFileID(fileID);
            removeViewAndBranchLabels();
            updateHeaderOnDisk();
        } catch (NullPointerException e) {
            LOGGER.warn("Failed to set fileID for: [{}]", getFileName());
            throw e;
        }
    }

    LogfileInfo getLogfileInfo() {
        if (logfileInfo == null) {
            logfileInfo = new LogfileInfo(getLogFileHeaderInfo(), getRevisionInformation(), getFileID(), getFileName());
        }
        return logfileInfo;
    }

    synchronized boolean isArchiveInformationRead() {
        return isHeaderInfoReadFlag && isRevisionInfoReadFlag;
    }

    boolean isValidArchive() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }
        return isArchiveInformationRead();
    }

    boolean getIsObsolete() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }
        return headerInfo.getIsObsolete();
    }

    int getRevisionCount() {
        int revisionCount = -1;
        try {
            if (!isArchiveInformationRead()) {
                readInformation();
            }
            revisionCount = headerInfo.getRevisionCount();
        } catch (NullPointerException e) {
            LOGGER.warn("Failed to get revision count for: [{}]", getFileName());
            throw e;
        }
        return revisionCount;
    }

    String getShortWorkfileName() {
        return shortWorkfileName;
    }

    String getShortArchiveName() {
        return shortArchiveName;
    }

    int getLockCount() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }

        return headerInfo.getLogFileHeader().lockCount();
    }

    File getFile() {
        return archiveFile;
    }

    String getFileName() {
        return fullArchiveFileName;
    }

    File getOldFile() {
        return oldArchiveFile;
    }

    File getTempFile() {
        return tempFile;
    }

    String getLockedByString() {
        return getLogfileInfo().getLockedByString();
    }

    /**
     * Return the user name(s) that hold any locks on this archive. The format of the returned string is for use within the GUI. If
     * there are multiple lockers, they are all returned.
     */
    String getLockedByUser() {
        String returnString;

        if (!isArchiveInformationRead()) {
            readInformation();
        }

        if (isArchiveInformationRead()) {
            if (headerInfo.getLogFileHeader().lockCount() > 0) {
                StringBuilder lockerString = new StringBuilder();
                int revisionCount = headerInfo.getRevisionCount();
                int lockCount = headerInfo.getLogFileHeader().lockCount();
                for (int i = 0, j = 0; (i < revisionCount) && (j < lockCount); i++) {
                    RevisionHeader revHeader = revisionInfo.getRevisionHeader(i);
                    if (revHeader.isLocked()) {
                        j++;
                        lockerString.append(revHeader.getRevisionString()).append("-").append(indexToUsername(revHeader.getLockerIndex()));
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

    String getWorkfileInLocation() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }

        String returnString = "";
        if (headerInfo.getLogFileHeader().lockCount() > 0) {
            returnString = headerInfo.getWorkfileName();
        }
        return returnString;
    }

    java.util.Date getLastCheckInDate() {
        java.util.Date lastCheckInDate = null;
        RevisionHeader defaultRevision = getDefaultRevisionHeader();
        if (defaultRevision != null) {
            lastCheckInDate = defaultRevision.getCheckInDate();
        }
        return lastCheckInDate;
    }

    String getLastEditBy() {
        String lastEditBy = null;
        RevisionHeader defaultRevision = getDefaultRevisionHeader();
        if (defaultRevision != null) {
            lastEditBy = indexToUsername(defaultRevision.getCreatorIndex());
        }
        return lastEditBy;
    }

    RevisionHeader getDefaultRevisionHeader() {
        RevisionHeader returnHeader = null;

        if (!isArchiveInformationRead()) {
            readInformation();
        }

        if (isArchiveInformationRead()) {
            // If the default branch isn't the trunk, then we have some work to do...
            if (headerInfo.getLogFileHeader().defaultDepth() > 0) {
                int revisionCount = headerInfo.getRevisionCount();
                RevisionDescriptor defaultDescriptor = headerInfo.getDefaultRevisionDescriptor();
                String defaultBranchString = defaultDescriptor.toString();
                for (int i = 0; i < revisionCount; i++) {
                    RevisionHeader revHeader = revisionInfo.getRevisionHeader(i);
                    if (revHeader.isTip() && (revHeader.getDepth() == defaultDescriptor.getElementCount() - 1)) {
                        String revisionString = revHeader.getRevisionString();
                        int lastDot = revisionString.lastIndexOf('.');
                        String truncatedRevisionString = revisionString.substring(0, lastDot);
                        if (truncatedRevisionString.compareToIgnoreCase(defaultBranchString) == 0) {
                            returnHeader = revHeader;
                            break;
                        }
                    }
                }
            } else {
                // The default branch is the trunk.  Things are simple here.
                returnHeader = revisionInfo.getRevisionHeader(0);
            }
        }

        return returnHeader;
    }

    String indexToUsername(int index) {
        if (!isArchiveInformationRead()) {
            readInformation();
        }

        return modifierList.indexToUser(index);
    }

    /**
     * This recursive routine will fetch the requested revision into the output file. This routine does handle compressed revisions,
     * but it does not perform keyword expansion.
     */
    synchronized boolean fetchRevision(RevisionHeader revisionHeader, String outputFilename, boolean recurseFlag, MutableByteArray processedBuffer) {
        boolean bRetVal = false;

        try {
            if ((revisionHeader.getDepth() == 0) && revisionHeader.isTip()) {
                // Read the archive file to retrieve the revision.
                byte[] unCompressedRevisionData;
                byte[] revisionData = new byte[revisionHeader.getRevisionSize()];
                inStream.seek(revisionHeader.getRevisionDataStartPosition());
                readRevisionData(revisionData);

                // Decompress the buffer if we need to.
                if (revisionHeader.isCompressed()) {
                    unCompressedRevisionData = deCompressRevisionData(revisionHeader, revisionData);
                } else {
                    unCompressedRevisionData = revisionData;
                }
                processedBuffer.setValue(unCompressedRevisionData);
                bRetVal = true;
            } else {
                // They are requesting an older revision.
                if (revisionHeader.getParentRevisionHeader() == null) {
                    // We've reached the tip revision for the requested revision.
                    // but we're recursing and this must be a non-tip revision.
                    // All non-tip revisions MUST have a parent, so to get here
                    // is a boo-boo.
                    QumaAssert.isTrue(false);
                } else {
                    // We're still working our way to the tip revision.
                    bRetVal = fetchRevision(revisionHeader.getParentRevisionHeader(), outputFilename, true, processedBuffer);

                    // We got our parent.  Now apply our edits to our parent to get the result.
                    if (bRetVal) {
                        byte[] editBuffer = new byte[revisionHeader.getRevisionSize()];
                        byte[] uncompressedEditBuffer;
                        inStream.seek(revisionHeader.getRevisionDataStartPosition());
                        readRevisionData(editBuffer);
                        if (revisionHeader.isCompressed()) {
                            uncompressedEditBuffer = deCompressRevisionData(revisionHeader, editBuffer);
                        } else {
                            uncompressedEditBuffer = editBuffer;
                        }
                        byte[] afterEdits = applyEdits(uncompressedEditBuffer, processedBuffer.getValue());
                        processedBuffer.setValue(afterEdits);
                        bRetVal = true;
                    }
                }
            }
        } catch (IOException | QVCSException e) {
            LOGGER.warn("Failed to fetch revision: [{}]", revisionHeader.getRevisionString());
            LOGGER.warn(e.getLocalizedMessage(), e);
            bRetVal = false;
        }

        // Write the result.
        if (!recurseFlag && bRetVal && (outputFilename != null)) {
            try {
                WorkFile outputFile = new WorkFile(outputFilename);

                // Overwrite if it is write protected.
                if (outputFile.exists()) {
                    if (!outputFile.canWrite()) {
                        outputFile.setReadWrite();
                    }
                }
                try (FileOutputStream outStream = new FileOutputStream(outputFile)) {
                    Utility.writeDataToStream(processedBuffer.getValue(), outStream);
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                bRetVal = false;
            }
        }
        return bRetVal;
    }

    @SuppressWarnings("LoggerStringConcat")
    private byte[] applyEdits(byte[] edits, byte[] originalData) throws QVCSException {
        DataInputStream editStream = new DataInputStream(new ByteArrayInputStream(edits));
        CompareFilesEditInformation editInfo = new CompareFilesEditInformation();
        byte[] editedBuffer = new byte[edits.length + originalData.length]; // It can't be any bigger than this.
        byte[] returnedBuffer = null;
        int inIndex = 0;
        int outIndex = 0;
        int deletedBytesCount;
        int insertedBytesCount;
        int bytesTillChange = 0;

        try {
            while (editStream.available() > 0) {
                editInfo.read(editStream);
                bytesTillChange = (int) editInfo.getSeekPosition() - inIndex;
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, bytesTillChange);

                inIndex += bytesTillChange;
                outIndex += bytesTillChange;

                deletedBytesCount = (int) editInfo.getDeletedBytesCount();
                insertedBytesCount = (int) editInfo.getInsertedBytesCount();

                switch (editInfo.getEditType()) {
                    case CompareFilesEditInformation.QVCS_EDIT_DELETE:
                        /*
                         * Delete input
                         */
                        // Just skip over deleted bytes
                        inIndex += deletedBytesCount;
                        break;

                    case CompareFilesEditInformation.QVCS_EDIT_INSERT:
                        /*
                         * Insert edit lines
                         */
                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    case CompareFilesEditInformation.QVCS_EDIT_REPLACE:
                        /*
                         * Replace input line with edit line.
                         * First skip over the bytes to be replaced, then copy the replacing bytes from the edit file to the output
                         * file.
                         */
                        inIndex += deletedBytesCount;

                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    default:
                        continue;
                }
            }

            // Copy the rest of the input "file" to the output "file".
            int remainingBytes = originalData.length - inIndex;
            if (remainingBytes > 0) {
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, remainingBytes);
                outIndex += remainingBytes;
            }
            returnedBuffer = new byte[outIndex];
            System.arraycopy(editedBuffer, 0, returnedBuffer, 0, outIndex);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            LOGGER.warn(" editInfo.seekPosition: " + editInfo.getSeekPosition() + " originalData.length: " + originalData.length + " inIndex: "
                    + inIndex + " editedBuffer.length: "
                    + editedBuffer.length + " outIndex: " + outIndex + " bytesTillChange: " + bytesTillChange);
            throw new QVCSException("Internal error!! for " + getShortWorkfileName());
        } finally {
            try {
                editStream.close();
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
        return returnedBuffer;
    }

    /**
     * Find the requested revision within the archive. Return true if we find that revision; also return the index of that revision.
     * If the revision is not found, return false.
     */
    synchronized boolean findRevision(String revision, AtomicInteger revisionIndex) {
        QumaAssert.isTrue(isHeaderInfoReadFlag);
        QumaAssert.isTrue(isRevisionInfoReadFlag);

        boolean bRetVal = false;
        int revisionCount = headerInfo.getRevisionCount();
        for (int i = 0; i < revisionCount; i++) {
            RevisionHeader revHeader = revisionInfo.getRevisionHeader(i);
            String revisionString = revHeader.getRevisionString();
            if (revision.compareTo(revisionString) == 0) {
                bRetVal = true;
                revisionIndex.set(i);
                break;
            }
        }

        return bRetVal;
    }

    private byte[] deCompressRevisionData(RevisionHeader revisionHeader, byte[] revisionData) {
        Compressor compressor = CompressionFactory.getCompressor(revisionHeader.getCompressionHeader());
        return compressor.expand(revisionHeader.getCompressionHeader(), revisionData);
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();
        returnString.append("QVCS archive name:\t").append(fullArchiveFileName).append(headerInfo.toString());
        return returnString.toString();
    }

    /**
     * Create a logfile implementation instance.
     * @param fullArchiveFilename the full path to the file that we use as the archive file.
     */
    public LogFileImpl(String fullArchiveFilename) {
        isOpen = false;
        isHeaderInfoReadFlag = false;
        isRevisionInfoReadFlag = false;

        // Set the filename
        fullArchiveFileName = fullArchiveFilename;
        archiveFile = new File(fullArchiveFileName);

        // Set the name of the temp file for the archive
        tempFileName = fullArchiveFileName + QVCSConstants.QVCS_ARCHIVE_TEMPFILE_SUFFIX;
        tempFile = new java.io.File(tempFileName);

        // Set the name of the old file for the archive
        oldArchiveFileName = fullArchiveFileName + QVCSConstants.QVCS_ARCHIVE_OLDFILE_SUFFIX;
        oldArchiveFile = new java.io.File(oldArchiveFileName);

        // Figure out the short workfile name
        shortWorkfileName = Utility.convertArchiveNameToShortWorkfileName(fullArchiveFilename);

        // Figure out the short archive name
        shortArchiveName = Utility.convertWorkfileNameToShortArchiveName(shortWorkfileName);
    }

    /**
     * open the archive file.
     */
    synchronized boolean open() {
        if (!isOpen) {
            // Make sure the file exists
            if (archiveFile.exists()) {
                try {
                    inStream = new RandomAccessFile(archiveFile, "r");
                    isOpen = true;
                } catch (FileNotFoundException e) {
                    isOpen = false;
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        return isOpen;
    }

    protected synchronized void close() {
        if (isOpen) {
            try {
                inStream.close();
                isOpen = false;
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    synchronized boolean reReadInformation() {
        isHeaderInfoReadFlag = false;
        isRevisionInfoReadFlag = false;
        logfileInfo = null;
        return readInformation();
    }

    synchronized boolean readInformation() {
        boolean bRetValue = true;
        try {
            if (open()) {
                bRetValue = readHeaderInformation();
                if (bRetValue) {
                    bRetValue = readRevisionInformation();
                }
            }
        } catch (Exception e) {
            bRetValue = false;
        } finally {
            close();
        }
        return bRetValue;
    }

    /**
     * read revision information for this archive
     */
    private synchronized boolean readRevisionInformation() {
        try {
            if (open() && !isHeaderInfoReadFlag) {
                headerInfo = new LogFileHeaderInfo();
                headerInfo.read(inStream);
            }
            if (isHeaderInfoReadFlag && !isRevisionInfoReadFlag) {
                revisionInfo = new RevisionInformation(headerInfo.getRevisionCount(),
                        new AccessList(headerInfo.getAccessList()),
                        new AccessList(headerInfo.getModifierList()));
                revisionInfo.read(inStream);
                isRevisionInfoReadFlag = true;
            }
        } catch (LogFileReadException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            isRevisionInfoReadFlag = false;
        }
        return isRevisionInfoReadFlag;
    }

    /**
     * read header information for this archive
     */
    private synchronized boolean readHeaderInformation() {
        try {
            if (open() && !isHeaderInfoReadFlag) {
                headerInfo = new LogFileHeaderInfo();
                isHeaderInfoReadFlag = headerInfo.read(inStream);
                if (isHeaderInfoReadFlag) {
                    accessList = new AccessList(headerInfo.getAccessList());
                    modifierList = new AccessList(headerInfo.getModifierList());
                }
            }
        } catch (LogFileReadException e) {
            LOGGER.warn("Failed to read header for: [{}]", getFileName());
            LOGGER.warn(e.getLocalizedMessage(), e);
            isHeaderInfoReadFlag = false;
        }
        return isHeaderInfoReadFlag;
    }

    LogFileHeaderInfo getLogFileHeaderInfo() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }
        return headerInfo;
    }

    void setHeaderInfo(LogFileHeaderInfo hdrInfo) throws QVCSException {
        if (isArchiveInformationRead()) {
            throw new QVCSException("Invalid use of setHeaderInfo method!!");
        } else {
            this.headerInfo = hdrInfo;
            mustReadArchiveFileFlag = false;
        }
    }

    boolean createArchive(CreateArchiveCommandArgs commandLineArgs, AbstractProjectProperties projectProperties, String filename) throws QVCSException {
        boolean retVal = false;
        // <editor-fold>
        Object[] args = new Object[4];
        args[0] = this;
        args[1] = filename;
        args[2] = projectProperties;
        args[3] = commandLineArgs;
        // </editor-fold>
        LogFileOperationCreateArchive createArchiveOperation = new LogFileOperationCreateArchive(args);
        if (createArchiveOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    boolean renameArchive(String userName, String appendedPath, String oldShortWorkfileName, String newShortWorkfilename, final Date date) {
        boolean retVal = false;
        try {
            // See if we can rename the archive file...
            String archiveDirectoryName = fullArchiveFileName.substring(0, fullArchiveFileName.length() - shortArchiveName.length());
            String newShortArchiveFilename = Utility.convertWorkfileNameToShortArchiveName(newShortWorkfilename);
            String newFullArchiveFilename = archiveDirectoryName + newShortArchiveFilename;
            File newArchiveFile = new File(newFullArchiveFilename);
            if (!newArchiveFile.exists()) {
                // We need to create new revision (with no changes) that marks
                // the boundary for this rename.  We need to do this so that
                // a 'get by label' will work correctly.  We'll use a revision
                // comment that indicates the revision was created by the rename
                // operation.
                String checkInComment = QVCSConstants.QVCS_INTERNAL_FILE_RENAMED_FROM + oldShortWorkfileName + BRACKET_TO + newShortWorkfilename + "].";
                LogFileOperationRenameArchive renameArchiveOperation = new LogFileOperationRenameArchive(this, userName, checkInComment, appendedPath, oldShortWorkfileName, date);
                if (renameArchiveOperation.execute()) {
                    retVal = reReadInformation();
                }

                // Rename the archive file...
                if (!archiveFile.renameTo(newArchiveFile)) {
                    LOGGER.warn("Failed to rename [" + getFileName() + BRACKET_TO + newFullArchiveFilename + "]");
                }
            } else {
                LOGGER.warn("Rename of archive failed because new archive name already exists: [" + newFullArchiveFilename + "]");
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        }
        return retVal;
    }

    boolean moveArchive(String userName, String appendedPath, ArchiveDirManager targetArchiveDirManager, String shortWorkfilename, final Date date) {
        boolean retVal = false;
        try {
            // Make sure we have a fileID...
            int fileID = getFileID();
            LOGGER.trace("File id in moveArchive is: [{}]", fileID);

            // See if we can rename the archive file...
            String newFullArchiveFilename = targetArchiveDirManager.getArchiveDirectoryName() + File.separator + shortArchiveName;
            File newArchiveFile = new File(newFullArchiveFilename);
            if (!newArchiveFile.exists()) {
                // We need to create new revision (with no changes) that marks
                // the boundary for this rename.  We need to do this so that
                // a 'get by label' will work correctly.  We'll use a revision
                // comment that indicates the revision was created by the move
                // operation.
                String oldFullAppendedWorkfileName;
                if (appendedPath.length() > 0) {
                    oldFullAppendedWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortWorkfilename;
                } else {
                    oldFullAppendedWorkfileName = shortWorkfilename;
                }
                String newFullAppendedWorkfileName;
                if (targetArchiveDirManager.getAppendedPath().length() > 0) {
                    newFullAppendedWorkfileName = targetArchiveDirManager.getAppendedPath() + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortWorkfilename;
                } else {
                    newFullAppendedWorkfileName = shortWorkfilename;
                }
                String checkInComment = QVCSConstants.QVCS_INTERNAL_FILE_MOVED_FROM + oldFullAppendedWorkfileName + BRACKET_TO + newFullAppendedWorkfileName + "].";
                LogFileOperationMoveArchive moveArchiveOperation = new LogFileOperationMoveArchive(this, userName, checkInComment, appendedPath, shortWorkfilename, date);
                if (moveArchiveOperation.execute()) {
                    retVal = reReadInformation();
                }

                // Move the archive file.
                if (retVal) {
                    if (!archiveFile.renameTo(newArchiveFile)) {
                        LOGGER.warn("Failed to move [" + getFileName() + BRACKET_TO + newFullArchiveFilename + "]");
                        retVal = false;
                    }
                } else {
                    LOGGER.warn("Move of archive failed.  Failed to re-read archive information for: [{}]", oldFullAppendedWorkfileName);
                }
            } else {
                LOGGER.warn("Move of archive failed because new archive name already exists: [{}]", newFullArchiveFilename);
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        }
        return retVal;
    }

    boolean deleteArchiveOnTranslucentBranch(String userName, String appendedPath, String shortWorkfilename, final Date date,
            final String branchLabel, ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) {
        boolean retVal = false;

        // Check in a new revision (creating a file branch if required) marking the delete.
        String branchTipRevisionString = archiveInfoForTranslucentBranch.getBranchTipRevisionString();
        if (branchTipRevisionString.length() > 0) {
            try {
                // We need to create new revision (with no changes) that marks
                // the boundary for this delete.  We need to do this so that
                // a 'get by label' will work correctly.  We'll use a revision
                // comment that indicates the revision was created by the delete
                // operation.
                String oldFullAppendedWorkfileName;
                if (appendedPath.length() > 0) {
                    oldFullAppendedWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortWorkfilename;
                } else {
                    oldFullAppendedWorkfileName = shortWorkfilename;
                }
                String checkInComment = "Deleted: '" + oldFullAppendedWorkfileName + "' to cemetery.";
                LogFileOperationDeleteArchiveOnTranslucentBranch deleteArchiveOperation
                        = new LogFileOperationDeleteArchiveOnTranslucentBranch(archiveInfoForTranslucentBranch, userName,
                        checkInComment, appendedPath, shortWorkfilename, date, branchLabel);
                retVal = deleteArchiveOperation.execute();
            } catch (QVCSException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                retVal = false;
            }
        }
        return retVal;
    }

    boolean moveArchiveOnTranslucentBranch(String userName, String appendedPath, ArchiveDirManagerForTranslucentBranch targetArchiveDirManager, String shortWorkfilename,
            final Date date,
            final String branchLabel, ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) {
        boolean retVal = false;

        // Check in a new revision (creating a file branch if required) marking the move.
        String branchTipRevisionString = archiveInfoForTranslucentBranch.getBranchTipRevisionString();
        if (branchTipRevisionString.length() > 0) {
            try {
                // We need to create new revision (with no changes) that marks
                // the boundary for this move.  We need to do this so that
                // a 'get by label' will work correctly.  We'll use a revision
                // comment that indicates the revision was created by the move
                // operation.
                String oldFullAppendedWorkfileName;
                if (appendedPath.length() > 0) {
                    oldFullAppendedWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortWorkfilename;
                } else {
                    oldFullAppendedWorkfileName = shortWorkfilename;
                }
                String newFullAppendedWorkfileName;
                if (targetArchiveDirManager.getAppendedPath().length() > 0) {
                    newFullAppendedWorkfileName = targetArchiveDirManager.getAppendedPath() + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortWorkfilename;
                } else {
                    newFullAppendedWorkfileName = shortWorkfilename;
                }
                String checkInComment = QVCSConstants.QVCS_INTERNAL_FILE_MOVED_FROM + oldFullAppendedWorkfileName + " to [" + newFullAppendedWorkfileName + "].";
                LogFileOperationMoveArchiveOnTranslucentBranch moveArchiveOperation = new LogFileOperationMoveArchiveOnTranslucentBranch(archiveInfoForTranslucentBranch,
                        userName, checkInComment,
                        appendedPath, shortWorkfilename, date, branchLabel);
                retVal = moveArchiveOperation.execute();
            } catch (QVCSException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                retVal = false;
            }
        }
        return retVal;
    }

    boolean renameArchiveOnTranslucentBranch(String userName, String appendedPath, String oldShortWorkfilename, String newShortWorkfileName, final Date date,
            final String branchLabel, ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) {
        boolean retVal = false;

        // Check in a new revision (creating a file branch if required) marking the rename.
        String branchTipRevisionString = archiveInfoForTranslucentBranch.getBranchTipRevisionString();
        if (branchTipRevisionString.length() > 0) {
            try {
                // We need to create new revision (with no changes) that marks
                // the boundary for this rename.  We need to do this so that
                // a 'get by label' will work correctly.  We'll use a revision
                // comment that indicates the revision was created by the rename
                // operation.
                String oldFullAppendedWorkfileName;
                String newFullAppendedWorkfileName;
                if (appendedPath.length() > 0) {
                    oldFullAppendedWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + oldShortWorkfilename;
                    newFullAppendedWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + newShortWorkfileName;
                } else {
                    oldFullAppendedWorkfileName = oldShortWorkfilename;
                    newFullAppendedWorkfileName = newShortWorkfileName;
                }
                String checkInComment = QVCSConstants.QVCS_INTERNAL_FILE_RENAMED_FROM + oldFullAppendedWorkfileName + BRACKET_TO + newFullAppendedWorkfileName + "].";
                LogFileOperationRenameArchiveOnTranslucentBranch renameArchiveOperation
                        = new LogFileOperationRenameArchiveOnTranslucentBranch(archiveInfoForTranslucentBranch, userName,
                        checkInComment, appendedPath, oldShortWorkfilename, date, branchLabel);
                retVal = renameArchiveOperation.execute();
            } catch (QVCSException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                retVal = false;
            }
        }
        return retVal;
    }

    boolean deleteArchive(String userName, String appendedPath, ArchiveDirManager cemeteryArchiveDirManager, String shortWorkfilename, final Date date) {
        boolean retVal = false;
        try {
            // Make sure we have a fileID...
            int fileID = getFileID();

            // See if we can rename the archive file...
            String newShortArchiveName = Utility.createCemeteryShortArchiveName(fileID);
            String newFullArchiveFilename = cemeteryArchiveDirManager.getArchiveDirectoryName() + File.separator + newShortArchiveName;
            File newArchiveFile = new File(newFullArchiveFilename);
            if (!newArchiveFile.exists()) {
                // We need to create new revision (with no changes) that marks
                // the boundary for this delete.  We need to do this so that
                // a 'get by label' will work correctly.  We'll use a revision
                // comment that indicates the revision was created by the delete
                // operation.
                String oldFullAppendedWorkfileName;
                if (appendedPath.length() > 0) {
                    oldFullAppendedWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortWorkfilename;
                } else {
                    oldFullAppendedWorkfileName = shortWorkfilename;
                }
                String checkInComment = "Deleted: '" + oldFullAppendedWorkfileName + "' to cemetery archive file: '" + newFullArchiveFilename + "'.";
                LogFileOperationMoveArchive moveArchiveOperation = new LogFileOperationMoveArchive(this, userName, checkInComment, appendedPath, shortWorkfilename, date);
                if (moveArchiveOperation.execute()) {
                    retVal = reReadInformation();
                }

                // Move the archive file.
                if (retVal) {
                    // Make sure the cemetery exists.
                    if (!newArchiveFile.getParentFile().exists()) {
                        newArchiveFile.getParentFile().mkdirs();
                    }

                    // Make sure the original archive is read/write.
                    if (!archiveFile.canWrite()) {
                        WorkFile oldFile = new WorkFile(archiveFile.getPath());
                        oldFile.setReadWrite();
                    }

                    // And move the archive to the cemetery.
                    if (!archiveFile.renameTo(newArchiveFile)) {
                        LOGGER.warn("Failed to move [" + getFileName() + BRACKET_TO + newFullArchiveFilename + "] in project cemetery.");
                        retVal = false;
                    }
                } else {
                    LOGGER.warn("Delete of archive failed.  Failed to re-read archive information for: [{}]", oldFullAppendedWorkfileName);
                }
            } else {
                LOGGER.warn("Delete of archive failed because archive name already exists in project cemetery: [{}]", newFullArchiveFilename);
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        }
        return retVal;
    }

    /**
     * Get a revision from the archive file and write it into the filename provided. Return true if successful, false otherwise.
     * Keywords are NOT expanded by this method.
     */
    boolean getRevision(GetRevisionCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        boolean retVal = false;
        // <editor-fold>
        Object[] args = new Object[3];
        args[0] = this;
        args[1] = fetchToFileName;
        args[2] = commandLineArgs;
        // </editor-fold>

        LogFileOperationGetRevision getOperation = new LogFileOperationGetRevision(args);
        if (getOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    boolean checkOutRevision(CheckOutCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        boolean retVal = false;
        // <editor-fold>
        Object[] args = new Object[3];
        args[0] = this;
        args[1] = fetchToFileName;
        args[2] = commandLineArgs;
        // </editor-fold>

        LogFileOperationCheckOut checkOutOperation = new LogFileOperationCheckOut(args);
        if (checkOutOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    boolean lockRevision(LockRevisionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;
        Object[] args = new Object[2];
        args[0] = this;
        args[1] = commandLineArgs;

        LogFileOperationLockRevision lockRevisionOperation = new LogFileOperationLockRevision(args);
        if (lockRevisionOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    boolean checkInRevision(CheckInCommandArgs commandLineArgs, String filename, boolean ignoreLocksToEnableBranchCheckInFlag) throws QVCSException {
        boolean retVal = false;
        // <editor-fold>
        Object[] args = new Object[3];
        args[0] = this;
        args[1] = filename;
        args[2] = commandLineArgs;
        // </editor-fold>
        LogFileOperationCheckIn checkInOperation = new LogFileOperationCheckIn(args, ignoreLocksToEnableBranchCheckInFlag);
        if (checkInOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    boolean unlockRevision(UnlockRevisionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;
        Object[] args = new Object[2];
        args[0] = this;
        args[1] = commandLineArgs;

        LogFileOperationUnlockRevision unlockRevisionOperation = new LogFileOperationUnlockRevision(args);
        if (unlockRevisionOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    boolean unlockRevision(String userName, AtomicReference<String> revisionString) throws QVCSException {
        boolean retVal = false;

        // Make sure user is on the access list.
        makeSureIsOnAccessList(userName);

        RevisionHeader defaultRevisionHeader = getDefaultRevisionHeader();
        if (defaultRevisionHeader == null) {
            throw new QVCSException("Unable to get default revision header for [" + getShortWorkfileName() + "]");
        }

        // Figure out the default revision string if we need to.
        if (0 == revisionString.get().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
            revisionString.set(defaultRevisionHeader.getRevisionString());
        }

        // Make sure the revision is already locked.
        AtomicInteger revisionIndex = new AtomicInteger();
        if (!isRevisionLocked(revisionString, revisionIndex)) {
            throw new QVCSException("Revision [" + revisionString + "] is not locked for [" + getShortWorkfileName() + "]");
        }

        RevisionHeader revisionHeader = getRevisionHeader(revisionIndex.get());
        if (revisionHeader == null) {
            throw new QVCSException("Unable to get revision header at index: [" + revisionIndex.get() + "] for file: [" + getShortWorkfileName() + "]");
        }

        // Make sure the current user holds the lock on the given revision.
        if (modifierList.userToIndex(userName) != revisionHeader.getLockerIndex()) {
            throw new QVCSException("Revision [" + revisionString + "] for [" + getShortWorkfileName() + "] is not locked by [" + userName + "]");
        }

        // Make a copy of the archive.  We'll operate on this copy.
        if (!createCopyOfArchive()) {
            throw new QVCSException("Unable to create temporary copy of archive for [" + getShortWorkfileName() + "]");
        }

        java.io.RandomAccessFile ioStream = null;
        try {
            ioStream = new java.io.RandomAccessFile(tempFile, "rw");

            // Seek to the location of this revision in the file.
            ioStream.seek(revisionHeader.getRevisionStartPosition());

            // Update the revision information before we write it out.
            revisionHeader.setIsLocked(false);
            revisionHeader.setLockerIndex(0);

            // Write the updated revision info.
            revisionHeader.updateInPlace(ioStream);

            // Update the header with info about this locker.
            getLogFileHeaderInfo().getLogFileHeader().decrementLockCount();

            // Update the header information in the stream.
            getLogFileHeaderInfo().updateInPlace(ioStream);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        } finally {
            try {
                if (ioStream != null) {
                    ioStream.close();
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                retVal = false;
            }
        }

        // Remove any old archives.
        oldArchiveFile.delete();
        if (archiveFile.renameTo(oldArchiveFile)) {
            if (tempFile.renameTo(archiveFile)) {
                retVal = true;
                oldArchiveFile.delete();
            } else {
                oldArchiveFile.renameTo(archiveFile);
                throw new QVCSException("Unable to rename temporary copy of archive for [" + getShortWorkfileName() + "]");
            }
        } else {
            throw new QVCSException("Unable to rename archive file for [" + getShortWorkfileName() + "]");
        }
        return retVal;
    }

    boolean labelRevision(LabelRevisionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;
        Object[] args = new Object[2];
        args[0] = this;
        args[1] = commandLineArgs;

        LogFileOperationLabelRevision labelRevisionOperation = new LogFileOperationLabelRevision(args);
        if (labelRevisionOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    boolean unLabelRevision(UnLabelRevisionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;
        Object[] args = new Object[2];
        args[0] = this;
        args[1] = commandLineArgs;

        LogFileOperationUnLabelRevision unLabelRevisionOperation = new LogFileOperationUnLabelRevision(args);
        if (unLabelRevisionOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    synchronized AccessList getAccessList() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }
        return accessList;
    }

    AccessList getModifierList() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }
        return modifierList;
    }

    /**
     * Make sure the given user is on the access list for this archive. If the user is not on the access list, this method will
     * automatically add the user to the access list (and modifier list), and update the archive file.
     */
    void makeSureIsOnAccessList(String userName) throws QVCSException {
        boolean success = false;

        boolean accessListFlag = getAccessList().addUser(userName);
        boolean modifierListFlag = getModifierList().addUser(userName);

        // If we updated either access list or modifier list, we need
        // to re-write the archive file.
        if (accessListFlag || modifierListFlag) {
            // Need to update the string copy of the access list maintained
            // in the header.

            // Need to re-write the archive to have the modified header
            // information.
            RandomAccessFile newArchiveStream = null;
            try {
                // Need to re-write the archive to have the modified header
                // information.
                getLogFileHeaderInfo().setAccessList(getAccessList().getAccessListAsCommaSeparatedString());
                getLogFileHeaderInfo().setModifierList(getModifierList().getAccessListAsCommaSeparatedString());

                newArchiveStream = new RandomAccessFile(getTempFile(), "rw");
                try (RandomAccessFile oldArchiveStream = new RandomAccessFile(getFile(), "r")) {

                    // Figure out where we need to copy from in the existing archive
                    // file... (It's the beginning of all revision info).
                    RevisionHeader revInfo = getRevisionHeader(0);
                    if (revInfo != null) {
                        long revStartPosition = revInfo.getRevisionStartPosition();
                        oldArchiveStream.seek(revStartPosition);

                        // Write the new header out to the new archive file.
                        getLogFileHeaderInfo().write(newArchiveStream);

                        // And copy the rest of the existing archive to the new one.
                        long bytesToCopy = oldArchiveStream.length() - revStartPosition;
                        AbstractLogFileOperation.copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, bytesToCopy);

                        success = true;
                    }
                }
            } catch (IOException e) {
                throw new QVCSException("Exception in makeSureIsOnAccessList(): " + e.getLocalizedMessage());
            } finally {
                try {
                    if (newArchiveStream != null) {
                        newArchiveStream.close();
                    }
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                    success = false;
                }

                if (success) {
                    replaceExistingArchiveWithNewTempArchive();
                    reReadInformation();

                } else {
                    getTempFile().delete();
                }
            }
        }
    }

    void replaceExistingArchiveWithNewTempArchive() throws QVCSException {
        getOldFile().delete();
        if (getFile().renameTo(getOldFile())) {
            if (getTempFile().renameTo(getFile())) {
                getOldFile().delete();
            } else {
                getOldFile().renameTo(getFile());
                throw new QVCSException("Unable to rename temporary copy of archive for [" + getShortWorkfileName() + "]");
            }
        } else {
            throw new QVCSException("Unable to rename archive file for [" + getShortWorkfileName() + "]");
        }
    }

    boolean isRevisionLocked(AtomicReference<String> revisionString, AtomicInteger revisionIndex) throws QVCSException {
        boolean retVal = true;

        // Figure out the default revision string if we need to.
        if (0 == revisionString.get().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
            RevisionHeader revisionHeader = getDefaultRevisionHeader();
            if (revisionHeader != null) {
                revisionString.set(revisionHeader.getRevisionString());
            } else {
                throw new QVCSRuntimeException("Unable to get default revision header!");
            }
        }

        if (findRevision(revisionString.get(), revisionIndex)) {
            RevisionHeader revInfo = getRevisionHeader(revisionIndex.get());
            if (revInfo != null) {
                retVal = revInfo.isLocked();
            } else {
                throw new QVCSRuntimeException("Unable to get revision header!");
            }
        } else {
            throw new QVCSException("Revision [" + revisionString.get() + "] not found in [" + getShortWorkfileName() + "]");
        }
        return retVal;
    }

    String getLockedRevisionString(String userName) {
        String retVal = null;
        RevisionInformation revisionInformation = getRevisionInformation();
        AccessList localModifierList = revisionInformation.getModifierList();
        int revisionCount = getLogFileHeaderInfo().getRevisionCount();
        for (int i = 0; i < revisionCount; i++) {
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
            if (revHeader.isLocked()) {
                String lockerName = localModifierList.indexToUser(revHeader.getLockerIndex());
                if (0 == lockerName.compareTo(userName)) {
                    retVal = revHeader.getRevisionString();
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Determine whether the given user holds any locks for this archive.
     */
    boolean isLockedByUser(String userName) {
        boolean retVal = false;

        if (!isArchiveInformationRead()) {
            readInformation();
        }

        if (isArchiveInformationRead()) {
            if (headerInfo.getLogFileHeader().lockCount() > 0) {
                int revisionCount = headerInfo.getRevisionCount();
                int lockCount = headerInfo.getLogFileHeader().lockCount();
                for (int i = 0, j = 0; (i < revisionCount) && (j < lockCount); i++) {
                    RevisionHeader revHeader = revisionInfo.getRevisionHeader(i);
                    if (revHeader.isLocked()) {
                        j++;
                        if (0 == userName.compareTo(indexToUsername(revHeader.getLockerIndex()))) {
                            retVal = true;
                            break;
                        }
                    }
                }
            }
        }
        return retVal;
    }

    boolean createCopyOfArchive() {
        boolean retVal = false;
        try {
            // Make sure the temp file is gone.
            tempFile.delete();

            // Copy the archive to the tempfile.
            retVal = copyFile(archiveFile, tempFile);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return retVal;
    }

    boolean copyFile(java.io.File fromFile, java.io.File toFile) throws IOException {
        boolean retVal = true;
        try (FileChannel fromChannel = new FileInputStream(fromFile).getChannel()) {
            try (FileChannel toChannel = new FileOutputStream(toFile).getChannel()) {
                toChannel.transferFrom(fromChannel, 0L, fromChannel.size());
            }
        }

        return retVal;
    }

    ArchiveAttributes getAttributes() {
        if (mustReadArchiveFileFlag && !isArchiveInformationRead()) {
            readInformation();
        }

        return headerInfo.getLogFileHeader().attributes();
    }

    boolean setAttributes(String userName, ArchiveAttributes attributes) throws QVCSException {
        boolean retVal = false;
        LogFileOperationSetAttributes setAttributesOperation = new LogFileOperationSetAttributes(this, userName, attributes);
        if (setAttributesOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    String getCommentPrefix() {
        if (mustReadArchiveFileFlag && !isArchiveInformationRead()) {
            readInformation();
        }

        return headerInfo.getCommentPrefix();
    }

    boolean setCommentPrefix(final String userName, final String newCommentPrefix) throws QVCSException {
        boolean retVal = false;
        LogFileOperationSetCommentPrefix setCommentPrefixOperation = new LogFileOperationSetCommentPrefix(this, userName, newCommentPrefix);
        if (setCommentPrefixOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    String getModuleDescription() {
        if (mustReadArchiveFileFlag && !isArchiveInformationRead()) {
            readInformation();
        }

        return headerInfo.getModuleDescription();
    }

    boolean setModuleDescription(final String userName, final String newDescription) throws QVCSException {
        boolean retVal = false;
        LogFileOperationSetModuleDescription setModuleDescriptionOperation = new LogFileOperationSetModuleDescription(this, userName, newDescription);
        if (setModuleDescriptionOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    boolean setRevisionDescription(SetRevisionDescriptionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;
        Object[] args = new Object[2];
        args[0] = this;
        args[1] = commandLineArgs;

        LogFileOperationSetRevisionDescription setRevisionDescriptionOperation = new LogFileOperationSetRevisionDescription(args);
        if (setRevisionDescriptionOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    String getRevisionDescription(final String revisionString) {
        String retVal = null;
        AtomicInteger revisionIndex = new AtomicInteger();
        if (findRevision(revisionString, revisionIndex)) {
            RevisionHeader revInfo = getRevisionHeader(revisionIndex.get());
            if (revInfo != null) {
                retVal = revInfo.getRevisionDescription();
            }
        }
        return retVal;
    }

    boolean setIsObsolete(String userName, boolean flag) throws QVCSException {
        // NOT USED.
        boolean retVal = false;

        LogFileOperationSetIsObsolete setIsObsoleteOperation = new LogFileOperationSetIsObsolete(this, userName, flag);
        if (setIsObsoleteOperation.execute()) {
            retVal = reReadInformation();
        } else {
            reReadInformation();
        }
        return retVal;
    }

    byte[] getRevisionAsByteArray(String revisionString) {
        byte[] workfileBuffer = null;
        try {
            if (open()) {
                AtomicInteger revisionIndex = new AtomicInteger();
                if (findRevision(revisionString, revisionIndex)) {
                    RevisionHeader revInfo = getRevisionHeader(revisionIndex.get());
                    MutableByteArray processedBuffer = new MutableByteArray();
                    if (fetchRevision(revInfo, null, false, processedBuffer)) {
                        workfileBuffer = processedBuffer.getValue();
                    }
                }
            }
        } finally {
            close();
        }
        return workfileBuffer;
    }

    boolean hasLabel(final String label) {
        boolean retVal = false;

        LogFileHeaderInfo logfileHeaderInfo = getLogFileHeaderInfo();
        LabelInfo[] labelInfo = logfileHeaderInfo.getLabelInfo();
        if (labelInfo != null) {
            for (LabelInfo labelInfo1 : labelInfo) {
                if (0 == label.compareTo(labelInfo1.getLabelString())) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    private void updateHeaderOnDisk() throws QVCSException {
        // Need to re-write the archive to have the modified header
        // information.
        RandomAccessFile newArchiveStream = null;
        boolean success = false;
        try {
            // Need to re-write the archive to have the modified header
            // information.
            newArchiveStream = new RandomAccessFile(getTempFile(), "rw");
            try (RandomAccessFile oldArchiveStream = new RandomAccessFile(getFile(), "r")) {

                // Figure out where we need to copy from in the existing archive
                // file... (It's the beginning of all revision info).
                RevisionHeader revInfo = getRevisionHeader(0);
                if (revInfo != null) {
                    long revStartPosition = revInfo.getRevisionStartPosition();
                    oldArchiveStream.seek(revStartPosition);

                    // Write the new header out to the new archive file.
                    getLogFileHeaderInfo().write(newArchiveStream);

                    // And copy the rest of the existing archive to the new one.
                    long bytesToCopy = oldArchiveStream.length() - revStartPosition;
                    AbstractLogFileOperation.copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, bytesToCopy);
                    success = true;
                } else {
                    throw new QVCSRuntimeException("Unable to get revision header 0");
                }
            }
        } catch (IOException e) {
            throw new QVCSException("Exception in updateHeaderOnDisk(): " + e.getLocalizedMessage());
        } finally {
            try {
                if (newArchiveStream != null) {
                    newArchiveStream.close();
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                success = false;
            }

            if (success) {
                replaceExistingArchiveWithNewTempArchive();
                reReadInformation();
            } else {
                getTempFile().delete();
            }
        }
    }

    @SuppressWarnings("LoggerStringConcat")
    private void readRevisionData(byte[] revisionData) throws QVCSException {
        try {
            int offset = 0;

            while (true) {
                int bytesLeft = revisionData.length - offset;
                if (bytesLeft > QVCSConstants.BYTES_TO_XFER) {
                    int bytesToRead = QVCSConstants.BYTES_TO_XFER;
                    inStream.readFully(revisionData, offset, bytesToRead);
                    offset += bytesToRead;
                    continue;
                } else {
                    int bytesToRead = bytesLeft;
                    inStream.readFully(revisionData, offset, bytesToRead);
                    break;
                }
            }
        } catch (java.lang.OutOfMemoryError e) {
            // If they are trying to create an archive for a really big file,
            // we might have problems.
            LOGGER.warn("Out of memory trying to read revision data.");
            throw new QVCSException("Out of memory trying to read revision data.");
        } catch (IOException e) {
            LOGGER.warn("Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            throw new QVCSException("Exception in readRevisionData.\n" + Utility.expandStackTraceToString(e));
        }
    }

    /**
     * Remove any view and branch labels. This method should only be called when performing a file ID reset operation, i.e. when all
     * views and branches are thrown away.
     */
    private void removeViewAndBranchLabels() {
        LabelInfo[] labelInfoCollection = getLogFileHeaderInfo().getLabelInfo();
        if (getLogFileHeaderInfo().getLabelInfo() != null) {
            List<LabelInfo> labelInfoArray = new ArrayList<>();
            for (LabelInfo labelInfo : labelInfoCollection) {
                String labelString = labelInfo.getLabelString();
                if (labelString.startsWith(QVCSConstants.QVCS_VIEW_LABEL)
                        || labelString.startsWith(QVCSConstants.QVCS_FEATURE_BRANCH_LABEL)
                        || labelString.startsWith(QVCSConstants.QVCS_OPAQUE_BRANCH_LABEL)) {
                    continue;
                } else {
                    labelInfoArray.add(labelInfo);
                }
            }
            LabelInfo[] newLabelInfoCollection = new LabelInfo[labelInfoArray.size()];
            int i = 0;
            for (LabelInfo labelInfo : labelInfoArray) {
                newLabelInfoCollection[i++] = labelInfo;
            }
            getLogFileHeaderInfo().setLabelInfo(newLabelInfoCollection);
        }
    }
}
