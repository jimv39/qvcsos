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
package com.qumasoft.clientapi;

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.CheckOutCommentManager;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetMostRecentActivityData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.LabelManager;
import com.qumasoft.qvcslib.LogFileProxy;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.PasswordChangeListenerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteProjectProperties;
import com.qumasoft.qvcslib.ServerManager;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.response.ServerResponseChangePassword;
import com.qumasoft.qvcslib.response.ServerResponseGetMostRecentActivity;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.qvcslib.response.ServerResponseListViews;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyListenerInterface;
import com.qumasoft.qvcslib.TransportProxyType;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A class that implements the {@link ClientAPI} interface.
 * @author Jim Voris
 */
class ClientAPIImpl implements ClientAPI, ChangeListener, PasswordChangeListenerInterface, TransportProxyListenerInterface {

    private static final String NOT_SUPPORTED = "Not Supported";
    private static final long TEN_SECONDS = 10000L;
    private static final long ONE_HUNDRED_MILLISECONDS = 100L;

    ClientAPIImpl(ClientAPIContextImpl contextImpl) {
        this.clientAPIContextImpl = contextImpl;
    }

    /**
     * So no one can invoke the default constructor.
     */
    private ClientAPIImpl() {
        this.clientAPIContextImpl = null;
    }
    /**
     * Create our logger object
     */
    private static Logger logger = Logger.getLogger("com.qumasoft.clientapi");
    /**
     * The name we use for the server... it really can be anything.
     */
    private static final String SERVER_NAME = "QVCS Enterprise Server for ClientAPI";
    /**
     * Where we get the results of our login request.
     */
    private final AtomicReference<String> passwordResponse = new AtomicReference<>(QVCSConstants.QVCS_NO);
    /**
     * Count the number of logins so each one gets its own unique server name.
     */
    private static AtomicInteger serverInstanceCounter = new AtomicInteger(0);
    /**
     * The current clientContextAPIImpl.
     */
    private final ClientAPIContextImpl clientAPIContextImpl;

    /**
     * Get the list of projects from the QVCS-Enterprise server. The list of
     * projects returned will depend on the username/password supplied in the
     * clientAPIContext object. Only those projects visible to the given user
     * will be returned. A project is 'visible' to a user if that user has the
     * 'Get File' privilege. Typically, any QVCS-Enterprise user with a READER
     * role, or a DEVELOPER role will have the 'Get File' privilege.
     *
     * @return a List<String> of project names visible to the username defined
     * in the clientAPIContext.
     * @throws ClientAPIException if there are any problems.
     */
    @Override
    public List<String> getProjectList() throws ClientAPIException {
        List<String> projectList = populateProjectList();

        // End the operation.
        endOperation();
        return projectList;
    }

    /**
     * Get the list of views for the a given project. To use this method, you
     * must set the name of the project in the clientAPIContext as well as the
     * other parameters needed for the getProjectList method.
     *
     * @param clientAPIContext the client API context object used to define the
     * username/password, server connection information, and project name.
     * @return a List<String> of view names. At the very least, this List will
     * include the 'Trunk' view.
     * @throws ClientAPIException if there are any problems, or if the requested
     * project is not found.
     */
    @Override
    public List<String> getViewList() throws ClientAPIException {
        List<String> viewList = populateViewList();

        // End the operation.
        endOperation();

        return viewList;
    }

    /**
     * Get the list of directories for the given project/view. To use this
     * method, you must set the view name in the clientAPIContext, as well as
     * all the other parameters needed for the getProjectList method.
     *
     * @param clientAPIContext the client API context object used to define the
     * username/password, server connection information, project name, and view
     * name.
     * @return a List<String> of appended path strings for all the directories
     * of a project/view. Each string represents one directory. The empty string
     * "" is used to represent the root directory of the project/view. The
     * Strings for sub-directories are relative to the project root directory
     * and in QVCS-Enterprise terminology are called the directory's
     * 'appendedPath'.
     * @throws ClientAPIException if there are any problems.
     */
    @Override
    public List<String> getProjectDirectoryList() throws ClientAPIException {
        List<String> directoryList = populateDirectoryList();

        // End the operation.
        endOperation();
        return directoryList;
    }

    /**
     * Get the list of {@link FileInfo} objects for the directory specified via
     * the {@link ClientAPIContext#setAppendedPath(java.lang.String)} attribute
     * on the clientAPIContext object. If you set the recurse flag to true (via
     * the {@link ClientAPIContext#setRecurseFlag(boolean)} attribute), then the
     * returned List will contain elements for the directory identified by the
     * appended path, as well as all the directories beneath that directory.
     *
     * @param clientAPIContext the client API context object used to define the
     * username/password, server connection information, project name, view
     * name, appended path, and optionally, the recurse flag.
     * @return a List of {@link FileInfo} objects; one per file.
     * @throws ClientAPIException if there are any problems.
     */
    @Override
    public List<FileInfo> getFileInfoList() throws ClientAPIException {
        List<FileInfo> fileInfoList = populateFileInfoList();

        // End the operation.
        endOperation();
        return fileInfoList;
    }

    /**
     * Get the list of {@link RevisionInfo} objects for the file specified by
     * the clientAPIContext.
     *
     * @param clientAPIContext the client API context object used to define the
     * username/password, server connection information, project name, view
     * name, appended path, and the file name.
     * @return the List of {@link RevisionInfo} objects for the file specified
     * by the clientAPIContext. The List could be empty if the file is not under
     * version control. Be careful to identify the location of the file
     * correctly -- i.e. the project name, view name, appended path, and file
     * name must be correct in order for the server to find the file and report
     * its revision information.
     * @throws ClientAPIException if there are any problems.
     */
    @Override
    public List<RevisionInfo> getRevisionInfoList() throws ClientAPIException {
        List<RevisionInfo> revisionInfoList = populateRevisionInfoList();

        // End the operation.
        endOperation();
        return revisionInfoList;
    }

    @Override
    public Date getMostRecentActivity() throws ClientAPIException {
        Date mostRecentActivity = fetchMostRecentActivity();
        logger.log(Level.INFO, "Most recent activity for Project/View/Appended Path: [" + this.clientAPIContextImpl.getProjectName() + "/"
                + this.clientAPIContextImpl.getViewName() + "/"
                + this.clientAPIContextImpl.getAppendedPath()
                + ": " + mostRecentActivity.toString());

        // End the operation.
        endOperation();
        return mostRecentActivity;
    }

    @Override
    public String getPendingPassword(String serverName) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void notifyLoginResult(ServerResponseLogin response) {
        synchronized (passwordResponse) {
            if (response.getLoginResult()) {
                if (response.getVersionsMatchFlag()) {
                    passwordResponse.set(QVCSConstants.QVCS_YES);
                } else {
                    logger.log(Level.WARNING, "Client jar file is out of date.");
                    passwordResponse.set(QVCSConstants.QVCS_NO);
                }
            } else {
                passwordResponse.set(QVCSConstants.QVCS_NO);
                logger.log(Level.WARNING, "Login failure: [" + response.getFailureReason() + "]");
            }
            passwordResponse.notifyAll();
        }
    }

    @Override
    public void notifyPasswordChange(ServerResponseChangePassword response) {
    }

    @Override
    public void notifyTransportProxyListener(ServerResponseInterface message) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void notifyUpdateComplete() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void savePendingPassword(String serverName, String password) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        String msg = "Change Event: " + changeEvent.getSource().getClass().getName();
        logger.log(Level.INFO, msg);

        Object source = changeEvent.getSource();
        if (source instanceof ServerResponseListProjects) {
            msg = "Received list of projects for server: [" + SERVER_NAME + "]";
            logger.log(Level.INFO, msg);
            synchronized (clientAPIContextImpl.getSyncObject()) {
                ServerResponseListProjects projectList = (ServerResponseListProjects) source;
                String[] projectNames = projectList.getProjectList();
                clientAPIContextImpl.setProjectProperties(projectList.getPropertiesList());
                for (String projectName : projectNames) {
                    logger.log(Level.INFO, projectName);
                }
                clientAPIContextImpl.setServerProjectNames(projectNames);
                clientAPIContextImpl.getSyncObject().notifyAll();
            }
        } else if (source instanceof ServerResponseListViews) {
            msg = "Received list of views for project: [" + clientAPIContextImpl.getProjectName() + "]";
            logger.log(Level.INFO, msg);
            synchronized (clientAPIContextImpl.getSyncObject()) {
                ServerResponseListViews serverResponseListViews = (ServerResponseListViews) source;
                String[] viewNames = serverResponseListViews.getViewList();
                for (String viewName : viewNames) {
                    logger.log(Level.INFO, viewName);
                }
                clientAPIContextImpl.setProjectViewNames(viewNames);
                clientAPIContextImpl.getSyncObject().notifyAll();
            }
        } else if (source instanceof ServerResponseProjectControl) {
            ServerResponseProjectControl projectControl = (ServerResponseProjectControl) source;
            if (projectControl.getAddFlag()) {
                String[] segments = projectControl.getDirectorySegments();

                // Create appended path for this prospective directory manager
                String appendedPath = Utility.createAppendedPathFromSegments(segments);
                msg = "********************************************************** Received AppendedPath: [" + appendedPath + "]";
                logger.log(Level.INFO, msg);

                // Add the appendedPath to the collection of appended paths for prospective directory
                // managers.  We'll create a sync object that we can use to synchronize on that
                // directory manager, if we wind up needing to create a directory manager.
                // (At this point we are simply creating the entire set of appended paths
                // that exist for a given project... we will have to use some subset of that
                // collection, based on the appended path of the user's request).
                if (null == clientAPIContextImpl.getAppendedPathMap().get(appendedPath)) {
                    clientAPIContextImpl.getAppendedPathMap().put(appendedPath, new Object());
                }
            }
        } else if (source instanceof ArchiveDirManagerProxy) {
            ArchiveDirManagerProxy archiveDirManager = (ArchiveDirManagerProxy) source;
            String appendedPath = archiveDirManager.getAppendedPath();
            logger.log(Level.INFO, "Directory: " + appendedPath);
            Object localSyncObject = null;

            while (localSyncObject == null) {
                localSyncObject = clientAPIContextImpl.getAppendedPathMap().get(appendedPath);
                if (localSyncObject == null) {
                    try {
                        // This can happen if we get the response from the server before
                        // we've had a chance to populate the appendPathMap with an
                        // entry for the given directory.
                        logger.log(Level.INFO, "Did not find synchronization object for: [" + appendedPath + "]. Waiting for server response...");
                        Thread.sleep(ONE_HUNDRED_MILLISECONDS);
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                    }
                }
            }
            if (localSyncObject != null) {
                synchronized (localSyncObject) {
                    localSyncObject.notifyAll();
                }
            }
        } else if (source instanceof ServerResponseGetMostRecentActivity) {
            msg = "Received most recent activity.";
            logger.log(Level.INFO, msg);
            ServerResponseGetMostRecentActivity serverResponseGetMostRecentActivity = (ServerResponseGetMostRecentActivity) source;
            logger.log(Level.INFO, "Project: [" + serverResponseGetMostRecentActivity.getProjectName() + "] View: ["
                    + serverResponseGetMostRecentActivity.getViewName() + "] Appended path: "
                    + "[" + serverResponseGetMostRecentActivity.getAppendedPath() + "]");
            synchronized (clientAPIContextImpl.getSyncObject()) {
                clientAPIContextImpl.setMostRecentActivity(serverResponseGetMostRecentActivity.getMostRecentActivityDate());
                clientAPIContextImpl.getSyncObject().notifyAll();
            }
        } else {
            msg = "stateChanged received unexpected object of type [" + source.getClass().getName() + "]";
            logger.log(Level.WARNING, msg);
        }
    }

    private void createDirectoryManager(String appendedPath) throws ClientAPIException {
        Object syncObject = new Object();
        synchronized (syncObject) {
            ArchiveDirManagerProxy archiveDirManagerProxy = clientAPIContextImpl.getArchiveDirManagerProxyMap().get(appendedPath);
            if (archiveDirManagerProxy == null) {
                try {
                    // Save the sync object for future use...
                    clientAPIContextImpl.getAppendedPathMap().put(appendedPath, syncObject);

                    // Create the directory manager proxy.
                    archiveDirManagerProxy = new ArchiveDirManagerProxy(getProjectProperties(), clientAPIContextImpl.getServerProperties(), clientAPIContextImpl.getViewName(),
                            clientAPIContextImpl.getUserName(), appendedPath);
                    archiveDirManagerProxy.setFastNotify(true);
                    archiveDirManagerProxy.addChangeListener(this);

                    clientAPIContextImpl.getArchiveDirManagerProxyMap().put(appendedPath, archiveDirManagerProxy);
                    archiveDirManagerProxy.startDirectoryManager();

                    // Wait for the response from the server.
                    String msg = "Waiting for server response for: [" + appendedPath + "]";
                    logger.log(Level.FINE, msg);
                    syncObject.wait();

                    msg = "Received server response for: [" + appendedPath + "]";
                    logger.log(Level.FINE, msg);
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            } else {
                String msg = "Found existing directory manager for: [" + appendedPath + "]";
                logger.log(Level.INFO, msg);
            }
        }
    }

    private void endOperation() {
        if (!clientAPIContextImpl.getPreserveStateFlag()) {
            TransportProxyFactory.getInstance().removeChangeListener(this);
            TransportProxyFactory.getInstance().removeChangedPasswordListener(this);
            ServerManager.getServerManager().removeChangeListener(this);
            clientAPIContextImpl.getTransportProxy().close();
            clientAPIContextImpl.setLoggedInFlag(false);
        }
    }

    private AbstractProjectProperties getProjectProperties() {
        Properties remoteProperties = null;
        for (int i = 0; i < clientAPIContextImpl.getProjectProperties().length; i++) {
            if (0 == clientAPIContextImpl.getServerProjectNames()[i].compareTo(clientAPIContextImpl.getProjectName())) {
                remoteProperties = clientAPIContextImpl.getProjectProperties()[i];
                String msg = "Matched project: " + clientAPIContextImpl.getServerProjectNames()[i] + "; Index: " + i;
                logger.log(Level.FINE, msg);
                break;
            }
        }
        return new RemoteProjectProperties(clientAPIContextImpl.getProjectName(), remoteProperties);
    }

    private void login() throws ClientAPIException {
        clientAPIContextImpl.setProjectProperties(null);
        clientAPIContextImpl.setServerProjectNames(null);
        clientAPIContextImpl.setProjectViewNames(null);
        clientAPIContextImpl.setAppendedPathMap(Collections.synchronizedMap(new TreeMap<String, Object>()));
        clientAPIContextImpl.setArchiveDirManagerProxyMap(Collections.synchronizedMap(new TreeMap<String, ArchiveDirManagerProxy>()));

        if (!clientAPIContextImpl.getLoggedInFlag()) {
            TransportProxyType transportType = TransportProxyFactory.RAW_SOCKET_PROXY;

            // The port we'll connect on...
            int port = clientAPIContextImpl.getPort();

            // User name
            String userName = clientAPIContextImpl.getUserName();

            // Hash the password...
            byte[] hashedPassword = Utility.getInstance().hashPassword(clientAPIContextImpl.getPassword());

            // Initialize the workfile digest manager
            WorkfileDigestManager.getInstance().initialize();

            // Initialize the checkout comment manager.
            CheckOutCommentManager.getInstance().initialize();

            // Initialize the label manager
            String systemUserName = System.getProperty(clientAPIContextImpl.getUserName());
            LabelManager.setUserName(systemUserName);
            LabelManager.getInstance().initialize();

            // And force the login to the transport...
            TransportProxyFactory.getInstance().addChangeListener(this);
            TransportProxyFactory.getInstance().addChangedPasswordListener(this);
            ServerManager.getServerManager().addChangeListener(this);

            // Create a server properties instance
            ServerProperties serverProperties = new ServerProperties();
            String serverName = SERVER_NAME + " Instance " + serverInstanceCounter.getAndIncrement();
            serverProperties.setServerName(serverName);
            serverProperties.setServerIPAddress(clientAPIContextImpl.getServerIPAddress());
            serverProperties.setClientPort(port);
            clientAPIContextImpl.setServerProperties(serverProperties);

            synchronized (passwordResponse) {
                try {
                    logger.log(Level.INFO, "********************* Before login attempt to [" + serverProperties.getServerIPAddress() + "] *********************");
                    clientAPIContextImpl.setTransportProxy(TransportProxyFactory.getInstance().getTransportProxy(transportType, serverProperties, port,
                            userName, hashedPassword, this, null));

                    // Wait 10 seconds for a response.
                    passwordResponse.wait(TEN_SECONDS);
                    logger.log(Level.INFO, "********************* After login attempt to [" + serverProperties.getServerIPAddress() + "] *********************");
                } catch (InterruptedException e) {
                    logger.log(Level.INFO, "Interrupted exception.");
                }
            }

            if (passwordResponse.get().equals(QVCSConstants.QVCS_YES)) {
                logger.log(Level.INFO, "Logged in to server.");
                clientAPIContextImpl.setLoggedInFlag(true);
            } else {
                throw new ClientAPIException("***************** Failed to login to server *****************");
            }
        }
    }

    private List<String> populateDirectoryList() throws ClientAPIException {
        List<String> directoryList = new ArrayList<>();

        // Populate the view list. (which populates the project list).
        populateViewList();

        // Validate the input parameters.
        validateAPIContextGetDirectoryList();

        // Make sure the requested view exists.
        boolean requestedViewFoundFlag = false;
        for (String viewName : clientAPIContextImpl.getProjectViewNames()) {
            if (0 == viewName.compareToIgnoreCase(clientAPIContextImpl.getViewName())) {
                requestedViewFoundFlag = true;
                break;
            }
        }

        if (!requestedViewFoundFlag) {
            throw new ClientAPIException("Requested view not found: " + clientAPIContextImpl.getViewName());
        }

        // Save credentials for this server.
        DirectoryManagerFactory.getInstance().setServerUsername(clientAPIContextImpl.getServerProperties().getServerName(), clientAPIContextImpl.getUserName());
        DirectoryManagerFactory.getInstance().setServerPassword(clientAPIContextImpl.getServerProperties().getServerName(), clientAPIContextImpl.getPassword());

        // We need to get a directory manager for the root of the project so we'll get the additional
        // segments that exist for this project.  We'll then have to use those to figure out the
        // set of directory managers that we need to build in order to satisfy the users request.
        logger.log(Level.INFO, "Getting directory list from server");
        createDirectoryManager("");

        Iterator<String> it = clientAPIContextImpl.getAppendedPathMap().keySet().iterator();
        while (it.hasNext()) {
            directoryList.add(it.next());
        }

        return directoryList;
    }

    private List<FileInfo> populateFileInfoList() throws ClientAPIException {
        // Populate the directory list (which will populate the project list, and the view list as well).
        populateDirectoryList();

        // Validate the input parameters.
        validateAPIContextGetFileInfoList();

        // Make sure the requested appended path exists.
        if (!clientAPIContextImpl.getAppendedPathMap().keySet().contains(this.clientAPIContextImpl.getAppendedPath())) {
            throw new ClientAPIException("Request appended path does not exist.");
        }

        // Create the collection of directories that the user is interested in...
        Set<String> candidateAppendedPaths = new HashSet<>();
        candidateAppendedPaths.add(this.clientAPIContextImpl.getAppendedPath());
        if (this.clientAPIContextImpl.getRecurseFlag()) {
            Iterator<String> it = clientAPIContextImpl.getAppendedPathMap().keySet().iterator();
            while (it.hasNext()) {
                String candidate = it.next();
                if (candidate.startsWith(clientAPIContextImpl.getAppendedPath())) {
                    candidateAppendedPaths.add(candidate);
                }
            }
        }

        // Create the directory proxys for the set of directories the user is interested in.
        Iterator<String> it = candidateAppendedPaths.iterator();
        while (it.hasNext()) {
            String interestingAppendedPath = it.next();
            createDirectoryManager(interestingAppendedPath);
        }

        // Create the collection of FileInfo objects.
        Map<String, FileInfo> sortedFileInfo = new TreeMap<>();
        Iterator<ArchiveDirManagerProxy> proxyIterator = this.clientAPIContextImpl.getArchiveDirManagerProxyMap().values().iterator();
        while (proxyIterator.hasNext()) {
            ArchiveDirManagerProxy archiveDirManagerProxy = proxyIterator.next();
            Iterator<ArchiveInfoInterface> archiveInfoIterator = archiveDirManagerProxy.getArchiveInfoCollection().values().iterator();
            while (archiveInfoIterator.hasNext()) {
                ArchiveInfoInterface archiveInfo = archiveInfoIterator.next();
                sortedFileInfo.put(archiveInfo.getShortWorkfileName(), new FileInfoImpl(archiveInfo, archiveDirManagerProxy.getAppendedPath()));
            }
        }

        return new ArrayList<>(sortedFileInfo.values());
    }

    private List<String> populateProjectList() throws ClientAPIException {
        List<String> projectList;

        // Validate the input parameters.
        validateAPIContextGetProjectList();

        // Login to the server.
        login();

        // Wait for the project list from the server.
        logger.log(Level.INFO, "Client API waiting for project list from server...");
        waitForProjectList();

        projectList = new ArrayList<>(clientAPIContextImpl.getServerProjectNames().length);
        projectList.addAll(Arrays.asList(clientAPIContextImpl.getServerProjectNames()));

        return projectList;
    }

    private List<RevisionInfo> populateRevisionInfoList() throws ClientAPIException {

        // Populate the file info list (which will populate the project list, and the view list as well, and the directory info list).
        populateFileInfoList();

        // Validate the input parameters.
        validateAPIContextGetRevisionInfoList();

        // Make sure the requested file actually exists on the server.
        LogFileProxy logFileProxy = (LogFileProxy) clientAPIContextImpl.getArchiveDirManagerProxyMap().get(clientAPIContextImpl
                .getAppendedPath()).getArchiveInfo(clientAPIContextImpl.getFileName());
        if (logFileProxy == null) {
            throw new ClientAPIException("Requested file [" + clientAPIContextImpl.getFileName() + "] not found in directory: [" + clientAPIContextImpl.getAppendedPath() + "]");
        }

        // Wait for revision detail from the server.
        logger.log(Level.INFO, "Client API waiting for revision detail from server...");
        LogfileInfo logFileInfo = logFileProxy.getLogfileInfo();

        // And populate the list with the revision info.
        List<RevisionInfo> revisionInfoList = new ArrayList<>(logFileProxy.getRevisionCount());
        for (int i = 0; i < logFileProxy.getRevisionCount(); i++) {
            RevisionInfoImpl revisionInfoImpl = new RevisionInfoImpl(logFileInfo, logFileInfo.getRevisionInformation().getRevisionHeader(i));
            revisionInfoList.add(revisionInfoImpl);
        }

        return revisionInfoList;
    }

    private List<String> populateViewList() throws ClientAPIException {

        List<String> viewList;

        // Populate the project list...
        populateProjectList();

        // Validate the input parameters.
        validateAPIContextGetViewList();

        // Make sure the requested project exists.
        boolean requestedProjectFoundFlag = false;
        for (String projectName : clientAPIContextImpl.getServerProjectNames()) {
            if (0 == projectName.compareToIgnoreCase(clientAPIContextImpl.getProjectName())) {
                requestedProjectFoundFlag = true;
                break;
            }
        }

        if (!requestedProjectFoundFlag) {
            throw new ClientAPIException("Requested project not found: " + clientAPIContextImpl.getProjectName());
        }

        waitForViewList();
        viewList = new ArrayList<>(clientAPIContextImpl.getProjectViewNames().length);
        viewList.addAll(Arrays.asList(clientAPIContextImpl.getProjectViewNames()));
        return viewList;
    }

    private void validateAPIContextGetDirectoryList() throws ClientAPIException {
    }

    private void validateAPIContextGetFileInfoList() throws ClientAPIException {
        // Make sure the user supplied an appended path.
        if (this.clientAPIContextImpl.getAppendedPath() == null) {
            throw new ClientAPIException("You must define an appended path");
        }
    }

    private void validateAPIContextGetProjectList() throws ClientAPIException {
        // Make sure the user supplied the required parameters for a getProjectList request.
        if (this.clientAPIContextImpl.getUserName() == null || this.clientAPIContextImpl.getUserName().length() == 0) {
            throw new ClientAPIException("You must define a user name");
        }

        if (this.clientAPIContextImpl.getPassword() == null || this.clientAPIContextImpl.getPassword().length() == 0) {
            throw new ClientAPIException("You must supply a password");
        }

        if (this.clientAPIContextImpl.getServerIPAddress() == null || this.clientAPIContextImpl.getServerIPAddress().length() == 0) {
            throw new ClientAPIException("You must supply the server IP address");
        }

        if (this.clientAPIContextImpl.getPort() == null) {
            throw new ClientAPIException("You must define the server port.");
        }
    }

    private void validateAPIContextGetRevisionInfoList() throws ClientAPIException {
        if (this.clientAPIContextImpl.getFileName() == null || this.clientAPIContextImpl.getFileName().length() == 0) {
            throw new ClientAPIException("You must supply a filename.");
        }
    }

    private void validateAPIContextGetViewList() throws ClientAPIException {
        // Make sure they supplied a project name as well.
        if (this.clientAPIContextImpl.getProjectName() == null || this.clientAPIContextImpl.getProjectName().length() == 0) {
            throw new ClientAPIException("You must supply a project name.");
        }
    }

    private void waitForProjectList() {
        ClientRequestListClientProjectsData projectsData = new ClientRequestListClientProjectsData();
        projectsData.setServerName(SERVER_NAME);
        synchronized (clientAPIContextImpl.getSyncObject()) {
            try {
                clientAPIContextImpl.getTransportProxy().write(projectsData);
                clientAPIContextImpl.getSyncObject().wait();
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Interrupted exception waiting for project list.");
            }
        }
    }

    private void waitForViewList() {
        synchronized (clientAPIContextImpl.getSyncObject()) {
            try {
                TransportProxyFactory.getInstance().requestViewList(clientAPIContextImpl.getServerProperties(), this.clientAPIContextImpl.getProjectName());
                clientAPIContextImpl.getSyncObject().wait();
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Interrupted exception waiting for view list.");
            }
        }
    }

    private Date fetchMostRecentActivity() throws ClientAPIException {
        Date mostRecentActivity = null;

        // Login to the server.
        login();

        ClientRequestGetMostRecentActivityData request = new ClientRequestGetMostRecentActivityData();
        request.setProjectName(this.clientAPIContextImpl.getProjectName());
        request.setViewName(this.clientAPIContextImpl.getViewName());
        request.setAppendedPath(this.clientAPIContextImpl.getAppendedPath());
        synchronized (clientAPIContextImpl.getSyncObject()) {
            try {
                clientAPIContextImpl.getTransportProxy().write(request);
                clientAPIContextImpl.getSyncObject().wait();
                mostRecentActivity = clientAPIContextImpl.getMostRecentActivity();
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Interrupted exception waiting for most recent activity.");
            }
        }
        return mostRecentActivity;
    }
}
