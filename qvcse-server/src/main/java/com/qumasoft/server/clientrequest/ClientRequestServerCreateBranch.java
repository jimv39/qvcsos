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

import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateBranchData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.QVCSShutdownException;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Project;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a branch.
 * @author Jim Voris
 */
public class ClientRequestServerCreateBranch extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerCreateBranch.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestServerCreateBranch.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerCreateBranch(ClientRequestServerCreateBranchData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(getRequest().getUserName(), response);
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.info("User name: [{}]", getRequest().getUserName());

            // Create a branch.
            returnObject = createBranch();
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user " + getRequest().getUserName(), null, null, null);
            error.setSyncToken(getRequest().getSyncToken());
            returnObject = error;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private ServerResponseInterface createBranch() {
        ServerResponseInterface returnObject = null;
        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        // Make sure the branch doesn't already exist.
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        Branch branch = functionalQueriesDAO.findBranchByProjectNameAndBranchName(projectName, branchName);
        if (branch == null) {
            // Add branch to new postgres database...
            addBranchToPostgres();

            // The reply is the new list of branches.
            ServerResponseListBranches listBranchesResponse = new ServerResponseListBranches();
            listBranchesResponse.setServerName(getRequest().getServerName());
            listBranchesResponse.setProjectName(projectName);

            ClientRequestListClientBranches.buildBranchInfo(listBranchesResponse, projectName);
            listBranchesResponse.setSyncToken(getRequest().getSyncToken());

            returnObject = listBranchesResponse;

            // Add an entry to the server journal file.
            ActivityJournalManager.getInstance().addJournalEntry("For project: [" + projectName + "] created new branch named [" + branchName + "].");
        } else {
            // The branch already exists... don't create it again.
            LOGGER.info("For project : [{}], branch: [{}] already exists", projectName, branchName);
        }
        return returnObject;
    }

    private void addBranchToPostgres() {
        try {
            ClientRequestServerCreateBranchData clientRequestServerCreateBranchData = (ClientRequestServerCreateBranchData) getRequest();
            SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();

            // Make sure the database is initialized.
            String projectName = getRequest().getProjectName();
            String branchName = getRequest().getBranchName();
            String parentBranchName = clientRequestServerCreateBranchData.getParentBranchName();
            String tag = clientRequestServerCreateBranchData.getTagBasedTag();

            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(projectName);

            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch parentBranch = branchDAO.findByProjectIdAndBranchName(project.getId(), parentBranchName);
            if (clientRequestServerCreateBranchData.getIsFeatureBranchFlag()) {
                sourceControlBehaviorManager.createFeatureBranch(branchName, project.getId(), parentBranch.getId());
            } else if (clientRequestServerCreateBranchData.getIsTagBasedBranchFlag()) {
                sourceControlBehaviorManager.createTagBasedBranch(branchName, project.getId(), parentBranch.getId(), tag);
            } else if (clientRequestServerCreateBranchData.getIsReleaseBranchFlag()) {
                sourceControlBehaviorManager.createReleaseBranch(branchName, project.getId(), parentBranch.getId());
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
