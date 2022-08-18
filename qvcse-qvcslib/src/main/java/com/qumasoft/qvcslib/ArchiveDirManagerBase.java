/*   Copyright 2004-2021 Jim Voris
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
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive directory manager base class.
 *
 * @author Jim Voris
 */
public abstract class ArchiveDirManagerBase implements ArchiveDirManagerInterface {

    protected static final long UPDATE_DELAY = 1000;
    /**
     * Create our logger object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManagerBase.class);
    private final String instanceProjectName;
    private final String instanceBranchName;
    private final String instanceAppendedPath;
    private final String instanceUserName;
    private boolean instanceFastNotifyFlag = false;
    private DirectoryManagerInterface instanceDirectoryManager = null;
    private Timer instanceTimer = null;
    private TimerTask instanceNotifyListenerTask = null;
    private final EventListenerList instanceChangeListenerArray = new EventListenerList();
    private final Map<String, ArchiveInfoInterface> instanceArchiveInfoCollection = Collections.synchronizedMap(new TreeMap<>());

    /**
     * Creates a new instance of ArchiveDirManagerBase.
     *
     * @param projectName project name.
     * @param branchName name of the branch.
     * @param appendedPath the appended path.
     * @param userName user name
     */
    public ArchiveDirManagerBase(String projectName, String branchName, String appendedPath, String userName) {
        instanceBranchName = branchName;
        instanceAppendedPath = appendedPath;
        instanceUserName = userName;

        instanceProjectName = projectName;

        // Create our daemon timer task so we can aggregate updates.
        instanceTimer = TimerManager.getInstance().getTimer();
    }

    /**
     * Get the collection of archive's for this directory.
     *
     * @return the collection of archive's for this directory.
     */
    @Override
    public Map<String, ArchiveInfoInterface> getArchiveInfoCollection() {
        return instanceArchiveInfoCollection;
    }

    /**
     * Get the appended path.
     *
     * @return the appended path.
     */
    @Override
    public String getAppendedPath() {
        return instanceAppendedPath;
    }

    /**
     * Get the appended path of the parent directory.
     *
     * @return the appended path of the parent directory.
     */
    protected String getParentAppendedPath() {
        if (getAppendedPath().length() > 0) {
            int lastForwardSlashIndex = getAppendedPath().lastIndexOf("/");
            int lastBackSlashIndex = getAppendedPath().lastIndexOf("\\");
            int maxIndex;
            if (lastForwardSlashIndex > lastBackSlashIndex) {
                maxIndex = lastForwardSlashIndex;
            } else {
                maxIndex = lastBackSlashIndex;
            }
            if (maxIndex > 0) {
                String parentAppendedPath = getAppendedPath().substring(0, maxIndex);
                return parentAppendedPath;
            } else {
                return "";
            }
        } else {
            return null;
        }
    }

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    @Override
    public String getProjectName() {
        return instanceProjectName;
    }

    /**
     * Get the user name.
     *
     * @return the user name.
     */
    @Override
    public String getUserName() {
        return instanceUserName;
    }

    /**
     * Get the branch name.
     *
     * @return the branch name.
     */
    @Override
    public String getBranchName() {
        return instanceBranchName;
    }

    /**
     * <p>
     * TODO Thinking about the problem of limiting the server memory footprint. One approach is to put a shim into this getArchiveInfo method so that it goes to a cache to find the
     * archive info. If the logfile exists in the cache, then all is well, and we return the logfile object we find in the cache. If the logfile has been purged from the cache,
     * then we'll need to re-create the logfile instance, add it to the cache, and age away the oldest element in the cache so that the size of the cache remains limited to some
     * specified watermark. This approach implies that the creation of logfile instances also puts them into the cache. If the cache uses a strategy of keeping the most recently
     * used logfiles, then the least recently used logfiles will fall off the end of the cache and not consume memory. The worst case is to have a cache size of 1 which would allow
     * just a single logfile in the cache. The code should be tested to make sure that it can handle a cache size of 1. A more reasonable cache size would be 2,000 or so. You can
     * make it larger if you're willing to increase the memory size of the JVM.</p>
     * <p>
     * Get the archive info for the given file.
     * </p>
     *
     * @param shortWorkfileName the name of the workfile whose archive info should be returned.
     * @return the archive info object associated with the given workfile. This will return a null if the archive is not found.
     */
    @Override
    public ArchiveInfoInterface getArchiveInfo(String shortWorkfileName) {
        return instanceArchiveInfoCollection.get(shortWorkfileName);
    }

    /**
     * Add a change listener.
     *
     * @param listener the new listener.
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        synchronized (instanceChangeListenerArray) {
            instanceChangeListenerArray.add(ChangeListener.class, listener);
        }
    }

    /**
     * Remove a change listener.
     *
     * @param listener the listener to remove.
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        synchronized (instanceChangeListenerArray) {
            instanceChangeListenerArray.remove(ChangeListener.class, listener);
        }
    }

    @Override
    public void notifyListeners() {
        // Cancel pending notify listener task.
        if (instanceNotifyListenerTask != null) {
            instanceNotifyListenerTask.cancel();
        }

        if (getFastNotify()) {
            synchronized (instanceChangeListenerArray) {
                Object[] listeners = instanceChangeListenerArray.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    javax.swing.event.ChangeEvent event = new javax.swing.event.ChangeEvent(this);
                    ((ChangeListener) listeners[i + 1]).stateChanged(event);
                }
            }
        } else {
            final ArchiveDirManagerBase finalThis = this;

            instanceNotifyListenerTask = new TimerTask() {

                @Override
                public void run() {
                    if (instanceDirectoryManager != null) {
                        try {
                            instanceDirectoryManager.mergeManagers();
                        } catch (QVCSException e) {
                            LOGGER.warn("notifyListeners caught unexpected exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                            LOGGER.warn(e.getLocalizedMessage(), e);
                        }
                    }
                    synchronized (getChangeListenerArray()) {
                        Object[] listeners = getChangeListenerArray().getListenerList();
                        for (int i = listeners.length - 2; i >= 0; i -= 2) {
                            javax.swing.event.ChangeEvent event = new javax.swing.event.ChangeEvent(finalThis);
                            ((ChangeListener) listeners[i + 1]).stateChanged(event);
                        }
                    }
                }
            };
            instanceTimer.schedule(instanceNotifyListenerTask, UPDATE_DELAY);
        }
    }

    @Override
    public void setDirectoryManager(DirectoryManagerInterface directoryManager) {
        instanceDirectoryManager = directoryManager;
    }

    /**
     * Get the directory Manager associated with this archive dir manager proxy.
     * @return the directory manager instance.
     */
    public DirectoryManagerInterface getDirectoryManager() {
        return instanceDirectoryManager;
    }

    @Override
    public boolean getFastNotify() {
        return instanceFastNotifyFlag;
    }

    @Override
    public void setFastNotify(boolean flag) {
        instanceFastNotifyFlag = flag;
    }

    @Override
    public void createReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile, byte[] buffer) {
        // Figure out where the reference copy goes...
        String referenceLocation = projectProperties.getReferenceLocation();
        String fullReferenceDirectory;
        if (getAppendedPath().length() == 0) {
            fullReferenceDirectory = referenceLocation;
        } else {
            fullReferenceDirectory = referenceLocation + File.separator + getAppendedPath();
        }
        String fullReferencePath = fullReferenceDirectory + File.separator + logfile.getShortWorkfileName();
        java.io.FileOutputStream outputStream = null;

        try {
            File referenceDirectoryFile = new File(fullReferenceDirectory);

            // Make sure the directory exists.
            referenceDirectoryFile.mkdirs();
            File referenceWorkfile = new File(fullReferencePath);
            outputStream = new java.io.FileOutputStream(referenceWorkfile);
            // We only have to write the file to the reference location.
            outputStream.write(buffer);
        } catch (IOException e) {
            LOGGER.warn("Caught exception creating reference copy: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            LOGGER.warn("Caught exception creating reference copy for: " + logfile.getShortWorkfileName());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (java.io.IOException e) {
                    LOGGER.warn("Caught IOException in createReferenceCopy: " + e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public void deleteReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile) {
        // Figure out where the reference copy goes...
        String referenceLocation = projectProperties.getReferenceLocation();
        String fullReferenceDirectory;
        if (getAppendedPath().length() == 0) {
            fullReferenceDirectory = referenceLocation;
        } else {
            fullReferenceDirectory = referenceLocation + File.separator + getAppendedPath();
        }
        String fullReferencePath = fullReferenceDirectory + File.separator + logfile.getShortWorkfileName();

        try {
            // Delete reference file.
            File referenceFile = new File(fullReferencePath);
            referenceFile.delete();
        } catch (Exception e) {
            LOGGER.warn("Caught exception deleting reference copy: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            LOGGER.warn("Caught exception deleting reference copy for: " + logfile.getShortWorkfileName());
        }
    }

    /**
     * @return the instanceChangeListenerArray
     */
    public EventListenerList getChangeListenerArray() {
        return instanceChangeListenerArray;
    }
}
