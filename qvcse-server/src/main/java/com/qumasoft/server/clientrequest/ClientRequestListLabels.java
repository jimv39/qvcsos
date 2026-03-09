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
import com.qumasoft.qvcslib.requestdata.ClientRequestListLabelsData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListLabels;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.LabelDAO;
import com.qvcsos.server.dataaccess.impl.LabelDAOImpl;
import com.qvcsos.server.datamodel.Label;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestListLabels extends AbstractClientRequest {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestListLabels.class);

    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;

    public ClientRequestListLabels(ClientRequestListLabelsData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();

        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject;
        LabelDAO labelDAO = new LabelDAOImpl(schemaName);
        List<Label> labelList = labelDAO.findAll();
        List<CommonLabel> labelTextList = new ArrayList<>();
        for (Label label : labelList) {
            CommonLabel commonLabel = new CommonLabel(label.getLabelId(), label.getLabelText());
            labelTextList.add(commonLabel);
        }

        // Send back label info.
        ServerResponseListLabels serverResponseListLabels = new ServerResponseListLabels();
        serverResponseListLabels.setLabelList(labelTextList);
        returnObject = serverResponseListLabels;
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }

}
