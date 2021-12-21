/*
 * Copyright 2021 Jim Voris.
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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.TagInfoData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetTagsInfoData;
import com.qumasoft.qvcslib.response.ServerResponseGetTagsInfo;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestGetTagsInfo implements ClientRequestInterface {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetTagsInfo.class);

    private final ClientRequestGetTagsInfoData request;

    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;

    public ClientRequestGetTagsInfo(ClientRequestGetTagsInfoData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();

        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseInterface returnObject;

        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        if (branchName.length() == 0) {
            branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
        }

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(projectName);

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);

        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<TagInfoData> tagInfoDataList = functionalQueriesDAO.getTagsInfoData(branch.getId());

        // Send back the list of tags' text.
        ServerResponseGetTagsInfo serversResponseGetTagsInfo = new ServerResponseGetTagsInfo();
        serversResponseGetTagsInfo.setProjectName(request.getProjectName());
        serversResponseGetTagsInfo.setBranchName(request.getBranchName());
        serversResponseGetTagsInfo.setTagInfoList(tagInfoDataList);
        returnObject = serversResponseGetTagsInfo;
        LOGGER.info("Found: [{}] tags.", tagInfoDataList.size());

        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

}
