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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerMaintainProjectData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.LogFile;
import com.qumasoft.server.QVCSShutdownException;
import com.qumasoft.server.RolePrivilegesManager;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintain project data.
 * @author Jim Voris
 */
public class ClientRequestServerMaintainProject implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerMaintainProject.class);
    private final ClientRequestServerMaintainProjectData request;
    private static final Map<String, Object> SYNCHRONIZATION_MAP = Collections.synchronizedMap(new TreeMap<String, Object>());

    /**
     * Creates a new instance of ClientRequestServerMaintainProject.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerMaintainProject(ClientRequestServerMaintainProjectData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String requestUserName = request.getUserName();
        byte[] password = request.getPassword();
        try {
            LOGGER.info("User name: [{}]", requestUserName);

            // Need to re-authenticate this guy.
            if (AuthenticationManager.getAuthenticationManager().authenticateUser(requestUserName, password)) {
                // The user is authenticated.  Make sure they have the right privilege.
                if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, requestUserName, RolePrivilegesManager.SERVER_MAINTAIN_PROJECT)) {
                    // We authenticated this guy, and he is a PROJECT_ADMIN user for this project...
                    // So it is okay to maintain the requested project.
                    returnObject = maintainProject(response);
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError(requestUserName + " is not authorized to maintain project " + projectName, null, null, null);
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Failed to authenticate: " + requestUserName, null, null, null);
                returnObject = error;
            }
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        }
        return returnObject;
    }

    private ServerResponseInterface maintainProject(ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        try {
            AbstractProjectProperties projectProperties = ArchiveDirManagerFactoryForServer.getInstance().getProjectProperties(request.getServerName(), projectName, viewName,
                    QVCSConstants.QVCS_SERVED_PROJECT_TYPE);

            // Get the current value of the create reference copy flag.
            boolean existingReferenceCopyFlag = projectProperties.getCreateReferenceCopyFlag();
            boolean existingIgnoreCaseFlag = projectProperties.getIgnoreCaseFlag();
            boolean existingDefineAlternateLocationForReferenceFilesFlag = projectProperties.getDefineAlternateReferenceLocationFlag();
            String existingReferenceLocation = projectProperties.getReferenceLocation();

            // See if we have any work to do here...
            if ((request.getCreateReferenceCopyFlag() != existingReferenceCopyFlag)
                    || (request.getIgnoreCaseFlag() != existingIgnoreCaseFlag)
                    || (request.getDefineAlternateReferenceLocationFlag() != existingDefineAlternateLocationForReferenceFilesFlag)
                    || (request.getAlternateReferenceLocation().compareTo(existingReferenceLocation) != 0)
                    || request.getCreateOrDeleteCurrentReferenceFilesFlag()) {
                projectProperties.setCreateReferenceCopyFlag(request.getCreateReferenceCopyFlag());

                // This is where the reference files go...
                String referenceLocation;
                if (request.getDefineAlternateReferenceLocationFlag()) {
                    referenceLocation = request.getAlternateReferenceLocation();
                } else {
                    referenceLocation = System.getProperty("user.dir")
                            + File.separator
                            + QVCSConstants.QVCS_REFERENCECOPY_DIRECTORY
                            + File.separator
                            + projectName;
                }

                // Make sure the directory exists.
                File referenceDirectory = new File(referenceLocation);
                referenceDirectory.mkdirs();

                if (request.getCreateReferenceCopyFlag()) {
                    String activity = "Enabling create reference copies for project: [" + projectName + "]";
                    LOGGER.info(activity);

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(activity);

                    // This the reference directory for this project.
                    projectProperties.setReferenceLocation(referenceLocation);

                    if (request.getCreateOrDeleteCurrentReferenceFilesFlag()) {
                        // Create the entire tree of reference copies.
                        LOGGER.info("Creating all reference copies for project: [{}]", projectName);
                        createReferenceCopies(referenceDirectory, projectProperties, response);
                    }
                } else {
                    String activity = "Disabling create reference copies for project: [" + projectName + "]";
                    LOGGER.info(activity);

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(activity);

                    if (request.getCreateOrDeleteCurrentReferenceFilesFlag()) {
                        // Delete the entire tree of reference copies.
                        LOGGER.info("Deleting all reference copies for project: [{}]", projectName);
                        File existingReferenceDirectory = new File(existingReferenceLocation);
                        deleteReferenceCopies(existingReferenceDirectory, projectProperties);
                    }
                }

                projectProperties.setIgnoreCaseFlag(request.getIgnoreCaseFlag());
                if (request.getIgnoreCaseFlag()) {
                    String activity = "Setting ignore case flag for project: [" + projectName + "]";
                    LOGGER.info(activity);

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(activity);
                } else {
                    String activity = "Clearing ignore case flag for project: [" + projectName + "]";
                    LOGGER.info(activity);

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(activity);
                }

                // Write the change to disk.
                projectProperties.saveProperties();

                // The reply is the new list of projects.
                ServerResponseListProjects listProjectsResponse = new ServerResponseListProjects();
                listProjectsResponse.setServerName(request.getServerName());
                ClientRequestServerListProjects.getServedProjectsList(listProjectsResponse);
                returnObject = listProjectsResponse;
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return an error.
            ServerResponseError error = new ServerResponseError("Caught exception trying change project properties: " + e.getLocalizedMessage(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }

    private void createReferenceCopies(final File referenceDirectory, final AbstractProjectProperties projectProperties, final ServerResponseFactoryInterface response) {
        Runnable worker = () -> {
            Object projectLockObject = lookupProjectLockObject(projectProperties);
            synchronized (projectLockObject) {
                fetchReferenceCopiesForDirectory("", referenceDirectory, projectProperties, response);
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }

    private void deleteReferenceCopies(final File referenceDirectory, final AbstractProjectProperties projectProperties) {
        Runnable worker = () -> {
            Object projectLockObject = lookupProjectLockObject(projectProperties);
            synchronized (projectLockObject) {
                removeDirectory(referenceDirectory);
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }

    private Object lookupProjectLockObject(AbstractProjectProperties projectProperties) {
        Object lockObject;
        synchronized (SYNCHRONIZATION_MAP) {
            lockObject = SYNCHRONIZATION_MAP.get(projectProperties.getProjectName());
            if (lockObject == null) {
                lockObject = new Object();
                SYNCHRONIZATION_MAP.put(projectProperties.getProjectName(), lockObject);
            }
        }
        return lockObject;
    }

    private void removeDirectory(File directory) {
        LOGGER.info("Removing reference files in directory: [{}]", directory.getAbsolutePath());

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            file.delete();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                removeDirectory(file);
            }
        }

        directory.delete();
    }

    private void fetchReferenceCopiesForDirectory(String appendedPath, File referenceBaseDirectory, AbstractProjectProperties projectProperties,
                                                                                                    ServerResponseFactoryInterface response) {
        LOGGER.info("Creating reference copies for files in directory: [" + appendedPath + "] for project: [" + projectProperties.getProjectName() + "]");
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(request.getProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, appendedPath);
            ArchiveDirManager archiveDirManager = (ArchiveDirManager) ArchiveDirManagerFactoryForServer.getInstance()
                    .getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, directoryCoordinate,
                    QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);

            File archiveDirectory = new File(archiveDirManager.getArchiveDirectoryName());

            File[] files = archiveDirectory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (appendedPath.length() == 0) {
                        if (0 == file.getName().compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                            continue;
                        }
                        if (0 == file.getName().compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                            continue;
                        }
                        if (0 == file.getName().compareTo(QVCSConstants.QVCS_DIRECTORY_METADATA_DIRECTORY)) {
                            continue;
                        }
                    }
                    String adjustedAppendedPath = appendedPath + File.separator + file.getName();
                    if (appendedPath.length() == 0) {
                        adjustedAppendedPath = file.getName();
                    }
                    fetchReferenceCopiesForDirectory(adjustedAppendedPath, referenceBaseDirectory, projectProperties, response);
                }
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }
                if (0 == file.getName().compareTo(QVCSConstants.QVCS_DIRECTORYID_FILENAME)) {
                    continue;
                }
                String workfileName = Utility.convertArchiveNameToShortWorkfileName(file.getName());
                ArchiveInfoInterface archiveInfo = archiveDirManager.getArchiveInfo(workfileName);
                if (archiveInfo != null) {
                    LogFile logfile = (LogFile) archiveInfo;

                    LOGGER.trace("Creating reference copies for: [{}]", logfile.getShortWorkfileName());
                    byte[] buffer = logfile.getRevisionAsByteArray(logfile.getDefaultRevisionString());
                    archiveDirManager.createReferenceCopy(projectProperties, logfile, buffer);
                }
            }
        } catch (QVCSException e) {
            LOGGER.warn("Exception creating reference copies for: " + appendedPath + ". Exception: " + e.getClass().toString() + ". Message: "
                    + e.getLocalizedMessage());
        }
    }
}
