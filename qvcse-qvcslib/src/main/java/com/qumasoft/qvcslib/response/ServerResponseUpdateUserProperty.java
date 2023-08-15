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
import com.qumasoft.qvcslib.UserPropertyData;

/**
 *
 * @author Jim Voris.
 */
public class ServerResponseUpdateUserProperty extends AbstractServerResponse {
    // This is what gets serialized.
    private UserPropertyData userPropertyData;

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_UPDATE_USER_PROPERTY;
    }

    /**
     * @return the userPropertyData
     */
    public UserPropertyData getUserPropertyData() {
        return userPropertyData;
    }

    /**
     * @param upData the userPropertyData to set
     */
    public void setUserPropertyData(UserPropertyData upData) {
        this.userPropertyData = upData;
    }

}
