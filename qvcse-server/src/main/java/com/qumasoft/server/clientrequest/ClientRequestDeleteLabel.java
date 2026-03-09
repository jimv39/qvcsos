/*
 * Copyright 2026 Jim Voris.
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

import com.qumasoft.qvcslib.CommonLabel;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteLabelData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListLabels;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.LabelDAO;
import com.qvcsos.server.dataaccess.impl.LabelDAOImpl;
import com.qvcsos.server.datamodel.Label;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestDeleteLabel extends AbstractClientRequest {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestApplyTag.class);

    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;

    public ClientRequestDeleteLabel(ClientRequestDeleteLabelData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();

        setRequest(data);
    }


    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        String labelText = getRequest().getLabelText();
        boolean successFlag = false;
        AbstractServerResponse returnObject;
        try {
            LabelDAO labelDAO = new LabelDAOImpl(schemaName);
            Label label = labelDAO.findByLabelText(labelText);

            successFlag = labelDAO.delete(label);
            DatabaseManager.getInstance().getConnection().commit();

            if (successFlag) {
                LOGGER.info("Deleted label: [{}] with id: [{}]", labelText, label.getLabelId());
                // Send back the remaining list of labels.
                List<Label> labelList = labelDAO.findAll();
                List<CommonLabel> commonLabelList = new ArrayList<>();
                for (Label retainedLabel : labelList) {
                    CommonLabel commonLabel = new CommonLabel(retainedLabel.getLabelId(), retainedLabel.getLabelText());
                    commonLabelList.add(commonLabel);
                }
                ServerResponseListLabels serverResponseListLabels = new ServerResponseListLabels();
                serverResponseListLabels.setLabelList(commonLabelList);
                returnObject = serverResponseListLabels;
            } else {
                // Send a response message so the client can treat this as a synchronous request.
                ServerResponseMessage message = new ServerResponseMessage("Delete of label failed.", getRequest().getProjectName(), getRequest().getBranchName(), "",
                        ServerResponseMessage.HIGH_PRIORITY);
                returnObject = message;
            }
        } catch (SQLException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), getRequest().getProjectName(), getRequest().getBranchName(), "",
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }

}
