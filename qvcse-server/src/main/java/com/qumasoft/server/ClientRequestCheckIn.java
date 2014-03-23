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

import com.qumasoft.qvcslib.AddRevisionData;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyViewInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckInData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.LogFileOperationCheckInCommandArgs;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.response.ServerResponseCheckIn;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request check in.
 * @author Jim Voris
 */
public class ClientRequestCheckIn implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestCheckInData request;

    /**
     * Creates a new instance of ClientRequestCheckIn.
     *
     * @param data the request data.
     */
    public ClientRequestCheckIn(ClientRequestCheckInData data) {
        request = data;
    }

    /**
     * Check in a new revision.
     *
     * @param userName the user's user name.
     * @param response identify the client.
     * @return an object to tell the user how things went.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseCheckIn serverResponse;
        ServerResponseInterface returnObject = null;
        LogFileOperationCheckInCommandArgs commandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        FileOutputStream outputStream = null;
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManagerInterface
                    = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, directoryCoordinate,
                    QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            LOGGER.log(Level.FINE, "project name: " + projectName + " view name: " + viewName + " appended path: " + appendedPath);
            LOGGER.log(Level.FINE, "full workfile name: " + commandArgs.getFullWorkfileName());
            LOGGER.log(Level.FINE, "short workfile name: " + commandArgs.getShortWorkfileName());
            LOGGER.log(Level.INFO, "User: " + userName + " checked in " + commandArgs.getShortWorkfileName() + " to view: " + viewName + ", directory: "
                    + appendedPath);
            ArchiveInfoInterface logfile = archiveDirManagerInterface.getArchiveInfo(commandArgs.getShortWorkfileName());
            if ((logfile != null) && (archiveDirManagerInterface instanceof ArchiveDirManagerReadWriteViewInterface)) {
                java.io.File tempFile = java.io.File.createTempFile("QVCS", ".tmp");
                tempFile.deleteOnExit();
                outputStream = new java.io.FileOutputStream(tempFile);
                Utility.writeDataToStream(request.getBuffer(), outputStream);
                commandArgs.setFailureReason("");
                int currentRevisionCount = logfile.getRevisionCount();
                if (logfile.checkInRevision(commandArgs, tempFile.getAbsolutePath(), false)) {
                    // Update the most recent activity date for the containing archiveDirManager.
                    if (archiveDirManagerInterface instanceof ArchiveDirManager) {
                        ArchiveDirManager archiveDirManager = (ArchiveDirManager) archiveDirManagerInterface;
                        archiveDirManager.updateMostRecentActivityDate(logfile.getLastCheckInDate());
                    }
                    // Things worked.  Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseCheckIn();
                    serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                    serverResponse.setClientWorkfileName(commandArgs.getFullWorkfileName());
                    serverResponse.setProjectName(projectName);
                    serverResponse.setViewName(viewName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setKeepLockedFlag(commandArgs.getLockFlag());
                    serverResponse.setProtectWorkfileFlag(commandArgs.getProtectWorkfileFlag());
                    serverResponse.setNoExpandKeywordsFlag(commandArgs.getNoExpandKeywordsFlag());
                    serverResponse.setNewRevisionString(commandArgs.getNewRevisionString());
                    serverResponse.setIndex(request.getIndex());

                    // If there is keyword expansion and a new revision was created,
                    // then send back the info the client will need.
                    int newRevisionCount = logfile.getRevisionCount();
                    if (logfile.getAttributes().getIsExpandKeywords()
                            && (commandArgs.getNewRevisionString() != null)
                            && (currentRevisionCount != newRevisionCount)) {
                        serverResponse.setLogfileInfo(logfile.getLogfileInfo());
                        serverResponse.setAddedRevisionData(createAddedRevisionData(logfile, commandArgs));
                    }
                    byte[] digest = logfile.getDefaultRevisionDigest();
                    LogFileInterface logFileInterface = (LogFileInterface) logfile;
                    serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator, logFileInterface.getIsObsolete(), digest,
                            logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                    tempFile.delete();

                    // If we need to create a reference copy...
                    if (archiveDirManagerInterface.getProjectProperties().getCreateReferenceCopyFlag()) {
                        archiveDirManagerInterface.createReferenceCopy(archiveDirManagerInterface.getProjectProperties(), logfile, request.getBuffer());
                    }
                    returnObject = serverResponse;

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(buildJournalEntry(userName, logfile));

                    // If we need to capture a new fileID-to-directory association because we're creating the first revision
                    // on a branch...
                    if (commandArgs.getForceBranchFlag() && (logfile instanceof ArchiveInfoForTranslucentBranch)) {
                        // Add an entry into the FileIDDictionary for this branch... making sure to add it to the
                        // dictionary only if we're creating the branch... i.e. only if the commandArgs.getForceBranchFlag() is true.
                        FileIDDictionary.getInstance().saveFileIDInfo(projectName, viewName, logfile.getFileID(), appendedPath,
                                logfile.getShortWorkfileName(),
                                archiveDirManagerInterface.getDirectoryID());
                    }
                } else {
                    // Return a command error.
                    String errorMessage = "Failed to check in " + commandArgs.getShortWorkfileName() + ". " + commandArgs.getFailureReason();
                    ServerResponseMessage message = new ServerResponseMessage(errorMessage, projectName, viewName, appendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    returnObject = message;
                }
            } else {
                if (logfile == null) {
                    // Explain the error.
                    ServerResponseMessage message = new ServerResponseMessage("Archive not found for " + commandArgs.getShortWorkfileName(), projectName,
                            viewName, appendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    returnObject = message;
                } else {
                    if (archiveDirManagerInterface instanceof ArchiveDirManagerReadOnlyViewInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Checkin not allowed for read-only view.", projectName, viewName,
                                appendedPath,
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                        returnObject = message;
                    }
                }
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath,
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            returnObject = message;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Caught exception on check-in: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath,
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            returnObject = message;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
        return returnObject;
    }

    private String buildJournalEntry(final String userName, final ArchiveInfoInterface logfile) {
        LogFileOperationCheckInCommandArgs commandArgs = request.getCommandArgs();
        if (commandArgs.getApplyLabelFlag()) {
            return "User: [" + userName + "] checked-in revision [" + commandArgs.getNewRevisionString() + "] of ["
                    + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getViewName(), request.getAppendedPath(), logfile.getShortWorkfileName())
                    + "] and applied label: [" + commandArgs.getLabel() + "]";
        } else {
            return "User: [" + userName + "] checked-in revision [" + commandArgs.getNewRevisionString() + "] of ["
                    + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getViewName(), request.getAppendedPath(), logfile.getShortWorkfileName()) + "]";
        }
    }

    /**
     * Create data about the newly added revision.
     *
     * @param logfile the archive info to which the new revision was added.
     * @param commandArgs the command arguments used to create the new revision which will include the new revision string.
     * @return an object identifying the new revision.
     */
    public static AddRevisionData createAddedRevisionData(ArchiveInfoInterface logfile, LogFileOperationCheckInCommandArgs commandArgs) {
        AddRevisionData addedRevisionData = new AddRevisionData();

        RevisionInformation revisionInformation = logfile.getRevisionInformation();
        String newRevisionString = commandArgs.getNewRevisionString();
        String parentRevisionString = commandArgs.getParentRevisionString();

        for (int i = 0; i < logfile.getRevisionCount(); i++) {
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
            if (0 == revHeader.getRevisionString().compareTo(newRevisionString)) {
                addedRevisionData.setNewRevisionHeader(revHeader);
                addedRevisionData.setNewRevisionIndex(i);
            }

            if (0 == revHeader.getRevisionString().compareTo(parentRevisionString)) {
                addedRevisionData.setParentRevisionHeader(revHeader);
                addedRevisionData.setParentRevisionIndex(i);
            }
        }

        return addedRevisionData;
    }
}
