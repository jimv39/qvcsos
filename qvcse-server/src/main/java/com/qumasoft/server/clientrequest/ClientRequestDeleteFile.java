/*   Copyright 2004-2022 Jim Voris
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
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.logfileaction.AddFile;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteFileData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.NotificationManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a file.
 * @author Jim Voris
 */
public class ClientRequestDeleteFile extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestDeleteFile.class);
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
        setRequest(data);
    }

    /**
     * Delete the file.
     *
     * @param userName the user name.
     * @param response the response object that identifies the client.
     * @return a response object that we'll serialize back to the client.
     */
    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject;
        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        String appendedPath = getRequest().getAppendedPath();
        String shortWorkfileName = getRequest().getShortWorkfileName();
        try {
            DirectoryCoordinate dc = new DirectoryCoordinate(projectName, branchName, appendedPath);
            DirectoryCoordinate cemeteryDc = new DirectoryCoordinate(projectName, branchName, QVCSConstants.QVCSOS_CEMETERY_FAKE_APPENDED_PATH);
            AtomicInteger newRevisionId = new AtomicInteger();

            sourceControlBehaviorManager.deleteFile(projectName, branchName, appendedPath, shortWorkfileName, newRevisionId);

            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(newRevisionId.get());

            // Log the result.
            String activity = "User: [" + userName + "] deleted: ["
                    + Utility.formatFilenameForActivityJournal(projectName, branchName, appendedPath, shortWorkfileName) + "] file.";
            LOGGER.info(activity);

            // Notify listeners.
            NotificationManager.getNotificationManager().notifySkinnyInfoListeners(dc, skinnyInfo, new Remove(shortWorkfileName));

            // Notify cemetery listeners.
            NotificationManager.getNotificationManager().notifySkinnyInfoListeners(cemeteryDc, skinnyInfo, new AddFile());

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
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }
}
