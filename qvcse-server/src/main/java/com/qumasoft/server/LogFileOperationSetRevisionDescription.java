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

import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation to change the revision description of an existing revision.
 *
 * @author Jim Voris
 */
class LogFileOperationSetRevisionDescription extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileOperationSetRevisionDescription.class);
    private final SetRevisionDescriptionCommandArgs commandLineArgs;
    private final String userName;
    private final String revisionString;
    private final String revisionDescription;
    private RandomAccessFile newArchiveStream;
    private RandomAccessFile oldArchiveStream;

    /**
     * Creates a new instance of LogFileOperationSetRevisionDescription.
     * @param args arguments needed for the operation.
     */
    LogFileOperationSetRevisionDescription(Object[] args) {
        super(args, (LogFileImpl) args[0]);
        commandLineArgs = (SetRevisionDescriptionCommandArgs) args[1];
        userName = commandLineArgs.getUserName();
        revisionString = commandLineArgs.getRevisionString();
        revisionDescription = commandLineArgs.getRevisionDescription();
    }

    @Override
    public boolean execute() throws QVCSException {

        return modifyRevisionDescription();
    }

    private boolean modifyRevisionDescription() throws QVCSException {
        boolean retVal;

        if (!getLogFileImpl().isArchiveInformationRead()) {
            retVal = getLogFileImpl().readInformation();
        } else {
            retVal = true;
        }

        // If we can't read the archive information, then don't bother to try to do anything.
        if (!retVal) {
            return retVal;
        }

        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        // Figure out the revision index.
        AtomicInteger revisionIndex = new AtomicInteger();
        if (!getLogFileImpl().findRevision(revisionString, revisionIndex)) {
            throw new QVCSException("Revision " + revisionString + " not found for " + getLogFileImpl().getShortWorkfileName());
        }

        // Delete the temp file for sure.  We need the temp file gone, since
        // we need to create the new archive file.
        getLogFileImpl().getTempFile().delete();

        try {
            newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");
            oldArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getFile(), "r");

            // Lookup the location of this revision in the file.
            RevisionHeader revInfo = getLogFileImpl().getRevisionHeader(revisionIndex.get());

            // Copy the old archive to the new archive up to the start of the revision
            // that we are changing.
            copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, revInfo.getRevisionStartPosition());

            // Update the revision information before we write it out.
            revInfo.setRevisionDescription(revisionDescription);

            // Write the new revision info the the new archive.
            revInfo.write(newArchiveStream);

            // Seek to where we need to begin to copy the rest of the file.
            oldArchiveStream.seek(revInfo.getRevisionDataStartPosition());

            // Now copy the rest of the old archive to the new one.
            long numberOfBytesToCopyFromSource = oldArchiveStream.length() - oldArchiveStream.getFilePointer();
            copyFromOneOpenFileToAnotherOpenFile(oldArchiveStream, newArchiveStream, numberOfBytesToCopyFromSource);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            try {
                if (newArchiveStream != null) {
                    newArchiveStream.close();
                    newArchiveStream = null;
                }
                if (oldArchiveStream != null) {
                    oldArchiveStream.close();
                    oldArchiveStream = null;
                }
                retVal = true;
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
        return retVal;
    }
}
