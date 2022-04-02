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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to manage the collection of digests associated with a user's set of workfiles. Each user gets their own instance of the dictionary (since this work is done on the
 * client) It is a singleton.
 *
 * @author Jim Voris
 */
public final class WorkfileDigestManager {

    /**
     * Wait 10 seconds before saving the latest file id.
     */
    private static final long SAVE_WORKFILE_DIGEST_DELAY = 1000L * 10L;
    private static final WorkfileDigestManager WORKFILE_DIGEST_MANAGER_MEMBER = new WorkfileDigestManager();
    private boolean isInitializedFlag = false;
    private String storeName = null;
    private String oldStoreName = null;
    private WorkfileDigestDictionaryStore store = null;
    private MessageDigest messageDigest = null;
    private final Object messageDigestSyncObject = new Object();
    private SaveWorkfileDigestStoreTimerTask saveWorkfileDigestStoreTimerTask = null;
    // Create our logger object
    private static final transient Logger LOGGER = LoggerFactory.getLogger(WorkfileDigestManager.class);

    /**
     * Creates a new instance of WorkfileDigestDictionary.
     */
    private WorkfileDigestManager() {
        try {
            messageDigest = MessageDigest.getInstance(QVCSConstants.QVCSOS_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Failed to create [{}] digest instance! [{}] [{}]", QVCSConstants.QVCSOS_DIGEST_ALGORITHM, e.getClass().toString(), e.getLocalizedMessage());
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Get the singleton instance of the WorkfileDigestManager.
     *
     * @return the singleton instance of the WorkfileDigestManager.
     */
    public static WorkfileDigestManager getInstance() {
        return WORKFILE_DIGEST_MANAGER_MEMBER;
    }

    /**
     * Initialize the Workfile Digest Manager.
     *
     * @return true if initialization succeeded; false otherwise.
     */
    public boolean initialize() {
        if (!isInitializedFlag) {
            storeName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_META_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_WORKFILE_DIGEST_STORE_NAME
                    + System.getProperty("user.name")
                    + ".dat";

            oldStoreName = storeName + ".old";

            loadStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }

    /**
     * This method gets called from MergedInfo when we are computing the status that we display. This method is not meant to be called for any other purpose. In particular, this
     * method does not expect that the passed in workfileInfo object will contain a fetch date/time or the fetched revision string. For those kinds of updates to the digest, you
     * should call the updateWorkfileDigest method instead.
     *
     * @param workfileInfo the workfile information.
     * @param projectProperties the project properties.
     * @return the digest for the workfile.
     */
    public byte[] updateWorkfileDigestOnly(WorkfileInfoInterface workfileInfo, AbstractProjectProperties projectProperties) {
        if (workfileInfo == null) {
            throw new QVCSRuntimeException("Unexpected null value for workfileInfo argument.");
        }
        byte[] retVal = store.lookupWorkfileDigest(workfileInfo);
        WorkfileInfoInterface storedWorkfileInfo = null;

        // Need to make sure the workfile info hasn't changed, i.e.
        // we want to do a cursory check to see that we don't need
        // to re-compute the digest.
        boolean computeDigestNeeded = false;

        if (retVal != null) {
            storedWorkfileInfo = getDigestWorkfileInfo(workfileInfo);

            if (storedWorkfileInfo != null) {
                if (storedWorkfileInfo.getWorkfileSize() == workfileInfo.getWorkfileSize()) {
                    if (!storedWorkfileInfo.getWorkfileLastChangedDate().equals(workfileInfo.getWorkfileLastChangedDate())) {
                        computeDigestNeeded = true;
                    }
                } else {
                    computeDigestNeeded = true;
                }
            } else {
                computeDigestNeeded = true;
            }
        } else {
            // We didn't find an entry in the digest cache.  We need to compute
            // the digest, and store it away.
            computeDigestNeeded = true;
        }

        if (computeDigestNeeded) {
            if (storedWorkfileInfo != null) {
                if (storedWorkfileInfo.getFetchedDate() == 0L) {
                    LOGGER.warn("missing fetched date in stored workfile information for:" + workfileInfo.getShortWorkfileName());
                    retVal = computeWorkfileDigest(workfileInfo);
                } else {
                    workfileInfo.setFetchedDate(storedWorkfileInfo.getFetchedDate());
                    workfileInfo.setWorkfileRevisionString(storedWorkfileInfo.getWorkfileRevisionString());
                    retVal = computeWorkfileDigest(workfileInfo);
                }
            } else {
                retVal = computeWorkfileDigest(workfileInfo);
            }
        }
        return retVal;
    }

    /**
     * Update the digest value for a given workfile.
     * @param workfileInfo the workfile.
     * @param projectProperties the project properties.
     * @return the value of the workfile's digest.
     * @throws QVCSException if the workfileInfo doesn't have the fetched date, or if it doesn't have the workfile revision string.
     */
    public byte[] updateWorkfileDigest(WorkfileInfoInterface workfileInfo, AbstractProjectProperties projectProperties) throws QVCSException {
        byte[] retVal = store.lookupWorkfileDigest(workfileInfo);
        if (retVal == null) {
            retVal = computeWorkfileDigest(workfileInfo);
        } else {
            if ((workfileInfo.getFetchedDate() == 0L)
                    || (workfileInfo.getWorkfileRevisionString() == null)) {
                throw new QVCSException("Missing workfile information!");
            }

            retVal = computeWorkfileDigest(workfileInfo);
        }
        return retVal;
    }

    /**
     * Method to force an update to the workfile digest store for a successful merge operation so the workfile digest manager will 'think' that the latest revision successfully
     * fetched by the user is the one that they merged against.
     *
     * @param workfileBytes a byte array of the tip revision. i.e. the one that
     * we will 'think' we have now fetched.
     * @param workfileInfo the workfile info object the describes that workfile. This should describe the default revision, not the result of the merge.
     * @throws com.qumasoft.qvcslib.QVCSException when there is a problem.
     */
    public void updateWorkfileDigestForMerge(byte[] workfileBytes, WorkfileInfoInterface workfileInfo) throws QVCSException {
        synchronized (messageDigestSyncObject) {
            try {
                messageDigest.reset();
                byte[] digest = messageDigest.digest(workfileBytes);
                store.addWorkfileDigest(workfileInfo, digest);
            } catch (Exception e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
        scheduleSaveOfStore();
    }

    /**
     * Cancel the save of store task. This is needed for test code.
     */
    public synchronized void cancelSaveOfStoreTask() {
        if (saveWorkfileDigestStoreTimerTask != null) {
            saveWorkfileDigestStoreTimerTask.cancel();
            saveWorkfileDigestStoreTimerTask = null;
        }
    }

    private byte[] computeWorkfileDigest(WorkfileInfoInterface workfileInfo) {
        byte[] retVal = null;
        if (workfileInfo.getWorkfileExists()) {
            retVal = computeDigest(workfileInfo.getWorkfile());
            store.addWorkfileDigest(workfileInfo, retVal);
            scheduleSaveOfStore();
        }
        return retVal;
    }

    /**
     * Lookup the workfile info that is stored in the dictionary.
     * @param workfileInfo a workfile info object from which we build the lookup key to find the workfile info contained in the dictionary.
     * @return the workfile info from the dictionary.
     */
    public WorkfileInfoInterface getDigestWorkfileInfo(WorkfileInfoInterface workfileInfo) {
        WorkfileInfoInterface digestWorkfileInfo = null;
        if (workfileInfo != null) {
            digestWorkfileInfo = store.lookupWorkfileInfo(workfileInfo);
        }
        return digestWorkfileInfo;
    }

    private byte[] computeDigest(File workFile) {
        byte[] digest = null;
        FileInputStream inStream = null;

        synchronized (messageDigestSyncObject) {
            try {
                messageDigest.reset();

                inStream = new FileInputStream(workFile);
                byte[] buffer = new byte[(int) workFile.length()];
                Utility.readDataFromStream(buffer, inStream);
                LOGGER.trace("computing digest on buffer of size: " + buffer.length + " for file: " + workFile.getName());
                digest = messageDigest.digest(buffer);
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    }
                }
            }
        }

        return digest;
    }

    /**
     * Remove the digest for the given workfile.
     *
     * @param workfileInfo the workfile info for the file that should have its digest value removed from the digest dictionary.
     */
    public void removeWorkfileDigest(WorkfileInfoInterface workfileInfo) {
        store.removeWorkfileDigest(workfileInfo);
        scheduleSaveOfStore();
    }

    private void loadStore() {
        File storeFile;
        FileInputStream fileStream = null;

        try {
            storeFile = new File(storeName);
            fileStream = new FileInputStream(storeFile);

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectInputStream inStream = new ObjectInputStream(fileStream)) {
                store = (WorkfileDigestDictionaryStore) inStream.readObject();
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new WorkfileDigestDictionaryStore();
        } catch (IOException | ClassNotFoundException e) {
            // Serialization failed.  Create a default store.
            store = new WorkfileDigestDictionaryStore();
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Write the digest store to disk.
     */
    public synchronized void writeStore() {
        FileOutputStream fileStream = null;
        ObjectOutputStream outStream = null;

        try {
            File storeFile = new File(storeName);
            File oldStoreFile = new File(oldStoreName);

            if (oldStoreFile.exists()) {
                oldStoreFile.delete();
            }

            if (storeFile.exists()) {
                storeFile.renameTo(oldStoreFile);
            }

            File newStoreFile = new File(storeName);

            // Make sure the needed directories exists
            if (!newStoreFile.getParentFile().exists()) {
                newStoreFile.getParentFile().mkdirs();
            }

            fileStream = new FileOutputStream(newStoreFile);
            outStream = new ObjectOutputStream(fileStream);
            outStream.writeObject(store);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            if (fileStream != null) {
                try {
                    if (outStream != null) {
                        outStream.close();
                    }
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Schedule the save of the file id store. We want to save the file id store after things are quiet for the SAVE_WORKFILE_DIGEST_DELAY amount of time so that the file id will
     * have been preserved in the case of a crash.
     */
    private synchronized void scheduleSaveOfStore() {
        if (saveWorkfileDigestStoreTimerTask != null) {
            saveWorkfileDigestStoreTimerTask.cancel();
            saveWorkfileDigestStoreTimerTask = null;
        }
        saveWorkfileDigestStoreTimerTask = new SaveWorkfileDigestStoreTimerTask();
        TimerManager.getInstance().getTimer().schedule(saveWorkfileDigestStoreTimerTask, SAVE_WORKFILE_DIGEST_DELAY);
    }

    /**
     * Use a timer to write the digest store after a while so it will have been saved before a crash.
     */
    class SaveWorkfileDigestStoreTimerTask extends TimerTask {

        @Override
        public void run() {
            LOGGER.info("Performing scheduled save of workfile digest store.");
            writeStore();
        }
    }
}
