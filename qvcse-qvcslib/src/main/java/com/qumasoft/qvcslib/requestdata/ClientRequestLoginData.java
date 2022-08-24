/*   Copyright 2004-2014 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib.requestdata;

/**
 * Login request data.
 * @author Jim Voris
 */
public class ClientRequestLoginData extends ClientRequestClientData {
    private static final long serialVersionUID = -7105830351304007174L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.PASSWORD,
        ValidRequestElementType.SYNC_TOKEN
    };
    private String version;

    /**
     * Creates a new instance of ClientRequestLogin.
     */
    public ClientRequestLoginData() {
    }

    /**
     * Get the client version.
     * @return the client version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the client version.
     * @param v the client version.
     */
    public void setVersion(String v) {
        version = v;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.LOGIN;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
