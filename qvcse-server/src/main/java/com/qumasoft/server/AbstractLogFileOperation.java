/*   Copyright 2004-2015 Jim Voris
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

import com.qumasoft.qvcslib.CompareFilesEditHeader;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract logfile operation. Abstract base class for server operations. Code common across different operations belongs here.
 * @author Jim Voris
 */
public abstract class AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLogFileOperation.class);
    private static final int FILECOPY_BUFFER_SIZE = 524_288;
    private final Object[] args;
    private final LogFileImpl logfileImpl;

    /**
     * Creates a new instance of AbstractLogFileOperation.
     * @param a the arguments.
     * @param impl the logfile implementation for this operation.
     */
    public AbstractLogFileOperation(Object[] a, LogFileImpl impl) {
        this.args = a;
        this.logfileImpl = impl;
    }

    LogFileImpl getLogFileImpl() {
        return this.logfileImpl;
    }

    Object[] getArgs() {
        return this.args;
    }

    /**
     * Perform the operation.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if things went wrong.
     */
    public abstract boolean execute() throws QVCSException;

    static void copyFromOneOpenFileToAnotherOpenFile(RandomAccessFile sourceStream, RandomAccessFile outputStream, long numberOfBytesToCopyFromSource) throws IOException {
        // Assume that both streams are positioned where they need to be positioned.
        if (numberOfBytesToCopyFromSource > 0L) {
            byte[] fileCopyBuffer = new byte[FILECOPY_BUFFER_SIZE];

            while (true) {
                // Figure out how much to tranfer with this read/write
                long lChunk;
                if (((numberOfBytesToCopyFromSource / FILECOPY_BUFFER_SIZE) > 0)) {
                    lChunk = FILECOPY_BUFFER_SIZE;
                } else {
                    lChunk = (numberOfBytesToCopyFromSource % FILECOPY_BUFFER_SIZE);
                }

                // See if there is any more to xfer
                if (lChunk <= 0L) {
                    break;
                }

                int lRead = sourceStream.read(fileCopyBuffer, 0, (int) lChunk);
                if (lRead == lChunk) {
                    outputStream.write(fileCopyBuffer, 0, (int) lChunk);
                } else {
                    throw new IOException("Failed to read " + lChunk + " bytes.");
                }

                numberOfBytesToCopyFromSource -= lRead;
            }
        }
    }

    static void copyFromOneOpenFileToAnotherOpenFile(InputStream sourceStream, RandomAccessFile outputStream, long numberOfBytesToCopyFromSource) throws IOException {
        // Assume that both streams are positioned where they need to be positioned.
        if (numberOfBytesToCopyFromSource > 0L) {
            byte[] fileCopyBuffer = new byte[FILECOPY_BUFFER_SIZE];

            while (true) {
                // Figure out how much to tranfer with this read/write
                long lChunk;
                if (((numberOfBytesToCopyFromSource / FILECOPY_BUFFER_SIZE) > 0)) {
                    lChunk = FILECOPY_BUFFER_SIZE;
                } else {
                    lChunk = (numberOfBytesToCopyFromSource % FILECOPY_BUFFER_SIZE);
                }

                // See if there is any more to xfer
                if (lChunk <= 0L) {
                    break;
                }

                int lRead = sourceStream.read(fileCopyBuffer, 0, (int) lChunk);
                if (lRead == lChunk) {
                    outputStream.write(fileCopyBuffer, 0, (int) lChunk);
                }

                numberOfBytesToCopyFromSource -= lRead;
            }
        }
    }

    static void copyFromOneOpenFileToAnotherOpenFile(InputStream sourceStream, OutputStream outputStream, long numberOfBytesToCopyFromSource) throws IOException {
        // Assume that both streams are positioned where they need to be positioned.
        if (numberOfBytesToCopyFromSource > 0L) {
            byte[] fileCopyBuffer = new byte[FILECOPY_BUFFER_SIZE];

            while (true) {
                // Figure out how much to tranfer with this read/write
                long lChunk;
                if (((numberOfBytesToCopyFromSource / FILECOPY_BUFFER_SIZE) > 0)) {
                    lChunk = FILECOPY_BUFFER_SIZE;
                } else {
                    lChunk = (numberOfBytesToCopyFromSource % FILECOPY_BUFFER_SIZE);
                }

                // See if there is any more to xfer
                if (lChunk <= 0L) {
                    break;
                }

                int lRead = sourceStream.read(fileCopyBuffer, 0, (int) lChunk);
                if (lRead == lChunk) {
                    outputStream.write(fileCopyBuffer, 0, (int) lChunk);
                }

                numberOfBytesToCopyFromSource -= lRead;
            }
        }
    }

    protected boolean getRevision(String userName, AtomicReference<String> mutableRevisionString, String outputFilename) throws QVCSException {
        GetRevisionCommandArgs commandArgs = new GetRevisionCommandArgs();
        commandArgs.setRevisionString(mutableRevisionString.get());
        commandArgs.setShortWorkfileName(logfileImpl.getShortWorkfileName());
        commandArgs.setOutputFileName(outputFilename);
        commandArgs.setUserName(userName);
        return logfileImpl.getRevision(commandArgs, outputFilename);
    }

    /**
     * Used by the checkInRevision operation. This returns the name of the tempfile that we get the revision into.
     * @param userName the user name.
     * @param revisionString the revision string.
     * @return the full file name of the temp file that contains the fetched revision associated with the given revision string.
     * @throws com.qumasoft.qvcslib.QVCSException
     */
    protected String getRevisionToTempfile(String userName, String revisionString) throws QVCSException {
        File tempFileForExistingRevision = null;
        try {
            tempFileForExistingRevision = File.createTempFile("QVCS", ".tmp");
        } catch (java.io.IOException e) {
            LOGGER.warn("Could not create temp file for existing revision:", e);
            throw new QVCSException("Failed to create QVCS temp file: " + e.getMessage());
        }
        String tempFileNameForExistingRevision = tempFileForExistingRevision.getAbsolutePath();
        AtomicReference<String> mutableRevisionString = new AtomicReference<>(revisionString);
        if (!getRevision(userName, mutableRevisionString, tempFileNameForExistingRevision)) {
            throw new QVCSException("Failed to get revision " + mutableRevisionString.get() + " into QVCS temp file: " + tempFileNameForExistingRevision);
        } else {
            LOGGER.trace("Fetched revision: [{}] into temporary file: [{}]", revisionString, tempFileNameForExistingRevision);
        }
        return tempFileNameForExistingRevision;
    }

    protected int getCompressedRevisionSizeForWorkfile(File workfile, Compressor compressor) {
        return getCompressedRevisionSize(workfile, 0, compressor);
    }

    protected int getCompressedRevisionSizeForDelta(File tempFileForCompareResults, Compressor compressor) {
        return getCompressedRevisionSize(tempFileForCompareResults, CompareFilesEditHeader.getEditHeaderSize(), compressor);
    }

    protected int getCompressedRevisionSize(File inputFile, int skipBeginningByteCount, Compressor compressor) {
        int returnedLength = (int) inputFile.length() - skipBeginningByteCount;

        java.io.FileInputStream inputStream = null;
        try {
            inputStream = new java.io.FileInputStream(inputFile);
            compressor.setUncompressedBuffer(new byte[returnedLength]);
            inputStream.skip(skipBeginningByteCount);
            Utility.readDataFromStream(compressor.getUncompressedBuffer(), inputStream);
            if (logfileImpl.getLogFileHeaderInfo().getLogFileHeader().attributes().getIsCompression()) {
                if (compressor.compress(compressor.getUncompressedBuffer())) {
                    returnedLength = compressor.getCompressedBuffer().length;
                    LOGGER.trace("Compressed from: [{}] to: [{}]", compressor.getUncompressedBuffer().length, returnedLength);
                }
            }
        } catch (IOException e) {
            // Really should log a problem here.
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.info(Utility.expandStackTraceToString(e));
                }
            }
        }
        return returnedLength;
    }

    protected String getRevisionStringFromLabel(final java.lang.String labelString) {
        String revisionStringForLabel = null;

        // This is a 'get by label' request.  Find the label, if we can.
        if ((labelString != null) && (logfileImpl != null)) {
            LabelInfo[] labelInfo = logfileImpl.getLogFileHeaderInfo().getLabelInfo();
            if (labelInfo != null) {
                for (LabelInfo labelInfo1 : labelInfo) {
                    if (labelString.equals(labelInfo1.getLabelString())) {
                        // If it is a floating label, we have to figure out the
                        // revision string...
                        if (labelInfo1.isFloatingLabel()) {
                            RevisionInformation revisionInformation = logfileImpl.getRevisionInformation();
                            int revisionCount = logfileImpl.getRevisionCount();
                            for (int j = 0; j < revisionCount; j++) {
                                RevisionHeader revHeader = revisionInformation.getRevisionHeader(j);
                                if (revHeader.getDepth() == labelInfo1.getDepth()) {
                                    if (revHeader.isTip()) {
                                        String labelRevisionString = labelInfo1.getLabelRevisionString();
                                        String revisionString = revHeader.getRevisionString();
                                        if (revisionString.startsWith(labelRevisionString)) {
                                            revisionStringForLabel = revisionString;
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            revisionStringForLabel = labelInfo1.getLabelRevisionString();
                        }
                        break;
                    }
                }
            }
        }
        return revisionStringForLabel;
    }

    protected void addTipRevision(String userName, String operationName, String checkinComment, Date checkinDate, String appendedPathWorkfileName, String shortWorkfileName,
            RevisionHeader revisionHeader) throws QVCSException {
        // Get this tip revision to a temp file that we'll use for the checkin
        // operation.
        String tempFileName = getRevisionToTempfile(userName, revisionHeader.getRevisionString());

        // If lock-checking is enabled, I have to lock this tip revision before
        // I can create a new tip revision.
        if (logfileImpl.getLogFileHeaderInfo().getLogFileHeader().attributes().getIsCheckLock()) {
            // The command args
            LockRevisionCommandArgs currentCommandArgs = new LockRevisionCommandArgs();

            currentCommandArgs.setRevisionString(revisionHeader.getRevisionString());

            currentCommandArgs.setUserName(userName);
            currentCommandArgs.setFullWorkfileName(appendedPathWorkfileName);
            currentCommandArgs.setShortWorkfileName(shortWorkfileName);
            currentCommandArgs.setOutputFileName(tempFileName);
            if (!logfileImpl.lockRevision(currentCommandArgs)) {
                throw new QVCSException("Failed to lock revision " + revisionHeader.getRevisionString() + " for " + operationName + " operation.");
            }
        }

        // Checkin the new revision for this tip.
        CheckInCommandArgs checkInCommandArgs = new CheckInCommandArgs();
        checkInCommandArgs.setUserName(userName);
        checkInCommandArgs.setLockedRevisionString(revisionHeader.getRevisionString());
        checkInCommandArgs.setCheckInComment(checkinComment);

        checkInCommandArgs.setInputfileTimeStamp(checkinDate);
        checkInCommandArgs.setFullWorkfileName(appendedPathWorkfileName);
        checkInCommandArgs.setShortWorkfileName(shortWorkfileName);

        // Set flags;
        checkInCommandArgs.setLockFlag(false);
        checkInCommandArgs.setForceBranchFlag(false);
        checkInCommandArgs.setApplyLabelFlag(false);
        checkInCommandArgs.setFloatLabelFlag(false);
        checkInCommandArgs.setReuseLabelFlag(false);
        checkInCommandArgs.setCreateNewRevisionIfEqual(true);
        checkInCommandArgs.setNoExpandKeywordsFlag(true);
        checkInCommandArgs.setProtectWorkfileFlag(false);

        // Set some other values
        checkInCommandArgs.setLabel(null);

        // And add the new tip revision.
        if (!logfileImpl.checkInRevision(checkInCommandArgs, tempFileName, false)) {
            throw new QVCSException("Failed to add revision for " + operationName + " operation.");
        }
    }

    protected void addTipRevisionForFeatureBranch(String userName, String operationName, String checkinComment, Date checkinDate, String appendedPathWorkfileName,
            String shortWorkfileName,
            RevisionHeader revisionHeader, final String branchLabel) throws QVCSException {
        // Get this tip revision to a temp file that we'll use for the checkin
        // operation.
        String tempFileName = getRevisionToTempfile(userName, revisionHeader.getRevisionString());

        // Checkin the new revision for this tip.
        CheckInCommandArgs checkInCommandArgs = new CheckInCommandArgs();
        if (logfileImpl.getLogfileInfo().getLogFileHeaderInfo().hasLabel(branchLabel)) {
            checkInCommandArgs.setForceBranchFlag(false);
            checkInCommandArgs.setReuseLabelFlag(true);
        } else {
            checkInCommandArgs.setForceBranchFlag(true);
        }
        checkInCommandArgs.setUserName(userName);
        checkInCommandArgs.setLockedRevisionString(revisionHeader.getRevisionString());
        checkInCommandArgs.setCheckInComment(checkinComment);

        checkInCommandArgs.setInputfileTimeStamp(checkinDate);
        checkInCommandArgs.setFullWorkfileName(appendedPathWorkfileName);
        checkInCommandArgs.setShortWorkfileName(shortWorkfileName);

        // Set flags;
        checkInCommandArgs.setLockFlag(false);
        checkInCommandArgs.setApplyLabelFlag(true);
        checkInCommandArgs.setCreateNewRevisionIfEqual(true);
        checkInCommandArgs.setNoExpandKeywordsFlag(true);
        checkInCommandArgs.setProtectWorkfileFlag(false);

        // Set some other values
        checkInCommandArgs.setLabel(branchLabel);

        // And add the new tip revision.
        if (!logfileImpl.checkInRevision(checkInCommandArgs, tempFileName, true)) {
            throw new QVCSException("Failed to add revision for " + operationName + " operation.");
        }
    }

    /**
     * Is the given revision the tip revision of a feature or opaque branch?
     *
     * @param revisionHeader the revision in question.
     * @return true if the revision is the tip revision on either a feature or opaque branch; false for all other revisions.
     */
    protected boolean isRevisionTipRevisionOfFeatureOrOpaqueBranch(RevisionHeader revisionHeader) {
        boolean retVal = false;
        if (revisionHeader.isTip() && revisionHeader.getDepth() > 0) {
            String revisionString = revisionHeader.getRevisionString();
            LabelInfo[] labelInfo = logfileImpl.getLogFileHeaderInfo().getLabelInfo();
            if (labelInfo != null) {
                for (LabelInfo labelInfo1 : labelInfo) {
                    String labelRevisionString = labelInfo1.getLabelRevisionString();
                    if (labelRevisionString.equals(revisionString)) {
                        // This label is associated with this revision.... is it a label for a feature branch?
                        if (labelInfo1.getLabelString().startsWith(QVCSConstants.QVCS_FEATURE_BRANCH_LABEL)) {
                            retVal = true;
                            break;
                        } else if (labelInfo1.getLabelString().startsWith(QVCSConstants.QVCS_OPAQUE_BRANCH_LABEL)) {
                            // This label is associated with this revision.... is it a label for an opaque branch?
                            retVal = true;
                            break;
                        }
                    }
                }
            }
        }
        return retVal;
    }
}
