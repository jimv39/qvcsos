/*   Copyright 2004-2023 Jim Voris
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
import com.qumasoft.qvcslib.notifications.ServerNotificationCreateArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationInterface;
import com.qumasoft.qvcslib.notifications.ServerNotificationMoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRemoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRenameArchive;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientBranchesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLoginData;
import com.qumasoft.qvcslib.response.AbstractServerManagementResponse;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.AbstractServerResponsePromoteFile;
import com.qumasoft.qvcslib.response.ServerResponseAddDirectory;
import com.qumasoft.qvcslib.response.ServerResponseAddUserProperty;
import com.qumasoft.qvcslib.response.ServerResponseApplyTag;
import com.qumasoft.qvcslib.response.ServerResponseChangePassword;
import com.qumasoft.qvcslib.response.ServerResponseCheckIn;
import com.qumasoft.qvcslib.response.ServerResponseCreateArchive;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetAllLogfileInfo;
import com.qumasoft.qvcslib.response.ServerResponseGetBriefCommitInfoList;
import com.qumasoft.qvcslib.response.ServerResponseGetCommitListForMoveableTagReadOnlyBranches;
import com.qumasoft.qvcslib.response.ServerResponseGetDirectory;
import com.qumasoft.qvcslib.response.ServerResponseGetForVisualCompare;
import com.qumasoft.qvcslib.response.ServerResponseGetInfoForMerge;
import com.qumasoft.qvcslib.response.ServerResponseGetLogfileInfo;
import com.qumasoft.qvcslib.response.ServerResponseGetMostRecentActivity;
import com.qumasoft.qvcslib.response.ServerResponseGetRevision;
import com.qumasoft.qvcslib.response.ServerResponseGetRevisionForCompare;
import com.qumasoft.qvcslib.response.ServerResponseGetTags;
import com.qumasoft.qvcslib.response.ServerResponseGetTagsInfo;
import com.qumasoft.qvcslib.response.ServerResponseGetUserCommitComments;
import com.qumasoft.qvcslib.response.ServerResponseGetUserProperties;
import com.qumasoft.qvcslib.response.ServerResponseHeartBeat;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListFilesToPromote;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseMoveFile;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.qvcslib.response.ServerResponsePromotionCreate;
import com.qumasoft.qvcslib.response.ServerResponsePromotionDelete;
import com.qumasoft.qvcslib.response.ServerResponsePromotionMove;
import com.qumasoft.qvcslib.response.ServerResponsePromotionMoveAndRename;
import com.qumasoft.qvcslib.response.ServerResponsePromotionRename;
import com.qumasoft.qvcslib.response.ServerResponsePromotionSimple;
import com.qumasoft.qvcslib.response.ServerResponseRegisterClientListener;
import com.qumasoft.qvcslib.response.ServerResponseRenameArchive;
import com.qumasoft.qvcslib.response.ServerResponseResolveConflictFromParentBranch;
import com.qumasoft.qvcslib.response.ServerResponseSuccess;
import com.qumasoft.qvcslib.response.ServerResponseTransactionBegin;
import com.qumasoft.qvcslib.response.ServerResponseTransactionEnd;
import com.qumasoft.qvcslib.response.ServerResponseUpdateFilterFileCollection;
import com.qumasoft.qvcslib.response.ServerResponseUpdateViewUtilityCommandLine;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
    private List<EndTransactionListenerInterface> endTransactionListenerList = null;
    private List<ViewUtilityResponseListenerInterface> viewUtilityResponseListenerInterfaceListenerList = null;
    private List<FileFilterResponseListenerInterface> fileFilterListenerInterfaceListenerList = null;
    private EventListenerList changeListenerArray = null;
    private String directory = null;

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportProxyFactory.class);

    private TransportProxyFactory() {
        transportProxyMap = Collections.synchronizedMap(new TreeMap<>());
        changedPasswordListenersList = Collections.synchronizedList(new ArrayList<>());
        endTransactionListenerList = Collections.synchronizedList(new ArrayList<>());
        viewUtilityResponseListenerInterfaceListenerList = Collections.synchronizedList(new ArrayList<>());
        fileFilterListenerInterfaceListenerList = Collections.synchronizedList(new ArrayList<>());
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
     * Set the directory that is the base for finding property and data files for the application.
     * @param dir the base directory.
     */
    public synchronized void setDirectory(String dir) {
        this.directory = dir;
    }

    /**
     * Get the directory that is the base for finding property and data files for the application.
     * @return the directory that is the base for finding property and data files for the application.
     */
    public synchronized String getDirectory() {
        return this.directory;
    }

    /**
     * Add a change listener.
     * @param l the change listener.
     */
    public synchronized void addChangeListener(ChangeListener l) {
        changeListenerArray.add(ChangeListener.class, l);
    }

    /**
     * Remove a change listener.
     *
     * @param l the change listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener l) {
        changeListenerArray.remove(ChangeListener.class, l);
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
     * Add an end transaction listener.
     *
     * @param listener the end transaction listener.
     */
    public void addEndTransactionListener(EndTransactionListenerInterface listener) {
        endTransactionListenerList.add(listener);
    }

    /**
     * Add a view utility response listener (the ViewUtilityManager).
     * @param listener the ViewUtilityManager.
     */
    public void addViewUtilityResponseListener(ViewUtilityResponseListenerInterface listener) {
        viewUtilityResponseListenerInterfaceListenerList.add(listener);
    }

    /**
     * Add a file filter response listener (the FilterManager).
     * @param listener the ViewUtilityManager.
     */
    public void addFileFilterResponseListener(FileFilterResponseListenerInterface listener) {
        fileFilterListenerInterfaceListenerList.add(listener);
    }

    /**
     * Remove an end transaction listener.
     *
     * @param listener the end transaction listener to remove.
     */
    public void removeEndTransactionListener(EndTransactionListenerInterface listener) {
        endTransactionListenerList.remove(listener);
    }

    /**
     * Lookup the transport proxy for this server. Return null if it does not exist
     *
     * @param serverProperties the server properties
     * @return the transport proxy to communicate with this server.
     */
    public TransportProxyInterface getTransportProxy(ServerProperties serverProperties) {
        int port = serverProperties.getClientPort();
        TransportProxyType transportType = serverProperties.getClientTransport();
        String keyValue = makeKey(transportType, serverProperties, port);
        return transportProxyMap.get(keyValue);
    }

    /**
     * Lookup the admin transport proxy for this server. Return null if it does not exist
     *
     * @param serverProperties the server properties
     * @return the transport proxy to communicate with this server.
     */
    public TransportProxyInterface getAdminTransportProxy(ServerProperties serverProperties) {
        int port = serverProperties.getServerAdminPort();
        TransportProxyType transportType = serverProperties.getServerAdminTransport();
        String keyValue = makeKey(transportType, serverProperties, port);
        return transportProxyMap.get(keyValue);
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
    public TransportProxyInterface getTransportProxy(TransportProxyType transportType, ServerProperties serverProperties, int port, String userName,
                                                                  byte[] hashedPassword,
                                                                  TransportProxyListenerInterface listener,
                                                                  VisualCompareInterface visualCompareInterface) {
        String keyValue = makeKey(transportType, serverProperties, port);
        TransportProxyInterface transportProxy = transportProxyMap.get(keyValue);
        if (transportProxy == null) {
            // There is no transportProxy for this server yet.
            // We'll need to make one.
            if (transportType == RAW_SOCKET_PROXY) {
                transportProxy = new RawSocketTransportProxy(keyValue, serverProperties, listener, visualCompareInterface);
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
    private void login(String userName, byte[] hashedPassword, TransportProxyInterface transportProxy, ServerProperties serverProperties) {
        ClientRequestLoginData loginRequest = new ClientRequestLoginData();
        loginRequest.setUserName(userName);
        loginRequest.setPassword(hashedPassword);
        loginRequest.setServerName(serverProperties.getServerName());
        loginRequest.setVersion(QVCSConstants.QVCS_RELEASE_VERSION);
        loginRequest.setClientComputerName(Utility.getComputerName());
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, loginRequest);
    }

    /**
     * Close all transports.
     */
    public synchronized void closeAllTransports() {
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
    public void notifyListeners(AbstractServerResponsePromoteFile response) {
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

    void notifyRecentActivityListeners(ServerResponseGetMostRecentActivity response) {
        ChangeEvent event = new ChangeEvent(response);
        Object[] listeners = changeListenerArray.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ChangeListener) listeners[i + 1]).stateChanged(event);
        }
    }

    void notifyTransactionEndListeners(ServerResponseTransactionEnd response) {
        endTransactionListenerList.stream().forEach((listener) -> {
            listener.notifyEndTransaction(response);
        });
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
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestListClientProjectsData);
        }
    }

    /**
     * Request the list of branches for the given project.
     * @param serverProperties the server properties.
     * @param projectName the project name.
     */
    public void requestBranchList(ServerProperties serverProperties, String projectName) {
        ClientRequestListClientBranchesData clientRequestListClientBranchesData = new ClientRequestListClientBranchesData();
        clientRequestListClientBranchesData.setServerName(serverProperties.getServerName());
        clientRequestListClientBranchesData.setProjectName(projectName);
        TransportProxyInterface transportProxy = getTransportProxy(serverProperties);
        if (transportProxy != null) {
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestListClientBranchesData);
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
                        // Restore interrupted state...
                        Thread.currentThread().interrupt();
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
                boolean loopTest = true;
                synchronized (localProxy.getReadLock()) {
                    while (loopTest) {
                        try {
                            responseHandler.handleResponse();
                        } catch (QVCSRuntimeException e) {
                            LOGGER.warn("Breaking connection to: [" + connectedTo + "]");
                            loopTest = false;
                        } catch (RuntimeException e) {
                            LOGGER.info("Breaking connection to: [" + connectedTo + "]");
                            LOGGER.info(Utility.expandStackTraceToString(e));
                            loopTest = false;
                        } catch (Exception e) {
                            LOGGER.info("Breaking connection to: [" + connectedTo + "]");
                            LOGGER.info(Utility.expandStackTraceToString(e));
                            loopTest = false;
                        } catch (java.lang.OutOfMemoryError e) {
                            LOGGER.error("Out of memory.");
                            LOGGER.warn("Out of memory; breaking connection to: [", connectedTo + "]");
                            LOGGER.warn(e.getLocalizedMessage(), e);
                            loopTest = false;
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

        ResponseHandler(TransportProxyInterface transportProxy) {
            responseProxy = transportProxy;
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
            String logFormatString = "Skipping get for [{}] because file is read/write.";
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
                            LOGGER.info(logFormatString, response.getClientWorkfileName());
                        } else {
                            LOGGER.warn(logFormatString, response.getClientWorkfileName());
                            retVal = false;
                        }
                    } else {
                        assert (response.getOverwriteBehavior() == Utility.OverwriteBehavior.DO_NOT_REPLACE_WRITABLE_FILE);
                        LOGGER.info(logFormatString, response.getClientWorkfileName());
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
            } else if (object instanceof AbstractServerManagementResponse) {
                ServerManager.getServerManager().handleServerManagement(object);
                AbstractServerManagementResponse abstractServerManagementResponse = (AbstractServerManagementResponse) object;
                Integer syncToken = abstractServerManagementResponse.getSyncToken();
                SynchronizationManager.getSynchronizationManager().notifyOnToken(syncToken);
            } else if (object instanceof AbstractServerResponse) {
                AbstractServerResponse abstractServerResponse = (AbstractServerResponse) object;
                ServerResponseInterface.ResponseOperationType responseType = abstractServerResponse.getOperationType();
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
                    case SR_GET_DIRECTORY:
                        handleGetDirectoryResponse(object);
                        break;
                    case SR_GET_REVISION_FOR_COMPARE:
                        handleGetRevisionForCompareResponse(object);
                        break;
                    case SR_GET_USER_COMMIT_COMMENTS:
                        handleGetUserCommitComments(object);
                        break;
                    case SR_GET_COMMIT_LIST_FOR_MOVEABLE_TAG_READ_ONLY_BRANCHES:
                        handleGetCommitListForMoveableTagReadOnlyBranches(object);
                        break;
                    case SR_GET_BRIEF_COMMIT_INFO_LIST:
                        handleGetBriefCommitInfoList(object);
                        break;
                    case SR_GET_USER_PROPERTIES:
                        handleGetUserPropertiesResponse(object);
                        break;
                    case SR_ADD_USER_PROPERTY:
                        handleAddUserPropertyResponse(object);
                        break;
                    case SR_UPDATE_VIEW_UTILITY_COMMAND:
                        handleUpdateViewUtilityCommandResponse(object);
                        break;
                    case SR_UPDATE_FILTER_FILE_COLLECTION:
                        handleFileFilterResponse(object);
                        break;
                    case SR_CHECK_IN:
                        handleCheckInResponse(object);
                        break;
                    case SR_GET_FOR_VISUAL_COMPARE:
                        handleGetForVisualCompareResponse(object);
                        break;
                    case SR_ADD_DIRECTORY:
                        handleAddDirectoryResponse(object);
                        break;
                    case SR_PROJECT_CONTROL:
                        handleProjectControlResponse(object);
                        break;
                    case SR_GET_LOGFILE_INFO:
                        handleGetLogfileInfoResponse(object);
                        break;
                    case SR_GET_ALL_LOGFILE_INFO:
                        handleGetAllLogfileInfoResponse(object);
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
                    case SR_GET_INFO_FOR_MERGE:
                        handleGetInfoForMerge(object);
                        break;
                    case SR_RESOLVE_CONFLICT_FROM_PARENT_BRANCH:
                        handleResolveConflictFromParentBranch(object);
                        break;
                    case SR_LIST_FILES_TO_PROMOTE:
                        handleListFilesToPromoteResponse(object);
                        break;
                    case SR_PROMOTE_FILE_SIMPLE:
                        handlePromoteFileSimpleResponse(object);
                        break;
                    case SR_PROMOTE_FILE_CREATE:
                        handlePromoteCreateResponse(object);
                        break;
                    case SR_PROMOTE_FILE_RENAME:
                        handlePromoteRenameResponse(object);
                        break;
                    case SR_PROMOTE_FILE_MOVE:
                        handlePromoteMoveResponse(object);
                        break;
                    case SR_PROMOTE_FILE_MOVE_AND_RENAME:
                        handlePromoteMoveAndRenameResponse(object);
                        break;
                    case SR_PROMOTE_FILE_DELETE:
                        handlePromoteDeleteResponse(object);
                        break;
                    case SR_GET_MOST_RECENT_ACTIVITY:
                        handleGetMostRecentActivity(object);
                        break;
                    case SR_APPLY_TAG:
                        handleApplyTagResponse(object);
                        break;
                    case SR_GET_TAGS:
                        handleGetTagsResponse(object);
                        break;
                    case SR_GET_TAGS_INFO:
                        handleGetTagsInfoResponse(object);
                        break;
                    default:
                        LOGGER.warn("read unknown or unexpected response object: " + object.getClass().toString());
                        break;
                }
                Integer syncToken = abstractServerResponse.getSyncToken();
                LOGGER.debug("Response type: [{}] token: [{}]", abstractServerResponse.getOperationType(), syncToken);
                SynchronizationManager.getSynchronizationManager().notifyOnToken(syncToken);
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
         * @param object the notification object.
         */
        void handleServerNotifications(Object object) {
            ServerNotificationInterface serverNotification = (ServerNotificationInterface) object;
            ServerNotificationInterface.NotificationType notificationType = serverNotification.getNotificationType();
            switch (notificationType) {
                case SR_NOTIFY_CHECKIN:
                    handleCheckInNotification(object);
                    break;
                case SR_NOTIFY_CREATE:
                    handleCreateFileNotification(object);
                    break;
                case SR_NOTIFY_REMOVE:
                    handleRemoveFileNotification(object);
                    break;
                case SR_NOTIFY_RENAME:
                    handleRenameFileNotification(object);
                    break;
                case SR_NOTIFY_MOVEFILE:
                    handleMoveFileNotification(object);
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
                if (response.getVersionsMatchFlag()) {
                    // Capture user properties...
                    RemotePropertiesBaseClass remoteProperties = RemotePropertiesManager.getInstance().getRemoteProperties(responseProxy.getUsername(), responseProxy);
                    Map<String, UserPropertyData> propertyMap = remoteProperties.getUserPropertyMap();
                    for (UserPropertyData u : response.getUserPropertyList()) {
                        propertyMap.put(u.getPropertyName(), u);
                    }
                }
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
            LOGGER.trace("received ServerResponseRegisterClientListener for [{}]:[{}]:[{}]", response.getProjectName(), response.getBranchName(), response.getAppendedPath());
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
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
                dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(), response.getAppendedPath());

                if ((dirManagerProxy != null) && createWorkfileDirectory(workfile) && canOverwriteWorkfile(response, workfile)) {
                    // Save this workfile in the client workfile cache.
                    ClientWorkfileCache.getInstance().addBuffer(response.getProjectName(), response.getAppendedPath(), response.getShortWorkfileName(),
                            response.getRevisionString(), response.getBuffer());

                    try {
                        outputStream = new java.io.FileOutputStream(workfile);
                        Utility.writeDataToStream(response.getBuffer(), outputStream);
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
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
                SynchronizationManager.getSynchronizationManager().notifyOnToken(response.getSyncToken());
            } catch (java.io.IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                if (dirManagerProxy != null) {
                    dirManagerProxy.notifyListeners();
                }
            }
        }

        private void handleGetDirectoryResponse(Object object) {
            ServerResponseGetDirectory response = (ServerResponseGetDirectory) object;
        }

        void handleCreateArchiveResponse(Object object) {
            ServerResponseCreateArchive response = (ServerResponseCreateArchive) object;
            LOGGER.trace("read ServerResponseCreateArchive for directory " + response.getAppendedPath());
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleGetLogfileInfoResponse(Object object) {
            ServerResponseGetLogfileInfo response = (ServerResponseGetLogfileInfo) object;

            // Figure out our proxy directory manager.
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleGetRevisionForCompareResponse(Object object) {
            ServerResponseGetRevisionForCompare response = (ServerResponseGetRevisionForCompare) object;

            // Figure out our proxy directory manager.
            ArchiveDirManagerProxy dirManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            response.updateDirManagerProxy(dirManagerProxy);
        }

        void handleGetUserCommitComments(Object object) {
            ServerResponseGetUserCommitComments response = (ServerResponseGetUserCommitComments) object;
            responseProxy.getProxyListener().notifyTransportProxyListener(response);
        }

        void handleGetCommitListForMoveableTagReadOnlyBranches(Object object) {
            ServerResponseGetCommitListForMoveableTagReadOnlyBranches response = (ServerResponseGetCommitListForMoveableTagReadOnlyBranches) object;
            responseProxy.getProxyListener().notifyTransportProxyListener(response);
        }

        void handleGetBriefCommitInfoList(Object object) {
            ServerResponseGetBriefCommitInfoList response = (ServerResponseGetBriefCommitInfoList) object;
            responseProxy.getProxyListener().notifyTransportProxyListener(response);
        }

        void handleCheckInResponse(Object object) {
            ServerResponseCheckIn response = (ServerResponseCheckIn) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());

            // Update the LogfileInfo info for the ArchiveDirectoryManagerProxy
            directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
            LogFileProxy logFileProxy = (LogFileProxy) directoryManagerProxy.getArchiveInfo(response.getShortWorkfileName());
            WorkFile workfile = new WorkFile(response.getClientWorkfileName());
            if (logFileProxy.getAttributes().getIsDeleteWork()) {
                // The attributes say to delete the workfile....
                workfile.delete();
            } else {
                // Make the workfile read-only if we are supposed to.
                if (logFileProxy.getAttributes().getIsProtectWorkfile() || response.getProtectWorkfileFlag()) {
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
                directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(), response.getAppendedPath());

                if ((directoryManagerProxy != null) && createWorkfileDirectory(workfile) && canOverwriteWorkfile(workfile)) {
                    try {
                        outputStream = new java.io.FileOutputStream(workfile);
                        Utility.writeDataToStream(response.getBuffer(), outputStream);
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
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

        private void handleAddDirectoryResponse(Object object) {
            ServerResponseAddDirectory response = (ServerResponseAddDirectory) object;
        }

        private void handleMoveFileResponse(Object object) {
            ServerResponseMoveFile response = (ServerResponseMoveFile) object;

            // With a move response, there is no guarantee that the required directory managers already exist...
            // So we have to make sure that they do so that we can move the workfile (at least).
            TransportProxyInterface transProxy = getTransportProxy(responseProxy.getServerProperties());
            RemotePropertiesBaseClass remoteProperties =
                    RemotePropertiesManager.getInstance().getRemoteProperties(responseProxy.getUsername(), transProxy);
            String workfileBaseDirectory = remoteProperties.getWorkfileLocation(responseProxy.getServerProperties().getServerName(), response.getProjectName(),
                    response.getBranchName());
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

            DirectoryCoordinate originDirectoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getBranchName(), response.getOriginAppendedPath());
            DirectoryManagerInterface originDirectoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(TransportProxyFactory.getInstance().getDirectory(),
                    responseProxy.getServerProperties().getServerName(),
                    originDirectoryCoordinate,
                    originWorkfileDirectory, null, false, true);
            DirectoryCoordinate destinationDirectoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getBranchName(),
                    response.getDestinationAppendedPath());
            DirectoryManagerInterface destinationDirectoryManager =
                    DirectoryManagerFactory.getInstance().getDirectoryManager(TransportProxyFactory.getInstance().getDirectory(),
                            responseProxy.getServerProperties().getServerName(),
                    destinationDirectoryCoordinate,
                    destinationWorkfileDirectory, null, false, true);

            ArchiveDirManagerProxy originDirectoryManagerProxy = (ArchiveDirManagerProxy) originDirectoryManager.getArchiveDirManager();
            ArchiveDirManagerProxy destinationDirectoryManagerProxy = (ArchiveDirManagerProxy) destinationDirectoryManager.getArchiveDirManager();

            if ((originDirectoryManagerProxy != null) && (destinationDirectoryManagerProxy != null)) {
                // Remove the archive info from the origin directory...
                // (This should already have been done by the notify).
                originDirectoryManagerProxy.removeArchiveInfo(response.getShortWorkfileName());

                // Add the archive info to the destination directory...
                // (This should already have been done by the notify).
                destinationDirectoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());

                // Move the workfile from the origin directory to the destination directory.
                // (This was NOT done by the notify).
                WorkFile.moveFile(originWorkfileDirectory, destinationWorkfileDirectory, response.getShortWorkfileName());

                LOGGER.info("Move file response received for: [" + response.getShortWorkfileName() + "]");

                // So we have a fresh notion of the workfiles that we have...
                if (originDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager() != null) {
                    originDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();
                }
                if (destinationDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager() != null) {
                    destinationDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();
                }

                originDirectoryManagerProxy.notifyListeners();
                destinationDirectoryManagerProxy.notifyListeners();
            }
        }

        void handleRenameArchiveResponse(Object object) {
            ServerResponseRenameArchive response = (ServerResponseRenameArchive) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
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
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
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

        void handlePromoteFileSimpleResponse(Object object) {
            ServerResponsePromotionSimple response = (ServerResponsePromotionSimple) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getMergedInfoSyncBranchName(),
                    response.getMergedInfoSyncAppendedPath());
            if (directoryManagerProxy != null) {
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            notifyListeners(response);
        }

        void handlePromoteCreateResponse(Object object) {
            ServerResponsePromotionCreate response = (ServerResponsePromotionCreate) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getMergedInfoSyncBranchName(),
                    response.getMergedInfoSyncAppendedPath());
            if (directoryManagerProxy != null) {
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            notifyListeners(response);
        }

        void handlePromoteRenameResponse(Object object) {
            LOGGER.info("Received rename response");
            ServerResponsePromotionRename response = (ServerResponsePromotionRename) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getMergedInfoSyncBranchName(),
                    response.getMergedInfoSyncAppendedPath());
            if (directoryManagerProxy != null) {
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            notifyListeners(response);
        }

        void handlePromoteMoveResponse(Object object) {
            ServerResponsePromotionMove response = (ServerResponsePromotionMove) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getMergedInfoSyncBranchName(),
                    response.getMergedInfoSyncAppendedPath());
            if (directoryManagerProxy != null) {
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            notifyListeners(response);
        }

        void handlePromoteMoveAndRenameResponse(Object object) {
            ServerResponsePromotionMoveAndRename response = (ServerResponsePromotionMoveAndRename) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getMergedInfoSyncBranchName(),
                    response.getMergedInfoSyncAppendedPath());
            if (directoryManagerProxy != null) {
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            notifyListeners(response);
        }

        void handlePromoteDeleteResponse(Object object) {
            ServerResponsePromotionDelete response = (ServerResponsePromotionDelete) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getMergedInfoSyncBranchName(),
                    response.getMergedInfoSyncAppendedPath());
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
            if (response.getShowCemeteryFlag()) {
                LOGGER.debug("ServerResponseProjectControl: projectName: [{}] branchName: [{}] addFlag: [{}] removeFlag: [{}] show cemetery Flag: [{}]",
                        response.getProjectName(), response.getBranchName(), response.getAddFlag(), response.getRemoveFlag(), response.getShowCemeteryFlag());
            } else {
                LOGGER.debug("ServerResponseProjectControl: projectName: [{}] branchName: [{}] addFlag: [{}] removeFlag: [{}] final segment: [{}]",
                        response.getProjectName(), response.getBranchName(), response.getAddFlag(), response.getRemoveFlag(),
                        response.getDirectorySegments()[response.getDirectorySegments().length - 1]);
            }
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
                ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                        response.getAppendedPath());
                response.updateDirManagerProxy(directoryManagerProxy);
            }
            LOGGER.warn("Error message from server [Project][Branch][AppendedPath]: [" + response.getProjectName() + "][" + response.getBranchName() + "]["
                    + response.getAppendedPath() + "]: "
                    + response.getErrorMessage());
        }

        void handleResponseMessage(Object object) {
            ServerResponseMessage response = (ServerResponseMessage) object;
            ArchiveDirManagerProxy directoryManagerProxy = null;
            if (response.getProjectName() != null) {
                directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(), response.getAppendedPath());
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

        void handleTransactionBeginResponse(Object object) {
            ServerResponseTransactionBegin response = (ServerResponseTransactionBegin) object;
            ClientTransactionManager.getInstance().beginTransaction(responseProxy.getServerProperties().getServerName(), response.getTransactionID());
        }

        void handleTransactionEndResponse(Object object) {
            ServerResponseTransactionEnd response = (ServerResponseTransactionEnd) object;
            ClientTransactionManager.getInstance().endTransaction(responseProxy.getServerProperties().getServerName(), response.getTransactionID());
            notifyTransactionEndListeners(response);
        }

        void handleApplyTagResponse(Object object) {
            ServerResponseApplyTag response = (ServerResponseApplyTag) object;
            LOGGER.info("Received apply tag response; tag text: [{}], tag id: [{}], commit id: [{}]", response.getTagText(), response.getTagId(), response.getCommitId());
        }

        void handleGetTagsResponse(Object object) {
            ServerResponseGetTags response = (ServerResponseGetTags) object;
            responseProxy.getProxyListener().notifyTransportProxyListener(response);
        }

        private void handleGetTagsInfoResponse(Object object) {
            ServerResponseGetTagsInfo response = (ServerResponseGetTagsInfo) object;
            responseProxy.getProxyListener().notifyTransportProxyListener(response);
        }

        private void handleGetAllLogfileInfoResponse(Object object) {
            ServerResponseGetAllLogfileInfo response = (ServerResponseGetAllLogfileInfo) object;
            responseProxy.getProxyListener().notifyTransportProxyListener(response);
        }

        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        // Handle notification messages.
        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        void handleCheckInNotification(Object object) {
            ServerNotificationCheckIn response = (ServerNotificationCheckIn) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                // Update the skinny logfileInfo info for the ArchiveDirectoryManagerProxy
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("CheckIn notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getBranchName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleCreateFileNotification(Object object) {
            ServerNotificationCreateArchive response = (ServerNotificationCreateArchive) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            if (directoryManagerProxy == null) {
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getBranchName(), response.getAppendedPath());
                DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(TransportProxyFactory.getInstance().getDirectory(),
                        response.getServerName(), directoryCoordinate,
                        null, null, false, true);
                directoryManagerProxy = (ArchiveDirManagerProxy) directoryManager.getArchiveDirManager();
            }
            if (directoryManagerProxy != null) {
                directoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());
                LOGGER.info("Creation notification received for: [{}::{}::{}/{}]", response.getProjectName(), response.getBranchName(), response.getAppendedPath(), response.getShortWorkfileName());

                // For promotions, we may get a create notification (after a remove notification). To compute an accurate status, we need
                // to update the associated workfile's revision string to match the parent branch.
                WorkfileDirectoryManagerInterface workfileDirManager = directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager();
                if (workfileDirManager != null) {
                    WorkfileInfoInterface workfileInfo = workfileDirManager.lookupWorkfileInfo(response.getShortWorkfileName());
                    if (workfileInfo != null) {
                        workfileInfo.setWorkfileRevisionString(response.getSkinnyLogfileInfo().getDefaultRevisionString());

                        // We also need to update the workfileinfo captured by the WorkfileDigestManager...
                        WorkfileInfoInterface digestWorkfileInfo = WorkfileDigestManager.getInstance().getDigestWorkfileInfo(workfileInfo);
                        if (digestWorkfileInfo != null) {
                            digestWorkfileInfo.setWorkfileRevisionString(response.getSkinnyLogfileInfo().getDefaultRevisionString());
                        }
                    }
                }
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleRemoveFileNotification(Object object) {
            ServerNotificationRemoveArchive response = (ServerNotificationRemoveArchive) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                directoryManagerProxy.removeArchiveInfo(response.getShortWorkfileName());
                LOGGER.info("Remove notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getBranchName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");
                directoryManagerProxy.notifyListeners();
            }
        }

        void handleRenameFileNotification(Object object) {
            ServerNotificationRenameArchive response = (ServerNotificationRenameArchive) object;
            ArchiveDirManagerProxy directoryManagerProxy = (ArchiveDirManagerProxy) responseProxy.getDirectoryManager(response.getProjectName(), response.getBranchName(),
                    response.getAppendedPath());
            if (directoryManagerProxy != null) {
                directoryManagerProxy.removeArchiveInfo(response.getOldShortWorkfileName());
                directoryManagerProxy.updateArchiveInfo(response.getNewShortWorkfileName(), response.getSkinnyLogfileInfo());

                LOGGER.info("Rename notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getBranchName() + "::["
                        + response.getAppendedPath() + "/"
                        + response.getOldShortWorkfileName() + "]");

                // So we have a fresh notion of the workfiles that we have...
                directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();

                directoryManagerProxy.notifyListeners();
            }
        }

        void handleMoveFileNotification(Object object) {
            ServerNotificationMoveArchive response = (ServerNotificationMoveArchive) object;

            // With a move notification, there is no guarantee that the required directory managers already exist...
            // So we have to make sure that they do so that we can move the workfile (at least).
            TransportProxyInterface transProxy = getTransportProxy(responseProxy.getServerProperties());
            RemotePropertiesBaseClass remoteProperties =
                    RemotePropertiesManager.getInstance().getRemoteProperties(responseProxy.getUsername(), transProxy);
            String workfileBaseDirectory = remoteProperties.getWorkfileLocation(responseProxy.getServerProperties().getServerName(), response.getProjectName(),
                    response.getBranchName());
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

            DirectoryCoordinate originDirectoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getBranchName(), response.getOriginAppendedPath());
            DirectoryManagerInterface originDirectoryManager =
                    DirectoryManagerFactory.getInstance().getDirectoryManager(TransportProxyFactory.getInstance().getDirectory(),
                            responseProxy.getServerProperties().getServerName(),
                            originDirectoryCoordinate,
                            originWorkfileDirectory, null, false, true);
            DirectoryCoordinate destinationDirectoryCoordinate = new DirectoryCoordinate(response.getProjectName(), response.getBranchName(),
                    response.getDestinationAppendedPath());
            DirectoryManagerInterface destinationDirectoryManager =
                    DirectoryManagerFactory.getInstance().getDirectoryManager(TransportProxyFactory.getInstance().getDirectory(),
                            responseProxy.getServerProperties().getServerName(),
                            destinationDirectoryCoordinate,
                            destinationWorkfileDirectory, null, false, true);

            ArchiveDirManagerProxy originDirectoryManagerProxy = (ArchiveDirManagerProxy) originDirectoryManager.getArchiveDirManager();
            ArchiveDirManagerProxy destinationDirectoryManagerProxy = (ArchiveDirManagerProxy) destinationDirectoryManager.getArchiveDirManager();

            if ((originDirectoryManagerProxy != null) && (destinationDirectoryManagerProxy != null)) {
                // Remove the archive info from the origin directory...
                originDirectoryManagerProxy.removeArchiveInfo(response.getShortWorkfileName());

                // Add the archive info to the destination directory...
                destinationDirectoryManagerProxy.updateArchiveInfo(response.getShortWorkfileName(), response.getSkinnyLogfileInfo());

                LOGGER.info("Move notification received for: ["
                        + response.getProjectName() + "::"
                        + response.getBranchName() + "::["
                        + response.getOriginAppendedPath() + "/"
                        + response.getShortWorkfileName() + "]");

                // So we have a fresh notion of the workfiles that we have...
                if (originDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager() != null) {
                    originDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();
                }
                if (destinationDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager() != null) {
                    destinationDirectoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();
                }

                originDirectoryManagerProxy.notifyListeners();
                destinationDirectoryManagerProxy.notifyListeners();
            }
        }

        private void handleGetUserPropertiesResponse(Object object) {
            ServerResponseGetUserProperties response = (ServerResponseGetUserProperties) object;
            RemotePropertiesBaseClass remoteProperties = RemotePropertiesManager.getInstance().getRemoteProperties(responseProxy.getUsername(), responseProxy);
            if (remoteProperties != null) {
                Map<String, UserPropertyData> propertyMap = remoteProperties.getUserPropertyMap();
                for (UserPropertyData u : response.getUserPropertyList()) {
                    propertyMap.put(u.getPropertyName(), u);
                }
            } else {
                LOGGER.warn("handleGetUserPropertiesResponse Failed to find remote properties for key value: [{}]", response.getPropertiesKey());
            }
        }

        private void handleAddUserPropertyResponse(Object object) {
            ServerResponseAddUserProperty response = (ServerResponseAddUserProperty) object;
            LOGGER.info("Received add user property response: [{}]:[{}]",
                    response.getUserPropertyData().getPropertyName(), response.getUserPropertyData().getPropertyValue());
            RemotePropertiesBaseClass remoteProperties = RemotePropertiesManager.getInstance().getRemoteProperties(responseProxy.getUsername(), responseProxy);
            if (remoteProperties != null) {
                LOGGER.info("handleAddUserPropertyResponse found remote properties for key: [{}]", response.getPropertiesKey());
            } else {
                LOGGER.warn("handleAddUserPropertyResponse Failed to find remote properties for key: [{}]", response.getPropertiesKey());
            }
        }

        private void handleUpdateViewUtilityCommandResponse(Object object) {
            ServerResponseUpdateViewUtilityCommandLine response = (ServerResponseUpdateViewUtilityCommandLine) object;
            LOGGER.info("Received update view utility response; commandLine: [{}], commandLineId: [{}], extension: [{}]",
                    response.getCommandLine(), response.getCommandLineId(), response.getExtension());
            viewUtilityResponseListenerInterfaceListenerList.stream().forEach((listener) -> {
                listener.notifyViewUtilityResponse(response);
            });
        }

        private void handleFileFilterResponse(Object object) {
            ServerResponseUpdateFilterFileCollection response = (ServerResponseUpdateFilterFileCollection) object;
            LOGGER.info("Received file filter response");
            fileFilterListenerInterfaceListenerList.stream().forEach((listener) -> {
                listener.notifyFileFilterResponse(response);
            });
        }
    }
}
