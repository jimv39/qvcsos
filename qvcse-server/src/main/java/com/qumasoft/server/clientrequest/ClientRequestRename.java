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
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestRenameData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseRenameArchive;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ArchiveDirManagerForTranslucentBranch;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rename an archive file.
 *
 * @author Jim Voris
 */
public class ClientRequestRename implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestRename.class);
    private final ClientRequestRenameData request;

    /**
     * Creates a new instance of ClientRequestRename.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestRename(ClientRequestRenameData data) {
        request = data;
    }

    /**
     * Perform the rename operation.
     *
     * @param userName the user making the request.
     * @param response the response object that identifies the client connection.
     * @return an object we'll send back to the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String viewName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String originalShortWorkfileName = request.getOriginalShortWorkfileName();
        String newShortWorkfileName = request.getNewShortWorkfileName();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            LOGGER.info("project name: [" + projectName + "] view name: [" + viewName + "] appended path: [" + appendedPath + "]");
            ArchiveInfoInterface logfile = directoryManager.getArchiveInfo(originalShortWorkfileName);
            if ((logfile != null) && ((directoryManager instanceof ArchiveDirManager) || (directoryManager instanceof ArchiveDirManagerForTranslucentBranch))) {
                // Send a response to the user (note that a notification has also been sent earlier).
                if (directoryManager.renameArchive(userName, originalShortWorkfileName, newShortWorkfileName, response)) {
                    ServerResponseRenameArchive serverResponseRenameArchive = new ServerResponseRenameArchive();
                    serverResponseRenameArchive.setServerName(response.getServerName());
                    serverResponseRenameArchive.setProjectName(projectName);
                    serverResponseRenameArchive.setViewName(viewName);
                    serverResponseRenameArchive.setAppendedPath(appendedPath);
                    serverResponseRenameArchive.setOldShortWorkfileName(originalShortWorkfileName);
                    serverResponseRenameArchive.setNewShortWorkfileName(newShortWorkfileName);
                    ArchiveInfoInterface newArchiveInfo = directoryManager.getArchiveInfo(newShortWorkfileName);
                    serverResponseRenameArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(newArchiveInfo.getLogfileInfo(), File.separator,
                            newArchiveInfo.getDefaultRevisionDigest(), newShortWorkfileName, newArchiveInfo.getIsOverlap()));
                    returnObject = serverResponseRenameArchive;

                    // Add an entry to the server journal file.
                    String logMessage = buildJournalEntry(userName);

                    ActivityJournalManager.getInstance().addJournalEntry(logMessage);
                    LOGGER.info(logMessage);
                }
            } else {
                if (logfile == null) {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive not found for " + originalShortWorkfileName, projectName, viewName, appendedPath);
                    returnObject = error;
                } else {
                    // Explain the error.
                    ServerResponseMessage message = new ServerResponseMessage("Rename not allowed for non-Trunk view.", projectName, viewName, appendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(originalShortWorkfileName);
                    returnObject = message;
                }
            }
        } catch (IOException | QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to rename [" + originalShortWorkfileName + "] to [" + newShortWorkfileName
                    + "]. Exception string: " + e.getMessage(), projectName, viewName, appendedPath);
            returnObject = error;
        }
        return returnObject;
    }

    private String buildJournalEntry(final String userName) {
        return "User: [" + userName + "] renamed file ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getBranchName(), request.getAppendedPath(), request.getOriginalShortWorkfileName())
                + "] to ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getBranchName(), request.getAppendedPath(), request.getNewShortWorkfileName()) + "]";
    }
}
