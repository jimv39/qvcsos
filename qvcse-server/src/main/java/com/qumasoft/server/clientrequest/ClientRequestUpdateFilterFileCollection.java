/*
 * Copyright 2023 Jim Voris.
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

import com.qumasoft.qvcslib.CommonFilterFile;
import com.qumasoft.qvcslib.CommonFilterFileCollection;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestUpdateFilterFileCollectionData;
import static com.qumasoft.qvcslib.requestdata.ClientRequestUpdateFilterFileCollectionData.ADD_FILE_FILTER_COLLECTION_REQUEST;
import static com.qumasoft.qvcslib.requestdata.ClientRequestUpdateFilterFileCollectionData.RESET_FILE_FILTER_COLLECTION_REQUEST;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseUpdateFilterFileCollection;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FilterFileCollectionDAO;
import com.qvcsos.server.dataaccess.FilterFileDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.FilterFileCollectionDAOImpl;
import com.qvcsos.server.dataaccess.impl.FilterFileDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.FilterFile;
import com.qvcsos.server.datamodel.FilterFileCollection;
import com.qvcsos.server.datamodel.Project;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestUpdateFilterFileCollection extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestUpdateViewUtilityCommand.class);
    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;
    private final ClientRequestUpdateFilterFileCollectionData clientRequestUpdateFilterFileCollectionData;

    public ClientRequestUpdateFilterFileCollection(ClientRequestUpdateFilterFileCollectionData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.clientRequestUpdateFilterFileCollectionData = data;
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        AbstractServerResponse returnObject;
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        try {
            ServerResponseUpdateFilterFileCollection srResponse = new ServerResponseUpdateFilterFileCollection();
            srResponse.setServerName(clientRequestUpdateFilterFileCollectionData.getServerName());
            switch (clientRequestUpdateFilterFileCollectionData.getUpdateType()) {
                case ADD_FILE_FILTER_COLLECTION_REQUEST:
                    returnObject = addCollections(srResponse);
                    break;
                case RESET_FILE_FILTER_COLLECTION_REQUEST:
                    returnObject = resetCollection(srResponse);
                    break;
                default:
                    throw new QVCSRuntimeException("Invalid request type.");
            }
        } catch (SQLException e) {
            ServerResponseError sqlError = new ServerResponseError(e.getLocalizedMessage(), "", "", "");
            returnObject = sqlError;
        }

        returnObject.setSyncToken(getRequest().getSyncToken());
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private AbstractServerResponse addCollections(ServerResponseUpdateFilterFileCollection srResponse) throws SQLException {
        List<CommonFilterFileCollection> cffcList = clientRequestUpdateFilterFileCollectionData.getCommonFilterFileCollectionList();
        FilterFileCollectionDAO filterFileCollectionDAO = new FilterFileCollectionDAOImpl(this.schemaName);
        FilterFileDAO filterFileDAO = new FilterFileDAOImpl(this.schemaName);
        ProjectDAO projectDAO = new ProjectDAOImpl(this.schemaName);
        for (CommonFilterFileCollection cffc : cffcList) {
            FilterFileCollection filterFileCollection = new FilterFileCollection();
            Project project = projectDAO.findByProjectName(cffc.getAssociatedProjectName());
            if (project != null) {
                filterFileCollection.setAssociatedProjectId(project.getId());
            }
            filterFileCollection.setCollectionName(cffc.getCollectionName());
            filterFileCollection.setBuiltInFlag(cffc.getBuiltInFlag());
            filterFileCollection.setUserId(sourceControlBehaviorManager.getUserId());
            Integer ffcId = filterFileCollectionDAO.insert(filterFileCollection);
            filterFileCollection.setId(ffcId);
            cffc.setId(ffcId);
            List<CommonFilterFile> cffList = cffc.getFilterFileList();
            for (CommonFilterFile cff : cffList) {
                FilterFile ff = new FilterFile();
                ff.setFilterCollectionId(ffcId);
                ff.setFilterData(cff.getFilterData());
                ff.setFilterTypeId(cff.getFilterTypeId());
                ff.setIsAndFlag(cff.getIsAndFlag());
                Integer ffId = filterFileDAO.insert(ff);
                ff.setId(ffId);
                cff.setId(ffId);
            }
        }
        srResponse.setCommonFilterFileCollectionList(cffcList);
        return srResponse;
    }

    private AbstractServerResponse resetCollection(ServerResponseUpdateFilterFileCollection srResponse) throws SQLException {
        // Reset to remove ALL non-built-in collections.
        FilterFileCollectionDAO filterFileCollectionDAO = new FilterFileCollectionDAOImpl(this.schemaName);
        FilterFileDAO filterFileDAO = new FilterFileDAOImpl(this.schemaName);
        List<FilterFileCollection> ffcList = filterFileCollectionDAO.findAllByUserId(sourceControlBehaviorManager.getUserId());
        for (FilterFileCollection ffc : ffcList) {
            if (!ffc.getBuiltInFlag()) {
                List<FilterFile> ffList = filterFileDAO.findByCollectionId(ffc.getId());
                for (FilterFile ff : ffList) {
                    filterFileDAO.delete(ff.getId());
                }
                filterFileCollectionDAO.delete(ffc.getId());
            }
        }
        List<CommonFilterFileCollection> cffcList = new ArrayList<>();
        // We return an empty list.
        srResponse.setCommonFilterFileCollectionList(cffcList);
        return srResponse;
    }

}
