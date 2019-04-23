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

import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.CompareFilesEditHeader;
import com.qumasoft.qvcslib.CompareFilesEditInformation;
import com.qumasoft.qvcslib.CompareFilesWithApacheDiff;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QumaAssert;
import com.qumasoft.qvcslib.RevisionDescriptor;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.ZlibCompressor;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checkin file revision.
 * @author Jim Voris
 */
class LogFileOperationCheckIn extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileOperationCheckIn.class);
    private final CompareFilesWithApacheDiff compareFilesOperator;
    private final CheckInCommandArgs commandLineArgs;
    private final boolean lockFlag;
    private final boolean labelFlag;
    private final boolean reuseLabelFlag;
    private final boolean floatLabelFlag;
    private final boolean forceBranchFlag;
    private final boolean ignoreLocksToEnableBranchCheckInFlag;
    private final String userName;
    private String lockedRevisionString;
    private final String filename;
    private final String checkInComment;
    private final String labelString;
    private RandomAccessFile newArchiveStream;
    private RandomAccessFile oldArchiveStream;
    private int creatorIndex = -1;
    private Date checkInDate;

    /**
     * Creates a new instance of LogFileOperationCheckIn.
     * @param a arguments for the operation. a[0] is the logfileImpl; a[1] is the fetch-to filename; a[2] is the command arguments object.
     * @param flag ignore locks to enable branch check in flag.
     */
    LogFileOperationCheckIn(Object[] a, boolean flag) {
        super(a, (LogFileImpl) a[0]);
        this.ignoreLocksToEnableBranchCheckInFlag = flag;
        this.filename = (String) a[1];
        this.commandLineArgs = (CheckInCommandArgs) a[2];
        this.userName = commandLineArgs.getUserName();
        this.lockedRevisionString = commandLineArgs.getLockedRevisionString();
        this.checkInComment = commandLineArgs.getCheckInComment();
        this.lockFlag = commandLineArgs.getLockFlag();
        this.labelFlag = commandLineArgs.getApplyLabelFlag();
        this.reuseLabelFlag = commandLineArgs.getReuseLabelFlag();
        this.floatLabelFlag = commandLineArgs.getFloatLabelFlag();
        this.forceBranchFlag = commandLineArgs.getForceBranchFlag();
        this.labelString = commandLineArgs.getLabel();
        this.compareFilesOperator = new CompareFilesWithApacheDiff();
    }

    @Override
    public boolean execute() throws QVCSException {
        boolean retVal = false;
        boolean unlockRequiredFlag = false;

        // Figure out the checkin date
        if (commandLineArgs.getCheckInTimestamp() == null) {
            // The checkin date is now.
            checkInDate = new Date();
        } else {
            checkInDate = commandLineArgs.getCheckInTimestamp();
        }

        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        // Figure out who is doing this operation.
        creatorIndex = getLogFileImpl().getModifierList().userToIndex(userName);
        RevisionHeader revInfo;

        // If we check locks, then check that this user has the file locked...
        if (getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().attributes().getIsCheckLock() && !ignoreLocksToEnableBranchCheckInFlag) {
            AtomicInteger revisionIndex = new AtomicInteger();
            AtomicReference<String> revisionString = new AtomicReference<>(lockedRevisionString);

            // Make sure the revision is locked.
            if (!getLogFileImpl().isRevisionLocked(revisionString, revisionIndex)) {
                throw new QVCSException("Revision " + revisionString.get() + " is not locked for " + userName);
            }

            // Make sure the user has locked this
            if (!getLogFileImpl().isLockedByUser(userName)) {
                throw new QVCSException("User '" + userName + "' does not hold a lock on " + getLogFileImpl().getShortWorkfileName());
            }
            revInfo = getLogFileImpl().getRevisionHeader(revisionIndex.get());

            // If working on the default revision, this gets filled in for us.
            lockedRevisionString = revisionString.get();
        } else {
            if (lockedRevisionString == null) {
                // TODO -- This will change if/when I support concurrent development.
                // Always check in to the tip revision of the default branch.
                revInfo = getLogFileImpl().getDefaultRevisionHeader();
                lockedRevisionString = revInfo.getRevisionString();
            } else {
                // (For rename and move operations that need to be able to checkin
                // new tip revisions on all branches).
                revInfo = getLogFileImpl().getRevisionHeader(getLogFileImpl().getRevisionInformation().getRevisionIndex(lockedRevisionString));
            }
        }

        /*
         * 1. Get the locked revision into a temp file.
         */
        String tempFileNameForExistingRevision = getRevisionToTempfile(userName, lockedRevisionString);
        File tempFileNameForExistingRevisionFile = new File(tempFileNameForExistingRevision);

        // Make sure the temp file is gone.
        getLogFileImpl().getTempFile().delete();
        File tempFileForCompareResults = null;
        try {
            newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");
            oldArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getFile(), "r");
            String tempFileNameForCompareResults;

            // If we're checking in a new tip revision on the TRUNK.
            if ((revInfo.getDepth() == 0) && revInfo.isTip() && !forceBranchFlag) {
                assert (revInfo.getDepth() == 0);
                LOGGER.trace("Adding new TRUNK tip revision");

                /*
                 * 2. Compare that temp file with the filename passed in, writing the delta to a temp file.
                 */
                tempFileNameForCompareResults = compareFileToTempfile(tempFileNameForExistingRevision, true);
                tempFileForCompareResults = new File(tempFileNameForCompareResults);

                // Store a new revision.
                if (!compareFilesOperator.isEqual() || commandLineArgs.getCreateNewRevisionIfEqual()) {
                    writeNewLogfileCreatingNewTrunkTip(revInfo, tempFileForCompareResults);
                    updateDigestManager();
                } else {
                    // Nothing changed.  Just unlock the revision.
                    if (labelFlag) {
                        // The user is requesting that we apply a label.
                        writeNewLogfileThatOnlyChangesTheHeader(revInfo);
                    } else {
                        unlockRequiredFlag = true;
                    }
                }
            } else if ((revInfo.getDepth() == 0) && revInfo.isTip() && forceBranchFlag) {
                // If we are checking in a new branch right off the tip of the TRUNK.
                QumaAssert.isTrue(revInfo.getDepth() == 0);
                LOGGER.trace("Forcing a new branch off of the tip of the TRUNK.");

                // Compare that temp file with the filename passed in,  writing the delta to a temp file.
                tempFileNameForCompareResults = compareFileToTempfile(tempFileNameForExistingRevision, false);
                tempFileForCompareResults = new File(tempFileNameForCompareResults);
                unlockRequiredFlag = createBranch(revInfo, tempFileForCompareResults);
            } else if (revInfo.isTip() && !forceBranchFlag) {
                // If we're checking in a new tip revision on a branch.
                QumaAssert.isTrue(revInfo.getDepth() > 0);

                LOGGER.trace("Adding new tip revision to branch [{}]", revInfo.getRevisionString());

                /*
                 * 2. Compare that temp file with the filename passed in, writing the delta to a temp file.
                 */
                tempFileNameForCompareResults = compareFileToTempfile(tempFileNameForExistingRevision, false);
                tempFileForCompareResults = new File(tempFileNameForCompareResults);

                // Store a new revision.
                if (!compareFilesOperator.isEqual() || commandLineArgs.getCreateNewRevisionIfEqual()) {
                    writeNewLogfileCreatingNewBranchTip(revInfo, tempFileForCompareResults);
                    updateDigestManager();
                } else {
                    // Nothing changed.  Just unlock the revision.
                    if (labelFlag) {
                        // The user is requesting that we apply a label.
                        writeNewLogfileThatOnlyChangesTheHeader(revInfo);
                    } else {
                        unlockRequiredFlag = true;
                    }
                }
            } else if (revInfo.isTip() && forceBranchFlag) {
                // If we're forcing a new branch off the tip revision of an existing branch.
                QumaAssert.isTrue(revInfo.getDepth() > 0);
                QumaAssert.isTrue(revInfo.isTip());
                LOGGER.trace("Forcing a new branch off of the tip of branch [{}]", revInfo.getRevisionString());

                // Compare that temp file with the filename passed in,  writing the delta to a temp file.
                tempFileNameForCompareResults = compareFileToTempfile(tempFileNameForExistingRevision, false);
                tempFileForCompareResults = new File(tempFileNameForCompareResults);
                unlockRequiredFlag = createBranch(revInfo, tempFileForCompareResults);
            } else {
                // TODO -- auto merge?
                // If we're checking in a new revision somewhere in the middle.
                // (we'll have to make a new branch automatically).  I guess this would
                // be where we'd automatically merge if auto-merge was implemented.
                QumaAssert.isTrue(!revInfo.isTip());
                LOGGER.trace("Creating new branch at revision [{}]", revInfo.getRevisionString());

                // Compare that temp file with the filename passed in,  writing the delta to a temp file.
                tempFileNameForCompareResults = compareFileToTempfile(tempFileNameForExistingRevision, false);
                tempFileForCompareResults = new File(tempFileNameForCompareResults);
                unlockRequiredFlag = createBranch(revInfo, tempFileForCompareResults);
            }
            retVal = true;
        } catch (QVCSException | IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        } finally {
            try {
                if (oldArchiveStream != null) {
                    oldArchiveStream.close();
                    oldArchiveStream = null;
                }
                if (newArchiveStream != null) {
                    newArchiveStream.close();
                    newArchiveStream = null;
                }
                if (tempFileForCompareResults != null) {
                    tempFileForCompareResults.delete();
                }
                if (tempFileNameForExistingRevisionFile.exists()) {
                    tempFileNameForExistingRevisionFile.delete();
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                retVal = false;
                newArchiveStream = null;
                oldArchiveStream = null;
            }
        }
        // If we just need to unlock the archive, that's what we'll do.
        if (unlockRequiredFlag) {
            if (getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().attributes().getIsCheckLock()) {
                if (!lockFlag) {
                    AtomicReference<String> revisionString = new AtomicReference<>(lockedRevisionString);
                    retVal = getLogFileImpl().unlockRevision(userName, revisionString);
                    commandLineArgs.setNewRevisionString(revisionString.get());
                } else {
                    retVal = true;
                    commandLineArgs.setNewRevisionString(revInfo.getRevisionString());
                }
            } else {
                // There is no lock checking for this archive, and it did not
                // change, so we're done.
                retVal = true;
                commandLineArgs.setNewRevisionString(revInfo.getRevisionString());
            }
        } else {
            // Replace existing archive with new one.
            if (retVal) {
                getLogFileImpl().replaceExistingArchiveWithNewTempArchive();
            } else {
                getLogFileImpl().getTempFile().delete();
            }
        }
        return retVal;
    }

    // Return the state of the unlockRequiredFlag flag.
    private boolean createBranch(RevisionHeader revInfo, File tempFileForCompareResults) throws QVCSException, IOException {
        boolean retVal = false;

        // Store a new revision.
        if (!compareFilesOperator.isEqual() || commandLineArgs.getCreateNewRevisionIfEqual()) {
            writeNewLogfileCreatingNewBranch(revInfo, tempFileForCompareResults);
            updateDigestManager();
        } else {
            // Nothing changed.  Just unlock the revision.
            if (labelFlag) {
                // The user is requesting that we apply a label.
                writeNewLogfileThatOnlyChangesTheHeader(revInfo);
            } else {
                retVal = true;
            }
        }
        return retVal;
    }

    private void updateDigestManager() {
        FileInputStream inStream = null;
        try {
            File workfile = new File(filename);
            inStream = new FileInputStream(workfile);
            byte[] buffer = new byte[(int) workfile.length()];
            Utility.readDataFromStream(buffer, inStream);
            ArchiveDigestManager.getInstance().addRevision(getLogFileImpl(), commandLineArgs.getNewRevisionString(), buffer);
        } catch (IOException e) {
            LOGGER.warn(e.getClass().toString() + " " + e.getLocalizedMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Compare the filename to a tempfile that contains a fetched revision from the archive. We return the name of the file that
     * contains the result of the compare. This routine uses the member m_CompareFilesOperator to perform the comparison.
     */
    String compareFileToTempfile(String tempfileName, boolean isReverseDelta) throws QVCSException {
        File tempFileForCompareResults = null;
        try {
            tempFileForCompareResults = File.createTempFile("QVCS", ".tmp");
        } catch (java.io.IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new QVCSException("Failed to create QVCS temp file: " + e.getMessage());
        }
        String tempFileNameForCompareResults = tempFileForCompareResults.getAbsolutePath();
        ArchiveAttributes attributes = getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().attributes();
        if (attributes.getIsBinaryfile() || !attributes.getIsComputeDelta()) {
            // The file is binary or we are not supposed to compute a delta for it.
            // So, create an edit script that just replaces the current revision
            // with the new revision.
            createNoComputeDeltaEditScript(tempfileName, tempFileNameForCompareResults, isReverseDelta);
        } else {
            // <editor-fold>
            String[] localArgs = new String[3];
            // </editor-fold>
            if (isReverseDelta) {
                localArgs[0] = filename;
                localArgs[1] = tempfileName;
            } else {
                localArgs[0] = tempfileName;
                localArgs[1] = filename;
            }
            localArgs[2] = tempFileNameForCompareResults;
            LOGGER.trace("Comparing " + filename + " to " + tempfileName);
            if (!compareFilesOperator.execute(localArgs)) {
                throw new QVCSException("Failed to compare " + filename + " to file revision " + lockedRevisionString);
            }
        }
        return tempFileNameForCompareResults;
    }

    void writeNewLogfileCreatingNewTrunkTip(RevisionHeader parentRevInfo, File tempFileForCompareResults) throws QVCSException, IOException {
        // The file that we are checking in (note that it is already keyword compressed).
        File workfile = new File(filename);

        // Create the revision header for this new revision.
        RevisionHeader newRevisionHeader = new RevisionHeader(getLogFileImpl().getAccessList(),
                getLogFileImpl().getModifierList());
        newRevisionHeader.setCreator(userName);
        newRevisionHeader.setCheckInDate(checkInDate);
        newRevisionHeader.setEditDate(commandLineArgs.getInputfileTimeStamp());
        newRevisionHeader.setRevisionDescription(checkInComment);

        newRevisionHeader.setIsTip(true);
        if (lockFlag) {
            newRevisionHeader.setIsLocked(true);
            newRevisionHeader.setLocker(userName);
        }
        newRevisionHeader.setMajorNumber(parentRevInfo.getMajorNumber());
        newRevisionHeader.setMinorNumber(1 + parentRevInfo.getMinorNumber());
        Compressor compressor = new ZlibCompressor();
        newRevisionHeader.setRevisionSize(getCompressedRevisionSizeForWorkfile(workfile, compressor));
        newRevisionHeader.setIsCompressed(compressor.getBufferIsCompressedFlag());
        newRevisionHeader.setRevisionDescriptor(new RevisionDescriptor(newRevisionHeader, parentRevInfo));
        commandLineArgs.setNewRevisionString(newRevisionHeader.getRevisionString());

        // Increment the number of revisions.
        getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().incrementRevisionCount();
        if (!lockFlag) {
            getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().decrementLockCount();
        }

        // TODO -- is there anything we need to do here if the default branch is not the TRUNK?
        // e.g. is there anything else in the archive header that might need to get updated?
        getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().incrementMinorNumber();

        // Set the stuff that goes in the supplemental info.
        getLogFileImpl().getLogFileHeaderInfo().setWorkfileName(commandLineArgs.getFullWorkfileName());
        getLogFileImpl().getLogFileHeaderInfo().getSupplementalHeaderInfo().setLastWorkfileSize(workfile.length());
        getLogFileImpl().getLogFileHeaderInfo().getSupplementalHeaderInfo().setLastArchiveUpdateDate(checkInDate);
        getLogFileImpl().getLogFileHeaderInfo().getSupplementalHeaderInfo().setLastModifierIndex(getLogFileImpl().getModifierList().userToIndex(userName));

        // Update the labels if we need to.
        if (labelFlag) {
            addLabelToHeader(newRevisionHeader);
        }

        // Write the header.
        getLogFileImpl().getLogFileHeaderInfo().write(newArchiveStream);

        // Write the new tip revision header
        newRevisionHeader.write(newArchiveStream);

        // Copy the (potentially compressed) workfile as the first revision.
        copyFromOneOpenFileToAnotherOpenFile(compressor.getCompressedStream(), newArchiveStream, newRevisionHeader.getRevisionSize());

        // Figure out the number of bytes we'll need to copy from the original archive
        // (before we overwrite the data that allows us to figure that out).
        long oldArchiveSeekPosition = parentRevInfo.getRevisionDataStartPosition() + parentRevInfo.getRevisionSize();
        long numberOfBytesToCopyFromSource = getLogFileImpl().getFile().length() - oldArchiveSeekPosition;

        // Update the 'parent' revision
        parentRevInfo.setIsTip(false);
        parentRevInfo.setIsLocked(false);
        Compressor deltaCompressor = new ZlibCompressor();
        parentRevInfo.setRevisionSize(getCompressedRevisionSizeForDelta(tempFileForCompareResults, deltaCompressor));
        parentRevInfo.setIsCompressed(deltaCompressor.getBufferIsCompressedFlag());
        parentRevInfo.write(newArchiveStream);
        commandLineArgs.setParentRevisionString(parentRevInfo.getRevisionString());

        // Write the delta to the archive
        copyFromOneOpenFileToAnotherOpenFile(deltaCompressor.getCompressedStream(), newArchiveStream, parentRevInfo.getRevisionSize());

        // Copy the rest of the archive file...
        // Copy the rest of the original archive to the new archive.
        oldArchiveStream.seek(oldArchiveSeekPosition);
        copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, numberOfBytesToCopyFromSource);
    }

    void writeNewLogfileCreatingNewBranchTip(RevisionHeader parentRevInfo, File tempFileForCompareResults) throws QVCSException, IOException {
        // This revision will be a forward delta.

        // Create the revision header for this new revision.
        RevisionHeader newRevisionHeader = new RevisionHeader(getLogFileImpl().getAccessList(),
                getLogFileImpl().getModifierList());
        newRevisionHeader.setCreator(userName);
        newRevisionHeader.setCheckInDate(checkInDate);
        newRevisionHeader.setEditDate(commandLineArgs.getInputfileTimeStamp());
        newRevisionHeader.setRevisionDescription(checkInComment);

        newRevisionHeader.setParentRevisionHeader(parentRevInfo);
        newRevisionHeader.setIsTip(true);
        if (lockFlag) {
            newRevisionHeader.setIsLocked(true);
            newRevisionHeader.setLocker(userName);
        }
        newRevisionHeader.setDepth(parentRevInfo.getDepth());
        Compressor deltaCompressor = new ZlibCompressor();
        newRevisionHeader.setRevisionSize(getCompressedRevisionSizeForDelta(tempFileForCompareResults, deltaCompressor));
        newRevisionHeader.setIsCompressed(deltaCompressor.getBufferIsCompressedFlag());

        newRevisionHeader.setMajorNumber(parentRevInfo.getMajorNumber());
        newRevisionHeader.setMinorNumber(1 + parentRevInfo.getMinorNumber());
        newRevisionHeader.setRevisionDescriptor(new RevisionDescriptor(newRevisionHeader, parentRevInfo));
        commandLineArgs.setNewRevisionString(newRevisionHeader.getRevisionString());

        // Write the header, incrementing the number of revisions.
        // Increment the number of revisions.
        getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().incrementRevisionCount();
        if (!lockFlag) {
            getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().decrementLockCount();
        }

        // Add the label to the header if we need to
        if (labelFlag) {
            addLabelToHeader(newRevisionHeader);
        }

        // Write the header.
        getLogFileImpl().getLogFileHeaderInfo().write(newArchiveStream);

        // Write the logfile, up to the parent revision.
        RevisionHeader firstRevInfo = getLogFileImpl().getRevisionHeader(0);
        long startingSourceSeekPosition = firstRevInfo.getRevisionStartPosition();
        long endingSourceSeekPosition = parentRevInfo.getRevisionStartPosition();
        long numberOfBytesToCopyFromSource = endingSourceSeekPosition - startingSourceSeekPosition;
        oldArchiveStream.seek(startingSourceSeekPosition);
        copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, numberOfBytesToCopyFromSource);

        // Update the parent revision
        parentRevInfo.setIsLocked(false);
        parentRevInfo.setIsTip(false);

        // Write the parent revision header
        parentRevInfo.write(newArchiveStream);
        commandLineArgs.setParentRevisionString(parentRevInfo.getRevisionString());

        // Write the parent revision data
        startingSourceSeekPosition = parentRevInfo.getRevisionDataStartPosition();
        oldArchiveStream.seek(startingSourceSeekPosition);
        copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, parentRevInfo.getRevisionSize());

        // Copy any deeper branch revisions to the new archive so that any child branch revisions
        // immediately follow their parent. This fixes the bug that was posted on the forums where things
        // got hosed up on creation of a 2nd revision on a branch that already had children branches.
        writeAnyBranchRevisionsThatAreChildrenOfParent(parentRevInfo);

        // Write the new revision. Recall that this new revision is a forward delta, and therefore
        // follows the parent revision in the logfile.
        newRevisionHeader.write(newArchiveStream);

        copyFromOneOpenFileToAnotherOpenFile(deltaCompressor.getCompressedStream(), newArchiveStream, newRevisionHeader.getRevisionSize());

        // Copy the rest of the original archive to the new archive.
        numberOfBytesToCopyFromSource = getLogFileImpl().getFile().length() - oldArchiveStream.getFilePointer();
        copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, numberOfBytesToCopyFromSource);
    }

    void writeNewLogfileCreatingNewBranch(RevisionHeader parentRevInfo, File tempFileForCompareResults) throws QVCSException, IOException {
        // This revision will be a forward delta.

        // Update the parent revision
        parentRevInfo.incrementChildCount();
        parentRevInfo.setIsLocked(false);

        // Create the revision header for this new revision.  We need to capture
        // the new revision header info before we write the logfile header
        // because we need the new revision header info in the case that we
        // are applying a label.  The label info gets written out to the logfile
        // header.
        RevisionHeader newRevisionHeader = new RevisionHeader(getLogFileImpl().getAccessList(), getLogFileImpl().getModifierList());
        newRevisionHeader.setCreator(userName);
        newRevisionHeader.setCheckInDate(checkInDate);
        newRevisionHeader.setEditDate(commandLineArgs.getInputfileTimeStamp());
        newRevisionHeader.setRevisionDescription(checkInComment);
        newRevisionHeader.setParentRevisionHeader(parentRevInfo);
        newRevisionHeader.setIsTip(true);
        if (lockFlag) {
            newRevisionHeader.setIsLocked(true);
            newRevisionHeader.setLocker(userName);
        }
        Compressor compressor = new ZlibCompressor();
        newRevisionHeader.setDepth(1 + parentRevInfo.getDepth());
        newRevisionHeader.setRevisionSize(getCompressedRevisionSizeForDelta(tempFileForCompareResults, compressor));
        newRevisionHeader.setIsCompressed(compressor.getBufferIsCompressedFlag());
        newRevisionHeader.setMajorNumber(parentRevInfo.getChildCount());
        newRevisionHeader.setMinorNumber(1);
        newRevisionHeader.setRevisionDescriptor(new RevisionDescriptor(newRevisionHeader, parentRevInfo));

        // Update the command line with the new revision's revision string so we
        // can return that to the client.
        commandLineArgs.setNewRevisionString(newRevisionHeader.getRevisionString());

        // TODO -- is there anything we need to do here if the default branch is not the TRUNK?
        // e.g. is there anything else in the archive header that might need to get updated?
        // Might have to update the supplemental info -- I'm not sure if the supplemental info
        // ever contains non-TRUNK info, e.g. can it contain the default branch info?

        // Increment the number of revisions.
        getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().incrementRevisionCount();
        if (!lockFlag) {
            getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().decrementLockCount();
        }

        if (labelFlag) {
            addLabelToHeader(newRevisionHeader);
        }

        // Write the header.
        getLogFileImpl().getLogFileHeaderInfo().write(newArchiveStream);

        // Write the logfile, up to the parent revision.
        RevisionHeader firstRevInfo = getLogFileImpl().getRevisionHeader(0);
        long startingSourceSeekPosition = firstRevInfo.getRevisionStartPosition();
        long endingSourceSeekPosition = parentRevInfo.getRevisionStartPosition();
        long numberOfBytesToCopyFromSource = endingSourceSeekPosition - startingSourceSeekPosition;
        oldArchiveStream.seek(startingSourceSeekPosition);
        copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, numberOfBytesToCopyFromSource);

        // Write the parent revision header
        parentRevInfo.write(newArchiveStream);
        commandLineArgs.setParentRevisionString(parentRevInfo.getRevisionString());

        // Write the parent revision data
        startingSourceSeekPosition = parentRevInfo.getRevisionDataStartPosition();
        oldArchiveStream.seek(startingSourceSeekPosition);
        copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, parentRevInfo.getRevisionSize());
        newRevisionHeader.write(newArchiveStream);

        copyFromOneOpenFileToAnotherOpenFile(compressor.getCompressedStream(), newArchiveStream, newRevisionHeader.getRevisionSize());

        // Copy the rest of the original archive to the new archive.
        numberOfBytesToCopyFromSource = oldArchiveStream.length() - oldArchiveStream.getFilePointer();
        copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, numberOfBytesToCopyFromSource);
    }

    /**
     * This method is used in the case where there are no differences between the existing TRUNK tip revision and the new revision,
     * AND the user wants to apply a label at checkin time. This is a feature that I never got implemented in the C++ product in the
     * 'right' way.
     */
    void writeNewLogfileThatOnlyChangesTheHeader(RevisionHeader revInfo) throws QVCSException, IOException {
        assert (labelFlag);
        if (!lockFlag) {
            getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().decrementLockCount();
        }

        // Update the command line with the revision's revision string so we
        // can return that to the client.
        commandLineArgs.setNewRevisionString(revInfo.getRevisionString());

        // Add the label to the header.
        addLabelToHeader(revInfo);

        // Write the header information to the stream.
        getLogFileImpl().getLogFileHeaderInfo().write(newArchiveStream);

        // Position the old archive to the end of the header area.
        LogFileHeaderInfo oldHeaderInfo = new LogFileHeaderInfo();
        oldHeaderInfo.read(oldArchiveStream);

        if (!lockFlag) {
            // We need to release the lock on the revision.
            // At this point, both the new and old archive streams are positioned
            // immediately after the archive header.

            // Copy up to the revision that needs to change.
            long bytesToBeginningOfRevision = revInfo.getRevisionStartPosition() - oldArchiveStream.getFilePointer();
            copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, bytesToBeginningOfRevision);

            // Now write the revision header, unlocked.
            revInfo.setIsLocked(false);
            revInfo.write(newArchiveStream);

            // Position the old stream to the beginning of the revision data.
            oldArchiveStream.seek(revInfo.getRevisionDataStartPosition());

            long remainingBytesToCopy = oldArchiveStream.length() - oldArchiveStream.getFilePointer();

            // Copy the rest of the original archive to the new archive.
            copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, remainingBytesToCopy);
        } else {
            // The user is keeping the archive locked.  This means we don't
            // need to specifically update the revision header, since it will
            // remain locked.

            // Figure out how many bytes remain in the original that need to
            // be copied to the new archive.
            long numberOfBytesToCopyFromSource = oldArchiveStream.length() - oldArchiveStream.getFilePointer();

            // Copy the rest of the original archive to the new archive.
            copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, numberOfBytesToCopyFromSource);
        }
    }

    private void addLabelToHeader(RevisionHeader revInfo) throws QVCSException {
        // Check to see that the label is not already in use.... If it is, get
        // rid of it IF they have specified the reuse label flag.  If it is
        // already in use, and they did not specify the reuse label flag, then
        // we have a problem... so we bail with an exception.
        LabelInfo[] existingLabels = getLogFileImpl().getLogFileHeaderInfo().getLabelInfo();
        if (existingLabels != null) {
            for (int i = 0; i < existingLabels.length; i++) {
                String existingLabel = existingLabels[i].getLabelString();
                if (existingLabel.equals(labelString)) {
                    if (reuseLabelFlag) {
                        // Remove the old label from the array of labels.
                        LabelInfo[] retainedLabels = new LabelInfo[existingLabels.length - 1];
                        int k = 0;
                        for (int j = 0; j < existingLabels.length; j++) {
                            if (j == i) {
                                // skip the matching label.
                                continue;
                            } else {
                                retainedLabels[k++] = existingLabels[j];
                            }
                        }
                        getLogFileImpl().getLogFileHeaderInfo().setLabelInfo(retainedLabels);
                    } else {
                        // The user is trying to use a label that is already in use,
                        // and they did not specify the reuse label flag.  Don't
                        // allow this!
                        String errorMessage = "Checkin failed.  Label request ignored for " + getLogFileImpl().getShortWorkfileName() + ". Label (" + labelString
                                + ") is already in use. Try using the 'reuse label' flag.";
                        commandLineArgs.setFailureReason(errorMessage);
                        throw new QVCSException(errorMessage);
                    }
                    break;
                }
            }
        }

        // Add the label to the header.
        // Create a new LabelInfo array that's one bigger than before
        LabelInfo[] oldLabelInfo = getLogFileImpl().getLogFileHeaderInfo().getLabelInfo();
        int newLabelInfoSize = 1;
        if (oldLabelInfo != null) {
            newLabelInfoSize = oldLabelInfo.length + 1;
        }
        LabelInfo[] newLabelInfo = new LabelInfo[newLabelInfoSize];

        // Copy the existing labels to the new label info array, leaving
        // room at the front for the new label
        if (oldLabelInfo != null) {
            System.arraycopy(oldLabelInfo, 0, newLabelInfo, 1, newLabelInfo.length - 1);
        }

        // Create the LabelInfo object for this label.
        LabelInfo labelInfo = new LabelInfo(labelString, revInfo.getRevisionString(), floatLabelFlag, creatorIndex);

        // Add it to the array at the beginning.
        newLabelInfo[0] = labelInfo;

        // Set the new label info array on the archive header.
        getLogFileImpl().getLogFileHeaderInfo().setLabelInfo(newLabelInfo);
    }

    /**
     * Create an edit script that just replaces the existing revision with the new file.
     */
    private void createNoComputeDeltaEditScript(String existingRevisionFilename, String editScriptOutputFilename, boolean isReverseDelta) {
        DataInputStream inStream = null;
        long bytesToCopy;

        try {
            File newRevisionFile = new File(filename);
            File existingRevisionFile = new File(existingRevisionFilename);
            File editScriptFile = new File(editScriptOutputFilename);

            CompareFilesEditInformation editInfo = new CompareFilesEditInformation();
            editInfo.setEditType(CompareFilesEditInformation.QVCS_EDIT_REPLACE);
            editInfo.setSeekPosition(0);

            try (DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(editScriptFile)))) {

                if (isReverseDelta) {
                    editInfo.setDeletedBytesCount((int) newRevisionFile.length());
                    editInfo.setInsertedBytesCount((int) existingRevisionFile.length());
                    inStream = new DataInputStream(new BufferedInputStream(new FileInputStream(existingRevisionFile)));
                    bytesToCopy = existingRevisionFile.length();
                } else {
                    editInfo.setDeletedBytesCount((int) existingRevisionFile.length());
                    editInfo.setInsertedBytesCount((int) newRevisionFile.length());
                    inStream = new DataInputStream(new BufferedInputStream(new FileInputStream(newRevisionFile)));
                    bytesToCopy = newRevisionFile.length();
                }
                // Pad the beginning of the file with a bogus header.  A read compare
                // puts something useful in this header info, but we just need to
                // put those bytes in for padding...
                byte[] padBytes = new byte[CompareFilesEditHeader.getEditHeaderSize()];
                outStream.write(padBytes);

                editInfo.write(outStream);
                AbstractLogFileOperation.copyFromOneOpenFileToAnotherOpenFile(inStream, outStream, bytesToCopy);

                // Fake things out so we'll think a comparison was actually attempted.
                // This is a bit of a kludge, in that it creates coupling between this
                // code and the CompareFiles class.  The correct way to handle this would
                // be to create a separate implementation of a compare algorithm that
                // had the same interface as CompareFiles, but basically performed the
                // same code that we have above.  That implementation would set the
                // compare attempted flag to true, and it would be good.  As it is,
                // we'll survive with the kludge.
                compareFilesOperator.setCompareAttempted(true);
            }
        } catch (IOException e) {
            LOGGER.warn("Caught exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Caught exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * This method handles the case where a user is checking in a new branch revision and that new branch revision's parent revision
     * has some child branches. The logfile layout requires that the child branch revisions (that have a greater depth) get written
     * to the logfile <i>before</i> the new revision.
     *
     * @param parentRevInfo the parent revision header.
     */
    private void writeAnyBranchRevisionsThatAreChildrenOfParent(RevisionHeader parentRevHeader) throws IOException, QVCSException {
        int parentDepth = parentRevHeader.getDepth();
        int revisionToLookAtIndex = parentRevHeader.getRevisionIndex() + 1;
        // We have to subtract 2 to get the maximum index, since we have already written the header, and incremented the revision count in the header.
        int maximumRevisionIndex = getLogFileImpl().getRevisionCount() - 2;
        while (revisionToLookAtIndex <= maximumRevisionIndex) {
            RevisionHeader revisionToLookAt = getLogFileImpl().getRevisionHeader(revisionToLookAtIndex);
            if (revisionToLookAt.getDepth() > parentDepth) {
                // Write the revision header
                revisionToLookAt.write(newArchiveStream);

                // Write the revision data.
                long startingSourceSeekPosition = revisionToLookAt.getRevisionDataStartPosition();
                if (revisionToLookAt.getRevisionSize() > 0L) {
                    oldArchiveStream.seek(startingSourceSeekPosition);
                    copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, revisionToLookAt.getRevisionSize());
                }
            } else {
                break;
            }
            revisionToLookAtIndex++;
        }
    }
}
