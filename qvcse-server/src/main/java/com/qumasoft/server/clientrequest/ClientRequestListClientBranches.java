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
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServedProjectProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientBranchesData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.server.BranchManager;
import com.qumasoft.server.ProjectBranch;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List client branches.
 * @author Jim Voris
 */
public class ClientRequestListClientBranches implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestListClientBranches.class);
    private final ClientRequestListClientBranchesData request;

    /**
     * Creates a new instance of ClientRequestListClientBranches.
     *
     * @param data the request data.
     */
    public ClientRequestListClientBranches(ClientRequestListClientBranchesData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseListBranches listBranchesResponse = new ServerResponseListBranches();

        listBranchesResponse.setServerName(request.getServerName());
        listBranchesResponse.setProjectName(request.getProjectName());
        buildBranchInfo(listBranchesResponse, request.getProjectName());

        return listBranchesResponse;
    }

    /**
     * Build the list of branches for a given project.
     * @param listBranchesResponse the response object into which we populate the list of branches.
     * @param projectName the project name.
     */
    public static void buildBranchInfo(ServerResponseListBranches listBranchesResponse, String projectName) {
        // Get the branches for this project...
        Collection<ProjectBranch> branches = BranchManager.getInstance().getBranches(projectName);

        String[] branchList;
        if (branches != null) {
            branchList = new String[1 + branches.size()];
        } else {
            branchList = new String[1];
        }
        branchList[0] = QVCSConstants.QVCS_TRUNK_BRANCH;

        Properties[] properties = new Properties[branchList.length];

        try {
            ServedProjectProperties projectProperties = new ServedProjectProperties(System.getProperty("user.dir"), projectName);
            RemoteBranchProperties remoteBranchProperties = new RemoteBranchProperties(projectName, QVCSConstants.QVCS_TRUNK_BRANCH, projectProperties.getProjectProperties());

            // TODO -- Figure out if this user has write access to the trunk branch...
            remoteBranchProperties.setIsReadOnlyBranchFlag(false);
            properties[0] = remoteBranchProperties.getProjectProperties();
        } catch (QVCSException e) {
            LOGGER.warn("Error finding served project names for project: [" + projectName + "].");
        }

        int branchListIndex = 1;

        if (branches != null) {
            Iterator<ProjectBranch> it = branches.iterator();
            while (it.hasNext()) {
                // TODO -- Figure out whether this user should be able to even 'see'
                // this branch.  I haven't decided for sure whether I'll supply this
                // level of granularity for authorization of see branches or not. As
                // a first pass, I should just keep it simple, instead of gunning
                // for overkill.
                ProjectBranch projectBranch = it.next();
                branchList[branchListIndex] = projectBranch.getBranchName();

                properties[branchListIndex] = projectBranch.getRemoteBranchProperties().getProjectProperties();
                branchListIndex++;
            }
        }

        listBranchesResponse.setBranchList(branchList);
        listBranchesResponse.setBranchProperties(properties);
    }
}
