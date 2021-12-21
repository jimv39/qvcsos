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

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryCoordinateListener;
import com.qumasoft.qvcslib.NotificationManager;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteFileData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a file.
 * @author Jim Voris
 */
public class ClientRequestDeleteFile implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestDeleteFile.class);
    private final ClientRequestDeleteFileData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestSetIsObsolete.
     *
     * @param data command line data, etc.
     */
    public ClientRequestDeleteFile(ClientRequestDeleteFileData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    /**
     * Delete the file.
     *
     * @param userName the user name.
     * @param response the response object that identifies the client.
     * @return a response object that we'll serialize back to the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        try {
            DirectoryCoordinate dc = new DirectoryCoordinate(projectName, branchName, appendedPath);
            SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo(shortWorkfileName);
            sourceControlBehaviorManager.deleteFile(projectName, branchName, appendedPath, shortWorkfileName);

            // Log the result.
            String activity = "User: [" + userName + "] deleted: ["
                    + Utility.formatFilenameForActivityJournal(projectName, branchName, appendedPath, shortWorkfileName) + "] file.";
            LOGGER.info(activity);

            // Notify listeners.
            DirectoryCoordinateListener directoryCoordinateListener = NotificationManager.getNotificationManager().getDirectoryCoordinateListener(response, dc);
            if (directoryCoordinateListener != null) {
                directoryCoordinateListener.notifySkinnyInfoListeners(skinnyInfo, new Remove(shortWorkfileName));
            }

            // Add an entry to the server journal file.
            ActivityJournalManager.getInstance().addJournalEntry(activity);

            // Send a response message so the client can treat this as a synchronous request.
            ServerResponseMessage message = new ServerResponseMessage("Delete file successful.", projectName, branchName, appendedPath,
                    ServerResponseMessage.LO_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        } catch (SQLException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }
}
