/*   Copyright 2004-2015 Jim Voris
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
import com.qumasoft.qvcslib.requestdata.ClientRequestSetCommentPrefixData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.LogFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set file comment prefix.
 * @author Jim Voris
 */
public class ClientRequestSetCommentPrefix implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestSetCommentPrefix.class);
    private final ClientRequestSetCommentPrefixData request;

    /**
     * Creates a new instance of ClientRequestSetAttributes.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestSetCommentPrefix(ClientRequestSetCommentPrefixData data) {
        request = data;
    }

    /**
     * Execute set comment prefix.
     *
     * @param userName the user name to associate with the operation.
     * @param response identify the client.
     * @return a response to the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            if (directoryManager != null) {
                ArchiveInfoInterface archiveInfo = directoryManager.getArchiveInfo(shortWorkfileName);
                if (archiveInfo != null) {
                    if (archiveInfo instanceof LogFile) {
                        LogFile logfile = (LogFile) archiveInfo;

                        // Save existing comment prefix so we can log the change.
                        String oldCommentPrefix = logfile.getCommentPrefix();

                        if (logfile.setCommentPrefix(userName, request.getCommentPrefix())) {
                            // Log the result.
                            String activity = "User: [" + userName + "] changed comment prefix for ["
                                    + Utility.formatFilenameForActivityJournal(projectName, viewName, appendedPath, shortWorkfileName)
                                    + "] from: [" + oldCommentPrefix
                                    + "] to: [" + request.getCommentPrefix() + "].";
                            LOGGER.info(activity);

                            // Send a response message so the client can treat this as a synchronous request.
                            ServerResponseMessage message = new ServerResponseMessage("Set comment prefix successful.", projectName, viewName, appendedPath,
                                    ServerResponseMessage.LO_PRIORITY);
                            message.setShortWorkfileName(shortWorkfileName);
                            returnObject = message;

                            // Add an entry to the server journal file.
                            ActivityJournalManager.getInstance().addJournalEntry(activity);
                        } else {
                            // Return a command error.
                            ServerResponseError error = new ServerResponseError("Failed to change comment prefix for " + shortWorkfileName, projectName, viewName, appendedPath);
                            returnObject = error;
                        }
                    } else {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Set comment prefix not allowed for non-trunk views.", projectName, viewName, appendedPath,
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(shortWorkfileName);
                        returnObject = message;
                    }
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive not found for " + shortWorkfileName, projectName, viewName, appendedPath);
                    returnObject = error;
                }

            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Directory manager not found for " + shortWorkfileName, projectName, viewName, appendedPath);
                returnObject = error;
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        return returnObject;
    }
}
