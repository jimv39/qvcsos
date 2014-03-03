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
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.ClientRequestLabelDirectoryData;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.LogFileOperationLabelDirectoryCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseLabel;
import com.qumasoft.qvcslib.ServerResponseMessage;
import com.qumasoft.qvcslib.ServerResponseSuccess;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Label directory.
 * @author Jim Voris
 */
public class ClientRequestLabelDirectory implements ClientRequestInterface, DirectoryOperationInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
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
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();

        try {
            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                throw new QVCSException("You cannot apply a label to the cemetery!");
            }
            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                throw new QVCSException("You cannot apply a label to the branch archives directory!");
            }

            if (0 == viewName.compareTo(QVCSConstants.QVCS_TRUNK_VIEW)) {
                // We have to do this directory at least...
                directoryMap.put(appendedPath, appendedPath);

                if (request.getCommandArgs().getRecurseFlag()) {
                    directoryOperationHelper.addChildDirectories(directoryMap, viewName, appendedPath, response);
                }
                successCounter = 0;
                operationAttemptCounter = 0;
                directoryOperationHelper.processDirectoryCollection(viewName, directoryMap, response);

                // Let the client know the total number of files that we labeled.
                ServerResponseSuccess message = new ServerResponseSuccess("Labeled: " + successCounter + " out of " + operationAttemptCounter + " files.");
                response.createServerResponse(message);
            } else {
                // TODO.
                ServerResponseMessage message = new ServerResponseMessage("Applying a label at the directory level is not supported for non-Trunk views.", projectName, viewName,
                        appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName("");
                returnObject = message;
            }

        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return returnObject;
    }

    @Override
    public ServerResponseInterface processFile(ArchiveDirManagerInterface archiveDirManager, ArchiveInfoInterface logfile, String appendedPath,
            ServerResponseFactoryInterface response) {
        operationAttemptCounter++;
        ServerResponseLabel serverResponse;
        ServerResponseInterface returnObject = null;
        LogFileOperationLabelRevisionCommandArgs labelCommandArgs = new LogFileOperationLabelRevisionCommandArgs();
        LogFileOperationLabelDirectoryCommandArgs directoryCommandArgs = request.getCommandArgs();
        try {
            LOGGER.log(Level.INFO, "appended path: [" + appendedPath + "]");
            if (logfile != null) {
                LOGGER.log(Level.INFO, "short workfile name: [" + logfile.getShortWorkfileName() + "]");

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

                if (archiveDirManager instanceof ArchiveDirManagerReadWriteViewInterface) {
                    if (logfile.labelRevision(labelCommandArgs)) {
                        if ((labelCommandArgs.getRevisionString().length() > 0) && (logfile instanceof LogFileInterface)) {
                            // If the revision string is not empty, we actually applied a
                            // label. Set up the response object to contain the information
                            // the client needs.
                            LogFileInterface logFileInterface = (LogFileInterface) logfile;
                            serverResponse = new ServerResponseLabel();

                            serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator, logFileInterface.getIsObsolete(),
                                    logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                            serverResponse.setProjectName(request.getProjectName());
                            serverResponse.setViewName(request.getViewName());
                            serverResponse.setAppendedPath(appendedPath);
                            serverResponse.setRevisionString(labelCommandArgs.getRevisionString());
                            serverResponse.setLabelString(labelCommandArgs.getLabelString());
                            serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                            LOGGER.log(Level.INFO, "Label [" + labelCommandArgs.getShortWorkfileName() + "] revision: [" + labelCommandArgs.getRevisionString() + "] with label: ["
                                    + labelCommandArgs.getLabelString() + "]");
                            returnObject = serverResponse;

                            // Send a message to indicate that we labeled the file.
                            ServerResponseMessage message = new ServerResponseMessage("Labeled revision: [" + labelCommandArgs.getRevisionString() + "] of [" + appendedPath
                                    + File.separator
                                    + logfile.getShortWorkfileName() + "] with label [" + labelCommandArgs.getLabelString() + "].", request.getProjectName(),
                                    request.getViewName(), appendedPath,
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
                                + labelCommandArgs.getErrorMessage(), request.getProjectName(), request.getViewName(), appendedPath);
                        returnObject = error;
                    }
                } else {
                    // Explain the error.
                    ServerResponseError error = new ServerResponseError("Label not allowed for read-only view.", request.getProjectName(), request.getViewName(), appendedPath);
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Archive not found for [" + labelCommandArgs.getShortWorkfileName() + "]", getProjectName(),
                        request.getViewName(), appendedPath);
                returnObject = error;
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), getProjectName(), request.getViewName(), appendedPath,
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(labelCommandArgs.getShortWorkfileName());
            returnObject = message;
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return returnObject;
    }

    private String buildJournalEntry(final ArchiveInfoInterface logfile, final String appendedPath, final LogFileOperationLabelRevisionCommandArgs labelRevisionCommandArgs) {
        return "User: [" + userName + "] labeled revision [" + labelRevisionCommandArgs.getRevisionString() + "] of ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getViewName(), appendedPath, logfile.getShortWorkfileName()) + "] with label: ["
                + labelRevisionCommandArgs.getLabelString() + "].";
    }

    @Override
    public String getProjectName() {
        return request.getProjectName();
    }
}
