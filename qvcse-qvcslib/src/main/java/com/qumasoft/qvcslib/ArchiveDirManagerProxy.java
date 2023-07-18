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

import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestAddDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestCreateArchiveData;
import com.qumasoft.qvcslib.requestdata.ClientRequestRegisterClientListenerData;
import com.qumasoft.qvcslib.requestdata.ClientRequestRenameData;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive directory manager proxy. This is the client-side proxy for the archive directory manager.
 *
 * @author Jim Voris
 */
public final class ArchiveDirManagerProxy extends ArchiveDirManagerBase {

    /**
     * Create our logger object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManagerProxy.class);
    /**
     * An object we use for synchronization for those cases where we cannot synchronize on the LogfileProxy object
     */
    private final Object synchObject = new Object();
    private Integer syncToken = null;

    /**
     * These are the server properties that this object is the proxy for.
     */
    private ServerProperties serverProperties = null;
    /**
     * This is the transport proxy we'll use to send/receive objects to the remote server.
     */
    private TransportProxyInterface transportProxy = null;
    private boolean initCompleteFlag = false;
    private int directoryID = -1;
    private final Object initSyncObject = new Object();
    private Date mostRecentCheckInDate = new Date(0L);

    /**
     * Creates a new instance of ArchiveDirManagerProxy.
     *
     * @param directory where to find the parent directory of where the server properties directory is located.
     * @param serverName the name of the server.
     * @param projectName the project name.
     * @param branchName the name of the branch.
     * @param userName the user's QVCS user name.
     * @param appendedPath the appended path for this directory.
     */
    public ArchiveDirManagerProxy(String directory, String serverName, String projectName, String branchName, String userName,
            String appendedPath) {
        super(projectName, branchName, appendedPath, userName);
        serverProperties = new ServerProperties(directory, serverName);

        transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
    }

    /**
     * Creates a new instance of ArchiveDirManagerProxy. This constructor is used by the ClientAPI.
     *
     * @param projectName the project name.
     * @param servProperties server properties.
     * @param branchName the name of the branch.
     * @param userName the user's QVCS user name.
     * @param appendedPath the appended path for this directory.
     */
    public ArchiveDirManagerProxy(String projectName, ServerProperties servProperties, String branchName, String userName, String appendedPath) {
        super(projectName, branchName, appendedPath, userName);
        serverProperties = servProperties;

        transportProxy = TransportProxyFactory.getInstance().getTransportProxy(servProperties);
    }

    /**
     * Start this instance. Sort of a two step construction. We cannot start the listener threads within the ctor, so we use this method to get things going.
     */
    @Override
    public void startDirectoryManager() {
        // So we can be informed when things happen to the transport.
        transportProxy.addReadListener(this);

        // Register as a listener for this archive directory.
        ClientRequestRegisterClientListenerData clientListener = new ClientRequestRegisterClientListenerData();
        clientListener.setProjectName(getProjectName());
        clientListener.setAppendedPath(getAppendedPath());
        clientListener.setBranchName(getBranchName());
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientListener);
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
    }

    /**
     * Get the transport proxy we use to communicate with the server.
     * @return the transport proxy we use to communicate with the server.
     */
    public TransportProxyInterface getTransportProxy() {
        return transportProxy;
    }

    @Override
    public boolean createDirectory() {
        boolean retVal = false;

        try {
            ClientRequestAddDirectoryData addDirectoryData = new ClientRequestAddDirectoryData();
            addDirectoryData.setAppendedPath(getAppendedPath());
            addDirectoryData.setProjectName(getProjectName());
            addDirectoryData.setBranchName(getBranchName());
            int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, addDirectoryData);
            ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            retVal = true;
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return retVal;
    }

    @Override
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, String fullWorkfilename,
                                 ServerResponseFactoryInterface response) throws IOException, QVCSException {
        boolean retVal = false;
        int length = 0;
        FileInputStream fileInputStream = null;

        ClientRequestCreateArchiveData clientRequest = new ClientRequestCreateArchiveData();

        clientRequest.setProjectName(getProjectName());
        clientRequest.setBranchName(getBranchName());
        clientRequest.setAppendedPath(getAppendedPath());

        clientRequest.setCommandArgs(commandLineArgs);
        try {
            File createFile = new File(fullWorkfilename);

            // Need to read the resulting file into a buffer that we can send to the client.
            fileInputStream = new FileInputStream(createFile);
            length = (int) createFile.length();
            byte[] buffer = new byte[length];
            Utility.readDataFromStream(buffer, fileInputStream);
            clientRequest.setBuffer(buffer);

            // Save the workfile buffer.
            int cacheIndex = ClientWorkfileCache.getInstance().addBuffer(getProjectName(), getBranchName(),
                    getAppendedPath(),
                    Utility.convertWorkfileNameToShortWorkfileName(fullWorkfilename),
                    buffer);
            clientRequest.setIndex(cacheIndex);

            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequest);
            retVal = true;
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } catch (java.lang.OutOfMemoryError e) {
            // If they are trying to create an archive for a really big file,
            // we might have problems.
            LOGGER.warn("Out of memory trying to create archive for: " + fullWorkfilename + ". File size was: " + length);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        return retVal;
    }

    /**
     * Update the archive information for the given workfile.
     *
     * @param shortWorkfileName the short workfile name.
     * @param skinnyLogfileInfo the skinny logfile information.
     */
    public void updateArchiveInfo(String shortWorkfileName, SkinnyLogfileInfo skinnyLogfileInfo) {
        synchronized (getArchiveInfoCollection()) {
            LogFileProxy existingLogFileProxy = (LogFileProxy) getArchiveInfoCollection().get(shortWorkfileName);
            String serverName = this.transportProxy.getServerProperties().getServerName();
            ClientBranchInfo branchInfo = ClientBranchManager.getInstance().getClientBranchInfo(serverName, getProjectName(), getBranchName());
            if (skinnyLogfileInfo != null) {
                updateMostRecentActivityDate(skinnyLogfileInfo.getLastCheckInDate());
                if (existingLogFileProxy != null) {
                    existingLogFileProxy.setSkinnyLogfileInfo(skinnyLogfileInfo);

                    LogFileProxyCache proxyCache = LogFileProxyCacheFactory.getInstance().getLogFileProxyCache(branchInfo.getProjectId());
                    proxyCache.updateLogFileProxy(serverName, getProjectName(), branchInfo, existingLogFileProxy);
                } else {
                    LogFileProxy logFileProxy = new LogFileProxy(skinnyLogfileInfo, this);
                    getArchiveInfoCollection().put(shortWorkfileName, logFileProxy);

                    LogFileProxyCache proxyCache = LogFileProxyCacheFactory.getInstance().getLogFileProxyCache(branchInfo.getProjectId());
                    proxyCache.updateLogFileProxy(serverName, getProjectName(), branchInfo, logFileProxy);
                }
            } else {
                // remove this entry from the container.
                LogFileProxy logFileProxy = (LogFileProxy) getArchiveInfoCollection().remove(shortWorkfileName);
                if (logFileProxy != null) {
                    LogFileProxyCache proxyCache = LogFileProxyCacheFactory.getInstance().getLogFileProxyCache(branchInfo.getProjectId());
                    proxyCache.removeLogFileProxy(branchInfo.getBranchId(), logFileProxy.getFileID());
                }
            }
        }
    }

    /**
     * Remove the archive info from our collection. This does NOT delete the file. It merely removes the file from our collection.
     * @param shortWorkfileName the short workfile name.
     */
    public void removeArchiveInfo(String shortWorkfileName) {
        // It doesn't matter whether this is already here or not... we are
        // going to remove it from the container.
        synchronized (getArchiveInfoCollection()) {
            // Remove this entry from the container.  The server's copy
            // of the archive is gone, or is obsolete.
            LogFileProxy logFileProxy = (LogFileProxy) getArchiveInfoCollection().remove(shortWorkfileName);
            if (logFileProxy != null) {
                ClientBranchInfo branchInfo = ClientBranchManager.getInstance().getClientBranchInfo(this.transportProxy.getServerProperties().getServerName(),
                        getProjectName(), getBranchName());
                LogFileProxyCache proxyCache = LogFileProxyCacheFactory.getInstance().getLogFileProxyCache(branchInfo.getProjectId());
                proxyCache.removeLogFileProxy(branchInfo.getBranchId(), logFileProxy.getFileID());
            }
        }
    }

    /**
     * We received a String message from the server. Notify our listeners.
     * @param messageString the message.
     */
    public void updateInfo(String messageString) {
        LOGGER.info(messageString);
        notifyListeners();
    }

    /**
     * We received a message from the server that might be useful. Notify any of our change listeners.
     * @param message the message from the server.
     */
    public void updateInfo(ServerResponseMessage message) {
        synchronized (getChangeListenerArray()) {
            Object[] listeners = getChangeListenerArray().getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                javax.swing.event.ChangeEvent event = new javax.swing.event.ChangeEvent(message);
                ((ChangeListener) listeners[i + 1]).stateChanged(event);
            }
        }
    }

    /**
     * Get the server properties.
     * @return the server properties.
     */
    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    /**
     * Indicate that initialization is complete; notify any other threads that may be waiting.
     */
    public void setInitComplete() {
        synchronized (initSyncObject) {
            initCompleteFlag = true;
            initSyncObject.notifyAll();
        }
    }

    /**
     * Wait for initialization to complete.
     */
    public void waitForInitToComplete() {
        synchronized (initSyncObject) {
            if (!initCompleteFlag) {
                try {
                    initSyncObject.wait();
                } catch (InterruptedException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);

                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public boolean renameArchive(String userName, String oldShortWorkfileName, String newShortWorkfileName, ServerResponseFactoryInterface response) {
        try {
            ClientRequestRenameData renameData = new ClientRequestRenameData();
            renameData.setUserName(userName);
            renameData.setAppendedPath(getAppendedPath());
            renameData.setProjectName(getProjectName());
            renameData.setBranchName(getBranchName());
            renameData.setNewShortWorkfileName(newShortWorkfileName);
            renameData.setOriginalShortWorkfileName(oldShortWorkfileName);

            // The rename operation MUST be wrapped in a transaction.
            int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, renameData);
            ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return true;
    }

    @Override
    public int getDirectoryID() {
        return directoryID;
    }

    /**
     * Set the directory id.
     * @param dirID the directory id.
     */
    public void setDirectoryID(int dirID) {
        this.directoryID = dirID;
    }

    /**
     * This is just a placeholder method to satisfy the interface.
     * @return N/A
     */
    @Override
    public long getOldestRevision() {
        return 0L;
    }

    /**
     * This is just a placeholder method to satisfy the interface.
     * @param response N/A
     */
    @Override
    public void addLogFileListener(ServerResponseFactoryInterface response) {
    }

    /**
     * This is just a placeholder method to satisfy the interface.
     * @param response N/A
     */
    @Override
    public void removeLogFileListener(ServerResponseFactoryInterface response) {
    }

    @Override
    public boolean moveArchive(String userName, String shortWorkfileName, final ArchiveDirManagerInterface targetArchiveDirManager,
                               ServerResponseFactoryInterface response) throws IOException, QVCSException {
        // This is not implemented on the client proxy.
        throw new UnsupportedOperationException("This is not implemented on the client proxy.");
    }

    @Override
    public boolean deleteArchive(String userName, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        // This is not implemented on the client proxy.
        throw new UnsupportedOperationException("This is not implemented on the client proxy.");
    }

    @Override
    public boolean unDeleteArchive(String userName, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        // This is not implemented on the client proxy.
        throw new UnsupportedOperationException("This is not implemented on the client proxy.");
    }

    /**
     * Get the sync object that we use for this directory. We use this to synchronize synchronous calls for the cases where we cannot directly use the LogfileProxy object for
     * synchronization. For example, when promoting a create from a feature branch, we cannot synchronize on the LogfileProxy object because notifications that we receive
     * before we receive the response will already have caused the LogfileProxy object to have been removed from the list of files held by this directory manager.
     *
     * @return the sync object for this directory manager.
     */
    public Object getSynchronizationObject() {
        return synchObject;
    }

    @Override
    public Date getMostRecentActivityDate() {
        return mostRecentCheckInDate;
    }

    /**
     * Update the most recent activity date. This will only update the most recent activity date <i>if</i> the given activity date is newer than the current 'most' recent
     * activity date.
     * @param activityDate the most recent activity date.
     */
    public void updateMostRecentActivityDate(Date activityDate) {
        if (activityDate.after(mostRecentCheckInDate)) {
            mostRecentCheckInDate = activityDate;
        }
    }
}
