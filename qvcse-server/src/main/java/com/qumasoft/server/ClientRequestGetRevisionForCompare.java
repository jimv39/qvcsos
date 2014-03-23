//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionForCompareData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseGetRevisionForCompare;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.Utility;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Get revision for compare.
 * @author Jim Voris
 */
public class ClientRequestGetRevisionForCompare implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestGetRevisionForCompareData request;

    /**
     * Creates a new instance of ClientRequestGetRevisionForCompare.
     *
     * @param data the request data.
     */
    public ClientRequestGetRevisionForCompare(ClientRequestGetRevisionForCompareData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseGetRevisionForCompare fetchedRevision;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        String revisionString = request.getRevisionString();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            ArchiveInfoInterface logfile = archiveDirManager.getArchiveInfo(shortWorkfileName);
            if (logfile != null) {
                byte[] workfileBuffer = logfile.getRevisionAsByteArray(revisionString);
                if (workfileBuffer != null) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    fetchedRevision = new ServerResponseGetRevisionForCompare();

                    fetchedRevision.setBuffer(workfileBuffer);

                    if (request.getIsLogfileInfoRequired()) {
                        LogFileInterface logFileInterface = (LogFileInterface) logfile;
                        LogfileInfo logfileInfo = new LogfileInfo(logFileInterface.getLogfileInfo().getLogFileHeaderInfo(), logFileInterface.getRevisionInformation(),
                                logFileInterface.getLogfileInfo().getFileID(), logFileInterface.getFullArchiveFilename());
                        fetchedRevision.setLogfileInfo(logfileInfo);
                    } else {
                        fetchedRevision.setLogfileInfo(null);
                    }

                    fetchedRevision.setProjectName(projectName);
                    fetchedRevision.setViewName(viewName);
                    fetchedRevision.setAppendedPath(appendedPath);
                    fetchedRevision.setShortWorkfileName(shortWorkfileName);
                    fetchedRevision.setRevisionString(revisionString);
                    returnObject = fetchedRevision;
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Failed to get revision " + revisionString + " for " + shortWorkfileName, projectName, viewName,
                            appendedPath);
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Archive not found for " + shortWorkfileName, projectName, viewName, appendedPath);
                returnObject = error;
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return returnObject;
    }
}
