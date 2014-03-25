/*   Copyright 2004-2014 Jim Voris
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

import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Get a revision.
 * @author Jim Voris
 */
class LogFileOperationGetRevision extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final GetRevisionCommandArgs commandLineArgs;
    private final String revisionString;
    private final String labelString;
    private final String fetchToFilename;
    private final AtomicReference<String> mutableRevisionString;
    private final MutableByteArray processedBuffer = new MutableByteArray();

    /**
     * Creates a new instance of LogFileOperationCheckOut
     * @param args the arguments. a[0] is the logfileImpl; a[1] is the command argument object.
     */
    public LogFileOperationGetRevision(Object[] args) {
        super(args, (LogFileImpl) args[0]);
        fetchToFilename = (String) args[1];
        commandLineArgs = (GetRevisionCommandArgs) args[2];
        revisionString = commandLineArgs.getRevisionString();
        labelString = commandLineArgs.getLabel();
        mutableRevisionString = new AtomicReference<>(revisionString);
    }

    @Override
    public boolean execute() throws QVCSException {
        boolean retVal = false;


        // Do the real work here.
        try {
            // 1. Retrieve the revision into the requested file
            retVal = getRevision();
            commandLineArgs.setRevisionString(mutableRevisionString.get());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "LogFileOperationCheckOut exception: " + e.toString() + ": " + e.getMessage());
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            retVal = false;
        } finally {
            // Remove any old archives.
            getLogFileImpl().getTempFile().delete();
            getLogFileImpl().getOldFile().delete();
        }

        return retVal;
    }

    private boolean getRevision() {
        boolean bRetVal = true;
        if (!getLogFileImpl().isArchiveInformationRead()) {
            bRetVal = getLogFileImpl().readInformation();
        }

        if (bRetVal) {
            // Figure out the default revision string if we need to.
            if ((revisionString != null) && (revisionString.length() > 0)) {
                if (0 == mutableRevisionString.get().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
                    mutableRevisionString.set(getLogFileImpl().getDefaultRevisionHeader().getRevisionString());
                }
            } else if (commandLineArgs.getByLabelFlag()) {
                // The label may not be present here.
                bRetVal = false;

                // This is a 'get by label' request.  Find the label, if we can.
                mutableRevisionString.set(getRevisionStringFromLabel(labelString));
                if (mutableRevisionString.get() != null) {
                    bRetVal = true;
                } else {
                    commandLineArgs.setFailureReason("Revision not found for label: [" + labelString + "].");
                }
            } else if (commandLineArgs.getByDateFlag()) {
                // Find the revision in effect at the requested date... If the file
                // hadn't yet been created, then we'll have to skip it. Note that
                // we have to honor the 'default branch', so that we need to walk
                // the revision tree to find only those revisions associated with
                // the 'default branch', and then choose the correct revision from
                // among that set of revisions.
                bRetVal = getRevisionInEffectAtRequestedDate();
            } else {
                bRetVal = false;
                commandLineArgs.setFailureReason("Internal error: Invalid command sent to getRevision()");
            }

            if (bRetVal) {
                AtomicInteger revisionIndex = new AtomicInteger();

                // Confirm that the revision exists in the archive
                bRetVal = getLogFileImpl().findRevision(mutableRevisionString.get(), revisionIndex);

                if (bRetVal) {
                    try {
                        if (getLogFileImpl().open()) {
                            bRetVal = getLogFileImpl().fetchRevision(getLogFileImpl().getRevisionHeader(revisionIndex.get()), fetchToFilename, false, processedBuffer);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                        commandLineArgs.setFailureReason(e.getLocalizedMessage());
                        bRetVal = false;
                    } finally {
                        getLogFileImpl().close();
                    }
                } else {
                    commandLineArgs.setFailureReason("Revision: " + mutableRevisionString.get() + " not found in " + getLogFileImpl().getShortWorkfileName());
                }
            }
        } else {
            commandLineArgs.setFailureReason("Failed to read logfile information for " + getLogFileImpl().getShortWorkfileName());
        }

        return bRetVal;
    }

    private boolean getRevisionInEffectAtRequestedDate() {
        boolean retVal = false;
        Map<String, RevisionHeader> dateMap = new TreeMap<>();
        RevisionInformation revisionInformation = getLogFileImpl().getRevisionInformation();

        RevisionHeader defaultRevisionHeader = getLogFileImpl().getDefaultRevisionHeader();
        if (defaultRevisionHeader.getDepth() == 0) {
            for (int i = 0; i < getLogFileImpl().getRevisionCount(); i++) {
                RevisionHeader revisionHeader = revisionInformation.getRevisionHeader(i);
                if (revisionHeader.getDepth() == 0) {
                    dateMap.put(createDateKey(revisionHeader.getCheckInDate(), revisionHeader), revisionHeader);
                }
            }
        } else {
            for (int i = 0; i < getLogFileImpl().getRevisionCount(); i++) {
                RevisionHeader revisionHeader = revisionInformation.getRevisionHeader(i);
                if (revisionHeader.getRevisionDescriptor().compareTo(defaultRevisionHeader.getRevisionDescriptor()) <= 0) {
                    dateMap.put(createDateKey(revisionHeader.getCheckInDate(), revisionHeader), revisionHeader);
                }
            }
        }

        // This will iterate in date order, since we are using a Date as the key
        // to the TreeMap collection.  Find the last revision that is still
        // before the requested date.
        Iterator<RevisionHeader> it = dateMap.values().iterator();
        while (it.hasNext()) {
            RevisionHeader revisionHeader = it.next();
            if (revisionHeader.getCheckInDate().before(commandLineArgs.getByDateValue())) {
                mutableRevisionString.set(revisionHeader.getRevisionString());
                retVal = true;
            }
        }

        if (!retVal) {
            commandLineArgs.setFailureReason("No revision exists on the requested date: " + commandLineArgs.getByDateValue().toString());
        }
        return retVal;
    }

    // We need to include a sortable revision number in the key so that revisions
    // that have the same checkin Date will sort in a useful way. This is used
    // for getting the correct revision of a DirectoryContents object, since
    // the checkin times for those objects are often identical (since the checkin
    // Date is taken from the enclosing transaction object).
    private String createDateKey(Date date, RevisionHeader revisionHeader) {
        long time = date.getTime();
        String key = String.format("%020d:%s", time, revisionHeader.getRevisionDescriptor().toSortableString());
        return key;
    }
}
