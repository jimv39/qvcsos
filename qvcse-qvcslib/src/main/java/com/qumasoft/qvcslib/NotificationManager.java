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

import com.qumasoft.qvcslib.logfileaction.ActionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    private final Map<String, DirectoryCoordinateListener> directoryCoordinatelisteners = Collections.synchronizedMap(new TreeMap<>());
    private final Map<String, Map<String, DirectoryCoordinate>> responseMapOfListeners = Collections.synchronizedMap(new TreeMap<>());
    private final List<DirectoryCoordinateListener> queuedDirectoryCoordinateListeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * Creates a new instance of Notification Manager.
     */
    private NotificationManager() {
    }

    /**
     * Get the Authentication Manager singleton.
     * @return the Authentication Manager singleton.
     */
    public static NotificationManager getNotificationManager() {
        return NOTIFICATION_MANAGER;
    }

    public void addDirectoryCoordinateListener(ServerResponseFactoryInterface response, DirectoryCoordinate directoryCoordinate) {
        String coordinateKey = buildCoordinateKey(directoryCoordinate);
        DirectoryCoordinateListener listener = directoryCoordinatelisteners.get(coordinateKey);
        if (listener == null) {
            listener = new DirectoryCoordinateListener(directoryCoordinate);
            directoryCoordinatelisteners.put(coordinateKey, listener);
        }
        listener.addClientListener(response);
        String responseFactoryKey = buildResponseFactoryKey(response);
        Map<String, DirectoryCoordinate> dcMap = responseMapOfListeners.get(responseFactoryKey);
        if (dcMap == null) {
            dcMap = Collections.synchronizedMap(new TreeMap<>());
            responseMapOfListeners.put(responseFactoryKey, dcMap);
        }
        dcMap.put(buildCoordinateKey(directoryCoordinate), directoryCoordinate);
    }

    public DirectoryCoordinateListener getDirectoryCoordinateListener(ServerResponseFactoryInterface response, DirectoryCoordinate dc) {
        String coordinateKey = buildCoordinateKey(dc);
        DirectoryCoordinateListener listener = directoryCoordinatelisteners.get(coordinateKey);
        if (listener == null) {
            LOGGER.info("Directory coordinate listener not found for [{}]:[{}]:[{}]", dc.getProjectName(), dc.getBranchName(), dc.getAppendedPath());
        }
        return listener;
    }

    public DirectoryCoordinateListener queueNotification(ServerResponseFactoryInterface response, DirectoryCoordinate directoryCoordinate, SkinnyLogfileInfo skinnyInfo, ActionType action) {
        String coordinateKey = buildCoordinateKey(directoryCoordinate);
        DirectoryCoordinateListener listener = directoryCoordinatelisteners.get(coordinateKey);
        if (listener == null) {
            throw new QVCSRuntimeException("Listener not found for coordinate key: [" + coordinateKey + "]");
        } else {
            listener.queueNotification(skinnyInfo, action);
        }
        queuedDirectoryCoordinateListeners.add(listener);
        return listener;
    }

    /**
     * The client has logged out, or otherwise disappeared. Get rid of the client's listeners.
     * @param response the response factory for the disappearing client.
     */
    public void removeServerResponseFactory(ServerResponseFactoryInterface response) {
        Map<String, DirectoryCoordinate> dcMap = responseMapOfListeners.get(buildResponseFactoryKey(response));
        if (dcMap != null) {
            for (DirectoryCoordinate dc : dcMap.values()) {
                String coordinateKey = buildCoordinateKey(dc);
                DirectoryCoordinateListener listener = directoryCoordinatelisteners.get(coordinateKey);
                listener.removeClientListener(response);
            }
        }
    }

    private String buildCoordinateKey(DirectoryCoordinate directoryCoordinate) {
        String key = directoryCoordinate.getProjectName() + "::" + directoryCoordinate.getBranchName() + "::" + directoryCoordinate.getAppendedPath();
        return key;
    }

    private String buildResponseFactoryKey(ServerResponseFactoryInterface response) {
        return response.getServerName() + "::" + response.getClientIPAddress() + "::" + response.getClientPort();
    }

    public void sendQueuedNotifications() {
        Runnable later = new Runnable() {
            @Override
            public void run() {
                try {
                    // <editor-fold>
                    Thread.sleep(1000L);
                    // </editor-fold>
                } catch (InterruptedException e) {
                    LOGGER.warn("Sleep interrupted.", e);
                }
                synchronized (queuedDirectoryCoordinateListeners) {
                    for (DirectoryCoordinateListener listener : queuedDirectoryCoordinateListeners) {
                        listener.sendQueuedNotifications();
                    }
                    queuedDirectoryCoordinateListeners.clear();
                }
            }
        };
        // Put all this on a separate worker thread.
        new Thread(later).start();
    }
}
