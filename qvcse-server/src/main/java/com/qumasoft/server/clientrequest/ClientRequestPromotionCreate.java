/*
 * Copyright 2022-2023 Jim Voris.
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
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.qvcslib.response.ServerResponsePromotionCreate;
import com.qumasoft.server.NotificationManager;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.RolePrivilegesManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.FileRevision;
import com.qvcsos.server.datamodel.ProvisionalDirectoryLocation;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestPromotionCreate extends AbstractClientRequestPromoteFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestPromotionCreate.class);

    ClientRequestPromotionCreate(ClientRequestPromoteFileData data) {
        super(data);
    }

    @Override
    AbstractServerResponse executePromotion(String userName, DirectoryCoordinate fbDirectoryCoordinates, DirectoryCoordinate pbDirectoryCoordinates, DirectoryCoordinateIds fbDcIds,
            DirectoryCoordinateIds pbDcIds, String parentBranchName, FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) throws QVCSException, IOException, SQLException {
        ServerResponsePromotionCreate serverResponsePromotionCreate = new ServerResponsePromotionCreate();

        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        serverResponsePromotionCreate.setPromotedToBranchName(parentBranchName);
        serverResponsePromotionCreate.setPromotedToAppendedPath(filePromotionInfo.getPromotedToAppendedPath());
        serverResponsePromotionCreate.setPromotedToShortWorkfileName(filePromotionInfo.getPromotedToShortWorkfileName());
        serverResponsePromotionCreate.setPromotedFromBranchName(filePromotionInfo.getPromotedFromBranchName());
        serverResponsePromotionCreate.setPromotedFromAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromotionCreate.setPromotedFromShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromotionCreate.setMergedInfoSyncBranchName(filePromotionInfo.getPromotedFromBranchName());
        serverResponsePromotionCreate.setMergedInfoSyncAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        serverResponsePromotionCreate.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        serverResponsePromotionCreate.setProjectName(getRequest().getProjectName());
        ClientRequestPromoteFileData clientRequestPromoteFileData = (ClientRequestPromoteFileData) getRequest();
        serverResponsePromotionCreate.setPromotionType(clientRequestPromoteFileData.getFilePromotionInfo().getTypeOfPromotion());

        // Fetch the promoted-from branch tip revision file
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(getSchemaName());
        FileRevision featureTipRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(fbDcIds.getBranchId(), filePromotionInfo.getFileId());
        if (featureTipRevision.getId().intValue() != filePromotionInfo.getFeatureBranchRevisionId().intValue()) {
            throw new QVCSRuntimeException("Feature tip revision mismatch. Bug in queries.");
        }
        serverResponsePromotionCreate.setFeatureBranchTipRevisionId(filePromotionInfo.getFeatureBranchRevisionId());

        File featureBranchTipRevisionFile = sourceControlBehaviorManager.getFileRevision(filePromotionInfo.getFeatureBranchRevisionId());
        serverResponsePromotionCreate.setMergedResultBuffer(Utility.readFileToBuffer(featureBranchTipRevisionFile));

        handleCreatePromotion(pbDcIds, filePromotionInfo, serverResponsePromotionCreate);
        String removeShortFileName = filePromotionInfo.getPromotedFromShortWorkfileName();
        SkinnyLogfileInfo promotedFromSkinnyInfo = serverResponsePromotionCreate.getPromotedFromSkinnyLogfileInfo();

        // We need to queue the notification so that it does not arrive at the client before the response, since
        // if it does, it will cause the file to be removed before the client has a chance to copy the file data to the
        // workfile directory.
        NotificationManager.getNotificationManager().queueNotification(response, fbDirectoryCoordinates, promotedFromSkinnyInfo, new Remove(removeShortFileName));

        serverResponsePromotionCreate.setSyncToken(getRequest().getSyncToken());
        List<ProvisionalDirectoryLocation> toBeNotifiedList = new ArrayList<>();
        sourceControlBehaviorManager.markPromoted(filePromotionInfo, toBeNotifiedList);
        notifyClientsOfNewDirectoryAsResultOfPromotion(parentBranchName, toBeNotifiedList);
        return serverResponsePromotionCreate;
    }

    private void handleCreatePromotion(DirectoryCoordinateIds pbDcIds, FilePromotionInfo filePromotionInfo,
            ServerResponsePromotionCreate serverResponsePromotionCreate) throws QVCSException {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(getSchemaName());
        SkinnyLogfileInfo promoteFromSkinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(filePromotionInfo.getFeatureBranchRevisionId());
        LogfileInfo logfileInfo = functionalQueriesDAO.getLogfileInfo(pbDcIds, filePromotionInfo.getPromotedFromShortWorkfileName(), filePromotionInfo.getFileId());
        serverResponsePromotionCreate.setLogfileInfo(logfileInfo);
        serverResponsePromotionCreate.setPromotedFromSkinnyLogfileInfo(promoteFromSkinnyInfo);
    }

    private void notifyClientsOfNewDirectoryAsResultOfPromotion(String promotedToBranchName, List<ProvisionalDirectoryLocation> toBeNotifiedList) {
        for (ProvisionalDirectoryLocation pdLocation : toBeNotifiedList) {
            String appendedPath = pdLocation.getAppendedPath();
            String[] directorySegments = appendedPath.split(java.io.File.separator);
            for (ServerResponseFactoryInterface responseFactory : QVCSEnterpriseServer.getConnectedUsers()) {
                // And let users who have the privilege know about this added directory.
                if (RolePrivilegesManager.getInstance().isUserPrivileged(getRequest().getProjectName(), responseFactory.getUserName(), RolePrivilegesManager.GET)) {
                    ServerResponseProjectControl serverResponse = new ServerResponseProjectControl();
                    serverResponse.setAddFlag(true);
                    serverResponse.setProjectName(getRequest().getProjectName());
                    serverResponse.setBranchName(promotedToBranchName);
                    serverResponse.setDirectorySegments(directorySegments);
                    serverResponse.setServerName(responseFactory.getServerName());
                    responseFactory.createServerResponse(serverResponse);
                    LOGGER.info("notifyClientsOfNewDirectoryAsResultOfPromotion: Sent created directory info for branch: [{}] directory: [{}] to user: [{}]",
                            promotedToBranchName, appendedPath, responseFactory.getUserName());
                }
            }
        }
    }

}
