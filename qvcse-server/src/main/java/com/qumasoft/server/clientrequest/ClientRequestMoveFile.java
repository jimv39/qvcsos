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
import com.qumasoft.qvcslib.requestdata.ClientRequestMoveFileData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseMoveFile;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ArchiveDirManagerForTranslucentBranch;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Move an archive file.
 *
 * @author Jim Voris
 */
public class ClientRequestMoveFile implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestMoveFile.class);
    private final ClientRequestMoveFileData request;

    /**
     * Creates a new instance of ClientRequestRename.
     *
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestMoveFile(ClientRequestMoveFileData data) {
        request = data;
    }

    /**
     * Perform the move operation.
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
        String shortWorkfileName = request.getShortWorkfileName();
        String originalAppendedPath = request.getOriginalAppendedPath();
        try {
            DirectoryCoordinate originCoordinate = new DirectoryCoordinate(projectName, viewName, originalAppendedPath);
            ArchiveDirManagerInterface originDirectoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    originCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            DirectoryCoordinate destinationCoordinate = new DirectoryCoordinate(projectName, viewName, request.getNewAppendedPath());
            ArchiveDirManagerInterface destinationDirectoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    destinationCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            ArchiveInfoInterface logfile = originDirectoryManager.getArchiveInfo(shortWorkfileName);
            if ((logfile != null) && ((originDirectoryManager instanceof ArchiveDirManager) || (originDirectoryManager instanceof ArchiveDirManagerForTranslucentBranch))) {
                // Send a response to the user (note that a notification has also been sent earlier).
                if (originDirectoryManager.moveArchive(userName, shortWorkfileName, destinationDirectoryManager, response)) {
                    ServerResponseMoveFile serverResponseMoveFile = new ServerResponseMoveFile();
                    serverResponseMoveFile.setServerName(response.getServerName());
                    serverResponseMoveFile.setProjectName(originDirectoryManager.getProjectName());
                    serverResponseMoveFile.setViewName(originDirectoryManager.getBranchName());
                    serverResponseMoveFile.setProjectProperties(originDirectoryManager.getProjectProperties().getProjectProperties());
                    serverResponseMoveFile.setOriginAppendedPath(originDirectoryManager.getAppendedPath());
                    serverResponseMoveFile.setDestinationAppendedPath(destinationDirectoryManager.getAppendedPath());
                    serverResponseMoveFile.setShortWorkfileName(shortWorkfileName);
                    ArchiveInfoInterface newArchiveInfo = destinationDirectoryManager.getArchiveInfo(shortWorkfileName);
                    serverResponseMoveFile.setSkinnyLogfileInfo(new SkinnyLogfileInfo(newArchiveInfo.getLogfileInfo(), File.separator,
                            newArchiveInfo.getDefaultRevisionDigest(), shortWorkfileName, newArchiveInfo.getIsOverlap()));
                    returnObject = serverResponseMoveFile;

                    // Add an entry to the server journal file.
                    String logMessage = buildJournalEntry(userName);

                    ActivityJournalManager.getInstance().addJournalEntry(logMessage);
                    LOGGER.info(logMessage);
                }
            } else {
                if (logfile == null) {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive not found for " + shortWorkfileName, projectName, viewName, originalAppendedPath);
                    returnObject = error;
                } else {
                    // Explain the error.
                    ServerResponseMessage message = new ServerResponseMessage("Move not allowed for non-Trunk views.", projectName, viewName, originalAppendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(shortWorkfileName);
                    returnObject = message;
                }
            }
        } catch (IOException | QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to move " + shortWorkfileName + " from " + originalAppendedPath + " to "
                    + request.getNewAppendedPath() + ". Exception string: " + e.getMessage(), projectName, viewName, originalAppendedPath);
            returnObject = error;
        }
        return returnObject;
    }

    private String buildJournalEntry(final String userName) {
        return "User: [" + userName + "] moved file ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getBranchName(), request.getOriginalAppendedPath(),
                        request.getShortWorkfileName()) + "] to ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getBranchName(), request.getNewAppendedPath(), request.getShortWorkfileName()) + "].";
    }
}
