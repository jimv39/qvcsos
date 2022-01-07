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
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectsData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Project;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List the projects that exist. This is used by the Admin tool to show all the projects on the server.
 *
 * @author Jim Voris
 */
public class ClientRequestServerListProjects implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerListProjects.class);
    private final ClientRequestServerListProjectsData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;
    private String userName;

    /**
     * Creates a new instance of ClientRequestServerListProjects.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerListProjects(ClientRequestServerListProjectsData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String user, ServerResponseFactoryInterface response) {
        this.userName = user;
        ServerResponseInterface returnObject = null;

        try {
            LOGGER.info("ClientRequestServerListProjects.execute user: [" + userName + "] attempting to list projects.");

            ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
            listProjectsResponse.setServerName(request.getServerName());
            getServedProjectsList(listProjectsResponse);
            returnObject = listProjectsResponse;
        } catch (Exception e) {
            LOGGER.warn("ClientRequestServerListProjects.execute exception: " + e.getClass().getName() + ":" + e.getLocalizedMessage());
        }
        return returnObject;
    }

    static void getServedProjectsList(ServerResponseListProjects listProjectsResponse) {
        String[] servedProjectsList;
        Properties[] servedProjectsProperties;

        List<Properties> servedProjectsPropertiesVector = new ArrayList<>();
        List<String> servedProjectsNamesVector = new ArrayList<>();
        ProjectDAO projectDAO = new ProjectDAOImpl(DatabaseManager.getInstance().getSchemaName());
        List<Project> projectList = projectDAO.findAll();
        if (projectList != null && projectList.size() > 0) {
            for (Project projectFile : projectList) {
                String projectName = projectFile.getProjectName();
                Properties projectProperties = new Properties();
                // TODO
                projectProperties.setProperty("BRANCH_TYPE", QVCSConstants.QVCS_YES);
                servedProjectsPropertiesVector.add(projectProperties);
                servedProjectsNamesVector.add(projectName);
            }
        }

        servedProjectsProperties = new Properties[servedProjectsPropertiesVector.size()];
        servedProjectsList = new String[servedProjectsPropertiesVector.size()];

        for (int i = 0; i < servedProjectsList.length; i++) {
            servedProjectsProperties[i] = servedProjectsPropertiesVector.get(i);
            servedProjectsList[i] = servedProjectsNamesVector.get(i);
        }

        listProjectsResponse.setProjectList(servedProjectsList);
        listProjectsResponse.setPropertiesList(servedProjectsProperties);
    }
}
