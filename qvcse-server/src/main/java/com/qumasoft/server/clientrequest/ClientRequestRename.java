/*   Copyright 2004-2021 Jim Voris
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
import com.qumasoft.qvcslib.DirectoryCoordinateListener;
import com.qumasoft.qvcslib.NotificationManager;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.logfileaction.Rename;
import com.qumasoft.qvcslib.requestdata.ClientRequestRenameData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseRenameArchive;
import com.qumasoft.server.ActivityJournalManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileRevision;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rename an archive file.
 *
 * @author Jim Voris
 */
public class ClientRequestRename implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestRename.class);
    private final ClientRequestRenameData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestRename.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestRename(ClientRequestRenameData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    /**
     * Perform the rename operation.
     *
     * @param userName the user making the request.
     * @param response the response object that identifies the client connection.
     * @return an object we'll send back to the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String originalShortWorkfileName = request.getOriginalShortWorkfileName();
        String newShortWorkfileName = request.getNewShortWorkfileName();
        DirectoryCoordinate dc = new DirectoryCoordinate(request.getProjectName(), request.getBranchName(), request.getAppendedPath());
        Integer fileNameId = null;
        try {
            LOGGER.info("Rename file: project name: [{}] branch name: [{}] appended path: [{}]", projectName, branchName, appendedPath);
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);

            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            DirectoryCoordinateIds dcIds = functionalQueriesDAO.getDirectoryCoordinateIds(directoryCoordinate);

            // Find the file...
            Integer fileId = null;
            FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
            List<FileName> originalFileNameList = fileNameDAO.findByDirectoryIdAndFileName(dcIds.getDirectoryId(), originalShortWorkfileName);
            if (originalFileNameList.isEmpty()) {
                throw new QVCSRuntimeException("No Filename records found for [" + originalShortWorkfileName + "]");
            } else {
                fileId = originalFileNameList.get(0).getFileId();
            }

            fileNameId = sourceControlBehaviorManager.renameFile(dcIds.getBranchId(), fileId, newShortWorkfileName);

            if (fileNameId != null) {
                // Send a response to the user.
                ServerResponseRenameArchive serverResponseRenameArchive = new ServerResponseRenameArchive();
                serverResponseRenameArchive.setServerName(response.getServerName());
                serverResponseRenameArchive.setProjectName(projectName);
                serverResponseRenameArchive.setBranchName(branchName);
                serverResponseRenameArchive.setAppendedPath(appendedPath);
                serverResponseRenameArchive.setOldShortWorkfileName(originalShortWorkfileName);
                serverResponseRenameArchive.setNewShortWorkfileName(newShortWorkfileName);

                // Find the file's newest branch revision...
                FileRevision fileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(dcIds.getBranchId(), fileId);

                SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(fileRevision.getId());
                serverResponseRenameArchive.setSkinnyLogfileInfo(skinnyInfo);
                returnObject = serverResponseRenameArchive;

                // Add an entry to the server journal file.
                String logMessage = buildJournalEntry(userName);

                // Notify listeners.
                DirectoryCoordinateListener directoryCoordinateListener = NotificationManager.getNotificationManager().getDirectoryCoordinateListener(response, dc);
                if (directoryCoordinateListener != null) {
                    directoryCoordinateListener.notifySkinnyInfoListeners(skinnyInfo, new Rename(request.getOriginalShortWorkfileName()));
                }

                ActivityJournalManager.getInstance().addJournalEntry(logMessage);
                LOGGER.info(logMessage);
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("File not found for " + originalShortWorkfileName, projectName, branchName, appendedPath);
                returnObject = error;
            }
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to rename [" + originalShortWorkfileName + "] to [" + newShortWorkfileName
                    + "]. Exception string: " + e.getMessage(), projectName, branchName, appendedPath);
            returnObject = error;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private String buildJournalEntry(final String userName) {
        return "User: [" + userName + "] renamed file ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getBranchName(), request.getAppendedPath(), request.getOriginalShortWorkfileName())
                + "] to ["
                + Utility.formatFilenameForActivityJournal(request.getProjectName(), request.getBranchName(), request.getAppendedPath(), request.getNewShortWorkfileName()) + "]";
    }
}
