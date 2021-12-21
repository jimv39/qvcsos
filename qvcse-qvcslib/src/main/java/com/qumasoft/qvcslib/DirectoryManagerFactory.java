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
package com.qumasoft.qvcslib;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DirectoryManagerFactory is used to create the DirectoryManager for a given project.
 * <p>
 * Note that DirectoryManager objects are only useful for the GUI, and/or other client programs that need to have a merged view of archives and workfiles. The server should not
 * need to use DirectoryManager objects, and should use the ArchiveDirManager class instead.</p>
 *
 * @author Jim Voris
 */
public final class DirectoryManagerFactory {
    // Create our logger object

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryManagerFactory.class);
    // This is a singleton.
    private static final DirectoryManagerFactory FACTORY = new DirectoryManagerFactory();
    private final Map<String, DirectoryManagerInterface> directoryManagerMap;
    private final Map<String, AbstractProjectProperties> projectPropertiesMap;
    private final Map<String, String> serverPasswordsMap;
    private final Map<String, String> serverUsersMap;
    // This map holds a collection of Maps that contain the directory managers for a given project.
    private final Map<String, Map<String, DirectoryManagerInterface>> directoryManagerProjectCollectionMap;

    /**
     * Creates a new instance of DirectoryManagerFactory.
     */
    private DirectoryManagerFactory() {
        directoryManagerMap = Collections.synchronizedMap(new TreeMap<>());
        projectPropertiesMap = Collections.synchronizedMap(new TreeMap<>());
        serverPasswordsMap = Collections.synchronizedMap(new TreeMap<>());
        serverUsersMap = Collections.synchronizedMap(new TreeMap<>());
        directoryManagerProjectCollectionMap = Collections.synchronizedMap(new TreeMap<>());
    }

    /**
     * Get the directory manager factory singleton.
     * @return the directory manager factory singleton.
     */
    public static DirectoryManagerFactory getInstance() {
        return FACTORY;
    }

    /**
     * Build the directory manager for the given parameters.(This may just return the existing directory manager for the given parameters, since it may already have been built.).
     * @param directory parent directory of where to find the server properties directory.
     * @param serverName the server name.
     * @param directoryCoordinate the directory coordinate.
     * @param projectType the type of project.
     * @param projectProperties the project's project properties.
     * @param workfileDirectory the workfile directory.
     * @param listener a change listener.
     * @param fastNotifyFlag the fast notify flag.
     * @return the directory manager for the given parameters.
     */
    public DirectoryManagerInterface getDirectoryManager(String directory, String serverName, DirectoryCoordinate directoryCoordinate, String projectType,
                                                         AbstractProjectProperties projectProperties,
                                                         String workfileDirectory, ChangeListener listener, boolean fastNotifyFlag) {
        String projectName = directoryCoordinate.getProjectName();
        String branchName = directoryCoordinate.getBranchName();
        String appendedPath = directoryCoordinate.getAppendedPath();
        Map<String, DirectoryManagerInterface> directoryManagersForProjectMap;

        DirectoryManager directoryManager = (DirectoryManager) lookupDirectoryManager(serverName, projectName, branchName, appendedPath, projectType);

        if (directoryManager == null) {
            // Create the directory manager that we'll return.
            directoryManager = new DirectoryManager(getServerUsername(serverName), projectName, branchName);

            // Create the archive directory manager that we need.
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactory.getInstance().getDirectoryManager(directory, serverName, directoryCoordinate,
                    projectType, projectProperties, getServerUsername(serverName), true);
            archiveDirManager.setDirectoryManager(directoryManager);

            // Create the workfile directory manager that we need.
            WorkfileDirectoryManager workfileDirectoryManager = new WorkfileDirectoryManager(workfileDirectory, archiveDirManager, directoryManager);

            directoryManager.setArchiveDirManager(archiveDirManager);
            directoryManager.setWorkfileDirectoryManager(workfileDirectoryManager);
            String keyValue = getProjectBranchKey(serverName, projectName, branchName, projectProperties, appendedPath);

            // Update the property map if we can or need to.
            String propertiesKey = getPropertiesBranchKey(serverName, projectName, branchName);
            AbstractProjectProperties existingProjectProperties = projectPropertiesMap.get(propertiesKey);
            if ((existingProjectProperties == null) && (projectProperties != null)) {
                projectPropertiesMap.put(propertiesKey, projectProperties);

                // Create the Map that we will use to contain the collection
                // of directory managers for a given project.
                directoryManagersForProjectMap = Collections.synchronizedMap(new TreeMap<>());
                directoryManagerProjectCollectionMap.put(propertiesKey, directoryManagersForProjectMap);
            } else {
                // Lookup the Map that we use to contain the collection of
                // directory managers for this project.
                directoryManagersForProjectMap = directoryManagerProjectCollectionMap.get(propertiesKey);
            }

            directoryManagerMap.put(keyValue, directoryManager);
            directoryManagersForProjectMap.put(keyValue, directoryManager);
            if (listener != null) {
                directoryManager.addChangeListener(listener);
            }

            LOGGER.trace("DirectoryManagerFactory created directoryManager for: " + keyValue);

            // Things are now setup.  It's okay to get started.
            // (This is here so a remote won't deliver a response to us before
            // we have made an entry in the map of directory managers....
            // this did happen -- which is why I broke the initialization
            // of the archiveDirProxy into two steps.
            archiveDirManager.setFastNotify(fastNotifyFlag);
            archiveDirManager.startDirectoryManager();
        } else {
            LOGGER.trace("DirectoryManagerFactory found existing directoryManager for: " + getProjectBranchKey(serverName, projectName, branchName,
                    projectProperties, appendedPath));
        }

        return directoryManager;
    }

    /**
     * Lookup an existing directory manager given these parameters.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param appendedPath the appended path.
     * @param projectType the project type.
     * @return the associated directory manager (or null if it has not been created).
     */
    public DirectoryManagerInterface lookupDirectoryManager(String serverName, String projectName, String branchName, String appendedPath, String projectType) {
        DirectoryManagerInterface directoryManager = null;
        String propertiesKey = getPropertiesBranchKey(serverName, projectName, branchName);
        AbstractProjectProperties projectProperties = projectPropertiesMap.get(propertiesKey);
        if (projectProperties != null) {
            String keyValue = getProjectBranchKey(serverName, projectName, branchName, projectProperties, appendedPath);
            directoryManager = directoryManagerMap.get(keyValue);
        }
        return directoryManager;
    }

    /**
     * Remove the directory manager associated with the given parameters. The goal of this method is to remove any references to the directory manager so that it can get
     * garbage collected.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param projectType the project type.
     * @param appendedPath the appended path.
     */
    public void removeDirectoryManager(String serverName, String projectName, String branchName, String projectType, String appendedPath) {
        String propertiesKey = getPropertiesBranchKey(serverName, projectName, branchName);
        AbstractProjectProperties projectProperties = projectPropertiesMap.get(propertiesKey);
        if (projectProperties != null) {
            String keyValue = getProjectBranchKey(serverName, projectName, branchName, projectProperties, appendedPath);
            LOGGER.trace("DirectoryManagerFactory.removeDirectoryManager: removing directory manager for: [{}]", keyValue);
            directoryManagerMap.remove(keyValue);
            if ((appendedPath.length() == 0) && (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH))) {
                serverPasswordsMap.remove(serverName);
                serverUsersMap.remove(serverName);
            }

            // And remove the directory manager from the directory manager
            // for project collection...
            Map map = directoryManagerProjectCollectionMap.get(propertiesKey);
            if (map != null) {
                map.remove(keyValue);
            }
        }
        ArchiveDirManagerFactory.getInstance().removeDirectoryManager(serverName, projectName, branchName, projectType, appendedPath);
    }

    /**
     * Set the server password for the given server. This is within the context of a given user... i.e. Directory managers are client side objects, and as a result, necessarily
     * have a user name associated with them.
     * @param serverName the server name.
     * @param password the password for that server.
     */
    public void setServerPassword(String serverName, String password) {
        serverPasswordsMap.put(serverName, password);
        ArchiveDirManagerFactory.getInstance().setServerPassword(serverName, password);
    }

    /**
     * Get the server password for a given server name.
     * @param serverName the server name.
     * @return the user's password for that server.
     */
    public String getServerPassword(String serverName) {
        return serverPasswordsMap.get(serverName);
    }

    /**
     * Set the server user name. This allows us to capture the user name that is used to connect to the given server.
     * @param serverName the server name.
     * @param username the associated user name.
     */
    public void setServerUsername(String serverName, String username) {
        serverUsersMap.put(serverName, username);
        ArchiveDirManagerFactory.getInstance().setServerUsername(serverName, username);
    }

    /**
     * Get the user name that is used for the given server name.
     * @param serverName the server name.
     * @return the associated user name.
     */
    public String getServerUsername(String serverName) {
        return serverUsersMap.get(serverName);
    }

    /**
     * Get the directory managers collection for the given project/branch.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @return the collection of directory managers for the given project/branch.
     */
    public Collection<DirectoryManagerInterface> getDirectoryManagersForProject(String serverName, String projectName, String branchName) {
        String key = getPropertiesBranchKey(serverName, projectName, branchName);
        Map<String, DirectoryManagerInterface> map = directoryManagerProjectCollectionMap.get(key);
        if (map == null) {
            Map<String, DirectoryManagerInterface> directoryManagersForProjectMap = Collections.synchronizedMap(new TreeMap<>());
            return directoryManagersForProjectMap.values();
        } else {
            return map.values();
        }
    }

    /**
     * Discard any directory managers that we have for the given project. The purpose is to discard any references that this factory class has to any directory managers associated
     * with the given project. This includes any non-trunk branch related directory managers as well as all trunk directory managers.
     * @param serverName the server name.
     * @param projectName the project name.
     */
    public void discardDirectoryManagersForProject(String serverName, String projectName) {
        // Get the iterator for the collection of project/branch maps. This iterator
        // iterates over the collection of maps that contain the directory managers
        // for a given project/branch. Since we are discarding all directory managers
        // for a given project, we need to discard any project/branch map for the
        // given project, AND we must also discard all the directory managers
        // contained in those maps. The caller does not know the branch that we
        // need to discard -- and in fact we need to discard any/all branches associated
        // with the given project.... which is why we have to do things the way
        // we do in the code below, since we cannot construct the map keys until
        // we know the branch...
        DirectoryManagerInterface directoryManager = null;
        Iterator<Map<String, DirectoryManagerInterface>> mapIt = directoryManagerProjectCollectionMap.values().iterator();
        while (mapIt.hasNext()) {
            // Get the next map that contains directory managers for a given
            // project/branch.
            boolean discardThisMap = false;
            Map<String, DirectoryManagerInterface> map = mapIt.next();
            Iterator<DirectoryManagerInterface> it = map.values().iterator();
            while (it.hasNext()) {
                // Get the first directory manager contained within a project/branch
                // map so that we can figure out whether this map contains
                // directory managers for the given servername/project.
                directoryManager = it.next();
                ArchiveDirManagerProxy archiveDirManager = (ArchiveDirManagerProxy) directoryManager.getArchiveDirManager();
                if (archiveDirManager != null) {
                    String archiveDirManagerServerName = archiveDirManager.getServerProperties().getServerName();
                    String dirManagerProjectName = directoryManager.getProjectName();

                    if (0 == archiveDirManagerServerName.compareTo(serverName)
                            && 0 == dirManagerProjectName.compareTo(projectName)) {
                        discardThisMap = true;
                    }
                    break;
                }
            }

            // If this map contains directoryManagers for the given servername/project...
            if (discardThisMap && (directoryManager != null)) {
                mapIt.remove();
                projectPropertiesMap.remove(getPropertiesBranchKey(serverName, projectName, directoryManager.getBranchName()));

                it = map.values().iterator();
                while (it.hasNext()) {
                    directoryManager = it.next();
                    String keyValue = getProjectBranchKey(serverName, projectName, directoryManager.getBranchName(), directoryManager.getProjectProperties(),
                            directoryManager.getAppendedPath());
                    directoryManagerMap.remove(keyValue);
                    ArchiveDirManagerFactory.getInstance().removeDirectoryManager(serverName, projectName, directoryManager.getBranchName(), QVCSConstants.QVCS_REMOTE_PROJECT_TYPE,
                            directoryManager.getAppendedPath());
                }
            }
        }
    }

    private String getPropertiesBranchKey(String serverName, String projectName, String branchName) {
        String keyValue = serverName + "." + projectName + "." + branchName;
        return keyValue;
    }

    private String getProjectBranchKey(String serverName, String projectName, String branchName, AbstractProjectProperties projectProperties, String appendedPath) {
        // Make this a standard appended path...
        String standardAppendedPath = Utility.convertToStandardPath(appendedPath);

        String keyValue = serverName + ":" + projectName + ":" + branchName + "//" + projectProperties.getProjectType() + ":" + standardAppendedPath;
        return keyValue;
    }
}
