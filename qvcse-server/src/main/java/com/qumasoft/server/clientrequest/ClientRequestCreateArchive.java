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
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestCreateArchiveData;
import com.qumasoft.qvcslib.response.ServerResponseCreateArchive;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDigestManager;
import com.qumasoft.server.ArchiveDirManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ArchiveDirManagerForTranslucentBranch;
import com.qumasoft.server.LogFile;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request create archive.
 * @author Jim Voris
 */
public class ClientRequestCreateArchive implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestCreateArchiveData request;

    /**
     * Creates a new instance of ClientRequestCreateArchive.
     *
     * @param data the request data.
     */
    public ClientRequestCreateArchive(ClientRequestCreateArchiveData data) {
        request = data;
    }

    // TODO -- This may need significant work if we change the practice of 'deleting' a file
    // by marking it obsolete. As I write this, I've begun to think that a 'delete' should
    // really delete the archive from the directory (actually move it an flatten its name) to
    // a project archive attic: the archive filename would be based on the file's fileID, and
    // it would be located in a special project specific attic directory. This means that
    // we would not need the code below that checks for an obsolete file.  One issue with
    // this new approach would be the migration of an existing archive directory tree to
    // the new regime: all obsolete files would need to be migrated to the attic as part
    // of the 'upgrade' process. This is a pain.
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseCreateArchive serverResponse;
        ServerResponseInterface returnObject;
        CreateArchiveCommandArgs commandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = Utility.convertWorkfileNameToShortWorkfileName(commandArgs.getWorkfileName());
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManagerInterface = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            if (archiveDirManagerInterface instanceof ArchiveDirManager) {
                LogFile logfile = null;
                ArchiveDirManager archiveDirManager = (ArchiveDirManager) archiveDirManagerInterface;
                if (!archiveDirManager.directoryExists()) {
                    // Log an error.  The client is supposed to separately request the creation of the archive directory
                    // before it tries to create an archive.
                    LOGGER.log(Level.WARNING, "Requested creation of archive file, but archive directory does not yet exist for: " + appendedPath);
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive directory not found for " + appendedPath, projectName, viewName, appendedPath);
                    returnObject = error;
                } else {
                    LOGGER.log(Level.FINE, "Creating archive for: " + appendedPath + File.separator + shortWorkfileName);
                    java.io.File tempFile = java.io.File.createTempFile("QVCS", ".tmp");
                    tempFile.deleteOnExit();
                    try (java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                        outputStream.write(request.getBuffer());
                    }
                    // Check to see if the archive already exists -- maybe the file had been marked as obsolete, so the archive file may already exist.
                    LogFile obsoleteLogfile = (LogFile) archiveDirManager.getArchiveInfo(shortWorkfileName);
                    if (obsoleteLogfile != null) {
                        if (obsoleteLogfile.getIsObsolete()) {
                            // A flag we use to figure out if it's okay to continue with the work we're doing here...
                            boolean continueFlag = true;

                            // Mark the archive as not obsolete.
                            obsoleteLogfile.setIsObsolete(userName, false);

                            // If lock checking is enabled for this archive, we'll have to lock its tip revision, and then check-in this new revision.
                            if (obsoleteLogfile.getAttributes().getIsCheckLock()) {
                                LockRevisionCommandArgs lockCommandArgs = new LockRevisionCommandArgs();
                                lockCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                                lockCommandArgs.setUserName(userName);
                                lockCommandArgs.setFullWorkfileName(commandArgs.getWorkfileName());
                                lockCommandArgs.setShortWorkfileName(shortWorkfileName);
                                lockCommandArgs.setOutputFileName(commandArgs.getWorkfileName());

                                continueFlag = obsoleteLogfile.lockRevision(lockCommandArgs);
                            }

                            if (continueFlag) {
                                // Now just check-in the new revision.
                                CheckInCommandArgs checkInCommandArgs = new CheckInCommandArgs();
                                checkInCommandArgs.setUserName(userName);
                                checkInCommandArgs.setFullWorkfileName(commandArgs.getWorkfileName());
                                checkInCommandArgs.setCheckInTimestamp(commandArgs.getCheckInTimestamp());
                                checkInCommandArgs.setInputfileTimeStamp(commandArgs.getInputfileTimeStamp());
                                checkInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                                checkInCommandArgs.setCheckInComment(commandArgs.getArchiveDescription());
                                checkInCommandArgs.setLockFlag(commandArgs.getLockFlag());
                                if (obsoleteLogfile.checkInRevision(checkInCommandArgs, tempFile.getAbsolutePath(), false)) {
                                    serverResponse = new ServerResponseCreateArchive();
                                    logfile = (LogFile) archiveDirManager.getArchiveInfo(shortWorkfileName);
                                    SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo(logfile.getLogfileInfo(), File.separator, logfile.getIsObsolete(),
                                            logfile.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap());

                                    // Set the index so the client can match this response with the cached workfile.
                                    skinnyInfo.setCacheIndex(request.getIndex());
                                    serverResponse.setSkinnyLogfileInfo(skinnyInfo);
                                    serverResponse.setLogfileInfo(logfile.getLogfileInfo());
                                    serverResponse.setProjectName(projectName);
                                    serverResponse.setViewName(viewName);
                                    serverResponse.setAppendedPath(appendedPath);
                                    serverResponse.setLockFlag(commandArgs.getLockFlag());
                                    returnObject = serverResponse;
                                    tempFile.delete();

                                    ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] reusing archive for ["
                                            + Utility.formatFilenameForActivityJournal(projectName, viewName, appendedPath, shortWorkfileName)
                                            + "].  Obsolete archive is re-activated.");
                                } else {
                                    LOGGER.log(Level.WARNING, "Creation of archive file failed for: '" + appendedPath + File.separator + shortWorkfileName
                                            + "'. Unable to checkin revision to existing archive file!");
                                    // Return a command error.
                                    ServerResponseError error = new ServerResponseError("Creation of archive file failed for: '" + appendedPath + File.separator + shortWorkfileName
                                            + "'. Unable to checkin revision to existing archive file!", projectName, viewName, appendedPath);
                                    returnObject = error;
                                }
                            } else {
                                LOGGER.log(Level.WARNING, "Creation of archive file failed for: '" + appendedPath + File.separator + shortWorkfileName
                                        + "'. Unable to lock existing archive file!");
                                // Return a command error.
                                ServerResponseError error = new ServerResponseError("Creation of archive file failed for: '" + appendedPath + File.separator + shortWorkfileName
                                        + "'. Unable to lock existing archive file!", projectName, viewName, appendedPath);
                                returnObject = error;
                            }
                        } else {
                            LOGGER.log(Level.WARNING, "Creation of archive file failed for: '" + appendedPath + File.separator + shortWorkfileName
                                    + "'. Archive file already exists!");
                            // Return a command error.
                            ServerResponseError error = new ServerResponseError("Creation of archive file failed for: '" + appendedPath + File.separator + shortWorkfileName
                                    + "'. Archive file already exists!", projectName, viewName, appendedPath);
                            returnObject = error;
                        }
                    } else if (archiveDirManager.createArchive(commandArgs, tempFile.getAbsolutePath(), response)) {
                        serverResponse = new ServerResponseCreateArchive();
                        logfile = (LogFile) archiveDirManager.getArchiveInfo(shortWorkfileName);
                        SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo(logfile.getLogfileInfo(), File.separator, logfile.getIsObsolete(),
                                ArchiveDigestManager.getInstance().addRevision(logfile, logfile.getDefaultRevisionString()), logfile.getShortWorkfileName(),
                                logfile.getIsOverlap());

                        // Set the index so the client can match this response with the cached workfile.
                        skinnyInfo.setCacheIndex(request.getIndex());
                        serverResponse.setSkinnyLogfileInfo(skinnyInfo);
                        serverResponse.setLogfileInfo(logfile.getLogfileInfo());
                        serverResponse.setProjectName(projectName);
                        serverResponse.setViewName(viewName);
                        serverResponse.setAppendedPath(appendedPath);
                        serverResponse.setLockFlag(commandArgs.getLockFlag());
                        returnObject = serverResponse;
                        tempFile.delete();

                        ActivityJournalManager.getInstance().addJournalEntry("User: '" + userName + "' creating archive for ["
                                + Utility.formatFilenameForActivityJournal(projectName, viewName, appendedPath, shortWorkfileName) + "].");
                    } else {
                        LOGGER.log(Level.WARNING, "Creation of archive file failed for: " + appendedPath + File.separator + shortWorkfileName);

                        // Return a command error.
                        ServerResponseError error = new ServerResponseError("Creation of archive file failed for: "
                                + appendedPath + File.separator + shortWorkfileName, projectName, viewName, appendedPath);
                        returnObject = error;
                    }
                }

                // Create a reference copy if we need to.
                if ((returnObject instanceof ServerResponseCreateArchive) && (logfile != null)) {
                    AbstractProjectProperties projectProperties = archiveDirManager.getProjectProperties();

                    if (projectProperties.getCreateReferenceCopyFlag()) {
                        archiveDirManager.createReferenceCopy(projectProperties, logfile, request.getBuffer());
                    }
                }
            } else if (archiveDirManagerInterface instanceof ArchiveDirManagerForTranslucentBranch) {
                ArchiveDirManagerForTranslucentBranch archiveDirManagerForTranslucentBranch = (ArchiveDirManagerForTranslucentBranch) archiveDirManagerInterface;
                LOGGER.log(Level.INFO, "Creating branch archive for: [" + appendedPath + File.separator + shortWorkfileName + "]");
                java.io.File tempFile = java.io.File.createTempFile("QVCS", ".tmp");
                tempFile.deleteOnExit();
                try (java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                    outputStream.write(request.getBuffer());
                }
                if (archiveDirManagerForTranslucentBranch.createArchive(commandArgs, tempFile.getAbsolutePath(), response)) {
                    serverResponse = new ServerResponseCreateArchive();
                    ArchiveInfoInterface archiveInfo = archiveDirManagerForTranslucentBranch.getArchiveInfo(shortWorkfileName);
                    SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo(archiveInfo.getLogfileInfo(), File.separator, archiveInfo.getIsObsolete(),
                            archiveInfo.getDefaultRevisionDigest(), archiveInfo.getShortWorkfileName(), archiveInfo.getIsOverlap());

                    // Set the index so the client can match this response with the cached workfile.
                    skinnyInfo.setCacheIndex(request.getIndex());
                    serverResponse.setSkinnyLogfileInfo(skinnyInfo);
                    serverResponse.setLogfileInfo(archiveInfo.getLogfileInfo());
                    serverResponse.setProjectName(projectName);
                    serverResponse.setViewName(viewName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setLockFlag(commandArgs.getLockFlag());
                    returnObject = serverResponse;
                    tempFile.delete();

                    ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] creating branch archive for ["
                            + Utility.formatFilenameForActivityJournal(projectName, viewName, appendedPath, shortWorkfileName) + "].");
                } else {
                    LOGGER.log(Level.WARNING, "Creation of archive file failed for: " + appendedPath + File.separator + shortWorkfileName);
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Creation of archive file failed for: "
                            + appendedPath + File.separator + shortWorkfileName, projectName, viewName, appendedPath);
                    returnObject = error;
                }
            } else {
                // Explain the error.
                ServerResponseMessage message = new ServerResponseMessage("Create archive is not allowed for read-only view.",
                        projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName(shortWorkfileName);
                returnObject = message;
            }
        } catch (QVCSException | IOException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        return returnObject;
    }
}
