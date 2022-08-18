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
package com.qumasoft.qvcslib;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive Directory Manager Factory.
 *
 * @author Jim Voris
 */
public final class ArchiveDirManagerFactory {
    // This is a singleton.
    private static final ArchiveDirManagerFactory ARCHIVE_DIR_MANAGER_FACTORY = new ArchiveDirManagerFactory();
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManagerFactory.class);
    private final Map<String, ArchiveDirManagerInterface> directoryManagerMap;
//    private final Map<String, AbstractProjectProperties> projectPropertiesMap;
    private final Map<String, String> serverPasswordsMap;
    private final Map<String, String> serverUsersMap;

    /**
     * Creates a new instance of ArchiveDirManagerFactory.
     */
    private ArchiveDirManagerFactory() {
        directoryManagerMap = Collections.synchronizedMap(new TreeMap<>());
//        projectPropertiesMap = Collections.synchronizedMap(new TreeMap<>());
        serverPasswordsMap = Collections.synchronizedMap(new TreeMap<>());
        serverUsersMap = Collections.synchronizedMap(new TreeMap<>());
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
     * @param directory parent directory of where to find the server properties directory.
     * @param serverName the server name.
     * @param directoryCoordinate the directory coordinate.
     * @param userName the user name.
     * @return the archive dir manager interface for the given parameters.
     */
    public synchronized ArchiveDirManagerInterface getDirectoryManager(String directory, String serverName, DirectoryCoordinate directoryCoordinate, String userName) {
//        projectPropertiesMap.put(getPropertiesBranchKey(serverName, directoryCoordinate.getProjectName(), directoryCoordinate.getBranchName(), projectType), projectProperties);
        return getArchiveDirectoryManager(directory, serverName, directoryCoordinate.getProjectName(), directoryCoordinate.getBranchName(),
                directoryCoordinate.getAppendedPath());
    }

    /**
     * Discard any directory manager references associated with the given parameters.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     */
    public synchronized void discardBranchDirectoryManagers(String serverName, String projectName, String branchName) {
        String keyPrefix;
        if (serverName.length() > 0) {
            keyPrefix = serverName + ":" + projectName + ":" + branchName + "//";
        } else {
            keyPrefix = projectName + ":" + branchName + "//";
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

    private ArchiveDirManagerInterface getArchiveDirectoryManager(String directory, String serverName, String projectName, String branchName, String appendedPath) {
        String keyValue = getProjectBranchKey(serverName, projectName, branchName, appendedPath);
        LOGGER.trace("ArchiveDirManagerFactory.getDirectoryManager: Getting directory manager for: [{}]", keyValue);
        ArchiveDirManagerInterface directoryManager = directoryManagerMap.get(keyValue);
        if (directoryManager == null) {
            // There is no directoryManager for this archive directory yet.
            // We'll need to make one.  Figure out which kind we need --
            // a proxy or a local directory manager.
            LOGGER.trace("ArchiveDirManagerFactory.getDirectoryManager: creating ArchiveDirManagerProxy for directory: [{}]", appendedPath);

            // Get the password for this project.  The GUI should have set this via a call to setProjectPassword
            // before calling the factory to build the ArchiveDirManager.
            String userName = serverUsersMap.get(serverName);
            directoryManager = new ArchiveDirManagerProxy(directory, serverName, projectName, branchName, userName, appendedPath);

            directoryManagerMap.put(keyValue, directoryManager);
        } else {
            LOGGER.trace("Re-using existing directory manager for [{}]", keyValue);
        }
        return directoryManager;
    }

    /**
     * Remove a directory manager. Removes the directory manager associated with the given parameters from the cache that the factory maintains.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param appendedPath the appended path.
     */
    public synchronized void removeDirectoryManager(String serverName, String projectName, String branchName, String appendedPath) {
        String keyValue = getProjectBranchKey(serverName, projectName, branchName, appendedPath);
        LOGGER.trace("ArchiveDirManagerFactory.removeDirectoryManager: removing directory manager for: [{}]", keyValue);
        directoryManagerMap.remove(keyValue);
        if (appendedPath.length() == 0) {
            serverPasswordsMap.remove(serverName);
            serverUsersMap.remove(serverName);
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

    private String getProjectBranchKey(String serverName, String projectName, String branchName, String appendedPath) {
        // Make this a standard appended path...
        String standardAppendedPath = Utility.convertToStandardPath(appendedPath);

        String keyValue = serverName + ":" + projectName + ":" + branchName + "//" + standardAppendedPath;

        return keyValue;
    }
}
