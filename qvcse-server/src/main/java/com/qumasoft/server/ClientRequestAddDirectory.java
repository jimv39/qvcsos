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
import com.qumasoft.qvcslib.requestdata.ClientRequestAddDirectoryData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.qvcslib.Utility;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request add directory.
 * @author Jim Voris
 */
public class ClientRequestAddDirectory implements ClientRequestInterface {

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestAddDirectoryData request;

    /**
     * Creates a new instance of ClientRequestAddDirectory.
     * @param data client request data.
     */
    public ClientRequestAddDirectory(ClientRequestAddDirectoryData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseProjectControl serverResponse;
        ServerResponseInterface returnObject = null;
        try {
            if (request.getAppendedPath().startsWith(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                throw new QVCSException("You cannot add a directory to the cemetery!");
            }
            if (request.getAppendedPath().startsWith(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                throw new QVCSException("You cannot add a directory to the branch archives directory!");
            }
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(request.getProjectName(), request.getViewName(), request.getAppendedPath());
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);

            // Add this directory to the DirectoryContents object of the parent directory.
            // Only do this work if the view is a read-write view...
            if ((request.getAppendedPath().length() > 0) && (archiveDirManager instanceof ArchiveDirManagerReadWriteViewInterface)) {
                if (archiveDirManager instanceof ArchiveDirManager) {
                    ArchiveDirManager dirManager = (ArchiveDirManager) archiveDirManager;
                    String parentAppendedPath = ServerUtility.getParentAppendedPath(request.getAppendedPath());
                    DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(request.getProjectName()).addDirectory(request.getViewName(),
                            dirManager.getProjectRootArchiveDirManager().getDirectoryID(), parentAppendedPath, dirManager.getParent().getDirectoryID(), dirManager.getDirectoryID(),
                            Utility.getLastDirectorySegment(request.getAppendedPath()), response);
                } else {
                    if (archiveDirManager instanceof ArchiveDirManagerForTranslucentBranch) {
                        DirectoryCoordinate rootCoordinate = new DirectoryCoordinate(request.getProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "");
                        ArchiveDirManagerInterface projectRootArchiveDirManager
                                = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                                rootCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, userName, response, true);
                        int rootDirectoryId = projectRootArchiveDirManager.getDirectoryID();
                        int childDirectoryID = DirectoryIDManager.getInstance().getNewDirectoryID();
                        String parentAppendedPath = ServerUtility.getParentAppendedPath(request.getAppendedPath());
                        DirectoryCoordinate parentCoordinate = new DirectoryCoordinate(request.getProjectName(), request.getViewName(), parentAppendedPath);
                        ArchiveDirManagerInterface parentDirManager
                                = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, parentCoordinate,
                                QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
                        int parentDirectoryID = parentDirManager.getDirectoryID();
                        DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(request.getProjectName());
                        String finalDirectorySegment = Utility.getLastDirectorySegment(request.getAppendedPath());
                        directoryContentsManager.addDirectory(request.getViewName(), rootDirectoryId, parentAppendedPath, parentDirectoryID, childDirectoryID,
                                finalDirectorySegment, response);
                    } else if (archiveDirManager instanceof ArchiveDirManagerForOpaqueBranch) {
                        // TODO
                        LOGGER.log(Level.INFO, "Add directory not yet implemented for an opaque branch.");
                    } else {
                        throw new QVCSException("Unexpected directory manager type: " + archiveDirManager.getClass().toString());
                    }
                }
                for (ServerResponseFactoryInterface responseFactory : QVCSEnterpriseServer.getConnectedUsers()) {
                    // And let users who have the privilege know about this added directory.
                    if (RolePrivilegesManager.getInstance().isUserPrivileged(request.getProjectName(), responseFactory.getUserName(), RolePrivilegesManager.GET)) {
                        serverResponse = new ServerResponseProjectControl();
                        serverResponse.setAddFlag(true);
                        serverResponse.setProjectName(request.getProjectName());
                        serverResponse.setViewName(request.getViewName());
                        serverResponse.setDirectorySegments(Utility.getDirectorySegments(request.getAppendedPath()));
                        serverResponse.setServerName(responseFactory.getServerName());
                        responseFactory.createServerResponse(serverResponse);
                        LOGGER.log(Level.INFO, "Sending created directory info to: " + responseFactory.getUserName());
                    }
                }
                ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] added directory: [" + archiveDirManager.getProjectName() + "//"
                        + archiveDirManager.getAppendedPath()
                        + "] to " + request.getViewName());
            } else {
                if (request.getAppendedPath().length() > 0) {
                    if (archiveDirManager instanceof ArchiveDirManagerReadOnlyViewInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Adding a directory is not allowed for read-only view.", request.getProjectName(),
                                request.getViewName(), request.getAppendedPath(),
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName("");
                        returnObject = message;
                    } else {
                        throw new QVCSException("#### Internal error: use of unsupported view type.");
                    }
                }
            }
        } catch (QVCSException | SQLException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), request.getProjectName(), request.getViewName(), request.getAppendedPath(),
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
        }
        return returnObject;
    }
}
