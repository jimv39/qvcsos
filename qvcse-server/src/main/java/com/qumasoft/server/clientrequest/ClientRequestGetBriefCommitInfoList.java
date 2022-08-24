/*
 * Copyright 2022 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.BriefCommitInfo;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetBriefCommitInfoListData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseGetBriefCommitInfoList;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.CommitDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.Project;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestGetBriefCommitInfoList extends AbstractClientRequest {

    private static final Integer LOOK_BACK_COUNT = 100;

    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;

    public ClientRequestGetBriefCommitInfoList(ClientRequestGetBriefCommitInfoListData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();

        setRequest(data);
    }

    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject;

        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        if (branchName.length() == 0) {
            throw new QVCSRuntimeException("Branch name missing!");
        }

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(projectName);

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);

        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        ClientRequestGetBriefCommitInfoListData clientRequestGetBriefCommitInfoListData = (ClientRequestGetBriefCommitInfoListData) getRequest();
        Integer startingCommitId = clientRequestGetBriefCommitInfoListData.getCommitId() - LOOK_BACK_COUNT;
        if (startingCommitId < 0) {
            startingCommitId = 1;
        }

        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<Branch> branchAncestryList = functionalQueriesDAO.getBranchAncestryList(branch.getId());
        String branchesToSearchString = functionalQueriesDAO.buildBranchesToSearchString(branchAncestryList);

        List<Commit> commitList = commitDAO.getCommitList(startingCommitId, branchesToSearchString);
        List<BriefCommitInfo> briefCommitInfoList = new ArrayList<>();
        for (Commit commit : commitList) {
            BriefCommitInfo briefCommitInfo = new BriefCommitInfo();
            briefCommitInfo.setCommitId(commit.getId());
            briefCommitInfo.setCommitDate(commit.getCommitDate());
            briefCommitInfo.setCommitMessage(commit.getCommitMessage());
            briefCommitInfoList.add(briefCommitInfo);
        }

        // Look up the files that have the given commit id...
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        List<Integer> fileIdList = fileRevisionDAO.findFileIdListForCommitId(clientRequestGetBriefCommitInfoListData.getCommitId());

        ServerResponseGetBriefCommitInfoList list = new ServerResponseGetBriefCommitInfoList();
        list.setProjectName(getRequest().getProjectName());
        list.setBranchName(getRequest().getBranchName());
        list.setBriefCommitInfoList(briefCommitInfoList);
        list.setFileIdList(fileIdList);
        list.setSyncToken(getRequest().getSyncToken());
        returnObject = list;
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());

        return returnObject;
    }

}
