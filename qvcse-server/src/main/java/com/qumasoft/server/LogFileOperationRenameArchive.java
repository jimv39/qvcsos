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
import com.qumasoft.qvcslib.RevisionInformation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Rename an archive. Note that this only updates the archive file to add a new revision to serve as a record of the rename. It does not actually rename the archive file.
 * That is done elsewhere.
 * @author Jim Voris
 */
class LogFileOperationRenameArchive extends AbstractLogFileOperation {
    private final String userName;
    private final String checkInComment;
    private final String oldShortWorkfileName;
    private final String appendedPathWorkfileName;
    private final Date checkInDate;

    /**
     * Creates a new instance of LogFileOperationRenameArchive. Note that this only updates the archive file to add a new revision to serve as a record of the rename. It does not
     * actually rename the archive file. That is done elsewhere.
     * @param logFile the file to rename.
     * @param user the user name.
     * @param checkInCmmnt the checkin comment.
     * @param appendedPath the appended path.
     * @param oldShortName the original short workfile name.
     * @param date the date for this operation.
     */
    LogFileOperationRenameArchive(LogFileImpl logFile, String user, String checkInCmmnt, String appendedPath, String oldShortName, final Date date) {
        super(null, logFile);
        userName = user;
        oldShortWorkfileName = oldShortName;
        checkInComment = QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX + checkInCmmnt;
        checkInDate = date;
        appendedPathWorkfileName = appendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + oldShortName;
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
                if (revisionInformation.getRevisionHeader(i).isTip() && !isRevisionTipRevisionOfFeatureOrOpaqueBranch(revisionInformation.getRevisionHeader(i))) {
                    tipRevisionCollection.add(revisionInformation.getRevisionHeader(i));
                }
            }

            // Add a new tip revision for each valid non-feature/opaque branch tip revision in the logfile
            for (int i = 0; i < tipRevisionCollection.size(); i++) {
                addTipRevision(userName, "rename", checkInComment, checkInDate, appendedPathWorkfileName, oldShortWorkfileName, tipRevisionCollection.get(i));
            }
        } catch (QVCSException e) {
            retVal = false;
        }

        return retVal;
    }
}
