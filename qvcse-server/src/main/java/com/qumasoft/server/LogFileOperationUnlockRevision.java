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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation to unlock a revision.
 *
 * @author Jim Voris
 */
class LogFileOperationUnlockRevision extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileOperationUnlockRevision.class);

    private final UnlockRevisionCommandArgs commandLineArgs;
    private final String userName;
    private final String revisionString;
    private final AtomicReference<String> mutableRevisionString;
    private RandomAccessFile newArchiveStream;

    /**
     * Creates a new instance of LogFileOperationUnlockRevision.
     * @param args command arguments for the unlock operation.
     */
    LogFileOperationUnlockRevision(Object[] args) {
        super(args, (LogFileImpl) args[0]);
        commandLineArgs = (UnlockRevisionCommandArgs) args[1];
        userName = commandLineArgs.getUserName();
        revisionString = commandLineArgs.getRevisionString();
        mutableRevisionString = new AtomicReference<>(revisionString);
    }

    @Override
    public boolean execute() throws QVCSException {

        // Make sure lock checking is enabled... we can't unlock an archive that does not support it.
        if (!getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().attributes().getIsCheckLock()) {
            // Do not allow a lock operation on a file that
            // does not support locking.
            throw new QVCSException(getLogFileImpl().getShortWorkfileName() + " does not support locking.");
        }

        return unlockRevision();
    }

    private boolean unlockRevision() throws QVCSException {
        boolean retVal = false;

        if (!getLogFileImpl().isArchiveInformationRead()) {
            retVal = getLogFileImpl().readInformation();
        }

        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        // Figure out the revision string if we need to.
        if (0 == mutableRevisionString.get().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
            String lockedRevision = getLogFileImpl().getLockedRevisionString(userName);
            if (lockedRevision != null) {
                mutableRevisionString.set(getLogFileImpl().getLockedRevisionString(userName));
                commandLineArgs.setRevisionString(mutableRevisionString.get());
            } else {
                throw new QVCSException(userName + " does not have a locked revision for " + getLogFileImpl().getShortWorkfileName());
            }
        }

        // Make sure the revision is locked.
        AtomicInteger revisionIndex = new AtomicInteger();
        if (!getLogFileImpl().isRevisionLocked(mutableRevisionString, revisionIndex)) {
            throw new QVCSException("Revision " + mutableRevisionString.get() + " is not locked for " + getLogFileImpl().getShortWorkfileName());
        }

        // Make a copy of the archive.  We'll operate on this copy.
        if (!getLogFileImpl().createCopyOfArchive()) {
            throw new QVCSException("Unable to create temporary copy of archive for " + getLogFileImpl().getShortWorkfileName());
        }

        try {
            newArchiveStream = new java.io.RandomAccessFile(getLogFileImpl().getTempFile(), "rw");

            // Seek to the location of this revision in the file.
            RevisionHeader revInfo = getLogFileImpl().getRevisionHeader(revisionIndex.get());
            newArchiveStream.seek(revInfo.getRevisionStartPosition());

            // Update the revision information before we write it out.
            revInfo.setIsLocked(false);
            revInfo.setLockerIndex(0);

            // Write the updated revision info.
            revInfo.updateInPlace(newArchiveStream);

            // Update the header with info about this locker.
            getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().decrementLockCount();

            // Update the header information in the stream.
            getLogFileImpl().getLogFileHeaderInfo().updateInPlace(newArchiveStream);
            retVal = true;
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new QVCSException(e.getLocalizedMessage());
        } finally {
            try {
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
