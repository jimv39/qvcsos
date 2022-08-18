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
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.logfileaction.AddFile;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponsePromotionMoveAndRename;
import com.qumasoft.server.NotificationManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.FileRevision;
import java.io.IOException;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestPromotionMoveAndRename extends AbstractClientRequestPromoteFile {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestPromotionMove.class);

    ClientRequestPromotionMoveAndRename(ClientRequestPromoteFileData data) {
        super(data);
    }

    @Override
    ServerResponseInterface executePromotion(String userName, DirectoryCoordinate fbDirectoryCoordinates, DirectoryCoordinate pbDirectoryCoordinates, DirectoryCoordinateIds fbDcIds,
            DirectoryCoordinateIds pbDcIds, String parentBranchName, FilePromotionInfo fpi, ServerResponseFactoryInterface response) throws QVCSException, IOException, SQLException {
        LOGGER.info("Moving and renaming file from: [{}/{}] to [{}/{}]",
                fpi.getPromotedToAppendedPath(), fpi.getPromotedToShortWorkfileName(),
                fpi.getPromotedFromAppendedPath(), fpi.getPromotedFromShortWorkfileName());
        ServerResponsePromotionMoveAndRename serverResponsePromotionMoveAndRename = new ServerResponsePromotionMoveAndRename();
        buildCommonResponseData(fbDcIds, pbDcIds, parentBranchName, fpi, serverResponsePromotionMoveAndRename);
        serverResponsePromotionMoveAndRename.setMergedInfoSyncShortWorkfileName(fpi.getPromotedFromShortWorkfileName());
        handleMoveAndRenamePromotion(pbDcIds, fpi, serverResponsePromotionMoveAndRename);
        String removeShortFileName = fpi.getPromotedFromShortWorkfileName();
        SkinnyLogfileInfo promotedFromSkinnyInfo = serverResponsePromotionMoveAndRename.getPromotedFromSkinnyLogfileInfo();

        // We need to queue the notification so that it does not arrive at the client before the response, since
        // if it does, it will cause the file to be removed before the client has a chance to copy the file data to the
        // workfile directory.
        NotificationManager.getNotificationManager().queueNotification(response, fbDirectoryCoordinates, promotedFromSkinnyInfo, new Remove(removeShortFileName));

        // Queue a 2nd notification to update the feature branch with the skinnyInfo of the Trunk. This will be an AddFile notification, since we just did a remove.
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(getSchemaName());
        FileRevision parentBranchFileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(pbDcIds.getBranchId(), promotedFromSkinnyInfo.getFileID());
        SkinnyLogfileInfo parentSkinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(parentBranchFileRevision.getId());
        NotificationManager.getNotificationManager().queueNotification(response, pbDirectoryCoordinates, parentSkinnyInfo, new AddFile());

        return serverResponsePromotionMoveAndRename;
    }

    private ServerResponseInterface handleMoveAndRenamePromotion(DirectoryCoordinateIds pbDcIds, FilePromotionInfo filePromotionInfo,
            ServerResponsePromotionMoveAndRename serverResponsePromotionMoveAndRename) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(getSchemaName());
        FileRevision newestPromotedToFileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(pbDcIds.getBranchId(), filePromotionInfo.getFileId());
        serverResponsePromotionMoveAndRename.setParentBranchTipRevisionId(newestPromotedToFileRevision.getId());

        SkinnyLogfileInfo promotedToSkinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(newestPromotedToFileRevision.getId());
        serverResponsePromotionMoveAndRename.setPromotedToSkinnyLogfileInfo(promotedToSkinnyInfo);

        SkinnyLogfileInfo promotedFromSkinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(filePromotionInfo.getFeatureBranchRevisionId());
        serverResponsePromotionMoveAndRename.setPromotedFromSkinnyLogfileInfo(promotedFromSkinnyInfo);
        return serverResponsePromotionMoveAndRename;
    }

}
