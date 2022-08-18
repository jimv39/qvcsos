/*
 * Copyright 2022 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.response.AbstractServerResponsePromoteFile;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ServerUtility;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.FileRevision;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public abstract class AbstractClientRequestPromoteFile implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientRequestPromoteFile.class);
    private final ClientRequestPromoteFileData request;
    private final MutableByteArray commonAncestorBuffer = new MutableByteArray();
    private final MutableByteArray parentBranchTipRevisionBuffer = new MutableByteArray();
    private final MutableByteArray featureBranchTipRevisionBuffer = new MutableByteArray();
    private final DatabaseManager databaseManager;
    private final String schemaName;

    AbstractClientRequestPromoteFile(ClientRequestPromoteFileData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);

        ServerResponseInterface returnObject;
        String projectName = getRequest().getProjectName();
        String featureBranchName = getRequest().getBranchName();
        String parentBranchName = getRequest().getParentBranchName();
        FilePromotionInfo filePromotionInfo = getRequest().getFilePromotionInfo();
        try {
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(getSchemaName());
            DirectoryCoordinate fbDc = new DirectoryCoordinate(projectName, featureBranchName, filePromotionInfo.getPromotedFromAppendedPath());
            DirectoryCoordinate pbDc = new DirectoryCoordinate(projectName, parentBranchName, filePromotionInfo.getPromotedToAppendedPath());
            DirectoryCoordinateIds fbDcIds = functionalQueriesDAO.getDirectoryCoordinateIds(fbDc);
            DirectoryCoordinateIds pbDcIds = functionalQueriesDAO.getDirectoryCoordinateIds(pbDc);
            if (fbDcIds != null) {
                returnObject = executePromotion(userName, fbDc, pbDc, fbDcIds, pbDcIds, parentBranchName, filePromotionInfo, response);
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
        if (returnObject instanceof AbstractServerResponsePromoteFile) {
            LOGGER.info("Created response object for promotion operation: [{}]", filePromotionInfo.getDescribeTypeOfPromotion());
        }
        return returnObject;
    }

    abstract ServerResponseInterface executePromotion(String userName, DirectoryCoordinate fbDirectoryCoordinates, DirectoryCoordinate pbDirectoryCoordinates, DirectoryCoordinateIds fbDcIds,
            DirectoryCoordinateIds pbDcIds, String parentBranchName, FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) throws QVCSException, IOException, SQLException;

    void buildCommonResponseData(DirectoryCoordinateIds fbDcIds, DirectoryCoordinateIds pbDcIds, String parentBranchName,
            FilePromotionInfo filePromotionInfo, AbstractServerResponsePromoteFile serverResponsePromoteFile) throws QVCSException, IOException, SQLException {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        serverResponsePromoteFile.setPromotedToBranchName(parentBranchName);
        serverResponsePromoteFile.setPromotedToAppendedPath(filePromotionInfo.getPromotedToAppendedPath());
        serverResponsePromoteFile.setPromotedToShortWorkfileName(filePromotionInfo.getPromotedToShortWorkfileName());
        serverResponsePromoteFile.setPromotedFromBranchName(filePromotionInfo.getPromotedFromBranchName());
        serverResponsePromoteFile.setPromotedFromAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromoteFile.setPromotedFromShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setMergedInfoSyncBranchName(filePromotionInfo.getPromotedFromBranchName());
        serverResponsePromoteFile.setMergedInfoSyncAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromoteFile.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromoteFile.setProjectName(getRequest().getProjectName());
        serverResponsePromoteFile.setPromotionType(getRequest().getFilePromotionInfo().getTypeOfPromotion());

        // Fetch the promoted-from branch tip revision file
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(getSchemaName());
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
            LOGGER.info("No merge required line 139");
        } else if (Utility.digestsMatch(featureTipRevision.getRevisionDigest(), parentTipRevision.getRevisionDigest())) {
            // No merge required here either, since the files are identical.
            serverResponsePromoteFile.setMergedResultBuffer(featureBranchTipRevisionBuffer.getValue());
            LOGGER.info("No merge required line 143");
        } else {
            // Need to figure out the common ancestor.
            FileRevision commonAncestorFileRevision = deduceCommonAncestorRevision(pbDcIds.getBranchId(), fbDcIds.getBranchId(), filePromotionInfo.getFileId());
            serverResponsePromoteFile.setCommonAncestorRevisionId(commonAncestorFileRevision.getId());

            // If the parent tip revision is the same as the common ancestor revision, then no merge is needed since there have been no parent edits to merge.
            if (commonAncestorFileRevision.getId().intValue() == parentTipRevision.getId().intValue()) {
                serverResponsePromoteFile.setMergedResultBuffer(featureBranchTipRevisionBuffer.getValue());
                LOGGER.info("No merge required line 152");
            } else {
                File parentBranchTipRevisionFile = sourceControlBehaviorManager.getFileRevision(parentTipRevision.getId());
                parentBranchTipRevisionBuffer.setValue(Utility.readFileToBuffer(parentBranchTipRevisionFile));
                File commonAncestorRevisionFile = sourceControlBehaviorManager.getFileRevision(commonAncestorFileRevision.getId());
                commonAncestorBuffer.setValue(Utility.readFileToBuffer(commonAncestorRevisionFile));
                byte[] mergedResultBuffer = ServerUtility.createMergedResultBuffer(commonAncestorRevisionFile, parentBranchTipRevisionFile, featureBranchTipRevisionFile);

                if (mergedResultBuffer != null) {
                    serverResponsePromoteFile.setMergedResultBuffer(mergedResultBuffer);
                    LOGGER.info("No merge required line 162");
                } else {
                    serverResponsePromoteFile.setBranchTipRevisionBuffer(featureBranchTipRevisionBuffer.getValue());
                    serverResponsePromoteFile.setCommonAncestorBuffer(commonAncestorBuffer.getValue());
                    serverResponsePromoteFile.setBranchParentTipRevisionBuffer(parentBranchTipRevisionBuffer.getValue());
                }
            }
        }

        // Update the database to indicate the file has been promoted.
        sourceControlBehaviorManager.markPromoted(filePromotionInfo);
    }

    private FileRevision deduceCommonAncestorRevision(int promoteToBranchId, int promoteFromBranchId, Integer fileId) {
        FileRevision commonAncestorRevision = null;

        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(getSchemaName());
        FileRevision newestRevisionOnBranch = fileRevisionDAO.findNewestRevisionOnBranch(promoteFromBranchId, fileId);
        FileRevision newestPromoteToRevision = fileRevisionDAO.findNewestRevisionOnBranch(promoteToBranchId, fileId);
        FileRevision promoteToRevisionAncestor = null;
        if (newestPromoteToRevision.getAncestorRevisionId() != null) {
            promoteToRevisionAncestor = fileRevisionDAO.findById(newestPromoteToRevision.getAncestorRevisionId());
        }
        if (newestRevisionOnBranch != null) {
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

    /**
     * @return the schemaName
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * @return the request
     */
    public ClientRequestPromoteFileData getRequest() {
        return request;
    }

}
