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
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteBranchInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.LabelDirectoryCommandArgs;
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestLabelDirectoryData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseLabel;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseSuccess;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.DirectoryOperationHelper;
import com.qumasoft.server.DirectoryOperationInterface;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Label directory.
 * @author Jim Voris
 */
public class ClientRequestLabelDirectory implements ClientRequestInterface, DirectoryOperationInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestLabelDirectory.class);
    private final ClientRequestLabelDirectoryData request;
    private final Map<String, String> directoryMap = new TreeMap<>();
    private String userName = null;
    private int successCounter = 0;
    private int operationAttemptCounter = 0;

    /**
     * Creates a new instance of ClientRequestLabelDirectory.
     *
     * @param data the request data.
     */
    public ClientRequestLabelDirectory(ClientRequestLabelDirectoryData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String user, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        DirectoryOperationHelper directoryOperationHelper = new DirectoryOperationHelper(this);
        this.userName = user;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();

        try {
            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                throw new QVCSException("You cannot apply a label to the cemetery!");
            }
            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                throw new QVCSException("You cannot apply a label to the branch archives directory!");
            }

            if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                // We have to do this directory at least...
                directoryMap.put(appendedPath, appendedPath);

                if (request.getCommandArgs().getRecurseFlag()) {
                    directoryOperationHelper.addChildDirectories(directoryMap, branchName, appendedPath, response);
                }
                successCounter = 0;
                operationAttemptCounter = 0;
                directoryOperationHelper.processDirectoryCollection(branchName, directoryMap, response);

                // Let the client know the total number of files that we labeled.
                ServerResponseSuccess message = new ServerResponseSuccess("Labeled: " + successCounter + " out of " + operationAttemptCounter + " files.");
                response.createServerResponse(message);
            } else {
                // TODO.
                ServerResponseMessage message = new ServerResponseMessage("Applying a label at the directory level is not supported for non-Trunk branches.", projectName,
                        branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName("");
                returnObject = message;
            }

        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnObject;
    }

    @Override
    public ServerResponseInterface processFile(ArchiveDirManagerInterface archiveDirManager, ArchiveInfoInterface logfile, String appendedPath,
            ServerResponseFactoryInterface response) {
        operationAttemptCounter++;
        ServerResponseLabel serverResponse;
        ServerResponseInterface returnObject = null;
        LabelRevisionCommandArgs labelCommandArgs = new LabelRevisionCommandArgs();
        LabelDirectoryCommandArgs directoryCommandArgs = request.getCommandArgs();
        try {
            LOGGER.info("appended path: [" + appendedPath + "]");
            if (logfile != null) {
                LOGGER.info("short workfile name: [" + logfile.getShortWorkfileName() + "]");

                labelCommandArgs.setDuplicateFlag(directoryCommandArgs.getDuplicateFlag());
                labelCommandArgs.setDuplicateLabelString(directoryCommandArgs.getExistingLabelString());
                labelCommandArgs.setFloatingFlag(directoryCommandArgs.getFloatingFlag());
                labelCommandArgs.setLabelString(directoryCommandArgs.getNewLabelString());
                labelCommandArgs.setReuseLabelFlag(directoryCommandArgs.getReuseLabelFlag());
                if (!directoryCommandArgs.getDuplicateFlag()) {
                    labelCommandArgs.setRevisionFlag(true);
                    labelCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                } else {
                    labelCommandArgs.setRevisionFlag(false);
                }
                labelCommandArgs.setShortWorkfileName(logfile.getShortWorkfileName());
                labelCommandArgs.setUserName(directoryCommandArgs.getUserName());

                if (archiveDirManager instanceof ArchiveDirManagerReadWriteBranchInterface) {
                    if (logfile.labelRevision(labelCommandArgs)) {
                        if ((labelCommandArgs.getRevisionString().length() > 0) && (logfile instanceof LogFileInterface)) {
                            // If the revision string is not empty, we actually applied a
                            // label. Set up the response object to contain the information
                            // the client needs.
                            LogFileInterface logFileInterface = (LogFileInterface) logfile;
                            serverResponse = new ServerResponseLabel();

                            serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator,
                                    logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                            serverResponse.setProjectName(request.getProjectName());
                            serverResponse.setBranchName(request.getBranchName());
                            serverResponse.setAppendedPath(appendedPath);
                            serverResponse.setRevisionString(labelCommandArgs.getRevisionString());
                            serverResponse.setLabelString(labelCommandArgs.getLabelString());
                            serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                            LOGGER.info("Label [" + labelCommandArgs.getShortWorkfileName() + "] revision: [" + labelCommandArgs.getRevisionString() + "] with label: ["
                                    + labelCommandArgs.getLabelString() + "]");
                            returnObject = serverResponse;

                            // Send a message to indicate that we labeled the file.
                            ServerResponseMessage message = new ServerResponseMessage("Labeled revision: [" + labelCommandArgs.getRevisionString() + "] of [" + appendedPath
                                    + File.separator
                                    + logfile.getShortWorkfileName() + "] with label [" + labelCommandArgs.getLabelString() + "].", request.getProjectName(),
                                    request.getBranchName(), appendedPath,
                                    ServerResponseMessage.MEDIUM_PRIORITY);
                            message.setShortWorkfileName(logfile.getShortWorkfileName());
                            response.createServerResponse(message);
                            successCounter++;

                            // Add an entry to the server journal file.
                            ActivityJournalManager.getInstance().addJournalEntry(buildJournalEntry(logfile, appendedPath, labelCommandArgs));
                        }
                    } else {
                        // Return a command error.
                        ServerResponseError error = new ServerResponseError("Failed to label revision [" + labelCommandArgs.getRevisionString() + "] for ["
                                + logfile.getShortWorkfileName() + "] :"
                                + labelCommandArgs.getErrorMessage(), request.getProjectName(), request.getBranchName(), appendedPath);
                        returnObject = error;
                    }
                } else {
                    // Explain the error.
                    ServerResponseError error = new ServerResponseError("Label not allowed for read-only branch.", request.getProjectName(), request.getBranchName(), appendedPath);
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Archive not found for [" + labelCommandArgs.getShortWorkfileName() + "]", getProjectName(),
                        request.getBranchName(), appendedPath);
                returnObject = error;
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), getProjectName(), request.getBranchName(), appendedPath,
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(labelCommandArgs.getShortWorkfileName());
            returnObject = message;
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnObject;
    }

    private String buildJournalEntry(final ArchiveInfoInterface logfile, final String appendedPath, final LabelRevisionCommandArgs labelRevisionCommandArgs) {
        return "User: [" + userName + "] labeled revision [" + labelRevisionCommandArgs.getRevisionString() + "] of ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getBranchName(), appendedPath, logfile.getShortWorkfileName()) + "] with label: ["
                + labelRevisionCommandArgs.getLabelString() + "].";
    }

    @Override
    public String getProjectName() {
        return request.getProjectName();
    }
}
