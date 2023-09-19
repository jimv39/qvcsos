/*
 * Copyright 2021-2023 Jim Voris.
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

import com.qumasoft.qvcslib.requestdata.ClientRequestClientData;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton to put synchronization all in one place so that even an error
 * response can release the wait... at least that's the idea.
 *
 * @author Jim Voris
 */
public final class SynchronizationManager {

    private static final SynchronizationManager SYNCHRONIZATION_MANAGER = new SynchronizationManager();

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationManager.class);

    // Use this AtomicInteger to create Integer token to identify the synchronization object.
    private AtomicInteger tokenIdCreator = new AtomicInteger(1);

    // Where we store the sync objects.
    private final Map<Integer, Object> syncObjectsMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * Creates a new instance of Synchronization Manager.
     */
    private SynchronizationManager() {
    }

    /**
     * Get the Synchronization Manager singleton.
     *
     * @return the Synchronization Manager singleton.
     */
    public static SynchronizationManager getSynchronizationManager() {
        return SYNCHRONIZATION_MANAGER;
    }

    /**
     * Get a synchronization token (just an Integer).
     *
     * @return an Integer that acts as a token for the internal sync object.
     */
    public Integer getSynchronizationToken() {
        Integer token = tokenIdCreator.getAndAdd(1);
        Object syncObject = new Object();
        syncObjectsMap.put(token, syncObject);
        return token;
    }

    /**
     * Wait on the internal sync object associated with the given token.
     *
     * @param transportProxy
     * @param request
     */
    public void waitOnToken(TransportProxyInterface transportProxy, ClientRequestClientData request) {
        Integer token = request.getSyncToken();
        LOGGER.info("Waiting for token: [{}] on thread: [{}]", token, Thread.currentThread().getName());
        Object syncObject = syncObjectsMap.get(token);
        if (syncObject != null) {
            synchronized (syncObject) {
                transportProxy.write(request);
                try {
                    syncObject.wait();
                } catch (InterruptedException e) {
                    LOGGER.warn("Interrupted exception.", e);
                }
            }
        } else {
            LOGGER.warn("Sync token not found in waitOnToken: [{}]", token);
        }
    }

    /**
     * Notify any threads waiting on the internal sync object associated with
     * the given token.
     *
     * @param token the token that identifies the internal sync object.
     */
    public void notifyOnToken(Integer token) {
        LOGGER.info("Notify for token: [{}]", token);
        if (token != null) {
            Object syncObject = syncObjectsMap.get(token);
            if (syncObject != null) {
                synchronized (syncObject) {
                    syncObject.notifyAll();
                }
                syncObjectsMap.remove(token);
            } else {
                LOGGER.warn("Sync token not found in notifyOnToken: [{}]", token);
            }
        } else {
            LOGGER.warn("Null token");
        }
    }

}
