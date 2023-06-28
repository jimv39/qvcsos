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
package com.qumasoft.guitools.qwin;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.LoggerFactory;

/**
 * Singleton class to manage listeners that monitor for changes to a branch's
 * .qvcsosignore file.
 *
 * @author Jim Voris
 */
public final class IgnoreListenersManager {

    // Create our logger object
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IgnoreListenersManager.class);

    private static final IgnoreListenersManager IGNORE_LISTENERS_MANAGER = new IgnoreListenersManager();
    private final ExecutorService ignoreFileThreadPool;
    private Map<String, MonitorQvcsosignoreChanges> mapOfMonitors;

    private IgnoreListenersManager() {

        // Create the thread pool where we create the listener threads for changes to .qvcsosignore files.
        ignoreFileThreadPool = Executors.newCachedThreadPool();

        mapOfMonitors = new TreeMap<>();

    }

    public static IgnoreListenersManager getInstance() {
        return IGNORE_LISTENERS_MANAGER;
    }

    public void createOrResetListener(String workfileBaseDirectory) {
        LOGGER.info("Workfile base directory: {}", workfileBaseDirectory);
        MonitorQvcsosignoreChanges listener = mapOfMonitors.get(workfileBaseDirectory);
        if (listener == null) {
            // Need to create a brand new listener...
            listener = new MonitorQvcsosignoreChanges(workfileBaseDirectory);
            mapOfMonitors.put(workfileBaseDirectory, listener);
            ignoreFileThreadPool.execute(listener);
        }
    }
}
