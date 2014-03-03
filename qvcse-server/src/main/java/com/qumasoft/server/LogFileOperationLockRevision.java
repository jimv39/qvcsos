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

import com.qumasoft.qvcslib.LogFileOperationLockRevisionCommandArgs;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Operation to lock a revision.
 *
 * @author Jim Voris
 */
class LogFileOperationLockRevision extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final LogFileOperationLockRevisionCommandArgs commandLineArgs;
    private final String userName;
    private final String revisionString;
    private final String outputFileName;
    private final AtomicReference<String> mutableRevisionString;
    private RandomAccessFile newArchiveStream;

    /**
     * Creates a new instance of LogFileOperationCheckOut.
     * @param args the operation arguments.
     */
    public LogFileOperationLockRevision(Object[] args) {
        super(args, (LogFileImpl) args[0]);
        commandLineArgs = (LogFileOperationLockRevisionCommandArgs) args[1];
        userName = commandLineArgs.getUserName();
        revisionString = commandLineArgs.getRevisionString();
        outputFileName = commandLineArgs.getOutputFileName();
        mutableRevisionString = new AtomicReference<>(revisionString);
    }

    @Override
    public boolean execute() throws QVCSException {
        boolean retVal = true;

        if (!getLogFileImpl().isArchiveInformationRead()) {
            retVal = getLogFileImpl().readInformation();
        }

        if (retVal) {
            // Make sure lock checking is enabled... we can't lock an archive that does not support it.
            if (!getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().attributes().getIsCheckLock()) {
                // Do not allow a lock operation on a file that
                // does not support locking.
                throw new QVCSException(getLogFileImpl().getShortWorkfileName() + " does not support locking.  Try doing a 'get' instead.");
            }

            retVal = lockRevision();
        }
        return retVal;
    }

    private boolean lockRevision() throws QVCSException {
        boolean retVal = false;

        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        // Figure out the revision string if we need to.
        if (0 == mutableRevisionString.get().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
            mutableRevisionString.set(getLogFileImpl().getDefaultRevisionHeader().getRevisionString());
            commandLineArgs.setRevisionString(mutableRevisionString.get());
        }

        // Make sure the revision is not already locked.
        AtomicInteger revisionIndex = new AtomicInteger();
        if (getLogFileImpl().isRevisionLocked(mutableRevisionString, revisionIndex)) {
            throw new QVCSException("Revision [" + mutableRevisionString.get() + "] is already locked for [" + getLogFileImpl().getShortWorkfileName() + "]");
        }

        // Make sure user does not already have a different revision locked.
        if (getLogFileImpl().isLockedByUser(userName)) {
            throw new QVCSException("User [" + userName + "] already holds a lock for [" + getLogFileImpl().getShortWorkfileName() + "]");
        }

        // Make a copy of the archive.  We'll operate on this copy.
        if (!getLogFileImpl().createCopyOfArchive()) {
            throw new QVCSException("Unable to create temporary copy of archive for [" + getLogFileImpl().getShortWorkfileName() + "]");
        }

        try {
            newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");

            // Seek to the location of this revision in the file.
            RevisionHeader revInfo = getLogFileImpl().getRevisionHeader(revisionIndex.get());
            newArchiveStream.seek(revInfo.getRevisionStartPosition());

            // Update the revision information before we write it out.
            revInfo.setIsLocked(true);
            revInfo.setLockerIndex(getLogFileImpl().getModifierList().userToIndex(userName));

            // Write the updated revision info.
            revInfo.updateInPlace(newArchiveStream);

            // Update the header with info about this locker.
            getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().incrementLockCount();

            // Update the header with the info about the workfile location.
            getLogFileImpl().getLogFileHeaderInfo().setWorkfileName(outputFileName);

            // Update the header information in the stream.
            getLogFileImpl().getLogFileHeaderInfo().updateInPlace(newArchiveStream);

            retVal = true;
        } catch (IOException e) {
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
