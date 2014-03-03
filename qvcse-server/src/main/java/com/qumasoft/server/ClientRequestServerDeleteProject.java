//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.server;

import com.qumasoft.qvcslib.ClientRequestServerDeleteProjectData;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseListProjects;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delete a project.
 * @author Jim Voris
 */
public class ClientRequestServerDeleteProject implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestServerDeleteProjectData request;

    /**
     * Creates a new instance of ClientRequestServerDeleteProject.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerDeleteProject(ClientRequestServerDeleteProjectData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.log(Level.INFO, "User name: " + request.getUserName());

            // Need to re-authenticate this guy.
            if (AuthenticationManager.getAuthenticationManager().authenticateUser(request.getUserName(), request.getPassword())) {
                // The user is authenticated.  Make sure they are the ADMIN user -- that is the only
                // user allowed to create a project
                if (RoleManagerInterface.ADMIN_ROLE.getRoleType().equals(request.getUserName())) {
                    // We authenticated this guy, and he is the ADMIN user for this server.
                    // So it is okay to create a project.
                    returnObject = deleteProject(response);
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError(request.getUserName() + " is not authorized to delete a project on this server", null, null, null);
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Failed to authenticate: " + request.getUserName(), null, null, null);
                returnObject = error;
            }
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user " + request.getUserName(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }

    private ServerResponseInterface deleteProject(ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectPropertiesFilename = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_PROPERTIES_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_SERVED_PROJECTNAME_PREFIX + request.getDeleteProjectName() + ".properties";
        File projectPropertiesFile = new File(projectPropertiesFilename);
        if (projectPropertiesFile.exists()) {
            // We need to delete all the views for this project...
            Collection<ProjectView> views = ViewManager.getInstance().getViews(request.getDeleteProjectName());
            if (views != null) {
                Iterator<ProjectView> projectViewIterator = views.iterator();
                while (projectViewIterator.hasNext()) {
                    ProjectView projectView = projectViewIterator.next();
                    projectViewIterator.remove();
                    ViewManager.getInstance().removeView(projectView, response);
                }
            }

            if (projectPropertiesFile.delete()) {
                // Now, we need to remove roles for all project users.
                RoleManager.getRoleManager().removeAllProjectRoles(request.getDeleteProjectName(), request.getUserName());

                // Add an entry to the server journal file.
                ActivityJournalManager.getInstance().addJournalEntry("Deleted project '" + request.getDeleteProjectName()
                        + "'. All user roles removed. Project archives must be removed manually.");
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete project properties file.");
            }
        } else {
            // The project properties file is already gone...
            LOGGER.log(Level.WARNING, "Failed to delete non-existant project properties file for project '" + request.getDeleteProjectName() + "'.");
        }
        ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
        listProjectsResponse.setServerName(request.getServerName());
        ClientRequestServerListProjects.getServedProjectsList(listProjectsResponse);
        returnObject = listProjectsResponse;
        return returnObject;
    }
}
