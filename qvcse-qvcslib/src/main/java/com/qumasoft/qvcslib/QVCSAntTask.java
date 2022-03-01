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

import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestMoveFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestRenameData;
import com.qumasoft.qvcslib.response.ServerResponseChangePassword;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QVCS ant task. A custom ant task for performing QVCS tasks from within an ant script. This custom ant task supports the following operations:
 <ul>
 * <li>get</li>
 * <li>checkin</li>
 * <li>move</li>
 * <li>rename</li>
 * <li>delete</li>
 * <li>report</li>
 * </ul>
 *
 * @author Jim Voris
 */
public final class QVCSAntTask extends org.apache.tools.ant.Task implements ChangeListener, PasswordChangeListenerInterface, TransportProxyListenerInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(QVCSAntTask.class);

    // The operations that we support.
    static final String OPERATION_GET = "get";
    static final String OPERATION_CHECKIN = "checkin";
    static final String OPERATION_MOVE = "move";
    static final String OPERATION_RENAME = "rename";
    static final String OPERATION_DELETE = "delete";
    static final String OPERATION_REPORT = "report";
    private static final String SKIPPING = "Skipping [";
    private static final String BRACKET_IN = "] in [";
    private static final long ONE_HUNDRED_MILLISECONDS = 100;
    private static final long ONE_SECOND = 1_000;
    private static final long TEN_SECONDS = 10_000;
    private String pendingPassword;
    private final AtomicReference<String> passwordResponse = new AtomicReference<>(QVCSConstants.QVCS_NO);
    private TransportProxyInterface transportProxy = null;
    private String[] serverProjectNames = null;
    private Properties[] projectProperties = null;
    private RemoteProjectProperties remoteProjectProperties = null;
    private final Object classSyncObject = new Object();
    private final Map<String, Object> appendedPathMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Object> prospectiveAppendedPathCollection = Collections.synchronizedMap(new HashMap<>());
    private int operationCount = 0;

    private boolean overWriteFlag;
    private boolean recurseFlag = true;
    private String userDirectory;
    private String serverName;
    private String userName;
    private String password;
    private String projectName;
    private String branchName;
    private String appendedPath;
    private String workfileLocation;
    private String operation;
    private String fileName;
    private String fileExtension;
    private String moveToAppendedPath;
    private String renameToFileName;
    private String checkInComment;
    private String reportFilesWithStatus;

    /**
     * Set the overwrite flag.
     * @param flag the overwrite flag.
     */
    public void setOverWriteFlag(boolean flag) {
        this.overWriteFlag = flag;
    }

    /**
     * Set the recurse flag.
     * @param flag the recurse flag.
     */
    public void setRecurseFlag(boolean flag) {
        this.recurseFlag = flag;
    }

    /**
     * Set the user directory.
     * @param directory the user directory.
     */
    public void setUserDirectory(String directory) {
        this.userDirectory = directory;
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
    public void setServerName(String server) {
        this.serverName = server;
    }

    /**
     * Set the user name.
     * @param user the user name.
     */
    public void setUserName(String user) {
        this.userName = user;
    }

    /**
     * Set the password.
     * @param arg the password.
     */
    public void setPassword(String arg) {
        this.password = arg;
    }

    /**
     * Set the project name.
     * @param project the project name.
     */
    public void setProjectName(String project) {
        this.projectName = project;
    }

    /**
     * Set the branch name.
     * @param branch the branch name.
     */
    public void setBranchName(String branch) {
        this.branchName = branch;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
    }

    /**
     * Set the workfile location.
     * @param location the workfile location.
     */
    public void setWorkfileLocation(String location) {
        this.workfileLocation = location;
    }

    /**
     * Set the operation to perform. This must be one of the following:
     * <ul>
     * <li>get</li>
     * <li>label</li>
     * <li>checkin</li>
     * <li>checkout</li>
     * <li>lock</li>
     * <li>unlock</li>
     * <li>move</li>
     * <li>rename</li>
     * <li>delete</li>
     * <li>report</li>
     * </ul>
     * @param op the operation to perform.
     */
    public void setOperation(String op) {
        this.operation = op;
    }

    /**
     * Set the filename.
     * @param arg the filename.
     */
    public void setFileName(String arg) {
        this.fileName = arg;
    }

    /**
     * Set the move to appended path (for move operations).
     * @param path the move to appended path
     */
    public void setMoveToAppendedPath(String path) {
        this.moveToAppendedPath = path;
    }

    /**
     * Set the rename to filename.
     * @param arg the rename to filename.
     */
    public void setRenameToFileName(String arg) {
        this.renameToFileName = arg;
    }

    /**
     * Set the file extension.
     * @param arg the file extension.
     */
    public void setFileExtension(String arg) {
        this.fileExtension = arg;
    }

    /**
     * Set the checkin comment.
     * @param comment the checkin comment.
     */
    public void setCheckInComment(String comment) {
        this.checkInComment = comment;
    }

    /**
     * Set the status for reporting files with the given status.
     * @param status the status for reporting files with the given status.
     */
    public void setReportFilesWithStatus(String status) {
        this.reportFilesWithStatus = status;
    }

    private boolean login() {
        boolean resultFlag = false;

        ServerProperties serverProperties = new ServerProperties(userDirectory, serverName);

        // The type of transport we'll use...
        TransportProxyType transportType = serverProperties.getClientTransport();

        // The port we'll connect on...
        int port = serverProperties.getClientPort();

        // Hash the password...
        byte[] hashedPassword = Utility.getInstance().hashPassword(password);

        // Initialize the workfile digest manager
        WorkfileDigestManager.getInstance().initialize();

        // And force the login to the transport...
        TransportProxyFactory.getInstance().setDirectory(userDirectory);
        TransportProxyFactory.getInstance().addChangeListener(this);
        TransportProxyFactory.getInstance().addChangedPasswordListener(this);
        ServerManager.getServerManager().addChangeListener(this);

        synchronized (passwordResponse) {
            try {
                transportProxy = TransportProxyFactory.getInstance().getTransportProxy(transportType, serverProperties, port, userName, hashedPassword, this, null);

                // Wait 10 seconds for a response.
                passwordResponse.wait(TEN_SECONDS);
            } catch (InterruptedException e) {
                log(Utility.expandStackTraceToString(e));

                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }

        if (passwordResponse.get().equals(QVCSConstants.QVCS_YES)) {
            String msg = "Successful login in to server as user: '" + userName + "'";
            log(msg, Project.MSG_VERBOSE);
            resultFlag = true;
        } else {
            log("Failed to login to server", Project.MSG_WARN);
        }
        return resultFlag;
    }

    /**
     * Execute the qvcs ant task.
     *
     * @throws org.apache.tools.ant.BuildException
     */
    @Override
    public void execute() {
        boolean loggedInFlag = false;
        operationCount = 0;

        try {
            System.setProperty("user.dir", userDirectory);

            // Make sure they defined the needed properties.
            validateTaskProperties();

            // Report what properties will be used.
            reportTaskProperties();

            // Login to the server.
            loggedInFlag = login();
            if (!loggedInFlag) {
                throw new BuildException("Failed to login to server.");
            }

            // Wait for the project list from the server.
            log("Getting project list from server...");
            waitForProjectList();

            // Get the files we may be able to work on from the server.
            DirectoryManagerFactory.getInstance().setServerUsername(serverName, userName);
            DirectoryManagerFactory.getInstance().setServerPassword(serverName, password);

            // Create a canonical representation for the workfile location.
            createCanonicalWorkfileLocation();

            // We need to get a directory manager for the root of the project so we'll get the additional
            // segments that exist for this project.  We'll then have to use those to figure out the
            // set of directory managers that we need to build in order to satisfy the users request.
            log("Getting directory list from server...");
            createDirectoryManager("");

            log("Getting file list from server...");
            createDirectoryManagerCollection();

            log("Performing requested operation...");
            performRequestedOperation();
        } catch (QVCSException | BuildException e) {
            String msg = "Caught exception: " + e.getClass().getName() + " exception: " + e.getLocalizedMessage();
            log(msg, Project.MSG_WARN);
            log(Utility.expandStackTraceToString(e));
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            if (loggedInFlag) {
                try {
                    Thread.sleep(ONE_SECOND);
                } catch (InterruptedException e) {
                    log(Utility.expandStackTraceToString(e));

                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
                logout();
                log("Logged off server");

                // Write the stores that we may have changed.
                WorkfileDigestManager.getInstance().writeStore();

                try {
                    Thread.sleep(ONE_SECOND);
                } catch (InterruptedException e) {
                    log(Utility.expandStackTraceToString(e));

                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void createCanonicalWorkfileLocation() throws QVCSException {
        String basedir = getProject().getProperty("basedir");
        log("Basedir: '" + basedir + "'");
        String localWorkfileLocation = workfileLocation;

        // Create a canonical name for the workfile path if we're doing a get
        if (workfileLocation != null) {
            // This only needs to be done for relative pathnames.
            byte[] workfileLocationBytes = workfileLocation.getBytes();
            if ((workfileLocationBytes[0] != File.separatorChar) && (workfileLocationBytes[1] != ':')) {
                localWorkfileLocation = basedir + File.separator + workfileLocation;
            }

            try {
                File workfile = new File(localWorkfileLocation);
                String canonicalFilename = workfile.getCanonicalPath();
                workfileLocation = canonicalFilename;
                log("Workfile canonical location: '" + workfileLocation + "'");
            } catch (java.io.IOException e) {
                throw new QVCSException(e.getLocalizedMessage());
            }
        }
    }

    private void logout() {
        transportProxy.close();
    }

    private void waitForProjectList() {
        ClientRequestListClientProjectsData projectsData = new ClientRequestListClientProjectsData();
        projectsData.setServerName(serverName);
        synchronized (classSyncObject) {
            try {
                transportProxy.write(projectsData);
                classSyncObject.wait();
            } catch (InterruptedException e) {
                log(Utility.expandStackTraceToString(e));

                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
    }

    private AbstractProjectProperties getProjectProperties() {
        if (remoteProjectProperties == null) {
            Properties remoteProperties = null;
            for (int i = 0; i < serverProjectNames.length; i++) {
                if (serverProjectNames[i].equals(projectName)) {
                    remoteProperties = projectProperties[i];
                    String msg = "Matched project: [" + serverProjectNames[i] + "]; Index: " + i;
                    log(msg, Project.MSG_VERBOSE);
                    break;
                }
            }
            remoteProjectProperties = new RemoteProjectProperties(projectName, remoteProperties);
        }
        return remoteProjectProperties;
    }

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        try {
            String msg = "Change Event: [" + e.getSource().getClass().getName() + "]";
            log(msg, Project.MSG_VERBOSE);
            Object source = e.getSource();
            if (source instanceof ServerResponseListProjects) {
                msg = "State Changed; 515, Received list of projects for server: [" + serverName + "]";
                log(msg, Project.MSG_VERBOSE);
                synchronized (classSyncObject) {
                    ServerResponseListProjects projectList = (ServerResponseListProjects) source;
                    String[] projectNames = projectList.getProjectList();
                    projectProperties = projectList.getPropertiesList();
                    for (String projectName1 : projectNames) {
                        log(projectName1, Project.MSG_VERBOSE);
                    }
                    serverProjectNames = projectNames;
                    classSyncObject.notifyAll();
                }
            } else if (source instanceof ServerResponseProjectControl) {
                ServerResponseProjectControl projectControl = (ServerResponseProjectControl) source;
                if (projectControl.getAddFlag()) {
                    String[] segments = projectControl.getDirectorySegments();

                    // Create appended path for this prospective directory manager
                    String localAppendedPath = Utility.createAppendedPathFromSegments(segments);
                    msg = "State Changed; 534 appendedPath: [" + localAppendedPath + "]";
                    log(msg, Project.MSG_VERBOSE);

                    // Add the appendedPath to the collection of appended paths for prospective directory
                    // managers.  We'll create a sync object that we can use to synchronize on that
                    // directory manager, if we wind up needing to create a directory manager.
                    // (At this point we are simply creating the entire set of appended paths
                    // that exist for a given project... we will have to use some subset of that
                    // collection, based on the appended path of the user's request).
                    prospectiveAppendedPathCollection.put(localAppendedPath, new Object());
                }
            } else if (source instanceof ArchiveDirManagerProxy) {
                ArchiveDirManagerProxy archiveDirManager = (ArchiveDirManagerProxy) source;
                String localAppendedPath = archiveDirManager.getAppendedPath();
                log("State Changed; 548 appendedPath: " + localAppendedPath, Project.MSG_VERBOSE);
                Object syncObject = null;

                while (syncObject == null) {
                    syncObject = appendedPathMap.get(localAppendedPath);
                    if (syncObject == null) {
                        // This can happen if we get the response from the server before
                        // we've had a chance to populate the appendPathMap with an
                        // entry for the given directory.
                        log("Did not find synchronization object for: [" + localAppendedPath + "]. Waiting for server response...");
                        LOGGER.warn("Did not find synchronization object for: [" + localAppendedPath + "]. Waiting for server response...");
                        Thread.sleep(ONE_HUNDRED_MILLISECONDS);
                    }
                }
                synchronized (syncObject) {
                    syncObject.notifyAll();
                }
            } else if (source != null) {
                msg = "stateChanged received unexpected object of type " + source.getClass().getName();
                log(msg, Project.MSG_WARN);
                LOGGER.warn(msg);
            }
        } catch (InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getPendingPassword(String name) {
        return pendingPassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyLoginResult(ServerResponseLogin response) {
        synchronized (passwordResponse) {
            if (response.getLoginResult()) {
                if (response.getVersionsMatchFlag()) {
                    passwordResponse.set(QVCSConstants.QVCS_YES);
                } else {
                    log("Client jar file is out of date.", Project.MSG_WARN);
                    passwordResponse.set(QVCSConstants.QVCS_NO);
                }
            } else {
                passwordResponse.set(QVCSConstants.QVCS_NO);
            }
            passwordResponse.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUpdateComplete() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyPasswordChange(ServerResponseChangePassword response) {
    }

    @Override
    public void savePendingPassword(String server, String arg) {
        pendingPassword = arg;
    }

    /**
     * Create the collection of directory managers that this execute request will use. The collection is the set of directories that are part of the directory tree corresponding to
     * the user-provided appended path.
     */
    private void createDirectoryManagerCollection() {
        Set appendedPathsSet = prospectiveAppendedPathCollection.keySet();
        Iterator it = appendedPathsSet.iterator();
        while (it.hasNext()) {
            String localAppendedPath = (String) it.next();
            log("createDirectoryManagerCollection appended path: [" + localAppendedPath + "]", Project.MSG_VERBOSE);
            if (appendedPath.length() > 0) {
                if (localAppendedPath.startsWith(appendedPath)) {
                    if (recurseFlag) {
                        createDirectoryManager(localAppendedPath);
                    } else {
                        // We are not recursing directories, so we only need the
                        // one directory.
                        if (0 == localAppendedPath.compareTo(appendedPath)) {
                            createDirectoryManager(localAppendedPath);
                            break;
                        }
                    }
                }
            } else {
                // We already created the directory manager for the project
                // root directory.... don't create it again.
                if (localAppendedPath.length() > 0) {
                    if (recurseFlag) {
                        createDirectoryManager(localAppendedPath);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private void createDirectoryManager(String path) {
        String workfileDirectoryName;

        if (path.length() == 0) {
            workfileDirectoryName = workfileLocation;
        } else {
            workfileDirectoryName = workfileLocation + File.separator + path;
            log("path: [" + path + "] workfileDirectoryName: [" + workfileDirectoryName + "]", Project.MSG_VERBOSE);
        }

        Object syncObject = new Object();
        synchronized (syncObject) {
            try {
                DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(serverName, projectName, branchName,
                        path, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE);
                if (directoryManager == null) {
                    // Save the sync object for future use...
                    appendedPathMap.put(path, syncObject);

                    // Lookup or create the directory manager.
                    DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, path);
                    directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(userDirectory, serverName, directoryCoordinate,
                            QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, getProjectProperties(), workfileDirectoryName, this, true);

                    // Wait for the response from the server.
                    String msg = "Waiting for server response for appended path: [" + path + "]";
                    log(msg, Project.MSG_VERBOSE);

                    syncObject.wait();

                    msg = "Received server response for appended path: [" + directoryManager.getAppendedPath() + "]";
                    log(msg, Project.MSG_VERBOSE);
                } else {
                    String msg = "Found existing directory manager for: [" + path + "]";
                    log(msg);
                }
            } catch (InterruptedException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);

                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * This is where we perform the version control operation on the list of files we got from the server.
     */
    private void performRequestedOperation() {
        MyTransactionProgressListener myTransactionProgressListener = null;
        try {
            // Get rid of the project root directory manager's map entry if we can.
            if (operation.equals(OPERATION_MOVE)) {
                if (appendedPath.length() > 0 && moveToAppendedPath.length() > 0) {
                    // We don't need the directory manager for the project root since that directory manager won't be used.
                    appendedPathMap.remove("");
                }
            } else {
                if (appendedPath.length() > 0) {
                    // We don't need the directory manager for the project root since that directory manager won't be used.
                    appendedPathMap.remove("");
                }
            }

            // Set up a listener to listen for the end to the transaction that we wrap around all this work
            // so we can use that listener to notify us when the the server has completed its work.
            Object transactionCompleteSyncObject = new Object();
            myTransactionProgressListener = new MyTransactionProgressListener(transactionCompleteSyncObject);
            ClientTransactionManager.getInstance().addTransactionInProgressListener(myTransactionProgressListener);
            int transactionId = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);

            Set appendedPathsSet = appendedPathMap.keySet();
            Iterator it = appendedPathsSet.iterator();
            while (it.hasNext()) {
                String localAppendedPath = (String) it.next();
                String msg = "Performing [" + operation + "] for directory: [" + localAppendedPath + "]";
                log(msg, Project.MSG_VERBOSE);

                // Lookup up the directory manager for this directory.
                DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(serverName, projectName, branchName,
                        localAppendedPath, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE);
                directoryManager.mergeManagers();
                log("Workfile directory for appended path of [" + localAppendedPath + "]:" + directoryManager.getWorkfileDirectoryManager().getWorkfileDirectory(),
                        Project.MSG_VERBOSE);

                // Iterate over the collection of archives in this directory.
                Collection mergedInfoCollection = directoryManager.getMergedInfoCollection();
                Iterator mergedInfoIterator = mergedInfoCollection.iterator();
                while (mergedInfoIterator.hasNext()) {
                    MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoIterator.next();
                    if (mergedInfo.getArchiveInfo() != null) {
                        String shortWorkfileName = mergedInfo.getArchiveInfo().getShortWorkfileName();
                        String lowerCaseShortWorkfileName = shortWorkfileName.toLowerCase();

                        // Filter out extensions...
                        if (0 != fileExtension.compareTo("*")) {
                            if (fileExtension.length() == 0) {
                                // File should have no extension...
                                if (shortWorkfileName.contains(".")) {
                                    msg = SKIPPING + shortWorkfileName
                                            + "] because it has a file extension but the file extension attribute is accepting files without a file extension.";
                                    log(msg, Project.MSG_VERBOSE);
                                    continue;
                                }
                            } else if (!lowerCaseShortWorkfileName.endsWith(fileExtension)) {
                                msg = SKIPPING + shortWorkfileName + "] because it doesn't match the extension: " + fileExtension;
                                log(msg, Project.MSG_VERBOSE);
                                continue;
                            }
                        }

                        // Skip files that do not match the filename (if one is defined).
                        if (fileName.length() > 0 && (0 != fileName.compareTo(shortWorkfileName))) {
                            msg = SKIPPING + shortWorkfileName + "] because it doesn't match the file name: " + fileName;
                            log(msg, Project.MSG_VERBOSE);
                            continue;
                        }
                        msg = "Operating on: [" + shortWorkfileName + "]";
                        log(msg, Project.MSG_VERBOSE);
                        switch (operation) {
                            case OPERATION_GET:
                                requestGetOperation(mergedInfo);
                                break;
                            case OPERATION_CHECKIN:
                                requestCheckInOperation(mergedInfo);
                                break;
                            case OPERATION_MOVE:
                                requestMoveOperation(mergedInfo);
                                break;
                            case OPERATION_RENAME:
                                requestRenameOperation(mergedInfo);
                                break;
                            case OPERATION_DELETE:
                                requestDeleteOperation(mergedInfo);
                                break;
                            case OPERATION_REPORT:
                                requestReportOperation(mergedInfo);
                                break;
                            default:
                                msg = "Unsupported operation type: [" + operation + "]";
                                log(msg, Project.MSG_WARN);
                                break;
                        }
                    }
                }
            }
            synchronized (transactionCompleteSyncObject) {
                // Send the end transaction...
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionId);

                // Wait for the end transaction to arrive.
                try {
                    transactionCompleteSyncObject.wait();
                } catch (InterruptedException e) {
                    log(Utility.expandStackTraceToString(e));

                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            }
        } catch (QVCSException e) {
            log("Caught exception while performing operation: " + e.getClass().toString() + ": " + e.getLocalizedMessage(), Project.MSG_WARN);
            log(Utility.expandStackTraceToString(e));
        } finally {
            log("Performed [" + operationCount + "] operations");
            if (myTransactionProgressListener != null) {
                ClientTransactionManager.getInstance().removeTransactionInProgressListener(myTransactionProgressListener);
            }
        }
    }

    private boolean requestGetOperation(MergedInfoInterface mergedInfo) {
        // Use flag to indicate whether the operation was requested.
        boolean flag = false;

        String appendedPathSuffix = mergedInfo.getArchiveDirManager().getAppendedPath().substring(appendedPath.length());
        String workfileAppendedPath = workfileLocation + File.separator + appendedPathSuffix;
        String fullWorkfileName = workfileAppendedPath + File.separator + mergedInfo.getShortWorkfileName();

        // Figure out the command line arguments for this file.
        GetRevisionCommandArgs commandArgs = new GetRevisionCommandArgs();
        commandArgs.setFullWorkfileName(fullWorkfileName);
        commandArgs.setOutputFileName(fullWorkfileName);
        if (overWriteFlag) {
            commandArgs.setOverwriteBehavior(Utility.OverwriteBehavior.REPLACE_WRITABLE_FILE);
        } else {
            commandArgs.setOverwriteBehavior(Utility.OverwriteBehavior.DO_NOT_REPLACE_WRITABLE_FILE);
        }
        commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
        commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
        commandArgs.setUserName(mergedInfo.getUserName());

        try {
            if (mergedInfo.getRevision(commandArgs, fullWorkfileName)) {
                operationCount++;
                flag = true;
            }
        } catch (QVCSException e) {
            log(e.getLocalizedMessage());
        }
        return flag;
    }

    private boolean requestCheckInOperation(MergedInfoInterface mergedInfo) {
        log("Check in operation; evaluating : [" + mergedInfo.getFullWorkfileName() + "] has status of: " + mergedInfo.getStatusString(), Project.MSG_VERBOSE);

        // Use flag to indicate whether the operation was requested.
        boolean flag = false;

        // Skip any files that have an status that is unacceptable. The only statuses that
        // we will checkin are 'Current' (which will result in an unlocked file), and
        // 'Your copy changed'. Other status values will result in skipping the file.
        if ((mergedInfo.getStatusIndex() != MergedInfoInterface.YOUR_COPY_CHANGED_STATUS_INDEX)
                && (mergedInfo.getStatusIndex() != MergedInfoInterface.CURRENT_STATUS_INDEX)) {
            if (mergedInfo.getStatusIndex() == MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX) {
                log(SKIPPING + mergedInfo.getArchiveInfo().getShortWorkfileName() + BRACKET_IN + mergedInfo.getArchiveDirManager().getAppendedPath()
                        + "] directory because a merge is required.");
            } else if (mergedInfo.getStatusIndex() == MergedInfoInterface.DIFFERENT_STATUS_INDEX) {
                log(SKIPPING + mergedInfo.getArchiveInfo().getShortWorkfileName() + BRACKET_IN + mergedInfo.getArchiveDirManager().getAppendedPath()
                        + "] directory because status is 'Different'.");
            } else if (mergedInfo.getStatusIndex() == MergedInfoInterface.STALE_STATUS_INDEX) {
                log(SKIPPING + mergedInfo.getArchiveInfo().getShortWorkfileName() + BRACKET_IN + mergedInfo.getArchiveDirManager().getAppendedPath()
                        + "] directory because status is 'Stale'.", Project.MSG_VERBOSE);
            } else if (mergedInfo.getStatusIndex() == MergedInfoInterface.MISSING_STATUS_INDEX) {
                log(SKIPPING + mergedInfo.getArchiveInfo().getShortWorkfileName() + BRACKET_IN + mergedInfo.getArchiveDirManager().getAppendedPath()
                        + "] directory because status is 'Missing'.", Project.MSG_VERBOSE);
            }
            return false;
        }

        // Figure out the command line arguments for this file.
        CheckInCommandArgs commandArgs = new CheckInCommandArgs();
        commandArgs.setUserName(mergedInfo.getUserName());

        commandArgs.setCheckInComment(checkInComment);

        // Create a File associated with the File we check in
        File checkInFile = mergedInfo.getWorkfile();

        commandArgs.setInputfileTimeStamp(new Date(checkInFile.lastModified()));
        commandArgs.setFullWorkfileName(mergedInfo.getFullWorkfileName());
        commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());

        // Set flags;
        commandArgs.setCreateNewRevisionIfEqual(false);
        commandArgs.setProtectWorkfileFlag(false);

        // Set some other values
        commandArgs.setProjectName(mergedInfo.getProjectName());

        String checkInFilename;
        try {
            checkInFilename = checkInFile.getCanonicalPath();
        } catch (IOException e) {
            log(Utility.expandStackTraceToString(e));
            checkInFilename = null;
        }

        // The checkInFilename will be null if we are not able to read it.
        if (checkInFilename != null) {
            try {
                if (mergedInfo.checkInRevision(commandArgs, checkInFilename, false)) {
                    operationCount++;
                    flag = true;
                }
            } catch (QVCSException e) {
                log(Utility.expandStackTraceToString(e));
                flag = false;
            }
        }

        return flag;
    }

    private boolean requestMoveOperation(MergedInfoInterface mergedInfo) {
        log("Move operation; evaluating : [" + mergedInfo.getFullWorkfileName() + "] has status of: [" + mergedInfo.getStatusString() + "]", Project.MSG_VERBOSE);

        // Use flag to indicate whether the operation was requested.
        boolean flag = true;

        // Skip any files that have an status that is unacceptable. The only statuses that
        // we will move are 'Current' or 'Missing'. Other status values will result in skipping the file.
        int statusIndex = mergedInfo.getStatusIndex();
        if ((statusIndex != MergedInfoInterface.CURRENT_STATUS_INDEX) && (statusIndex != MergedInfoInterface.MISSING_STATUS_INDEX)) {
            log(SKIPPING + mergedInfo.getArchiveInfo().getShortWorkfileName() + BRACKET_IN + mergedInfo.getArchiveDirManager().getAppendedPath()
                    + "] directory because status is not 'Current' or 'Missing'.");
            flag = false;
        }

        // TODO -- this is where the .qvcseignore feature could go.

        if (flag) {
            ClientRequestMoveFileData clientRequestMoveFileData = new ClientRequestMoveFileData();
            clientRequestMoveFileData.setOriginalAppendedPath(appendedPath);
            clientRequestMoveFileData.setProjectName(projectName);
            clientRequestMoveFileData.setBranchName(branchName);
            clientRequestMoveFileData.setShortWorkfileName(fileName);
            clientRequestMoveFileData.setNewAppendedPath(moveToAppendedPath);
            int transactionID = 0;

            try {
                transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                transportProxy.write(clientRequestMoveFileData);
            } finally {
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        }

        return flag;
    }

    private boolean requestRenameOperation(MergedInfoInterface mergedInfo) {
        log("Rename operation; evaluating : [" + mergedInfo.getFullWorkfileName() + "] has status of: [" + mergedInfo.getStatusString() + "]", Project.MSG_VERBOSE);

        // Use flag to indicate whether the operation was requested.
        boolean flag = true;

        // Skip any files that have an status that is unacceptable. The only statuses that
        // we will rename are 'Current' or 'Missing'. Other status values will result in skipping the file.
        int statusIndex = mergedInfo.getStatusIndex();
        if ((statusIndex != MergedInfoInterface.CURRENT_STATUS_INDEX) && (statusIndex != MergedInfoInterface.MISSING_STATUS_INDEX)) {
            log(SKIPPING + mergedInfo.getArchiveInfo().getShortWorkfileName() + BRACKET_IN + mergedInfo.getArchiveDirManager().getAppendedPath()
                    + "] directory because status is not 'Current' or 'Missing'.");
            flag = false;
        }

        // TODO -- This is where the .qvcseignore feature could go.

        if (flag) {
            ClientRequestRenameData clientRequestRenameData = new ClientRequestRenameData();
            clientRequestRenameData.setAppendedPath(appendedPath);
            clientRequestRenameData.setProjectName(projectName);
            clientRequestRenameData.setBranchName(branchName);
            clientRequestRenameData.setOriginalShortWorkfileName(fileName);
            clientRequestRenameData.setNewShortWorkfileName(renameToFileName);
            int transactionID = 0;

            try {
                transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                transportProxy.write(clientRequestRenameData);
            } finally {
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        }

        return flag;
    }

    private boolean requestDeleteOperation(MergedInfoInterface mergedInfo) {
        log("Delete operation; evaluating : [" + mergedInfo.getFullWorkfileName() + "] has status of: [" + mergedInfo.getStatusString() + "]", Project.MSG_VERBOSE);

        // Use flag to indicate whether the operation was requested.
        boolean flag = true;

        if (flag) {
            ClientRequestDeleteFileData clientRequestDeleteFileData = new ClientRequestDeleteFileData();
            clientRequestDeleteFileData.setAppendedPath(appendedPath);
            clientRequestDeleteFileData.setProjectName(projectName);
            clientRequestDeleteFileData.setBranchName(branchName);
            clientRequestDeleteFileData.setShortWorkfileName(fileName);
            int transactionID = 0;

            try {
                transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                transportProxy.write(clientRequestDeleteFileData);
            } finally {
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        }

        return flag;
    }

    private void requestReportOperation(MergedInfoInterface mergedInfo) {
        boolean reportOnFileFlag = true;

        // If a specific status is requested, limit the report to only those files that have that status...
        if (reportFilesWithStatus != null && reportFilesWithStatus.length() > 0) {
            if (!mergedInfo.getStatusString().equals(reportFilesWithStatus)) {
                reportOnFileFlag = false;
            }
        }

        if (reportOnFileFlag) {
            StringBuilder outputString = new StringBuilder();
            RevisionHeader revHeader = mergedInfo.getRevisionInformation().getRevisionHeader(0);
            String revisionCreator = "TODO";
            if (mergedInfo.getArchiveDirManager().getAppendedPath().length() > 0) {
                outputString.append(mergedInfo.getArchiveDirManager().getAppendedPath()).append(QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING);
            }
            outputString.append(mergedInfo.getShortWorkfileName())
                    .append(":")
                    .append(" Revision: ")
                    .append(revHeader.getRevisionString())
                    .append(" check in time: ")
                    .append(revHeader.getCheckInDate().toString())
                    .append(" by ")
                    .append(revisionCreator)
                    .append(" File status: ")
                    .append(mergedInfo.getStatusString());
            System.out.println(outputString.toString());
        }
    }

    private void validateTaskProperties() {
        if (userDirectory == null || userDirectory.length() == 0) {
            log("You must define the userDirectory property");
            throw new BuildException("You must define the userDirectory property");
        }

        if (serverName == null || serverName.length() == 0) {
            log("You must define the serverName property");
            throw new BuildException("You must define the serverName property");
        }

        if (userName == null || userName.length() == 0) {
            log("You must define the userName property");
            throw new BuildException("You must define the userName property");
        }

        if (password == null || password.length() == 0) {
            log("You must define the password property");
            throw new BuildException("You must define the password property");
        }

        if (projectName == null || projectName.length() == 0) {
            log("You must define the projectName property");
            throw new BuildException("You must define the projectName property");
        }

        if (branchName == null || branchName.length() == 0) {
            log("BranchName not defined. Defaulting to the Trunk");
            branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
        }

        if (appendedPath == null) {
            log("You must define the appendedPath property");
            throw new BuildException("You must define the appendedPath property");
        }

        if (fileName == null) {
            fileName = "";
        }

        if (fileExtension == null) {
            fileExtension = "*";
        } else if (0 != fileExtension.compareTo("*")) {
            if (fileExtension.length() > 0) {
                // Make sure the file extension starts with a '.'
                if (!fileExtension.startsWith(".")) {
                    fileExtension = "." + fileExtension;
                }
            }
            fileExtension = fileExtension.toLowerCase();
        }

        if (checkInComment == null) {
            checkInComment = "";
        }

        if (operation == null) {
            operation = OPERATION_GET;
        }
        switch (operation) {
            case OPERATION_GET:
                if (workfileLocation == null || workfileLocation.length() == 0) {
                    log("You must define the workfileLocation property for the get operation.");
                    throw new BuildException("You must define the workfileLocation property for the get operation.");
                }
                break;
            case OPERATION_CHECKIN:
                if (workfileLocation == null || workfileLocation.length() == 0) {
                    log("You must define the workfileLocation property for the checkin operation.");
                    throw new BuildException("You must define the workfileLocation property for the checkin operation.");
                }
                if (checkInComment.length() == 0) {
                    log("A checkin comment is required.");
                    throw new BuildException("You must define the checkInComment property. It is required for the checkin operation.");
                }
                break;
            case OPERATION_REPORT:
                // If report files with status is set, the requested status must be a valid status string.
                if (reportFilesWithStatus != null && reportFilesWithStatus.length() > 0) {
                    String[] validStatusStrings = MergedInfo.getStatusStrings();
                    boolean foundStatusString = false;
                    for (String validStatusString : validStatusStrings) {
                        if (reportFilesWithStatus.equals(validStatusString)) {
                            foundStatusString = true;
                            break;
                        }
                    }
                    if (!foundStatusString) {
                        log("If limiting your report to files with a given status, the status string must be from the following list:");
                        for (String validStatusString : validStatusStrings) {
                            log("\t[" + validStatusString + "]");
                        }
                    }
                }
                break;
            case OPERATION_MOVE:
                if ((fileName == null) || (fileName.length() == 0)) {
                    throw new BuildException("Move operation must supply a fileName");
                }
                if (moveToAppendedPath == null) {
                    throw new BuildException("Move operation must supply a moveToAppendedPath");
                }
                if (moveToAppendedPath.equals(appendedPath)) {
                    throw new BuildException("Origin and destination directories must be different for a move operation.");
                }
                break;
            case OPERATION_RENAME:
                if ((fileName == null) || (fileName.length() == 0)) {
                    throw new BuildException("Rename operation must supply fileName");
                }
                if ((renameToFileName == null) || (renameToFileName.length() == 0)) {
                    throw new BuildException("Rename operation must supply renameToFileName");
                }
                if (fileName.equals(renameToFileName)) {
                    throw new BuildException("Original fileName and renameToFileName must be different for rename operation.");
                }
                break;
            case OPERATION_DELETE:
                if ((fileName == null) || (fileName.length() == 0)) {
                    throw new BuildException("Delete operation must supply filename");
                }
                break;
            default:
                log("operation must be 'get', 'checkin', move', rename, 'delete', or 'report'");
                throw new BuildException("You must define a valid operation.");
        }
    }

    private void reportTaskProperties() {
        log("User directory: " + userDirectory);
        log("Server name: " + serverName);
        log("User name: " + userName);
        log("User password: " + getSpoofedPassword(password));
        log("Project name: " + projectName);
        log("Branch name: " + branchName);
        log("Appended path: " + appendedPath);
        log("Workfile location: " + workfileLocation);
        log("Operation: " + operation);
        log("Recurse flag: " + recurseFlag);
        if (fileName != null) {
            log("File name: " + fileName);
        }
        log("File extension: " + fileExtension);
        if (operation.equals(OPERATION_GET)) {
            log("OverWrite Flag: " + overWriteFlag);
        }
        if (operation.equals(OPERATION_CHECKIN)) {
            log("Check in comment: " + checkInComment);
        }
        if (operation.equals(OPERATION_MOVE)) {
            log("MoveToAppendedPath: " + moveToAppendedPath);
        }
        if (operation.equals(OPERATION_RENAME)) {
            log("RenameToFileName: " + renameToFileName);
        }
        if (operation.equals(OPERATION_REPORT)) {
            if (reportFilesWithStatus != null) {
                log("Report files with status: " + reportFilesWithStatus);
            }
        }
    }

    /**
     * Create a string of '*' that is the same length as the user's password.
     *
     * @param arg the users password.
     * @return a spoofed string that is the same length as the password.
     */
    private String getSpoofedPassword(String arg) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < arg.length(); i++) {
            buffer.append("*");
        }
        return buffer.toString();
    }

    @Override
    public void notifyTransportProxyListener(ServerResponseInterface messageIn) {
        if (messageIn instanceof ServerResponseMessage) {
            ServerResponseMessage message = (ServerResponseMessage) messageIn;

            log(message.getMessage());
        }
    }

    static class MyTransactionProgressListener implements TransactionInProgressListenerInterface {

        private final Object syncObject;

        MyTransactionProgressListener(final Object arg) {
            this.syncObject = arg;
        }

        @Override
        public void setTransactionInProgress(boolean flag) {
            synchronized (syncObject) {
                if (!flag) {
                    syncObject.notifyAll();
                }
            }
        }
    }

}
