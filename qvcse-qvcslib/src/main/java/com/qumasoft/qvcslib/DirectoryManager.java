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

import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory Manager. Manage the combined (merged) model of workfiles and archive files.
 * @author Jim Voris
 */
public class DirectoryManager implements DirectoryManagerInterface {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryManager.class);

    private ArchiveDirManagerInterface archiveDirManager;
    private WorkfileDirectoryManagerInterface workfileDirectoryManager;
    private final String userName;
    private final String projectName;
    private final String branchName;
    private boolean hasChangedFlag = false;
    private final Map<String, MergedInfoInterface> mergedMap = Collections.synchronizedMap(new TreeMap<>());
    private final Map<Integer, MergedInfoInterface> mergedFileIdMap = Collections.synchronizedMap(new TreeMap<>());

    /**
     * Creates a new instance of DirectoryManager.
     * @param user user name.
     * @param project project name.
     * @param branch branch name.
     */
    public DirectoryManager(String user, String project, String branch) {
        this.userName = user;
        this.projectName = project;
        this.branchName = branch;
    }

    @Override
    public ArchiveDirManagerInterface getArchiveDirManager() {
        return archiveDirManager;
    }

    void setArchiveDirManager(ArchiveDirManagerInterface archiveDirMgr) {
        this.archiveDirManager = archiveDirMgr;
    }

    @Override
    public WorkfileDirectoryManagerInterface getWorkfileDirectoryManager() {
        return workfileDirectoryManager;
    }

    /**
     * Set the workfile directory manager.
     * @param workfileDirManager the workfile directory manager.
     */
    public void setWorkfileDirectoryManager(WorkfileDirectoryManagerInterface workfileDirManager) {
        this.workfileDirectoryManager = workfileDirManager;
    }

    @Override
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, String filename) throws IOException, QVCSException {
        if (archiveDirManager != null) {
            return archiveDirManager.createArchive(commandLineArgs, filename, null);
        } else {
            throw new QVCSRuntimeException("createArchive called but archiveDirManager is null!!");
        }
    }

    /**
     * Merge the entries from the archive directory and the entries from the workfile directory into a single combined branch of the set of the files.
     * @throws QVCSException the archive directory manager or the workfile directory manager is null.
     */
    @Override
    public synchronized void mergeManagers() throws QVCSException {
        boolean cemeteryFlag = false;
        boolean ignoreFileFlag = false;

        // The archive directory manager manager must be defined...
        if (archiveDirManager == null) {
            LOGGER.error("archive directory manager is not defined!!");
            throw new QVCSException("archive directory manager is not defined!!");
        }
        if (0 == archiveDirManager.getAppendedPath().compareTo(QVCSConstants.QVCSOS_CEMETERY_FAKE_APPENDED_PATH)) {
            cemeteryFlag = true;
        } else {
            if (workfileDirectoryManager == null) {
                LOGGER.error("workfile directory manager is not defined!!");
                throw new QVCSException("workfile directory manager is not defined!!");
            }
        }
        LOGGER.trace("DirectoryManager.mergeManagers for project::branch/appendedPath [{}]::[{}]/[{}] on thread: [{}]", getProjectName(), getBranchName(), getAppendedPath(),
                Thread.currentThread().getName());

        QvcsosClientIgnoreManager ignoreManager = QvcsosClientIgnoreManager.getInstance();

        // Do this in a while loop so we'll repeat the merge if we catch a
        // concurrent modification exception.  This latter can happen if we
        // get an update from the server while the merge is in progress.
        boolean continueWhileLoop = true;
        while (continueWhileLoop) {
            boolean concurrentExceptionThrown = false;
            try {
                // Make sure to start fresh.
                mergedMap.clear();
                mergedFileIdMap.clear();

                // Add the workfiles first.
                if (!cemeteryFlag) {
                    Iterator<WorkfileInfoInterface> workfilesIterator = getWorkfileDirectoryManager().getWorkfileCollection().iterator();
                    while (workfilesIterator.hasNext()) {
                        WorkfileInfoInterface workfileInfo = workfilesIterator.next();
                        File workFile = new File(workfileInfo.getFullWorkfileName());
                        ignoreFileFlag = ignoreManager.ignoreFile(getAppendedPath(), workFile);
                        if (ignoreFileFlag) {
                            LOGGER.debug("Ignoring workfile: [{}] because of entry in .qvcsosingore.", workfileInfo.getFullWorkfileName());
                        } else {
                            MergedInfoInterface mergedInfo = new MergedInfo(workfileInfo, getArchiveDirManager(), getProjectName(), getUserName());
                            mergedMap.put(mergedInfo.getMergedInfoKey(), mergedInfo);
                        }
                    }
                }

                // Add the archives, merging them with the existing workfile entries
                // and/or creating new entries for archives that do not have
                // corresponding workfiles.
                Iterator<ArchiveInfoInterface> archivesIterator = getArchiveDirManager().getArchiveInfoCollection().values().iterator();
                while (archivesIterator.hasNext()) {
                    ArchiveInfoInterface archiveInfo = archivesIterator.next();
                    MergedInfoInterface mergedInfo = getMergedInfo(archiveInfo.getShortWorkfileName());
                    File workFile;
                    if (mergedInfo == null) {
                        if (!cemeteryFlag) {
                            workFile = new File(getWorkfileDirectoryManager().getWorkfileDirectory() + File.separator + archiveInfo.getShortWorkfileName());
                            ignoreFileFlag = ignoreManager.ignoreFile(getAppendedPath(), workFile);
                            if (ignoreFileFlag) {
                                LOGGER.debug("Ignoring archive for file: [{}] because of entry in .qvcsosingore.", workFile.getCanonicalPath());
                            }
                        }
                        if (!ignoreFileFlag) {
                            mergedInfo = new MergedInfo(archiveInfo, getArchiveDirManager(), getProjectName(), getUserName());
                            mergedMap.put(mergedInfo.getMergedInfoKey(), mergedInfo);
                            mergedFileIdMap.put(archiveInfo.getFileID(), mergedInfo);
                        }
                    } else {
                        mergedInfo.setArchiveInfo(archiveInfo);
                        mergedFileIdMap.put(archiveInfo.getFileID(), mergedInfo);
                    }
                }
                setHasChanged(true);
            } catch (java.util.ConcurrentModificationException e) {
                LOGGER.info(e.getClass().toString() + ":" + e.getLocalizedMessage());
                concurrentExceptionThrown = true;
            } catch (IOException e) {
                throw new QVCSException(e.getClass().toString() + ":" + e.getLocalizedMessage());
            } finally {
                if (concurrentExceptionThrown) {
                    LOGGER.info("Will re-try building of merged information for [" + getAppendedPath() + "]");
                } else {
                    continueWhileLoop = false;
                }
            }
        }
    }

    @Override
    public Collection<MergedInfoInterface> getMergedInfoCollection() {
        Collection<MergedInfoInterface> collection;
        synchronized (mergedMap) {
            collection = new ArrayList<>(mergedMap.values());
        }
        return collection;
    }

    @Override
    public MergedInfoInterface getMergedInfo(String shortWorkfileName) {
        return mergedMap.get(getMergedMapKey(shortWorkfileName));
    }

    @Override
    public MergedInfoInterface getMergedInfoByFileId(Integer fileId) {
        return mergedFileIdMap.get(fileId);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        archiveDirManager.addChangeListener(listener);
    }

    @Override
    public String getAppendedPath() {
        return archiveDirManager.getAppendedPath();
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        archiveDirManager.removeChangeListener(listener);
    }

    @Override
    public int getCount() {
        return mergedMap.size();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getBranchName() {
        return branchName;
    }

    @Override
    public boolean getHasChanged() {
        return hasChangedFlag;
    }

    @Override
    public void setHasChanged(boolean flag) {
        hasChangedFlag = flag;
    }

    private String getMergedMapKey(String shortWorkfileName) {
        String key;
        key = shortWorkfileName;
        return key;
    }
}
