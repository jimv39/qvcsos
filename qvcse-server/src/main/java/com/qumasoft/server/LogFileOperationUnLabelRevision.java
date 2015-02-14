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

import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogFileReadException;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation to remove a label.
 *
 * @author Jim Voris
 */
class LogFileOperationUnLabelRevision extends AbstractLogFileOperation {

    private final UnLabelRevisionCommandArgs commandLineArgs;
    private final String userName;
    private final String labelString; // This is label we remove
    private final String shortWorkfileName;
    private RandomAccessFile newArchiveStream;
    private RandomAccessFile oldArchiveStream;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileOperationUnLabelRevision.class);

    /**
     * Creates a new instance of LogFileOperationUnLabelRevision.
     * @param args command arguments.
     */
    public LogFileOperationUnLabelRevision(Object[] args) {
        super(args, (LogFileImpl) args[0]);
        commandLineArgs = (UnLabelRevisionCommandArgs) args[1];
        userName = commandLineArgs.getUserName();
        labelString = commandLineArgs.getLabelString();
        shortWorkfileName = commandLineArgs.getShortWorkfileName();
    }

    @Override
    public boolean execute() throws QVCSException {
        return unLabelRevision();
    }

    private boolean unLabelRevision() throws QVCSException {
        boolean retVal = false;

        if (!getLogFileImpl().isArchiveInformationRead()) {
            retVal = getLogFileImpl().readInformation();
        }

        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        // Make sure we are removing a label string that exists.
        boolean labelFound = false;
        int labelIndex = -1;
        LabelInfo[] existingLabels = getLogFileImpl().getLogFileHeaderInfo().getLabelInfo();
        if (existingLabels != null) {
            for (int i = 0; i < existingLabels.length; i++) {
                String existingLabel = existingLabels[i].getLabelString();
                if (existingLabel.equals(labelString)) {
                    labelFound = true;
                    labelIndex = i;
                    break;
                }
            }
        }

        if (labelFound) {
            // Delete the temp file for sure.  We need the temp file gone, since
            // we need to create the new archive file and it will be smaller
            // than the existing archive file.
            getLogFileImpl().getTempFile().delete();

            try {
                newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");
                oldArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getFile(), "r");

                // Create a new LabelInfo array that's one smaller than before
                LabelInfo[] oldLabelInfo = getLogFileImpl().getLogFileHeaderInfo().getLabelInfo();
                int newLabelInfoSize = oldLabelInfo.length - 1;

                if (newLabelInfoSize == 0) {
                    // Clear all label information.
                    getLogFileImpl().getLogFileHeaderInfo().setLabelInfo(null);
                } else {
                    LabelInfo[] newLabelInfo = new LabelInfo[newLabelInfoSize];

                    // Copy the existing labels to the new label info array, leaving
                    // out the one we are deleting.
                    int j = 0;
                    for (int i = 0; i < oldLabelInfo.length; i++) {
                        if (i != labelIndex) {
                            newLabelInfo[j++] = oldLabelInfo[i];
                        }
                    }
                    // Set the label information.
                    getLogFileImpl().getLogFileHeaderInfo().setLabelInfo(newLabelInfo);
                }

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
            } catch (LogFileReadException | IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
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
                    LOGGER.warn(e.getLocalizedMessage(), e);
                    retVal = false;
                }
            }

            // Replace existing archive with new one.
            if (retVal) {
                getLogFileImpl().replaceExistingArchiveWithNewTempArchive();
            } else {
                getLogFileImpl().getTempFile().delete();
            }
        } else {
            String errorMessage = "Label not found: [" + labelString + "] in " + shortWorkfileName;
            commandLineArgs.setErrorMessage(errorMessage);
            LOGGER.info(errorMessage);
        }

        return retVal;
    }
}
