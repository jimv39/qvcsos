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
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteBranchInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.LogfileListenerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import com.qumasoft.qvcslib.logfileaction.Create;
import com.qumasoft.qvcslib.logfileaction.MoveFile;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.logfileaction.Rename;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckIn;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckOut;
import com.qumasoft.qvcslib.notifications.ServerNotificationCreateArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationHeaderChange;
import com.qumasoft.qvcslib.notifications.ServerNotificationInterface;
import com.qumasoft.qvcslib.notifications.ServerNotificationLock;
import com.qumasoft.qvcslib.notifications.ServerNotificationMoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRenameArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationSetRevisionDescription;
import com.qumasoft.qvcslib.notifications.ServerNotificationUnlock;
import java.io.IOException;
import java.sql.SQLException;
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
 * Archive directory manager for a feature branch directory.
 *
 * @author Jim Voris
 */
public class ArchiveDirManagerForFeatureBranch implements ArchiveDirManagerInterface, ArchiveDirManagerReadWriteBranchInterface, LogfileListenerInterface {
    // Create our logger object.
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManagerForFeatureBranch.class);
    private final String branchName;
    private final String projectName;
    private final String appendedPath;
    private final String userName;
    private final String branchParent;
    private int directoryID = -1;
    private final ProjectBranch projectBranch;
    private final String branchLabel;
    private final RemoteBranchProperties remoteBranchProperties;
    private final Map<String, ArchiveInfoInterface> archiveInfoMap = Collections.synchronizedMap(new TreeMap<>());
    /**
     * Keep track of oldest revision for this manager.
     */
    private long oldestRevision = Long.MAX_VALUE;
    // Remote listeners for changes to this directory.
    private final ArrayList<ServerResponseFactoryInterface> logfileListeners;

    /**
     * Constructor.
     *
     * @param bParent the name of this branch's parent branch (typically the Trunk, but other parents are supported, e.g. other
     * branches can serve as the parent to this branch).
     * @param rbProperties the remote branch properties.
     * @param branch the name of this branch.
     * @param path the appended path for this directory.
     * @param user the user name.
     * @param response identifies the client.
     */
    public ArchiveDirManagerForFeatureBranch(String bParent, RemoteBranchProperties rbProperties, String branch, String path, String user,
            ServerResponseFactoryInterface response) {
        this.logfileListeners = new ArrayList<>();
        this.branchParent = bParent;
        this.branchName = branch;
        this.appendedPath = path;
        this.remoteBranchProperties = rbProperties;
        this.userName = user;
        this.projectName = rbProperties.getProjectName();
        this.projectBranch = BranchManager.getInstance().getBranch(projectName, branchName);
        this.branchLabel = projectBranch.getFeatureBranchLabel();
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

    /**
     * Set the directory manager. This is only used on the client.
     *
     * @param directoryManager the directory manager.
     */
    @Override
    public void setDirectoryManager(DirectoryManagerInterface directoryManager) {
        // We don't need to do anything.
    }

    /**
     * Get the appended path.
     *
     * @return the appended path.
     */
    @Override
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    @Override
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the branch name.
     *
     * @return the branch name.
     */
    @Override
    public String getBranchName() {
        return branchName;
    }

    /**
     * Get the user name.
     *
     * @return the user name.
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * Get the name of this branch's parent branch.
     *
     * @return the name of this branch's parent branch.
     */
    public String getBranchParent() {
        return branchParent;
    }

    /**
     * Get the branch label associated with this feature branch.
     *
     * @return the branch label associated with this feature branch.
     */
    public String getBranchLabel() {
        return branchLabel;
    }

    /**
     * Get the project properties.
     *
     * @return the project properties.
     */
    @Override
    public AbstractProjectProperties getProjectProperties() {
        return remoteBranchProperties;
    }

    private ServerNotificationInterface buildLogfileNotification(ArchiveInfoInterface subject, ActionType action) {
        ServerNotificationInterface serverNotification = ArchiveDirManagerHelper.buildLogfileNotification(this, subject, action);
        if (subject instanceof LogFile) {
            // We need to turn off lock checking in the attributes.
            switch (action.getAction()) {
                case ActionType.CHECKOUT:
                    ServerNotificationCheckOut serverNotificationCheckOut = (ServerNotificationCheckOut) serverNotification;
                    serverNotificationCheckOut.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
                case ActionType.CHECKIN:
                    ServerNotificationCheckIn serverNotificationCheckIn = (ServerNotificationCheckIn) serverNotification;
                    serverNotificationCheckIn.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
                case ActionType.LOCK:
                    ServerNotificationLock serverNotificationLock = (ServerNotificationLock) serverNotification;
                    serverNotificationLock.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
                case ActionType.CREATE:
                    ServerNotificationCreateArchive serverNotificationCreateArchive = (ServerNotificationCreateArchive) serverNotification;
                    serverNotificationCreateArchive.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
                case ActionType.MOVE_FILE:
                    ServerNotificationMoveArchive serverNotificationMoveArchive = (ServerNotificationMoveArchive) serverNotification;
                    serverNotificationMoveArchive.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
                case ActionType.UNLOCK:
                    ServerNotificationUnlock serverNotificationUnlock = (ServerNotificationUnlock) serverNotification;
                    serverNotificationUnlock.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
                case ActionType.SET_REVISION_DESCRIPTION:
                    ServerNotificationSetRevisionDescription serverNotificationSetRevisionDescription = (ServerNotificationSetRevisionDescription) serverNotification;
                    serverNotificationSetRevisionDescription.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
                case ActionType.REMOVE:
                    break;
                case ActionType.RENAME:
                    ServerNotificationRenameArchive serverNotificationRenameArchive = (ServerNotificationRenameArchive) serverNotification;
                    serverNotificationRenameArchive.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
                case ActionType.CHANGE_ON_BRANCH:
                    if (serverNotification instanceof ServerNotificationHeaderChange) {
                        ServerNotificationHeaderChange serverNotificationHeaderChange = (ServerNotificationHeaderChange) serverNotification;
                        serverNotificationHeaderChange.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    }
                    break;
                case ActionType.SET_OBSOLETE:
                case ActionType.LABEL:
                case ActionType.UNLABEL:
                case ActionType.CHANGE_HEADER:
                case ActionType.CHANGE_REVHEADER:
                case ActionType.SET_ATTRIBUTES:
                case ActionType.SET_COMMENT_PREFIX:
                case ActionType.SET_MODULE_DESCRIPTION:
                default:
                    ServerNotificationHeaderChange serverNotificationHeaderChange = (ServerNotificationHeaderChange) serverNotification;
                    serverNotificationHeaderChange.getSkinnyLogfileInfo().getAttributes().setIsCheckLock(false);
                    break;
            }
        }
        return serverNotification;
    }

    private RemoteBranchProperties getRemoteBranchProperties() {
        return remoteBranchProperties;
    }

    /**
     * Get the archive info for the given short workfile name.
     *
     * @param shortWorkfileName the name of the workfile to lookup.
     * @return the archive info (on this branch) for the given workfile, or null if the file was not found.
     */
    @Override
    public ArchiveInfoInterface getArchiveInfo(String shortWorkfileName) {
        if (getProjectProperties().getIgnoreCaseFlag()) {
            shortWorkfileName = shortWorkfileName.toLowerCase();
        }
        return archiveInfoMap.get(shortWorkfileName);
    }

    /**
     * Create an archive file for this branched directory. We need to create the archive file in the branch archives directory
     * instead of on the trunk, since the file doesn't exist on the trunk.
     *
     * @param commandLineArgs the command line args to create the file.
     * @param inputFileName the input file name.
     * @param response where the create request came from.
     * @return true if we created the archive successfully.
     * @throws java.io.IOException if there are IO problems.
     * @throws com.qumasoft.qvcslib.QVCSException if there was a QVCS problem.
     */
    @Override
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, String inputFileName, ServerResponseFactoryInterface response)
            throws IOException, QVCSException {
        String shortWorkfileName = Utility.convertWorkfileNameToShortWorkfileName(commandLineArgs.getWorkfileName());
        verifyCreateIsAllowed(shortWorkfileName);

        boolean retVal = false;

        ArchiveDirManagerInterface branchArchiveDirManagerInterface = ServerUtility.getBranchArchiveDirManager(getProjectName(), response);
        ArchiveDirManager branchArchiveDirManager = (ArchiveDirManager) branchArchiveDirManagerInterface;
        if (!branchArchiveDirManager.directoryExists()) {
            if (!branchArchiveDirManager.createDirectory()) {
                LOGGER.warn("Failed to create archive directory for branch archive appended path: [{}]", branchArchiveDirManager.getAppendedPath());
                return false;
            }
        }

        int fileID;
        String branchArchiveDirectoryShortWorkfileName;
        boolean createSuccessFlag;

        // We have to synchronize on the FileIDManager so that the fileID we get will be correct. Absent this synchronization block, another thread could
        // come along after we figure out what the next fileID will be and grab that next fileID, making our notion of the fileID incorrect.
        synchronized (FileIDManager.getInstance()) {
            fileID = 1 + FileIDManager.getInstance().getCurrentMaximumFileID();
            String branchArchiveDirectoryShortArchiveFileName = Utility.createBranchShortArchiveName(1 + FileIDManager.getInstance().getCurrentMaximumFileID());
            branchArchiveDirectoryShortWorkfileName = Utility.convertArchiveNameToShortWorkfileName(branchArchiveDirectoryShortArchiveFileName);
            commandLineArgs.setWorkfileName(branchArchiveDirectoryShortWorkfileName);
            createSuccessFlag = branchArchiveDirManager.createArchive(commandLineArgs, inputFileName, response);
        }
        if (createSuccessFlag) {
            // The archive has been created in the branch archive directory with a filename based on the fileID.
            // We need to create an entry in the feature branch's DirectoryContents object to point to that
            // archive file, and we need to populate our own collection with info based on the actual
            // archive file, etc.

            String keyToFile = branchArchiveDirectoryShortWorkfileName;
            boolean ignoreCaseFlag = branchArchiveDirManager.getProjectProperties().getIgnoreCaseFlag();
            if (ignoreCaseFlag) {
                keyToFile = keyToFile.toLowerCase();
            }

            // Get the file's current archiveInfo...
            LogFile archiveInfo = (LogFile) branchArchiveDirManager.getArchiveInfo(keyToFile);

            // Create the feature branch archiveInfo.
            ArchiveInfoForFeatureBranch archiveInfoForFeatureBranch = new ArchiveInfoForFeatureBranch(shortWorkfileName, archiveInfo, getRemoteBranchProperties());
            archiveInfo.addListener(archiveInfoForFeatureBranch);
            archiveInfoForFeatureBranch.capturePromotionCandidate();

            String keyToOurFile = shortWorkfileName;
            boolean projectIgnoreCaseFlag = getProjectProperties().getIgnoreCaseFlag();
            if (projectIgnoreCaseFlag) {
                keyToOurFile = keyToOurFile.toLowerCase();
            }

            // And store in our map...
            synchronized (archiveInfoMap) {
                archiveInfoMap.put(keyToOurFile, archiveInfoForFeatureBranch);
            }

            // Capture the change to the directory contents...
            try {
                DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName()).addFileToFeatureBranch(getBranchName(), getDirectoryID(), fileID,
                        shortWorkfileName, response);
            } catch (SQLException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                throw new QVCSException("Caught SQLException: " + e.getLocalizedMessage());
            }

            // Capture the association of this file to this directory.
            FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getBranchName(), fileID, getAppendedPath(), shortWorkfileName, getDirectoryID());

            // Listen for changes to the info object (which itself listens
            // for changes to the LogFile from which it is built).
            archiveInfoForFeatureBranch.addListener(this);

            // Notify the clients of the move.
            Create logfileActionCreate = new Create(commandLineArgs);
            notifyLogfileListener(archiveInfoForFeatureBranch, logfileActionCreate);

            retVal = true;
        }
        return retVal;
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
    public boolean moveArchive(String user, String shortWorkfileName, ArchiveDirManagerInterface targetArchiveDirManager, ServerResponseFactoryInterface response)
            throws IOException, QVCSException {
        boolean retVal = false;

        // Make sure the target directory manager is of the correct type.
        if (!(targetArchiveDirManager instanceof ArchiveDirManagerForFeatureBranch)) {
            String errorMessage = "#### INTERNAL ERROR: Attempt to move a file on a feature branch to wrong type of target directory manager.";
            LOGGER.warn(errorMessage);
            throw new QVCSException(errorMessage);
        }

        // We need to synchronize on the class object -- only one move at at time
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
            // on the file branch associated with this feature branch.
            ArchiveInfoForFeatureBranch featureBranchArchiveInfo = (ArchiveInfoForFeatureBranch) getArchiveInfo(shortWorkfileName);
            Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);
            if (featureBranchArchiveInfo.moveArchive(user, getAppendedPath(), targetArchiveDirManager, shortWorkfileName, date)) {
                // Remove the archive info from our collection.
                ArchiveInfoInterface archiveInfo = getArchiveInfoCollection().remove(containerKeyValue);
                ArchiveInfoForFeatureBranch archiveInfoForFeatureBranch = (ArchiveInfoForFeatureBranch) archiveInfo;
                archiveInfoForFeatureBranch.removeListener(this);

                // Add it to the target directory's collection...
                targetArchiveDirManager.getArchiveInfoCollection().put(containerKeyValue, archiveInfo);
                ArchiveDirManagerForFeatureBranch targetDirManager = (ArchiveDirManagerForFeatureBranch) targetArchiveDirManager;
                archiveInfoForFeatureBranch.addListener(targetDirManager);

                // Capture the change to the directory contents...
                DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName());
                directoryContentsManager.moveFileOnFeatureBranch(getBranchName(), getDirectoryID(), targetArchiveDirManager.getDirectoryID(), fileID, response);

                // Capture the change in association of this file to this directory.
                FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getBranchName(), fileID, targetArchiveDirManager.getAppendedPath(), shortWorkfileName,
                        targetArchiveDirManager.getDirectoryID());

                // Notify the clients of the move.
                MoveFile logfileActionMoveFile = new MoveFile(getAppendedPath(), targetDirManager.getAppendedPath());
                notifyLogfileListener(archiveInfoForFeatureBranch, logfileActionMoveFile);

                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public boolean renameArchive(String user, String oldShortWorkfileName, String newShortWorkfileName, ServerResponseFactoryInterface response) throws IOException,
            QVCSException {
        boolean returnValue = false;
        String oldContainerKeyValue = oldShortWorkfileName;
        String newContainerKeyValue = newShortWorkfileName;
        if (getProjectProperties().getIgnoreCaseFlag()) {
            oldContainerKeyValue = oldShortWorkfileName.toLowerCase();
            newContainerKeyValue = newShortWorkfileName.toLowerCase();
        }

        // Step 1. Verify that the rename is allowed.
        verifyRenameIsAllowed(oldShortWorkfileName, newShortWorkfileName);

        // Only allow one rename per directory at a time.
        synchronized (this) {
            // Create the new revision in the archive file that documents the rename. This new revision must be
            // on the file branch associated with this feature branch.
            ArchiveInfoForFeatureBranch featureBranchArchiveInfo = (ArchiveInfoForFeatureBranch) getArchiveInfo(oldShortWorkfileName);
            Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);
            if (featureBranchArchiveInfo.renameArchive(user, getAppendedPath(), oldShortWorkfileName, newShortWorkfileName, date)) {
                // Remove the entry for the old name...
                getArchiveInfoCollection().remove(oldContainerKeyValue);

                // Add an entry for the new name...
                featureBranchArchiveInfo.setShortWorkfileName(newShortWorkfileName);
                getArchiveInfoCollection().put(newContainerKeyValue, featureBranchArchiveInfo);

                // Step 3. Update the DirectoryContents object to create a new revision there that has the new name.
                int fileID = getArchiveInfo(newShortWorkfileName).getFileID();
                DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName()).renameFileOnFeatureBranch(getBranchName(), fileID,
                        oldShortWorkfileName,
                        newShortWorkfileName, response);

                // Capture the change in association of this file to this directory.
                FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getBranchName(), fileID, getAppendedPath(), newShortWorkfileName, getDirectoryID());

                // Create a notification message to let everyone know about the 'new' file.
                Rename logfileActionRename = new Rename(oldShortWorkfileName);
                notifyLogfileListener(featureBranchArchiveInfo, logfileActionRename);
                returnValue = true;
            }
        }
        return returnValue;
    }

    @Override
    public boolean deleteArchive(String user, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        boolean retVal = false;

        // We need to synchronize on the class object -- only one delete at at time
        // is allowed on the whole server.
        synchronized (ArchiveDirManagerForFeatureBranch.class) {
            String containerKeyValue = shortWorkfileName;
            if (getProjectProperties().getIgnoreCaseFlag()) {
                containerKeyValue = shortWorkfileName.toLowerCase();
            }

            int fileID = getArchiveInfo(shortWorkfileName).getFileID();

            // Create the new revision in the archive file that documents the delete. This new revision must be
            // on the file branch associated with this feature branch.
            ArchiveInfoForFeatureBranch featureBranchArchiveInfo = (ArchiveInfoForFeatureBranch) getArchiveInfo(shortWorkfileName);
            Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);
            if (featureBranchArchiveInfo.deleteArchive(user, getAppendedPath(), shortWorkfileName, date)) {
                // Remove the archive info from our collection.
                ArchiveInfoInterface archiveInfo = getArchiveInfoCollection().remove(containerKeyValue);
                ArchiveInfoForFeatureBranch archiveInfoForFeatureBranch = (ArchiveInfoForFeatureBranch) archiveInfo;
                archiveInfoForFeatureBranch.removeListener(this);

                // Add it to the cemetery directory's collection...
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), QVCSConstants.QVCS_CEMETERY_DIRECTORY);
                ArchiveDirManagerInterface cemeteryDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                        directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
                String shortArchiveFilename = Utility.createCemeteryShortArchiveName(archiveInfo.getFileID());
                String cemeteryWorkfileName = Utility.convertArchiveNameToShortWorkfileName(shortArchiveFilename);
                String cemeteryKeyValue = cemeteryWorkfileName;
                if (getProjectProperties().getIgnoreCaseFlag()) {
                    cemeteryKeyValue = cemeteryKeyValue.toLowerCase();
                }

                // Notify the clients of the delete.
                Remove logfileActionRemove = new Remove();
                notifyLogfileListener(archiveInfoForFeatureBranch, logfileActionRemove);

                archiveInfoForFeatureBranch.setShortWorkfileName(cemeteryWorkfileName);
                cemeteryDirManager.getArchiveInfoCollection().put(cemeteryKeyValue, archiveInfoForFeatureBranch);
                ArchiveDirManagerForFeatureBranchCemetery targetCemeteryDirManager = (ArchiveDirManagerForFeatureBranchCemetery) cemeteryDirManager;
                archiveInfoForFeatureBranch.addListener(targetCemeteryDirManager);

                // Capture the change to the directory contents...
                DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName());
                directoryContentsManager.deleteFileFromFeatureBranch(getBranchName(), getDirectoryID(), -1, fileID, shortWorkfileName, response);

                // Capture the change in association of this file to this directory.
                FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getBranchName(), fileID, cemeteryDirManager.getAppendedPath(), cemeteryWorkfileName,
                        cemeteryDirManager.getDirectoryID());

                // Notify any cemetery listeners of the change.
                targetCemeteryDirManager.notifyLogfileListener(archiveInfoForFeatureBranch, new Create());

                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public boolean unDeleteArchive(String user, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        // We cannot undelete a file from a non-cemetery directory.
        return false;
    }

    /**
     * There is nothing to do here, since the archive files are really stored elsewhere.
     *
     * @return true indicating that creation of the archive directory was successful.
     */
    @Override
    public boolean createDirectory() {
        // TODO -- do we need to update the DirectoryContents for this directory to add the sub-directory?
        return true;
    }

    /**
     * Add change listener. Not supported on the server.
     *
     * @param listener the listener to add.
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        // Do not need to support this. It is used solely on the client side.
        throw new UnsupportedOperationException("Not supported on the server!");
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        // Do not need to support this. It is used solely on the client side.
        throw new UnsupportedOperationException("Not supported on the server!");
    }

    @Override
    public void startDirectoryManager() {
        // We don't need to do anything.
    }

    @Override
    public void notifyListeners() {
        // Do not need to support this. It is used solely on the client side.
        throw new UnsupportedOperationException("Not supported on the server!");
    }

    @Override
    public void setFastNotify(boolean flag) {
        // Do not need to support this. It is used solely on the client side.
    }

    @Override
    public boolean getFastNotify() {
        return false;
    }

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
        return directoryID;
    }

    void setDirectoryID(int dirID) {
        this.directoryID = dirID;
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

    /**
     * We receive notifications here from 2 sources. Most of the notifications here arrive from instances of
     * ArchiveInfoForFeatureBranch objects. The other source of notifications is when a new archive is created on the trunk, and
     * the trunk ArchiveDirManager sends a create notification here, passing the actual LogFile object as the subject. Note that we
     * are <i>not</i> listeners of LogFile objects here... the create notification is a special case.
     *
     * @param subject the object that has changed.
     * @param action the kind of change made to the object.
     */
    @Override
    public void notifyLogfileListener(ArchiveInfoInterface subject, ActionType action) {
        boolean continueFlag = true;

        // Handle create notifications...
        if (action.getAction() == ActionType.CREATE) {
            // We may have to just absorb the create notification...
            continueFlag = handleCreateNotification(subject);
        }

        if (continueFlag) {
            if (action.getAction() == ActionType.REMOVE) {
                handleRemoveNotification(subject);
            }

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
    }

    private boolean handleCreateNotification(ArchiveInfoInterface subject) {
        boolean continueFlag = true;
        if (subject instanceof LogFile) {
            LogFile logFile = (LogFile) subject;
            // Only pay attention to create notifications if the logfile is not branched for this feature branch.
            // This can happen in the case of a rename, or a file move, where the file is renamed or moved on the
            // trunk.... If that same file has already been branched on the feature branch, then we need to
            // ignore the create, since that same file is already represented elsewhere in the feature branch's
            // directory tree.
            if (!logFile.hasLabel(getBranchLabel())) {
                String filenameForBranch = logFile.getShortWorkfileName();

                // Create the feature branch archiveInfo.
                ArchiveInfoForFeatureBranch archiveInfoForFeatureBranch = new ArchiveInfoForFeatureBranch(filenameForBranch, logFile, getRemoteBranchProperties());
                logFile.addListener(archiveInfoForFeatureBranch);

                String keyToOurFile = filenameForBranch;
                boolean ignoreOurCaseFlag = getProjectProperties().getIgnoreCaseFlag();
                if (ignoreOurCaseFlag) {
                    keyToOurFile = keyToOurFile.toLowerCase();
                }

                // And store in our map...
                archiveInfoMap.put(keyToOurFile, archiveInfoForFeatureBranch);

                // And listen for changes to the info object (which itself listens
                // for changes to the LogFile from which it is built).
                archiveInfoForFeatureBranch.addListener(this);

                LOGGER.info("Adding file id: [{}] filename: [{}]", logFile.getFileID(), filenameForBranch);
            } else {
                continueFlag = false;
            }
        }
        return continueFlag;
    }

    private void handleRemoveNotification(ArchiveInfoInterface subject) {
        String filenameForBranch = subject.getShortWorkfileName();

        String keyToOurFile = filenameForBranch;
        boolean ignoreOurCaseFlag = getProjectProperties().getIgnoreCaseFlag();
        if (ignoreOurCaseFlag) {
            keyToOurFile = keyToOurFile.toLowerCase();
        }

        // And remove from our map...
        ArchiveInfoInterface archiveInfo = archiveInfoMap.remove(keyToOurFile);
        if (archiveInfo != null) {
            // And we don't need to listen to this anymore.
            ArchiveInfoForFeatureBranch listener = (ArchiveInfoForFeatureBranch) archiveInfo;
            listener.removeListener(this);
        }

        LOGGER.info("Removing from feature branch [" + getBranchName() + "] file id: [" + subject.getFileID() + "] filename: [" + filenameForBranch + "]");
    }

    private void populateCollection(ServerResponseFactoryInterface response) {
        try {
            String[] segments = Utility.convertToStandardPath(getAppendedPath()).split("/");

            // Work our way up from the root of the project until we get to this
            // directory...
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH, "");
            ArchiveDirManagerInterface projectRootArchiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, getUserName(), response);
            int projectRootDirectoryID = projectRootArchiveDirManager.getDirectoryID();

            ProjectBranch projBranch = BranchManager.getInstance().getBranch(getProjectName(), getBranchName());
            DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName());
            DirectoryContents projectRootDirectoryContents;

            // Get the root directory contents for this branch....
            projectRootDirectoryContents = directoryContentsManager.getDirectoryContentsForFeatureBranch(projBranch, "", projectRootDirectoryID, response);

            // 'Navigate' to the current 'directory' so we can get its contents.
            int segmentIndex;
            if (getAppendedPath().length() > 0) {
                segmentIndex = 0;
            } else {
                segmentIndex = 1;
            }
            DirectoryContents directoryContents = projectRootDirectoryContents;
            Integer dirID = projectRootDirectoryID;
            boolean foundFlag = false;
            while (segmentIndex < segments.length) {
                // Look through the child directories for the one that matches
                // this directory segment...
                Map<Integer, String> childDirectories = directoryContents.getChildDirectories();
                Iterator<Map.Entry<Integer, String>> entrySetIt = childDirectories.entrySet().iterator();
                while (entrySetIt.hasNext()) {
                    Map.Entry<Integer, String> directoryEntry = entrySetIt.next();
                    dirID = directoryEntry.getKey();
                    String directoryName = directoryEntry.getValue();
                    if (0 == directoryName.compareTo(segments[segmentIndex])) {
                        DirectoryContents childDirectoryContents = directoryContentsManager.getDirectoryContentsForFeatureBranch(projBranch, getAppendedPath(), dirID,
                                response);
                        if (childDirectoryContents != null) {
                            childDirectoryContents.setParentDirectoryID(directoryContents.getDirectoryID());
                            directoryContents = childDirectoryContents;
                            LOGGER.info("Found directory contents for: [{}]", getAppendedPath());
                            foundFlag = true;
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
            if (((directoryContents != null) && (getAppendedPath().length() == 0)) || ((getAppendedPath().length() > 0) && (directoryContents != null) && foundFlag)) {
                // This is the directory ID of the Trunk that is most closely associated with this branch directory.
                // TODO -- what about the case where this branch's directory does not exist on the trunk?  Will that
                // code path even go through here?  Won't the directoryContents object be null in that case?
                setDirectoryID(dirID);

                // Lookup the archiveDirManager for the file's current location so we can add ourselves
                // as a create listener (so we'll get notifications when an archive is created in the
                // trunk's directory).
                ArchiveDirManager trunkDirManager = DirectoryIDDictionary.getInstance().lookupArchiveDirManager(getProjectName(), dirID, response);
                trunkDirManager.addCreateListener(this);

                Map<Integer, String> files = directoryContents.getFiles();

                // Now, iterate over the directory contents files, and create the
                // objects that populate this object's container.
                Iterator<Integer> it = files.keySet().iterator();
                while (it.hasNext()) {
                    int fileID = it.next();
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

                    // Create the feature branch archiveInfo.
                    ArchiveInfoForFeatureBranch archiveInfoForFeatureBranch = new ArchiveInfoForFeatureBranch(filenameForBranch, archiveInfo,
                            getRemoteBranchProperties());
                    archiveInfo.addListener(archiveInfoForFeatureBranch);

                    String keyToOurFile = filenameForBranch;
                    if (ignoreOurCaseFlag) {
                        keyToOurFile = keyToOurFile.toLowerCase();
                    }

                    // Save the timestamp of the oldest revision in this logfile.
                    setOldestRevision(archiveInfoForFeatureBranch.getRevisionInformation().getRevisionHeader(archiveInfoForFeatureBranch.getRevisionCount() - 1)
                            .getCheckInDate().getTime());

                    // And store in our map...
                    archiveInfoMap.put(keyToOurFile, archiveInfoForFeatureBranch);

                    // And listen for changes to the info object (which itself listens
                    // for changes to the LogFile from which it is built).
                    archiveInfoForFeatureBranch.addListener(this);

                    LOGGER.trace("Adding file id: [" + fileID + "] filename: [" + filenameForBranch + "]");
                }
            } else {
                LOGGER.info("Found empty terminal directory: [{}]", getAppendedPath());
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Verify that we can create the archive for the given workfile.
     *
     * @param shortWorkfileName the short workfile name.
     * @throws com.qumasoft.qvcslib.QVCSException if the create is not allowed.
     */
    private void verifyCreateIsAllowed(String shortWorkfileName) throws QVCSException {
        // Make sure the file does not already exist in the destination directory.
        if (null != getArchiveInfo(shortWorkfileName)) {
            throw new QVCSException("Cannot create archive for file. File [" + shortWorkfileName + "] already exists.");
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

        // Make sure there are no locks.
        if (getArchiveInfo(shortWorkfileName).getLockCount() > 0) {
            throw new QVCSException("Cannot move an archive that is locked");
        }
    }

    /**
     * Verify that the rename is allowed.
     *
     * @param shortWorkfileName the current short workfile name.
     * @param newShortWorkfileName prospective new file name.
     * @throws com.qumasoft.qvcslib.QVCSException if the rename is not allowed.
     */
    private void verifyRenameIsAllowed(String shortWorkfileName, String newShortWorkfileName) throws QVCSException {
        // Make sure the file does not already exist in this directory.
        if (null != getArchiveInfo(newShortWorkfileName)) {
            throw new QVCSException("Cannot rename file. File " + newShortWorkfileName + " already exists.");
        }

        // Make sure there is an archive file.
        if (null == getArchiveInfo(shortWorkfileName)) {
            throw new QVCSException("Archive not found for '" + shortWorkfileName + "'. Archive file cannot be renamed since archive does not exist.");
        }

        // Make sure there are no locks.
        if (getArchiveInfo(shortWorkfileName).getLockCount() > 0) {
            throw new QVCSException("Cannot rename an archive that is locked");
        }
    }
}
