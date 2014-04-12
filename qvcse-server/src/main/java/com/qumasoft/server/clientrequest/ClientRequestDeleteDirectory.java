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
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyViewInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteViewInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteDirectoryData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.DirectoryContents;
import com.qumasoft.server.DirectoryContentsManagerFactory;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.RolePrivilegesManager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request delete directory.
 * @author Jim Voris
 */
public class ClientRequestDeleteDirectory implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestDeleteDirectoryData request;

    /**
     * Creates a new instance of ClientRequestDeleteDirectory.
     *
     * @param data the request data.
     */
    public ClientRequestDeleteDirectory(ClientRequestDeleteDirectoryData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseProjectControl serverResponse;
        ServerResponseInterface returnObject = null;
        boolean continueFlag = true;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();

        try {
            if (appendedPath.startsWith(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                throw new QVCSException("You cannot delete the cemetery!");
            }
            if (appendedPath.startsWith(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                throw new QVCSException("You cannot delete the branch archives directory!");
            }
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);

            // Add this directory to the DirectoryContents object of the parent directory.
            // Only do this work if the view is a read-write view...
            if ((appendedPath.length() > 0) && (archiveDirManager instanceof ArchiveDirManagerReadWriteViewInterface)) {
                if (archiveDirManager instanceof ArchiveDirManager) {
                    ArchiveDirManager dirManager = (ArchiveDirManager) archiveDirManager;
                    if (dirManager.getArchiveInfoCollection().isEmpty()) {
                        // Get the directory contents object for this directory, and make
                        // sure that there are no children (files or directories).
                        DirectoryContents directoryContents = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(projectName).
                                getDirectoryContentsForTrunk(appendedPath, dirManager.getDirectoryID(), response);
                        if (!directoryContents.getChildDirectories().isEmpty()) {
                            // The directory is not empty. We won't allow the user to delete it.
                            ServerResponseMessage message = new ServerResponseMessage("You cannot delete a directory unless it is empty and has no child directories.",
                                    projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                            message.setShortWorkfileName("");
                            returnObject = message;
                        } else if (!directoryContents.getFiles().isEmpty()) {
                            // The directory is not empty. We won't allow the user to delete it.
                            // This is an internal error since it means that the directory
                            // contents does not agree with the archive dir manager.
                            ServerResponseMessage message = new ServerResponseMessage("INTERNAL ERROR: You cannot delete a directory unless it is empty.",
                                    projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                            message.setShortWorkfileName("");
                            returnObject = message;
                        } else {
                            // Need to delete the directory from its parent's directoryContents...
                            if (dirManager.getParent() != null) {
                                DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(projectName).deleteDirectoryOnTrunk(dirManager.getDirectoryID(),
                                        response);
                            }

                            // Remove the directory manager from the directory manager's cache.
                            ArchiveDirManagerFactoryForServer.getInstance().removeDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, projectName, viewName,
                                    QVCSConstants.QVCS_SERVED_PROJECT_TYPE, appendedPath);

                            // Delete the actual directory...
                            if (dirManager.directoryExists()) {
                                File directory = new File(dirManager.getArchiveDirectoryName());

                                // Get rid of any clutter in the directory... e.g.
                                // the directoryID file, and other junk files that the user
                                // may have put here.
                                if (deleteChildren(directory)) {
                                    // And delete the directory.
                                    continueFlag = directory.delete();
                                }
                            }

                            // Now send notification to every known user who is logged in to this server...
                            if (continueFlag) {
                                for (ServerResponseFactoryInterface responseFactory : QVCSEnterpriseServer.getConnectedUsers()) {
                                    // And let users who have the privilege know about this added directory.
                                    if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, responseFactory.getUserName(), RolePrivilegesManager.GET)) {
                                        serverResponse = new ServerResponseProjectControl();
                                        serverResponse.setAddFlag(false);
                                        serverResponse.setRemoveFlag(true);
                                        serverResponse.setProjectName(projectName);
                                        serverResponse.setViewName(viewName);
                                        serverResponse.setDirectorySegments(Utility.getDirectorySegments(appendedPath));
                                        serverResponse.setServerName(responseFactory.getServerName());
                                        responseFactory.createServerResponse(serverResponse);
                                        LOGGER.log(Level.INFO, "Sending deleted directory info to: " + responseFactory.getUserName());
                                    }
                                }
                                ActivityJournalManager.getInstance().addJournalEntry("User: '" + userName + "' deleted directory: '" + projectName + "//" + appendedPath + "'");
                            } else {
                                // The directory is not empty!!
                                ServerResponseMessage message = new ServerResponseMessage("Server failed to empty directory for " + appendedPath, projectName, viewName,
                                        appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                                message.setShortWorkfileName("");
                                returnObject = message;
                            }
                        }
                    } else {
                        // The directory is not empty. We won't allow the user to delete it.
                        ServerResponseMessage message = new ServerResponseMessage("You cannot delete a directory unless it is empty.", projectName, viewName, appendedPath,
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName("");
                        returnObject = message;
                    }
                } else {
                    // TODO add support for read/write view.
                    throw new QVCSException("#### Internal error: use of unsupported read/write view type.");
                }
            } else {
                if (appendedPath.length() > 0) {
                    if (archiveDirManager instanceof ArchiveDirManagerReadOnlyViewInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Deleting a directory is not allowed for read-only view.", projectName, viewName,
                                appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName("");
                        returnObject = message;
                    } else {
                        throw new QVCSException("#### Internal error: use of unsupported view type.");
                    }
                }
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return returnObject;
    }

    private boolean deleteChildren(File directory) {
        boolean retVal = true;

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length && retVal; i++) {
            if (files[i].isFile()) {
                retVal = files[i].delete();
            }
        }
        return retVal;
    }
}
