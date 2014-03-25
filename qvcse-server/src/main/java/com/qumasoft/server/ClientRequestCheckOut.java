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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadOnlyViewInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerReadWriteViewInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckOutData;
import com.qumasoft.qvcslib.response.ServerResponseCheckOut;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request check out.
 * @author Jim Voris
 */
public class ClientRequestCheckOut implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestCheckOutData request;

    /**
     * Creates a new instance of ClientRequestCheckOutRevision.
     *
     * @param data the request data.
     */
    public ClientRequestCheckOut(ClientRequestCheckOutData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseCheckOut serverResponse;
        ServerResponseInterface returnObject = null;
        CheckOutCommandArgs commandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        FileInputStream fileInputStream = null;
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            ArchiveInfoInterface logfile = archiveDirManager.getArchiveInfo(commandArgs.getShortWorkfileName());
            if ((logfile != null) && (archiveDirManager instanceof ArchiveDirManagerReadWriteViewInterface)) {
                java.io.File tempFile = java.io.File.createTempFile("QVCS", ".tmp");
                if (logfile.checkOutRevision(commandArgs, tempFile.getAbsolutePath())) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseCheckOut();

                    // Need to read the resulting file into a buffer that we can send to the client.
                    fileInputStream = new FileInputStream(tempFile);
                    byte[] buffer = new byte[(int) tempFile.length()];
                    Utility.readDataFromStream(buffer, fileInputStream);
                    serverResponse.setBuffer(buffer);

                    LogFileInterface logFileInterface = (LogFileInterface) logfile;
                    serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator, logFileInterface.getIsObsolete(),
                            logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                    serverResponse.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                    serverResponse.setProjectName(projectName);
                    serverResponse.setViewName(viewName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setRevisionString(commandArgs.getRevisionString());
                    serverResponse.setLabelString(commandArgs.getLabel());
                    LOGGER.log(Level.INFO, "Checked out " + commandArgs.getShortWorkfileName() + " revision: " + commandArgs.getRevisionString());
                    if (logfile.getAttributes().getIsExpandKeywords()) {
                        serverResponse.setLogfileInfo(logfile.getLogfileInfo());
                    }
                    returnObject = serverResponse;

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry(buildJournalEntry(userName, logfile));
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Failed to checkout revision " + commandArgs.getRevisionString() + " for "
                            + logfile.getShortWorkfileName(), projectName,
                            viewName, appendedPath);
                    returnObject = error;
                }
                tempFile.delete();
            } else {
                if (logfile == null) {
                    // Explain the error.
                    ServerResponseMessage message = new ServerResponseMessage("Archive not found for " + commandArgs.getShortWorkfileName(), projectName, viewName, appendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    returnObject = message;
                } else {
                    if (archiveDirManager instanceof ArchiveDirManagerReadOnlyViewInterface) {
                        // Explain the error.
                        ServerResponseMessage message = new ServerResponseMessage("Checkout not allowed for read-only view.", projectName, viewName, appendedPath,
                                ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                        returnObject = message;
                    }
                }
            }
        } catch (QVCSException | IOException e) {
            LOGGER.log(Level.WARNING, "Caught exception on check-out: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            returnObject = message;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
        return returnObject;
    }

    private String buildJournalEntry(final String userName, final ArchiveInfoInterface logfile) {
        CheckOutCommandArgs commandArgs = request.getCommandArgs();
        return "User: [" + userName + "] checked-out revision [" + commandArgs.getRevisionString() + "] for ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getViewName(), request.getAppendedPath(), logfile.getShortWorkfileName()) + "].";
    }
}
