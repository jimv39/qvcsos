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
 * Default server properties. This is a singleton.
 * @author Jim Voris
 */
public final class DefaultServerProperties extends com.qumasoft.qvcslib.ServerProperties {

    private static final DefaultServerProperties DEFAULT_SERVER_PROPERTIES = new DefaultServerProperties();

    /**
     * Creates a new instance of DefaultServerProperties.
     */
    private DefaultServerProperties() {
        initProperties();
    }

    /**
     * Get the singleton instance of the default server properties.
     * @return the singleton instance of the default server properties.
     */
    public static DefaultServerProperties getInstance() {
        return DEFAULT_SERVER_PROPERTIES;
    }

    /**
     * Initialize the properties for the default project.
     */
    private void initProperties() {
        // Define some default values
        getActualProperties().put(getServerNameTag(), QVCSConstants.QVCS_DEFAULT_SERVER_NAME);
        getActualProperties().put(getServerIPAddressTag(), "");
        getActualProperties().put(getClientPortTag(), "9889");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return QVCSConstants.QVCS_SERVERS_NAME;
    }
}
