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
import java.util.List;

/**
 *
 * @author Jim Voris.
 */
public class ServerResponseGetUserProperties extends AbstractServerResponse {
    // These are serialized:
    private List<UserPropertyData> userPropertyList;
    private String propertiesKey;

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_USER_PROPERTIES;
    }

    /**
     * Get the user property list.
     * @return the user property list.
     */
    public List<UserPropertyData> getUserPropertyList() {
        return userPropertyList;
    }

    /**
     * Set the user property list.
     * @param upList the user property list.
     */
    public void setUserPropertyList(List<UserPropertyData> upList) {
        this.userPropertyList = upList;
    }

    /**
     * @return the propertiesKey
     */
    public String getPropertiesKey() {
        return propertiesKey;
    }

    /**
     * @param key the propertiesKey to set
     */
    public void setPropertiesKey(String key) {
        this.propertiesKey = key;
    }

}