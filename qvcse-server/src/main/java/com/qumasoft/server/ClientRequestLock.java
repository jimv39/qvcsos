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
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyViewInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.ClientRequestLockData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.LogFileOperationLockRevisionCommandArgs;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseLock;
import com.qumasoft.qvcslib.ServerResponseMessage;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lock a file revision.
 * @author Jim Voris
 */
public class ClientRequestLock implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestLockData request;

    /**
     * Creates a new instance of ClientRequestLock.
     *
     * @param data instance of super class that contains command line arguments, etc.
     */
    public ClientRequestLock(ClientRequestLockData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseLock serverResponse;
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        LogFileOperationLockRevisionCommandArgs commandArgs = request.getCommandArgs();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            LOGGER.log(Level.FINEST, "appended path: " + appendedPath);
            ArchiveInfoInterface logfile = directoryManager.getArchiveInfo(commandArgs.getShortWorkfileName());
            LOGGER.log(Level.FINEST, "full workfile name: " + commandArgs.getFullWorkfileName());
            LOGGER.log(Level.FINEST, "short workfile name: " + commandArgs.getShortWorkfileName());
            if ((logfile != null) && (directoryManager instanceof ArchiveDirManagerReadWriteViewInterface)) {
                if (logfile.lockRevision(commandArgs)) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseLock();

                    LogFileInterface logFileInterface = (LogFileInterface) logfile;
                    serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator, logFileInterface.getIsObsolete(),
                            logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                    serverResponse.setProjectName(projectName);
                    serverResponse.setViewName(viewName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setRevisionString(commandArgs.getRevisionString());
                    serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                    serverResponse.setClientWorkfileName(commandArgs.getOutputFileName());
                    LOGGER.log(Level.INFO, "Locked " + commandArgs.getShortWorkfileName() + " revision: " + commandArgs.getRevisionString());
                    returnObject = serverResponse;

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(buildJournalEntry(userName, logfile));
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Failed to lock revision [" + commandArgs.getRevisionString() + "] for ["
                            + logfile.getShortWorkfileName() + "]",
                            projectName, viewName, appendedPath);
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
                        ServerResponseMessage message = new ServerResponseMessage("Lock not allowed for read-only view.", projectName, viewName, appendedPath,
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
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return returnObject;
    }

    private String buildJournalEntry(final String userName, final ArchiveInfoInterface logfile) {
        LogFileOperationLockRevisionCommandArgs commandArgs = request.getCommandArgs();
        return "User: [" + userName + "] locked revision [" + commandArgs.getRevisionString() + "] of ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getViewName(), request.getAppendedPath(), logfile.getShortWorkfileName()) + "]";
    }
}