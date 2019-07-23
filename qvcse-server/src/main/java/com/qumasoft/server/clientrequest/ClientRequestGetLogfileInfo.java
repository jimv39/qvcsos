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
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetLogfileInfoData;
import com.qumasoft.qvcslib.response.ServerResponseGetLogfileInfo;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get Logfile info.
 * @author Jim Voris
 */
public class ClientRequestGetLogfileInfo implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetLogfileInfo.class);
    private final ClientRequestGetLogfileInfoData request;

    /**
     * Creates a new instance of ClientRequestLock.
     *
     * @param data instance of super class that contains command line arguments, etc.
     */
    public ClientRequestGetLogfileInfo(ClientRequestGetLogfileInfoData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseGetLogfileInfo serverResponse;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            if (directoryManager != null) {
                ArchiveInfoInterface logfile = directoryManager.getArchiveInfo(shortWorkfileName);
                if (logfile != null) {
                    // Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseGetLogfileInfo();

                    serverResponse.setLogfileInfo(logfile.getLogfileInfo());
                    serverResponse.setProjectName(projectName);
                    serverResponse.setBranchName(branchName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setShortWorkfileName(shortWorkfileName);
                    returnObject = serverResponse;
                } else {
                    ServerResponseMessage message = new ServerResponseMessage("Archive not found for " + shortWorkfileName, projectName, branchName, appendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(shortWorkfileName);
                    returnObject = message;
                }
            } else {
                ServerResponseMessage message = new ServerResponseMessage("Directory no longer exists for " + shortWorkfileName, projectName, branchName, appendedPath,
                        ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName(shortWorkfileName);
                returnObject = message;
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            ServerResponseMessage message = new ServerResponseMessage("Caught exception trying to get detailed information for " + shortWorkfileName
                    + ". Exception string: " + e.getMessage(),
                    projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        return returnObject;
    }
}
