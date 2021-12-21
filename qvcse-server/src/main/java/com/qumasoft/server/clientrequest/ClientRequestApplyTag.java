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
import com.qumasoft.qvcslib.requestdata.ClientRequestApplyTagData;
import com.qumasoft.qvcslib.response.ServerResponseApplyTag;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.CommitDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.dataaccess.impl.TagDAOImpl;
import com.qvcsos.server.dataaccess.impl.UserDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.Project;
import com.qvcsos.server.datamodel.Tag;
import com.qvcsos.server.datamodel.User;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestApplyTag implements ClientRequestInterface {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestApplyTag.class);

    private final ClientRequestApplyTagData request;

    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;

    public ClientRequestApplyTag(ClientRequestApplyTagData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();

        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseInterface returnObject = null;
        try {
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(request.getProjectName());

            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            String branchName = request.getBranchName();
            if (branchName.length() == 0) {
                branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
            }
            Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);

            UserDAO userDAO = new UserDAOImpl(schemaName);
            User user = userDAO.findByUserName(userName);

            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            Commit commit = new Commit();
            commit.setUserId(user.getId());
            String commitMessage = "Creating tag: [" + request.getTag() + "]";
            commit.setCommitMessage(commitMessage);
            Date now = new Date();
            Timestamp timestamp = new Timestamp(now.getTime());
            commit.setCommitDate(timestamp);
            Integer commitId = commitDAO.insert(commit);

            TagDAO tagDAO = new TagDAOImpl(schemaName);
            Tag newTag = new Tag();
            newTag.setBranchId(branch.getId());
            newTag.setMoveableFlag(request.getMoveableTagFlag());
            newTag.setTagText(request.getTag());
            newTag.setDescription(request.getDescription());
            newTag.setCommitId(commitId);
            Integer tagId = tagDAO.insert(newTag);
            DatabaseManager.getInstance().getConnection().commit();
            LOGGER.info("Added tag: [{}] with id: [{}]", request.getTag(), tagId);

            // Send back tag info.
            ServerResponseApplyTag serverResponseApplyTag = new ServerResponseApplyTag();
            serverResponseApplyTag.setTagText(request.getTag());
            serverResponseApplyTag.setDescription(request.getDescription());
            serverResponseApplyTag.setCommitId(commitId);
            serverResponseApplyTag.setTagId(tagId);
            returnObject = serverResponseApplyTag;
        } catch (SQLException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), request.getProjectName(), request.getBranchName(), "",
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

}
