/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;

/**
 * Rename response message.
 * @author Jim Voris
 */
public class ServerResponseRenameArchive implements ServerResponseInterface {
    private static final long serialVersionUID = 7899351158523869198L;

    // These are serialized:
    /**
     * Holds value of property oldShortWorkfileName.
     */
    private String oldShortWorkfileName;

    /**
     * Holds value of property newShortWorkfileName.
     */
    private String newShortWorkfileName;

    /**
     * Holds value of property appendedPath.
     */
    private String appendedPath;

    /**
     * Holds value of property projectName.
     */
    private String projectName;

    /**
     * Holds value of property viewName.
     */
    private String viewName;

    /**
     * Holds value of property projectName.
     */
    private String serverName;

    // Send back the skinny logfile info
    private SkinnyLogfileInfo skinnyLogfileInfo;

    /**
     * Creates a new instance of ServerResponseRenameArchive.
     */
    public ServerResponseRenameArchive() {
    }

    /**
     * Getter for property shortWorkfileName.
     *
     * @return Value of property shortWorkfileName.
     *
     */
    public String getOldShortWorkfileName() {
        return this.oldShortWorkfileName;
    }

    /**
     * Setter for property shortWorkfileName.
     *
     * @param oldShortName New value of property oldShortWorkfileName.
     *
     */
    public void setOldShortWorkfileName(String oldShortName) {
        this.oldShortWorkfileName = oldShortName;
    }

    /**
     * Getter for property newShortWorkfileName.
     *
     * @return Value of property newShortWorkfileName.
     *
     */
    public String getNewShortWorkfileName() {
        return this.newShortWorkfileName;
    }

    /**
     * Setter for property shortWorkfileName.
     *
     * @param newShortName New value of property newShortWorkfileName.
     *
     */
    public void setNewShortWorkfileName(String newShortName) {
        this.newShortWorkfileName = newShortName;
    }

    /**
     * Getter for property appendedPath.
     *
     * @return Value of property appendedPath.
     *
     */
    public String getAppendedPath() {
        return this.appendedPath;
    }

    /**
     * Setter for property appendedPath.
     *
     * @param path New value of property appendedPath.
     *
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
    }

    /**
     * Getter for property projectName.
     *
     * @return Value of property projectName.
     *
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Setter for property projectName.
     *
     * @param project New value of property projectName.
     *
     */
    public void setProjectName(String project) {
        this.projectName = project;
    }

    /**
     * Get the view name.
     * @return the view name.
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Set the view name.
     * @param view the view name.
     */
    public void setViewName(final String view) {
        viewName = view;
    }

    /**
     * Get server name.
     * @return server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
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
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_RENAME_FILE;
    }
}
