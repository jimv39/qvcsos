/*   Copyright 2004-2022 Jim Voris
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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateProjectData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.QVCSShutdownException;
import com.qumasoft.server.RoleManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a project.
 * @author Jim Voris
 */
public class ClientRequestServerCreateProject extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerCreateProject.class);

    /**
     * Creates a new instance of ClientRequestServerShutdown.
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestServerCreateProject(ClientRequestServerCreateProjectData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.info("User name: [{}]", getRequest().getUserName());

            // Need to re-authenticate this guy.
            if (AuthenticationManager.getAuthenticationManager().authenticateUser(getRequest().getUserName(), getRequest().getPassword())) {
                // The user is authenticated.  Make sure they are the ADMIN user -- that is the only
                // user allowed to create a project
                if (RoleManager.ADMIN.equals(getRequest().getUserName())) {
                    // We authenticated this guy, and he is the ADMIN user for this server.
                    // So it is okay to create a project.
                    returnObject = createProject(response);
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError(getRequest().getUserName() + " is not authorized to create a project on this server", null, null, null);
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Failed to authenticate: [" + getRequest().getUserName() + "]", null, null, null);
                returnObject = error;
            }
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user [" + getRequest().getUserName() + "]", null, null, null);
            returnObject = error;
        }
        return returnObject;
    }

    private ServerResponseInterface createProject(ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        ClientRequestServerCreateProjectData clientRequestServerCreateProjectData = (ClientRequestServerCreateProjectData) getRequest();
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(getRequest().getUserName(), response);
        try {
            RoleManager.getRoleManager().addUserRole(getRequest().getUserName(), clientRequestServerCreateProjectData.getNewProjectName(), getRequest().getUserName(),
                    RoleManager.getRoleManager().PROJECT_ADMIN_ROLE);

            // Create the project in the database.
            Integer projectId = sourceControlBehaviorManager.createProject(clientRequestServerCreateProjectData.getNewProjectName());
            LOGGER.info("Created project: [{}] returning project id: [{}]", clientRequestServerCreateProjectData.getNewProjectName(), projectId);

            // Give the ADMIN user ADMIN role for the project.
            RoleManager.getRoleManager().addUserRole(getRequest().getUserName(), clientRequestServerCreateProjectData.getNewProjectName(), getRequest().getUserName(),
                    RoleManager.getRoleManager().ADMIN_ROLE);

            // The reply is the new list of projects.
            ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
            listProjectsResponse.setServerName(getRequest().getServerName());
            ClientRequestServerListProjects.getServedProjectsList(listProjectsResponse);
            listProjectsResponse.setSyncToken(getRequest().getSyncToken());
            returnObject = listProjectsResponse;

            // Add an entry to the server journal file.
            ActivityJournalManager.getInstance().addJournalEntry("Created new project named [" + clientRequestServerCreateProjectData.getNewProjectName() + "].");
        } catch (SQLException e) {
            LOGGER.warn("Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());

            // Return an error.
            ServerResponseError error = new ServerResponseError("Caught exception trying create project: " + e.getLocalizedMessage(), null, null, null);
            error.setSyncToken(getRequest().getSyncToken());
            returnObject = error;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }
}
