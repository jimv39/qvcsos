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
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionForCompareData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetRevisionForCompare;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileRevision;
import com.qvcsos.server.datamodel.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get revision for compare.
 * @author Jim Voris
 */
public class ClientRequestGetRevisionForCompare implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetRevisionForCompare.class);
    private final ClientRequestGetRevisionForCompareData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestGetRevisionForCompare.
     *
     * @param data the request data.
     */
    public ClientRequestGetRevisionForCompare(ClientRequestGetRevisionForCompareData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseGetRevisionForCompare fetchedRevision;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        String revisionString = request.getRevisionString();
        java.io.File postgresFetchedFile = null;
        if (0 != request.getRevisionString().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
            String[] branchRevIdList = request.getRevisionString().split("\\.");
            Integer requestedRevisionId = Integer.valueOf(branchRevIdList[1]);
            postgresFetchedFile = getRevisionFromPostgres(requestedRevisionId);
        } else {
            postgresFetchedFile = getRevisionFromPostgres();
        }
        FileInputStream fileInputStream = null;
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            if (postgresFetchedFile != null) {
                // Need to read the resulting file into a buffer that we can send to the client.
                fileInputStream = new FileInputStream(postgresFetchedFile);
                byte[] workfileBuffer = new byte[(int) postgresFetchedFile.length()];
                Utility.readDataFromStream(workfileBuffer, fileInputStream);
                if (workfileBuffer != null) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    fetchedRevision = new ServerResponseGetRevisionForCompare();
                    fetchedRevision.setBuffer(workfileBuffer);

                    // Send back more info.
                    LogfileInfo logfileInfo = functionalQueriesDAO.getLogfileInfo(directoryCoordinate, shortWorkfileName, request.getFileID());
                    fetchedRevision.setLogfileInfo(logfileInfo);

                    fetchedRevision.setProjectName(projectName);
                    fetchedRevision.setBranchName(branchName);
                    fetchedRevision.setAppendedPath(appendedPath);
                    fetchedRevision.setShortWorkfileName(shortWorkfileName);
                    fetchedRevision.setRevisionString(request.getRevisionString());
                    returnObject = fetchedRevision;
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError("Failed to get revision " + revisionString + " for " + shortWorkfileName, projectName, branchName,
                            appendedPath);
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Archive not found for " + shortWorkfileName, projectName, branchName, appendedPath);
                returnObject = error;
            }
        } catch (FileNotFoundException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
            LOGGER.warn(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
            if (postgresFetchedFile != null) {
                postgresFetchedFile.delete();
            }
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    /**
     * Copied from the private method of the same name in ClientRequestGetRevision.
     * The code is very similar, but has some differences. The ideal would be to
     * refactor both into a single method shared by both classes.
     * @return A java.io.File containing the fetched file revision.
     */
    private File getRevisionFromPostgres() {
        java.io.File fetchedRevisionFile = null;
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(request.getProjectName());

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        String branchName = request.getBranchName();
        if (branchName == null) {
            branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
        }
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);

        DirectoryLocation directoryLocation = sourceControlBehaviorManager.findDirectoryLocationByAppendedPath(branch.getId(), request.getAppendedPath());
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        List<FileName> fileNameList = fileNameDAO.findByDirectoryIdAndFileName(directoryLocation.getDirectoryId(), request.getShortWorkfileName());

        if (!fileNameList.isEmpty()) {
            List<FileRevision> fileRevisionList = sourceControlBehaviorManager.getFileRevisionList(branch, fileNameList.get(0).getFileId());

            int fetchIndex = -1;
            if (0 == request.getRevisionString().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
                fetchIndex = 0;
            } else {
                int index = 0;
                String[] branchRevIdList = request.getRevisionString().split("\\.");
                Integer requestedRevisionId = Integer.valueOf(branchRevIdList[1]);
                for (FileRevision fileRevision : fileRevisionList) {
                    if (fileRevision.getId().intValue() == requestedRevisionId.intValue()) {
                        fetchIndex = index;
                        break;
                    }
                    index++;
                }
                if (fetchIndex == -1) {
                    LOGGER.warn("Requested revision: [{}] not found!", request.getRevisionString());
                }
            }

            if (fetchIndex >= 0) {
                try {
                    FileRevision fetchingRevision = fileRevisionList.get(fetchIndex);
                    String fetchedRevisionString = String.format("%d.%d", fetchingRevision.getBranchId(), fetchingRevision.getId());
                    request.setRevisionString(fetchedRevisionString);
                    fetchedRevisionFile = sourceControlBehaviorManager.getFileRevision(fileRevisionList.get(fetchIndex).getId());
                    LOGGER.info("File revision: [{}] for file: [{}] fetched from postgres returned in file: [{}]",
                            fetchedRevisionString, request.getShortWorkfileName(), fetchedRevisionFile.getAbsolutePath());
                } catch (SQLException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }
        return fetchedRevisionFile;
    }

    private File getRevisionFromPostgres(Integer requestedRevisionId) {
        java.io.File fetchedRevisionFile = null;
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        try {
            fetchedRevisionFile = sourceControlBehaviorManager.getFileRevision(requestedRevisionId);
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
        return fetchedRevisionFile;
    }
}
