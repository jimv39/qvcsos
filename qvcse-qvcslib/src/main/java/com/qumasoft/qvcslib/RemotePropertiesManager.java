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
package com.qumasoft.qvcslib;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the various remote properties.
 *
 * @author Jim Voris.
 */
public final class RemotePropertiesManager {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(RemotePropertiesManager.class);

    private static final RemotePropertiesManager REMOTE_PROPERTIES_MANAGER = new RemotePropertiesManager();
    private final Map<String, RemotePropertiesBaseClass> remoteProperitesMap;

    private RemotePropertiesManager() {
        remoteProperitesMap = Collections.synchronizedMap(new TreeMap<>());
    }

    /**
     * Get the Remote Properties Manager singleton.
     * @return the Remote Properties Manager singleton.
     */
    public static RemotePropertiesManager getInstance() {
        return REMOTE_PROPERTIES_MANAGER;
    }

    public RemotePropertiesBaseClass getRemoteProperties(String userName, TransportProxyInterface proxy) {
        String propertiesKey = makePropertyKey(userName, proxy.getTransportProxyKey());
        RemotePropertiesBaseClass remoteProperties = remoteProperitesMap.get(propertiesKey);
        if (remoteProperties == null) {
            remoteProperties = createRemoteProperties(userName, proxy, propertiesKey);
            LOGGER.info("Created remote properties for key: [{}]", propertiesKey);
        }
        return remoteProperties;
    }

    private String makePropertyKey(String userName, String proxyKey) {
        String key = userName + "-" + proxyKey;
        return key;
    }

    private RemotePropertiesBaseClass createRemoteProperties(String userName, TransportProxyInterface proxy, String propertiesKey) {
        RemotePropertiesBaseClass returnedProperties = new RemotePropertiesBaseClass(userName, proxy, propertiesKey);
        this.remoteProperitesMap.put(propertiesKey, returnedProperties);
        return returnedProperties;
    }
}
