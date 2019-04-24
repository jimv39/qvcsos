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
import com.qumasoft.qvcslib.ArchiveDirManagerBase;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogfileListenerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import com.qumasoft.qvcslib.logfileaction.CheckOut;
import com.qumasoft.qvcslib.logfileaction.Create;
import com.qumasoft.qvcslib.logfileaction.Lock;
import com.qumasoft.qvcslib.logfileaction.MoveFile;
import com.qumasoft.qvcslib.logfileaction.Rename;
import com.qumasoft.qvcslib.logfileaction.SetRevisionDescription;
import com.qumasoft.qvcslib.logfileaction.Unlock;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckIn;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckOut;
import com.qumasoft.qvcslib.notifications.ServerNotificationCreateArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationHeaderChange;
import com.qumasoft.qvcslib.notifications.ServerNotificationInterface;
import com.qumasoft.qvcslib.notifications.ServerNotificationLock;
import com.qumasoft.qvcslib.notifications.ServerNotificationMoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRemoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRenameArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationSetRevisionDescription;
import com.qumasoft.qvcslib.notifications.ServerNotificationUnlock;
import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.dataaccess.DirectoryDAO;
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.dataaccess.impl.BranchDAOImpl;
import com.qumasoft.server.dataaccess.impl.DirectoryDAOImpl;
import com.qumasoft.server.dataaccess.impl.ProjectDAOImpl;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Directory;
import com.qumasoft.server.datamodel.Project;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive directory manager. There is one instance per archive directory. This class manages the archive files for a given directory.
 *
 * @author Jim Voris
 */
public class ArchiveDirManager extends ArchiveDirManagerBase implements ArchiveDirManagerReadWriteViewInterface, LogfileListenerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManager.class);

    /**
     * Remote listeners for changes to this directory.
     */
    private final Set<ServerResponseFactoryInterface> instanceLogfileListeners = new HashSet<>();
    private final Set<LogfileListenerInterface> instanceCreateListeners = new HashSet<>();
    /**
     * Keep track of oldest revision for this manager.
     */
    private long instanceOldestRevision = Long.MAX_VALUE;
    // Set the default value of the directoryID to -1.
    private int instanceDirectoryID = -1;
    // This directory's parent
    private ArchiveDirManager instanceParentArchiveDirManager;
    private Date mostRecentActivityDate = new Date(0L);

    /**
     * Creates a new instance of ArchiveDirManager.
     *
     * @param projectProperties project properties.
     * @param view the name of the view.
     * @param path the appended path.
     * @param user user name.
     * @param response response so we know where to send status updates, etc.
     */
    public ArchiveDirManager(AbstractProjectProperties projectProperties, String view, String path, String user, ServerResponseFactoryInterface response) {
        super(projectProperties, view, path, user);
        initArchiveDirectory();
        initParent(response);
    }

    /**
     * Start the directory manager. Used only on the client.
     */
    @Override
    public void startDirectoryManager() {
        // We don't need to do anything.
    }

    /**
     * Get the archive directory manager for this directory's parent directory. Parent, in this context means closer to root directory, i.e. we're navigating up the directory tree.
     *
     * @return the parent directory's archive directory manager, or null if this is the root directory.
     */
    public ArchiveDirManager getParent() {
        return instanceParentArchiveDirManager;
    }

    /**
     * Get the archive directory manager for the root directory of this directory tree.
     *
     * @return the root directory's archive directory manager.
     */
    public ArchiveDirManager getProjectRootArchiveDirManager() {
        ArchiveDirManager projectRootArchiveDirManager = this;
        while (true) {
            if (projectRootArchiveDirManager.getParent() == null) {
                break;
            }
            projectRootArchiveDirManager = projectRootArchiveDirManager.getParent();
        }
        return projectRootArchiveDirManager;
    }

    private void notifyCreateListeners(LogFile logfile) {
        Create createAction = new Create();
        synchronized (instanceCreateListeners) {
            Iterator<LogfileListenerInterface> it = instanceCreateListeners.iterator();
            while (it.hasNext()) {
                LogfileListenerInterface listener = it.next();
                listener.notifyLogfileListener(logfile, createAction);
            }
        }
    }

    /**
     * Add a create listener.
     *
     * @param dirManager a listener who wishes to be notified of file create actions that occur within this archive directory manager.
     */
    public void addCreateListener(LogfileListenerInterface dirManager) {
        synchronized (instanceCreateListeners) {
            instanceCreateListeners.add(dirManager);
        }
    }

    /**
     * Remove a create listener.
     *
     * @param dirManager a listener who no longer wished to be notified of file create actions that occur within this archive directory manager.
     */
    public void removeCreateListener(ArchiveDirManagerInterface dirManager) {
        synchronized (instanceCreateListeners) {
            if (dirManager instanceof LogfileListenerInterface) {
                LogfileListenerInterface logfileListenerInterface = (LogfileListenerInterface) dirManager;
                instanceCreateListeners.remove(logfileListenerInterface);
            }
        }
    }

    /**
     * Figure out the parent archive directory manager for this archive directory manager.
     *
     * @param response an object that identifies the client.
     * @param discardObsoleteFilesFlag a flag indicating whether to discard obsolete files.
     */
    private void initParent(ServerResponseFactoryInterface response) {
        String parentAppendedPath = getParentAppendedPath();
        if (parentAppendedPath != null) {
            try {
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getViewName(), parentAppendedPath);
                instanceParentArchiveDirManager = (ArchiveDirManager) ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                        directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            } catch (QVCSException e) {
                LOGGER.warn("Caught exception when trying to initialize parent directory manager for: [{}]", getAppendedPath(), e);
            }
        } else {
            instanceParentArchiveDirManager = null;
        }
    }

    private void initArchiveDirectory() {
        File directory = new File(getArchiveDirectoryName());
        File[] fileList = directory.listFiles();
        File directoryIDFile = new File(getArchiveDirectoryName() + File.separator + QVCSConstants.QVCS_DIRECTORYID_FILENAME);

        // We need to init this first!!
        initDirectoryID(directoryIDFile);

        if (fileList == null) {
            return;
        }
        for (File fileList1 : fileList) {
            if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_CACHE_NAME) == 0) {
                // Get rid of the cache file, since we may be changing things here that
                // would make the cache out of date.
                if (fileList1.delete()) {
                    LOGGER.info("Deleting [{}] file from directory: [{}]", QVCSConstants.QVCS_CACHE_NAME, directory.getAbsolutePath());
                }
                continue;
            }
            if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_JOURNAL_NAME) == 0) {
                continue;
            }
            if (fileList1.isDirectory()) {
                continue;
            }
            if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_DIRECTORYID_FILENAME) == 0) {
                continue;
            }
            if (fileList1.getName().endsWith(QVCSConstants.QVCS_ARCHIVE_TEMPFILE_SUFFIX)) {
                continue;
            }
            if (fileList1.getName().endsWith(QVCSConstants.QVCS_ARCHIVE_OLDFILE_SUFFIX)) {
                continue;
            }
            if (Utility.isMacintosh() && fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_MAC_DS_STORE_FILENAME) == 0) {
                continue;
            }
            LogFile logfile = new LogFile(fileList1.getPath());
            if (logfile.readInformation()) {
                String shortWorkfileName = logfile.getShortWorkfileName();
                synchronized (getArchiveInfoCollection()) {
                    getArchiveInfoCollection().put(Utility.getArchiveKey(getProjectProperties(), shortWorkfileName), logfile);
                }
                logfile.addListener(this);

                // Capture the association of this file to this directory.
                FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getViewName(), logfile.getFileID(), getAppendedPath(), logfile.getShortWorkfileName(),
                        getDirectoryID());

                // Save the timestamp of the oldest revision in this logfile.
                setOldestRevision(logfile.getRevisionInformation().getRevisionHeader(logfile.getRevisionCount() - 1).getCheckInDate().getTime());
            } else {
                LOGGER.warn("Failed to read logfile information for: [{}]", fileList1.getPath());
            }
        }
    }

    private void setOldestRevision(long revisionCheckInTime) {
        if (revisionCheckInTime < instanceOldestRevision) {
            instanceOldestRevision = revisionCheckInTime;
        }
    }

    /**
     * Get the time of the oldest revision in this directory. (Used for license enforcement).
     *
     * @return the number of milliseconds past the epoch for the oldest revision in this directory.
     */
    @Override
    public long getOldestRevision() {
        return instanceOldestRevision;
    }

    /**
     * Does the archive directory exist.
     *
     * @return true if the directory already exists; false otherwise.
     */
    public boolean directoryExists() {
        File directory = new File(getArchiveDirectoryName());
        return directory.exists();
    }

    /**
     * Create the archive directory.
     *
     * @return true if the create succeeds; false otherwise.
     */
    @Override
    public boolean createDirectory() {
        boolean createdDirectory = false;
        if (!directoryExists()) {
            File directory = new File(getArchiveDirectoryName());
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    LOGGER.warn("Failed to create archive directory: [{}]", directory.getAbsolutePath());
                } else {
                    ProjectDAO projectDAO = new ProjectDAOImpl();
                    Project project = projectDAO.findByProjectName(getProjectName());
                    if (project != null) {
                        BranchDAO branchDAO = new BranchDAOImpl();
                        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), QVCSConstants.QVCS_TRUNK_VIEW);
                        if (branch != null) {
                            // Add the new directory to the database.
                            DirectoryDAO directoryDAO = new DirectoryDAOImpl();
                            Directory newDirectory = new Directory();
                            newDirectory.setDirectoryId(getDirectoryID());
                            newDirectory.setAppendedPath(getAppendedPath());
                            newDirectory.setBranchId(branch.getBranchId());
                            if (getParent() != null) {
                                newDirectory.setParentDirectoryId(getParent().getDirectoryID());
                            }
                            newDirectory.setRootDirectoryId(getProjectRootArchiveDirManager().getDirectoryID());
                            try {
                                directoryDAO.insert(newDirectory);
                                createdDirectory = true;
                            } catch (SQLException e) {
                                LOGGER.warn(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
            }
        } else {
            createdDirectory = true;
        }
        return createdDirectory;
    }

    /**
     * Create an archive file.
     *
     * @param commandLineArgs the command line arguments that define the archive to create.
     * @param inputFileName the name of the file that will be inserted into the archive file as the 1st revision.
     * @param response the object that identifies the client.
     * @return true if things work; false otherwise.
     * @throws IOException if there is a QVCS specific problem.
     * @throws QVCSException if there is an IO exception.
     */
    @Override
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, String inputFileName, ServerResponseFactoryInterface response) throws IOException,
            QVCSException {
        boolean retVal = false;

        // Make sure the archive directory exists.
        File directory = new File(getArchiveDirectoryName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                LOGGER.warn("Failed to create archive directory: [{}]", directory.getAbsolutePath());
                retVal = false;
            }
        } else {
            String shortArchiveFilename = Utility.convertWorkfileNameToShortArchiveName(commandLineArgs.getWorkfileName());
            String fullArchiveFilename = getArchiveDirectoryName() + File.separator + shortArchiveFilename;
            LogFile logfile = new LogFile(fullArchiveFilename);

            // Set the timestamp to be the one from the transaction...
            Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);
            commandLineArgs.setCheckInTimestamp(date);

            logfile.addListener(this);

            if (logfile.createArchive(commandLineArgs, getProjectProperties(), inputFileName)) {
                if (logfile.readInformation()) {
                    String keyShortWorkfileName = logfile.getShortWorkfileName();
                    synchronized (getArchiveInfoCollection()) {
                        getArchiveInfoCollection().put(Utility.getArchiveKey(getProjectProperties(), keyShortWorkfileName), logfile);
                    }
                    retVal = true;

                    // Capture the change to the directory contents...
                    try {
                        DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName()).addFileToTrunk(getDirectoryID(), logfile.getFileID(),
                                logfile.getShortWorkfileName(),
                                response);
                    } catch (SQLException e) {
                        LOGGER.warn(e.getLocalizedMessage(), e);
                        throw new QVCSException("Caught SQLException: " + e.getLocalizedMessage());
                    }

                    // Capture the association of this file to this directory.
                    FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getViewName(), logfile.getFileID(), getAppendedPath(), logfile.getShortWorkfileName(),
                            getDirectoryID());
                }
                notifyCreateListeners(logfile);
            }
        }

        return retVal;
    }

    /**
     * Rename a file within this directory. This needs to be synchronized so only one rename occurs at a time within this directory
     *
     * @param userName user name
     * @param oldShortWorkfileName the old short workfile name
     * @param newShortWorkfileName the new short workfile name
     * @param response object used to capture who we're working for.
     * @return true if things work okay; false otherwise.
     * @throws IOException if we have an IO exception.
     * @throws QVCSException if we have a QVCS related exception.
     */
    @Override
    public boolean renameArchive(String userName, String oldShortWorkfileName, String newShortWorkfileName, ServerResponseFactoryInterface response)
            throws IOException, QVCSException {
        LogFile originalLogfile;
        boolean retVal = false;

        String containerKeyValue = Utility.getArchiveKey(getProjectProperties(), oldShortWorkfileName);

        synchronized (getArchiveInfoCollection()) {
            // Lookup the existing LogFile object.
            originalLogfile = (LogFile) getArchiveInfoCollection().get(containerKeyValue);
            Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);

            if (originalLogfile != null) {
                if (originalLogfile.getLockCount() == 0) {
                    if (originalLogfile.renameArchive(userName, getAppendedPath(), oldShortWorkfileName, newShortWorkfileName, date)) {
                        // Throw away the old logfile.... we can't use it anymore.
                        getArchiveInfoCollection().remove(containerKeyValue);

                        // Delete the reference copy if we need to...
                        AbstractProjectProperties projectProperties = getProjectProperties();
                        if (projectProperties.getCreateReferenceCopyFlag()) {
                            deleteReferenceCopy(projectProperties, originalLogfile);
                        }

                        String shortArchiveFilename = Utility.convertWorkfileNameToShortArchiveName(newShortWorkfileName);
                        String fullArchiveFilename = getArchiveDirectoryName() + File.separator + shortArchiveFilename;

                        LogFile newLogfile = new LogFile(fullArchiveFilename);
                        if (newLogfile.readInformation()) {
                            // Capture the change to the directory contents...
                            DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName()).renameFileOnTrunk(getDirectoryID(),
                                    originalLogfile.getFileID(), oldShortWorkfileName, newShortWorkfileName, response);

                            // Add the new name to our container.
                            String shortWorkfileName = newLogfile.getShortWorkfileName();
                            getArchiveInfoCollection().put(Utility.getArchiveKey(getProjectProperties(), shortWorkfileName), newLogfile);
                            newLogfile.addListener(this);

                            // Capture the association of this file to this directory.
                            FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getViewName(), originalLogfile.getFileID(), getAppendedPath(),
                                    newLogfile.getShortWorkfileName(),
                                    getDirectoryID());

                            // Create a notification message to let everyone know about the
                            // 'new' file.
                            Rename logfileActionRename = new Rename(oldShortWorkfileName);
                            notifyLogfileListener(newLogfile, logfileActionRename);

                            // Notify any translucent branches about the 'new' file.
                            notifyCreateListeners(newLogfile);
                            retVal = true;

                            // Create a new reference copy if we need to...
                            if (projectProperties.getCreateReferenceCopyFlag()) {
                                byte[] buffer = newLogfile.getRevisionAsByteArray(newLogfile.getDefaultRevisionString());
                                createReferenceCopy(projectProperties, newLogfile, buffer);
                            }
                        }
                    }
                } else {
                    LOGGER.warn("Rename not allowed for locked file: [{}]", originalLogfile.getShortWorkfileName());
                }
            }
        }
        return retVal;
    }

    /**
     * Move an archive file from one directory to another.
     *
     * @param userName the user requesting the move.
     * @param shortWorkfileName the short name of the file.
     * @param targetArchiveDirManagerInterface the destination directory.
     * @param response identify the client.
     * @return true if the move worked; false if it did not.
     * @throws IOException for any IOException
     * @throws QVCSException for QVCS problems.
     */
    @Override
    public boolean moveArchive(String userName, String shortWorkfileName, final ArchiveDirManagerInterface targetArchiveDirManagerInterface,
            ServerResponseFactoryInterface response)
            throws IOException, QVCSException {
        LogFile logfile;
        boolean retVal = false;

        String containerKeyValue = Utility.getArchiveKey(getProjectProperties(), shortWorkfileName);

        // We need to synchronize on the class object -- only one move at at time
        // is allowed on the whole server. We need to do this to avoid a possible
        // deadlock situation that would occur if user A moved a file from directory
        // A to directory B at the same time as user B moving a file from
        // directory B to directory A.
        synchronized (ArchiveDirManager.class) {
            // Lookup the existing LogFile object.
            logfile = (LogFile) getArchiveInfoCollection().get(containerKeyValue);

            // Lookup the date we'll use..
            Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);

            if ((logfile != null) && logfile.moveArchive(userName, getAppendedPath(), targetArchiveDirManagerInterface, shortWorkfileName, date)) {
                // Throw away the old logfile.... we can't use it anymore.
                getArchiveInfoCollection().remove(containerKeyValue);

                // Delete the reference copy if we need to...
                AbstractProjectProperties projectProperties = getProjectProperties();
                if (projectProperties.getCreateReferenceCopyFlag()) {
                    deleteReferenceCopy(projectProperties, logfile);
                }

                // Save the listeners so we can restore them to the new LogFile
                // object.
                List<LogfileListenerInterface> logfileListeners = logfile.getLogfileListeners();

                // Cast the target to an actual ArchiveDirManager, since that is
                // what it MUST be here...
                if (targetArchiveDirManagerInterface instanceof ArchiveDirManager) {
                    ArchiveDirManager targetArchiveDirManager = (ArchiveDirManager) targetArchiveDirManagerInterface;

                    // Add the moved archive to the target archive directory manager.
                    String shortArchiveFilename = Utility.convertWorkfileNameToShortArchiveName(shortWorkfileName);
                    String fullTargetArchiveFilename = targetArchiveDirManager.getArchiveDirectoryName() + File.separator + shortArchiveFilename;
                    LogFile targetLogfile = new LogFile(fullTargetArchiveFilename);
                    targetLogfile.addListener(targetArchiveDirManager);
                    if (targetLogfile.readInformation()) {
                        synchronized (targetArchiveDirManager.getArchiveInfoCollection()) {
                            targetArchiveDirManager.getArchiveInfoCollection().put(containerKeyValue, targetLogfile);
                        }

                        // Capture the change to the directories' contents
                        DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName()).moveFileOnTrunk(QVCSConstants.QVCS_TRUNK_VIEW,
                                getDirectoryID(),
                                targetArchiveDirManager.getDirectoryID(), logfile.getFileID(), response);

                        // Capture the change in association of this file to this directory.
                        FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getViewName(), logfile.getFileID(), targetArchiveDirManager.getAppendedPath(),
                                shortWorkfileName,
                                targetArchiveDirManager.getDirectoryID());

                        // Add any view listeners back to the new LogFile object.
                        if (logfileListeners != null) {
                            logfileListeners.stream().filter((listener) -> (listener instanceof ArchiveInfoInterface)).forEach((listener) -> {
                                targetLogfile.addListener(listener);
                            });

                            // Discard any listeners on the old logfile so it can
                            // get garbage collected.
                            logfile.clearLogfileListeners();
                        }

                        // Create the reference copy if we need to...
                        if (projectProperties.getCreateReferenceCopyFlag()) {
                            byte[] buffer = targetLogfile.getRevisionAsByteArray(targetLogfile.getDefaultRevisionString());
                            targetArchiveDirManager.createReferenceCopy(projectProperties, targetLogfile, buffer);
                        }

                        // Create a notification message to let everyone know about the 'new' file.
                        Create logfileActionCreate = new Create();
                        targetArchiveDirManager.notifyLogfileListener(targetLogfile, logfileActionCreate);

                        // Notify any translucent branches about the 'new' file.
                        targetArchiveDirManager.notifyCreateListeners(targetLogfile);

                        retVal = true;
                    }
                } else {
                    throw new QVCSException("Internal error.");
                }
            }
        }

        return retVal;
    }

    @Override
    public boolean deleteArchive(String userName, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        LogFile logfile;
        boolean retVal = false;

        String containerKeyValue = Utility.getArchiveKey(getProjectProperties(), shortWorkfileName);

        // We need to synchronize on the class object -- only one delete at at time
        // is allowed on the whole server. We need to do this to avoid a possible
        // deadlock situation that would occur if user A moved a file from directory
        // A to directory B at the same time as user B moving a file from
        // directory B to directory A.
        synchronized (ArchiveDirManager.class) {
            // Lookup the existing LogFile object.
            logfile = (LogFile) getArchiveInfoCollection().get(containerKeyValue);

            // Lookup the date we'll use..
            Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);

            // Get the cemetery archive directory manager
            ArchiveDirManagerInterface cemeteryArchiveDirManagerInterface = ServerUtility.getCemeteryArchiveDirManager(getProjectName(), response);

            if (logfile != null) {
                int fileID = logfile.getFileID();

                if (logfile.deleteArchive(userName, getAppendedPath(), cemeteryArchiveDirManagerInterface, shortWorkfileName, date)) {
                    // Throw away the old logfile.... we can't use it anymore.
                    getArchiveInfoCollection().remove(containerKeyValue);

                    // Save the listeners so we can restore them to the new LogFile
                    // object.
                    List<LogfileListenerInterface> logfileListeners = logfile.getLogfileListeners();

                    // Cast the target to an actual ArchiveDirManager, since that is
                    // what it MUST be here...
                    ArchiveDirManager cemeteryArchiveDirManager = (ArchiveDirManager) cemeteryArchiveDirManagerInterface;

                    // Add the moved archive to the target archive directory manager.
                    String shortArchiveFilename = Utility.createCemeteryShortArchiveName(logfile.getFileID());
                    String cemeteryWorkfileName = Utility.convertArchiveNameToShortWorkfileName(shortArchiveFilename);
                    String cemeteryKeyValue = Utility.getArchiveKey(getProjectProperties(), cemeteryWorkfileName);

                    String fullTargetArchiveFilename = cemeteryArchiveDirManager.getArchiveDirectoryName() + File.separator + shortArchiveFilename;
                    LogFile targetLogfile = new LogFile(fullTargetArchiveFilename);
                    targetLogfile.addListener(cemeteryArchiveDirManager);
                    if (targetLogfile.readInformation()) {
                        synchronized (cemeteryArchiveDirManager.getArchiveInfoCollection()) {
                            cemeteryArchiveDirManager.getArchiveInfoCollection().put(cemeteryKeyValue, targetLogfile);
                        }

                        // Capture the change to the directories' contents
                        DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(getProjectName()).deleteFileFromTrunk(getAppendedPath(), getDirectoryID(),
                                cemeteryArchiveDirManager.getDirectoryID(), fileID, shortWorkfileName, response);

                        // Capture the change in association of this file to this directory.
                        FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getViewName(), fileID, cemeteryArchiveDirManager.getAppendedPath(), cemeteryWorkfileName,
                                cemeteryArchiveDirManager.getDirectoryID());

                        // Add any view listeners back to the new LogFile object.
                        if (logfileListeners != null) {
                            logfileListeners.stream().filter((listener) -> (listener instanceof ArchiveInfoInterface)).forEach((listener) -> {
                                targetLogfile.addListener(listener);
                            });

                            // Discard any listeners on the old logfile so it can
                            // get garbage collected.
                            logfile.clearLogfileListeners();
                        }

                        // Notify any cemetery listeners of the change.
                        cemeteryArchiveDirManager.notifyLogfileListener(targetLogfile, new Create());

                        retVal = true;
                    }
                }
            }
        }

        return retVal;
    }

    @Override
    public boolean unDeleteArchive(String userName, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException {
        UnDeleteArchiveOperation unDeleteArchiveOperation = new UnDeleteArchiveOperation(this, userName, shortWorkfileName, response);
        return unDeleteArchiveOperation.execute();
    }

    @Override
    public void addLogFileListener(ServerResponseFactoryInterface logfileListener) {
        synchronized (instanceLogfileListeners) {
            instanceLogfileListeners.add(logfileListener);
        }
    }

    @Override
    public void removeLogFileListener(ServerResponseFactoryInterface logfileListener) {
        synchronized (instanceLogfileListeners) {
            instanceLogfileListeners.remove(logfileListener);
        }
    }

    /**
     * Read this directory's directory ID from the directory ID file...
     */
    @SuppressWarnings("LoggerStringConcat")
    private void initDirectoryID(File directoryIDFile) {
        DataInputStream dataInputStream = null;
        try {
            dataInputStream = new DataInputStream(new FileInputStream(directoryIDFile));
            setDirectoryID(dataInputStream.readInt());
        } catch (FileNotFoundException e) {
            LOGGER.info("Unable to find directory ID file for: [{}]", getArchiveDirectoryName());
            instanceDirectoryID = -1;
        } catch (IOException e) {
            LOGGER.info("Unable to read directory ID file for: [{}]", getArchiveDirectoryName());
            instanceDirectoryID = -1;
        } finally {
            try {
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (IOException e) {
                LOGGER.warn("IOException when closing data input stream for: [{}]", getArchiveDirectoryName(), e);
            }
        }
    }

    /**
     * Save this directory's directory ID to the directory ID file...
     */
    @SuppressWarnings("LoggerStringConcat")
    private void saveDirectoryID() {
        DataOutputStream dataOutputStream = null;
        try {
            File directory = new File(getArchiveDirectoryName());
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File directoryIDFile = new File(getArchiveDirectoryName() + File.separator + QVCSConstants.QVCS_DIRECTORYID_FILENAME);
            dataOutputStream = new DataOutputStream(new FileOutputStream(directoryIDFile));
            dataOutputStream.writeInt(instanceDirectoryID);
        } catch (IOException e) {
            LOGGER.warn("Unable to write directory ID file for: [{}]", getArchiveDirectoryName(), e);
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    @Override
    public int getDirectoryID() {
        if (instanceDirectoryID == -1) {
            setDirectoryID(DirectoryIDManager.getInstance().getNewDirectoryID());
            saveDirectoryID();
        }

        return instanceDirectoryID;
    }

    /**
     * Set the directory id.
     * @param directoryID the directory id.
     */
    @SuppressWarnings("LoggerStringConcat")
    public void setDirectoryID(int directoryID) {
        LOGGER.info("Setting directory id for [{}] to: [{}]", getAppendedPath(), directoryID);
        instanceDirectoryID = directoryID;
        DirectoryIDDictionary.getInstance().put(directoryID, this);
    }

    /**
     * Return a Collection of LogFile objects that are associated with this directory for the given label string.
     *
     * @param label the label to search for.
     * @return A Collection of archive files (LogFile objects) that each have the given label.
     */
    public synchronized Collection<LogFile> getArchiveCollectionByLabel(final String label) {
        ArrayList<LogFile> arrayList = new ArrayList<>();
        Iterator it = getArchiveInfoCollection().values().iterator();
        while (it.hasNext()) {
            LogFile logFile = (LogFile) it.next();
            if (logFile.hasLabel(label)) {
                arrayList.add(logFile);
            }
        }

        return arrayList;
    }

    /**
     * Return a Collection of LogFile objects that are associated with this directory for the given Date.
     *
     * @param date date that defines the archive files that should compose the returned Collection.
     * @return A Collection of archive files (LogFile objects).
     */
    public synchronized Collection<LogFile> getArchiveCollectionByDate(final java.util.Date date) {
        ArrayList<LogFile> arrayList = new ArrayList<>();
        Iterator it = getArchiveInfoCollection().values().iterator();
        while (it.hasNext()) {
            LogFile logFile = (LogFile) it.next();
            int revisionCount = logFile.getRevisionCount();

            // If the 1st file revision was created before the requested date, then
            // include it in the returned collection.
            if (logFile.getRevisionInformation().getRevisionHeader(revisionCount - 1).getCheckInDate().getTime() < date.getTime()) {
                arrayList.add(logFile);
            }
        }
        return arrayList;
    }

    @Override
    public void notifyLogfileListener(ArchiveInfoInterface subject, ActionType action) {
        // Build the information we need to send to the listeners.
        ServerNotificationInterface info = buildLogfileNotification(subject, action);

        // Let any remote users know about the logfile change.
        if (info != null) {
            synchronized (instanceLogfileListeners) {
                Iterator<ServerResponseFactoryInterface> it = instanceLogfileListeners.iterator();
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

    private ServerNotificationInterface buildLogfileNotification(ArchiveInfoInterface subject, ActionType action) {
        ServerNotificationInterface info = null;
        byte[] digest = subject.getDefaultRevisionDigest();

        switch (action.getAction()) {
            case ActionType.CHECKOUT:
                ServerNotificationCheckOut serverNotificationCheckOut = new ServerNotificationCheckOut();
                if (action instanceof CheckOut) {
                    CheckOut checkOutAction = (CheckOut) action;
                    CheckOutCommandArgs commandArgs = checkOutAction.getCommandArgs();
                    serverNotificationCheckOut.setProjectName(getProjectName());
                    serverNotificationCheckOut.setViewName(getViewName());
                    serverNotificationCheckOut.setAppendedPath(getAppendedPath());
                    serverNotificationCheckOut.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationCheckOut.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationCheckOut.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    serverNotificationCheckOut.setRevisionString(commandArgs.getRevisionString());
                    info = serverNotificationCheckOut;
                }
                break;
            case ActionType.CHECKIN:
                ServerNotificationCheckIn serverNotificationCheckIn = new ServerNotificationCheckIn();
                serverNotificationCheckIn.setProjectName(getProjectName());
                serverNotificationCheckIn.setViewName(getViewName());
                serverNotificationCheckIn.setAppendedPath(getAppendedPath());
                serverNotificationCheckIn.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationCheckIn.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationCheckIn;
                break;
            case ActionType.LOCK:
                if (action instanceof Lock) {
                    ServerNotificationLock serverNotificationLock = new ServerNotificationLock();
                    Lock lockAction = (Lock) action;
                    LockRevisionCommandArgs commandArgs = lockAction.getCommandArgs();
                    serverNotificationLock.setProjectName(getProjectName());
                    serverNotificationLock.setViewName(getViewName());
                    serverNotificationLock.setAppendedPath(getAppendedPath());
                    serverNotificationLock.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationLock.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationLock.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationLock.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationLock;
                }
                break;
            case ActionType.CREATE:
                ServerNotificationCreateArchive serverNotificationCreateArchive = new ServerNotificationCreateArchive();
                serverNotificationCreateArchive.setProjectName(getProjectName());
                serverNotificationCreateArchive.setViewName(getViewName());
                serverNotificationCreateArchive.setAppendedPath(getAppendedPath());
                serverNotificationCreateArchive.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationCreateArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationCreateArchive;
                break;
            case ActionType.MOVE_FILE:
                if (action instanceof MoveFile) {
                    MoveFile moveFileAction = (MoveFile) action;
                    ServerNotificationMoveArchive serverNotificationMoveArchive = new ServerNotificationMoveArchive();
                    serverNotificationMoveArchive.setShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationMoveArchive.setOriginAppendedPath(moveFileAction.getOriginAppendedPath());
                    serverNotificationMoveArchive.setDestinationAppendedPath(moveFileAction.getDestinationAppendedPath());
                    serverNotificationMoveArchive.setProjectName(getProjectName());
                    serverNotificationMoveArchive.setViewName(getViewName());
                    serverNotificationMoveArchive.setProjectProperties(getProjectProperties().getProjectProperties());
                    serverNotificationMoveArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationMoveArchive;
                }
                break;
            case ActionType.UNLOCK:
                if (action instanceof Unlock) {
                    Unlock unlockAction = (Unlock) action;
                    ServerNotificationUnlock serverNotificationUnlock = new ServerNotificationUnlock();
                    UnlockRevisionCommandArgs commandArgs = unlockAction.getCommandArgs();
                    serverNotificationUnlock.setProjectName(getProjectName());
                    serverNotificationUnlock.setViewName(getViewName());
                    serverNotificationUnlock.setAppendedPath(getAppendedPath());
                    serverNotificationUnlock.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationUnlock.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationUnlock.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationUnlock.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationUnlock;
                }
                break;
            case ActionType.SET_REVISION_DESCRIPTION:
                if (action instanceof SetRevisionDescription) {
                    SetRevisionDescription setRevisionDescriptionAction = (SetRevisionDescription) action;
                    ServerNotificationSetRevisionDescription serverNotificationSetRevisionDescription = new ServerNotificationSetRevisionDescription();
                    SetRevisionDescriptionCommandArgs commandArgs = setRevisionDescriptionAction.getCommandArgs();
                    serverNotificationSetRevisionDescription.setProjectName(getProjectName());
                    serverNotificationSetRevisionDescription.setViewName(getViewName());
                    serverNotificationSetRevisionDescription.setAppendedPath(getAppendedPath());
                    serverNotificationSetRevisionDescription.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationSetRevisionDescription.setRevisionDescription(commandArgs.getRevisionDescription());
                    serverNotificationSetRevisionDescription.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationSetRevisionDescription.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationSetRevisionDescription;
                }
                break;
            case ActionType.REMOVE:
                ServerNotificationRemoveArchive serverNotificationRemoveArchive = new ServerNotificationRemoveArchive();
                serverNotificationRemoveArchive.setProjectName(getProjectName());
                serverNotificationRemoveArchive.setViewName(getViewName());
                serverNotificationRemoveArchive.setAppendedPath(getAppendedPath());
                serverNotificationRemoveArchive.setShortWorkfileName(subject.getShortWorkfileName());
                info = serverNotificationRemoveArchive;
                break;
            case ActionType.RENAME:
                if (action instanceof Rename) {
                    Rename renameAction = (Rename) action;
                    ServerNotificationRenameArchive serverNotificationRenameArchive = new ServerNotificationRenameArchive();
                    serverNotificationRenameArchive.setProjectName(getProjectName());
                    serverNotificationRenameArchive.setViewName(getViewName());
                    serverNotificationRenameArchive.setAppendedPath(getAppendedPath());
                    serverNotificationRenameArchive.setNewShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationRenameArchive.setOldShortWorkfileName(renameAction.getOldShortWorkfileName());
                    serverNotificationRenameArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationRenameArchive;
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
                ServerNotificationHeaderChange serverNotificationHeaderChange = new ServerNotificationHeaderChange();
                serverNotificationHeaderChange.setProjectName(getProjectName());
                serverNotificationHeaderChange.setViewName(getViewName());
                serverNotificationHeaderChange.setAppendedPath(getAppendedPath());
                serverNotificationHeaderChange.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationHeaderChange.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationHeaderChange;
                break;
        }
        return info;
    }

    /**
     * Update the most recent activity date.
     * @param activityDate the new activity date.
     */
    public void updateMostRecentActivityDate(Date activityDate) {
        if (activityDate.after(this.mostRecentActivityDate)) {
            this.mostRecentActivityDate = activityDate;
            if (getParent() != null) {
                getParent().updateMostRecentActivityDate(activityDate);
            }
        }
    }

    @Override
    public Date getMostRecentActivityDate() {
        return new Date(this.mostRecentActivityDate.getTime());
    }
}
