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
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnDeleteData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ArchiveDirManagerForTranslucentBranchCemetery;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Undelete a file. (Restore it from the cemetery).
 * @author Jim Voris
 */
public class ClientRequestUnDelete implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestUnDelete.class);
    private final ClientRequestUnDeleteData request;

    /**
     * Creates a new instance of ClientRequestUnDelete.
     *
     * @param data command line data, etc.
     */
    public ClientRequestUnDelete(ClientRequestUnDeleteData data) {
        request = data;
    }

    /**
     * Execute the undelete command.
     *
     * @param userName the user name.
     * @param response identify the client.
     * @return the response we send back to the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            ArchiveInfoInterface archiveInfo = directoryManager.getArchiveInfo(shortWorkfileName);
            if ((archiveInfo != null) && (directoryManager instanceof ArchiveDirManager)) {
                if (directoryManager.unDeleteArchive(userName, shortWorkfileName, response)) {
                    // Log the result.
                    String activity = "User: [" + userName + "] restored: ["
                            + Utility.formatFilenameForActivityJournal(projectName, branchName, appendedPath, shortWorkfileName) + "] from cemetery.";
                    LOGGER.info(activity);

                    // Send a response message so the client can treat this as a synchronous request.
                    ServerResponseMessage message = new ServerResponseMessage("Undelete successful.", projectName, branchName, appendedPath,
                            ServerResponseMessage.LO_PRIORITY);
                    message.setShortWorkfileName(shortWorkfileName);
                    returnObject = message;

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(activity);
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Failed to undelete [" + shortWorkfileName + "]", projectName, branchName, appendedPath);
                    returnObject = error;
                }
            } else {
                if (archiveInfo == null) {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive not found for [" + shortWorkfileName + "]", projectName, branchName, appendedPath);
                    returnObject = error;
                } else {
                    if (directoryManager instanceof ArchiveDirManagerForTranslucentBranchCemetery) {
                        if (directoryManager.unDeleteArchive(userName, shortWorkfileName, response)) {
                            // Log the result.
                            String activity = "User: [" + userName + "] restored: ["
                                    + Utility.formatFilenameForActivityJournal(projectName, branchName, appendedPath, shortWorkfileName) + "] from cemetery.";
                            LOGGER.info(activity);

                            // Send a response message so the client can treat this as a synchronous request.
                            ServerResponseMessage message = new ServerResponseMessage("Undelete successful.", projectName, branchName, appendedPath,
                                    ServerResponseMessage.LO_PRIORITY);
                            message.setShortWorkfileName(shortWorkfileName);
                            returnObject = message;

                            // Add an entry to the server journal file.
                            ActivityJournalManager.getInstance().addJournalEntry(activity);
                        } else {
                            // Return a command error.
                            ServerResponseError error = new ServerResponseError("Failed to undelete [" + shortWorkfileName + "]", projectName, branchName, appendedPath);
                            returnObject = error;
                        }
                    } else {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("UnDelete archive not allowed for [" + branchName + "] branch.", projectName, branchName,
                                appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(shortWorkfileName);
                        returnObject = message;
                    }
                }
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        return returnObject;
    }
}
