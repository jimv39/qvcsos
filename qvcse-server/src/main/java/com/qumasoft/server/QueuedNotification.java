/*
 * Copyright 2022 Jim Voris.
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
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class QueuedNotification {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueuedNotification.class);

    private final DirectoryCoordinate directoryCoordinate;
    private final SkinnyLogfileInfo skinnyInfo;
    private final ActionType action;

    public QueuedNotification(DirectoryCoordinate dc, SkinnyLogfileInfo ski, ActionType act) {
        LOGGER.info("Creating queued notification for {}::{}::{} Action: {}", dc.getProjectName(), dc.getBranchName(), dc.getAppendedPath(), act.getActionType());

        this.directoryCoordinate = dc;
        this.skinnyInfo = ski;
        this.action = act;
    }

    /**
     * @return the directoryCoordinate
     */
    public DirectoryCoordinate getDirectoryCoordinate() {
        return directoryCoordinate;
    }

    /**
     * @return the skinnyInfo
     */
    public SkinnyLogfileInfo getSkinnyInfo() {
        return skinnyInfo;
    }

    /**
     * @return the action
     */
    public ActionType getAction() {
        return action;
    }

}
