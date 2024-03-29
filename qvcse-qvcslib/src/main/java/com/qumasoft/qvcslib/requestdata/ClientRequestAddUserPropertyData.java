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
package com.qumasoft.qvcslib.requestdata;

import com.qumasoft.qvcslib.UserPropertyData;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestAddUserPropertyData extends ClientRequestClientData {
    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.SYNC_TOKEN
    };
    private String clientComputerName;
    private UserPropertyData userPropertyData;
    private String propertiesKey;

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.ADD_USER_PROPERTY;
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

    /**
     * @return the clientComputerName
     */
    public String getClientComputerName() {
        return clientComputerName;
    }

    /**
     * @param computerName the clientComputerName to set
     */
    public void setClientComputerName(String computerName) {
        this.clientComputerName = computerName;
    }

}
