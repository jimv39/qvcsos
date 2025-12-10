/*
 * Copyright 2021-2025 Jim Voris.
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
 * Factory singleton for creating/managing LogFileProxyCache's.
 *
 * @author Jim Voris.
 */
public final class LogFileProxyCacheFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileProxyCacheFactory.class);
    // This is a singleton.
    private static Map<String, LogFileProxyCacheFactory> logFileProxyCacheFactoryMap = null;
    private final Map<Integer, LogFileProxyCache> logFileProxyCacheMap;
    private final String serverName;

    /**
     * Creates a new instance of LogFileProxyCacheFactory.
     *
     * @param server the server name.
     */
    private LogFileProxyCacheFactory(String server) {
        this.serverName = server;
        logFileProxyCacheMap = Collections.synchronizedMap(new TreeMap<>());
    }

    /**
     * Get the LogFileProxyCache factory for the given server.
     *
     * @param server the server name.
     * @return the LogFileProxyCache factory singleton.
     */
    public static LogFileProxyCacheFactory getInstance(String server) {
        LogFileProxyCacheFactory logFileProxyCacheFactory;
        if (null == logFileProxyCacheFactoryMap) {
            logFileProxyCacheFactoryMap = new TreeMap<>();
        }
        logFileProxyCacheFactory = logFileProxyCacheFactoryMap.get(server);
        if (logFileProxyCacheFactory == null) {
            logFileProxyCacheFactory = new LogFileProxyCacheFactory(server);
            logFileProxyCacheFactoryMap.put(server, logFileProxyCacheFactory);
        }
        return logFileProxyCacheFactory;
    }

    public LogFileProxyCache getLogFileProxyCache(Integer projectId) {
        LogFileProxyCache cache = logFileProxyCacheMap.get(projectId);
        if (cache == null) {
            cache = new LogFileProxyCache(projectId);
            logFileProxyCacheMap.put(projectId, cache);
            LOGGER.info("Adding proxy cache for server [{}] with projectId: [{}]", this.serverName, projectId);
        }
        return cache;
    }
}
