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

/**
 * Create archive notification.
 * @author Jim Voris
 */
public class ServerNotificationCreateArchive implements ServerNotificationInterface {
    private static final long serialVersionUID = 3390665908381695827L;

    // These are serialized:
    private String serverName;
    private String projectName;
    private String branchName;
    private String appendedPath;
    private String shortWorkfileName;
    private SkinnyLogfileInfo skinnyLogfileInfo;

    /**
     * Creates a new instance of ServerNotificationCreateArchive.
     */
    public ServerNotificationCreateArchive() {
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
        this.serverName = server;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Set the project name.
     * @param project the project name.
     */
    public void setProjectName(String project) {
        this.projectName = project;
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
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return this.appendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return this.shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setShortWorkfileName(String shortName) {
        this.shortWorkfileName = shortName;
    }

    /**
     * Get the skinny logfile info.
     * @return the skinny logfile info.
     */
    public SkinnyLogfileInfo getSkinnyLogfileInfo() {
        return this.skinnyLogfileInfo;
    }

    /**
     * Set the skinny logfile info.
     * @param skinnyInfo the skinny logfile info.
     */
    public void setSkinnyLogfileInfo(SkinnyLogfileInfo skinnyInfo) {
        this.skinnyLogfileInfo = skinnyInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationType getNotificationType() {
        return NotificationType.SR_NOTIFY_CREATE;
    }
}
