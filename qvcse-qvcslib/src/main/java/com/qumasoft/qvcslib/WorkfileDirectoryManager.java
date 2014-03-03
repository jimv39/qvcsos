//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Workfile Directory Manager. Keep track of the workfiles for a given directory.
 *
 * @author Jim Voris
 */
public final class WorkfileDirectoryManager implements WorkfileDirectoryManagerInterface {

    private final String directoryName;
    private File directory;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
    // The container for our workfile information.
    private final Map<String, WorkfileInfoInterface> workfileMap = Collections.synchronizedMap(new TreeMap<String, WorkfileInfoInterface>());
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
            for (File fileList1 : fileList) {
                if (fileList1.isDirectory()) {
                    continue;
                }
                try {
                    boolean keywordExpansionFlag = false;
                    boolean binaryFileFlag = false;
                    ArchiveInfoInterface archiveInfo = archiveDirManager.getArchiveInfo(fileList1.getName());
                    if (archiveInfo != null) {
                        keywordExpansionFlag = archiveInfo.getAttributes().getIsExpandKeywords();
                        binaryFileFlag = archiveInfo.getAttributes().getIsBinaryfile();
                    }
                    WorkfileInfo workfileInfo = new WorkfileInfo(fileList1, keywordExpansionFlag, binaryFileFlag, archiveDirManager.getProjectName());
                    workfileMap.put(workfileInfo.getShortWorkfileName(), workfileInfo);
                } catch (IOException e) {
                    // Log the exception.  There isn't anything we can do about it.
                    LOGGER.log(Level.WARNING, "IOException when creating workfile information for " + fileList1.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to initialize workfile directory: " + directoryName + ". Caught exception: " + e.getLocalizedMessage());
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
        WorkfileDigestManager.getInstance().updateWorkfileDigest(workfileInfo, archiveDirManager.getProjectProperties());
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
                    LOGGER.log(Level.WARNING, "Failed to create archive directory: " + dir.getAbsolutePath());
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
