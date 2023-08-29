/*
 * Copyright 2023 Jim Voris.
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
import com.qumasoft.qvcslib.CommonFilterFileCollection;
import java.util.List;

/**
 *
 * @author Jim Voris.
 */
public class ServerResponseUpdateFilterFileCollection extends AbstractServerResponse {
    private String serverName;
    private List<CommonFilterFileCollection> commonFilterFileCollectionList;

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_UPDATE_FILTER_FILE_COLLECTION;
    }

    /**
     * @return the commonFilterFileCollectionList
     */
    public List<CommonFilterFileCollection> getCommonFilterFileCollectionList() {
        return commonFilterFileCollectionList;
    }

    /**
     * @param commonFilterCollectionList the commonFilterFileCollectionList to set
     */
    public void setCommonFilterFileCollectionList(List<CommonFilterFileCollection> commonFilterCollectionList) {
        this.commonFilterFileCollectionList = commonFilterCollectionList;
    }

    /**
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param svrName the serverName to set
     */
    public void setServerName(String svrName) {
        this.serverName = svrName;
    }

}
