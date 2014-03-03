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
package com.qumasoft.qvcslib;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Archive Directory Manager Factory.
 *
 * @author Jim Voris
 */
public final class ArchiveDirManagerFactory {
    // This is a singleton.
    private static final ArchiveDirManagerFactory ARCHIVE_DIR_MANAGER_FACTORY = new ArchiveDirManagerFactory();
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
    private final Map<String, ArchiveDirManagerInterface> directoryManagerMap;
    private final Map<String, AbstractProjectProperties> projectPropertiesMap;
    private final Map<String, String> serverPasswordsMap;
    private final Map<String, String> serverUsersMap;

    /**
     * Creates a new instance of ArchiveDirManagerFactory.
     */
    private ArchiveDirManagerFactory() {
        directoryManagerMap = Collections.synchronizedMap(new TreeMap<String, ArchiveDirManagerInterface>());
        projectPropertiesMap = Collections.synchronizedMap(new TreeMap<String, AbstractProjectProperties>());
        serverPasswordsMap = Collections.synchronizedMap(new TreeMap<String, String>());
        serverUsersMap = Collections.synchronizedMap(new TreeMap<String, String>());
    }

    /**
     * Get the factory singleton.
     * @return the factory singleton.
     */
    public static ArchiveDirManagerFactory getInstance() {
        return ARCHIVE_DIR_MANAGER_FACTORY;
    }

    /**
     * Create the archive directory manager for the given parameters.
     * @param serverName the server name.
     * @param directoryCoordinate the directory coordinate.
     * @param projectType the project type.
     * @param projectProperties the project properties.
     * @param userName the user name.
     * @param discardObsoleteFilesFlag true if we should discard obsolete files (this is only used when first porting C++ flavor archives to QVCS-Enterprise use).
     * @return the archive dir manager interface for the given parameters.
     * @throws QVCSException if we have problems.
     */
    public synchronized ArchiveDirManagerInterface getDirectoryManager(String serverName, DirectoryCoordinate directoryCoordinate,
                                                                       String projectType, AbstractProjectProperties projectProperties, String userName,
                                                                       boolean discardObsoleteFilesFlag) throws QVCSException {
        projectPropertiesMap.put(getPropertiesViewKey(serverName, directoryCoordinate.getProjectName(), directoryCoordinate.getViewName(), projectType), projectProperties);
        return getArchiveDirectoryManager(serverName, directoryCoordinate.getProjectName(), projectProperties, directoryCoordinate.getViewName(),
                directoryCoordinate.getAppendedPath());
    }

    /**
     * Discard any directory manager references associated with the given parameters.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     */
    public synchronized void discardViewDirectoryManagers(String serverName, String projectName, String viewName) {
        String keyPrefix;
        if (serverName.length() > 0) {
            keyPrefix = serverName + ":" + projectName + ":" + viewName + "//";
        } else {
            keyPrefix = projectName + ":" + viewName + "//";
        }
        Iterator<String> it = directoryManagerMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key.startsWith(keyPrefix)) {
                it.remove();
            }
        }
    }

    /**
     * Reset the directory map. Basically discard any references that we're holding of archive directory managers.
     */
    public void resetDirectoryMap() {
        directoryManagerMap.clear();
    }

    private ArchiveDirManagerInterface getArchiveDirectoryManager(String serverName, String projectName, AbstractProjectProperties projectProperties, String viewName,
                                                           String appendedPath) throws QVCSException {
        String keyValue = getProjectViewKey(serverName, projectName, viewName, projectProperties, appendedPath);
        LOGGER.log(Level.FINE, "ArchiveDirManagerFactory.getDirectoryManager: Getting directory manager for: " + keyValue);
        ArchiveDirManagerInterface directoryManager = directoryManagerMap.get(keyValue);
        if (directoryManager == null) {
            // There is no directoryManager for this archive directory yet.
            // We'll need to make one.  Figure out which kind we need --
            // a proxy or a local directory manager.
            if (projectProperties.isRemoteProject()) {
                // We're running on the client...
                LOGGER.log(Level.FINE, "ArchiveDirManagerFactory.getDirectoryManager: creating ArchiveDirManagerProxy for directory " + appendedPath);

                // Get the password for this project.  The GUI should have set this via a call to setProjectPassword
                // before calling the factory to build the ArchiveDirManager.
                String serverPassword = serverPasswordsMap.get(serverName);
                String userName = serverUsersMap.get(serverName);
                directoryManager = new ArchiveDirManagerProxy(serverName, projectProperties, projectName, viewName, userName, serverPassword, appendedPath);
            }

            if (directoryManager != null) {
                directoryManagerMap.put(keyValue, directoryManager);
            }
        } else {
            LOGGER.log(Level.FINE, "Re-using existing directory manager for " + keyValue);
        }
        return directoryManager;
    }

    /**
     * Get the project properties.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param projectType the project type.
     * @return the project's project properties.
     * @throws QVCSException if we cannot find or manufacture the project properties using the given parameters.
     */
    public synchronized AbstractProjectProperties getProjectProperties(String serverName, String projectName, String viewName, String projectType) throws QVCSException {
        String propertiesKey = getPropertiesViewKey(serverName, projectName, viewName, projectType);
        AbstractProjectProperties projectProperties = projectPropertiesMap.get(propertiesKey);
        if (projectProperties == null) {
            // There is no project properties for this project yet.
            // We'll need to make one.
            projectProperties = ProjectPropertiesFactory.getProjectPropertiesFactory().buildProjectProperties(projectName, projectType);
            if (projectProperties == null) {
                throw new QVCSException("Failed to build " + projectType + " project properties for project: " + projectName);
            }
            projectPropertiesMap.put(propertiesKey, projectProperties);
        }
        return projectProperties;
    }

    /**
     * Remove a directory manager. Removes the directory manager associated with the given parameters from the cache that the factory maintains.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param projectType the type of project.
     * @param appendedPath the appended path.
     */
    public synchronized void removeDirectoryManager(String serverName, String projectName, String viewName, String projectType, String appendedPath) {
        String propertiesKey = getPropertiesViewKey(serverName, projectName, viewName, projectType);
        AbstractProjectProperties projectProperties = projectPropertiesMap.get(propertiesKey);
        if (projectProperties != null) {
            String keyValue = getProjectViewKey(serverName, projectName, viewName, projectProperties, appendedPath);
            LOGGER.log(Level.FINE, "ArchiveDirManagerFactory.removeDirectoryManager: removing directory manager for: " + keyValue);
            directoryManagerMap.remove(keyValue);
            if (appendedPath.length() == 0) {
                serverPasswordsMap.remove(serverName);
                serverUsersMap.remove(serverName);
            }
        }
    }

    /**
     * Set the password to use for the given server.
     * @param serverName the server name.
     * @param password the password.
     */
    public void setServerPassword(String serverName, String password) {
        serverPasswordsMap.put(serverName, password);
    }

    /**
     * Set the user name associated with the given server name.
     * @param serverName the server name.
     * @param username the user name to associate with that server.
     */
    public void setServerUsername(String serverName, String username) {
        serverUsersMap.put(serverName, username);
    }

    private String getPropertiesViewKey(String serverName, String projectName, String viewName, String projectType) {
        String keyValue = serverName + "." + projectType + "." + projectName + "." + viewName;

        if (0 == projectType.compareTo((QVCSConstants.QVCS_SERVED_PROJECT_TYPE))) {
            keyValue = projectType + "." + projectName + "." + viewName;
        }
        return keyValue;
    }

    private String getProjectViewKey(String serverName, String projectName, String viewName, AbstractProjectProperties projectProperties, String appendedPath) {
        // Make this a standard appended path...
        String standardAppendedPath = Utility.convertToStandardPath(appendedPath);

        if (projectProperties.getIgnoreCaseFlag()) {
            standardAppendedPath = standardAppendedPath.toLowerCase();
        }

        String keyValue = serverName + ":" + projectName + ":" + viewName + "//" + projectProperties.getProjectType() + ":" + standardAppendedPath;
        if (0 == projectProperties.getProjectType().compareTo(QVCSConstants.QVCS_SERVED_PROJECT_TYPE)) {
            // If we're running on the server, the key value must not include the server name, since it may vary.
            // NOTE: IF THIS CHANGES, YOU HAVE TO CHANGE CODE IN ViewManager.removeView() SINCE THAT
            // CODE ASSUMES THAT A BLANK SERVERNAME WILL WORK WHEN RUNNING ON THE SERVER.
            keyValue = projectName + ":" + viewName + "//" + projectProperties.getProjectType() + ":" + standardAppendedPath;
        }

        return keyValue;
    }
}
