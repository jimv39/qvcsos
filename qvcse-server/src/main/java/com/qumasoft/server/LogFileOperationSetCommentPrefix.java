//   Copyright 2004-2015 Jim Voris
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
import com.qumasoft.qvcslib.QVCSException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation to change the comment prefix for an archive file.
 *
 * @author Jim Voris
 */
class LogFileOperationSetCommentPrefix extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileOperationSetCommentPrefix.class);

    private final String userName;
    private final String newCommentPrefix;
    private RandomAccessFile newArchiveStream;
    private RandomAccessFile oldArchiveStream;

    /**
     * Creates a new instance of LogFileOperationSetCommentPrefix
     * @param logfile the logfile we operate on.
     * @param user the user name.
     * @param newCommentPfx the new comment prefix.
     */
    public LogFileOperationSetCommentPrefix(LogFileImpl logfile, String user, String newCommentPfx) {
        super(null, logfile);
        userName = user;
        newCommentPrefix = newCommentPfx;
    }

    @Override
    public boolean execute() throws QVCSException {
        return setCommentPrefix();
    }

    private boolean setCommentPrefix() throws QVCSException {
        boolean retVal = true;

        if (!getLogFileImpl().isArchiveInformationRead()) {
            retVal = getLogFileImpl().readInformation();
        }

        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        // Delete the temp file for sure.  We need the temp file gone, since
        // we need to create the new archive file.
        getLogFileImpl().getTempFile().delete();

        try {
            newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");
            oldArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getFile(), "r");

            if (retVal) {
                // Change the new attributes.
                getLogFileImpl().getLogFileHeaderInfo().setCommentPrefix(newCommentPrefix);

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

        } catch (IOException e) {
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
