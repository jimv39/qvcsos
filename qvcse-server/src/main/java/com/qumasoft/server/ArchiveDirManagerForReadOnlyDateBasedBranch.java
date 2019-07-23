/*   Copyright 2004-2019 Jim Voris
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
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyBranchInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive directory manager for read-only date based branch.
 *
 * @author Jim Voris
 */
public final class ArchiveDirManagerForReadOnlyDateBasedBranch implements ArchiveDirManagerInterface, ArchiveDirManagerReadOnlyBranchInterface {
    // Create our logger object.
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManagerForReadOnlyDateBasedBranch.class);
    /**
     * The oldest revision for this type of branch can just be today.
     */
    private static final Date SPOOF_OLDEST_REVISION = new Date();
    private final Date anchorDate;
    private final String branchName;
    private final String projectName;
    private final String appendedPath;
    private final String userName;
    private final RemoteBranchProperties remoteBranchProperties;
    private final Map<String, ArchiveInfoInterface> archiveInfoMap;
    /**
     * Remote listeners for changes to this directory.
     */
    private final ArrayList<ServerResponseFactoryInterface> logfileListeners = new ArrayList<>();

    /**
     * Creates a new instance of ArchiveDirManagerForReadOnlyDateBasedBranch.
     *
     * @param anchrDate the anchor date for this date-based branch.
     * @param rbProperties the branch's properties.
     * @param branch the name of the branch.
     * @param path the appended path for this directory.
     * @param user the user name
     * @param response object to identify the client.
     */
    public ArchiveDirManagerForReadOnlyDateBasedBranch(Date anchrDate, RemoteBranchProperties rbProperties, String branch, String path, String user,
            ServerResponseFactoryInterface response) {
        this.archiveInfoMap = Collections.synchronizedMap(new TreeMap<>());
        this.anchorDate = anchrDate;
        this.branchName = branch;
        this.appendedPath = path;
        this.remoteBranchProperties = rbProperties;
        this.userName = user;
        this.projectName = rbProperties.getProjectName();
        populateCollection(response);
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
        // We don't need to do anything.
    }

    @Override
    public String getAppendedPath() {
        return appendedPath;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getBranchName() {
        return branchName;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * Get the anchor date for this branch.
     * @return the anchor date for this branch.
     */
    public Date getAnchorDate() {
        return anchorDate;
    }

    @Override
    public AbstractProjectProperties getProjectProperties() {
        return remoteBranchProperties;
    }

    private RemoteBranchProperties getRemoteBranchProperties() {
        return remoteBranchProperties;
    }

    @Override
    public ArchiveInfoInterface getArchiveInfo(String shortWorkfileName) {
        return archiveInfoMap.get(Utility.getArchiveKey(getProjectProperties(), shortWorkfileName));
    }

    @Override
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, String fullWorkfilename, ServerResponseFactoryInterface response) throws IOException,
            QVCSException {
        return false;
    }

    @Override
    public void createReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile, byte[] buffer) {
        // We don't need to do anything.
    }

    @Override
    public void deleteReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile) {
        // We don't need to do anything.
    }

    @Override
    public boolean createDirectory() {
        return false;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        throw new UnsupportedOperationException("Not supported on the server!");
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        throw new UnsupportedOperationException("Not supported on the server!");
    }

    @Override
    public void startDirectoryManager() {
        // We don't need to do anything.
    }

    @Override
    public void notifyListeners() {
        throw new UnsupportedOperationException("Not supported on the server!");
    }

    @Override
    public void setFastNotify(boolean flag) {
    }

    @Override
    public boolean getFastNotify() {
        return false;
    }

    @Override
    public boolean renameArchive(String user, String oldShortWorkfileName, String newShortWorkfileName, ServerResponseFactoryInterface response) throws IOException,
            QVCSException {
        return false;
    }

    @Override
    public Map<String, ArchiveInfoInterface> getArchiveInfoCollection() {
        return archiveInfoMap;
    }

    @Override
    public long getOldestRevision() {
        return SPOOF_OLDEST_REVISION.getTime();
    }

    @Override
    public int getDirectoryID() {
        // This is NOT a real directory!!
        return -1;
    }

    @Override
    public void addLogFileListener(ServerResponseFactoryInterface logfileListener) {
        synchronized (logfileListeners) {
            logfileListeners.add(logfileListener);
        }
    }

    @Override
    public void removeLogFileListener(ServerResponseFactoryInterface logfileListener) {
        synchronized (logfileListeners) {
            logfileListeners.remove(logfileListener);
        }
    }

    private void populateCollection(ServerResponseFactoryInterface response) {
        // TODO -- I'm not sure that I have to 'navigate' from the root to the requested directory -- it may be as simple as going to the database to find what files
        // were in the given directory at the given time.... The navigation might be required if the parent branch for this branch is not the trunk.
        try {
            String[] segments = Utility.convertToStandardPath(getAppendedPath()).split("/");

            // Work our way up from the root of the project until we get to this
            // directory...
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH, "");
            ArchiveDirManagerInterface projectRootArchiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, getUserName(), response);
            int projectRootDirectoryID = projectRootArchiveDirManager.getDirectoryID();
            ProjectBranch projectBranch = BranchManager.getInstance().getBranch(getProjectName(), getBranchName());
            DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName());
            DirectoryContents projectRootDirectoryContents = directoryContentsManager.getDirectoryContentsForDateBasedBranch(projectBranch, "", projectRootDirectoryID, response);

            // 'Navigate' to the current 'directory' so we can get its contents.
            int segmentIndex;
            if (getAppendedPath().length() > 0) {
                segmentIndex = 0;
            } else {
                segmentIndex = 1;
            }
            DirectoryContents directoryContents = projectRootDirectoryContents;
            while (segmentIndex < segments.length) {
                // Look through the child directories for the one that matches
                // this directory segment...
                Map<Integer, String> childDirectories = directoryContents.getChildDirectories();
                Iterator<Map.Entry<Integer, String>> entrySetIt = childDirectories.entrySet().iterator();
                while (entrySetIt.hasNext()) {
                    Map.Entry<Integer, String> directoryEntry = entrySetIt.next();
                    String directoryName = directoryEntry.getValue();
                    if (0 == directoryName.compareTo(segments[segmentIndex])) {
                        DirectoryContents childDirectoryContents = directoryContentsManager.getDirectoryContentsForDateBasedBranch(projectBranch, getAppendedPath(),
                                directoryEntry.getKey(), response);
                        if (childDirectoryContents != null) {
                            childDirectoryContents.setParentDirectoryID(directoryContents.getDirectoryID());
                            directoryContents = childDirectoryContents;
                            LOGGER.info("Found directory contents for: [{}]", getAppendedPath());
                        }
                        break;
                    }
                }
                segmentIndex++;
            }

            boolean ignoreOurCaseFlag = getProjectProperties().getIgnoreCaseFlag();

            // Ok. We have 'navigated' to the requested directory. Now we need
            // to build the collection of archiveInfoInterface objects to represent
            // the files present in the directory for the given branch.
            Map<Integer, String> files = directoryContents.getFiles();

            // Now, iterate over the directory contents files, and create the
            // objects that populate this object's container.
            Iterator<Integer> it = files.keySet().iterator();
            while (it.hasNext()) {
                int fileID = it.next();
                FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH, fileID);
                int directoryID = fileIDInfo.getDirectoryID();
                String filenameForBranch = files.get(fileID);

                // Lookup the archiveDirManager for the file's current location...
                ArchiveDirManager archiveDirManager = DirectoryIDDictionary.getInstance().lookupArchiveDirManager(getProjectName(), directoryID, response);

                String keyToFile = Utility.getArchiveKey(archiveDirManager.getProjectProperties(), fileIDInfo.getShortFilename());

                // Get the file's current archiveInfo...
                LogFile archiveInfo = (LogFile) archiveDirManager.getArchiveInfo(keyToFile);

                if (archiveInfo != null) {
                    // Create the read-only date-based branch of that archiveInfo on the
                    // trunk or on the floating branch.
                    String parentBranchName = getRemoteBranchProperties().getBranchParent();
                    // If the parent branch is the trunk...
                    if (0 == parentBranchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                        ArchiveInfoForReadOnlyDateBasedBranch archiveInfoForReadOnlyDateBasedBranch = new ArchiveInfoForReadOnlyDateBasedBranch(filenameForBranch, archiveInfo,
                                getRemoteBranchProperties());

                        if (archiveInfoForReadOnlyDateBasedBranch.getLogfileInfo() != null) {
                            String keyToOurFile = filenameForBranch;
                            if (ignoreOurCaseFlag) {
                                keyToOurFile = keyToOurFile.toLowerCase();
                            }

                            // And store that read-only date-based branch in our map...
                            archiveInfoMap.put(keyToOurFile, archiveInfoForReadOnlyDateBasedBranch);

                            LOGGER.info("Adding file id: [{}] filename: [{}]", fileID, filenameForBranch);
                        } else {
                            Object[] logArgs = {fileID, filenameForBranch, getRemoteBranchProperties().getDateBasedDate()};
                            LOGGER.info("Skipping file id: [{}] filename: [{}]  as no revisions were created after [{}]", logArgs);
                        }
                    } else {
                        LOGGER.info("Only Trunk is currently supported as parent branch for read-only branches. Skipping: [{}] for branch: [{}]", filenameForBranch,
                                parentBranchName);
                    }
                } else {
                    LOGGER.warn("Internal error: Archive not found for [" + fileIDInfo.getShortFilename() + "] for directory ID ["
                            + archiveDirManager.getDirectoryID() + "] "
                            + "and appended path of [" + archiveDirManager.getAppendedPath() + "].");
                }
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public boolean moveArchive(String user, String shortWorkfileName, final ArchiveDirManagerInterface targetArchiveDirManager, ServerResponseFactoryInterface response)
            throws IOException, QVCSException {
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
}
