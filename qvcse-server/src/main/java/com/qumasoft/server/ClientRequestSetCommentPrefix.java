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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.ClientRequestSetCommentPrefixData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseMessage;
import com.qumasoft.qvcslib.Utility;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Set file comment prefix.
 * @author Jim Voris
 */
public class ClientRequestSetCommentPrefix implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
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
                            LOGGER.log(Level.INFO, activity);

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
