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

import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteProjectData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.QVCSShutdownException;
import com.qumasoft.server.RoleManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a project.
 * @author Jim Voris
 */
public class ClientRequestServerDeleteProject implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerDeleteProject.class);
    private final ClientRequestServerDeleteProjectData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestServerDeleteProject.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerDeleteProject(ClientRequestServerDeleteProjectData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.info("User name: [{}]", request.getUserName());

            // Need to re-authenticate this guy.
            if (AuthenticationManager.getAuthenticationManager().authenticateUser(request.getUserName(), request.getPassword())) {
                // The user is authenticated.  Make sure they are the ADMIN user -- that is the only
                // user allowed to create a project
                if (RoleManager.ADMIN.equals(request.getUserName())) {
                    // We authenticated this guy, and he is the ADMIN user for this server.
                    // So it is okay to delete a project.
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
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user " + request.getUserName(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }

    private ServerResponseInterface deleteProject(ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(request.getDeleteProjectName());
        if (project != null) {
            // We don't really delete the project at all... we just remove all user access to the
            // project except for the ADMIN user.

            // Now, we need to remove roles for all project users.
            RoleManager.getRoleManager().removeAllProjectRoles(request.getDeleteProjectName(), request.getUserName());

            // Add an entry to the server journal file.
            ActivityJournalManager.getInstance().addJournalEntry("Deleted project [" + request.getDeleteProjectName()
                    + "]. All user roles removed. Project file history must be removed manually.");
        } else {
            // The project row is already gone...
            LOGGER.warn("Failed to delete non-existant project [" + request.getDeleteProjectName() + "].");
        }
        ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
        listProjectsResponse.setServerName(request.getServerName());
        ClientRequestServerListProjects.getServedProjectsList(listProjectsResponse);
        returnObject = listProjectsResponse;
        return returnObject;
    }
}
