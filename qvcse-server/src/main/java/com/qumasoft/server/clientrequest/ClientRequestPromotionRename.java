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
import com.qumasoft.qvcslib.response.ServerResponsePromotionRename;
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
public class ClientRequestPromotionRename extends AbstractClientRequestPromoteFile {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestPromotionRename.class);

    ClientRequestPromotionRename(ClientRequestPromoteFileData data) {
        super(data);
    }

    @Override
    ServerResponseInterface executePromotion(String userName, DirectoryCoordinate fbDirectoryCoordinates, DirectoryCoordinate pbDirectoryCoordinates, DirectoryCoordinateIds fbDcIds,
            DirectoryCoordinateIds pbDcIds, String parentBranchName, FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) throws QVCSException, IOException, SQLException {
        LOGGER.info("Changing file name from: [{}] to [{}]", filePromotionInfo.getPromotedToShortWorkfileName(), filePromotionInfo.getPromotedFromShortWorkfileName());
        ServerResponsePromotionRename serverResponsePromotionRename = new ServerResponsePromotionRename();
        buildCommonResponseData(fbDcIds, pbDcIds, parentBranchName, filePromotionInfo, serverResponsePromotionRename);
        serverResponsePromotionRename.setMergedInfoSyncShortWorkfileName(filePromotionInfo.getPromotedFromShortWorkfileName());
        handleRenamePromotion(pbDcIds, filePromotionInfo, serverResponsePromotionRename);
        String removeShortFileName = filePromotionInfo.getPromotedFromShortWorkfileName();
        SkinnyLogfileInfo promotedFromSkinnyInfo = serverResponsePromotionRename.getPromotedFromSkinnyLogfileInfo();

        // We need to queue the notification so that it does not arrive at the client before the response, since
        // if it does, it will cause the file to be removed before the client has a chance to copy the file data to the
        // workfile directory.
        NotificationManager.getNotificationManager().queueNotification(response, fbDirectoryCoordinates, promotedFromSkinnyInfo, new Remove(removeShortFileName));

        // Send a 2nd notification to update the feature branch with the skinnyInfo of the Trunk. This will be an AddFile notification, since we just did a remove.
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(getSchemaName());
        FileRevision parentBranchFileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(pbDcIds.getBranchId(), promotedFromSkinnyInfo.getFileID());
        SkinnyLogfileInfo parentSkinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(parentBranchFileRevision.getId());
        NotificationManager.getNotificationManager().queueNotification(response, fbDirectoryCoordinates, parentSkinnyInfo, new AddFile());

        return serverResponsePromotionRename;
    }

    private void handleRenamePromotion(DirectoryCoordinateIds pbDcIds, FilePromotionInfo filePromotionInfo, ServerResponsePromotionRename serverResponsePromotionRename) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(getSchemaName());
        FileRevision newestPromotedToFileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(pbDcIds.getBranchId(), filePromotionInfo.getFileId());
        serverResponsePromotionRename.setParentBranchTipRevisionId(newestPromotedToFileRevision.getId());

        SkinnyLogfileInfo promotedToSkinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(newestPromotedToFileRevision.getId());
        serverResponsePromotionRename.setPromotedToSkinnyLogfileInfo(promotedToSkinnyInfo);

        SkinnyLogfileInfo promotedFromSkinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(filePromotionInfo.getFeatureBranchRevisionId());
        serverResponsePromotionRename.setPromotedFromSkinnyLogfileInfo(promotedFromSkinnyInfo);
    }
}
