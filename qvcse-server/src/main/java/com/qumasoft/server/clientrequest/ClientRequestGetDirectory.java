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
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.GetDirectoryCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetDirectoryData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseGetDirectory;
import com.qumasoft.qvcslib.response.ServerResponseGetRevision;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesForReadOnlyBranchesDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesForReleaseBranchesDAO;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesForReadOnlyBranchesDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesForReleaseBranchesDAOImpl;
import com.qvcsos.server.dataaccess.impl.TagDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.Tag;
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
public class ClientRequestGetDirectory extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetDirectory.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestGetDirectory.
     *
     * @param data the request data.
     */
    public ClientRequestGetDirectory(ClientRequestGetDirectoryData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject = new ServerResponseGetDirectory();
        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        ClientRequestGetDirectoryData clientRequestGetDirectoryData = (ClientRequestGetDirectoryData) getRequest();
        GetDirectoryCommandArgs commandArgs = clientRequestGetDirectoryData.getCommandArgs();
        String appendedPath = getRequest().getAppendedPath();
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

            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch branch = branchDAO.findById(ids.getBranchId());

            if (commandArgs.getRecurseFlag()) {
                if (null == branch.getBranchTypeId()) {
                    throw new QVCSRuntimeException("Missing branch type!!");
                } else {
                    // <editor-fold>
                    switch (branch.getBranchTypeId()) {
                        case 1 -> {
                            addChildDirectoriesForTrunkOrFeatureBranch(appendedPathList, appendedPath, dcIdsList, ids.getDirectoryLocationId(), branchArray);
                        }
                        case 2 -> {
                            addChildDirectoriesForTrunkOrFeatureBranch(appendedPathList, appendedPath, dcIdsList, ids.getDirectoryLocationId(), branchArray);
                        }
                        case 3 -> {
                            addChildDirectoriesForReadOnlyBranch(branch, appendedPathList, appendedPath, dcIdsList, ids.getDirectoryLocationId(), branchArray);
                        }
                        case 4 -> {
                            addChildDirectoriesForReleaseBranch(branch, appendedPathList, appendedPath, dcIdsList, ids.getDirectoryLocationId(), branchArray);
                        }
                        default -> {
                            throw new QVCSRuntimeException("Unsupported branch type: " + branch.getBranchTypeId());
                        }
                    }
                    // </editor-fold>
                }
            }

            processDirectoryCollection(branch, commandArgs, appendedPathList, dcIdsList, response);
        } finally {
            LOGGER.info("Completed get directory for: [{}]", appendedPath);
        }
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }

    private void addChildDirectoriesForTrunkOrFeatureBranch(List<String> appendedPathList, String appendedPath, List<DirectoryCoordinateIds> dcIdsList, Integer parentDirectoryLocationId,
            List<Branch> branchArray) {
        LOGGER.info("addChildDirectoriesForTrunkOrFeatureBranch: appendedPath: [{}]", appendedPath);
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<DirectoryLocation> directoryLocationList = functionalQueriesDAO.findChildDirectoryLocations(branchArray, parentDirectoryLocationId);
        if (directoryLocationList != null) {
            for (DirectoryLocation dl : directoryLocationList) {
                DirectoryCoordinateIds dcIds = new DirectoryCoordinateIds(dcIdsList.get(0).getProjectId(), dl.getBranchId(), dl.getDirectoryId(), dl.getId(), null, new TreeMap<>());
                dcIdsList.add(dcIds);
                String newAppendedPath;
                if (appendedPath.length() == 0) {
                    newAppendedPath = dl.getDirectorySegmentName();
                } else {
                    newAppendedPath = appendedPath + File.separator + dl.getDirectorySegmentName();
                }
                appendedPathList.add(newAppendedPath);
                LOGGER.debug("Adding appended path: [{}}]", newAppendedPath);
                addChildDirectoriesForTrunkOrFeatureBranch(appendedPathList, newAppendedPath, dcIdsList, dcIds.getDirectoryLocationId(), branchArray);
            }
        }
    }

    private void addChildDirectoriesForReadOnlyBranch(Branch branch, List<String> appendedPathList, String appendedPath, List<DirectoryCoordinateIds> dcIdsList, Integer parentDirectoryLocationId,
            List<Branch> branchArray) {
        LOGGER.info("addChildDirectoriesForReadOnlyBranch: appendedPath: [{}]", appendedPath);
        FunctionalQueriesForReadOnlyBranchesDAO functionalQueriesForReadOnlyBranchesDAO = new FunctionalQueriesForReadOnlyBranchesDAOImpl(schemaName);

        TagDAO tagDAO = new TagDAOImpl(schemaName);
        Tag tag = tagDAO.findById(branch.getTagId());
        Integer boundingCommitId = tag.getCommitId();

        List<DirectoryLocation> directoryLocationList = functionalQueriesForReadOnlyBranchesDAO.findChildDirectoryLocationsForReadOnlyBranch(branch,
                branchArray, boundingCommitId, parentDirectoryLocationId);
        if (directoryLocationList != null) {
            for (DirectoryLocation dl : directoryLocationList) {
                DirectoryCoordinateIds dlId = new DirectoryCoordinateIds(dcIdsList.get(0).getProjectId(), dl.getBranchId(), dl.getDirectoryId(), dl.getId(), null, new TreeMap<>());
                if (dl.getBranchId() > branch.getId()) {
                    throw new QVCSRuntimeException("Branch id mismatch!!!");
                }
                dcIdsList.add(dlId);
                String newAppendedPath;
                if (appendedPath.length() == 0) {
                    newAppendedPath = dl.getDirectorySegmentName();
                } else {
                    newAppendedPath = appendedPath + File.separator + dl.getDirectorySegmentName();
                }
                appendedPathList.add(newAppendedPath);
                LOGGER.debug("Adding appended path: [{}}]", newAppendedPath);
                addChildDirectoriesForReadOnlyBranch(branch, appendedPathList, newAppendedPath, dcIdsList, dlId.getDirectoryLocationId(), branchArray);
            }
        }
    }

    private void addChildDirectoriesForReleaseBranch(Branch branch, List<String> appendedPathList, String appendedPath, List<DirectoryCoordinateIds> dcIdsList, Integer parentDirectoryLocationId,
            List<Branch> branchArray) {
        LOGGER.debug("addChildDirectoriesForReleaseBranch: appendedPath: [{}]", appendedPath);
        FunctionalQueriesForReleaseBranchesDAO functionalQueriesForReleaseBranchesDAO = new FunctionalQueriesForReleaseBranchesDAOImpl(schemaName);
        List<DirectoryLocation> directoryLocationList = functionalQueriesForReleaseBranchesDAO.findChildDirectoryLocationsForBranch(branch,
                branchArray, branch.getCommitId(), parentDirectoryLocationId);
        if (directoryLocationList != null) {
            for (DirectoryLocation dl : directoryLocationList) {
                DirectoryCoordinateIds dlId = new DirectoryCoordinateIds(dcIdsList.get(0).getProjectId(), dl.getBranchId(), dl.getDirectoryId(), dl.getId(), null, new TreeMap<>());
                if (dl.getBranchId() > branch.getId()) {
                    throw new QVCSRuntimeException("Branch id mismatch!!!");
                }
                dcIdsList.add(dlId);
                String newAppendedPath;
                if (appendedPath.length() == 0) {
                    newAppendedPath = dl.getDirectorySegmentName();
                } else {
                    newAppendedPath = appendedPath + File.separator + dl.getDirectorySegmentName();
                }
                appendedPathList.add(newAppendedPath);
                LOGGER.debug("Adding appended path: [{}}]", newAppendedPath);
                addChildDirectoriesForReleaseBranch(branch, appendedPathList, newAppendedPath, dcIdsList, dlId.getDirectoryLocationId(), branchArray);
            }
        }
    }

    private void processDirectoryCollection(Branch branch, GetDirectoryCommandArgs commandArgs, List<String> appendedPathList, List<DirectoryCoordinateIds> dcIds,
            ServerResponseFactoryInterface response) {
        LOGGER.info("processDirectoryCollection");
        if (appendedPathList.size() != dcIds.size()) {
            throw new QVCSRuntimeException("######## appendedPath list and directory coordinate ids list are not the same size!!!");
        }
        // <editor-fold>
        switch (branch.getBranchTypeId()) {
            case 1 -> {
                processDirectoryCollectionForTrunkOrFeatureBranch(branch, commandArgs, appendedPathList, dcIds, response);
            }
            case 2 -> {
                processDirectoryCollectionForTrunkOrFeatureBranch(branch, commandArgs, appendedPathList, dcIds, response);
            }
            case 3 -> {
                processDirectoryCollectionForReadOnlyBranch(branch, commandArgs, appendedPathList, dcIds, response);
            }
            case 4 -> {
                processDirectoryCollectionForReleaseBranch(branch, commandArgs, appendedPathList, dcIds, response);
            }
            default -> {
                throw new QVCSRuntimeException("Unsupported branch type: " + branch.getBranchTypeId());
            }
        }
        // </editor-fold>
    }

    private void processDirectoryCollectionForTrunkOrFeatureBranch(Branch branch, GetDirectoryCommandArgs commandArgs, List<String> appendedPathList, List<DirectoryCoordinateIds> dcIds,
            ServerResponseFactoryInterface response) {
        LOGGER.info("processDirectoryCollectionForTrunkOrFeatureBranch");
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        for (int i = 0; i < appendedPathList.size(); i++) {
            List<SkinnyLogfileInfo> skinnyList = functionalQueriesDAO.getSkinnyLogfileInfo(branch.getId(), dcIds.get(i).getDirectoryId());
            for (SkinnyLogfileInfo skinnyInfo : skinnyList) {
                sendToClient(commandArgs, appendedPathList.get(i), skinnyInfo, response);
            }
        }
    }

    private void processDirectoryCollectionForReadOnlyBranch(Branch branch, GetDirectoryCommandArgs commandArgs, List<String> appendedPathList, List<DirectoryCoordinateIds> dcIds,
            ServerResponseFactoryInterface response) {
        FunctionalQueriesForReadOnlyBranchesDAO functionalQueriesDAO = new FunctionalQueriesForReadOnlyBranchesDAOImpl(schemaName);

        TagDAO tagDAO = new TagDAOImpl(schemaName);
        Tag tag = tagDAO.findById(branch.getTagId());
        Integer boundingCommitId = tag.getCommitId();

        for (int i = 0; i < appendedPathList.size(); i++) {
            List<SkinnyLogfileInfo> skinnyList = functionalQueriesDAO.getSkinnyLogfileInfoForReadOnlyBranch(branch, boundingCommitId, dcIds.get(i));
            for (SkinnyLogfileInfo skinnyInfo : skinnyList) {
                sendToClient(commandArgs, appendedPathList.get(i), skinnyInfo, response);
            }
        }
    }

    private void processDirectoryCollectionForReleaseBranch(Branch branch, GetDirectoryCommandArgs commandArgs, List<String> appendedPathList, List<DirectoryCoordinateIds> dcIds,
            ServerResponseFactoryInterface response) {
        FunctionalQueriesForReleaseBranchesDAO functionalQueriesForReleaseBranchesDAO = new FunctionalQueriesForReleaseBranchesDAOImpl(schemaName);
        for (int i = 0; i < appendedPathList.size(); i++) {
            List<SkinnyLogfileInfo> skinnyList = functionalQueriesForReleaseBranchesDAO.getSkinnyLogfileInfoForReleaseBranches(branch, branch.getCommitId(), dcIds.get(i));
            for (SkinnyLogfileInfo skinnyInfo : skinnyList) {
                sendToClient(commandArgs, appendedPathList.get(i), skinnyInfo, response);
            }
        }
    }

    private void sendToClient(GetDirectoryCommandArgs commandArgs, String appendedPath, SkinnyLogfileInfo skinnyInfo, ServerResponseFactoryInterface response) {
        FileInputStream fileInputStream = null;
        try {
            SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
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
            serverResponse.setProjectName(getRequest().getProjectName());
            serverResponse.setBranchName(getRequest().getBranchName());
            serverResponse.setAppendedPath(appendedPath);
            serverResponse.setRevisionString(skinnyInfo.getDefaultRevisionString());
            serverResponse.setOverwriteBehavior(commandArgs.getOverwriteBehavior());
            serverResponse.setTimestampBehavior(commandArgs.getTimeStampBehavior());

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
