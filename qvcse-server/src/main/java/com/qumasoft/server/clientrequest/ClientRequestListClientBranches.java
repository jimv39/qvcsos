/*   Copyright 2004-2023 Jim Voris
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

import com.qumasoft.qvcslib.ClientBranchInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientBranchesData;
import com.qumasoft.qvcslib.response.AbstractServerManagementResponse;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.CommitDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.TagDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * List client branches.
 * @author Jim Voris
 */
public class ClientRequestListClientBranches extends AbstractClientRequest {

    /**
     * Creates a new instance of ClientRequestListClientBranches.
     *
     * @param data the request data.
     */
    public ClientRequestListClientBranches(ClientRequestListClientBranchesData data) {
        setRequest(data);
    }

    @Override
    public AbstractServerManagementResponse execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseListBranches listBranchesResponse = new ServerResponseListBranches();

        listBranchesResponse.setServerName(getRequest().getServerName());
        listBranchesResponse.setProjectName(getRequest().getProjectName());
        buildBranchInfo(listBranchesResponse, getRequest().getProjectName());

        listBranchesResponse.setSyncToken(getRequest().getSyncToken());
        return listBranchesResponse;
    }

    /**
     * Build the list of branches for a given project.
     * @param listBranchesResponse the response object into which we populate the list of branches.
     * @param projectName the project name.
     */
    public static void buildBranchInfo(ServerResponseListBranches listBranchesResponse, String projectName) {
        // Get the branches for this project...
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(DatabaseManager.getInstance().getSchemaName());
        TagDAO tagDAO = new TagDAOImpl(DatabaseManager.getInstance().getSchemaName());

        List<Branch> branches = functionalQueriesDAO.findBranchesForProjectName(projectName);

        if (branches != null && !branches.isEmpty()) {
            List<ClientBranchInfo> clientBranchInfoList = new ArrayList<>();
            for (Branch branch : branches) {
                ClientBranchInfo branchInfo = new ClientBranchInfo();
                branchInfo.setProjectId(branch.getProjectId());
                branchInfo.setBranchName(branch.getBranchName());
                branchInfo.setBranchId(branch.getId());
                Properties branchProperties = new Properties();

                // Figure out the name of the branch's parent branch.
                String parentBranchName = getParentBranchName(branch);

                if (branch.getBranchTypeId() == QVCSConstants.QVCS_TRUNK_BRANCH_TYPE) {
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticIsReadOnlyBranchFlagTag(), QVCSConstants.QVCS_NO);
                } else if (branch.getBranchTypeId() == QVCSConstants.QVCS_FEATURE_BRANCH_TYPE) {
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticIsFeatureBranchFlagTag(), QVCSConstants.QVCS_YES);
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticBranchParentTag(), parentBranchName);
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticIsReadOnlyBranchFlagTag(), QVCSConstants.QVCS_NO);
                } else if (branch.getBranchTypeId() == QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE) {
                    Tag tag = tagDAO.findById(branch.getTagId());
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticIsReadOnlyBranchFlagTag(), QVCSConstants.QVCS_YES);
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticIsTagBasedBranchFlagTag(), QVCSConstants.QVCS_YES);
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticTagBasedBranchTag(), tag.getTagText());
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticBranchParentTag(), parentBranchName);
                    if (tag.getMoveableFlag()) {
                        branchProperties.setProperty(RemotePropertiesBaseClass.getStaticMoveableTagTag(), QVCSConstants.QVCS_YES);
                    } else {
                        branchProperties.setProperty(RemotePropertiesBaseClass.getStaticMoveableTagTag(), QVCSConstants.QVCS_NO);
                    }
                    CommitDAO commitDAO = new CommitDAOImpl(DatabaseManager.getInstance().getSchemaName());
                    Commit commit = commitDAO.findById(tag.getCommitId());
                    Long commitTime = commit.getCommitDate().getTime();
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticBranchAnchorDateTag(), String.valueOf(commitTime));
                } else if (branch.getBranchTypeId() == QVCSConstants.QVCS_RELEASE_BRANCH_TYPE) {
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticIsReleaseBranchFlagTag(), QVCSConstants.QVCS_YES);
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticBranchParentTag(), parentBranchName);
                    branchProperties.setProperty(RemotePropertiesBaseClass.getStaticIsReadOnlyBranchFlagTag(), QVCSConstants.QVCS_NO);
                    CommitDAO commitDAO = new CommitDAOImpl(DatabaseManager.getInstance().getSchemaName());
                    Commit commit = commitDAO.findById(branch.getCommitId());
                    Long commitTime = commit.getCommitDate().getTime();
                    branchProperties.setProperty(RemotePropertiesBaseClass.getBranchAnchorDateTag(projectName, branch.getBranchName()), commitTime.toString());
                }
                branchInfo.setBranchProperties(branchProperties);
                clientBranchInfoList.add(branchInfo);
            }
            listBranchesResponse.setClientBranchInfoList(clientBranchInfoList);
        }
    }

    private static String getParentBranchName(Branch branch) {
        String parentBranchName = "";
        if (branch.getParentBranchId() != null) {
            BranchDAO branchDAO = new BranchDAOImpl(DatabaseManager.getInstance().getSchemaName());
            Branch parentBranch = branchDAO.findById(branch.getParentBranchId());
            parentBranchName = parentBranch.getBranchName();
        }
        return parentBranchName;
    }
}
