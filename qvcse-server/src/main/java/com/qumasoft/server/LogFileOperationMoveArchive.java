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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.Utility;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Make the archive changes needed to record a move operation. This class is used by the move operation to duplicate the tip revisions in the logfile so that the move operation
 * is guaranteed to create a new revision.
 * @author Jim Voris
 */
class LogFileOperationMoveArchive extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final String userName;
    private final String checkInComment;
    private final String shortWorkfileName;
    private final String appendedPathWorkfileName;
    private final Date checkInDate;

    /**
     * Creates a new instance of LogFileOperationMoveArchive.
     * @param logFile the file we are working on.
     * @param user the user name.
     * @param checkInCmmnt the checkin comment.
     * @param appendedPath the appended path.
     * @param shortName the short workfile name.
     * @param date the date to use for this operation.
     */
    public LogFileOperationMoveArchive(LogFileImpl logFile, String user, String checkInCmmnt, String appendedPath, String shortName, final Date date) {
        super(null, logFile);
        userName = user;
        shortWorkfileName = shortName;
        checkInComment = QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX + checkInCmmnt;
        checkInDate = date;
        appendedPathWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortWorkfileName;
    }

    @Override
    public boolean execute() throws QVCSException {
        boolean retVal = true;

        try {
            // Make sure user is on the access list.
            getLogFileImpl().makeSureIsOnAccessList(userName);

            // Make a collection of all the tip revisions in the logfile...
            List<RevisionHeader> tipRevisionCollection = new ArrayList<>();
            RevisionInformation revisionInformation = getLogFileImpl().getRevisionInformation();
            for (int i = 0; i < getLogFileImpl().getRevisionCount(); i++) {
                if (revisionInformation.getRevisionHeader(i).isTip() && !isRevisionTipRevisionOfTranslucentOrOpaqueBranch(revisionInformation.getRevisionHeader(i))) {
                    tipRevisionCollection.add(revisionInformation.getRevisionHeader(i));
                }
            }
            // Add a new tip revision for each valid non-translucent/opaque branch tip revision in the logfile
            for (int i = 0; i < tipRevisionCollection.size(); i++) {
                addTipRevision(userName, "move", checkInComment, checkInDate, appendedPathWorkfileName, shortWorkfileName, tipRevisionCollection.get(i));
            }
        } catch (QVCSException e) {
            String stackTrace = Utility.expandStackTraceToString(e);
            LOGGER.log(Level.SEVERE, stackTrace);
            retVal = false;
        }

        return retVal;
    }
}
