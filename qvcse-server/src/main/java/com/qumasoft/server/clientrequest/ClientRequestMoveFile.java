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
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.logfileaction.MoveFile;
import com.qumasoft.qvcslib.requestdata.ClientRequestMoveFileData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseMoveFile;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.NotificationManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileRevision;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Move an archive file.
 *
 * @author Jim Voris
 */
public class ClientRequestMoveFile extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestMoveFile.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestMoveFile.
     *
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestMoveFile(ClientRequestMoveFileData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    /**
     * Perform the move operation.
     *
     * @param userName the user making the request.
     * @param response the response object that identifies the client connection.
     * @return an object we'll send back to the client.
     */
    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);

        ClientRequestMoveFileData clientRequestMoveFileData = (ClientRequestMoveFileData) getRequest();
        AbstractServerResponse returnObject;
        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        String shortWorkfileName = getRequest().getShortWorkfileName();
        String originalAppendedPath = clientRequestMoveFileData.getOriginalAppendedPath();

        try {
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

            DirectoryCoordinate originCoordinate = new DirectoryCoordinate(projectName, branchName, originalAppendedPath);
            DirectoryCoordinateIds originIds = functionalQueriesDAO.getDirectoryCoordinateIds(originCoordinate);

            DirectoryCoordinate destinationCoordinate = new DirectoryCoordinate(projectName, branchName, clientRequestMoveFileData.getNewAppendedPath());
            DirectoryCoordinateIds destinationIds = functionalQueriesDAO.getDirectoryCoordinateIds(destinationCoordinate);

            // Find the origin file...
            Integer fileId = null;
            Integer fileNameId = null;
            FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
            List<FileName> originFileNameList = fileNameDAO.findByDirectoryIdAndFileName(originIds.getDirectoryId(), shortWorkfileName);
            if (originFileNameList.isEmpty()) {
                throw new QVCSRuntimeException("No Filename records found for [" + shortWorkfileName + "]");
            } else {
                fileId = originFileNameList.get(0).getFileId();
                for (FileName fileName : originFileNameList) {
                    if (fileName.getBranchId() == originIds.getBranchId()) {
                        fileNameId = fileName.getId();
                    }
                }
                if (fileNameId == null) {
                    // Need to find the 1st FileName record on an ancestor branch. The branchList returned from getBranchAncestryList is from deepest branch toward the trunk.
                    List<Branch> branchList = functionalQueriesDAO.getBranchAncestryList(originIds.getBranchId());
                    Map<Integer, FileName> fileNameMap = new HashMap<>();
                    for (FileName fileName : originFileNameList) {
                        fileNameMap.put(fileName.getBranchId(), fileName);
                    }
                    for (Branch branch : branchList) {
                        FileName fileName = fileNameMap.get(branch.getId());
                        if (fileName != null) {
                            fileNameId = fileName.getId();
                            break;
                        }
                    }
                    if (fileNameId == null) {
                        throw new QVCSRuntimeException("No matching FileName record found for requested branchId: [" + originIds.getBranchId() + "] for file: [" + shortWorkfileName + "]");
                    }
                }
            }

            // Find the file's tip revision...
            FileRevision fileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(originIds.getBranchId(), fileId);

            // And move the file...
            fileNameId = sourceControlBehaviorManager.moveFile(originIds.getBranchId(), fileNameId, destinationIds.getDirectoryId());

            // Send a response to the user (note that a notification will be sent before this response).
            if (fileNameId != null) {
                Properties fakeProperties = new Properties();
                fakeProperties.setProperty("QVCS_IGNORECASEFLAG", QVCSConstants.QVCS_NO);
                ServerResponseMoveFile serverResponseMoveFile = new ServerResponseMoveFile();
                serverResponseMoveFile.setServerName(response.getServerName());
                serverResponseMoveFile.setProjectName(projectName);
                serverResponseMoveFile.setBranchName(branchName);
                serverResponseMoveFile.setProjectProperties(fakeProperties);
                serverResponseMoveFile.setOriginAppendedPath(originalAppendedPath);
                serverResponseMoveFile.setDestinationAppendedPath(clientRequestMoveFileData.getNewAppendedPath());
                serverResponseMoveFile.setShortWorkfileName(shortWorkfileName);

                SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(fileRevision.getId());
                serverResponseMoveFile.setSkinnyLogfileInfo(skinnyInfo);
                returnObject = serverResponseMoveFile;

                // Add an entry to the server journal file.
                String logMessage = buildJournalEntry(userName);

                // Notify listeners.
                NotificationManager.getNotificationManager().notifySkinnyInfoListeners(originCoordinate, skinnyInfo, new MoveFile(originalAppendedPath,
                        clientRequestMoveFileData.getNewAppendedPath()));

                ActivityJournalManager.getInstance().addJournalEntry(logMessage);
                LOGGER.info(logMessage);
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Data not found for " + shortWorkfileName, projectName, branchName, originalAppendedPath);
                returnObject = error;
            }
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to move " + shortWorkfileName + " from " + originalAppendedPath + " to "
                    + clientRequestMoveFileData.getNewAppendedPath() + ". Exception string: " + e.getMessage(), projectName, branchName, originalAppendedPath);
            returnObject = error;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }

    private String buildJournalEntry(final String userName) {
        ClientRequestMoveFileData clientRequestMoveFileData = (ClientRequestMoveFileData) getRequest();
        return "User: [" + userName + "] moved file ["
                + Utility.formatFilenameForActivityJournal(getRequest().getProjectName(), getRequest().getBranchName(), clientRequestMoveFileData.getOriginalAppendedPath(),
                        getRequest().getShortWorkfileName()) + "] to ["
                + Utility.formatFilenameForActivityJournal(getRequest().getProjectName(), getRequest().getBranchName(), clientRequestMoveFileData.getNewAppendedPath(),
                        getRequest().getShortWorkfileName()) + "].";
    }
}
