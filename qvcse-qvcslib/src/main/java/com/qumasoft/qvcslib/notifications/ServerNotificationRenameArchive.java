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
 * Rename notification.
 * @author Jim Voris
 */
public class ServerNotificationRenameArchive implements ServerNotificationInterface {
    private static final long serialVersionUID = -7976793814816980040L;

    // These are serialized:
    private String oldShortWorkfileName;
    private String newShortWorkfileName;
    private String appendedPath;
    private String projectName;
    private String branchName;
    private String serverName;
    private SkinnyLogfileInfo skinnyLogfileInfo;

    /**
     * Creates a new instance of ServerNotificationRenameArchive.
     */
    public ServerNotificationRenameArchive() {
    }

    /**
     * Get the old short workfile name.
     * @return the old short workfile name.
     */
    public String getOldShortWorkfileName() {
        return this.oldShortWorkfileName;
    }

    /**
     * Set the old short workfile name.
     * @param oldShortName the old short workfile name.
     */
    public void setOldShortWorkfileName(String oldShortName) {
        this.oldShortWorkfileName = oldShortName;
    }

    /**
     * Get the new short workfile name.
     * @return the new short workfile name.
     */
    public String getNewShortWorkfileName() {
        return this.newShortWorkfileName;
    }

    /**
     * Set the new short workfile name.
     * @param newShortName the new short workfile name.
     */
    public void setNewShortWorkfileName(String newShortName) {
        this.newShortWorkfileName = newShortName;
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
        return NotificationType.SR_NOTIFY_RENAME;
    }
}
