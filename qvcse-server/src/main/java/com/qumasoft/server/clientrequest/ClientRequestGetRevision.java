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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetRevision;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ServerUtility;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get a revision.
 * @author Jim Voris
 */
public class ClientRequestGetRevision implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetRevision.class);
    private final ClientRequestGetRevisionData request;

    /**
     * Creates a new instance of ClientRequestFetchFileRevision.
     *
     * @param data the request data.
     */
    public ClientRequestGetRevision(ClientRequestGetRevisionData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseGetRevision serverResponse;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        GetRevisionCommandArgs commandArgs = request.getCommandArgs();
        FileInputStream fileInputStream = null;
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            ArchiveInfoInterface logfile = archiveDirManager.getArchiveInfo(commandArgs.getShortWorkfileName());
            if (logfile != null) {
                java.io.File tempFile = java.io.File.createTempFile("QVCS", ".tmp");
                if (logfile.getRevision(commandArgs, tempFile.getAbsolutePath())) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseGetRevision();

                    // Need to read the resulting file into a buffer that we can send to the client.
                    fileInputStream = new FileInputStream(tempFile);
                    byte[] buffer = new byte[(int) tempFile.length()];
                    Utility.readDataFromStream(buffer, fileInputStream);
                    serverResponse.setBuffer(buffer);

                    LogFileInterface logFileInterface = (LogFileInterface) logfile;
                    serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator,
                            logFileInterface.getDefaultRevisionDigest(), logfile.getShortWorkfileName(), logfile.getIsOverlap()));
                    serverResponse.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                    serverResponse.setProjectName(projectName);
                    serverResponse.setViewName(viewName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setRevisionString(commandArgs.getRevisionString());
                    serverResponse.setLabelString(commandArgs.getLabel());
                    serverResponse.setOverwriteBehavior(commandArgs.getOverwriteBehavior());
                    serverResponse.setTimestampBehavior(commandArgs.getTimestampBehavior());

                    // Figure out the timestamp that we send back.
                    ServerUtility.setTimestampData(logfile, serverResponse, commandArgs.getTimestampBehavior());

                    // Send back the logfile info if it's needed for
                    // keyword expansion.
                    if (logfile.getAttributes().getIsExpandKeywords()) {
                        serverResponse.setLogfileInfo(logfile.getLogfileInfo());
                    }
                    returnObject = serverResponse;
                } else {
                    // Return a command error.
                    ServerResponseError error;
                    if ((commandArgs.getFailureReason() != null) && (commandArgs.getFailureReason().length() > 0)) {
                        error = new ServerResponseError("Failed to get revision for " + logfile.getShortWorkfileName() + ". " + commandArgs.getFailureReason(), projectName,
                                viewName, appendedPath);
                    } else {
                        error = new ServerResponseError("Failed to get revision " + commandArgs.getRevisionString() + " for " + logfile.getShortWorkfileName(), projectName,
                                viewName, appendedPath);
                    }
                    returnObject = error;
                }
                tempFile.delete();
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Archive not found for " + commandArgs.getShortWorkfileName(), projectName, viewName, appendedPath);
                returnObject = error;
            }
        } catch (QVCSException | IOException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            returnObject = message;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        return returnObject;
    }
}
