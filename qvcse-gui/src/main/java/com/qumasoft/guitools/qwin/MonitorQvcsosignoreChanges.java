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

import com.qumasoft.qvcslib.QvcsosClientIgnoreManager;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.slf4j.LoggerFactory;

/**
 * Monitor changes to .qvcsosignore files.
 *
 * @author Jim Voris
 */
public class MonitorQvcsosignoreChanges implements Runnable {

    // Create our logger object
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MonitorQvcsosignoreChanges.class);

    private final String branchRootWorkfileDirectory;
    private boolean continueFlag;

    public MonitorQvcsosignoreChanges(String branchRoot) {
        branchRootWorkfileDirectory = branchRoot;
        continueFlag = true;
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(branchRootWorkfileDirectory);

            path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey key;
            while (continueFlag && (key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().equals(QvcsosClientIgnoreManager.getQvcsosIgnoreFilename())) {
                        System.out.println("Event kind:" + event.kind()
                                + ". File affected: " + event.context() + ".");
                        System.out.println("Resetting .qvcsosignore for: " + branchRootWorkfileDirectory);
                        boolean foundFlag = QvcsosClientIgnoreManager.getInstance().resetIgnoreData(branchRootWorkfileDirectory);
                        if (foundFlag) {
                            QWinFrame.getQWinFrame().refreshCurrentBranch();
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Caught exception: " + e.getLocalizedMessage());
        }
    }

    public void clearContinueFlag() {
        LOGGER.info("Clearing continue flag for: {}", branchRootWorkfileDirectory);
        continueFlag = false;
    }
}
