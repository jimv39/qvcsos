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
package com.qumasoft.server;

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.LogfileListenerInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.swing.event.ChangeListener;

/**
 * Archive directory manager for an opaque branch.
 * TODO -- this remain un-implemented.
 * @author Jim Voris
 */
public class ArchiveDirManagerForOpaqueBranch implements ArchiveDirManagerInterface, ArchiveDirManagerReadOnlyViewInterface, LogfileListenerInterface {

    ArchiveDirManagerForOpaqueBranch(String branchParent, RemoteBranchProperties remoteViewProperties, String viewName, String localAppendedPath, String userName,
            ServerResponseFactoryInterface response) {
    }

    /**
     * This is not used on the server.
     * @return null, since this is not used on the server.
     */
    @Override
    public Date getMostRecentActivityDate() {
        return null;
    }

    @Override
    public void setDirectoryManager(DirectoryManagerInterface directoryManager) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAppendedPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getProjectName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getBranchName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUserName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractProjectProperties getProjectProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArchiveInfoInterface getArchiveInfo(String shortWorkfileName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, String fullWorkfilename, ServerResponseFactoryInterface response) throws IOException,
            QVCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile, byte[] buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean moveArchive(String userName, String shortWorkfileName, ArchiveDirManagerInterface targetArchiveDirManager, ServerResponseFactoryInterface response)
            throws IOException, QVCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean renameArchive(String userName, String oldShortWorkfileName, String newShortWorkfileName, ServerResponseFactoryInterface response) throws IOException,
            QVCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean deleteArchive(String userName, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean unDeleteArchive(String userName, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean createDirectory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startDirectoryManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void notifyListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFastNotify(boolean flag) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getFastNotify() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, ArchiveInfoInterface> getArchiveInfoCollection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getOldestRevision() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getDirectoryID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addLogFileListener(ServerResponseFactoryInterface response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeLogFileListener(ServerResponseFactoryInterface response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void notifyLogfileListener(ArchiveInfoInterface subject, ActionType action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
