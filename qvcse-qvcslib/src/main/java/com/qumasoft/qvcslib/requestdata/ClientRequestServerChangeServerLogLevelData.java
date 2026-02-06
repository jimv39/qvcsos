/*
 * Copyright 2026 Jim Voris.
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

/**
 *
 * @author Jim Voris
 */
public class ClientRequestServerChangeServerLogLevelData extends ClientRequestClientData {

    private final ValidRequestElementType[] validElements = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.PASSWORD,
        ValidRequestElementType.SYNC_TOKEN
    };
    private String logLevel;

    @Override
    public ValidRequestElementType[] getValidElements() {
        return validElements;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SERVER_CHANGE_LOG_LEVEL;
    }

    /**
     * @return the logLevel
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * @param newlogLevel the logLevel to set
     */
    public void setLogLevel(String newlogLevel) {
        this.logLevel = newlogLevel;
    }

}
