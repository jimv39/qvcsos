//   Copyright 2004-2019 Jim Voris
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

import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogFileReadException;
import com.qumasoft.qvcslib.QVCSException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation to mark a logfile obsolete or not obsolete.
 *
 * @author Jim Voris
 * @deprecated Use the delete operation instead.
 */
class LogFileOperationSetIsObsolete extends AbstractLogFileOperation {

    private final String userName;
    private final boolean obsoleteFlag;
    private RandomAccessFile newArchiveStream;
    private RandomAccessFile oldArchiveStream;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileOperationSetIsObsolete.class);

    /**
     * Creates a new instance of LogFileOperationCheckOut
     */
    LogFileOperationSetIsObsolete(LogFileImpl logfile, String user, boolean flag) {
        super(null, logfile);
        this.userName = user;
        this.obsoleteFlag = flag;
    }

    @Override
    public boolean execute() throws QVCSException {
        return setIsObsolete();
    }

    private boolean setIsObsolete() throws QVCSException {
        // NOT USED.
        boolean retVal = false;

        if (!getLogFileImpl().isArchiveInformationRead()) {
            retVal = getLogFileImpl().readInformation();
        }

        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        // Make sure there are no locks on the archive
        if (getLogFileImpl().getLockCount() > 0) {
            throw new QVCSException("Unable to delete: " + getLogFileImpl().getShortWorkfileName() + ". There are locked revisions.  Remove all locks, then try again.");
        }

        // Delete the temp file for sure.  We need the temp file gone, since
        // we need to create the new archive file.
        getLogFileImpl().getTempFile().delete();

        try {
            newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");
            oldArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getFile(), "r");
            int creatorIndex = getLogFileImpl().getModifierList().userToIndex(userName);

            // Mark it obsolete
            if (obsoleteFlag) {
                if (!getLogFileImpl().getIsObsolete()) {
                    getLogFileImpl().getLogFileHeaderInfo().setIsObsolete(obsoleteFlag, creatorIndex);
                    retVal = true;
                }
            } else {
                if (getLogFileImpl().getIsObsolete()) {
                    getLogFileImpl().getLogFileHeaderInfo().setIsObsolete(obsoleteFlag, creatorIndex);
                    retVal = true;
                }
            }

            if (retVal) {
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
            }
        } catch (IOException | LogFileReadException e) {
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
        return retVal;
    }
}
