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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteBranchData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.QVCSShutdownException;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a branch.
 * @author Jim Voris
 */
public class ClientRequestServerDeleteBranch extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerDeleteBranch.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestServerDeleteBranch.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerDeleteBranch(ClientRequestServerDeleteBranchData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.info("User name: [{}]", userName);

            returnObject = deleteBranch();
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to delete branch " + getRequest().getBranchName(), getRequest().getProjectName(),
                    getRequest().getBranchName(), null);
            error.setSyncToken(getRequest().getSyncToken());
            returnObject = error;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private ServerResponseInterface deleteBranch() {
        ServerResponseInterface returnObject = null;
        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        String serverName = getRequest().getServerName();
        if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            ServerResponseMessage message = new ServerResponseMessage("You are not allowed to delete the Trunk branch", projectName, branchName, null,
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setSyncToken(getRequest().getSyncToken());
            returnObject = message;
        } else if (branchHasChildren()) {
            // There are branches that have this branch as their parent branch... so we do not allow this
            // branch to be deleted until child branches have been pruned.
            ServerResponseMessage message = new ServerResponseMessage("You are not allowed to delete a branch that has child branches.", projectName, branchName, null,
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setSyncToken(getRequest().getSyncToken());
            returnObject = message;
        } else {
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            Branch branch = functionalQueriesDAO.findBranchByProjectNameAndBranchName(projectName, branchName);
            if (branch != null) {
                try {
                    SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
                    sourceControlBehaviorManager.deleteBranch(branch.getProjectId(), branchName);

                    // The reply is the new list of projects.
                    ServerResponseListBranches listBranchesResponse = new ServerResponseListBranches();
                    listBranchesResponse.setServerName(serverName);
                    listBranchesResponse.setProjectName(projectName);

                    ClientRequestListClientBranches.buildBranchInfo(listBranchesResponse, projectName);
                    listBranchesResponse.setSyncToken(getRequest().getSyncToken());

                    returnObject = listBranchesResponse;

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry("Deleted branch [" + branchName + "].");
                } catch (SQLException ex) {
                    LOGGER.warn("Failed to delete branch: [{}] due to SQLException: [{}]", branchName, ex.getLocalizedMessage());
                }
            } else {
                // The branch is already gone...
                LOGGER.warn("Failed to delete non-existant branch: [{}]", branchName);
            }
        }
        return returnObject;
    }

    /**
     * Determine whether this branch has any child branches -- i.e. other branches that use this branch as their parent. Most
     * branches will use the Trunk as their parent, but some may use another branch as their parent branch.
     *
     * @return true if this branch has <i>any</i> child branches; false if there are no child branches.
     */
    private boolean branchHasChildren() {
        boolean retVal = false;
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        Integer childBranchCount = functionalQueriesDAO.getChildBranchCount(getRequest().getProjectName(), getRequest().getBranchName());
        if (childBranchCount > 0) {
            retVal = true;
        }
        return retVal;
    }
}
