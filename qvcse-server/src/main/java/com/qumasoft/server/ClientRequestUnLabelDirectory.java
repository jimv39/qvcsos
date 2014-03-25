/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.UnLabelDirectoryCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnLabelDirectoryData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseSuccess;
import com.qumasoft.qvcslib.response.ServerResponseUnLabel;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request unlabel directory.
 * @author Jim Voris
 */
public class ClientRequestUnLabelDirectory implements ClientRequestInterface, DirectoryOperationInterface {
    private static final String FROM_BRACKET = "] from [";
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestUnLabelDirectoryData request;
    private final Map<String, String> directoryMap = new TreeMap<>();
    private String userName = null;
    private int successCounter = 0;
    private int operationAttemptCounter = 0;

    /**
     * Creates a new instance of ClientRequestUnLabelDirectory.
     *
     * @param data command line data, etc.
     */
    public ClientRequestUnLabelDirectory(ClientRequestUnLabelDirectoryData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String user, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        DirectoryOperationHelper directoryOperationHelper = new DirectoryOperationHelper(this);
        this.userName = user;
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();

        try {
            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                throw new QVCSException("You cannot remove a label from the cemetery!");
            }
            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                throw new QVCSException("You cannot remove a label from the branch archives directory!");
            }

            // We have to do this directory at least...
            directoryMap.put(appendedPath, appendedPath);

            if (request.getCommandArgs().getRecurseFlag()) {
                directoryOperationHelper.addChildDirectories(directoryMap, viewName, appendedPath, response);
            }
            successCounter = 0;
            operationAttemptCounter = 0;
            directoryOperationHelper.processDirectoryCollection(viewName, directoryMap, response);

            // And let the client know the total number of files that we unlabeled.
            ServerResponseSuccess message = new ServerResponseSuccess("UnLabeled: " + successCounter + " out of " + operationAttemptCounter + " files.");
            response.createServerResponse(message);
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), getProjectName(), viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
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
        ServerResponseUnLabel serverResponse;
        ServerResponseInterface returnObject;
        UnLabelRevisionCommandArgs unlabelCommandArgs = new UnLabelRevisionCommandArgs();
        UnLabelDirectoryCommandArgs directoryCommandArgs = request.getCommandArgs();
        try {
            LOGGER.log(Level.INFO, "appended path: [" + appendedPath + "]");
            unlabelCommandArgs.setLabelString(directoryCommandArgs.getLabelString());
            unlabelCommandArgs.setUserName(directoryCommandArgs.getUserName());
            if (logfile != null) {
                LOGGER.log(Level.INFO, "short workfile name: [" + logfile.getShortWorkfileName() + "]");
                unlabelCommandArgs.setShortWorkfileName(logfile.getShortWorkfileName());

                if (archiveDirManager instanceof ArchiveDirManagerReadWriteViewInterface) {
                    if ((logfile.unLabelRevision(unlabelCommandArgs)) && (logfile instanceof LogFileInterface)) {
                        LogFileInterface logFileInterface = (LogFileInterface) logfile;
                        serverResponse = new ServerResponseUnLabel();

                        serverResponse.setProjectName(getProjectName());
                        serverResponse.setViewName(request.getViewName());
                        serverResponse.setAppendedPath(appendedPath);
                        serverResponse.setLabelString(directoryCommandArgs.getLabelString());
                        serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());

                        serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator, logFileInterface.getIsObsolete(),
                                logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));

                        LOGGER.log(Level.INFO, "Removed label [" + unlabelCommandArgs.getLabelString() + FROM_BRACKET + appendedPath + File.separator
                                + unlabelCommandArgs.getShortWorkfileName() + "]");
                        returnObject = serverResponse;

                        // Send a message to indicate that we unlabeled the file.
                        ServerResponseMessage message = new ServerResponseMessage("Removed label [" + unlabelCommandArgs.getLabelString() + FROM_BRACKET + appendedPath
                                + File.separator + unlabelCommandArgs.getShortWorkfileName() + "]", getProjectName(), request.getViewName(), appendedPath,
                                ServerResponseMessage.MEDIUM_PRIORITY);
                        message.setShortWorkfileName(logfile.getShortWorkfileName());
                        response.createServerResponse(message);
                        successCounter++;

                        // Add an entry to the server journal file.
                        ActivityJournalManager.getInstance().addJournalEntry(buildJournalEntry(logfile, appendedPath, unlabelCommandArgs));
                    } else {
                        // Return a command error.
                        ServerResponseError error = new ServerResponseError("Failed to remove label [" + unlabelCommandArgs.getLabelString() + FROM_BRACKET
                                + logfile.getShortWorkfileName() + "]: "
                                + unlabelCommandArgs.getErrorMessage(), getProjectName(), request.getViewName(), request.getAppendedPath());
                        returnObject = error;
                    }
                } else {
                    // Explain the error.
                    ServerResponseError error = new ServerResponseError("UnLabel not allowed for read-only view.", getProjectName(), request.getViewName(),
                            request.getAppendedPath());
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Archive not found for [" + unlabelCommandArgs.getShortWorkfileName() + "]", getProjectName(),
                        request.getViewName(), request.getAppendedPath());
                returnObject = error;
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), getProjectName(), request.getViewName(), request.getAppendedPath(),
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(unlabelCommandArgs.getShortWorkfileName());
            returnObject = message;
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return returnObject;
    }

    private String buildJournalEntry(final ArchiveInfoInterface logfile, final String appendedPath, final UnLabelRevisionCommandArgs unlabelRevisionCommandArgs) {
        return "User: [" + userName + "] removed label [" + unlabelRevisionCommandArgs.getLabelString() + FROM_BRACKET
                + Utility.formatFilenameForActivityJournal(getProjectName(), request.getViewName(), appendedPath, logfile.getShortWorkfileName()) + "].";
    }

    @Override
    public String getProjectName() {
        return request.getProjectName();
    }
}
