/*   Copyright 2004-2022 Jim Voris
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workfile Directory Manager. Keep track of the workfiles for a given directory.
 *
 * @author Jim Voris
 */
public final class WorkfileDirectoryManager implements WorkfileDirectoryManagerInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkfileDirectoryManager.class);

    private final String directoryName;
    private File directory;
    // The container for our workfile information.
    private final Map<String, WorkfileInfoInterface> workfileMap = Collections.synchronizedMap(new TreeMap<>());
    private ArchiveDirManagerInterface archiveDirManager = null;
    private DirectoryManager directoryManager = null;

    /**
     * Creates a new instance of WorkfileDirectoryManager.
     * @param workfileDirectory the workfile directory.
     * @param archiveManager the archive directory manager for the associated archive directory manager.
     * @param dirManager the directory manager for the associated DirectoryManager.
     */
    public WorkfileDirectoryManager(String workfileDirectory, ArchiveDirManagerInterface archiveManager, DirectoryManager dirManager) {
        directoryName = workfileDirectory;
        archiveDirManager = archiveManager;
        directoryManager = dirManager;
        initDirectory();
    }

    private void initDirectory() {
        try {
            directory = new File(directoryName);
            File[] fileList = directory.listFiles();
            if (fileList == null) {
                return;
            }
            for (File workFile : fileList) {
                if (workFile.isDirectory()) {
                    continue;
                }
                if (QvcsosClientIgnoreManager.getInstance().ignoreFile(this.archiveDirManager.getAppendedPath(), workFile)) {
                    LOGGER.debug("Ignoring file: [{}] due to an entry in .qvcsosignore", workFile.getAbsolutePath());
                    continue;
                }
                try {
                    boolean binaryFileFlag = false;
                    ArchiveInfoInterface archiveInfo = archiveDirManager.getArchiveInfo(workFile.getName());
                    if (archiveInfo != null) {
                        binaryFileFlag = archiveInfo.getAttributes().getIsBinaryfile();
                    }
                    WorkfileInfo workfileInfo = new WorkfileInfo(workFile, binaryFileFlag, archiveDirManager.getProjectName(), archiveDirManager.getBranchName());
                    workfileMap.put(workfileInfo.getShortWorkfileName(), workfileInfo);
                } catch (IOException e) {
                    // Log the exception.  There isn't anything we can do about it.
                    LOGGER.warn("IOException when creating workfile information for [{}]", workFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize workfile directory: " + directoryName + ". Caught exception: " + e.getLocalizedMessage());
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Collection<WorkfileInfoInterface> getWorkfileCollection() {
        return workfileMap.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorkfileDirectory() {
        return directoryName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateWorkfileInfo(WorkfileInfoInterface workfileInfo) throws QVCSException {
        workfileMap.put(workfileInfo.getShortWorkfileName(), workfileInfo);
        MergedInfoInterface mergedInfo = directoryManager.getMergedInfo(workfileInfo.getShortWorkfileName());
        if (mergedInfo != null) {
            mergedInfo.setWorkfileInfo(workfileInfo);
        }

        // Update the workfile digest.
        WorkfileDigestManager.getInstance().updateWorkfileDigest(workfileInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void refresh() {
        workfileMap.clear();
        initDirectory();
    }

    /**
     * Does the workfile directory exist.
     * @return true if it exists; false otherwise.
     */
    public boolean directoryExists() {
        File dir = new File(directoryName);
        return dir.exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createDirectory() {
        boolean createdDirectory = false;
        if (!directoryExists()) {
            File dir = new File(directoryName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    LOGGER.warn("Failed to create archive directory: " + dir.getAbsolutePath());
                    return false;
                } else {
                    createdDirectory = true;
                }
            }
        }
        return createdDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized WorkfileInfoInterface lookupWorkfileInfo(String shortWorkfileName) {
        WorkfileInfoInterface workfileInfo = workfileMap.get(shortWorkfileName);
        return workfileInfo;
    }
}
