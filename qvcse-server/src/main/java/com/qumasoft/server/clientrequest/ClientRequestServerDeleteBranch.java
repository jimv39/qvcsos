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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteBranchData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.BranchManager;
import com.qumasoft.server.ProjectBranch;
import com.qumasoft.server.QVCSShutdownException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a branch.
 * @author Jim Voris
 */
public class ClientRequestServerDeleteBranch implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerDeleteBranch.class);
    private final ClientRequestServerDeleteBranchData request;

    /**
     * Creates a new instance of ClientRequestServerDeleteBranch.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerDeleteBranch(ClientRequestServerDeleteBranchData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.info("User name: [{}]", request.getUserName());

            returnObject = deleteBranch(response);
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user " + request.getUserName(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }

    private ServerResponseInterface deleteBranch(ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String serverName = request.getServerName();
        if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            ServerResponseMessage message = new ServerResponseMessage("You are not allowed to delete the Trunk branch", projectName, branchName, null,
                    ServerResponseMessage.HIGH_PRIORITY);
            returnObject = message;
        } else if (branchHasChildren()) {
            // There are branches that have this branch as their parent branch... so we do not allow this
            // branch to be deleted until child branches have been pruned.
            ServerResponseMessage message = new ServerResponseMessage("You are not allowed to delete a branch that has child branches.", projectName, branchName, null,
                    ServerResponseMessage.HIGH_PRIORITY);
            returnObject = message;
        } else {
            ProjectBranch projectBranch = BranchManager.getInstance().getBranch(projectName, branchName);
            if (projectBranch != null) {
                BranchManager.getInstance().removeBranch(projectBranch, response);

                // The reply is the new list of projects.
                ServerResponseListBranches listBranchesResponse = new ServerResponseListBranches();
                listBranchesResponse.setServerName(serverName);
                listBranchesResponse.setProjectName(projectName);

                ClientRequestListClientBranches.buildBranchInfo(listBranchesResponse, projectName);

                returnObject = listBranchesResponse;

                // Add an entry to the server journal file.
                ActivityJournalManager.getInstance().addJournalEntry("Deleted branch [" + branchName + "].");
            } else {
                // The project properties file is already gone...
                LOGGER.warn("Failed to delete non-existant branch: [" + branchName + "].");
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
        Collection<ProjectBranch> branches = BranchManager.getInstance().getBranches(request.getProjectName());
        for (ProjectBranch projectBranch : branches) {
            String branchParent = projectBranch.getRemoteBranchProperties().getBranchParent();
            if (branchParent != null) {
                if (0 == branchParent.compareTo(request.getBranchName())) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }
}
