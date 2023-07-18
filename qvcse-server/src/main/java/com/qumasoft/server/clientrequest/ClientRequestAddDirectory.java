/*   Copyright 2004-2022 Jim Voris
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

import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestAddDirectoryData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseAddDirectory;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.RolePrivilegesManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.DirectoryLocationDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryLocationDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.Project;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request add directory.
 * @author Jim Voris
 */
public class ClientRequestAddDirectory extends AbstractClientRequest {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestAddDirectory.class);

    private int projectId;
    private int branchId;
    private int addedDirectoryLocationId;
    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;

    /**
     * Creates a new instance of ClientRequestAddDirectory.
     * @param data client request data.
     */
    public ClientRequestAddDirectory(ClientRequestAddDirectoryData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        setRequest(data);
    }

    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject = null;
        try {
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(getRequest().getProjectName());
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), getRequest().getBranchName());

            // Only do this work if the branch is a read-write branch...
            if ((getRequest().getAppendedPath().length() > 0) && (branch.getBranchTypeId() <= 2)) {
                this.projectId = project.getId();
                this.branchId = branch.getId();
                this.addedDirectoryLocationId = buildAddedDirectoryLocationId(userName, this.projectId, this.branchId, getRequest().getAppendedPath());
                LOGGER.info("projectId: [{}], branchId: [{}], built directoryLocationId: [{}]", this.projectId, this.branchId, this.addedDirectoryLocationId);
                notifyClientsOfAddedDirectory(getRequest().getBranchName());

                // Notify any child feature branches about the added directory.
                notifyChildFeatureBranches(branch);

                ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] added directory: [" + getRequest().getProjectName() + "//"
                        + getRequest().getAppendedPath()
                        + "] to " + getRequest().getBranchName());
                ServerResponseAddDirectory serverResponseAddDirectory = new ServerResponseAddDirectory();
                returnObject = serverResponseAddDirectory;
            } else {
                if (getRequest().getAppendedPath().length() > 0) {
                    if (branch.getBranchTypeId() > 2) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Adding a directory is not allowed for read-only branch.", getRequest().getProjectName(),
                                getRequest().getBranchName(), getRequest().getAppendedPath(),
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName("");
                        returnObject = message;
                    } else {
                        throw new QVCSException("#### Internal error: use of unsupported branch type.");
                    }
                } else {
                    // The project's root directory has already been created, but the request needs a response.
                    ServerResponseAddDirectory serverResponseAddDirectory = new ServerResponseAddDirectory();
                    returnObject = serverResponseAddDirectory;
                }
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), getRequest().getProjectName(), getRequest().getBranchName(), getRequest().getAppendedPath(),
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }

    private void notifyClientsOfAddedDirectory(String branchName) {
        ServerResponseProjectControl serverResponse;
        for (ServerResponseFactoryInterface responseFactory : QVCSEnterpriseServer.getConnectedUsers()) {
            // And let users who have the privilege know about this added directory.
            if (RolePrivilegesManager.getInstance().isUserPrivileged(getRequest().getProjectName(), responseFactory.getUserName(), RolePrivilegesManager.GET)) {
                serverResponse = new ServerResponseProjectControl();
                serverResponse.setAddFlag(true);
                serverResponse.setProjectName(getRequest().getProjectName());
                serverResponse.setBranchName(branchName);
                serverResponse.setDirectorySegments(Utility.getDirectorySegments(getRequest().getAppendedPath()));
                serverResponse.setServerName(responseFactory.getServerName());
                responseFactory.createServerResponse(serverResponse);
                LOGGER.info("notifyClientsOfAddedDirectory: Sent created directory info for branch: [{}] directory: [{}] to: [{}]",
                        branchName, getRequest().getAppendedPath(), responseFactory.getUserName());
            }
        }
    }

    private void notifyChildFeatureBranches(Branch branch) {
        // There is only work to do here if the addition was to the trunk...
        if (branch.getParentBranchId() == null) {
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            List<Branch> branches = functionalQueriesDAO.findBranchesForProjectName(getRequest().getProjectName());
            if (branches != null) {
                for (Branch b : branches) {
                    if (b.getBranchTypeId() == 2) {
                        notifyClientsOfAddedDirectory(b.getBranchName());
                    }
                }
            }
        }
    }

    public Integer buildAddedDirectoryLocationId(String userName, int projId, int brnchId, String appendedPath) {
        Integer id = null;
        String[] directorySegments = appendedPath.split(File.separator);

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findById(brnchId);
        Integer rootDirectoryId = branch.getRootDirectoryId();

        DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
        DirectoryLocation parentDirectoryLocation = directoryLocationDAO.findByDirectoryId(rootDirectoryId);

        StringBuilder constructedAppendedPath = new StringBuilder();
        boolean successFlag = true;
        for (String segment : directorySegments) {
            constructedAppendedPath.append(segment).append(File.separator);
            DirectoryLocation directoryLocation = sourceControlBehaviorManager.findChildDirectoryLocation(brnchId, parentDirectoryLocation.getId(), segment);
            if (directoryLocation == null) {
                try {
                    // Create the directory segment.
                    id = sourceControlBehaviorManager.addDirectory(brnchId, projId, parentDirectoryLocation.getId(), segment);
                    LOGGER.info("created directorylocation with id: [{}] for segment: [{}]", id, segment);
                    parentDirectoryLocation = directoryLocationDAO.findById(id);
                } catch (SQLException e) {
                    successFlag = false;
                    try {
                        databaseManager.getConnection().rollback();
                    } catch (SQLException ex) {
                        LOGGER.warn("Rollback failed.", ex);
                    }
                    LOGGER.warn("Failed to create directory.", e);
                    break;
                }
            } else {
                parentDirectoryLocation = directoryLocation;

                // Return id in the case where we do not need to create the directory.
                id = parentDirectoryLocation.getId();
            }
        }
        if (successFlag) {
            try {
                databaseManager.getConnection().commit();
            } catch (SQLException ex) {
                LOGGER.warn("Commit failed", ex);
            }
        }
        LOGGER.info("Appended path: [{}]; constructed appended path: [{}]", appendedPath, constructedAppendedPath.toString());
        return id;
    }

    /**
     * We need this accessor for the unit test.
     * @return the sourceControlBehaviorManager.
     */
    public SourceControlBehaviorManager getSourceControlBehaviorManager() {
        return sourceControlBehaviorManager;
    }
}
