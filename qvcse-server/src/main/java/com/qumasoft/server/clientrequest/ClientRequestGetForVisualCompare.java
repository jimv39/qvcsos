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
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetForVisualCompareData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetForVisualCompare;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get for visual compare.
 * @author Jim Voris
 */
public class ClientRequestGetForVisualCompare implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetForVisualCompare.class);
    private final ClientRequestGetForVisualCompareData request;

    /**
     * Creates a new instance of ClientRequestFetchFileRevision.
     *
     * @param data the request data.
     */
    public ClientRequestGetForVisualCompare(ClientRequestGetForVisualCompareData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseGetForVisualCompare fetchedFile;
        ServerResponseInterface returnObject;
        GetRevisionCommandArgs commandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String viewName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
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
                    fetchedFile = new ServerResponseGetForVisualCompare();

                    // Need to read the resulting file into a buffer that we can send to the client.
                    fileInputStream = new FileInputStream(tempFile);
                    byte[] buffer = new byte[(int) tempFile.length()];
                    fileInputStream.read(buffer);
                    fetchedFile.setBuffer(buffer);

                    LogFileInterface logFileInterface = (LogFileInterface) logfile;
                    LogfileInfo logfileInfo = new LogfileInfo(logFileInterface.getLogfileInfo().getLogFileHeaderInfo(), logFileInterface.getRevisionInformation(),
                            logFileInterface.getLogfileInfo().getFileID(), logFileInterface.getFullArchiveFilename());

                    fetchedFile.setLogfileInfo(logfileInfo);
                    fetchedFile.setClientOutputFileName(commandArgs.getOutputFileName());
                    fetchedFile.setFullWorkfileName(commandArgs.getFullWorkfileName());
                    fetchedFile.setProjectName(projectName);
                    fetchedFile.setViewName(viewName);
                    fetchedFile.setAppendedPath(appendedPath);
                    fetchedFile.setRevisionString(commandArgs.getRevisionString());
                    tempFile.delete();
                    returnObject = fetchedFile;
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Failed to get revision " + commandArgs.getRevisionString() + " for " + logfile.getShortWorkfileName(),
                            projectName, viewName, appendedPath);
                    returnObject = error;
                }
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
