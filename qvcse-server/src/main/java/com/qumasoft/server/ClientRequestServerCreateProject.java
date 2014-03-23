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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateProjectData;
import com.qumasoft.qvcslib.ProjectPropertiesFactory;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.dataaccess.impl.BranchDAOImpl;
import com.qumasoft.server.dataaccess.impl.ProjectDAOImpl;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Project;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create a project.
 * @author Jim Voris
 */
public class ClientRequestServerCreateProject implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestServerCreateProjectData request;
    /** So we can write the new project to the database */
    private ProjectDAO projectDAO = null;
    private BranchDAO branchDAO = null;

    /**
     * Creates a new instance of ClientRequestServerShutdown.
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestServerCreateProject(ClientRequestServerCreateProjectData data) {
        request = data;
        this.projectDAO = new ProjectDAOImpl();
        this.branchDAO = new BranchDAOImpl();
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
                    returnObject = createProject();
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError(request.getUserName() + " is not authorized to create a project on this server", null, null, null);
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

    private ServerResponseInterface createProject() {
        ServerResponseInterface returnObject = null;
        try {
            String projectPropertiesFilename = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_PROPERTIES_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_SERVED_PROJECTNAME_PREFIX + request.getNewProjectName() + ".properties";
            File projectPropertiesFile = new File(projectPropertiesFilename);

            // Make sure the properties directory exists...
            if (!projectPropertiesFile.getParentFile().exists()) {
                projectPropertiesFile.getParentFile().mkdirs();
            }

            // Make sure the property file exists. (This should create it.)
            if (projectPropertiesFile.createNewFile()) {
                AbstractProjectProperties projectProperties = ProjectPropertiesFactory.getProjectPropertiesFactory().buildProjectProperties(request.getNewProjectName(),
                        QVCSConstants.QVCS_SERVED_PROJECT_TYPE);

                // This is where the archives go...
                String projectLocation = System.getProperty("user.dir")
                        + File.separator
                        + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                        + File.separator
                        + request.getNewProjectName();

                // This the root directory for the archives for this project.
                projectProperties.setArchiveLocation(projectLocation);

                // Make sure the directory exists.
                File projectDirectory = new File(projectLocation);
                projectDirectory.mkdirs();

                // Set the project info for the reference copies
                projectProperties.setCreateReferenceCopyFlag(request.getCreateReferenceCopyFlag());
                if (request.getCreateReferenceCopyFlag()) {
                    String referenceLocation;
                    boolean defineAlternateReferenceLocationFlag = false;

                    if (request.getDefineAlternateReferenceLocationFlag()) {
                        defineAlternateReferenceLocationFlag = true;
                        referenceLocation = request.getAlternateReferenceLocation();
                    } else {
                        // This is where the reference files go...
                        referenceLocation = System.getProperty("user.dir")
                                + File.separator
                                + QVCSConstants.QVCS_REFERENCECOPY_DIRECTORY
                                + File.separator
                                + request.getNewProjectName();
                    }

                    // Make sure the directory exists.
                    File referenceDirectory = new File(referenceLocation);
                    referenceDirectory.mkdirs();

                    // This the reference directory for this project.
                    projectProperties.setReferenceLocation(referenceLocation);
                }

                // Set the ignore case flag.
                projectProperties.setIgnoreCaseFlag(request.getIgnoreCaseFlag());

                projectProperties.saveProperties();

                // Now, we need to add roles for this user so they can administer the project.
                RoleManager.getRoleManager().addUserRole(request.getUserName(), request.getNewProjectName(), request.getUserName(), RoleManagerInterface.PROJECT_ADMIN_ROLE);

                Project project = new Project();
                project.setProjectName(request.getNewProjectName());
                Project existingProject = projectDAO.findByProjectName(request.getNewProjectName());
                if (existingProject == null) {
                    // Create the Project record...
                    projectDAO.insert(project);
                    Project foundProject = projectDAO.findByProjectName(request.getNewProjectName());

                    // Create the Trunk branch for the new project.
                    Branch branch = new Branch();
                    branch.setBranchName(QVCSConstants.QVCS_TRUNK_VIEW);
                    branch.setBranchTypeId(1);
                    branch.setProjectId(foundProject.getProjectId());
                    branchDAO.insert(branch);
                }

                // The reply is the new list of projects.
                ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
                listProjectsResponse.setServerName(request.getServerName());
                ClientRequestServerListProjects.getServedProjectsList(listProjectsResponse);
                returnObject = listProjectsResponse;

                // Add an entry to the server journal file.
                ActivityJournalManager.getInstance().addJournalEntry("Created new project named '" + request.getNewProjectName() + "'.");
            } else {
                // The project already exists... don't create it again.
                LOGGER.log(Level.INFO, "Project: '" + request.getNewProjectName() + "' already exists.");
            }
        } catch (IOException | SQLException e) {
            LOGGER.log(Level.WARNING, "Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());

            // Return an error.
            ServerResponseError error = new ServerResponseError("Caught exception trying create project properties: " + e.getLocalizedMessage(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }
}
