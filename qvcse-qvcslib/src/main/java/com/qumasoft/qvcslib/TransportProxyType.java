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
package com.qumasoft.qvcslib;

/**
 * Transport proxy type.
 * TODO -- This should probably just be an enum.
 * @author Jim Voris
 */
public class TransportProxyType {

    private final String proxyType;

    /**
     * Creates new TransportProxyType.
     * @param proxType the type of proxy.
     */
    public TransportProxyType(String proxType) {
        proxyType = proxType;
    }

    /**
     * Get the transport type.
     * @return the transport type.
     */
    public String getTransportType() {
        return proxyType;
    }

    /**
     * The transport type.
     * @return the transport type.
     */
    @Override
    public String toString() {
        return proxyType;
    }
}
