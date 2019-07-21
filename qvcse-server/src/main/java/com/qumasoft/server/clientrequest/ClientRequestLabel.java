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
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestLabelData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseLabel;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteBranchInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyBranchInterface;

/**
 * Label a file.
 * @author Jim Voris
 */
public class ClientRequestLabel implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestLabel.class);
    private final ClientRequestLabelData request;

    /**
     * Creates a new instance of ClientRequestLabel.
     *
     * @param data client request label data.
     */
    public ClientRequestLabel(ClientRequestLabelData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseLabel serverResponse;
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String viewName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        LabelRevisionCommandArgs commandArgs = request.getCommandArgs();
        try {
            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                throw new QVCSException("You cannot apply a label to a file in the cemetery!");
            }
            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                throw new QVCSException("You cannot apply a label to a file in the branch archives directory!");
            }
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            ArchiveInfoInterface logfile = directoryManager.getArchiveInfo(commandArgs.getShortWorkfileName());
            if ((logfile != null) && (directoryManager instanceof ArchiveDirManagerReadWriteBranchInterface)) {
                if (logfile.labelRevision(commandArgs)) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseLabel();

                    LogFileInterface logFileInterface = (LogFileInterface) logfile;
                    serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator,
                            logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                    serverResponse.setProjectName(projectName);
                    serverResponse.setViewName(viewName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setRevisionString(commandArgs.getRevisionString());
                    serverResponse.setLabelString(commandArgs.getLabelString());
                    serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                    LOGGER.info("Label [" + commandArgs.getShortWorkfileName() + "] revision: [" + commandArgs.getRevisionString() + "] with label: ["
                            + commandArgs.getLabelString() + "]");
                    returnObject = serverResponse;

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(buildJournalEntry(userName, logfile));
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Failed to label revision [" + commandArgs.getRevisionString() + "] for ["
                            + logfile.getShortWorkfileName() + "] : [" + commandArgs.getErrorMessage() + "]", projectName, viewName, appendedPath);
                    returnObject = error;
                }
            } else {
                if (logfile == null) {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive not found for [" + commandArgs.getShortWorkfileName() + "]", projectName, viewName, appendedPath);
                    returnObject = error;
                } else {
                    if (directoryManager instanceof ArchiveDirManagerReadOnlyBranchInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Label not allowed for read-only view.", projectName, viewName, appendedPath,
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                        returnObject = message;
                    }
                }
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            returnObject = message;
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnObject;
    }

    private String buildJournalEntry(final String userName, final ArchiveInfoInterface logfile) {
        LabelRevisionCommandArgs commandArgs = request.getCommandArgs();
        return "User: [" + userName + "] labeled revision [" + commandArgs.getRevisionString() + "] of ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getBranchName(), request.getAppendedPath(), logfile.getShortWorkfileName())
                + "] with label: [" + commandArgs.getLabelString() + "].";
    }
}
