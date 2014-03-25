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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.swing.event.ChangeListener;

/**
 * Special archive directory manager for the root tree node.
 * @author Jim Voris
 */
public class ArchiveDirManagerForRoot implements ArchiveDirManagerInterface {

    private String userName = null;
    private final Date mostRecentCheckInDate = new Date(0L);

    /**
     * Creates a new instance of ArchiveDirManagerForRoot.
     */
    public ArchiveDirManagerForRoot() {
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public String getAppendedPath() {
        return "";
    }

    @Override
    public ArchiveInfoInterface getArchiveInfo(String shortWorkfileName) {
        return null;
    }

    @Override
    public Map<String, ArchiveInfoInterface> getArchiveInfoCollection() {
        return null;
    }

    @Override
    public String getProjectName() {
        return QVCSConstants.QWIN_DEFAULT_PROJECT_NAME;
    }

    @Override
    public AbstractProjectProperties getProjectProperties() {
        return null;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name.
     * @param user the user name.
     */
    public void setUserName(String user) {
        userName = user;
    }

    @Override
    public void notifyListeners() {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    @Override
    public void setDirectoryManager(DirectoryManagerInterface directoryManager) {
    }

    @Override
    public void startDirectoryManager() {
    }

    @Override
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, String filename, ServerResponseFactoryInterface response)
            throws IOException, QVCSException {
        return false;
    }

    @Override
    public boolean createDirectory() {
        return false;
    }

    @Override
    public boolean renameArchive(String user, String oldShortWorkfileName, String newShortWorkfileName, ServerResponseFactoryInterface response) {
        return false;
    }

    @Override
    public boolean getFastNotify() {
        return false;
    }

    @Override
    public void setFastNotify(boolean flag) {
    }

    @Override
    public void createReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile, byte[] buffer) {
    }

    @Override
    public void deleteReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile) {
    }

    @Override
    public String getViewName() {
        return QVCSConstants.QVCS_TRUNK_VIEW;
    }

    /**
     * This is just a placeholder method to satisfy the interface.
     * @return N/A. Always returns 0L;
     */
    @Override
    public long getOldestRevision() {
        return 0L;
    }

    /**
     * This is just a placeholder method to satisfy the interface.
     * @param response N/A
     */
    @Override
    public void addLogFileListener(ServerResponseFactoryInterface response) {
    }

    /**
     * This is just a placeholder method to satisfy the interface.
     * @param response N/A
     */
    @Override
    public void removeLogFileListener(ServerResponseFactoryInterface response) {
    }

    @Override
    public int getDirectoryID() {
        return -1;
    }

    @Override
    public boolean moveArchive(String user, String shortWorkfileName, final ArchiveDirManagerInterface targetArchiveDirManager,
                                                                          ServerResponseFactoryInterface response) throws IOException, QVCSException {
        return false;
    }

    @Override
    public boolean deleteArchive(String user, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        return false;
    }

    @Override
    public boolean unDeleteArchive(String user, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        return false;
    }

    @Override
    public Date getMostRecentActivityDate() {
        return this.mostRecentCheckInDate;
    }
}
