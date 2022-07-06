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
import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.logfileaction.MoveFile;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.logfileaction.Rename;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseMoveFile;
import com.qumasoft.qvcslib.response.ServerResponsePromoteFile;
import com.qumasoft.qvcslib.response.ServerResponseRenameArchive;
import com.qumasoft.server.NotificationManager;
import com.qumasoft.server.ServerUtility;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileRevision;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Promote a file.
 *
 * @author Jim Voris
 */
class ClientRequestPromoteFile implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestPromoteFile.class);
    private final ClientRequestPromoteFileData request;
    private final MutableByteArray commonAncestorBuffer = new MutableByteArray();
    private final MutableByteArray parentBranchTipRevisionBuffer = new MutableByteArray();
    private final MutableByteArray featureBranchTipRevisionBuffer = new MutableByteArray();
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestPromoteFile.
     *
     * @param data the command line data, etc.
     */
    ClientRequestPromoteFile(ClientRequestPromoteFileData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);

        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String featureBranchName = request.getBranchName();
        String parentBranchName = request.getParentBranchName();
        FilePromotionInfo filePromotionInfo = request.getFilePromotionInfo();
        try {
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            DirectoryCoordinate fbDc = new DirectoryCoordinate(projectName, featureBranchName, filePromotionInfo.getPromotedFromAppendedPath());
            DirectoryCoordinate pbDc = new DirectoryCoordinate(projectName, parentBranchName, filePromotionInfo.getPromotedToAppendedPath());
            DirectoryCoordinateIds fbDcIds = functionalQueriesDAO.getDirectoryCoordinateIds(fbDc);
            DirectoryCoordinateIds pbDcIds = functionalQueriesDAO.getDirectoryCoordinateIds(pbDc);
            if (fbDcIds != null) {
                switch (filePromotionInfo.getTypeOfPromotion()) {
                    case SIMPLE_PROMOTION_TYPE:
                        returnObject = simplePromote(fbDc, fbDcIds, pbDcIds, parentBranchName, filePromotionInfo, response);
                        break;
                    case FILE_NAME_CHANGE_PROMOTION_TYPE:
                        returnObject = nameChangePromote(pbDc, fbDc, fbDcIds, pbDcIds, parentBranchName, filePromotionInfo, response);
                        break;
                    case FILE_LOCATION_CHANGE_PROMOTION_TYPE:
                        returnObject = locationChangePromote(pbDc, fbDc, fbDcIds, pbDcIds, parentBranchName, filePromotionInfo, response);
                        break;
                    case LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE:
                        returnObject = locationAndNameChangePromote(pbDc, fbDc, fbDcIds, pbDcIds, parentBranchName, filePromotionInfo, response);
                        break;
                    case FILE_CREATED_PROMOTION_TYPE:
                        returnObject = createPromote(fbDcIds, parentBranchName, filePromotionInfo);
                        break;
                    case FILE_DELETED_PROMOTION_TYPE:
                        returnObject = deletePromote(pbDc, pbDcIds, parentBranchName, filePromotionInfo, response);
                        break;
                    default:
                        // Return an error message.
                        ServerResponseMessage message = new ServerResponseMessage("Promotion type is not supported yet: [" + filePromotionInfo.getTypeOfPromotion()
                                + "]", projectName,
                                featureBranchName, filePromotionInfo.getPromotedFromAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
                        LOGGER.warn(message.getMessage());
                        returnObject = message;
                        break;
                }
            } else {
                // Return an error message.
                ServerResponseMessage message = new ServerResponseMessage("Archive not found for " + filePromotionInfo.getPromotedFromShortWorkfileName(), projectName, featureBranchName,
                        filePromotionInfo.getPromotedFromAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
                LOGGER.warn(message.getMessage());
                returnObject = message;
            }
        } catch (SQLException | QVCSException | IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return an error message.
            ServerResponseMessage message = new ServerResponseMessage("Caught exception trying to promote a file: [" + filePromotionInfo.getPromotedFromShortWorkfileName()
                    + "]. Exception string: " + e.getMessage(), projectName, featureBranchName, filePromotionInfo.getPromotedFromAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
            returnObject = message;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private ServerResponsePromoteFile buildCommonResponseData(DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds, String parentBranchName,
            FilePromotionInfo filePromotionInfo)
            throws QVCSException, IOException, SQLException {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        ServerResponsePromoteFile serverResponsePromoteFile = new ServerResponsePromoteFile();
        serverResponsePromoteFile.setPromotedToBranchName(parentBranchName);
        serverResponsePromoteFile.setPromotedToAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromoteFile.setPromotedToShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setMergedInfoSyncBranchName(filePromotionInfo.getPromotedFromBranchName());
        serverResponsePromoteFile.setMergedInfoSyncAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromoteFile.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setProjectName(request.getProjectName());
        serverResponsePromoteFile.setPromotionType(request.getFilePromotionInfo().getTypeOfPromotion());

        // Fetch the promoted-from branch tip revision file
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        FileRevision featureTipRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(fbDcIds.getBranchId(), filePromotionInfo.getFileId());
        if (featureTipRevision.getId().intValue() != filePromotionInfo.getFeatureBranchRevisionId().intValue()) {
            throw new QVCSRuntimeException("Feature tip revision mismatch. Bug in queries.");
        }
        serverResponsePromoteFile.setFeatureBranchTipRevisionId(filePromotionInfo.getFeatureBranchRevisionId());

        // The promote-to tip revision file is the newest revision on the parent branch...
        FileRevision parentTipRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(pbDcIds.getBranchId(), filePromotionInfo.getFileId());
        serverResponsePromoteFile.setParentBranchTipRevisionId(parentTipRevision.getId());

        File featureBranchTipRevisionFile = sourceControlBehaviorManager.getFileRevision(filePromotionInfo.getFeatureBranchRevisionId());
        featureBranchTipRevisionBuffer.setValue(Utility.readFileToBuffer(featureBranchTipRevisionFile));

        if (featureTipRevision.getAncestorRevisionId().intValue() == parentTipRevision.getId().intValue()) {
            // No merge required...
            serverResponsePromoteFile.setMergedResultBuffer(featureBranchTipRevisionBuffer.getValue());
            LOGGER.info("No merge required line 174");
        } else if (Utility.digestsMatch(featureTipRevision.getRevisionDigest(), parentTipRevision.getRevisionDigest())) {
            // No merge required here either, since the files are identical.
            serverResponsePromoteFile.setMergedResultBuffer(featureBranchTipRevisionBuffer.getValue());
            LOGGER.info("No merge required line 178");
        } else {
            // Need to figure out the common ancestor.
            FileRevision commonAncestorFileRevision = deduceCommonAncestorRevision(pbDcIds.getBranchId(), fbDcIds.getBranchId(), filePromotionInfo.getFileId());
            serverResponsePromoteFile.setCommonAncestorRevisionId(commonAncestorFileRevision.getId());

            // If the parent tip revision is the same as the common ancestor revision, then no merge is needed since there have been no parent edits to merge.
            if (commonAncestorFileRevision.getId().intValue() == parentTipRevision.getId().intValue()) {
                serverResponsePromoteFile.setMergedResultBuffer(featureBranchTipRevisionBuffer.getValue());
                LOGGER.info("No merge required line 187");
            } else {
                File parentBranchTipRevisionFile = sourceControlBehaviorManager.getFileRevision(parentTipRevision.getId());
                parentBranchTipRevisionBuffer.setValue(Utility.readFileToBuffer(parentBranchTipRevisionFile));
                File commonAncestorRevisionFile = sourceControlBehaviorManager.getFileRevision(commonAncestorFileRevision.getId());
                commonAncestorBuffer.setValue(Utility.readFileToBuffer(commonAncestorRevisionFile));
                byte[] mergedResultBuffer = ServerUtility.createMergedResultBuffer(commonAncestorRevisionFile, parentBranchTipRevisionFile, featureBranchTipRevisionFile);

                if (mergedResultBuffer != null) {
                    serverResponsePromoteFile.setMergedResultBuffer(mergedResultBuffer);
                    LOGGER.info("No merge required line 197");
                } else {
                    serverResponsePromoteFile.setBranchTipRevisionBuffer(featureBranchTipRevisionBuffer.getValue());
                    serverResponsePromoteFile.setCommonAncestorBuffer(commonAncestorBuffer.getValue());
                    serverResponsePromoteFile.setBranchParentTipRevisionBuffer(parentBranchTipRevisionBuffer.getValue());
                }
            }
        }

        // Update the feature branch revision to indicate it has been promoted.
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        fileRevisionDAO.markPromoted(filePromotionInfo.getFeatureBranchRevisionId());

        return serverResponsePromoteFile;
    }

    private ServerResponsePromoteFile buildResponseData(DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds, String parentBranchName,
            FilePromotionInfo filePromotionInfo)
            throws QVCSException, IOException, SQLException {
        ServerResponsePromoteFile serverResponsePromoteFile = buildCommonResponseData(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo);
        return serverResponsePromoteFile;
    }

    private ServerResponsePromoteFile buildResponseDataForCreate(DirectoryCoordinateIds fbDcIds, FilePromotionInfo filePromotionInfo) throws QVCSException, IOException, SQLException {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        ServerResponsePromoteFile serverResponsePromoteFile = new ServerResponsePromoteFile();
        serverResponsePromoteFile.setPromotedToAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromoteFile.setPromotedToBranchName(request.getParentBranchName());
        serverResponsePromoteFile.setMergedInfoSyncBranchName(filePromotionInfo.getPromotedFromBranchName());
        serverResponsePromoteFile.setMergedInfoSyncAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromoteFile.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setProjectName(request.getProjectName());
        serverResponsePromoteFile.setPromotedToShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setPromotionType(request.getFilePromotionInfo().getTypeOfPromotion());

        // Fetch the feature branch tip revision file
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        FileRevision featureTipRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(fbDcIds.getBranchId(), filePromotionInfo.getFileId());
        if (featureTipRevision.getId().intValue() != filePromotionInfo.getFeatureBranchRevisionId().intValue()) {
            throw new QVCSRuntimeException("Feature tip revision mismatch. Bug in queries.");
        }
        File featureBranchTipRevisionFile = sourceControlBehaviorManager.getFileRevision(filePromotionInfo.getFeatureBranchRevisionId());
        serverResponsePromoteFile.setFeatureBranchTipRevisionId(filePromotionInfo.getFeatureBranchRevisionId());
        featureBranchTipRevisionBuffer.setValue(Utility.readFileToBuffer(featureBranchTipRevisionFile));

        serverResponsePromoteFile.setMergedResultBuffer(featureBranchTipRevisionBuffer.getValue());

        // Update the feature branch revision to indicate it has been promoted.
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        fileRevisionDAO.markPromoted(filePromotionInfo.getFeatureBranchRevisionId());

        // Update the fileName record to mark it deleted on the promote-from branch.
        Integer commitId = sourceControlBehaviorManager.getCommitId(null, "Marking FileName record deleted for promotion of create on branch.");
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        FileName fileName = fileNameDAO.findByBranchIdAndFileId(fbDcIds.getBranchId(), filePromotionInfo.getFileId());
        Integer fileNameId = fileNameDAO.delete(fileName.getId(), commitId);
        if (fileNameId  != null) {
            LOGGER.info("Deleted file name record for file: [{}]; fileId: [{}]. fileNameId: [{}]",
                    filePromotionInfo.getPromotedFromShortWorkfileName(), fileName.getFileId(), fileName.getId());
        } else {
            throw new QVCSRuntimeException("Failed to mark file_name record as deleted.");
        }

        return serverResponsePromoteFile;
    }

    private ServerResponsePromoteFile buildResponseDataForDelete(DirectoryCoordinate pbDc, DirectoryCoordinateIds pbDcIds, FilePromotionInfo filePromotionInfo) throws SQLException {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        ServerResponsePromoteFile serverResponsePromoteFile = new ServerResponsePromoteFile();
        serverResponsePromoteFile.setPromotedToAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromoteFile.setPromotedToBranchName(request.getParentBranchName());

        // Note that the mergedInfo object we synch to for deletes MUST be on the promoted-to branch,
        // since the mergedInfo has already been deleted on the promoted-from branch.
        serverResponsePromoteFile.setMergedInfoSyncBranchName(filePromotionInfo.getPromotedToBranchName());
        serverResponsePromoteFile.setMergedInfoSyncAppendedPath(filePromotionInfo.getPromotedToAppendedPath());
        serverResponsePromoteFile.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedToShortWorkfileName());

        serverResponsePromoteFile.setProjectName(request.getProjectName());
        serverResponsePromoteFile.setPromotedToShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setPromotionType(request.getFilePromotionInfo().getTypeOfPromotion());

        // Update the feature branch revision to indicate it has been promoted.
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        fileRevisionDAO.markPromoted(filePromotionInfo.getFeatureBranchRevisionId());

        // Update the fileName record to mark it deleted on the promote-to branch.
        Integer commitId = sourceControlBehaviorManager.getCommitId(null, "Marking FileName record deleted for promotion of delete on branch.");

        // Update the fileName record on the promoted-to branch.
        Integer fileNameId;
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        FileName parentFileName = fileNameDAO.findByBranchIdAndFileId(pbDcIds.getBranchId(), filePromotionInfo.getFileId());
        if (parentFileName == null) {
            // The filename record doesn't exist yet on the promoted-to branch (it must exist on its parent).
            fileNameId = sourceControlBehaviorManager.deleteFile(pbDc.getProjectName(), pbDc.getBranchName(), pbDc.getAppendedPath(), filePromotionInfo.getPromotedFromShortWorkfileName());
        } else {
            fileNameId = fileNameDAO.delete(parentFileName.getId(), commitId);
        }

        if (fileNameId != null) {
            LOGGER.info("Deleted file name record for file: [{}]; fileId: [{}]. fileNameId: [{}]",
                    filePromotionInfo.getPromotedFromShortWorkfileName(), filePromotionInfo.getFileId(), fileNameId);
        } else {
            throw new QVCSRuntimeException("Failed to mark file_name record as deleted.");
        }

        return serverResponsePromoteFile;
    }

    private ServerResponsePromoteFile buildResponseDataForNameChange(DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds, String parentBranchName,
            FilePromotionInfo filePromotionInfo) throws SQLException, IOException, QVCSException {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        ServerResponsePromoteFile serverResponsePromoteFile = buildCommonResponseData(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo);

        // Update the fileName record to mark it deleted on the promote-from branch.
        Integer commitId = sourceControlBehaviorManager.getCommitId(null, "Marking FileName record deleted for promotion of rename on branch.");
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        FileName fileName = fileNameDAO.findByBranchIdAndFileId(fbDcIds.getBranchId(), filePromotionInfo.getFileId());
        Integer fileNameId = fileNameDAO.delete(fileName.getId(), commitId);
        if (fileNameId  != null) {
            LOGGER.info("Deleted file name record for name change for file: [{}]; fileId: [{}]. fileNameId: [{}]",
                    filePromotionInfo.getPromotedFromShortWorkfileName(), fileName.getFileId(), fileName.getId());
        } else {
            throw new QVCSRuntimeException("Failed to mark file_name record as deleted for name change promotion.");
        }

        // Update the fileName record on the promoted-to branch.
        FileName parentFileName = fileNameDAO.findByBranchIdAndFileId(pbDcIds.getBranchId(), fileName.getFileId());
        if (parentFileName == null) {
            // The filename record doesn't exist yet on the promoted-to branch (it must exist on its parent).
            sourceControlBehaviorManager.renameFile(pbDcIds.getBranchId(), fileNameId, filePromotionInfo.getPromotedFromShortWorkfileName());
        } else {
            fileNameDAO.rename(parentFileName.getId(), commitId, filePromotionInfo.getPromotedFromShortWorkfileName());
        }

        return serverResponsePromoteFile;
    }

    private ServerResponsePromoteFile buildResponseDataForLocationChange(DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds, String parentBranchName,
            FilePromotionInfo filePromotionInfo) throws QVCSException, IOException, SQLException {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        ServerResponsePromoteFile serverResponsePromoteFile = buildCommonResponseData(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo);

        // Update the fileName record to mark it deleted on the promote-from branch.
        Integer commitId = sourceControlBehaviorManager.getCommitId(null, "Promoting location change on branch.");
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        FileName fileName = fileNameDAO.findByBranchIdAndFileId(fbDcIds.getBranchId(), filePromotionInfo.getFileId());
        Integer fileNameId = fileNameDAO.delete(fileName.getId(), commitId);
        if (fileNameId  != null) {
            LOGGER.info("Deleted file name record for location change for file: [{}]; fileId: [{}]. fileNameId: [{}]",
                    filePromotionInfo.getPromotedFromShortWorkfileName(), fileName.getFileId(), fileName.getId());
        } else {
            throw new QVCSRuntimeException("Failed to mark file_name record as deleted for location change promotion.");
        }

        // Update the fileName record on the promoted-to branch.
        FileName parentFileName = fileNameDAO.findByBranchIdAndFileId(pbDcIds.getBranchId(), fileName.getFileId());
        if (parentFileName == null) {
            // The filename record doesn't exist yet on the promoted-to branch (it must exist on its parent).
            sourceControlBehaviorManager.moveFile(pbDcIds.getBranchId(), fileNameId, fbDcIds.getDirectoryId());
        } else {
            fileNameDAO.move(parentFileName.getId(), commitId, fbDcIds.getDirectoryId());
        }

        return serverResponsePromoteFile;
    }

    private ServerResponsePromoteFile buildResponseDataForLocationChangeAndNameChange(DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds, String parentBranchName,
            FilePromotionInfo filePromotionInfo) throws QVCSException, IOException, SQLException {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        ServerResponsePromoteFile serverResponsePromoteFile = buildCommonResponseData(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo);

        // Update the fileName record to mark it deleted on the promote-from branch.
        Integer commitId = sourceControlBehaviorManager.getCommitId(null, "Promoting location change and name change on branch.");
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        FileName fileName = fileNameDAO.findByBranchIdAndFileId(fbDcIds.getBranchId(), filePromotionInfo.getFileId());
        Integer fileNameId = fileNameDAO.delete(fileName.getId(), commitId);
        if (fileNameId != null) {
            LOGGER.info("Deleted file name record for name and location change for file: [{}]; fileId: [{}]. fileNameId: [{}]",
                    filePromotionInfo.getPromotedFromShortWorkfileName(), fileName.getFileId(), fileName.getId());
        } else {
            throw new QVCSRuntimeException("Failed to mark file_name record as deleted for name and location change promotion.");
        }

        // Update the fileName record on the promoted-to branch.
        FileName parentFileName = fileNameDAO.findByBranchIdAndFileId(pbDcIds.getBranchId(), fileName.getFileId());
        if (parentFileName == null) {
            // The filename record doesn't exist yet on the promoted-to branch (it must exist on its parent).
            sourceControlBehaviorManager.moveAndRenameFile(pbDcIds.getBranchId(), fileNameId, fbDcIds.getDirectoryId(), filePromotionInfo.getPromotedFromShortWorkfileName());
        } else {
            fileNameDAO.moveAndRename(parentFileName.getId(), commitId, fbDcIds.getDirectoryId(), filePromotionInfo.getPromotedFromShortWorkfileName());
        }

        return serverResponsePromoteFile;
    }

    private ServerResponseInterface handleSimplePromotion(DirectoryCoordinateIds pbDcIds, FilePromotionInfo filePromotionInfo,
            ServerResponsePromoteFile serverResponsePromoteFile) throws QVCSException {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(filePromotionInfo.getFeatureBranchRevisionId());
        LogfileInfo logfileInfo = functionalQueriesDAO.getLogfileInfo(pbDcIds, filePromotionInfo.getPromotedFromShortWorkfileName(), filePromotionInfo.getFileId());
        serverResponsePromoteFile.setLogfileInfo(logfileInfo);
        serverResponsePromoteFile.setSkinnyLogfileInfo(skinnyInfo);
        return serverResponsePromoteFile;
    }

    private ServerResponseInterface handleRenamePromotion(DirectoryCoordinateIds pbDcIds, FilePromotionInfo filePromotionInfo, ServerResponsePromoteFile serverResponsePromoteFile) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        FileRevision newestPromotedToFileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(pbDcIds.getBranchId(), filePromotionInfo.getFileId());
        serverResponsePromoteFile.setParentBranchTipRevisionId(newestPromotedToFileRevision.getId());
        SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(newestPromotedToFileRevision.getId());
        skinnyInfo.setShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setSkinnyLogfileInfo(skinnyInfo);
        return serverResponsePromoteFile;
    }

    private ServerResponseInterface handleLocationChangePromotion(DirectoryCoordinateIds pbDcIds, FilePromotionInfo filePromotionInfo, ServerResponsePromoteFile serverResponsePromoteFile) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        FileRevision newestPromotedToFileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(pbDcIds.getBranchId(), filePromotionInfo.getFileId());
        serverResponsePromoteFile.setParentBranchTipRevisionId(newestPromotedToFileRevision.getId());
        SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(newestPromotedToFileRevision.getId());
        skinnyInfo.setShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setSkinnyLogfileInfo(skinnyInfo);
        return serverResponsePromoteFile;
    }

    private ServerResponseInterface handleChildCreatedPromotion(FilePromotionInfo filePromotionInfo, ServerResponsePromoteFile serverResponsePromoteFile) throws QVCSException {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(filePromotionInfo.getFeatureBranchRevisionId());
        serverResponsePromoteFile.setSkinnyLogfileInfo(skinnyInfo);
        return serverResponsePromoteFile;
    }

    private ServerResponseInterface handleChildDeletedPromotion(FilePromotionInfo filePromotionInfo, ServerResponsePromoteFile serverResponsePromoteFile) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(filePromotionInfo.getFeatureBranchRevisionId());
        serverResponsePromoteFile.setSkinnyLogfileInfo(skinnyInfo);
        return serverResponsePromoteFile;
    }

    private FileRevision deduceCommonAncestorRevision(int promoteToBranchId, int promoteFromBranchId, Integer fileId) {
        FileRevision commonAncestorRevision = null;

        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        FileRevision newestRevisionOnBranch = fileRevisionDAO.findNewestRevisionOnBranch(promoteFromBranchId, fileId);
        FileRevision newestPromoteToRevision = fileRevisionDAO.findNewestRevisionOnBranch(promoteToBranchId, fileId);
        FileRevision promoteToRevisionAncestor = null;
        if (newestPromoteToRevision.getAncestorRevisionId() != null) {
            promoteToRevisionAncestor = fileRevisionDAO.findById(newestPromoteToRevision.getAncestorRevisionId());
        }
        if (newestRevisionOnBranch != null && newestPromoteToRevision != null) {
            Integer newestBranchAncestorId = newestRevisionOnBranch.getAncestorRevisionId();
            Integer newestPromoteToAncestorId;
            if (newestPromoteToRevision.getAncestorRevisionId() != null && promoteToRevisionAncestor != null && promoteToRevisionAncestor.getBranchId() == promoteToBranchId) {
                newestPromoteToAncestorId = newestPromoteToRevision.getAncestorRevisionId();
            } else {
                newestPromoteToAncestorId = newestPromoteToRevision.getId();
            }

            // Find the newest promote-to revision that is a common ancestor.
            commonAncestorRevision = fileRevisionDAO.findCommonAncestorRevision(promoteToBranchId, newestBranchAncestorId, newestPromoteToAncestorId, fileId);
        }
        if (commonAncestorRevision == null) {
            LOGGER.warn("Failed to find common ancestor for promote to branchId: [{}], promote from branchId: [{}], fileId: [{}]", promoteToBranchId, promoteFromBranchId, fileId);
            throw new QVCSRuntimeException("Failed to find common ancestor!!!");
        }
        return commonAncestorRevision;
    }

    private ServerResponseInterface simplePromote(DirectoryCoordinate fbDirectoryCoordinates, DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds, String parentBranchName,
            FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) throws QVCSException, IOException, SQLException {
        ServerResponsePromoteFile serverResponsePromoteFile = buildResponseData(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo);
        ServerResponseInterface returnObject = handleSimplePromotion(pbDcIds, filePromotionInfo, serverResponsePromoteFile);
        String removeShortFileName = filePromotionInfo.getPromotedFromShortWorkfileName();
        if (serverResponsePromoteFile != null) {
            SkinnyLogfileInfo skinnyInfo = serverResponsePromoteFile.getSkinnyLogfileInfo();
            // We need to queue the notification so that it does not arrive at the client before the response, since
            // if it does, it will cause the file to be removed before the client has a chance to copy the file data to the
            // workfile directory.
            NotificationManager.getNotificationManager().queueNotification(response, fbDirectoryCoordinates, skinnyInfo, new Remove(removeShortFileName));
        }
        return returnObject;
    }

    private ServerResponseInterface nameChangePromote(DirectoryCoordinate pbDc, DirectoryCoordinate fbDirectoryCoordinates, DirectoryCoordinateIds fbDcIds,
            DirectoryCoordinateIds pbDcIds, String parentBranchName,
            FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) throws SQLException, IOException, QVCSException {
        LOGGER.info("Changing file name from: [{}] to [{}]", filePromotionInfo.getPromotedToShortWorkfileName(), filePromotionInfo.getPromotedFromShortWorkfileName());
        ServerResponsePromoteFile serverResponsePromoteFile = buildResponseDataForNameChange(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo);
        serverResponsePromoteFile.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        ServerResponseInterface returnObject = handleRenamePromotion(pbDcIds, filePromotionInfo, serverResponsePromoteFile);
        String removeShortFileName = filePromotionInfo.getPromotedFromShortWorkfileName();
        SkinnyLogfileInfo skinnyInfo = serverResponsePromoteFile.getSkinnyLogfileInfo();
        // We need to queue the notification so that it does not arrive at the client before the response, since
        // if it does, it will cause the file to be removed before the client has a chance to copy the file data to the
        // workfile directory.
        NotificationManager.getNotificationManager().queueNotification(response, fbDirectoryCoordinates, skinnyInfo, new Remove(removeShortFileName));

        // Send a rename response so the promoted-to directory will have the new name.
        sendRenameResponse(pbDc, pbDcIds.getBranchId(), filePromotionInfo, response);
        return returnObject;
    }

    private ServerResponseInterface locationChangePromote(DirectoryCoordinate pbDc, DirectoryCoordinate fbDc, DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds,
            String parentBranchName, FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) throws SQLException, IOException, QVCSException {
        LOGGER.info("Changing file [{}] location from: [{}] to [{}]", filePromotionInfo.getPromotedFromShortWorkfileName(), pbDc.getAppendedPath(), fbDc.getAppendedPath());
        ServerResponsePromoteFile serverResponsePromoteFile = buildResponseDataForLocationChange(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo);
        serverResponsePromoteFile.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        ServerResponseInterface returnObject = handleLocationChangePromotion(pbDcIds, filePromotionInfo, serverResponsePromoteFile);

        // Send a move response so the promoted-to directory will have the moved file.
        sendMoveResponse(pbDc, filePromotionInfo, response);
        return returnObject;
    }

    private ServerResponseInterface locationAndNameChangePromote(DirectoryCoordinate pbDc, DirectoryCoordinate fbDc, DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds,
            String parentBranchName, FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) throws SQLException, IOException, QVCSException {
        LOGGER.info("Changing file name from: [{}] to [{}]", filePromotionInfo.getPromotedToShortWorkfileName(), filePromotionInfo.getPromotedFromShortWorkfileName());
        LOGGER.info("Changing file [{}] location from: [{}] to [{}]", filePromotionInfo.getPromotedFromShortWorkfileName(), pbDc.getAppendedPath(), fbDc.getAppendedPath());
        ServerResponsePromoteFile serverResponsePromoteFile = buildResponseDataForLocationChangeAndNameChange(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo);
        serverResponsePromoteFile.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        ServerResponseInterface returnObject = handleRenamePromotion(pbDcIds, filePromotionInfo, serverResponsePromoteFile);
        String removeShortFileName = filePromotionInfo.getPromotedFromShortWorkfileName();
        SkinnyLogfileInfo skinnyInfo = serverResponsePromoteFile.getSkinnyLogfileInfo();

        // Send a rename response so the promoted-to directory will have the new name.
        sendRenameResponse(pbDc, pbDcIds.getBranchId(), filePromotionInfo, response);

        // Send a move response so the promoted-to directory will have the moved file.
        sendMoveResponse(pbDc, filePromotionInfo, response);
        return returnObject;
    }

    private ServerResponseInterface createPromote(DirectoryCoordinateIds fbDcIds, String parentBranchName, FilePromotionInfo filePromotionInfo) throws SQLException, IOException, QVCSException {
        LOGGER.info("Promoting created file: [{}] to parent branch: [{}]", filePromotionInfo.getPromotedFromShortWorkfileName(), parentBranchName);
        ServerResponsePromoteFile serverResponsePromoteFile = buildResponseDataForCreate(fbDcIds, filePromotionInfo);
        ServerResponseInterface returnObject = handleChildCreatedPromotion(filePromotionInfo, serverResponsePromoteFile);
        return returnObject;
    }

    private ServerResponseInterface deletePromote(DirectoryCoordinate pbDc, DirectoryCoordinateIds pbDcIds, String parentBranchName, FilePromotionInfo filePromotionInfo,
            ServerResponseFactoryInterface response) throws SQLException {
        LOGGER.info("Promoting deleted file: [{}] to parent branch: [{}]", filePromotionInfo.getPromotedFromShortWorkfileName(), parentBranchName);
        ServerResponsePromoteFile serverResponsePromoteFile = buildResponseDataForDelete(pbDc, pbDcIds, filePromotionInfo);
        ServerResponseInterface returnObject = handleChildDeletedPromotion(filePromotionInfo, serverResponsePromoteFile);

        // Queue a delete notification so the promoted-to directory will delete the file.
        SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo(filePromotionInfo.getPromotedFromShortWorkfileName());
        NotificationManager.getNotificationManager().queueNotification(response, pbDc, skinnyInfo, new Remove(filePromotionInfo.getPromotedFromShortWorkfileName()));

        return returnObject;
    }

    private void sendRenameResponse(DirectoryCoordinate pbDc, Integer promoteToBranchId, FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) {
        ServerResponseRenameArchive serverResponseRenameArchive = new ServerResponseRenameArchive();
        serverResponseRenameArchive.setServerName(response.getServerName());
        serverResponseRenameArchive.setProjectName(request.getProjectName());
        serverResponseRenameArchive.setBranchName(request.getParentBranchName());
        serverResponseRenameArchive.setAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponseRenameArchive.setOldShortWorkfileName(filePromotionInfo.getPromotedToShortWorkfileName());
        serverResponseRenameArchive.setNewShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());

        // Find the file's newest branch revision...
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        FileRevision fileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(promoteToBranchId, filePromotionInfo.getFileId());

        SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(fileRevision.getId());
        serverResponseRenameArchive.setSkinnyLogfileInfo(skinnyInfo);

        // Notify listeners.
        NotificationManager.getNotificationManager().notifySkinnyInfoListeners(pbDc, skinnyInfo, new Rename(filePromotionInfo.getPromotedToShortWorkfileName()));

        // Send the response back to the client.
        response.createServerResponse(serverResponseRenameArchive);
    }

    private void sendMoveResponse(DirectoryCoordinate pbDc, FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) {
        Properties fakeProperties = new Properties();
        fakeProperties.setProperty("QVCS_IGNORECASEFLAG", QVCSConstants.QVCS_NO);
        ServerResponseMoveFile serverResponseMoveFile = new ServerResponseMoveFile();
        serverResponseMoveFile.setServerName(response.getServerName());
        serverResponseMoveFile.setProjectName(pbDc.getProjectName());
        serverResponseMoveFile.setBranchName(pbDc.getBranchName());
        serverResponseMoveFile.setProjectProperties(fakeProperties);
        serverResponseMoveFile.setOriginAppendedPath(pbDc.getAppendedPath());
        serverResponseMoveFile.setDestinationAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponseMoveFile.setShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());

        // Find the file's newest branch revision...
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        FileRevision fileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(filePromotionInfo.getPromotedToBranchId(), filePromotionInfo.getFileId());

        SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(fileRevision.getId());
        serverResponseMoveFile.setSkinnyLogfileInfo(skinnyInfo);

        // Notify listeners.
        NotificationManager.getNotificationManager().notifySkinnyInfoListeners(pbDc, skinnyInfo, new MoveFile(pbDc.getAppendedPath(), filePromotionInfo.getPromotedFromAppendedPath()));

        // Send the response back to the client.
        response.createServerResponse(serverResponseMoveFile);
    }
}
