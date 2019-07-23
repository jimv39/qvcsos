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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateBranchData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.BranchManager;
import com.qumasoft.server.ProjectBranch;
import com.qumasoft.server.QVCSShutdownException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a branch.
 * @author Jim Voris
 */
public class ClientRequestServerCreateBranch implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerCreateBranch.class);
    private final ClientRequestServerCreateBranchData request;

    /**
     * Creates a new instance of ClientRequestServerCreateBranch.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerCreateBranch(ClientRequestServerCreateBranchData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.info("User name: [{}]", request.getUserName());

            // Create a branch.
            returnObject = createBranch();
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

    private ServerResponseInterface createBranch() {
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        try {
            // Make sure the branch doesn't already exist.
            ProjectBranch projectBranch = BranchManager.getInstance().getBranch(projectName, branchName);
            if (projectBranch == null) {
                projectBranch = new ProjectBranch();
                projectBranch.setProjectName(projectName);
                projectBranch.setBranchName(branchName);

                // The branch gets most of its properties from the parent project...
                AbstractProjectProperties projectProperties = ArchiveDirManagerFactoryForServer.getInstance().getProjectProperties(request.getServerName(),
                        projectName, QVCSConstants.QVCS_TRUNK_BRANCH,
                        QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
                RemoteBranchProperties remoteBranchProperties = new RemoteBranchProperties(projectName, branchName, projectProperties.getProjectProperties());

                // Set the branch specific properties.
                remoteBranchProperties.setIsReadOnlyBranchFlag(request.getIsReadOnlyBranchFlag());
                remoteBranchProperties.setIsDateBasedBranchFlag(request.getIsDateBasedBranchFlag());
                remoteBranchProperties.setIsTranslucentBranchFlag(request.getIsTranslucentBranchFlag());
                remoteBranchProperties.setIsOpaqueBranchFlag(request.getIsOpaqueBranchFlag());

                if (request.getIsDateBasedBranchFlag()) {
                    remoteBranchProperties.setDateBaseDate(request.getDateBasedDate());
                } else if (request.getIsTranslucentBranchFlag() || request.getIsOpaqueBranchFlag()) {
                    remoteBranchProperties.setBranchDate(new Date());
                }
                remoteBranchProperties.setBranchParent(request.getParentBranchName());

                projectBranch.setRemoteBranchProperties(remoteBranchProperties);

                // And add this branch to the collection of branches that we know about.
                BranchManager.getInstance().addBranch(projectBranch);

                // The reply is the new list of branches.
                ServerResponseListBranches listBranchesResponse = new ServerResponseListBranches();
                listBranchesResponse.setServerName(request.getServerName());
                listBranchesResponse.setProjectName(projectName);

                ClientRequestListClientBranches.buildBranchInfo(listBranchesResponse, projectName);

                returnObject = listBranchesResponse;

                // Add an entry to the server journal file.
                ActivityJournalManager.getInstance().addJournalEntry("Created new branch named '" + branchName + "'.");
            } else {
                // The branch already exists... don't create it again.
                LOGGER.info("Branch: [" + branchName + "] already exists.");
            }
        } catch (QVCSException e) {
            LOGGER.warn("Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());

            // Return an error.
            ServerResponseError error = new ServerResponseError("Caught exception trying change project properties: " + e.getLocalizedMessage(), projectName, branchName, null);
            returnObject = error;
        }
        return returnObject;
    }
}
