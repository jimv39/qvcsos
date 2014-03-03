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
package com.qumasoft.server;

import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogFileOperationLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.MajorMinorRevisionPair;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Operation to label a revision.
 *
 * @author Jim Voris
 */
class LogFileOperationLabelRevision extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");

    private final LogFileOperationLabelRevisionCommandArgs commandLineArgs;
    private final String userName;
    private String revisionString;
    private final String labelString; // This is the new label string
    private final String duplicateLabelString; // This is old label that we duplicate
    private final boolean duplicateFlag;
    private final boolean floatingFlag;
    private final boolean reuseLabelFlag;
    private final AtomicReference<String> mutableRevisionString;
    private RandomAccessFile newArchiveStream;
    private RandomAccessFile oldArchiveStream;

    /**
     * Creates a new instance of LogFileOperationCheckOut.
     * @param args the command arguments. a[0] is the logfileImpl; a[1] is the command args object.
     */
    public LogFileOperationLabelRevision(Object[] args) {
        super(args, (LogFileImpl) args[0]);
        commandLineArgs = (LogFileOperationLabelRevisionCommandArgs) args[1];
        userName = commandLineArgs.getUserName();
        revisionString = commandLineArgs.getRevisionString();
        labelString = commandLineArgs.getLabelString();
        duplicateLabelString = commandLineArgs.getDuplicateLabelString();
        duplicateFlag = commandLineArgs.getDuplicateFlag();
        floatingFlag = commandLineArgs.getFloatingFlag();
        reuseLabelFlag = commandLineArgs.getReuseLabelFlag();
        mutableRevisionString = new AtomicReference<>(revisionString);
    }

    @Override
    public boolean execute() throws QVCSException {
        return labelRevision();
    }

    private boolean labelRevision() throws QVCSException {
        boolean retVal = false;
        boolean revisionExists = false;

        if (!getLogFileImpl().isArchiveInformationRead()) {
            retVal = getLogFileImpl().readInformation();
        }

        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        if (revisionString != null) {
            // Figure out the revision string if we need to.
            if (revisionString.equals(QVCSConstants.QVCS_DEFAULT_REVISION)) {
                mutableRevisionString.set(getLogFileImpl().getDefaultRevisionHeader().getRevisionString());
                commandLineArgs.setRevisionString(mutableRevisionString.get());
                revisionString = mutableRevisionString.get();
                revisionExists = true;
            } else {
                // They entered a revision string.  Make sure that revision
                // actually exists here.
                RevisionInformation revisionInformation = getLogFileImpl().getRevisionInformation();
                for (int i = 0; i < getLogFileImpl().getLogFileHeaderInfo().getRevisionCount(); i++) {
                    String revString = revisionInformation.getRevisionHeader(i).getRevisionString();
                    if (revString.equals(this.revisionString)) {
                        revisionExists = true;
                        break;
                    }
                }

                if (!revisionExists) {
                    LOGGER.log(Level.INFO, "Unable to apply label '" + labelString + "'.  Revision '" + revisionString + "' does not exist for: "
                            + getLogFileImpl().getShortWorkfileName() + ".");
                }
            }
        } else if (duplicateFlag) {
            // Have to find the original label, if it exists
            LabelInfo[] existingLabels = getLogFileImpl().getLogFileHeaderInfo().getLabelInfo();
            if (existingLabels != null) {
                for (LabelInfo existingLabel1 : existingLabels) {
                    String existingLabel = existingLabel1.getLabelString();
                    if (existingLabel.equals(duplicateLabelString)) {
                        if (existingLabel1.isFloatingLabel()) {
                            LabelInfo existingLabelInfo = existingLabel1;
                            // We need to figure out the revision string to float the label to.
                            String labelRevisionString = existingLabelInfo.getLabelRevisionString();
                            int revisionCount = getLogFileImpl().getRevisionCount();
                            for (int j = 0; j < revisionCount; j++) {
                                RevisionHeader revisionHeader = getLogFileImpl().getRevisionHeader(j);
                                if (revisionHeader.isTip()) {
                                    String revString = revisionHeader.getRevisionString();

                                    // See if this tip revision is on the same branch as the floating label.
                                    if (revString.length() > labelRevisionString.length()) {
                                        if (revString.startsWith(labelRevisionString)) {
                                            // The major number of the last major/minor pair must
                                            // match
                                            MajorMinorRevisionPair[] revisionPairs = revisionHeader.getRevisionDescriptor().getRevisionPairs();
                                            int revisionMajorNumber = revisionPairs[revisionHeader.getDepth()].getMajorNumber();
                                            LabelInfo.LabelMajorMinor[] labelPairs = existingLabelInfo.getMajorMinorPairs();
                                            int labelMajorNumber = labelPairs[existingLabelInfo.getDepth()].getMajorNumber();
                                            if ((revisionMajorNumber == labelMajorNumber)
                                                    && (revisionHeader.getDepth() == existingLabelInfo.getDepth())) {
                                                this.revisionString = revString;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            revisionString = existingLabel1.getLabelRevisionString();
                        }
                        commandLineArgs.setRevisionString(revisionString);
                        revisionExists = true;
                        break;
                    }
                }
            }

            if (!revisionExists) {
                LOGGER.log(Level.INFO, "Duplicate label request ignored for " + getLogFileImpl().getShortWorkfileName() + ". Original label (" + duplicateLabelString
                        + ") does not exist.");
                commandLineArgs.setRevisionString("");
                retVal = true;
            }
        }

        if (!revisionExists) {
            return retVal;
        }

        // Make sure we are not applying a label string that is already in use.
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
                        String errorMessage = "Label request ignored for " + getLogFileImpl().getShortWorkfileName() + ". Label (" + labelString + ") is already in use.";
                        commandLineArgs.setErrorMessage(errorMessage);
                        LOGGER.log(Level.INFO, errorMessage);
                        return false;
                    }
                    break;
                }
            }
        }

        // Delete the temp file for sure.  We need the temp file gone, since
        // we need to create the new archive file.
        getLogFileImpl().getTempFile().delete();

        try {
            newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");
            oldArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getFile(), "r");

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
            int creatorIndex = getLogFileImpl().getModifierList().userToIndex(userName);
            LabelInfo labelInfo = new LabelInfo(labelString, revisionString, floatingFlag, creatorIndex);

            // Add it to the array at the beginning.
            newLabelInfo[0] = labelInfo;

            // Set the new label info array on the archive header.
            getLogFileImpl().getLogFileHeaderInfo().setLabelInfo(newLabelInfo);

            // Write the header information to the stream.
            getLogFileImpl().getLogFileHeaderInfo().write(newArchiveStream);

            // Position the old archive to the end of the header area.
            LogFileHeaderInfo oldHeaderInfo = new LogFileHeaderInfo();
            oldHeaderInfo.read(oldArchiveStream);

            // Figure out how many bytes remain in the original that need to
            // be copied to the new archive.
            long numberOfBytesToCopyFromSource = oldArchiveStream.length() - oldArchiveStream.getFilePointer();

            // Copy the rest of the original archive to the new archive.
            copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, numberOfBytesToCopyFromSource);

            retVal = true;
        } catch (QVCSException | IOException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            retVal = false;
        } finally {
            try {
                if (oldArchiveStream != null) {
                    oldArchiveStream.close();
                }
                if (newArchiveStream != null) {
                    newArchiveStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                retVal = false;
            }
        }

        // Replace existing archive with new one.
        if (retVal) {
            getLogFileImpl().replaceExistingArchiveWithNewTempArchive();
        } else {
            getLogFileImpl().getTempFile().delete();
        }
        return retVal;
    }
}
