/*
 * Copyright 2021-2022 Jim Voris.
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

import com.qumasoft.qvcslib.CommitInfo;
import com.qumasoft.qvcslib.CommitInfoListWrapper;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetCommitListForMoveableTagData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseGetCommitListForMoveableTagReadOnlyBranches;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.CommitDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.dataaccess.impl.TagDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.Project;
import com.qvcsos.server.datamodel.Tag;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestGetCommitListForMoveableTag extends AbstractClientRequest {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetCommitListForMoveableTag.class);
    private static final Integer LOOK_BACK_COUNT = 100;

    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;

    public ClientRequestGetCommitListForMoveableTag(ClientRequestGetCommitListForMoveableTagData data) {
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
        if (branch.getBranchTypeId() != QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE) {
            String errorMessage = String.format("Wrong branch type: [%d]", branch.getBranchTypeId());
            LOGGER.warn(errorMessage);
            throw new QVCSRuntimeException(errorMessage);
        }

        Integer tagId = branch.getTagId();
        TagDAO tagDAO = new TagDAOImpl(schemaName);
        Tag branchTag = tagDAO.findById(tagId);
        if (!branchTag.getMoveableFlag()) {
            String errorMessage = String.format("Tag is not moveable!!: [%s]", branchTag.getTagText());
            LOGGER.warn(errorMessage);
            throw new QVCSRuntimeException(errorMessage);
        }

        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        Integer startingCommitId = branchTag.getCommitId() - LOOK_BACK_COUNT;
        if (startingCommitId < 0) {
            startingCommitId = 1;
        }

        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<Branch> branchAncestryList = functionalQueriesDAO.getBranchAncestryList(branch.getParentBranchId());
        String branchesToSearchString = functionalQueriesDAO.buildBranchesToSearchString(branchAncestryList);

        List<Commit> commitList = commitDAO.getCommitList(startingCommitId, branchesToSearchString);
        List<CommitInfo> commitInfoList = new ArrayList<>();
        for (Commit commit : commitList) {
            CommitInfo commitInfo = new CommitInfo();
            commitInfo.setCommitId(commit.getId());
            commitInfo.setCommitDate(commit.getCommitDate());
            commitInfo.setCommitMessage(commit.getCommitMessage());
            commitInfoList.add(commitInfo);
        }

        ServerResponseGetCommitListForMoveableTagReadOnlyBranches list = new ServerResponseGetCommitListForMoveableTagReadOnlyBranches();
        list.setProjectName(getRequest().getProjectName());
        list.setBranchName(getRequest().getBranchName());
        CommitInfoListWrapper wrapper = new CommitInfoListWrapper();
        wrapper.setCommitInfoList(commitInfoList);
        wrapper.setTagCommitId(branchTag.getCommitId());
        list.setCommitInfoListWrapper(wrapper);
        list.setSyncToken(getRequest().getSyncToken());
        returnObject = list;
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());

        return returnObject;
    }

}
