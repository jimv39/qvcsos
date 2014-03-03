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
import com.qumasoft.qvcslib.Utility;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create the revision needed to capture the delete of a file on a translucent branch.
 *
 * @author Jim Voris
 */
class LogFileOperationDeleteArchiveOnTranslucentBranch extends AbstractLogFileOperation {

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final String userName;
    private final String checkInComment;
    private final String shortWorkfileName;
    private final String appendedPathWorkfileName;
    private final String branchLabel;
    private final Date checkInDate;
    private final RevisionHeader revisionHeader;
    private final LogFile logFile;

    /**
     * Creates a new instance of LogFileOperationDeleteArchiveOnTranslucentBranch.
     *
     * @param archiveInfoForTranslucentBranch the archive info for the file on the translucent branch.
     * @param user the user name.
     * @param checkInCmmnt the checkin comment.
     * @param appendedPath the appended path.
     * @param shortName the short workfile name.
     * @param date the date to apply to this operation.
     * @param bLabel the branch label.
     */
    public LogFileOperationDeleteArchiveOnTranslucentBranch(ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch, String user, String checkInCmmnt, String appendedPath,
            String shortName, final Date date, final String bLabel) throws QVCSException {
        super(null, archiveInfoForTranslucentBranch.getCurrentLogFile().getLogFileImpl());
        userName = user;
        shortWorkfileName = shortName;
        checkInComment = QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX + checkInCmmnt;
        checkInDate = date;
        appendedPathWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortName;
        branchLabel = bLabel;
        revisionHeader = archiveInfoForTranslucentBranch.getRevisionInformation().getRevisionHeader(0);
        logFile = archiveInfoForTranslucentBranch.getCurrentLogFile();
    }

    @Override
    public boolean execute() throws QVCSException {
        boolean retVal = false;
        if (logFile != null) {
            try {
                addTipRevisionForTranslucentBranch(userName, "delete", checkInComment, checkInDate, appendedPathWorkfileName, shortWorkfileName, revisionHeader, branchLabel);
                retVal = true;
            } catch (QVCSException e) {
                LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        } else {
            throw new QVCSException("LogFile not initialized for operation!");
        }
        return retVal;
    }
}
