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
import com.qumasoft.qvcslib.requestdata.ClientRequestGetMostRecentActivityData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseGetMostRecentActivity;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.Utility;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Get most recent activity.
 * @author Jim Voris
 */
public class ClientRequestGetMostRecentActivity implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestGetMostRecentActivityData request;

    /**
     * Creates a new instance of ClientRequestFetchFileRevision.
     *
     * @param data instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestGetMostRecentActivity(ClientRequestGetMostRecentActivityData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseGetMostRecentActivity serverResponse;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            if (archiveDirManager != null) {
                serverResponse = new ServerResponseGetMostRecentActivity();
                serverResponse.setProjectName(projectName);
                serverResponse.setViewName(viewName);
                serverResponse.setAppendedPath(appendedPath);
                serverResponse.setMostRecentActivityDate(archiveDirManager.getMostRecentActivityDate());
                returnObject = serverResponse;
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Directory manager not found for  ", projectName, viewName, appendedPath);
                returnObject = error;
            }
        } catch (QVCSException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            ServerResponseError error = new ServerResponseError("Directory manager not found for  ", projectName, viewName, appendedPath);
            returnObject = error;
        }
        return returnObject;
    }
}
