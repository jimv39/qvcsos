/*
 * Copyright 2021-2023 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.server;

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import com.qumasoft.qvcslib.logfileaction.AddFile;
import com.qumasoft.qvcslib.logfileaction.CheckIn;
import com.qumasoft.qvcslib.logfileaction.MoveFile;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.logfileaction.Rename;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckIn;
import com.qumasoft.qvcslib.notifications.ServerNotificationCreateArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationInterface;
import com.qumasoft.qvcslib.notifications.ServerNotificationMoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRemoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRenameArchive;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage notifications to clients.
 * @author Jim Voris
 */
public final class NotificationManager {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationManager.class);

    private static final NotificationManager NOTIFICATION_MANAGER = new NotificationManager();
    private final DatabaseManager databaseManager;
    private final String schemaName;

    // This is the way we keep track of the clients who are paying attention to a given directory coordinate.
    // The map key is the directory coordinate key; the Set is the collection of response objects.
    private final Map<String, Set<ServerResponseFactoryInterface>> mapOfSetsOfConnectedClients = Collections.synchronizedMap(new TreeMap<>());

    // This is where we keep track of the branchId of the tip revision of a file for a given branch.
    // The first map is keyed by branchId, the 2nd map is keyed by fileId, and its value is the branchId of the tip revision for the given file.
    private final Map<Integer, Map<Integer, Integer>> mapOfMapsOfTipBranchIds = Collections.synchronizedMap(new TreeMap<>());

    // The list of queued notifications.
    private final List<QueuedNotification> queuedNotificationList = Collections.synchronizedList(new ArrayList<>());

    /**
     * Creates a new instance of Notification Manager.
     */
    private NotificationManager() {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
    }

    /**
     * Get the Authentication Manager singleton.
     * @return the Authentication Manager singleton.
     */
    public static NotificationManager getNotificationManager() {
        return NOTIFICATION_MANAGER;
    }

    public void addDirectoryCoordinateListener(ServerResponseFactoryInterface response, DirectoryCoordinate directoryCoordinate, List<SkinnyLogfileInfo> skinnyArray) {
        String coordinateKey = buildCoordinateKey(directoryCoordinate);
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        DirectoryCoordinateIds fbDcIds = functionalQueriesDAO.getDirectoryCoordinateIds(directoryCoordinate);

        Set<ServerResponseFactoryInterface> setOfAttentiveClients = mapOfSetsOfConnectedClients.get(coordinateKey);
        if (setOfAttentiveClients == null) {
            setOfAttentiveClients = new HashSet<>();
            mapOfSetsOfConnectedClients.put(coordinateKey, setOfAttentiveClients);
        }
        setOfAttentiveClients.add(response);

        // Iterate over the skinnyArray to capture the branchId of the file's tip revision.
        Map<Integer, Integer> mapsOfTipBranchIds = mapOfMapsOfTipBranchIds.get(fbDcIds.getBranchId());
        if (mapsOfTipBranchIds == null) {
            mapsOfTipBranchIds = new TreeMap<>();
            mapOfMapsOfTipBranchIds.put(fbDcIds.getBranchId(), mapsOfTipBranchIds);
        }
        for (SkinnyLogfileInfo skinnyInfo : skinnyArray) {
            mapsOfTipBranchIds.put(skinnyInfo.getFileID(), skinnyInfo.getBranchId());
        }
    }

    public void addNotificationListenersForParentBranches(ServerResponseFactoryInterface response, DirectoryCoordinate dc, DirectoryCoordinateIds ids) {
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Integer branchId = ids.getChildWriteableBranchMap().keySet().iterator().next();
        Branch branch = branchDAO.findById(branchId);
        while (branch.getParentBranchId() != null) {
            branch = branchDAO.findById(branch.getParentBranchId());
            DirectoryCoordinate parentDirectoryCoordinate = new DirectoryCoordinate(dc.getProjectName(), branch.getBranchName(), dc.getAppendedPath());
            String parentCoordinateKey = buildCoordinateKey(parentDirectoryCoordinate);
            Set<ServerResponseFactoryInterface> setOfAttentiveClients = mapOfSetsOfConnectedClients.get(parentCoordinateKey);
            if (setOfAttentiveClients == null) {
                setOfAttentiveClients = new HashSet<>();
                mapOfSetsOfConnectedClients.put(parentCoordinateKey, setOfAttentiveClients);
            }
            setOfAttentiveClients.add(response);
        }
    }

    public void queueNotification(ServerResponseFactoryInterface response, DirectoryCoordinate directoryCoordinate, SkinnyLogfileInfo skinnyInfo, ActionType action) {
        QueuedNotification qn = new QueuedNotification(directoryCoordinate, skinnyInfo, action);
        queuedNotificationList.add(qn);
    }

    /**
     * The client has logged out, or otherwise disappeared. Get rid of the client's listeners.
     * @param response the response factory for the disappearing client.
     */
    public void removeServerResponseFactory(ServerResponseFactoryInterface response) {

        // Go through all the directories in the map of sets of connected clients, and remove the just disconnected response...
        for (Set<ServerResponseFactoryInterface> responseSet : mapOfSetsOfConnectedClients.values()) {
            if (responseSet.contains(response)) {
                responseSet.remove(response);
            }
        }
    }

    private String buildCoordinateKey(DirectoryCoordinate directoryCoordinate) {
        String key = directoryCoordinate.getProjectName() + "::" + directoryCoordinate.getBranchName() + "::" + directoryCoordinate.getAppendedPath();
        return key;
    }

    public void sendQueuedNotifications() {
        Runnable later = () -> {
            try {
                // <editor-fold>
                Thread.sleep(1000L);
                // </editor-fold>
            }
            catch (InterruptedException e) {
                LOGGER.warn("Sleep interrupted.", e);
            }
            synchronized (queuedNotificationList) {
                for (QueuedNotification qn : queuedNotificationList) {
                    final DirectoryCoordinate fdc = qn.getDirectoryCoordinate();
                    final SkinnyLogfileInfo fski = qn.getSkinnyInfo();
                    final ActionType fa = qn.getAction();
                    LOGGER.info("Sending queued notification: {}::{}::{} Action: {}", fdc.getProjectName(), fdc.getBranchName(), fdc.getAppendedPath(), fa.getActionType());
                    notifySkinnyInfoListeners(fdc, fski, fa);
                }
                queuedNotificationList.clear();
            }
        };
        // Put all this on a separate worker thread.
        new Thread(later).start();
    }

    public void notifySkinnyInfoListeners(DirectoryCoordinate dc, SkinnyLogfileInfo skinnyInfo, ActionType action) {
        String coordinateKey = buildCoordinateKey(dc);
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        DirectoryCoordinateIds fbDcIds = functionalQueriesDAO.getDirectoryCoordinateIds(dc);

        // Build the information we need to send to the listeners.
        ServerNotificationInterface notifyInfo = buildLogfileNotification(dc, skinnyInfo, action);

        // Let any remote users know about the change.
        if (notifyInfo != null) {

            // We may need to remove a fileId from a branch's fileId --> branchId map...
            Map<Integer, Integer> fileIdMapToUpdate = null;
            Integer fileIdToRemove = null;

            Set<ServerResponseFactoryInterface> clientListenerSet = mapOfSetsOfConnectedClients.get(coordinateKey);
            if (clientListenerSet != null) {
                for (ServerResponseFactoryInterface clientListener : clientListenerSet) {
                    // Set the server name on the notification message.
                    notifyInfo.setServerName(clientListener.getServerName());

                    // Into the meat of notifying all potentially interested branches...
                    for (Integer branchId : fbDcIds.getChildWriteableBranchMap().keySet()) {
                        Map<Integer, Integer> fileIdMap = mapOfMapsOfTipBranchIds.get(branchId);

                        if (fileIdMap != null) {
                            // If the clientListener (a.k.a. ServerResponseFactoryInterface object) is paying attention to this file...
                            Integer currentBranchId = fileIdMap.get(skinnyInfo.getFileID());
                            if (currentBranchId != null) {
                                LOGGER.info("Notification: {} for file: {}", action.getActionType(), skinnyInfo.getShortWorkfileName());
                                if (Objects.equals(skinnyInfo.getBranchId(), branchId) && !Objects.equals(currentBranchId, skinnyInfo.getBranchId())) {

                                    // Update the branch id.
                                    fileIdMap.put(skinnyInfo.getFileID(), skinnyInfo.getBranchId());
                                    currentBranchId = skinnyInfo.getBranchId();

                                    // Update any child branch map entries to use the new branchId, provided they don't already have checkins on that child branch.
                                    updateChildMapEntries(branchId, skinnyInfo, fbDcIds);
                                }

                                // And send the info for any child branches...
                                if (Objects.equals(currentBranchId, skinnyInfo.getBranchId())) {
                                    String branchName = fbDcIds.getChildWriteableBranchMap().get(branchId);
                                    notifyInfo.setBranchName(branchName);
                                    notifyInfo.setBranchId(branchId);
                                    clientListener.createServerResponse(notifyInfo);
                                }

                                // If this was a remove notification, we need to remove the file's entry in the map after notifications have been sent to all client listeners...
                                if (action.getAction() == ActionType.REMOVE_FILE) {
                                    // Save info that we need to remove the map entry...
                                    fileIdMapToUpdate = fileIdMap;
                                    fileIdToRemove = skinnyInfo.getFileID();
                                }
                            } else {
                                // Create notifications go through here.
                                fileIdMap.put(skinnyInfo.getFileID(), skinnyInfo.getBranchId());
                                switch (notifyInfo.getNotificationType()) {
                                    case SR_NOTIFY_CHECKIN -> {
                                        LOGGER.info("checkin notification");
                                        String branchName = fbDcIds.getChildWriteableBranchMap().get(branchId);
                                        notifyInfo.setBranchName(branchName);
                                        notifyInfo.setBranchId(branchId);
                                        clientListener.createServerResponse(notifyInfo);
                                    }
                                    case SR_NOTIFY_CREATE -> {
                                        LOGGER.info("create notification");
                                        String branchName = fbDcIds.getChildWriteableBranchMap().get(branchId);
                                        notifyInfo.setBranchName(branchName);
                                        notifyInfo.setBranchId(branchId);
                                        clientListener.createServerResponse(notifyInfo);
                                    }
                                    case SR_NOTIFY_REMOVE -> {
                                        LOGGER.info("remove notification");
                                        String branchName = fbDcIds.getChildWriteableBranchMap().get(branchId);
                                        notifyInfo.setBranchName(branchName);
                                        notifyInfo.setBranchId(branchId);
                                        clientListener.createServerResponse(notifyInfo);
                                    }
                                    case SR_NOTIFY_RENAME -> {
                                        LOGGER.info("rename notification");
                                        String branchName = fbDcIds.getChildWriteableBranchMap().get(branchId);
                                        notifyInfo.setBranchName(branchName);
                                        notifyInfo.setBranchId(branchId);
                                        clientListener.createServerResponse(notifyInfo);
                                    }
                                    case SR_NOTIFY_MOVEFILE -> {
                                        LOGGER.info("move notification");
                                        String branchName = fbDcIds.getChildWriteableBranchMap().get(branchId);
                                        notifyInfo.setBranchName(branchName);
                                        notifyInfo.setBranchId(branchId);
                                        clientListener.createServerResponse(notifyInfo);
                                    }
                                    default -> {
                                        throw new QVCSRuntimeException("Unexpected notification type.");
                                    }
                                }
                            }
                        }
                    }
                }
                // Remove any map entry for remove notifications.
                if (fileIdMapToUpdate != null) {
                    fileIdMapToUpdate.remove(fileIdToRemove);
                }
            }
        }
    }

    private ServerNotificationInterface buildLogfileNotification(DirectoryCoordinate dc, SkinnyLogfileInfo subject, ActionType action) {
        ServerNotificationInterface info = null;

        switch (action.getAction()) {
            case ActionType.CHECKIN_FILE -> {
                if (action instanceof CheckIn checkInAction) {
                    ServerNotificationCheckIn serverNotificationCheckIn = new ServerNotificationCheckIn();
                    serverNotificationCheckIn.setProjectName(dc.getProjectName());
                    serverNotificationCheckIn.setBranchName(dc.getBranchName());
                    serverNotificationCheckIn.setAppendedPath(dc.getAppendedPath());
                    serverNotificationCheckIn.setShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationCheckIn.setSkinnyLogfileInfo(subject);
                    info = serverNotificationCheckIn;
                }
            }
            case ActionType.ADD_FILE -> {
                if (action instanceof AddFile addFileAction) {
                    ServerNotificationCreateArchive serverNotificationCreateArchive = new ServerNotificationCreateArchive();
                    serverNotificationCreateArchive.setProjectName(dc.getProjectName());
                    serverNotificationCreateArchive.setBranchName(dc.getBranchName());
                    serverNotificationCreateArchive.setAppendedPath(dc.getAppendedPath());
                    serverNotificationCreateArchive.setShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationCreateArchive.setSkinnyLogfileInfo(subject);
                    info = serverNotificationCreateArchive;
                }
            }
            case ActionType.MOVE_FILE -> {
                if (action instanceof MoveFile moveFileAction) {
                    Properties fakeProperties = new Properties();
                    fakeProperties.setProperty("QVCS_IGNORECASEFLAG", QVCSConstants.QVCS_NO);
                    ServerNotificationMoveArchive serverNotificationMoveArchive = new ServerNotificationMoveArchive();
                    serverNotificationMoveArchive.setShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationMoveArchive.setOriginAppendedPath(moveFileAction.getOriginAppendedPath());
                    serverNotificationMoveArchive.setDestinationAppendedPath(moveFileAction.getDestinationAppendedPath());
                    serverNotificationMoveArchive.setProjectName(dc.getProjectName());
                    serverNotificationMoveArchive.setBranchName(dc.getBranchName());
                    serverNotificationMoveArchive.setProjectProperties(fakeProperties);
                    serverNotificationMoveArchive.setSkinnyLogfileInfo(subject);
                    info = serverNotificationMoveArchive;
                }
            }

            case ActionType.REMOVE_FILE -> {
                if (action instanceof Remove removeAction) {
                    ServerNotificationRemoveArchive serverNotificationRemoveArchive = new ServerNotificationRemoveArchive();
                    serverNotificationRemoveArchive.setProjectName(dc.getProjectName());
                    serverNotificationRemoveArchive.setBranchName(dc.getBranchName());
                    serverNotificationRemoveArchive.setAppendedPath(dc.getAppendedPath());
                    serverNotificationRemoveArchive.setShortWorkfileName(removeAction.getShortWorkfileName());
                    info = serverNotificationRemoveArchive;
                }
            }

            case ActionType.RENAME_FILE -> {
                if (action instanceof Rename renameAction) {
                    ServerNotificationRenameArchive serverNotificationRenameArchive = new ServerNotificationRenameArchive();
                    serverNotificationRenameArchive.setProjectName(dc.getProjectName());
                    serverNotificationRenameArchive.setBranchName(dc.getBranchName());
                    serverNotificationRenameArchive.setAppendedPath(dc.getAppendedPath());
                    serverNotificationRenameArchive.setNewShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationRenameArchive.setOldShortWorkfileName(renameAction.getOldShortWorkfileName());
                    serverNotificationRenameArchive.setSkinnyLogfileInfo(subject);
                    info = serverNotificationRenameArchive;
                }
            }

            default -> {
            }
        }
        return info;
    }

    private void updateChildMapEntries(Integer branchId, SkinnyLogfileInfo skinnyInfo, DirectoryCoordinateIds fbDcIds) {
        for (Integer brnchId : fbDcIds.getChildWriteableBranchMap().keySet()) {
            if (brnchId > branchId) {
                Map<Integer, Integer> fileIdMap = mapOfMapsOfTipBranchIds.get(brnchId);
                if (fileIdMap != null) {
                    Integer currentBranchId = fileIdMap.get(skinnyInfo.getFileID());
                    if (currentBranchId != null && currentBranchId < skinnyInfo.getBranchId()) {

                        // Update the branch id.
                        fileIdMap.put(skinnyInfo.getFileID(), skinnyInfo.getBranchId());
                    }
                }
            }
        }
    }
}
