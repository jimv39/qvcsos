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
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyBranchInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteBranchInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetRevisionDescriptionData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.LogFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set a revision description.
 * @author Jim Voris
 */
public class ClientRequestSetRevisionDescription implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestSetRevisionDescription.class);
    private final ClientRequestSetRevisionDescriptionData request;

    /**
     * Creates a new instance of ClientRequestSetRevisionDescription.
     *
     * @param data command line data, etc.
     */
    public ClientRequestSetRevisionDescription(ClientRequestSetRevisionDescriptionData data) {
        request = data;
    }

    /**
     * Execute set revision description.
     *
     * @param userName the user name to associate with the operation.
     * @param response identify the client.
     * @return a response to the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        SetRevisionDescriptionCommandArgs commandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            ArchiveInfoInterface archiveInfo = directoryManager.getArchiveInfo(commandArgs.getShortWorkfileName());
            if ((archiveInfo != null) && (directoryManager instanceof ArchiveDirManagerReadWriteBranchInterface)) {
                if (archiveInfo instanceof LogFile) {
                    LogFile logfile = (LogFile) archiveInfo;

                    // Save existing attributes so we can log the change.
                    String oldRevisionDescription = logfile.getRevisionDescription(commandArgs.getRevisionString());

                    if (logfile.setRevisionDescription(commandArgs)) {
                        // Log the result.
                        String activity = "User: [" + commandArgs.getUserName() + "] changed revision description for revision [" + commandArgs.getRevisionString() + "] of ["
                                + Utility.formatFilenameForActivityJournal(projectName, branchName, appendedPath, commandArgs.getShortWorkfileName())
                                + "] from: [" + oldRevisionDescription
                                + "] to: [" + commandArgs.getRevisionDescription() + "].";
                        LOGGER.info(activity);

                        // Send a response message so the client can treat this as a synchronous request.
                        ServerResponseMessage message = new ServerResponseMessage("Set revision description successful.", projectName, branchName, appendedPath,
                                ServerResponseMessage.LO_PRIORITY);
                        message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                        returnObject = message;

                        // Add an entry to the server journal file.
                        ActivityJournalManager.getInstance().addJournalEntry(activity);
                    } else {
                        // Return a command error.
                        ServerResponseError error = new ServerResponseError("Failed to change revision description for " + commandArgs.getShortWorkfileName(), projectName,
                                branchName, appendedPath);
                        returnObject = error;
                    }
                } else {
                    // Explain the error.
                    ServerResponseMessage message = new ServerResponseMessage("Set revision description not allowed for non-trunk branches.", projectName, branchName, appendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    returnObject = message;
                }
            } else {
                if (archiveInfo == null) {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive not found for " + commandArgs.getShortWorkfileName(), projectName, branchName, appendedPath);
                    returnObject = error;
                } else {
                    if (directoryManager instanceof ArchiveDirManagerReadOnlyBranchInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Set revision description not allowed for read-only branch.", projectName, branchName,
                                appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                        returnObject = message;
                    }
                }
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            returnObject = message;
        }
        return returnObject;
    }
}
