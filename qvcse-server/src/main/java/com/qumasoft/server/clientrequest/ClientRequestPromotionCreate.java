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
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponsePromotionCreate;
import com.qumasoft.server.NotificationManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.FileRevision;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestPromotionCreate extends AbstractClientRequestPromoteFile {

    ClientRequestPromotionCreate(ClientRequestPromoteFileData data) {
        super(data);
    }

    @Override
    ServerResponseInterface executePromotion(String userName, DirectoryCoordinate fbDirectoryCoordinates, DirectoryCoordinate pbDirectoryCoordinates, DirectoryCoordinateIds fbDcIds,
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
        serverResponsePromotionCreate.setPromotionType(getRequest().getFilePromotionInfo().getTypeOfPromotion());

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

}
