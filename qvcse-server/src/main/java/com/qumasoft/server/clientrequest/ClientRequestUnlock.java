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
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyViewInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnlockData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetRevision;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseUnlock;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ServerUtility;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request unlock.
 * @author Jim Voris
 */
public class ClientRequestUnlock implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestUnlock.class);
    private final ClientRequestUnlockData request;

    /**
     * Creates a new instance of ClientRequestLock.
     *
     * @param data command line data, etc.
     */
    public ClientRequestUnlock(ClientRequestUnlockData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseUnlock serverResponse;
        ServerResponseInterface returnObject = null;
        UnlockRevisionCommandArgs commandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            ArchiveInfoInterface logfile = directoryManager.getArchiveInfo(commandArgs.getShortWorkfileName());
            if ((logfile != null) && (directoryManager instanceof ArchiveDirManagerReadWriteViewInterface)) {
                if (logfile.unlockRevision(commandArgs)) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseUnlock();

                    LogFileInterface logFileInterface = (LogFileInterface) logfile;
                    serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator, logFileInterface.getIsObsolete(),
                            logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                    serverResponse.setProjectName(projectName);
                    serverResponse.setViewName(viewName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setRevisionString(commandArgs.getRevisionString());
                    serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                    serverResponse.setClientWorkfileName(commandArgs.getFullWorkfileName());
                    serverResponse.setUndoCheckoutBehavior(commandArgs.getUndoCheckoutBehavior());
                    returnObject = serverResponse;

                    if (commandArgs.getUndoCheckoutBehavior() != Utility.UndoCheckoutBehavior.JUST_UNLOCK_ARCHIVE) {
                        sendRevisionToClient(commandArgs, response, logfile);
                    }

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(buildJournalEntry(userName, logfile));
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Failed to unlock revision " + commandArgs.getRevisionString() + " for "
                            + logfile.getShortWorkfileName(), projectName, viewName, appendedPath);
                    returnObject = error;
                }
            } else {
                if (logfile == null) {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive not found for " + commandArgs.getShortWorkfileName(), projectName, viewName, appendedPath);
                    returnObject = error;
                } else {
                    if (directoryManager instanceof ArchiveDirManagerReadOnlyViewInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("UnLock not allowed for read-only view.", projectName, viewName, appendedPath,
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
        UnlockRevisionCommandArgs commandArgs = request.getCommandArgs();
        return "User: [" + userName + "] unlocked revision [" + commandArgs.getRevisionString() + "] of ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getViewName(), request.getAppendedPath(), logfile.getShortWorkfileName()) + "].";
    }

    private void sendRevisionToClient(UnlockRevisionCommandArgs commandArgs, ServerResponseFactoryInterface response, ArchiveInfoInterface logfile) {
        if (commandArgs.getUndoCheckoutBehavior() != Utility.UndoCheckoutBehavior.DELETE_WORKFILE) {
            ServerResponseGetRevision serverResponse;
            ServerResponseInterface returnObject = null;

            // Set the revision string to the one that was locked.
            String revisionString = commandArgs.getRevisionString();

            if (commandArgs.getUndoCheckoutBehavior() == Utility.UndoCheckoutBehavior.RESTORE_DEFAULT_REVISION) {
                revisionString = logfile.getDefaultRevisionString();
            }

            try {
                byte[] workfileBuffer = logfile.getRevisionAsByteArray(revisionString);
                if (workfileBuffer != null) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseGetRevision();
                    serverResponse.setBuffer(workfileBuffer);

                    LogFileInterface logFileInterface = (LogFileInterface) logfile;
                    serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator, logFileInterface.getIsObsolete(),
                            logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                    serverResponse.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                    serverResponse.setAppendedPath(request.getAppendedPath());
                    serverResponse.setProjectName(request.getProjectName());
                    serverResponse.setViewName(request.getViewName());
                    serverResponse.setRevisionString(revisionString);

                    // Figure out the timestamp that we send back.
                    ServerUtility.setTimestampData(logfile, serverResponse, Utility.TimestampBehavior.SET_TIMESTAMP_TO_NOW);

                    // Send back the logfile info if it's needed for
                    // keyword expansion.
                    if (logfile.getAttributes().getIsExpandKeywords()) {
                        serverResponse.setLogfileInfo(logfile.getLogfileInfo());
                    }
                    returnObject = serverResponse;
                }
            } catch (Exception e) {
                LOGGER.warn(e.getLocalizedMessage(), e);

                ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), request.getProjectName(), request.getViewName(), request.getAppendedPath(),
                        ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                returnObject = message;
            }
            response.createServerResponse(returnObject);
        }
    }
}
