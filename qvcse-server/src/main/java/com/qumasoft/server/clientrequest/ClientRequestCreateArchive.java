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
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.logfileaction.AddFile;
import com.qumasoft.qvcslib.requestdata.ClientRequestCreateArchiveData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseCreateArchive;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.NotificationManager;
import com.qumasoft.server.ServerUtility;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request create archive.
 *
 * @author Jim Voris
 */
public class ClientRequestCreateArchive extends AbstractClientRequest {

    // AddFile our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestCreateArchive.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestCreateArchive.
     *
     * @param data the request data.
     */
    public ClientRequestCreateArchive(ClientRequestCreateArchiveData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject;
        ClientRequestCreateArchiveData clientRequestCreateArchiveData = (ClientRequestCreateArchiveData) getRequest();
        CreateArchiveCommandArgs commandArgs = clientRequestCreateArchiveData.getCommandArgs();
        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        String appendedPath = getRequest().getAppendedPath();
        java.io.File tempFile = ServerUtility.createTempFileFromBuffer("qvcsos-ca-", clientRequestCreateArchiveData.getBuffer());
        String shortWorkfileName = Utility.convertWorkfileNameToShortWorkfileName(commandArgs.getWorkfileName());

        try {
            // Add the file to the database.
            AtomicInteger mutableFileRevisionId = new AtomicInteger();
            Date workfileEditDate = commandArgs.getInputfileTimeStamp();
            Timestamp workfileEditTimestamp = new Timestamp(workfileEditDate.getTime());
            sourceControlBehaviorManager.addFile(branchName, projectName, appendedPath, shortWorkfileName, tempFile, workfileEditTimestamp,
                    clientRequestCreateArchiveData.getCommandArgs().getArchiveDescription(), mutableFileRevisionId);

            // Get the information the client needs...
            DirectoryCoordinate dc = new DirectoryCoordinate(projectName, branchName, appendedPath);
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(mutableFileRevisionId.get());
            LogfileInfo logfileInfo = functionalQueriesDAO.getLogfileInfo(dc, shortWorkfileName, skinnyInfo.getFileID());
            ServerResponseCreateArchive serverResponse = new ServerResponseCreateArchive();

            // Set the index so the client can match this response with the cached workfile.
            skinnyInfo.setCacheIndex(clientRequestCreateArchiveData.getIndex());
            serverResponse.setSkinnyLogfileInfo(skinnyInfo);
            serverResponse.setLogfileInfo(logfileInfo);
            serverResponse.setProjectName(projectName);
            serverResponse.setBranchName(branchName);
            serverResponse.setAppendedPath(appendedPath);
            returnObject = serverResponse;

            // Notify listeners.
            NotificationManager.getNotificationManager().notifySkinnyInfoListeners(dc, skinnyInfo, new AddFile());

            ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] storing first revision for ["
                    + Utility.formatFilenameForActivityJournal(projectName, branchName, appendedPath, shortWorkfileName) + "].");
            tempFile.delete();
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }
}
