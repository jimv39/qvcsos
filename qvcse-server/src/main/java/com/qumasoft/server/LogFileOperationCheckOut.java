/*   Copyright 2004-2015 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compli9nce with the License.
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
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation to check out a revision.
 *
 * @author Jim Voris
 */
class LogFileOperationCheckOut extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileOperationCheckOut.class);
    private final CheckOutCommandArgs commandLineArgs;
    private final String userName;
    private String revisionString;
    private final String labelString;
    private final String fetchToFilename;
    private final String fullWorkfileName;
    private final String shortWorkfileName;
    private final String outputFileName;
    private final String checkOutComment;
    private final AtomicReference<String> mutableRevisionString;

    /**
     * Creates a new instance of LogFileOperationCheckOut.
     * @param args arguments for the operation. a[0] is the logfileImpl; a[1] is the fetch-to filename; a[2] is the command arguments object.
     */
    LogFileOperationCheckOut(Object[] args) {
        super(args, (LogFileImpl) args[0]);
        fetchToFilename = (String) args[1];
        commandLineArgs = (CheckOutCommandArgs) args[2];
        userName = commandLineArgs.getUserName();
        revisionString = commandLineArgs.getRevisionString();
        labelString = commandLineArgs.getLabel();
        checkOutComment = commandLineArgs.getCheckOutComment();
        fullWorkfileName = commandLineArgs.getFullWorkfileName();
        shortWorkfileName = commandLineArgs.getShortWorkfileName();
        outputFileName = commandLineArgs.getOutputFileName();
        mutableRevisionString = new AtomicReference<>(revisionString);
    }

    @Override
    public boolean execute() throws QVCSException {
        boolean retVal = true;


        // Make sure user is on the access list.
        getLogFileImpl().makeSureIsOnAccessList(userName);

        // If we check locks, then check that no one has locked this revision...
        if (getLogFileImpl().getLogFileHeaderInfo().getLogFileHeader().attributes().getIsCheckLock()) {
            AtomicInteger revisionIndex = new AtomicInteger();

            // If we are doing a checkout by label, make sure the label exists...
            if (labelString != null) {
                String revisionStringFromLabel = getRevisionStringFromLabel(labelString);
                if (revisionStringFromLabel != null) {
                    mutableRevisionString.set(revisionStringFromLabel);
                } else {
                    retVal = false;
                    LOGGER.info("Label: '" + labelString + "' does not exist for file: " + fullWorkfileName);
                }
            }

            if (retVal) {
                // Make sure the revision is not already locked by anyone.
                if (getLogFileImpl().isRevisionLocked(mutableRevisionString, revisionIndex)) {
                    throw new QVCSException("Revision " + mutableRevisionString.get() + " of '" + fullWorkfileName + "' is already locked by "
                            + getLogFileImpl().indexToUsername(getLogFileImpl().getRevisionInformation().getRevisionHeader(revisionIndex.get()).getLockerIndex()));
                }

                // If working on the default revision, this gets filled in for us.
                revisionString = mutableRevisionString.get();
            }
        } else {
            // Do not allow a check-out operation on a file that
            // does not support locking.
            throw new QVCSException(getLogFileImpl().getShortWorkfileName() + " does not support lock checking.  Try doing a 'get' instead.");
        }

        // Do the real work here.
        try {
            if (retVal) {
                // 1. Lock the requested revision
                retVal = lockRevision();

                // 2. Retrieve the revision into the requested file
                if (retVal) {
                    retVal = getRevision();
                    commandLineArgs.setRevisionString(mutableRevisionString.get());
                }
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        } finally {
            // Remove any old archives.
            getLogFileImpl().getTempFile().delete();
            getLogFileImpl().getOldFile().delete();
        }

        return retVal;
    }

    private boolean lockRevision() throws QVCSException {
        LockRevisionCommandArgs lockCommandLineArgs = new LockRevisionCommandArgs();
        lockCommandLineArgs.setUserName(userName);
        lockCommandLineArgs.setRevisionString(mutableRevisionString.get());
        lockCommandLineArgs.setFullWorkfileName(fullWorkfileName);
        lockCommandLineArgs.setShortWorkfileName(shortWorkfileName);
        lockCommandLineArgs.setOutputFileName(outputFileName);
        lockCommandLineArgs.setCheckOutComment(checkOutComment);

        Object[] localArgs = new Object[2];
        localArgs[0] = getLogFileImpl();
        localArgs[1] = lockCommandLineArgs;
        LogFileOperationLockRevision command = new LogFileOperationLockRevision(localArgs);

        return command.execute();
    }

    private boolean getRevision() throws QVCSException {
        GetRevisionCommandArgs commandArgs = new GetRevisionCommandArgs();
        commandArgs.setUserName(userName);
        commandArgs.setRevisionString(mutableRevisionString.get());
        commandArgs.setLabel(labelString);
        commandArgs.setFullWorkfileName(fullWorkfileName);
        commandArgs.setShortWorkfileName(shortWorkfileName);
        commandArgs.setOutputFileName(fetchToFilename);

        // <editor-fold>
        Object[] localArgs = new Object[3];
        localArgs[0] = getLogFileImpl();
        localArgs[1] = fetchToFilename;
        // </editor-fold>
        localArgs[2] = commandArgs;
        LogFileOperationGetRevision command = new LogFileOperationGetRevision(localArgs);

        return command.execute();
    }
}
