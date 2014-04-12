/*   Copyright 2004-2014 Jim Voris
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
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSServedProjectNamesFilter;
import com.qumasoft.qvcslib.ServedProjectProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectsData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List the projects that exist. This is used by the Admin tool to show all the projects on the server.
 *
 * @author Jim Voris
 */
public class ClientRequestServerListProjects implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestServerListProjectsData request;

    /**
     * Creates a new instance of ClientRequestServerListProjects.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerListProjects(ClientRequestServerListProjectsData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;

        try {
            LOGGER.log(Level.INFO, "ClientRequestServerListProjects.execute user: [" + userName + "] attempting to list projects.");

            ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
            listProjectsResponse.setServerName(request.getServerName());
            getServedProjectsList(listProjectsResponse);
            returnObject = listProjectsResponse;
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "ClientRequestServerListProjects.execute exception: " + e.getClass().getName() + ":" + e.getLocalizedMessage());
        }
        return returnObject;
    }

    static void getServedProjectsList(ServerResponseListProjects listProjectsResponse) {
        String[] servedProjectsList;
        Properties[] servedProjectsProperties;

        // Where all the property files can be found...
        File propertiesDirectory = new File(System.getProperty("user.dir")
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_PROPERTIES_DIRECTORY);

        QVCSServedProjectNamesFilter servedProjectNamesFilter = new QVCSServedProjectNamesFilter();
        File[] servedProjectFiles = propertiesDirectory.listFiles(servedProjectNamesFilter);
        if (servedProjectFiles != null) {
            servedProjectsProperties = new Properties[servedProjectFiles.length];
            servedProjectsList = new String[servedProjectFiles.length];

            // Put the collection in alphabetical order.
            Map<String, File> alphabeticalFileMap = new TreeMap<>();
            for (File servedProjectFile : servedProjectFiles) {
                String projectName = servedProjectNamesFilter.getProjectName(servedProjectFile.getName());
                alphabeticalFileMap.put(projectName, servedProjectFile);
            }

            int i = 0;
            for (File projectFile : alphabeticalFileMap.values()) {
                String projectName = servedProjectNamesFilter.getProjectName(projectFile.getName());
                try {
                    ServedProjectProperties projectProperties = new ServedProjectProperties(projectName);
                    servedProjectsProperties[i] = projectProperties.getProjectProperties();
                    servedProjectsList[i] = projectProperties.getProjectName();
                } catch (QVCSException e) {
                    LOGGER.log(Level.WARNING, "Error finding served project names for project: [" + projectName + "].");
                } finally {
                    i++;
                }
            }
        } else {
            servedProjectsProperties = new Properties[0];
            servedProjectsList = new String[0];
        }
        listProjectsResponse.setProjectList(servedProjectsList);
        listProjectsResponse.setPropertiesList(servedProjectsProperties);
    }
}
