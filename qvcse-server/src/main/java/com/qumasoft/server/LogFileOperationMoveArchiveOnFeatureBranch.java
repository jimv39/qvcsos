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
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create the revision needed to capture the move of a file on a feature branch.
 *
 * @author Jim Voris
 */
class LogFileOperationMoveArchiveOnFeatureBranch extends AbstractLogFileOperation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileOperationMoveArchiveOnFeatureBranch.class);
    private final String userName;
    private final String checkInComment;
    private final String shortWorkfileName;
    private final String appendedPathWorkfileName;
    private final String branchLabel;
    private final Date checkInDate;
    private final RevisionHeader revisionHeader;
    private final LogFile logFile;

    /**
     * Creates a new instance of LogFileOperationMoveArchiveOnFeatureBranch.
     * @param archiveInfoForFeatureBranch the archive info from the feature branch.
     * @param user the user name.
     * @param checkInCmmnt the check in comment to use.
     * @param appendedPath the appended path.
     * @param shortName the short workfile name.
     * @param date the date to use for this operation.
     * @param bLabel the branch label.
     */
    LogFileOperationMoveArchiveOnFeatureBranch(ArchiveInfoForFeatureBranch archiveInfoForFeatureBranch, String user, String checkInCmmnt, String appendedPath,
            String shortName, final Date date, final String bLabel) throws QVCSException {
        super(null, archiveInfoForFeatureBranch.getCurrentLogFile().getLogFileImpl());
        userName = user;
        shortWorkfileName = shortName;
        checkInComment = QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX + checkInCmmnt;
        checkInDate = date;
        appendedPathWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + shortName;
        branchLabel = bLabel;
        revisionHeader = archiveInfoForFeatureBranch.getRevisionInformation().getRevisionHeader(0);
        logFile = archiveInfoForFeatureBranch.getCurrentLogFile();
    }

    @Override
    public boolean execute() throws QVCSException {
        boolean retVal = false;
        if (logFile != null) {
            try {
                addTipRevisionForFeatureBranch(userName, "move", checkInComment, checkInDate, appendedPathWorkfileName, shortWorkfileName, revisionHeader, branchLabel);
                retVal = true;
            } catch (QVCSException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        } else {
            throw new QVCSException("LogFile not initialized for operation!");
        }
        return retVal;
    }
}
