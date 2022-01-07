/*
 * Copyright 2021 Jim Voris.
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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import com.qumasoft.qvcslib.logfileaction.MoveFile;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.logfileaction.Rename;
import com.qumasoft.qvcslib.logfileaction.SetRevisionDescription;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckIn;
import com.qumasoft.qvcslib.notifications.ServerNotificationCreateArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationHeaderChange;
import com.qumasoft.qvcslib.notifications.ServerNotificationInterface;
import com.qumasoft.qvcslib.notifications.ServerNotificationMoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRemoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRenameArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationSetRevisionDescription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class DirectoryCoordinateListener {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryCoordinateListener.class);

    private final DirectoryCoordinate directoryCoordinate;
    private final Map<String, ServerResponseFactoryInterface> clientListenerMap = Collections.synchronizedMap(new TreeMap<>());
    private final List<ServerNotificationInterface> queuedNotificationList = Collections.synchronizedList(new ArrayList<>());

    public DirectoryCoordinateListener(DirectoryCoordinate dc) {
        this.directoryCoordinate = dc;
    }

    public void addClientListener(ServerResponseFactoryInterface clientListener) {
        String listenerKey = buildKey(clientListener);
        clientListenerMap.put(listenerKey, clientListener);
    }

    public void removeClientListener(ServerResponseFactoryInterface clientListener) {
        String listenerKey = buildKey(clientListener);
        clientListenerMap.remove(listenerKey);
    }

    private String buildKey(ServerResponseFactoryInterface listener) {
        String key = listener.getClientIPAddress() + "::" + listener.getClientPort();
        return key;
    }

    public void queueNotification(SkinnyLogfileInfo skinnyInfo, ActionType action) {
        // Build the information we need to send to the listeners.
        ServerNotificationInterface info = buildLogfileNotification(skinnyInfo, action);

        queuedNotificationList.add(info);
    }

    public void sendQueuedNotifications() {
        for (ServerNotificationInterface info : queuedNotificationList) {
            for (ServerResponseFactoryInterface clientListener : clientListenerMap.values()) {
                // Set the server name on the notification message.
                info.setServerName(clientListener.getServerName());

                LOGGER.info("Sending queued notification to: [{}]", buildKey(clientListener));

                // And send the info.
                clientListener.createServerResponse(info);
            }
        }
        queuedNotificationList.clear();
    }

    public void notifySkinnyInfoListeners(SkinnyLogfileInfo skinnyInfo, ActionType action) {
        // Build the information we need to send to the listeners.
        ServerNotificationInterface info = buildLogfileNotification(skinnyInfo, action);

        // Let any remote users know about the change.
        if (info != null) {
            for (ServerResponseFactoryInterface clientListener : clientListenerMap.values()) {
                // Set the server name on the notification message.
                info.setServerName(clientListener.getServerName());

                // And send the info.
                clientListener.createServerResponse(info);
            }
        }
    }

    private String getProjectName() {
        return this.directoryCoordinate.getProjectName();
    }

    private String getBranchName() {
        return this.directoryCoordinate.getBranchName();
    }

    private String getAppendedPath() {
        return this.directoryCoordinate.getAppendedPath();
    }

    private ServerNotificationInterface buildLogfileNotification(SkinnyLogfileInfo subject, ActionType action) {
        ServerNotificationInterface info = null;

        switch (action.getAction()) {
            case ActionType.CHECKIN:
                ServerNotificationCheckIn serverNotificationCheckIn = new ServerNotificationCheckIn();
                serverNotificationCheckIn.setProjectName(getProjectName());
                serverNotificationCheckIn.setBranchName(getBranchName());
                serverNotificationCheckIn.setAppendedPath(getAppendedPath());
                serverNotificationCheckIn.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationCheckIn.setSkinnyLogfileInfo(subject);
                info = serverNotificationCheckIn;
                break;
            case ActionType.CREATE:
                ServerNotificationCreateArchive serverNotificationCreateArchive = new ServerNotificationCreateArchive();
                serverNotificationCreateArchive.setProjectName(getProjectName());
                serverNotificationCreateArchive.setBranchName(getBranchName());
                serverNotificationCreateArchive.setAppendedPath(getAppendedPath());
                serverNotificationCreateArchive.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationCreateArchive.setSkinnyLogfileInfo(subject);
                info = serverNotificationCreateArchive;
                break;
            case ActionType.MOVE_FILE:
                if (action instanceof MoveFile) {
                    MoveFile moveFileAction = (MoveFile) action;
                    Properties fakeProperties = new Properties();
                    fakeProperties.setProperty("QVCS_IGNORECASEFLAG", QVCSConstants.QVCS_NO);
                    ServerNotificationMoveArchive serverNotificationMoveArchive = new ServerNotificationMoveArchive();
                    serverNotificationMoveArchive.setShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationMoveArchive.setOriginAppendedPath(moveFileAction.getOriginAppendedPath());
                    serverNotificationMoveArchive.setDestinationAppendedPath(moveFileAction.getDestinationAppendedPath());
                    serverNotificationMoveArchive.setProjectName(getProjectName());
                    serverNotificationMoveArchive.setBranchName(getBranchName());
                    serverNotificationMoveArchive.setProjectProperties(fakeProperties);
                    serverNotificationMoveArchive.setSkinnyLogfileInfo(subject);
                    info = serverNotificationMoveArchive;
                }
                break;
            case ActionType.SET_REVISION_DESCRIPTION:
                if (action instanceof SetRevisionDescription) {
                    SetRevisionDescription setRevisionDescriptionAction = (SetRevisionDescription) action;
                    ServerNotificationSetRevisionDescription serverNotificationSetRevisionDescription = new ServerNotificationSetRevisionDescription();
                    SetRevisionDescriptionCommandArgs commandArgs = setRevisionDescriptionAction.getCommandArgs();
                    serverNotificationSetRevisionDescription.setProjectName(getProjectName());
                    serverNotificationSetRevisionDescription.setBranchName(getBranchName());
                    serverNotificationSetRevisionDescription.setAppendedPath(getAppendedPath());
                    serverNotificationSetRevisionDescription.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationSetRevisionDescription.setRevisionDescription(commandArgs.getRevisionDescription());
                    serverNotificationSetRevisionDescription.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationSetRevisionDescription.setSkinnyLogfileInfo(subject);
                    info = serverNotificationSetRevisionDescription;
                }
                break;
            case ActionType.REMOVE:
                if (action instanceof Remove) {
                    Remove removeAction = (Remove) action;
                    ServerNotificationRemoveArchive serverNotificationRemoveArchive = new ServerNotificationRemoveArchive();
                    serverNotificationRemoveArchive.setProjectName(getProjectName());
                    serverNotificationRemoveArchive.setBranchName(getBranchName());
                    serverNotificationRemoveArchive.setAppendedPath(getAppendedPath());
                    serverNotificationRemoveArchive.setShortWorkfileName(removeAction.getShortWorkfileName());
                    info = serverNotificationRemoveArchive;
                }
                break;
            case ActionType.RENAME:
                if (action instanceof Rename) {
                    Rename renameAction = (Rename) action;
                    ServerNotificationRenameArchive serverNotificationRenameArchive = new ServerNotificationRenameArchive();
                    serverNotificationRenameArchive.setProjectName(getProjectName());
                    serverNotificationRenameArchive.setBranchName(getBranchName());
                    serverNotificationRenameArchive.setAppendedPath(getAppendedPath());
                    serverNotificationRenameArchive.setNewShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationRenameArchive.setOldShortWorkfileName(renameAction.getOldShortWorkfileName());
                    serverNotificationRenameArchive.setSkinnyLogfileInfo(subject);
                    info = serverNotificationRenameArchive;
                }
                break;
            case ActionType.CHANGE_HEADER:
            case ActionType.CHANGE_REVHEADER:
            case ActionType.SET_ATTRIBUTES:
            case ActionType.SET_COMMENT_PREFIX:
            case ActionType.SET_MODULE_DESCRIPTION:
            default:
                ServerNotificationHeaderChange serverNotificationHeaderChange = new ServerNotificationHeaderChange();
                serverNotificationHeaderChange.setProjectName(getProjectName());
                serverNotificationHeaderChange.setBranchName(getBranchName());
                serverNotificationHeaderChange.setAppendedPath(getAppendedPath());
                serverNotificationHeaderChange.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationHeaderChange.setSkinnyLogfileInfo(subject);
                info = serverNotificationHeaderChange;
                break;
        }
        return info;
    }

}
