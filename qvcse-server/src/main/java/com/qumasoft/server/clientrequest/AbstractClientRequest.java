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

import com.qumasoft.qvcslib.requestdata.ClientRequestClientData;

/**
 *
 * @author Jim Voris
 */
public abstract class AbstractClientRequest implements ClientRequestInterface {

    /**
     * The request message.
     */
    private ClientRequestClientData request;

    @Override
    public Integer getSyncToken() {
        return request.getSyncToken();
    }

    /**
     * @return the request
     */
    public ClientRequestClientData getRequest() {
        return request;
    }

    /**
     * @param requestData the request to set
     */
    public void setRequest(ClientRequestClientData requestData) {
        this.request = requestData;
    }
}
