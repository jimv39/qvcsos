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
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request create archive.
 * @author Jim Voris
 */
public class ClientRequestCreateArchive implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestCreateArchive.class);
    private final ClientRequestCreateArchiveData request;

    /**
     * Creates a new instance of ClientRequestCreateArchive.
     *
     * @param data the request data.
     */
    public ClientRequestCreateArchive(ClientRequestCreateArchiveData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseCreateArchive serverResponse;
        ServerResponseInterface returnObject;
        CreateArchiveCommandArgs commandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String viewName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = Utility.convertWorkfileNameToShortWorkfileName(commandArgs.getWorkfileName());
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManagerInterface = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            if (archiveDirManagerInterface instanceof ArchiveDirManager) {
                LogFile logfile = null;
                ArchiveDirManager archiveDirManager = (ArchiveDirManager) archiveDirManagerInterface;
                if (!archiveDirManager.directoryExists()) {
                    // Log an error.  The client is supposed to separately request the creation of the archive directory
                    // before it tries to create an archive.
                    LOGGER.warn("Requested creation of archive file, but archive directory does not yet exist for: " + appendedPath);
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Archive directory not found for " + appendedPath, projectName, viewName, appendedPath);
                    returnObject = error;
                } else {
                    LOGGER.trace("Creating archive for: " + appendedPath + File.separator + shortWorkfileName);
                    java.io.File tempFile = java.io.File.createTempFile("QVCS", ".tmp");
                    tempFile.deleteOnExit();
                    try (java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                        outputStream.write(request.getBuffer());
                    }
                    // Check to see if the archive already exists -- maybe the file had been marked as obsolete, so the archive file may already exist.
                    LogFile existingLogFile = (LogFile) archiveDirManager.getArchiveInfo(shortWorkfileName);
                    if (existingLogFile != null) {
                        LOGGER.warn("Creation of archive file failed for: [" + appendedPath + File.separator + shortWorkfileName
                                + "]. Archive file already exists!");
                        // Return a command error.
                        ServerResponseError error = new ServerResponseError("Creation of archive file failed for: [" + appendedPath + File.separator + shortWorkfileName
                                + "]. Archive file already exists!", projectName, viewName, appendedPath);
                        returnObject = error;
                    } else if (archiveDirManager.createArchive(commandArgs, tempFile.getAbsolutePath(), response)) {
                        serverResponse = new ServerResponseCreateArchive();
                        logfile = (LogFile) archiveDirManager.getArchiveInfo(shortWorkfileName);
                        SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo(logfile.getLogfileInfo(), File.separator,
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

                        ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] creating archive for ["
                                + Utility.formatFilenameForActivityJournal(projectName, viewName, appendedPath, shortWorkfileName) + "].");
                    } else {
                        LOGGER.warn("Creation of archive file failed for: [" + appendedPath + File.separator + shortWorkfileName + "]");

                        // Return a command error.
                        ServerResponseError error = new ServerResponseError("Creation of archive file failed for: ["
                                + appendedPath + File.separator + shortWorkfileName + "]", projectName, viewName, appendedPath);
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
                LOGGER.info("Creating branch archive for: [" + appendedPath + File.separator + shortWorkfileName + "]");
                java.io.File tempFile = java.io.File.createTempFile("QVCS", ".tmp");
                tempFile.deleteOnExit();
                try (java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                    outputStream.write(request.getBuffer());
                }
                if (archiveDirManagerForTranslucentBranch.createArchive(commandArgs, tempFile.getAbsolutePath(), response)) {
                    serverResponse = new ServerResponseCreateArchive();
                    ArchiveInfoInterface archiveInfo = archiveDirManagerForTranslucentBranch.getArchiveInfo(shortWorkfileName);
                    SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo(archiveInfo.getLogfileInfo(), File.separator,
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
                    LOGGER.warn("Creation of archive file failed for: [" + appendedPath + File.separator + shortWorkfileName + "]");
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Creation of archive file failed for: ["
                            + appendedPath + File.separator + shortWorkfileName + "]", projectName, viewName, appendedPath);
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
            LOGGER.warn(e.getLocalizedMessage(), e);
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        return returnObject;
    }
}
