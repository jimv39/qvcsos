/*   Copyright 2004-2026 Jim Voris
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

import com.qumasoft.qvcslib.response.ServerResponseChangePassword;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to manage the collection of digests associated with a user's set of workfiles. For each server, there is a different digest store.
 *
 * @author Jim Voris
 */
public final class WorkfileDigestManager implements PasswordChangeListenerInterface {

    /**
     * Wait 10 seconds before saving the latest file id.
     */
    private static final long SAVE_WORKFILE_DIGEST_DELAY = 1000L * 10L;
    private static final WorkfileDigestManager WORKFILE_DIGEST_MANAGER_MEMBER = new WorkfileDigestManager();
    private String activeServerName = "UnknownServer";
    private Map<String, WorkfileDigestDictionaryStore> storeMap = new TreeMap<>();
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

    @Override
    public void notifyLoginResult(ServerResponseLogin response) {
        if (response.getLoginResult()) {
            // The login succeeded. Initialize the workfile digest for this user/server.
            QVCSConstants.setServerName(response.getServerName());
            initializeDigestStoreForServer(response.getServerName());
        }
    }

    private void initializeDigestStoreForServer(String serverName) {
        String storeName = getStoreName(serverName);
        loadStore(storeName);
    }

    private String getStoreName(String serverName) {
        String storeName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_META_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_WORKFILE_DIGEST_STORE_NAME
                + serverName
                + ".dat";
        return storeName;
    }

    /**
     * This method gets called from MergedInfo when we are computing the status that we display. This method is not meant to be called for any other purpose. In particular, this
     * method does not expect that the passed in workfileInfo object will contain a fetch date/time or the fetched revision string. For those kinds of updates to the digest, you
     * should call the updateWorkfileDigest method instead.
     *
     * @param workfileInfo the workfile information.
     * @return the digest for the workfile.
     */
    public byte[] updateWorkfileDigestOnly(WorkfileInfoInterface workfileInfo) {
        if (workfileInfo == null) {
            throw new QVCSRuntimeException("Unexpected null value for workfileInfo argument.");
        }
        WorkfileDigestDictionaryStore store = storeMap.get(QVCSConstants.getServerName());
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
     * @return the value of the workfile's digest.
     * @throws QVCSException if the workfileInfo doesn't have the fetched date, or if it doesn't have the workfile revision string.
     */
    public byte[] updateWorkfileDigest(WorkfileInfoInterface workfileInfo) throws QVCSException {
        WorkfileDigestDictionaryStore store = storeMap.get(QVCSConstants.getServerName());
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
                WorkfileDigestDictionaryStore store = storeMap.get(QVCSConstants.getServerName());
                store.addWorkfileDigest(workfileInfo, digest);
            } catch (Exception e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
        scheduleSaveOfStores();
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
            WorkfileDigestDictionaryStore store = storeMap.get(QVCSConstants.getServerName());
            store.addWorkfileDigest(workfileInfo, retVal);
            scheduleSaveOfStores();
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
            WorkfileDigestDictionaryStore store = storeMap.get(QVCSConstants.getServerName());
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
        WorkfileDigestDictionaryStore store = storeMap.get(QVCSConstants.getServerName());
        store.removeWorkfileDigest(workfileInfo);
        scheduleSaveOfStores();
    }

    private void loadStore(String storeName) {
        File storeFile;
        FileInputStream fileStream = null;

        try {
            storeFile = new File(storeName);
            fileStream = new FileInputStream(storeFile);

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectInputStream inStream = new ObjectInputStream(fileStream)) {
                WorkfileDigestDictionaryStore store = (WorkfileDigestDictionaryStore) inStream.readObject();
                storeMap.put(QVCSConstants.getServerName(), store);
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            WorkfileDigestDictionaryStore store = new WorkfileDigestDictionaryStore();
            storeMap.put(QVCSConstants.getServerName(), store);
        } catch (IOException | ClassNotFoundException e) {
            // Serialization failed.  Create a default store.
            WorkfileDigestDictionaryStore store = new WorkfileDigestDictionaryStore();
            storeMap.put(QVCSConstants.getServerName(), store);
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
     * Write the digest stores to disk.
     */
    public synchronized void writeStores() {
        for (String serverName : storeMap.keySet()) {
            FileOutputStream fileStream = null;
            ObjectOutputStream outStream = null;

            try {
                String storeName = getStoreName(serverName);
                String oldStoreName = storeName + ".old";

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
                WorkfileDigestDictionaryStore store = storeMap.get(serverName);
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
    }

    /**
     * Schedule the save of the digest stores. We want to save the workfile digest stores after things are quiet for the SAVE_WORKFILE_DIGEST_DELAY amount of time so that the workfile digests will
     * be preserved for next time the application runs.
     */
    private synchronized void scheduleSaveOfStores() {
        if (saveWorkfileDigestStoreTimerTask != null) {
            saveWorkfileDigestStoreTimerTask.cancel();
            saveWorkfileDigestStoreTimerTask = null;
        }
        saveWorkfileDigestStoreTimerTask = new SaveWorkfileDigestStoreTimerTask();
        TimerManager.getInstance().getTimer().schedule(saveWorkfileDigestStoreTimerTask, SAVE_WORKFILE_DIGEST_DELAY);
    }

    @Override
    public void notifyPasswordChange(ServerResponseChangePassword response) {
    }

    @Override
    public void savePendingPassword(String serverName, String password) {
    }

    @Override
    public String getPendingPassword(String serverName) {
        return null;
    }

    /**
     * Use a timer to write the digest store after a while so it will have been saved before a crash.
     */
    class SaveWorkfileDigestStoreTimerTask extends TimerTask {

        @Override
        public void run() {
            LOGGER.info("Performing scheduled save of workfile digest store.");
            writeStores();
        }
    }

    /**
     * @param serverName the activeServerName to set
     */
    public void setActiveServerName(String serverName) {
        this.activeServerName = serverName;
    }

    /**
     * @return the activeServerName
     */
    public String getActiveServerName() {
        return activeServerName;
    }
}
