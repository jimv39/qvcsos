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
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.server.RolePrivilegesManager;
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
 * List client projects.
 * @author Jim Voris
 */
public class ClientRequestListClientProjects implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestListClientProjects.class);
    private final ClientRequestListClientProjectsData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientLoginRequest.
     *
     * @param data instance of super class that contains command line arguments, etc.
     */
    public ClientRequestListClientProjects(ClientRequestListClientProjectsData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface responseFactory) {
        ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
        listProjectsResponse.setServerName(responseFactory.getServerName());

        String[] servedProjectsList;
        Properties[] servedProjectsProperties;

        List<Properties> servedProjectsPropertiesVector = new ArrayList<>();
        List<String> servedProjectsNamesVector = new ArrayList<>();
        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        List<Project> projectList = projectDAO.findAll();
        if (projectList != null && projectList.size() > 0) {
            for (Project projectFile : projectList) {
                String projectName = projectFile.getProjectName();

                // Only return info on this project if the user has read access
                // to the project.
                if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, responseFactory.getUserName(), RolePrivilegesManager.GET)) {
                    Properties projectProperties = new Properties();
                    // TODO
                    projectProperties.setProperty("BRANCH_TYPE", QVCSConstants.QVCS_YES);
                    servedProjectsPropertiesVector.add(projectProperties);
                    servedProjectsNamesVector.add(projectName);
                }
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

        return listProjectsResponse;
    }
}
