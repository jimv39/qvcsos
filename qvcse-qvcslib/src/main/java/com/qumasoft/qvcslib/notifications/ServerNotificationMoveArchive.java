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
package com.qumasoft.qvcslib.notifications;

import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import java.util.Properties;

/**
 * Move notification.
 * @author Jim Voris
 */
public class ServerNotificationMoveArchive implements ServerNotificationInterface {
    private static final long serialVersionUID = 368051276262981754L;

    // These are serialized:
    private String serverName;
    private String projectName;
    private String branchName;
    private String originAppendedPath;
    private String destinationAppendedPath;
    private String shortWorkfileName;
    private Properties projectPropertiesValues;
    private SkinnyLogfileInfo skinnyLogfileInfo;

    /**
     * Creates a new instance of ServerNotificationRenameArchive.
     */
    public ServerNotificationMoveArchive() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerName() {
        return serverName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerName(String server) {
        serverName = server;
    }

    /**
     * Get the project name.
     * @return the project name.
     *
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     * @param project the project name.
     *
     */
    public void setProjectName(String project) {
        projectName = project;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    @Override
    public String getBranchName() {
        return branchName;
    }

    /**
     * Set the branch name.
     * @param branch the branch name.
     */
    @Override
    public void setBranchName(final String branch) {
        branchName = branch;
    }

    /**
     * Get the origin appended path.
     * @return the origin appended path.
     */
    public String getOriginAppendedPath() {
        return originAppendedPath;
    }

    /**
     * Set the origin appended path.
     * @param originPath the origin appended path.
     */
    public void setOriginAppendedPath(String originPath) {
        originAppendedPath = originPath;
    }

    /**
     * Get the destination appended path.
     * @return the destination appended path.
     */
    public String getDestinationAppendedPath() {
        return destinationAppendedPath;
    }

    /**
     * Set the destination appended path.
     * @param destinationPath the destination appended path.
     */
    public void setDestinationAppendedPath(String destinationPath) {
        destinationAppendedPath = destinationPath;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     *
     */
    public void setShortWorkfileName(String shortName) {
        shortWorkfileName = shortName;
    }

    /**
     * Get the project properties.
     * @return the project properties.
     */
    public Properties getProjectProperties() {
        return projectPropertiesValues;
    }

    /**
     * Set the project properties.
     * @param projectProperties the project properties.
     */
    public void setProjectProperties(Properties projectProperties) {
        projectPropertiesValues = projectProperties;
    }

    /**
     * Get the skinny logfile info.
     * @return the skinny logfile info.
     */
    public SkinnyLogfileInfo getSkinnyLogfileInfo() {
        return skinnyLogfileInfo;
    }

    /**
     * Set the skinny logfile info.
     * @param skinnyInfo the skinny logfile info.
     */
    public void setSkinnyLogfileInfo(SkinnyLogfileInfo skinnyInfo) {
        skinnyLogfileInfo = skinnyInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationType getNotificationType() {
        return NotificationType.SR_NOTIFY_MOVEFILE;
    }
}
