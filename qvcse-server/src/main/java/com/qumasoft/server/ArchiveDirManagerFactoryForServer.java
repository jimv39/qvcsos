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
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.ProjectPropertiesFactory;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
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
public final class ArchiveDirManagerFactoryForServer {
    // This is a singleton.
    private static final ArchiveDirManagerFactoryForServer FACTORY = new ArchiveDirManagerFactoryForServer();
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final Map<String, ArchiveDirManagerInterface> directoryManagerMap;
    private final Map<String, AbstractProjectProperties> projectPropertiesMap;

    /**
     * Creates a new instance of ArchiveDirManagerFactory.
     */
    private ArchiveDirManagerFactoryForServer() {
        directoryManagerMap = Collections.synchronizedMap(new TreeMap<String, ArchiveDirManagerInterface>());
        projectPropertiesMap = Collections.synchronizedMap(new TreeMap<String, AbstractProjectProperties>());
    }

    /**
     * Get the Archive directory manager factory for server singleton.
     * @return the Archive directory manager factory for server singleton.
     */
    static ArchiveDirManagerFactoryForServer getInstance() {
        return FACTORY;
    }

    synchronized ArchiveDirManagerInterface getDirectoryManager(String serverName, DirectoryCoordinate directoryCoordinate, String projectType,
            String userName, ServerResponseFactoryInterface response, boolean discardObsoleteFilesFlag) throws QVCSException {
        AbstractProjectProperties projectProperties = getProjectProperties(serverName, directoryCoordinate.getProjectName(), directoryCoordinate.getViewName(), projectType);
        return getDirectoryManager(serverName, directoryCoordinate, projectProperties, userName, response, discardObsoleteFilesFlag);
    }

    synchronized void discardViewDirectoryManagers(String serverName, String projectName, String viewName) {
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

    void resetDirectoryMap() {
        directoryManagerMap.clear();
    }

    private ArchiveDirManagerInterface getDirectoryManager(String serverName, DirectoryCoordinate directoryCoordinate, AbstractProjectProperties projectProperties,
            String userName, ServerResponseFactoryInterface response, boolean discardObsoleteFilesFlag) throws QVCSException {
        String projectName = directoryCoordinate.getProjectName();
        String viewName = directoryCoordinate.getViewName();
        String appendedPath = directoryCoordinate.getAppendedPath();
        String keyValue = getProjectViewKey(serverName, projectName, viewName, projectProperties, appendedPath);
        LOGGER.log(Level.FINE, "ArchiveDirManagerFactory.getDirectoryManager: Getting directory manager for: " + keyValue);
        ArchiveDirManagerInterface directoryManager = directoryManagerMap.get(keyValue);
        if (directoryManager == null) {
            // We're running on the server...
            String localAppendedPath = Utility.convertToLocalPath(appendedPath);
            if (0 == viewName.compareTo(QVCSConstants.QVCS_TRUNK_VIEW)) {
                directoryManager = new ArchiveDirManager(projectProperties, viewName, localAppendedPath, userName, response, discardObsoleteFilesFlag);
                LOGGER.log(Level.INFO, "ArchiveDirManagerFactory.getDirectoryManager: creating ArchiveDirManager for directory [" + localAppendedPath + "] for Trunk view.");
            } else {
                directoryManager = ArchiveDirManagerFactoryForViews.getInstance().getDirectoryManager(serverName, projectName, viewName, appendedPath, userName, response);
                LOGGER.log(Level.INFO, "ArchiveDirManagerFactory.getDirectoryManager: creating ArchiveDirManager for view directory [" + localAppendedPath
                        + "] for [" + viewName + "] view.");
            }

            if (directoryManager != null) {
                directoryManagerMap.put(keyValue, directoryManager);

                // If the discardObsoleteFilesFlag is true, then we'll throw away (actually move!)
                // any obsolete files.  During an upgrade, we need to preserve obsolete
                // files until after we've captured the directory contents... so we
                // have this flag so we don't have to re-write all the archiveDirManager
                // code for the upgrade...
                if (discardObsoleteFilesFlag) {
                    // Get rid of any obsolete files (needed to migrate from pre-2.1 to post 2.1 releases.
                    if (directoryManager instanceof ArchiveDirManager) {
                        ArchiveDirManager archiveDirManager = (ArchiveDirManager) directoryManager;
                        archiveDirManager.deleteObsoleteFiles(userName, response);
                    }
                }
            }
        } else {
            LOGGER.log(Level.FINE, "Re-using existing directory manager for " + keyValue);
        }
        return directoryManager;
    }

    synchronized AbstractProjectProperties getProjectProperties(String serverName, String projectName, String viewName, String projectType) throws QVCSException {
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

    synchronized void removeDirectoryManager(String serverName, String projectName, String viewName, String projectType, String appendedPath) {
        String propertiesKey = getPropertiesViewKey(serverName, projectName, viewName, projectType);
        AbstractProjectProperties projectProperties = projectPropertiesMap.get(propertiesKey);
        if (projectProperties != null) {
            String keyValue = getProjectViewKey(serverName, projectName, viewName, projectProperties, appendedPath);
            LOGGER.log(Level.FINE, "ArchiveDirManagerFactory.removeDirectoryManager: removing directory manager for: " + keyValue);
            directoryManagerMap.remove(keyValue);
        }
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