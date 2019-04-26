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
package com.qumasoft.clientapi;

/**
 * A factory class used to create classes that provide implementations for the
 * {@link ClientAPI} interface and the {@link ClientAPIContext} interface.
 *
 * @author Jim Voris
 */
public final class ClientAPIFactory {

    /**
     * Make this private so it cannot be used.
     */
    private ClientAPIFactory() {
    }

    /**
     * Create a class that implements the {@link ClientAPI} interface.
     *
     * @param clientAPIContext the client API context object that will be used
     * to hold state for the created ClientAPI.
     *
     * @return a new instance of a class that implements the {@link ClientAPI}
     * interface.
     */
    public static ClientAPI createClientAPI(ClientAPIContext clientAPIContext) {
        ClientAPIContextImpl clientAPIContextImpl = (ClientAPIContextImpl) clientAPIContext;
        return new ClientAPIImpl(clientAPIContextImpl);
    }

    /**
     * Create a class that implements the {@link ClientAPIContext} interface.
     * Use this method to create the ClientAPIContext that you subsequently pass
     * to the createClientAPI factory method.
     *
     * @return a new instance of a class that implements the
     * {@link ClientAPIContext} interface.
     */
    public static ClientAPIContext createClientAPIContext() {
        return new ClientAPIContextImpl();
    }
}
