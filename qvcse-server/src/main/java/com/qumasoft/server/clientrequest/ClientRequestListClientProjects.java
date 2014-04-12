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
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.server.RolePrivilegesManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List client projects.
 * @author Jim Voris
 */
public class ClientRequestListClientProjects implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestListClientProjectsData request;

    /**
     * Creates a new instance of ClientLoginRequest.
     *
     * @param data instance of super class that contains command line arguments, etc.
     */
    public ClientRequestListClientProjects(ClientRequestListClientProjectsData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface responseFactory) {
        ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
        listProjectsResponse.setServerName(responseFactory.getServerName());

        String[] servedProjectsList;
        Properties[] servedProjectsProperties;

        // Where all the property files can be found...
        File propertiesDirectory = new File(System.getProperty("user.dir")
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_PROPERTIES_DIRECTORY);

        QVCSServedProjectNamesFilter servedProjectNamesFilter = new QVCSServedProjectNamesFilter();
        File[] servedProjectFiles = propertiesDirectory.listFiles(servedProjectNamesFilter);
        List<Properties> servedProjectsPropertiesVector = new ArrayList<>();
        List<String> servedProjectsNamesVector = new ArrayList<>();
        if (servedProjectFiles != null) {
            // Put the collection in alphabetical order.
            Map<String, File> alphabeticalFileMap = new TreeMap<>();
            for (File servedProjectFile : servedProjectFiles) {
                String projectName = servedProjectNamesFilter.getProjectName(servedProjectFile.getName());
                alphabeticalFileMap.put(projectName, servedProjectFile);
            }
            int i = 0;
            for (File projectFile : alphabeticalFileMap.values()) {
                String projectName = servedProjectNamesFilter.getProjectName(projectFile.getName());

                // Only return info on this project if the user has read access
                // to the project.
                if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, responseFactory.getUserName(), RolePrivilegesManager.GET)) {
                    try {
                        ServedProjectProperties projectProperties = new ServedProjectProperties(projectName);
                        servedProjectsPropertiesVector.add(projectProperties.getProjectProperties());
                        servedProjectsNamesVector.add(projectProperties.getProjectName());
                    } catch (QVCSException e) {
                        LOGGER.log(Level.WARNING, "Error finding served project names for project: '" + projectName + "'.");
                    } finally {
                        i++;
                    }
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
