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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Directory operation helper.
 *
 * @author Jim Voris
 */
public class DirectoryOperationHelper {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");

    private final DirectoryOperationInterface directoryOperationInterface;

    /**
     * Creates a new instance of DirectoryOperationHelper.
     *
     * @param operation the operation that we're helping.
     */
    public DirectoryOperationHelper(DirectoryOperationInterface operation) {
        directoryOperationInterface = operation;
    }

    /**
     * Add child directories.
     * @param appendedPathMap a map where we store the appended paths.
     * @param viewName the view name.
     * @param appendedPath the appended path.
     * @param response identify the client.
     */
    public void addChildDirectories(Map<String, String> appendedPathMap, String viewName, String appendedPath, ServerResponseFactoryInterface response) {
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(directoryOperationInterface.getProjectName(), viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            if (archiveDirManager instanceof ArchiveDirManager) {
                String projectBaseDirectory = archiveDirManager.getProjectProperties().getArchiveLocation();
                String baseDirectory;
                if (appendedPath.length() > 0) {
                    baseDirectory = projectBaseDirectory + File.separator + appendedPath;
                } else {
                    baseDirectory = projectBaseDirectory;
                }

                // Find all the project sub-projects.
                SubProjectNamesFilter subProjectNameFilter = new SubProjectNamesFilter(false, false);

                // Add all the child appendedPaths.
                addSubProjects(projectBaseDirectory, baseDirectory, subProjectNameFilter, appendedPathMap);
            } else {
                // TODO
                LOGGER.log(Level.WARNING, "Support for views not implemented yet!!!");
            }
        } catch (QVCSException e) {
            LOGGER.log(Level.WARNING, "Caught exception on addChildDirectories: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } finally {
            LOGGER.log(Level.FINE, "Finished addChildDirectories for: [" + appendedPath + "]");
        }
    }

    private void addSubProjects(String projectBaseDirectory, String baseDirectory, SubProjectNamesFilter filter, Map<String, String> appendedPathMap) {
        java.io.File projectDirectory = new java.io.File(baseDirectory);
        java.io.File[] projectFiles = projectDirectory.listFiles(filter);
        if (projectFiles != null) {
            for (File projectFile : projectFiles) {
                String fullChildDirectoryName = baseDirectory + File.separator + projectFile.getName();
                String appendedPath = fullChildDirectoryName.substring(1 + projectBaseDirectory.length());
                // Don't allow the cemetery directory or branch archives directory into the collection.
                if ((0 != appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY))
                        && (0 != appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY))) {
                    appendedPathMap.put(appendedPath, appendedPath);
                    addSubProjects(projectBaseDirectory, fullChildDirectoryName, filter, appendedPathMap);
                }
            }
        }
    }

    /**
     * Process a directory collection.
     * @param viewName the view name.
     * @param directoryMap the map of directories to process.
     * @param response identify the client.
     */
    public void processDirectoryCollection(String viewName, Map directoryMap, ServerResponseFactoryInterface response) {
        int transactionID = ServerTransactionManager.getInstance().sendBeginTransaction(response);
        Iterator it = directoryMap.keySet().iterator();
        while (it.hasNext()) {
            String appendedPath = (String) it.next();
            processDirectory(viewName, appendedPath, response);
        }
        ServerTransactionManager.getInstance().sendEndTransaction(response, transactionID);
    }

    /**
     * Process a directory collection by date.
     * @param viewName the view name.
     * @param directoryMap the map of directories to process.
     * @param response identify the client.
     * @param date the date used to identify what belongs in the collection.
     */
    public void processDirectoryCollectionByDate(String viewName, Map directoryMap, ServerResponseFactoryInterface response, final Date date) {
        int transactionID = ServerTransactionManager.getInstance().sendBeginTransaction(response);
        Iterator it = directoryMap.keySet().iterator();
        while (it.hasNext()) {
            String appendedPath = (String) it.next();
            processDirectoryByDate(viewName, appendedPath, response, date);
        }
        ServerTransactionManager.getInstance().sendEndTransaction(response, transactionID);
    }

    /**
     * Process a directory collection by label.
     * @param viewName the view name.
     * @param directoryMap the map of directories to process.
     * @param response identify the client.
     * @param label the label used to identify what belongs in the collection.
     */
    public void processDirectoryCollectionByLabel(String viewName, Map directoryMap, ServerResponseFactoryInterface response, final String label) {
        int transactionID = ServerTransactionManager.getInstance().sendBeginTransaction(response);
        Iterator it = directoryMap.keySet().iterator();
        while (it.hasNext()) {
            String appendedPath = (String) it.next();
            processDirectoryByLabel(viewName, appendedPath, response, label);
        }
        ServerTransactionManager.getInstance().sendEndTransaction(response, transactionID);
    }

    private void processDirectoryByDate(String viewName, String appendedPath, ServerResponseFactoryInterface response, Date date) {
        LOGGER.log(Level.INFO, "processDirectoryByDate appended path: [" + appendedPath + "]");

        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(directoryOperationInterface.getProjectName(), viewName, appendedPath);
            ArchiveDirManager archiveDirManager = (ArchiveDirManager) ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            Iterator<LogFile> it = archiveDirManager.getArchiveCollectionByDate(date).iterator();
            while (it.hasNext()) {
                ArchiveInfoInterface archiveInfo = (ArchiveInfoInterface) it.next();
                ServerResponseInterface resultObject = directoryOperationInterface.processFile(archiveDirManager, archiveInfo, appendedPath, response);
                if (resultObject != null) {
                    // Send the response.
                    response.createServerResponse(resultObject);
                }
            }
        } catch (QVCSException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    private void processDirectoryByLabel(String viewName, String appendedPath, ServerResponseFactoryInterface response, String label) {
        LOGGER.log(Level.INFO, "processDirectoryByLabel appended path: [" + appendedPath + "]");

        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(directoryOperationInterface.getProjectName(), viewName, appendedPath);
            ArchiveDirManager archiveDirManager = (ArchiveDirManager) ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            Iterator<LogFile> it = archiveDirManager.getArchiveCollectionByLabel(label).iterator();
            while (it.hasNext()) {
                ArchiveInfoInterface archiveInfo = (ArchiveInfoInterface) it.next();
                ServerResponseInterface resultObject = directoryOperationInterface.processFile(archiveDirManager, archiveInfo, appendedPath, response);
                if (resultObject != null) {
                    // Send the response.
                    response.createServerResponse(resultObject);
                }
            }
        } catch (QVCSException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    private void processDirectory(String viewName, String appendedPath, ServerResponseFactoryInterface response) {
        LOGGER.log(Level.INFO, "processDirectory appended path: [" + appendedPath + "]");

        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(directoryOperationInterface.getProjectName(), viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            Iterator<ArchiveInfoInterface> it = archiveDirManager.getArchiveInfoCollection().values().iterator();
            while (it.hasNext()) {
                ArchiveInfoInterface archiveInfo = it.next();
                ServerResponseInterface resultObject = directoryOperationInterface.processFile(archiveDirManager, archiveInfo, appendedPath, response);
                if (resultObject != null) {
                    // Send the response.
                    response.createServerResponse(resultObject);
                }
            }
        } catch (QVCSException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } finally {
            LOGGER.log(Level.INFO, "Completed processing for directory: [" + appendedPath + "]");
        }
    }
}
