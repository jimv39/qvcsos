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
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.LogfileListenerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import com.qumasoft.qvcslib.logfileaction.MoveFile;
import com.qumasoft.qvcslib.notifications.ServerNotificationInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteBranchInterface;

/**
 * Archive directory manager for a translucent branch's cemetery.
 *
 * @author Jim Voris
 */
public class ArchiveDirManagerForTranslucentBranchCemetery implements ArchiveDirManagerInterface, ArchiveDirManagerReadWriteBranchInterface, LogfileListenerInterface {
    // Create our logger object.
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManagerForTranslucentBranchCemetery.class);
    /**
     * The project name
     */
    private final String projectName;
    /**
     * The branch name
     */
    private final String branchName;
    /**
     * Keep track of oldest revision for this manager.
     */
    private long oldestRevision;
    private final RemoteBranchProperties remoteViewProperties;
    /**
     * The collection of archive info objects for this cemetery
     */
    private final Map<String, ArchiveInfoInterface> archiveInfoMap = Collections.synchronizedMap(new TreeMap<String, ArchiveInfoInterface>());
    // Remote listeners for changes to this directory.
    private final ArrayList<ServerResponseFactoryInterface> logfileListeners;

    /**
     * Constructor that takes the project name, and the branch name.
     *
     * @param project the name of the project.
     * @param branch the name of the branch.
     * @param rvProperties the project's properties.
     * @param response the response object.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    public ArchiveDirManagerForTranslucentBranchCemetery(String project, String branch, RemoteBranchProperties rvProperties,
                                                         ServerResponseFactoryInterface response) throws IOException, QVCSException {
        this.oldestRevision = Long.MAX_VALUE;
        this.logfileListeners = new ArrayList<>();
        this.projectName = project;
        this.branchName = branch;
        this.remoteViewProperties = rvProperties;

        // Get the directory contents object for this cemetery.
        DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(project);
        DirectoryContents cemeteryContents = directoryContentsManager.getDirectoryContentsForTranslucentBranchCemetery(branch, response);

        // 2. populate our archive info collection based on the directory contents object.
        Map<Integer, String> files = cemeteryContents.getFiles();
        Set<Integer> fileIdSet = cemeteryContents.getFiles().keySet();
        for (Integer fileId : fileIdSet) {
            // Use the file id to lookup the archive that we can use to create the branch's archive info object.
            int fileID = fileId;
            FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH, fileID);
            int directoryIDForFile = fileIDInfo.getDirectoryID();
            String filenameForBranch = files.get(fileID);

            // Lookup the archiveDirManager for the file's current location...
            ArchiveDirManager archiveDirManager = DirectoryIDDictionary.getInstance().lookupArchiveDirManager(getProjectName(), directoryIDForFile, response);

            String keyToFile = fileIDInfo.getShortFilename();
            boolean ignoreCaseFlag = archiveDirManager.getProjectProperties().getIgnoreCaseFlag();
            if (ignoreCaseFlag) {
                keyToFile = keyToFile.toLowerCase();
            }

            // Get the file's current archiveInfo...
            LogFile archiveInfo = (LogFile) archiveDirManager.getArchiveInfo(keyToFile);

            // Create the translucent branch archiveInfo.
            ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch = new ArchiveInfoForTranslucentBranch(filenameForBranch, archiveInfo, rvProperties);
            archiveInfo.addListener(archiveInfoForTranslucentBranch);

            String keyToOurFile = filenameForBranch;
            if (rvProperties.getIgnoreCaseFlag()) {
                keyToOurFile = keyToOurFile.toLowerCase();
            }

            // And store in our map...
            archiveInfoMap.put(keyToOurFile, archiveInfoForTranslucentBranch);

            // Save the timestamp of the oldest revision in this archiveInfo.
            setOldestRevision(archiveInfoForTranslucentBranch.getRevisionInformation().getRevisionHeader(archiveInfoForTranslucentBranch.getRevisionCount() - 1)
                    .getCheckInDate().getTime());

            // And listen for changes to the info object (which itself listens
            // for changes to the LogFile from which it is built).
            archiveInfoForTranslucentBranch.addListener(this);

            LOGGER.trace("Adding file id: [" + fileID + "] filename: [" + filenameForBranch + "]");
        }

        // Add the logfile listener for the cemetery.
        addLogFileListener(response);
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
        return QVCSConstants.QVCS_CEMETERY_DIRECTORY;
    }

    @Override
    public final String getProjectName() {
        return projectName;
    }

    @Override
    public final String getBranchName() {
        return branchName;
    }

    @Override
    public String getUserName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractProjectProperties getProjectProperties() {
        return remoteViewProperties;
    }

    @Override
    public ArchiveInfoInterface getArchiveInfo(String shortWorkfileName) {
        if (getProjectProperties().getIgnoreCaseFlag()) {
            shortWorkfileName = shortWorkfileName.toLowerCase();
        }
        return archiveInfoMap.get(shortWorkfileName);
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
    public boolean moveArchive(String userName, String shortWorkfileName, ArchiveDirManagerInterface targetArchiveDirManager, ServerResponseFactoryInterface response) throws
            IOException, QVCSException {
        boolean retVal = false;

        // Make sure the target directory manager is of the correct type.
        if (!(targetArchiveDirManager instanceof ArchiveDirManagerForFeatureBranch)) {
            String errorMessage = "#### INTERNAL ERROR: Attempt to move a file on a translucent branch to wrong type of target directory manager.";
            LOGGER.warn(errorMessage);
            throw new QVCSException(errorMessage);
        }

        // We need to synchronize on the target's class object -- only one move at at time
        // is allowed on the whole server. We need to do this to avoid a possible
        // deadlock situation that would occur if user A moved a file from directory
        // A to directory B at the same time as user B moving a file from
        // directory B to directory A.
        synchronized (ArchiveDirManagerForFeatureBranch.class) {
            String containerKeyValue = shortWorkfileName;
            if (getProjectProperties().getIgnoreCaseFlag()) {
                containerKeyValue = shortWorkfileName.toLowerCase();
            }

            int fileID = getArchiveInfo(shortWorkfileName).getFileID();

            // Verify that the move is allowed.
            verifyMoveIsAllowed(shortWorkfileName, targetArchiveDirManager);

            // Create the new revision in the archive file that documents the move. This new revision must be
            // on the file branch associated with this translucent branch.
            ArchiveInfoForTranslucentBranch translucentBrancharchiveInfo = (ArchiveInfoForTranslucentBranch) getArchiveInfo(shortWorkfileName);
            Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);
            if (translucentBrancharchiveInfo.moveArchive(userName, getAppendedPath(), targetArchiveDirManager, shortWorkfileName, date)) {
                // Remove the archive info from our collection.
                ArchiveInfoInterface archiveInfo = getArchiveInfoCollection().remove(containerKeyValue);
                ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch = (ArchiveInfoForTranslucentBranch) archiveInfo;
                archiveInfoForTranslucentBranch.removeListener(this);

                // Add it to the target directory's collection...
                targetArchiveDirManager.getArchiveInfoCollection().put(containerKeyValue, archiveInfo);
                ArchiveDirManagerForFeatureBranch targetDirManager = (ArchiveDirManagerForFeatureBranch) targetArchiveDirManager;
                archiveInfoForTranslucentBranch.addListener(targetDirManager);

                // Capture the change to the directory contents...
                DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName());
                directoryContentsManager.moveFileFromTranslucentBranchCemetery(getBranchName(), targetArchiveDirManager.getDirectoryID(), fileID, shortWorkfileName, response);

                // Notify the clients of the move.
                MoveFile logfileActionMoveFile = new MoveFile(getAppendedPath(), targetDirManager.getAppendedPath());
                notifyLogfileListener(archiveInfoForTranslucentBranch, logfileActionMoveFile);

                retVal = true;
            }
        }
        return retVal;
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
        UnDeleteArchiveForTranslucentBranchOperation unDeleteArchiveOperation = new UnDeleteArchiveForTranslucentBranchOperation(this, userName, shortWorkfileName, response);
        return unDeleteArchiveOperation.execute();
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

    /**
     * Get the collection of archive info objects for this branch cemetery.
     *
     * @return the collection of archive info objects for this branch cemetery.
     */
    @Override
    public Map<String, ArchiveInfoInterface> getArchiveInfoCollection() {
        return archiveInfoMap;
    }

    @Override
    public long getOldestRevision() {
        return oldestRevision;
    }

    private void setOldestRevision(long revisionCheckInTime) {
        if (revisionCheckInTime < oldestRevision) {
            oldestRevision = revisionCheckInTime;
        }
    }

    @Override
    public int getDirectoryID() {
        return -1;
    }

    @Override
    public final void addLogFileListener(ServerResponseFactoryInterface logfileListener) {
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

    @Override
    public void notifyLogfileListener(ArchiveInfoInterface subject, ActionType action) {
        // Build the information we need to send to the listeners.
        ServerNotificationInterface info = buildLogfileNotification(subject, action);

        // Let any remote users know about the logfile change.
        if (info != null) {
            synchronized (logfileListeners) {
                Iterator<ServerResponseFactoryInterface> it = logfileListeners.iterator();
                while (it.hasNext()) {
                    // Get who we'll send the information to.
                    ServerResponseFactoryInterface serverResponseFactory = it.next();

                    // Set the server name on the notification message.
                    info.setServerName(serverResponseFactory.getServerName());

                    // And send the info.
                    serverResponseFactory.createServerResponse(info);
                }
            }
        }
    }

    /**
     * Verify that a file move is allowed.
     *
     * @param userName the user name.
     * @param shortWorkfileName the short workfile name.
     * @param targetArchiveDirManager the destination directory.
     * @param response response object identifying the client.
     * @throws com.qumasoft.qvcslib.QVCSException if the move is not allowed.
     */
    private void verifyMoveIsAllowed(String shortWorkfileName, ArchiveDirManagerInterface targetArchiveDirManager) throws QVCSException {
        // Make sure the file does not already exist in the destination directory.
        if (null != targetArchiveDirManager.getArchiveInfo(shortWorkfileName)) {
            throw new QVCSException("Cannot move file to " + targetArchiveDirManager.getAppendedPath() + " directory. File " + shortWorkfileName + " already exists.");
        }

        // Make sure there is an archive file.
        if (null == getArchiveInfo(shortWorkfileName)) {
            throw new QVCSException("Archive not found for '" + shortWorkfileName + "'. Archive file cannot be moved since archive does not exist.");
        }
    }

    private ServerNotificationInterface buildLogfileNotification(ArchiveInfoInterface subject, ActionType action) {
        return ArchiveDirManagerHelper.buildLogfileNotification(this, subject, action);
    }
}
