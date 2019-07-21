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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestRegisterClientListenerData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.qvcslib.response.ServerResponseRegisterClientListener;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.DirectoryContentsManager;
import com.qumasoft.server.DirectoryContentsManagerFactory;
import com.qumasoft.server.DirectoryOperationHelper;
import com.qumasoft.server.DirectoryOperationInterface;
import com.qumasoft.server.ProjectView;
import com.qumasoft.server.ServerTransactionManager;
import com.qumasoft.server.SubProjectNamesFilter;
import com.qumasoft.server.ViewManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request register client listener.
 *
 * @author Jim Voris
 */
public class ClientRequestRegisterClientListener implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestRegisterClientListener.class);
    private final ClientRequestRegisterClientListenerData request;
    private boolean showCemeteryFlag = false;
    private boolean showBranchArchivesFlag = false;

    /**
     * Creates a new instance of ClientRequestRegisterClientListener.
     *
     * @param data the command line data, etc.
     */
    public ClientRequestRegisterClientListener(ClientRequestRegisterClientListenerData data) {
        request = data;
    }

    /**
     * Get the show cemetery flag.
     *
     * @return the show cemetery flag.
     */
    public boolean getShowCemeteryFlag() {
        return showCemeteryFlag;
    }

    /**
     * Set the show cemetery flag.
     *
     * @param flag the show cemetery flag.
     */
    public void setShowCemeteryFlag(boolean flag) {
        showCemeteryFlag = flag;
    }

    /**
     * Get the show branch archives flag.
     *
     * @return the show branch archives flag.
     */
    public boolean getShowBranchArchivesFlag() {
        return showBranchArchivesFlag;
    }

    /**
     * Set the show branch archives flag.
     *
     * @param flag the show branch archives flag.
     */
    public void setShowBranchArchivesFlag(boolean flag) {
        showBranchArchivesFlag = flag;
    }

    /**
     * Perform the operation... which results in sending information about the requested directory back to the client.
     *
     * @param userName the user name.
     * @param response the object used to identify the client.
     * @return a response that will get sent back to the client.
     */
    @SuppressWarnings("LoggerStringConcat")
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseRegisterClientListener serverResponse;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            LOGGER.info("ClientRequestRegisterClientListener.execute archive directory: [{}]", appendedPath);
            if (archiveDirManager != null) {
                // So the response factory will get notified when logfiles change.
                archiveDirManager.addLogFileListener(response);

                // Keep track of the archive directory managers associated with this
                // connection, so when it goes away, we can remove the response
                // as a listener to the archive directory Manager.
                response.addArchiveDirManager(archiveDirManager);

                serverResponse = new ServerResponseRegisterClientListener();
                Iterator it = archiveDirManager.getArchiveInfoCollection().values().iterator();
                while (it.hasNext()) {
                    LogFileInterface logfile = (LogFileInterface) it.next();
                    ArchiveInfoInterface archiveInfo = (ArchiveInfoInterface) logfile;
                    SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo(logfile.getLogfileInfo(), File.separator, logfile.getDefaultRevisionDigest(),
                            archiveInfo.getShortWorkfileName(), archiveInfo.getIsOverlap());
                    serverResponse.addLogfileInformation(skinnyInfo);
                }
                serverResponse.setAppendedPath(appendedPath);
                serverResponse.setProjectName(projectName);
                serverResponse.setViewName(viewName);
                serverResponse.setDirectoryID(archiveDirManager.getDirectoryID());
                returnObject = serverResponse;

                // Let the client know about any sub-projects.
                if (0 == appendedPath.length()) {
                    int transactionID = ServerTransactionManager.getInstance().sendBeginTransaction(response);
                    if (0 == viewName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                        // If we haven't built the directory contents tree yet for this
                        // project, we better do it now.
                        if (!archiveDirManager.getProjectProperties().getDirectoryContentsInitializedFlag()) {
                            buildDirectoryTreeForTrunk(response);
                            archiveDirManager.getProjectProperties().setDirectoryContentsInitializedFlag(true);
                            archiveDirManager.getProjectProperties().saveProperties();
                        }
                        boolean ignoreCaseFlag = archiveDirManager.getProjectProperties().getIgnoreCaseFlag();
                        sendListOfSubProjects(archiveDirManager, ignoreCaseFlag, response);
                    } else {
                        // We have to use the trunk's archiveDirManager object here to get
                        // our anchor directoryID.
                        DirectoryCoordinate rootCoordinate = new DirectoryCoordinate(projectName, QVCSConstants.QVCS_TRUNK_BRANCH, appendedPath);
                        ArchiveDirManagerInterface projectRootArchiveDirManager =
                                ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                                rootCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
                        sendListOfSubProjectsForView(projectRootArchiveDirManager.getDirectoryID(), archiveDirManager, response);
                    }
                    ServerTransactionManager.getInstance().sendEndTransaction(response, transactionID);
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Archive directory not found for [" + appendedPath + "]", projectName, viewName,
                        appendedPath);
                returnObject = error;
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to register client listener for [" + appendedPath + "]",
                    projectName, viewName, appendedPath);
            returnObject = error;
        } catch (QVCSException e) {
            LOGGER.warn("Caught exception trying to register client listener for project: [" + projectName + "] for view: [" + viewName
                    + "] with appended path: ["
                    + appendedPath + "]");
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to register client listener for [" + appendedPath + "]",
                    projectName, viewName, appendedPath);
            returnObject = error;
        }
        return returnObject;
    }

    /**
     * Send the list of sub projects for the trunk.
     *
     * @param archiveDirManager
     * @param ignoreCaseFlag
     * @param response
     */
    private void sendListOfSubProjects(ArchiveDirManagerInterface archiveDirManager, boolean ignoreCaseFlag, ServerResponseFactoryInterface response) {
        String archiveLocation = archiveDirManager.getProjectProperties().getArchiveLocation();

        // Find all the project sub-projects.
        SubProjectNamesFilter subProjectNameFilter = new SubProjectNamesFilter(getShowCemeteryFlag(), getShowBranchArchivesFlag());
        List<String> segments = new ArrayList<>();

        String projectName = archiveDirManager.getProjectProperties().getProjectName();

        // Add all the sub-projects.
        addSubProjects(archiveLocation, ignoreCaseFlag, subProjectNameFilter, segments, projectName, request.getBranchName(), response);
    }

    /**
     * Add the subprojects (aka subdirectories) for the trunk.
     *
     * @param archiveLocation the location of the archive file directory.
     * @param ignoreCaseFlag whether we should ignore case in filenames.
     * @param filter a filter used to ignore QVCS internal files.
     * @param segments the directory segments that we're interested in.
     * @param projectName the name of the project.
     * @param viewName the name of the view (aka branch).
     * @param response the object that identifies the client.
     */
    private void addSubProjects(String archiveLocation, boolean ignoreCaseFlag, SubProjectNamesFilter filter, List<String> segments, String projectName, String viewName,
                                ServerResponseFactoryInterface response) {
        java.io.File projectDirectory = new java.io.File(archiveLocation);
        java.io.File[] projectFilesList = projectDirectory.listFiles(filter);
        if (projectFilesList != null) {
            // We want to guarantee that we send these back in sorted order...
            TreeMap<String, java.io.File> sortedProjectFiles = new TreeMap<>();
            for (File projectFilesList1 : projectFilesList) {
                String directoryName = projectFilesList1.getName();
                if (ignoreCaseFlag) {
                    directoryName = directoryName.toLowerCase();
                }
                sortedProjectFiles.put(directoryName, projectFilesList1);
            }
            Iterator<java.io.File> it = sortedProjectFiles.values().iterator();
            while (it.hasNext()) {
                java.io.File directory = it.next();
                String fullChildDirectoryName = archiveLocation + File.separator + directory.getName();
                segments.add(directory.getName());
                String[] stringSegments = new String[segments.size()];
                for (int j = 0; j < stringSegments.length; j++) {
                    stringSegments[j] = segments.get(j);
                }
                ServerResponseProjectControl responseControlMsg = new ServerResponseProjectControl();
                responseControlMsg.setAddFlag(true);
                responseControlMsg.setServerName(response.getServerName());
                responseControlMsg.setProjectName(projectName);
                responseControlMsg.setBranchName(viewName);
                responseControlMsg.setDirectorySegments(stringSegments);

                // Let the client know about this sub-project.
                response.createServerResponse(responseControlMsg);
                addSubProjects(fullChildDirectoryName, ignoreCaseFlag, filter, segments, projectName, viewName, response);

                // Remove the last segment (the one we just added).
                segments.remove(segments.size() - 1);
            }
        }
    }

    /**
     * Send the list of sub projects (a.k.a. subdirectories) for a view (a.k.a. branch).
     *
     * @param projectRootDirectoryID the directory id of the root directory.
     * @param archiveDirManager the archive directory manager for the root directory on the trunk.
     * @param response the object that identifies the client.
     * @throws IOException if we have an IO exception.
     * @throws QVCSException if we have something QVCS specific go wrong.
     */
    private void sendListOfSubProjectsForView(int projectRootDirectoryID, ArchiveDirManagerInterface archiveDirManager,
                                                                          ServerResponseFactoryInterface response) throws IOException, QVCSException {
        DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(archiveDirManager.getProjectName());
        RemoteBranchProperties remoteViewProperties = (RemoteBranchProperties) archiveDirManager.getProjectProperties();
        List<String> segments = new ArrayList<>();
        addSubProjectsForView(projectRootDirectoryID, directoryContentsManager, remoteViewProperties, segments, response);
    }

    private void addSubProjectsForView(int directoryID, DirectoryContentsManager directoryContentsManager, RemoteBranchProperties remoteViewProperties, List<String> segments,
                                       ServerResponseFactoryInterface response) throws IOException, QVCSException {
        Map<Integer, String> directoryCollection = null;
        if (remoteViewProperties.getIsDateBasedBranchFlag()) {
            Map<Integer, String> fullDirectoryCollection = directoryContentsManager.getDirectoryIDCollectionForDateBasedView(request.getBranchName(), "", directoryID, response);

            // We need to remove the QVCS house keeping directories from the response...
            directoryCollection = new TreeMap<>(fullDirectoryCollection);
            removeQVCSHousekeepingDirectories(directoryCollection);
        } else if (remoteViewProperties.getIsTranslucentBranchFlag()) {
            ProjectView projectView = ViewManager.getInstance().getView(request.getProjectName(), request.getBranchName());
            String appendedPath = Utility.createAppendedPathFromSegments(segments);
            Map<Integer, String> fullDirectoryCollection = directoryContentsManager.getDirectoryIDCollectionForTranslucentBranch(projectView, appendedPath, directoryID, response);

            // We need to remove the QVCS house keeping directories from the response...
            directoryCollection = new TreeMap<>(fullDirectoryCollection);
            removeQVCSHousekeepingDirectories(directoryCollection);
        } else if (remoteViewProperties.getIsOpaqueBranchFlag()) {
            ProjectView projectView = ViewManager.getInstance().getView(request.getProjectName(), request.getBranchName());
            Map<Integer, String> fullDirectoryCollection = directoryContentsManager.getDirectoryIDCollectionForOpaqueBranch(projectView, "", directoryID, response);

            // We need to remove the QVCS house keeping directories from the response...
            directoryCollection = new TreeMap<>(fullDirectoryCollection);
            removeQVCSHousekeepingDirectories(directoryCollection);
        }

        if ((directoryCollection != null) && (directoryCollection.size() > 0)) {
            // We want to guarantee that we send these back in sorted order...
            TreeMap<String, Integer> sortedDirectoryIDs = new TreeMap<>();
            HashMap<Integer, String> visibleDirectoryNames = new HashMap<>();
            Iterator<Map.Entry<Integer, String>> entryIt = directoryCollection.entrySet().iterator();
            while (entryIt.hasNext()) {
                Map.Entry<Integer, String> childDirectoryEntry;
                childDirectoryEntry = entryIt.next();
                String childDirectoryName = childDirectoryEntry.getValue();
                String sortableChildDirectoryName = childDirectoryName;
                if (remoteViewProperties.getIgnoreCaseFlag()) {
                    sortableChildDirectoryName = childDirectoryName.toLowerCase();
                }
                sortedDirectoryIDs.put(sortableChildDirectoryName, childDirectoryEntry.getKey());
                visibleDirectoryNames.put(childDirectoryEntry.getKey(), childDirectoryName);
            }

            Iterator<Map.Entry<String, Integer>> directoryNameIterator = sortedDirectoryIDs.entrySet().iterator();
            while (directoryNameIterator.hasNext()) {
                Map.Entry<String, Integer> mapEntry = directoryNameIterator.next();
                String childDirectoryName = visibleDirectoryNames.get(mapEntry.getValue());
                segments.add(childDirectoryName);
                String[] stringSegments = new String[segments.size()];
                for (int j = 0; j < stringSegments.length; j++) {
                    stringSegments[j] = segments.get(j);
                }
                ServerResponseProjectControl responseControlMsg = new ServerResponseProjectControl();
                responseControlMsg.setAddFlag(true);
                responseControlMsg.setServerName(response.getServerName());
                responseControlMsg.setProjectName(request.getProjectName());
                responseControlMsg.setBranchName(request.getBranchName());
                responseControlMsg.setDirectorySegments(stringSegments);

                String sortableChildDirectoryName = childDirectoryName;
                if (remoteViewProperties.getIgnoreCaseFlag()) {
                    sortableChildDirectoryName = childDirectoryName.toLowerCase();
                }
                // Let the client know about this sub-project.
                response.createServerResponse(responseControlMsg);
                addSubProjectsForView(sortedDirectoryIDs.get(sortableChildDirectoryName), directoryContentsManager, remoteViewProperties, segments, response);

                // Remove the last segment (the one we just added).
                segments.remove(segments.size() - 1);
            }
        }
        if (getShowCemeteryFlag()) {
            String[] stringSegments = new String[1];
            stringSegments[0] = QVCSConstants.QVCS_CEMETERY_DIRECTORY;
            ServerResponseProjectControl responseControlMsg = new ServerResponseProjectControl();
            responseControlMsg.setAddFlag(true);
            responseControlMsg.setServerName(response.getServerName());
            responseControlMsg.setProjectName(request.getProjectName());
            responseControlMsg.setBranchName(request.getBranchName());
            responseControlMsg.setDirectorySegments(stringSegments);

            // Let the client know about the cemetery.
            response.createServerResponse(responseControlMsg);
        }
    }

    private void buildDirectoryTreeForTrunk(ServerResponseFactoryInterface response) throws IOException, QVCSException {
        // Let the user know that we're building directory contents, and that it
        // may take awhile.
        ServerResponseMessage message = new ServerResponseMessage("The server needs to create project meta-data. This may take some time.", request.getProjectName(),
                request.getBranchName(),
                request.getAppendedPath(),
                ServerResponseMessage.HIGH_PRIORITY);
        message.setShortWorkfileName("");
        response.createServerResponse(message);

        // Build all the archive directory managers so we'll have directory ID's for everything.
        buildArchiveDirManagers(response);
    }

    private void buildArchiveDirManagers(ServerResponseFactoryInterface response) {
        DirectoryBuilderHelper directoryBuilderHelper = new DirectoryBuilderHelper(request.getProjectName());
        DirectoryOperationHelper directoryOperationHelper = new DirectoryOperationHelper(directoryBuilderHelper);
        TreeMap<String, String> directoryMap = new TreeMap<>();

        try {
            // We have to do this directory at least...
            directoryMap.put("", "");

            directoryOperationHelper.addChildDirectories(directoryMap, QVCSConstants.QVCS_TRUNK_BRANCH, "", response);
            directoryOperationHelper.processDirectoryCollection(QVCSConstants.QVCS_TRUNK_BRANCH, directoryMap, response);
        } catch (java.lang.NullPointerException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Remove any QVCS house keeping directories from the directory collection so the client will not see them. This is for views.
     *
     * @param directoryCollection the directory collection. This is modified in place.
     */
    private void removeQVCSHousekeepingDirectories(Map<Integer, String> directoryCollection) {
        Iterator<String> it = directoryCollection.values().iterator();
        while (it.hasNext()) {
            String directoryName = it.next();
            switch (directoryName) {
                case QVCSConstants.QVCS_DIRECTORY_METADATA_DIRECTORY:
                    it.remove();
                    break;
                case QVCSConstants.QVCS_CEMETERY_DIRECTORY:
                    it.remove();
                    break;
                case QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY:
                    it.remove();
                    break;
                default:
                    break;
            }
        }
    }

    static class DirectoryBuilderHelper implements DirectoryOperationInterface {

        private String projectName = null;

        DirectoryBuilderHelper(String project) {
            this.projectName = project;
        }

        @Override
        public ServerResponseInterface processFile(ArchiveDirManagerInterface archiveDirManager, ArchiveInfoInterface archiveInfo, String appendedPath,
                                                                                                                                   ServerResponseFactoryInterface response) {
            // Guarantee that we have a directory id for the given archive directory manager.
            archiveDirManager.getDirectoryID();
            return null;
        }

        @Override
        public String getProjectName() {
            return projectName;
        }
    }

}
