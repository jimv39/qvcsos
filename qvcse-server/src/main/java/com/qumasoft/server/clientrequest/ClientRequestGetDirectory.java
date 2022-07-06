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
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.GetDirectoryCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetDirectoryData;
import com.qumasoft.qvcslib.response.ServerResponseGetRevision;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.DirectoryLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request get directory.
 * @author Jim Voris
 */
public class ClientRequestGetDirectory implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetDirectory.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;
    private final ClientRequestGetDirectoryData request;

    /**
     * Creates a new instance of ClientRequestGetDirectory.
     *
     * @param data the request data.
     */
    public ClientRequestGetDirectory(ClientRequestGetDirectoryData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        GetDirectoryCommandArgs commandArgs = request.getCommandArgs();
        String appendedPath = request.getAppendedPath();
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
        try {
            FunctionalQueriesDAOImpl functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            DirectoryCoordinateIds ids = functionalQueriesDAO.getDirectoryCoordinateIds(directoryCoordinate);
            List<Branch> branchArray = functionalQueriesDAO.getBranchAncestryList(ids.getBranchId());

            List<String> appendedPathList = new ArrayList<>();
            List<DirectoryCoordinateIds> dcIdsList = new ArrayList<>();

            // We have to do this directory at least...
            appendedPathList.add(appendedPath);
            dcIdsList.add(ids);

            if (commandArgs.getRecurseFlag()) {
                addChildDirectories(appendedPathList, appendedPath, dcIdsList, ids.getDirectoryLocationId(), branchArray);
            }

            processDirectoryCollection(commandArgs, appendedPathList, dcIdsList, response);
        } finally {
            LOGGER.info("Completed get directory for: [{}]", appendedPath);
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private void addChildDirectories(List<String> appendedPathList, String appendedPath, List<DirectoryCoordinateIds> dcIds, Integer parentDirectoryLocationId, List<Branch> branchArray) {
        LOGGER.info("addChildDirectories: appendedPath: [{}]", appendedPath);
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<DirectoryLocation> directoryLocationList = functionalQueriesDAO.findChildDirectoryLocations(branchArray, parentDirectoryLocationId);
        if (directoryLocationList != null) {
            for (DirectoryLocation dl : directoryLocationList) {
                DirectoryCoordinateIds dlId = new DirectoryCoordinateIds(dcIds.get(0).getProjectId(), dl.getBranchId(), dl.getDirectoryId(), dl.getId(), null, new TreeMap<>());
                dcIds.add(dlId);
                String newAppendedPath;
                if (appendedPath.length() == 0) {
                    newAppendedPath = dl.getDirectorySegmentName();
                } else {
                    newAppendedPath = appendedPath + File.separator + dl.getDirectorySegmentName();
                }
                appendedPathList.add(newAppendedPath);
                addChildDirectories(appendedPathList, newAppendedPath, dcIds, dlId.getDirectoryLocationId(), branchArray);
            }
        }
    }

    private void processDirectoryCollection(GetDirectoryCommandArgs commandArgs, List<String> appendedPathList, List<DirectoryCoordinateIds> dcIds, ServerResponseFactoryInterface response) {
        LOGGER.info("processDirectoryCollection");
        if (appendedPathList.size() != dcIds.size()) {
            throw new QVCSRuntimeException("######## appendedPath list and directory coordinate ids list are not the same size!!!");
        }
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        for (int i = 0; i < appendedPathList.size(); i++) {
            List<SkinnyLogfileInfo> skinnyList = functionalQueriesDAO.getSkinnyLogfileInfo(dcIds.get(i).getBranchId(), dcIds.get(i).getDirectoryId());
            for (SkinnyLogfileInfo skinnyInfo : skinnyList) {
                sendToClient(commandArgs, appendedPathList.get(i), dcIds.get(i), skinnyInfo, response);
            }
        }
    }

    private void sendToClient(GetDirectoryCommandArgs commandArgs, String appendedPath, DirectoryCoordinateIds dcIds, SkinnyLogfileInfo skinnyInfo, ServerResponseFactoryInterface response) {
        FileInputStream fileInputStream = null;
        try {
            SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            java.io.File fetchedRevisionFile = sourceControlBehaviorManager.getFileRevision(skinnyInfo.getFileRevisionId());

            ServerResponseGetRevision serverResponse = new ServerResponseGetRevision();

            // Need to read the resulting file into a buffer that we can send to the client.
            fileInputStream = new FileInputStream(fetchedRevisionFile);
            byte[] buffer = new byte[(int) fetchedRevisionFile.length()];
            Utility.readDataFromStream(buffer, fileInputStream);
            String fullWorkfileName = commandArgs.getWorkfileBaseDirectory() + File.separator + appendedPath + File.separator + skinnyInfo.getShortWorkfileName();
            serverResponse.setBuffer(buffer);
            serverResponse.setSkinnyLogfileInfo(skinnyInfo);
            serverResponse.setClientWorkfileName(fullWorkfileName);
            serverResponse.setShortWorkfileName(skinnyInfo.getShortWorkfileName());
            serverResponse.setProjectName(request.getProjectName());
            serverResponse.setBranchName(request.getBranchName());
            serverResponse.setAppendedPath(appendedPath);
            serverResponse.setRevisionString(skinnyInfo.getDefaultRevisionString());
            serverResponse.setOverwriteBehavior(commandArgs.getOverwriteBehavior());
            serverResponse.setTimestampBehavior(commandArgs.getTimeStampBehavior());

            // Send back more info.
            LogfileInfo logfileInfo = functionalQueriesDAO.getLogfileInfo(dcIds, skinnyInfo.getShortWorkfileName(), skinnyInfo.getFileID());
            serverResponse.setLogfileInfo(logfileInfo);
            // Send a message to indicate that we're getting the file.
            ServerResponseMessage message = new ServerResponseMessage("Retrieving revision " + skinnyInfo.getDefaultRevisionString() + " for " + appendedPath + File.separator
                    + skinnyInfo.getShortWorkfileName() + " from server.", request.getProjectName(), request.getBranchName(), appendedPath, ServerResponseMessage.MEDIUM_PRIORITY);
            message.setShortWorkfileName(skinnyInfo.getShortWorkfileName());
            response.createServerResponse(message);

            // Send the response.
            response.createServerResponse(serverResponse);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientRequestGetDirectory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | SQLException ex) {
            java.util.logging.Logger.getLogger(ClientRequestGetDirectory.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }
}
