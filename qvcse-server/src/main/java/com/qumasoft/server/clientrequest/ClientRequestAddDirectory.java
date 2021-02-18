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
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyBranchInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteBranchInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestAddDirectoryData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ArchiveDirManagerForFeatureBranch;
import com.qumasoft.server.ArchiveDirManagerForOpaqueBranch;
import com.qumasoft.server.BranchManager;
import com.qumasoft.server.DirectoryContentsManager;
import com.qumasoft.server.DirectoryContentsManagerFactory;
import com.qumasoft.server.DirectoryIDManager;
import com.qumasoft.server.ProjectBranch;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.RolePrivilegesManager;
import com.qumasoft.server.ServerUtility;
import java.sql.SQLException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request add directory.
 * @author Jim Voris
 */
public class ClientRequestAddDirectory implements ClientRequestInterface {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestAddDirectory.class);
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
        ServerResponseInterface returnObject = null;
        try {
            if (request.getAppendedPath().startsWith(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                throw new QVCSException("You cannot add a directory to the cemetery!");
            }
            if (request.getAppendedPath().startsWith(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                throw new QVCSException("You cannot add a directory to the branch archives directory!");
            }
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(request.getProjectName(), request.getBranchName(), request.getAppendedPath());
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);

            // Add this directory to the DirectoryContents object of the parent directory.
            // Only do this work if the branch is a read-write branch...
            if ((request.getAppendedPath().length() > 0) && (archiveDirManager instanceof ArchiveDirManagerReadWriteBranchInterface)) {
                if (archiveDirManager instanceof ArchiveDirManager) {
                    ArchiveDirManager dirManager = (ArchiveDirManager) archiveDirManager;
                    String parentAppendedPath = ServerUtility.getParentAppendedPath(request.getAppendedPath());
                    DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(request.getProjectName()).addDirectory(request.getBranchName(),
                            dirManager.getProjectRootArchiveDirManager().getDirectoryID(), parentAppendedPath, dirManager.getParent().getDirectoryID(), dirManager.getDirectoryID(),
                            Utility.getLastDirectorySegment(request.getAppendedPath()), response);
                } else {
                    if (archiveDirManager instanceof ArchiveDirManagerForFeatureBranch) {
                        DirectoryCoordinate rootCoordinate = new DirectoryCoordinate(request.getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH, "");
                        ArchiveDirManagerInterface projectRootArchiveDirManager
                                = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                                rootCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, userName, response);
                        int rootDirectoryId = projectRootArchiveDirManager.getDirectoryID();
                        int childDirectoryID = DirectoryIDManager.getInstance().getNewDirectoryID();
                        String parentAppendedPath = ServerUtility.getParentAppendedPath(request.getAppendedPath());
                        DirectoryCoordinate parentCoordinate = new DirectoryCoordinate(request.getProjectName(), request.getBranchName(), parentAppendedPath);
                        ArchiveDirManagerInterface parentDirManager
                                = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, parentCoordinate,
                                QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
                        int parentDirectoryID = parentDirManager.getDirectoryID();
                        DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(request.getProjectName());
                        String finalDirectorySegment = Utility.getLastDirectorySegment(request.getAppendedPath());
                        directoryContentsManager.addDirectory(request.getBranchName(), rootDirectoryId, parentAppendedPath, parentDirectoryID, childDirectoryID,
                                finalDirectorySegment, response);
                    } else if (archiveDirManager instanceof ArchiveDirManagerForOpaqueBranch) {
                        // TODO
                        LOGGER.info("Add directory not yet implemented for an opaque branch.");
                        throw new UnsupportedOperationException("Add directory not yet implemented for an opaque branch.");
                    } else {
                        throw new QVCSException("Unexpected directory manager type: " + archiveDirManager.getClass().toString());
                    }
                }
                notifyClientsOfAddedDirectory(request.getBranchName());

                // Notify any child feature branches about the added directory.
                notifyChildFeatureBranches(archiveDirManager);

                ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] added directory: [" + archiveDirManager.getProjectName() + "//"
                        + archiveDirManager.getAppendedPath()
                        + "] to " + request.getBranchName());
            } else {
                if (request.getAppendedPath().length() > 0) {
                    if (archiveDirManager instanceof ArchiveDirManagerReadOnlyBranchInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Adding a directory is not allowed for read-only branch.", request.getProjectName(),
                                request.getBranchName(), request.getAppendedPath(),
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName("");
                        returnObject = message;
                    } else {
                        throw new QVCSException("#### Internal error: use of unsupported branch type.");
                    }
                }
            }
        } catch (QVCSException | SQLException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), request.getProjectName(), request.getBranchName(), request.getAppendedPath(),
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
        }
        return returnObject;
    }

    private void notifyClientsOfAddedDirectory(String branchName) {
        ServerResponseProjectControl serverResponse;
        for (ServerResponseFactoryInterface responseFactory : QVCSEnterpriseServer.getConnectedUsers()) {
            // And let users who have the privilege know about this added directory.
            if (RolePrivilegesManager.getInstance().isUserPrivileged(request.getProjectName(), responseFactory.getUserName(), RolePrivilegesManager.GET)) {
                serverResponse = new ServerResponseProjectControl();
                serverResponse.setAddFlag(true);
                serverResponse.setProjectName(request.getProjectName());
                serverResponse.setBranchName(branchName);
                serverResponse.setDirectorySegments(Utility.getDirectorySegments(request.getAppendedPath()));
                serverResponse.setServerName(responseFactory.getServerName());
                responseFactory.createServerResponse(serverResponse);
                LOGGER.info("notifyClientsOfAddedDirectory: Sent created directory info for branch: [{}] directory: [{}] to: [{}]",
                        branchName, request.getAppendedPath(), responseFactory.getUserName());
            }
        }
    }

    private void notifyChildFeatureBranches(ArchiveDirManagerInterface archiveDirManager) {
        // There is only work to do here if the addition was to the trunk...
        if (archiveDirManager instanceof ArchiveDirManager) {
            Collection<ProjectBranch> branches = BranchManager.getInstance().getBranches(request.getProjectName());
            if (branches != null) {
                branches.forEach((projectBranch) -> {
                    notifyClientsOfAddedDirectory(projectBranch.getBranchName());
                });
            }
        }
    }
}
