//   Copyright 2004-2019 Jim Voris
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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;

/**
 * Undelete an archive for translucent branch.
 * @author Jim Voris
 */
public class UnDeleteArchiveForTranslucentBranchOperation {

    private final ArchiveDirManagerInterface cemeteryArchiveDirManager;
    private final String userName;
    private final String shortWorkfileName;
    private final ServerResponseFactoryInterface response;
    private String nonCemeteryShortWorkfileName;
    private ArchiveDirManagerForTranslucentBranch targetArchiveDirManager;

    /**
     * Creates a new instance of UnDeleteArchiveOperation.
     *
     * @param cemeteryArchiveDirMgr the cemetery archive directory manager.
     * @param user the user name
     * @param shortName the short workfile name in the cemetery.
     * @param resp identify the client.
     */
    public UnDeleteArchiveForTranslucentBranchOperation(ArchiveDirManagerInterface cemeteryArchiveDirMgr, String user, String shortName, ServerResponseFactoryInterface resp) {
        cemeteryArchiveDirManager = cemeteryArchiveDirMgr;
        userName = user;
        shortWorkfileName = shortName;
        response = resp;
    }

    /**
     * Move the file from the cemetery back to where it came from.
     *
     * @return true if successful.
     * @throws QVCSException if something goes wrong.
     * @throws IOException if there is an IO problem.
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
        String originalFilename = Utility.deduceOriginalFilenameForUndeleteFromTranslucentBranchCemetery(archiveInfo);

        // Now figure out the appended path of the directory to which we'll restore
        // this 'deleted' file.
        String appendedPath = "";
        nonCemeteryShortWorkfileName = originalFilename;
        if (-1 != originalFilename.lastIndexOf(QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR)) {
            int nameSegmentIndex = originalFilename.lastIndexOf(QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR);
            appendedPath = originalFilename.substring(0, nameSegmentIndex);
            nonCemeteryShortWorkfileName = originalFilename.substring(1 + nameSegmentIndex);
        }
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(cemeteryArchiveDirManager.getProjectName(), cemeteryArchiveDirManager.getViewName(), appendedPath);
        targetArchiveDirManager = (ArchiveDirManagerForTranslucentBranch) ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
    }
}
