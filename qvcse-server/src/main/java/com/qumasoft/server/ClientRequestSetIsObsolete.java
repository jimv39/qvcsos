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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.ClientRequestSetIsObsoleteData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseMessage;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Set a file obsolete... which moves it to the cemetery.
 * @author Jim Voris
 */
public class ClientRequestSetIsObsolete implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestSetIsObsoleteData request;

    /**
     * Creates a new instance of ClientRequestSetIsObsolete.
     *
     * @param data command line data, etc.
     */
    public ClientRequestSetIsObsolete(ClientRequestSetIsObsoleteData data) {
        request = data;
    }

    /**
     * Delete the file (really just move it to the cemetery).
     *
     * @param userName the user name.
     * @param response the response object that identifies the client.
     * @return a response object that we'll serialize back to the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            ArchiveInfoInterface archiveInfo = directoryManager.getArchiveInfo(shortWorkfileName);
            if ((archiveInfo != null) && ((directoryManager instanceof ArchiveDirManager) || (directoryManager instanceof ArchiveDirManagerForTranslucentBranch))) {
                if (archiveInfo instanceof LogFile) {
                    LogFile logfile = (LogFile) archiveInfo;
                    if (directoryManager.deleteArchive(userName, shortWorkfileName, response)) {
                        // Log the result.
                        String activity = "User: [" + userName + "] moved: ["
                                + Utility.formatFilenameForActivityJournal(projectName, viewName, appendedPath, shortWorkfileName) + "] to cemetery.";
                        LOGGER.log(Level.INFO, activity);

                        // Send a response message so the client can treat this as a synchronous request.
                        ServerResponseMessage message = new ServerResponseMessage("Set obsolete successful.", projectName, viewName, appendedPath,
                                ServerResponseMessage.LO_PRIORITY);
                        message.setShortWorkfileName(shortWorkfileName);
                        returnObject = message;

                        // Add an entry to the server journal file.
                        ActivityJournalManager.getInstance().addJournalEntry(activity);

                        // Remove the reference copy if we need to...
                        AbstractProjectProperties projectProperties = directoryManager.getProjectProperties();

                        if (projectProperties.getCreateReferenceCopyFlag()) {
                            directoryManager.deleteReferenceCopy(projectProperties, logfile);
                        }
                    } else {
                        // Return a command error.
                        ServerResponseError error = new ServerResponseError("Failed to delete " + shortWorkfileName, projectName, viewName, appendedPath);
                        returnObject = error;
                    }
                } else if (archiveInfo instanceof ArchiveInfoForTranslucentBranch) {
                    if (directoryManager.deleteArchive(userName, shortWorkfileName, response)) {
                        // Log the result.
                        String activity = "User: [" + userName + "] moved: ["
                                + Utility.formatFilenameForActivityJournal(projectName, viewName, appendedPath, shortWorkfileName) + "] to cemetery.";
                        LOGGER.log(Level.INFO, activity);

                        // Send a response message so the client can treat this as a synchronous request.
                        ServerResponseMessage message = new ServerResponseMessage("Set obsolete successful.", projectName, viewName, appendedPath,
                                ServerResponseMessage.LO_PRIORITY);
                        message.setShortWorkfileName(shortWorkfileName);
                        returnObject = message;

                        // Add an entry to the server journal file.
                        ActivityJournalManager.getInstance().addJournalEntry(activity);

                        // Remove the reference copy if we need to...
                        AbstractProjectProperties projectProperties = directoryManager.getProjectProperties();

                        if (projectProperties.getCreateReferenceCopyFlag()) {
                            directoryManager.deleteReferenceCopy(projectProperties, archiveInfo);
                        }
                    } else {
                        // Return a command error.
                        ServerResponseError error = new ServerResponseError("Failed to delete " + shortWorkfileName, projectName, viewName, appendedPath);
                        returnObject = error;
                    }
                } else {
                    // Explain the error.
                    ServerResponseMessage message = new ServerResponseMessage("Delete not allowed for non-trunk views.", projectName, viewName, appendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(shortWorkfileName);
                    returnObject = message;
                }
            } else {
                if (archiveInfo == null) {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive not found for " + shortWorkfileName, projectName, viewName, appendedPath);
                    returnObject = error;
                } else {
                    if (directoryManager instanceof ArchiveDirManagerReadOnlyViewInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Delete archive not allowed for read-only view.", projectName, viewName, appendedPath,
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(shortWorkfileName);
                        returnObject = message;
                    }
                }
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        return returnObject;
    }
}
