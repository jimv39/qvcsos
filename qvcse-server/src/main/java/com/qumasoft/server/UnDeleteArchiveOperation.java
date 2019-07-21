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

import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;

/**
 * Undelete an archive... i.e. restore it from the cemetery.
 * @author Jim Voris
 */
public class UnDeleteArchiveOperation {

    private ArchiveDirManager cemeteryArchiveDirManager = null;
    private String userName = null;
    private String shortWorkfileName = null;
    private ServerResponseFactoryInterface response = null;
    private String nonCemeteryShortWorkfileName = null;
    private ArchiveDirManager targetArchiveDirManager = null;

    /**
     * Creates a new instance of UnDeleteArchiveOperation.
     *
     * @param cemeteryArchiveDirMgr the cemetery archive directory manager.
     * @param user the user name.
     * @param shortName the short workfile name.
     * @param client identify the client.
     */
    public UnDeleteArchiveOperation(ArchiveDirManager cemeteryArchiveDirMgr, String user, String shortName, ServerResponseFactoryInterface client) {
        cemeteryArchiveDirManager = cemeteryArchiveDirMgr;
        userName = user;
        shortWorkfileName = shortName;
        response = client;
    }

    /**
     * Perform the undelete operation. This will restore the archive from the cemetery to its original directory location, and rename it to its original name.
     * @return true if everything worked; false if there was a problem.
     * @throws QVCSException the move or rename could cause problems.
     * @throws IOException the move or rename could cause problems.
     */
    public boolean execute() throws QVCSException, IOException {
        deduceNonCemeteryShortWorkfileNameAndTargetArchiveDirManager();

        // First restore the archive from the cemetery
        if (cemeteryArchiveDirManager.moveArchive(userName, shortWorkfileName, targetArchiveDirManager, response)) {
            // Rename the moved archive back to its original name...
            return targetArchiveDirManager.renameArchive(userName, shortWorkfileName, nonCemeteryShortWorkfileName, response);
        } else {
            return false;
        }
    }

    private void deduceNonCemeteryShortWorkfileNameAndTargetArchiveDirManager() throws QVCSException {
        ArchiveInfoInterface archiveInfo = cemeteryArchiveDirManager.getArchiveInfo(shortWorkfileName);
        String originalFilename = Utility.deduceOriginalFilenameForUndeleteFromCemetery(archiveInfo);

        // Now figure out the appended path of the directory to which we'll restore
        // this 'deleted' file.
        String appendedPath = "";
        nonCemeteryShortWorkfileName = originalFilename;
        if (-1 != originalFilename.lastIndexOf(QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR)) {
            int nameSegmentIndex = originalFilename.lastIndexOf(QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR);
            appendedPath = originalFilename.substring(0, nameSegmentIndex);
            nonCemeteryShortWorkfileName = originalFilename.substring(1 + nameSegmentIndex);
        }
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(cemeteryArchiveDirManager.getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH, appendedPath);
        targetArchiveDirManager = (ArchiveDirManager) ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
    }
}
