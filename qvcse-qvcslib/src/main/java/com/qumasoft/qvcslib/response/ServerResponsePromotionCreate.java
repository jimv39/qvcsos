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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.PromoteFileResults;
import com.qumasoft.qvcslib.PromoteFileResultsHelper;
import com.qumasoft.qvcslib.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ServerResponsePromotionCreate extends AbstractServerResponsePromoteFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResponsePromotionCreate.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_PROMOTE_FILE_CREATE;
    }

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        String message = String.format("ServerResponsePromotionCreate Promoted file: [%s::%s/%s] to [%s::%s/%s]", getMergedInfoSyncBranchName(), getMergedInfoSyncAppendedPath(),
                getMergedInfoSyncShortWorkfileName(),
                getPromotedToBranchName(), getPromotedToAppendedPath(), getPromotedToShortWorkfileName());
        PromoteFileResults promoteFileResults = new PromoteFileResults(this);
        PromoteFileResultsHelper promoteFileResultsHelper = Utility.getInstance().getSyncObjectForFileId(getPromotedFromSkinnyLogfileInfo().getFileID());
        synchronized (promoteFileResultsHelper) {
            promoteFileResultsHelper.setPromoteFileResults(promoteFileResults);
            promoteFileResultsHelper.notifyAll();
        }
        directoryManagerProxy.updateInfo(message);
        LOGGER.info(message);
    }

}
