/*   Copyright 2004-2015 Jim Voris
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

import com.qumasoft.qvcslib.notifications.ServerNotificationCheckIn;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckOut;
import com.qumasoft.qvcslib.notifications.ServerNotificationCreateArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationHeaderChange;
import com.qumasoft.qvcslib.notifications.ServerNotificationInterface;
import com.qumasoft.qvcslib.notifications.ServerNotificationLock;
import com.qumasoft.qvcslib.notifications.ServerNotificationMoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRemoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRenameArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationSetRevisionDescription;
import com.qumasoft.qvcslib.notifications.ServerNotificationUnlock;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientViewsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLoginData;
import com.qumasoft.qvcslib.response.ServerManagementInterface;
import com.qumasoft.qvcslib.response.ServerResponseChangePassword;
import com.qumasoft.qvcslib.response.ServerResponseCheckIn;
import com.qumasoft.qvcslib.response.ServerResponseCheckOut;
import com.qumasoft.qvcslib.response.ServerResponseCreateArchive;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetForVisualCompare;
import com.qumasoft.qvcslib.response.ServerResponseGetInfoForMerge;
import com.qumasoft.qvcslib.response.ServerResponseGetLogfileInfo;
import com.qumasoft.qvcslib.response.ServerResponseGetMostRecentActivity;
import com.qumasoft.qvcslib.response.ServerResponseGetRevision;
import com.qumasoft.qvcslib.response.ServerResponseGetRevisionForCompare;
import com.qumasoft.qvcslib.response.ServerResponseHeartBeat;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseLabel;
import com.qumasoft.qvcslib.response.ServerResponseListFilesToPromote;
import com.qumasoft.qvcslib.response.ServerResponseLock;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseMoveFile;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.qvcslib.response.ServerResponsePromoteFile;
import com.qumasoft.qvcslib.response.ServerResponseRegisterClientListener;
import com.qumasoft.qvcslib.response.ServerResponseRenameArchive;
import com.qumasoft.qvcslib.response.ServerResponseResolveConflictFromParentBranch;
import com.qumasoft.qvcslib.response.ServerResponseSuccess;
import com.qumasoft.qvcslib.response.ServerResponseTransactionBegin;
import com.qumasoft.qvcslib.response.ServerResponseTransactionEnd;
import com.qumasoft.qvcslib.response.ServerResponseUnLabel;
import com.qumasoft.qvcslib.response.ServerResponseUnlock;
import com.qumasoft.qvcslib.response.ServerResponseUpdateClient;
import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton transport proxy factory. Use this factory class to get instances of a transport proxy implementation for communication with server.
 * @author Jim Voris
 */
public final class TransportProxyFactory {
    private static int heartbeatThreadCounter = 0;
    // These are the kinds of proxies that we support.

    /** A raw socket proxy type. */
    public static final TransportProxyType RAW_SOCKET_PROXY = new TransportProxyType("RawSocketProxy");
    private static final TransportProxyFactory TRANSPORT_PROXY_FACTORY = new TransportProxyFactory();
    private Map<String, TransportProxyInterface> transportProxyMap = null;
    private List<PasswordChangeListenerInterface> changedPasswordListenersList = null;
    private EventListenerList changeListenerArray = null;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportProxyFactory.class);

    private TransportProxyFactory() {
        transportProxyMap = Collections.synchronizedMap(new TreeMap<String, TransportProxyInterface>());
        changedPasswordListenersList = Collections.synchronizedList(new ArrayList<PasswordChangeListenerInterface>());
        changeListenerArray = new EventListenerList();
    }

    /**
     * Get the factory singleton.
     * @return the factory singleton.
     */
    public static TransportProxyFactory getInstance() {
        return TRANSPORT_PROXY_FACTORY;
    }

    /**
     * Add a change listener.
     * @param l the change listener.
     */
    public synchronized void addChangeListener(ChangeListener l) {
        changeListenerArray.add(ChangeListener.class, l);
    }

    /**
     * Add a changed password listener.
     * @param listener the changed password listener.
     */
    public void addChangedPasswordListener(PasswordChangeListenerInterface listener) {
        changedPasswordListenersList.add(listener);
    }

    /**
     * Remove a changed password listener.
     * @param listener the changed password listener to remove.
     */
    public void removeChangedPasswordListener(PasswordChangeListenerInterface listener) {
        changedPasswordListenersList.remove(listener);
    }

    /**
     * Remove a change listener.
     * @param l the change listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener l) {
        changeListenerArray.remove(ChangeListener.class, l);
    }

    /**
     * Lookup the transport proxy for this server. Return null if it does not exist
     *
     * @param serverProperties the server properties
     * @return the transport proxy to communicate with this server.
     */
    public synchronized TransportProxyInterface getTransportProxy(ServerProperties serverProperties) {
        int port = serverProperties.getClientPort();
        TransportProxyType transportType = serverProperties.getClientTransport();
        String keyValue = makeKey(transportType, serverProperties, port);
        TransportProxyInterface transportProxy = transportProxyMap.get(keyValue);
        return transportProxy;
    }

    /**
     * Lookup the admin transport proxy for this server. Return null if it does not exist
     *
     * @param serverProperties the server properties
     * @return the transport proxy to communicate with this server.
     */
    public synchronized TransportProxyInterface getAdminTransportProxy(ServerProperties serverProperties) {
        int port = serverProperties.getServerAdminPort();
        TransportProxyType transportType = serverProperties.getServerAdminTransport();
        String keyValue = makeKey(transportType, serverProperties, port);
        TransportProxyInterface transportProxy = transportProxyMap.get(keyValue);
        return transportProxy;
    }

    /**
     * Lookup or create a transport proxy for the given parameters. This method will send the login message to the server, but it does not wait for the login response.
     * @param transportType the type of transport.
     * @param serverProperties the server properties.
     * @param port the port number of the server.
     * @param userName the user name.
     * @param hashedPassword the hashed password.
     * @param listener the proxy listener.
     * @param visualCompareInterface the visual compare interface.
     * @return a transport proxy for the given parameters.
     */
    public synchronized TransportProxyInterface getTransportProxy(TransportProxyType transportType, ServerProperties serverProperties, int port, String userName,
                                                                  byte[] hashedPassword,
                                                                  TransportProxyListenerInterface listener,
                                                                  VisualCompareInterface visualCompareInterface) {
        String keyValue = makeKey(transportType, serverProperties, port);
        TransportProxyInterface transportProxy = transportProxyMap.get(keyValue);
        if (transportProxy == null) {
            // There is no transportProxy for this server yet.
            // We'll need to make one.
            if (transportType == RAW_SOCKET_PROXY) {
                transportProxy = new RawSocketTransportProxy(serverProperties, listener, visualCompareInterface);
                if (transportProxy.open(port)) {
                    transportProxyMap.put(keyValue, transportProxy);
                    ReceiveThread receiveThread = new ReceiveThread(transportProxy, keyValue);
                    receiveThread.setName("Receive thread " + keyValue);
                    receiveThread.start();
                    login(userName, hashedPassword, transportProxy, serverProperties);
                } else {
                    transportProxy = null;
                }
            }
        } else {
            LOGGER.info("Re-using transport proxy for server: [" + keyValue + "] transport type: [" + transportProxy.getTransportName() + "]");
        }
        return transportProxy;
    }

    private String makeKey(TransportProxyType transportProxyType, ServerProperties serverProperties, int port) {
        String keyValue = transportProxyType.toString() + ":" + serverProperties.getServerName() + ":" + serverProperties.getServerIPAddress() + ":" + Integer.toString(port);
        return keyValue;
    }

    /**
     * Send a login request to the server.
     * @param userName the user name.
     * @param hashedPassword the hashed password.
     * @param transportProxy the transport proxy.
     * @param serverProperties the server properties.
     */
    private static void login(String userName, byte[] hashedPassword, TransportProxyInterface transportProxy, ServerProperties serverProperties) {
        ClientRequestLoginData loginRequest = new ClientRequestLoginData();
        loginRequest.setUserName(userName);
        loginRequest.setPassword(hashedPassword);
        loginRequest.setServerName(serverProperties.getServerName());
        loginRequest.setVersion(QVCSConstants.QVCS_RELEASE_VERSION);

        synchronized (transportProxy) {
            transportProxy.write(loginRequest);
        }
    }

    /**
     * Close all transports.
     */
    public void closeAllTransports() {
        Set<String> transportKeys = transportProxyMap.keySet();
        transportKeys.stream().map((key) -> transportProxyMap.get(key)).map((transportProxy) -> {
            transportProxy.removeAllListeners();
            return transportProxy;
        }).forEach((transportProxy) -> {
            transportProxy.close();
        });
        transportProxyMap.clear();
    }

    /**
     * Notify listeners for a project control response.
     * @param response a project control response.
     */
    public void notifyListeners(ServerResponseProjectControl response) {
        ChangeEvent event = new ChangeEvent(response);
        Object[] listeners = changeListenerArray.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ChangeListener) listeners[i + 1]).stateChanged(event);
        }
    }

    /**
     * Notify listeners for a list files to promote response.
     * @param response a list files to promote response.
     */
    public void notifyListeners(ServerResponseListFilesToPromote response) {
        ChangeEvent event = new ChangeEvent(response);
        Object[] listeners = changeListenerArray.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ChangeListener) listeners[i + 1]).stateChanged(event);
        }
    }

    /**
     * Notify listeners for file promotion response.
     * @param response a file promotion response.
     */
    public void notifyListeners(ServerResponsePromoteFile response) {
        ChangeEvent event = new ChangeEvent(response);
        Object[] listeners = changeListenerArray.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ChangeListener) listeners[i + 1]).stateChanged(event);
        }
    }

    void notifyPasswordChangeListeners(ServerResponseChangePassword response) {
        changedPasswordListenersList.stream().forEach((listener) -> {
            listener.notifyPasswordChange(response);
        });
    }

    void notifyPasswordChangeListeners(ServerResponseLogin response) {
        changedPasswordListenersList.stream().forEach((listener) -> {
            listener.notifyLoginResult(response);
        });
    }

    void notifyPasswordChangeListeners(ServerResponseUpdateClient response) {
        changedPasswordListenersList.stream().forEach((listener) -> {
            listener.notifyUpdateComplete();
        });
    }

    void notifyRecentActivityListeners(ServerResponseGetMostRecentActivity response) {
        ChangeEvent event = new ChangeEvent(response);
        Object[] listeners = changeListenerArray.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ChangeListener) listeners[i + 1]).stateChanged(event);
        }
    }

    /**
     * Get the list of projects.
     * @param serverProperties the server properties.
     */
    public void requestProjectList(ServerProperties serverProperties) {
        ClientRequestListClientProjectsData clientRequestListClientProjectsData = new ClientRequestListClientProjectsData();
        clientRequestListClientProjectsData.setServerName(serverProperties.getServerName());
        TransportProxyInterface transportProxy = getTransportProxy(serverProperties);
        if (transportProxy != null) {
            synchronized (transportProxy) {
                transportProxy.write(clientRequestListClientProjectsData);
            }
        }
    }

    /**
     * Request the list of views for the given project.
     * @param serverProperties the server properties.
     * @param projectName the project name.
     */
    public void requestViewList(ServerProperties serverProperties, String projectName) {
        ClientRequestListClientViewsData clientRequestListClientViewsData = new ClientRequestListClientViewsData();
        clientRequestListClientViewsData.setServerName(serverProperties.getServerName());
        clientRequestListClientViewsData.setProjectName(projectName);
        TransportProxyInterface transportProxy = getTransportProxy(serverProperties);
        if (transportProxy != null) {
            synchronized (transportProxy) {
                transportProxy.write(clientRequestListClientViewsData);
            }
        }
    }

    class ReceiveThread extends java.lang.Thread {

        private final TransportProxyInterface localProxy;
        private final String keyValue;

        ReceiveThread(TransportProxyInterface transportProxy, String key) {
            localProxy = transportProxy;
            this.keyValue = key;
            setDaemon(true);
        }

        @Override
        public synchronized void run() {
            while (true) {
                if (localProxy == null) {
                    /*
                     * nothing to do
                     */
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        /*
                         * should not happen
                         */
                        continue;
                    }
                }
                handleServerMessages();

                // Something's happened to shut down our connection.
                if (localProxy != null) {
                    transportProxyMap.remove(this.keyValue);
                    localProxy.removeAllListeners();
                    ClientTransactionManager.getInstance().discardServerTransactions(localProxy);
                    break;
                }
            }
            LOGGER.trace("Receive thread exiting for [" + this.keyValue + "]");
        }

        void handleServerMessages() {
            String connectedTo;
            connectedTo = "[" + localProxy.getServerProperties().getServerName() + "] at IP address [" + localProxy.getServerProperties().getServerIPAddress() + "] using ["
                    + localProxy.getTransportName() + "]";
            LOGGER.trace("Waiting for messages from: [" + connectedTo + "]");
            ResponseHandler responseHandler = new ResponseHandler(localProxy);

            try {
                synchronized (localProxy.getReadLock()) {
                    while (true) {
                        try {
                            responseHandler.handleResponse();
                        } catch (QVCSRuntimeException e) {
                            LOGGER.trace("Breaking connection to: [" + connectedTo + "]");
                            break;
                        } catch (RuntimeException e) {
                            LOGGER.info("Breaking connection to: [" + connectedTo + "]");
                            LOGGER.trace(Utility.expandStackTraceToString(e));
                            break;
                        } catch (Exception e) {
                            LOGGER.info("Breaking connection to: [" + connectedTo + "]");
                            LOGGER.trace(Utility.expandStackTraceToString(e));
                            break;
                        } catch (java.lang.OutOfMemoryError e) {
                            LOGGER.error("Out of memory.");
                            LOGGER.warn("Out of memory; breaking connection to: [", connectedTo + "]");
                            LOGGER.warn(e.getLocalizedMessage(), e);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Breaking connection to: [" + connectedTo + "]");
                LOGGER.info(Utility.expandStackTraceToString(e));
            } finally {
                try {
                    localProxy.close();
                } catch (Exception e) {
                    LOGGER.trace("Breaking connection to: [" + connectedTo + "]");
                }
            }
        }
    }

    class ResponseHandler {

        private TransportProxyInterface responseProxy = null;
        // There needs to be a separate keyword manager for each separate
        // receive thread.
        private KeywordManagerInterface keywordManager = null;

        ResponseHandler(TransportProxyInterface transportProxy) {
            responseProxy = transportProxy;
            try {
                keywordManager = KeywordManagerFactory.getInstance().getKeywordManager();
            } catch (Exception e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                keywordManager = null;
            }
        }

        private boolean createWorkfileDirectory(File workfileFile) {
            boolean retVal = false;
            File parentFile = workfileFile.getParentFile();
            if (parentFile != null) {
                if (!parentFile.exists()) {
                    if (parentFile.mkdirs()) {
                        retVal = true;
                    }
                } else {
                    retVal = true;
                }
            }
            return retVal;
        }

        private boolean canOverwriteWorkfile(ServerResponseGetRevision response, WorkFile workfileFile) {
            boolean retVal = false;
            if (workfileFile.exists()) {
                // Check to see if we should overwrite the file...
                if (workfileFile.canWrite()) {
                    // The file is writable.  See if we should overwrite it.
                    if (response.getOverwriteBehavior() == Utility.OverwriteBehavior.REPLACE_WRITABLE_FILE) {
                        retVal = workfileFile.delete();
                    } else if (response.getOverwriteBehavior() == Utility.OverwriteBehavior.ASK_BEFORE_OVERWRITE_OF_WRITABLE_FILE) {
                        // We need to ask the user if we can overwrite the writable
                        // workfile.
                        if (response.getDirectoryLevelOperationFlag()) {
                            retVal = false;
                            LOGGER.info("Skipping get for [" + response.getClientWorkfileName() + "] because file is read/write.");
                        } else {
                            LOGGER.warn("Skipping get for [" + response.getClientWorkfileName() + "] because file is read/write.");
                            retVal = false;
                        }
                    } else {
                        assert (response.getOverwriteBehavior() == Utility.OverwriteBehavior.DO_NOT_REPLACE_WRITABLE_FILE);
                        LOGGER.info("Skipping get for [" + response.getClientWorkfileName() + "] because file is read/write.");
                        retVal = false;
                    }
                } else {
                    // The file is marked read-only...
                    // Remove the existing workfile.
                    if (workfileFile.setReadWrite()) {
                        retVal = workfileFile.delete();
                    }
                }
            } else {
                // If the file doesn't exist, then we should be able to
                // write to the directory to create the file.
                retVal = true;
            }
            return retVal;
        }

        private boolean canOverwriteWorkfile(WorkFile workfileFile) {
            boolean retVal = false;
            if (workfileFile.exists()) {
                // Remove the existing workfile.
                if (workfileFile.setReadWrite()) {
                    retVal = workfileFile.delete();
                }
            } else {
                // If the file doesn't exist, then we should be able to
                // write to the directory to create the file.
                retVal = true;
            }
            return retVal;
        }

        /**
         * Dispatch response messages
         */
        void handleResponse() {
            Object object = responseProxy.read();
            if (object instanceof ServerNotificationInterface) {
                handleServerNotifications(object);
            } else if (object instanceof ServerManagementInterface) {
                ServerManager.getServerManager().handleServerManagement(object);
            } else if (object instanceof ServerResponseInterface) {
                ServerResponseInterface serverResponse = (ServerResponseInterface) object;
                ServerResponseInterface.ResponseOperationType responseType = serverResponse.getOperationType();
                switch (responseType) {
                    case SR_LOGIN:
                        handleLoginResponse(object);
                        break;
                    case SR_REGISTER_CLIENT_LISTENER:
                        handleRegisterClientListenerResponse(object);
                        break;
                    case SR_GET_REVISION:
                        handleGetRevisionResponse(object);
                        break;
                    case SR_GET_REVISION_FOR_COMPARE:
                        handleGetRevisionForCompareResponse(object);
                        break;
                    case SR_CHECK_OUT:
                        handleCheckOutResponse(object);
                        break;
                    case SR_LOCK:
                        handleLockResponse(object);
                        break;
                    case SR_UNLOCK:
                        handleUnlockResponse(object);
                        break;
                    case SR_CHECK_IN:
                        handleCheckInResponse(object);
                        break;
                    case SR_GET_FOR_VISUAL_COMPARE:
                        handleGetForVisualCompareResponse(object);
                        break;
                    case SR_PROJECT_CONTROL:
                        handleProjectControlResponse(object);
                        break;
                    case SR_LABEL:
                        handleLabelResponse(object);
                        break;
                    case SR_REMOVE_LABEL:
                        handleUnLabelResponse(object);
                        break;
                    case SR_GET_LOGFILE_INFO:
                        handleGetLogfileInfoResponse(object);
                        break;
                    case SR_CHANGE_USER_PASSWORD:
                        handleChangePasswordResponse(object);
                        break;
                    case SR_RESPONSE_SUCCESS:
                        handleSuccessResponse(object);
                        break;
                    case SR_RESPONSE_ERROR:
                        handleErrorResponse(object);
                        break;
                    case SR_CREATE_ARCHIVE:
                        handleCreateArchiveResponse(object);
                        break;
                    case SR_MOVE_FILE:
                        handleMoveFileResponse(object);
                        break;
                    case SR_RENAME_FILE:
                        handleRenameArchiveResponse(object);
                        break;
                    case SR_BEGIN_TRANSACTION:
                        handleTransactionBeginResponse(object);
                        break;
                    case SR_END_TRANSACTION:
                        handleTransactionEndResponse(object);
                        break;
                    case SR_RESPONSE_MESSAGE:
                        handleResponseMessage(object);
                        break;
                    case SR_HEARTBEAT:
                        handleHeartBeatResponseMessage(object);
                        break;
                    case SR_UPDATE_CLIENT_JAR:
                        handleUpdateClientResponseMessage(object);
                        break;
                    case SR_GET_INFO_FOR_MERGE:
                        handleGetInfoForMerge(object);
                        break;
                    case SR_RESOLVE_CONFLICT_FROM_PARENT_BRANCH:
                        handleResolveConflictFromParentBranch(object);
                        break;
                    case SR_LIST_FILES_TO_PROMOTE:
                        handleListFilesToPromoteResponse(object);
                        break;
                    case SR_PROMOTE_FILE:
                        handlePromoteFileResponse(object);
                        break;
                    case SR_GET_MOST_RECENT_ACTIVITY:
                        handleGetMostRecentActivity(object);
                        break;
                    default:
                        LOGGER.warn("read unknown or unexpected response object: " + object.getClass().toString());
                        break;
                }
            } else {
                if (object != null) {
                    LOGGER.warn("read unknown or unexpected response object: " + object.getClass().toString());
                } else {
                    LOGGER.trace("failed to read object from server.... server is probably shutting down.");
                    throw new QVCSRuntimeException("Server is shutting down. Breaking connection");
                }
            }
        }

        /**
         * Dispatch notification messages.
         */
        void handleServerNotifications(Object object) {
            ServerNotificationInterface serverNotification = (ServerNotificationInterface) object;
            ServerNotificationInterface.NotificationType notificationType = serverNotification.getNotificationType();
            switch (notificationType) {
                case SR_NOTIFY_CHECKIN:
                    handleCheckInNotification(object);
                    break;
                case SR_NOTIFY_CHECKOUT:
                    handleCheckOutNotification(object);
                    break;
                case SR_NOTIFY_CREATE:
                    handleCreateArchiveNotification(object);
                    break;
                case SR_NOTIFY_HEADER_CHANGE:
                    handleHeaderChangeNotification(object);
                    break;
                case SR_NOTIFY_LOCK:
                    handleLockNotification(object);
                    break;
                case SR_NOTIFY_SET_REV_DESCRIPTION:
                    handleSetRevisionDescriptionNotification(object);
                    break;
                case SR_NOTIFY_UNLOCK:
                    handleUnlockNotification(object);
                    break;
                case SR_NOTIFY_REMOVE:
                    handleRemoveArchiveNotification(object);
                    break;
                case SR_NOTIFY_RENAME:
                    handleRenameArchiveNotification(object);
                    break;
                case SR_NOTIFY_MOVEFILE:
                    handleMoveArchiveNotification(object);
                    break;
                default:
                    LOGGER.warn("read unknown or unexpected notification object: " + object.getClass().toString());
                    break;
            }
        }

        void handleLoginResponse(Object object) {
            ServerResponseLogin response = (ServerResponseLogin) object;
            LOGGER.trace("ServerResponseLogin for user [" + response.getUserName() + "]");
            responseProxy.setIsLoggedInToServer(response.getLoginResult());
            if (responseProxy.getIsLoggedInToServer()) {
                // Start the heartbeat thread.
                HeartbeatThread heartbeatThread = new HeartbeatThread(responseProxy);
                heartbeatThread.setName("Heart beat thread " + heartbeatThreadCounter++);
                responseProxy.setHeartBeatThread(heartbeatThread);
                heartbeatThread.start();

                responseProxy.setUsername(response.getUserName());
                notifyPasswordChangeListeners(response);
                LOGGER.info("User [" + response.getUserName() + "] is logged in to server: [" + response.getServerName() + "]");
            } else {
                notifyPasswordChangeListeners(response);
                LOGGER.info("User [" + response.getUserName() + "] failed to log in to server: [" + response.getServerName() + "]");
                responseProxy.close();
            }
        }

        void handleHeartBeatResponseMessage(Object object) {
            ServerResponseHeartBeat response = (ServerResponseHeartBeat) object;
            LOGGER.trace("ServerResponseHeartBeat for server: " + response.getServerName());
        }

        void handleRegisterClientListenerResponse(Object object) {
            ServerResponseRegisterClientListener response = (ServerResponseRegisterClientListener) object;
            LOGGER.trace("read ServerResponseRegisterClientListener for directory [" + response.getAppendedPath() + "]");
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleGetRevisionResponse(Object object) {
            ServerResponseGetRevision response = (ServerResponseGetRevision) object;
            java.io.FileOutputStream outputStream = null;
            ArchiveDirManagerProxy dirManagerProxy = null;

            try {
                WorkFile workfile = new WorkFile(response.getClientWorkfileName());

                // Figure out our directory manager.
                dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(), response.getAppendedPath());

                if ((dirManagerProxy != null) && createWorkfileDirectory(workfile) && canOverwriteWorkfile(response, workfile)) {
                    // Save this workfile in the keyword contracted cache.
                    KeywordContractedWorkfileCache.getInstance().addContractedBuffer(response.getProjectName(), response.getAppendedPath(), response.getShortWorkfileName(),
                            response.getRevisionString(), response.getBuffer());

                    // See if we have to worry about keyword expansion...
                    if ((keywordManager != null) && response.getSkinnyLogfileInfo().getAttributes().getIsExpandKeywords()) {
                        try {
                            outputStream = new java.io.FileOutputStream(workfile);
                            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outputStream,
                                    workfile,
                                    response.getLogfileInfo(),
                                    response.getLogfileInfo().getRevisionInformation().getRevisionIndex(response.getRevisionString()),
                                    response.getLabelString(),
                                    dirManagerProxy.getAppendedPath(),
                                    dirManagerProxy.getProjectProperties());
                            keywordManager.expandKeywords(response.getBuffer(), keywordExpansionContext);
                        } catch (QVCSException e) {
                            LOGGER.warn(e.getLocalizedMessage(), e);
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    } else {
                        try {
                            outputStream = new java.io.FileOutputStream(workfile);
                            Utility.writeDataToStream(response.getBuffer(), outputStream);
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    }

                    // Set the timestamp on the workfile.
                    if (response.getTimestampBehavior() != Utility.TimestampBehavior.SET_TIMESTAMP_TO_NOW) {
                        workfile.setLastModified(response.getTimestamp());
                    }

                    // Mark the workfile read-only if we are supposed to
                    if (response.getSkinnyLogfileInfo().getAttributes().getIsProtectWorkfile()) {
                        workfile.setReadOnly();
                    }
                    response.updateDirManagerProxy(dirManagerProxy);
                } else {
                    // We need to put this outside of the preceding conditional so that we'll send a notifyAll
                    // to the associated proxy object, even if we didn't do anything, since that thread is blocked
                    // waiting for the notify.
                    if (dirManagerProxy != null) {
                        dirManagerProxy.notifyListeners();
                    }
                }

            } catch (java.io.IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                if (dirManagerProxy != null) {
                    dirManagerProxy.notifyListeners();
                }
            }
        }

        void handleCreateArchiveResponse(Object object) {
            ServerResponseCreateArchive response = (ServerResponseCreateArchive) object;
            LOGGER.trace("read ServerResponseCreateArchive for directory " + response.getAppendedPath());
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleCheckOutResponse(Object object) {
            ServerResponseCheckOut response = (ServerResponseCheckOut) object;
            java.io.FileOutputStream outputStream = null;
            ArchiveDirManagerProxy dirManagerProxy = null;

            try {
                WorkFile workfile = new WorkFile(response.getClientWorkfileName());

                // Figure out our proxy directory manager.
                dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(), response.getAppendedPath());

                if ((dirManagerProxy != null) && createWorkfileDirectory(workfile) && canOverwriteWorkfile(workfile)) {
                    // Save this workfile in the keyword contracted cache.
                    KeywordContractedWorkfileCache.getInstance().addContractedBuffer(response.getProjectName(), response.getAppendedPath(), response.getShortWorkfileName(),
                            response.getRevisionString(), response.getBuffer());

                    // See if we have to worry about keyword expansion...
                    if ((keywordManager != null) && response.getSkinnyLogfileInfo().getAttributes().getIsExpandKeywords()) {
                        try {
                            outputStream = new java.io.FileOutputStream(workfile);
                            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outputStream,
                                    workfile,
                                    response.getLogfileInfo(),
                                    response.getLogfileInfo().getRevisionInformation().getRevisionIndex(response.getRevisionString()),
                                    response.getLabelString(),
                                    dirManagerProxy.getAppendedPath(),
                                    dirManagerProxy.getProjectProperties());
                            keywordManager.expandKeywords(response.getBuffer(), keywordExpansionContext);
                        } catch (QVCSException e) {
                            LOGGER.warn(e.getLocalizedMessage(), e);
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    } else {
                        try {
                            outputStream = new java.io.FileOutputStream(workfile);
                            Utility.writeDataToStream(response.getBuffer(), outputStream);
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    }
                    response.updateDirManagerProxy(dirManagerProxy);
                } else {
                    // We need to put this outside of the preceding conditional so that we'll send a notifyAll
                    // to the associated proxy object, even if we didn't do anything, since that thread is blocked
                    // waiting for the notify.
                    if (dirManagerProxy != null) {
                        dirManagerProxy.notifyListeners();
                    }
                }
            } catch (java.io.IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                if (dirManagerProxy != null) {
                    dirManagerProxy.notifyListeners();
                }
            }
        }

        void handleLabelResponse(Object object) {
            ServerResponseLabel response = (ServerResponseLabel) object;

            // Figure out our proxy directory manager.
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleUnLabelResponse(Object object) {
            ServerResponseUnLabel response = (ServerResponseUnLabel) object;

            // Figure out our proxy directory manager.
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleGetLogfileInfoResponse(Object object) {
            ServerResponseGetLogfileInfo response = (ServerResponseGetLogfileInfo) object;

            // Figure out our proxy directory manager.
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleGetRevisionForCompareResponse(Object object) {
            ServerResponseGetRevisionForCompare response = (ServerResponseGetRevisionForCompare) object;

            // Figure out our proxy directory manager.
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleLockResponse(Object object) {
            ServerResponseLock response = (ServerResponseLock) object;

            // Figure out our proxy directory manager.
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleUnlockResponse(Object object) {
            ServerResponseUnlock response = (ServerResponseUnlock) object;

            try {
                WorkFile workfile = new WorkFile(response.getClientWorkfileName());

                // Figure out our directory manager.
                ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                        response.getAppendedPath());

                if ((dirManagerProxy != null) && createWorkfileDirectory(workfile)) {
                    if (response.getUndoCheckoutBehavior() == Utility.UndoCheckoutBehavior.DELETE_WORKFILE) {
                        workfile.setReadWrite();
                        workfile.delete();
                        LOGGER.info("Undo checkout complete; deleted workfile: [" + workfile.getCanonicalPath() + "]");
                    } else {
                        // Mark the workfile read-only if we are supposed to
                        if (response.getSkinnyLogfileInfo().getAttributes().getIsProtectWorkfile()) {
                            workfile.setReadOnly();
                        }
                    }
                    response.updateDirManagerProxy(dirManagerProxy);
                } else {
                    // We need to put this outside of the preceding conditional so that we'll send a notifyAll
                    // to the associated proxy object, even if we didn't do anything, since that thread is blocked
                    // waiting for the notify.
                    if (dirManagerProxy != null) {
                        dirManagerProxy.notifyListeners();
                    }
                }

            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }

        void handleCheckInResponse(Object object) {
            ServerResponseCheckIn response = (ServerResponseCheckIn) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());

            // Update the LogfileInfo info for the ArchiveDirectoryManagerProxy
            directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
            LogFileProxy logFileProxy = (LogFileProxy) directoryManagerProxy.getArchiveInfo(response.getShortWorkfileName());
            WorkFile workfile = new WorkFile(response.getClientWorkfileName());
            byte[] buffer = KeywordContractedWorkfileCache.getInstance().getContractedBuffer(response.getIndex(), response.getNewRevisionString());
            if ((buffer != null)
                    && logFileProxy.getAttributes().getIsExpandKeywords()
                    && (response.getAddedRevisionData() != null)
                    && !response.getNoExpandKeywordsFlag()) {
                // See if we have to worry about keyword expansion...
                if (createWorkfileDirectory(workfile) && canOverwriteWorkfile(workfile)) {
                    try (FileOutputStream outputStream = new FileOutputStream(workfile)) {
                        KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outputStream,
                                workfile,
                                response.getLogfileInfo(),
                                response.getAddedRevisionData().getNewRevisionIndex(),
                                null,
                                directoryManagerProxy.getAppendedPath(),
                                directoryManagerProxy.getProjectProperties());
                        keywordManager.expandKeywords(buffer, keywordExpansionContext);
                    } catch (QVCSException | IOException e) {
                        LOGGER.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
            if (logFileProxy.getAttributes().getIsDeleteWork()) {
                // The attributes say to delete the workfile.... do
                // that only if they are not keeping the file locked.
                if (!response.getKeepLockedFlag()) {
                    workfile.delete();
                }
            } else {
                // Make the workfile read-only if we are supposed to.
                if (!response.getKeepLockedFlag() && logFileProxy.getAttributes().getIsProtectWorkfile()) {
                    workfile.setReadOnly();
                }

                // Protect the workfile if the user asked us to.
                if (response.getProtectWorkfileFlag()) {
                    workfile.setReadOnly();
                }
            }

            response.updateDirManagerProxy(directoryManagerProxy);
        }

        void handleGetForVisualCompareResponse(Object object) {
            ServerResponseGetForVisualCompare response = (ServerResponseGetForVisualCompare) object;
            response.setVisualCompareInterface(responseProxy.getVisualCompareInterface());
            java.io.FileOutputStream outputStream = null;
            ArchiveDirManagerProxy directoryManagerProxy = null;

            try {
                WorkFile workfile = new WorkFile(response.getClientOutputFileName());
                java.io.File workFile = new java.io.File(response.getFullWorkfileName());

                // Figure out our proxy directory manager.
                directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(), response.getAppendedPath());

                if ((directoryManagerProxy != null) && createWorkfileDirectory(workfile) && canOverwriteWorkfile(workfile)) {
                    // Save this workfile in the keyword contracted cache.
                    String shortWorkfileName = response.getFullWorkfileName().substring(1 + response.getFullWorkfileName().lastIndexOf(File.separatorChar));
                    KeywordContractedWorkfileCache.getInstance().addContractedBuffer(response.getProjectName(), response.getAppendedPath(), shortWorkfileName,
                            response.getRevisionString(),
                            response.getBuffer());

                    // See if we have to worry about keyword expansion...
                    if ((keywordManager != null) && response.getLogfileInfo().getLogFileHeaderInfo().getLogFileHeader().attributes().getIsExpandKeywords()) {
                        try {
                            outputStream = new java.io.FileOutputStream(workfile);
                            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outputStream,
                                    workFile,
                                    response.getLogfileInfo(),
                                    response.getLogfileInfo().getRevisionInformation().getRevisionIndex(response.getRevisionString()),
                                    null,
                                    directoryManagerProxy.getAppendedPath(),
                                    directoryManagerProxy.getProjectProperties());
                            keywordManager.expandKeywords(response.getBuffer(), keywordExpansionContext);
                        } catch (QVCSException e) {
                            LOGGER.warn(e.getLocalizedMessage(), e);
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    } else {
                        try {
                            outputStream = new java.io.FileOutputStream(workfile);
                            Utility.writeDataToStream(response.getBuffer(), outputStream);
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    }
                    response.updateDirManagerProxy(directoryManagerProxy);
                } else {
                    // We need to put this outside of the preceding conditional so that we'll send a notifyAll
                    // to the associated proxy object, even if we didn't do anything, since that thread is blocked
                    // waiting for the notify.
                    if (directoryManagerProxy != null) {
                        directoryManagerProxy.notifyListeners();
                    }
                }
            } catch (java.io.IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                if (directoryManagerProxy != null) {
                    directoryManagerProxy.notifyListeners();
                }
            }
        }

        private void handleMoveFileResponse(Object object) {
            ServerResponseMoveFile response = (ServerResponseMoveFile) object;

            // With a move response, there is no guarantee that the required directory managers already exist...
            // So we have to make sure that they do so that we can move the workfile (at least).
            try {
                UserLocationProperties userLocationProperties = new UserLocationProperties(System.getProperty("user.dir"), responseProxy.getUsername());
                String workfileBaseDirectory = userLocationProperties.getWorkfileLocation(responseProxy.getServerProperties().getServerName(), response.getProjectName(),
                        response.getViewName());
                String originWorkfileDirectory;
                String destinationWorkfileDirectory;
                if (response.getOriginAppendedPath().length() > 0) {
                    originWorkfileDirectory = workfileBaseDirectory + File.separator + response.getOriginAppendedPath();
                } else {
                    originWorkfileDirectory = workfileBaseDirectory;
                }

                if (response.getDestinationAppendedPath().length() > 0) {
                    destinationWorkfileDirectory = workfileBaseDirectory + File.separator + response.getDestinationAppendedPath();
                } else {
                    destinationWorkfileDirectory = workfileBaseDirectory;
                }

                RemoteProjectProperties remoteProjectProperties = new RemoteProjectProperties(response.getProjectName(), response.getProjectProperties());
                DirectoryCoordinate originDirectoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getViewName(), response.getOriginAppendedPath());
                DirectoryManagerInterface originDirectoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(responseProxy.getServerProperties().getServerName(),
                        originDirectoryCoordinate, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, remoteProjectProperties,
                        originWorkfileDirectory, null, false);
                DirectoryCoordinate destinationDirectoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getViewName(),
                        response.getDestinationAppendedPath());
                DirectoryManagerInterface destinationDirectoryManager =
                        DirectoryManagerFactory.getInstance().getDirectoryManager(responseProxy.getServerProperties().getServerName(),
                        destinationDirectoryCoordinate, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, remoteProjectProperties,
                        destinationWorkfileDirectory, null, false);

                ArchiveDirManagerProxy originDirectoryManagerProxy = (ArchiveDirManagerProxy) originDirectoryManager.getArchiveDirManager();
                ArchiveDirManagerProxy destinationDirectoryManagerProxy = (ArchiveDirManagerProxy) destinationDirectoryManager.getArchiveDirManager();

                if ((originDirectoryManagerProxy != null) && (destinationDirectoryManagerProxy != null)) {
                    // Remove the archive info from the origin directory...
                    // (This should already have been done by the notify).
                    originDirectoryManagerProxy.removeArchiveInfo(response.getShortWorkfileName());

                    // Add the archive info to the destination directory...
                    // (This should already have been done by the notify).
                    destinationDirectoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());

                    // Move the workfile from the origin director to the destination directory.
                    // (This was NOT done by the notify).
                    WorkFile.moveFile(originWorkfileDirectory, destinationWorkfileDirectory, response.getShortWorkfileName());

                    LOGGER.info("Move file response received for: [" + response.getShortWorkfileName() + "]");

                    // So we have a fresh notion of the workfiles that we have...
                    originDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();
                    destinationDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();

                    originDirectoryManagerProxy.notifyListeners();
                    destinationDirectoryManagerProxy.notifyListeners();
                }
            } catch (QVCSException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }

        void handleRenameArchiveResponse(Object object) {
            ServerResponseRenameArchive response = (ServerResponseRenameArchive) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                directoryManagerProxy.removeArchiveInfo(response.getOldShortWorkfileName());
                directoryManagerProxy.updateArchiveInfo(response.getNewShortWorkfileName(), response.getSkinnyLogfileInfo());

                String workfileDirectory = directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().getWorkfileDirectory();
                WorkFile.renameFile(workfileDirectory, response.getOldShortWorkfileName(), response.getNewShortWorkfileName());

                LOGGER.info("Rename response received for: [" + response.getOldShortWorkfileName() + "]");

                // So we have a fresh notion of the workfiles that we have...
                directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();

                directoryManagerProxy.notifyListeners();
            }
        }

        void handleGetInfoForMerge(Object object) {
            ServerResponseGetInfoForMerge response = (ServerResponseGetInfoForMerge) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                response.updateDirManagerProxy(directoryManagerProxy);
            }
        }

        void handleResolveConflictFromParentBranch(Object object) {
            ServerResponseResolveConflictFromParentBranch response = (ServerResponseResolveConflictFromParentBranch) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                response.updateDirManagerProxy(directoryManagerProxy);
            }
        }

        void handlePromoteFileResponse(Object object) {
            ServerResponsePromoteFile response = (ServerResponsePromoteFile) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            notifyListeners(response);
        }

        void handleListFilesToPromoteResponse(Object object) {
            ServerResponseListFilesToPromote response = (ServerResponseListFilesToPromote) object;
            notifyListeners(response);
        }

        void handleProjectControlResponse(Object object) {
            ServerResponseProjectControl response = (ServerResponseProjectControl) object;
            notifyListeners(response);
        }

        void handleChangePasswordResponse(Object object) {
            ServerResponseChangePassword response = (ServerResponseChangePassword) object;
            notifyPasswordChangeListeners(response);
        }

        private void handleGetMostRecentActivity(Object object) {
            ServerResponseGetMostRecentActivity response = (ServerResponseGetMostRecentActivity) object;
            notifyRecentActivityListeners(response);
        }

        void handleSuccessResponse(Object object) {
            ServerResponseSuccess response = (ServerResponseSuccess) object;
            responseProxy.getProxyListener().notifyTransportProxyListener(response);
        }

        void handleErrorResponse(Object object) {
            ServerResponseError response = (ServerResponseError) object;
            if (response.getProjectName() != null) {
                ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                        response.getAppendedPath());
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            LOGGER.warn("Error message from server [Project][View][AppendedPath]: [" + response.getProjectName() + "][" + response.getViewName() + "]["
                    + response.getAppendedPath() + "]: "
                    + response.getErrorMessage());
        }

        void handleResponseMessage(Object object) {
            ServerResponseMessage response = (ServerResponseMessage) object;
            ArchiveDirManagerProxy directoryManagerProxy = null;
            if (response.getProjectName() != null) {
                directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(), response.getAppendedPath());
            }

            if (directoryManagerProxy != null) {
                // Wake up any waiting threads.
                LogFileProxy logFileProxy = (LogFileProxy) directoryManagerProxy.getArchiveInfo(response.getShortWorkfileName());
                if (logFileProxy != null) {
                    synchronized (logFileProxy) {
                        logFileProxy.notifyAll();
                    }
                }

                // Wake up any other threads that may be synchronized on the directory (like the ANT tasks)
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            responseProxy.getProxyListener().notifyTransportProxyListener(response);
        }

        private void handleUpdateClientResponseMessage(Object object) {
            ServerResponseUpdateClient response = (ServerResponseUpdateClient) object;
            java.io.FileOutputStream outputStream = null;
            try {
                try {
                    String clientFileName = response.getRequestedFileName() + ".new";
                    String homeDirectory = System.getProperty("user.dir");
                    clientFileName = homeDirectory + File.separator + clientFileName;
                    outputStream = new java.io.FileOutputStream(clientFileName);
                    outputStream.write(response.getBuffer());
                    LOGGER.info("Update received for: " + response.getRequestedFileName());
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }

                if (response.getRestartFlag()) {
                    // Show the message on the Swing thread.
                    Runnable later = () -> {
                        // Time to exit the application.
                        JOptionPane.showMessageDialog(null, "Updates received.  Please restart the application.", "Updates Complete", JOptionPane.PLAIN_MESSAGE);
                        Frame[] frames = Frame.getFrames();
                        for (Frame frame : frames) {
                            if (frame instanceof ExitAppInterface) {
                                ExitAppInterface exitApp = (ExitAppInterface) frame;
                                exitApp.exitTheApp();
                            }
                        }
                    };
                    SwingUtilities.invokeLater(later);
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }

        void handleTransactionBeginResponse(Object object) {
            ServerResponseTransactionBegin response = (ServerResponseTransactionBegin) object;
            ClientTransactionManager.getInstance().beginTransaction(responseProxy.getServerProperties().getServerName(), response.getTransactionID());
        }

        void handleTransactionEndResponse(Object object) {
            ServerResponseTransactionEnd response = (ServerResponseTransactionEnd) object;
            ClientTransactionManager.getInstance().endTransaction(responseProxy.getServerProperties().getServerName(), response.getTransactionID());
        }

        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        // Handle notification messages.
        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        void handleCheckInNotification(Object object) {
            ServerNotificationCheckIn response = (ServerNotificationCheckIn) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                // Update the skinny logfileInfo info for the ArchiveDirectoryManagerProxy
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("CheckIn notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleCheckOutNotification(Object object) {
            ServerNotificationCheckOut response = (ServerNotificationCheckOut) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());

            if (directoryManagerProxy != null) {
                // Update the skinny logfileInfo info for the ArchiveDirectoryManagerProxy
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("CheckOut notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleHeaderChangeNotification(Object object) {
            ServerNotificationHeaderChange response = (ServerNotificationHeaderChange) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                // Update the skinny logfileInfo info for the ArchiveDirectoryManagerProxy
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("Header change notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleLockNotification(Object object) {
            ServerNotificationLock response = (ServerNotificationLock) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());

            if (directoryManagerProxy != null) {
                // Update the skinny logfileInfo info for the ArchiveDirectoryManagerProxy
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("Lock notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleUnlockNotification(Object object) {
            ServerNotificationUnlock response = (ServerNotificationUnlock) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());

            if (directoryManagerProxy != null) {
                // Update the skinny logfileInfo info for the ArchiveDirectoryManagerProxy
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("UnLock notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleCreateArchiveNotification(Object object) {
            ServerNotificationCreateArchive response = (ServerNotificationCreateArchive) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            if (directoryManagerProxy == null) {
                try {
                    DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getViewName(), response.getAppendedPath());
                    DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(response.getServerName(), directoryCoordinate,
                            QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, null, null, null, false);
                    directoryManagerProxy = (ArchiveDirManagerProxy) directoryManager.getArchiveDirManager();
                } catch (QVCSException e) {
                    LOGGER.warn("Not able to create directory manager!!" + e.getLocalizedMessage());
                    directoryManagerProxy = null;
                }
            }
            if (directoryManagerProxy != null) {
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("Creation notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleRemoveArchiveNotification(Object object) {
            ServerNotificationRemoveArchive response = (ServerNotificationRemoveArchive) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                directoryManagerProxy.removeArchiveInfo(response.getShortWorkfileName());
                LOGGER.info("Remove notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleRenameArchiveNotification(Object object) {
            ServerNotificationRenameArchive response = (ServerNotificationRenameArchive) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                directoryManagerProxy.removeArchiveInfo(response.getOldShortWorkfileName());
                directoryManagerProxy.updateArchiveInfo(response.getNewShortWorkfileName(), response.getSkinnyLogfileInfo());

                LOGGER.info("Rename notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getOldShortWorkfileName() + "]");

                // So we have a fresh notion of the workfiles that we have...
                directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();

                directoryManagerProxy.notifyListeners();
            }
        }

        void handleMoveArchiveNotification(Object object) {
            ServerNotificationMoveArchive response = (ServerNotificationMoveArchive) object;

            // With a move notification, there is no guarantee that the required directory managers already exist...
            // So we have to make sure that they do so that we can move the workfile (at least).
            try {
                UserLocationProperties userLocationProperties = new UserLocationProperties(System.getProperty("user.dir"), responseProxy.getUsername());
                String workfileBaseDirectory = userLocationProperties.getWorkfileLocation(responseProxy.getServerProperties().getServerName(), response.getProjectName(),
                        response.getViewName());
                String originWorkfileDirectory;
                String destinationWorkfileDirectory;
                if (response.getOriginAppendedPath().length() > 0) {
                    originWorkfileDirectory = workfileBaseDirectory + File.separator + response.getOriginAppendedPath();
                } else {
                    originWorkfileDirectory = workfileBaseDirectory;
                }

                if (response.getDestinationAppendedPath().length() > 0) {
                    destinationWorkfileDirectory = workfileBaseDirectory + File.separator + response.getDestinationAppendedPath();
                } else {
                    destinationWorkfileDirectory = workfileBaseDirectory;
                }

                RemoteProjectProperties remoteProjectProperties = new RemoteProjectProperties(response.getProjectName(), response.getProjectProperties());
                DirectoryCoordinate originDirectoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getViewName(), response.getOriginAppendedPath());
                DirectoryManagerInterface originDirectoryManager =
                        DirectoryManagerFactory.getInstance().getDirectoryManager(responseProxy.getServerProperties().getServerName(),
                        originDirectoryCoordinate, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, remoteProjectProperties,
                        originWorkfileDirectory, null, false);
                DirectoryCoordinate destinationDirectoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getViewName(),
                        response.getDestinationAppendedPath());
                DirectoryManagerInterface destinationDirectoryManager =
                        DirectoryManagerFactory.getInstance().getDirectoryManager(responseProxy.getServerProperties().getServerName(),
                        destinationDirectoryCoordinate, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, remoteProjectProperties,
                        destinationWorkfileDirectory, null, false);

                ArchiveDirManagerProxy originDirectoryManagerProxy = (ArchiveDirManagerProxy) originDirectoryManager.getArchiveDirManager();
                ArchiveDirManagerProxy destinationDirectoryManagerProxy = (ArchiveDirManagerProxy) destinationDirectoryManager.getArchiveDirManager();

                if ((originDirectoryManagerProxy != null) && (destinationDirectoryManagerProxy != null)) {
                    // Remove the archive info from the origin directory...
                    originDirectoryManagerProxy.removeArchiveInfo(response.getShortWorkfileName());

                    // Add the archive info to the destination directory...
                    destinationDirectoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());

                    LOGGER.info("Move notification received for: ["
                            + response.getProjectName() + "::"
                            + response.getViewName() + "::["
                            + response.getOriginAppendedPath() + "/"
                            + response.getShortWorkfileName() + "]");

                    // So we have a fresh notion of the workfiles that we have...
                    originDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();
                    destinationDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();

                    originDirectoryManagerProxy.notifyListeners();
                    destinationDirectoryManagerProxy.notifyListeners();
                }
            } catch (QVCSException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }

        void handleSetRevisionDescriptionNotification(Object object) {
            ServerNotificationSetRevisionDescription response = (ServerNotificationSetRevisionDescription) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getViewName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                // Update the skinny logfileInfo info for the ArchiveDirectoryManagerProxy
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("SetRevisionDescription notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getViewName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }
    }

}
