/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.TimerManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class and its associated store class are meant to capture the location of archive files, i.e. you can ask this class to
 * identify the directory that contains the given fileID at this point in time. If a file moves, then its entry in the store
 * maintained here gets updated to point to its new home.
 *
 * @author Jim Voris
 */
public final class FileIDDictionary {
    // This is a singleton.
    private static final FileIDDictionary FILE_ID_DICTIONARY = new FileIDDictionary();
    /**
     * Wait 2 seconds before saving the latest file id dictionary
     */
    private static final long SAVE_FILEID_DICTIONARY_DELAY = 1000L * 2L;
    private SaveFileIdDictionaryStoreTimerTask saveFileIdDictionaryStoreTimerTask;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(FileIDDictionary.class);
    private boolean isInitializedFlag;
    private String storeName;
    private String oldStoreName;
    private FileIDDictionaryStore store;

    /**
     * Creates a new instance of FileIDDictionary.
     */
    private FileIDDictionary() {
    }

    /**
     * Get the FileIDDictionary instance.
     *
     * @return the singleton FileIDDictionary.
     */
    public static FileIDDictionary getInstance() {
        return FILE_ID_DICTIONARY;
    }

    /**
     * Initialize the dictionary.
     */
    public void initialize() {
        if (!isInitializedFlag) {
            oldStoreName = getStoreName() + ".old";

            loadStore();
            isInitializedFlag = true;
        }
    }

    /**
     * Reset the dictionary store.
     */
    void resetStore() {
        File storeFile = new File(getStoreName());
        if (storeFile.exists()) {
            storeFile.delete();
        }
    }

    /**
     * Get the name of the store.
     *
     * @return the name of the store.
     */
    private String getStoreName() {
        if (storeName == null) {
            storeName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_META_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_FILEID_DICT_STORE_NAME
                    + "dat";
        }
        return storeName;
    }

    private synchronized void loadStore() {
        File storeFile;
        FileInputStream fileStream = null;

        try {
            storeFile = new File(getStoreName());
            fileStream = new FileInputStream(storeFile);

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectInputStream inStream = new ObjectInputStream(fileStream)) {
                store = (FileIDDictionaryStore) inStream.readObject();
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new FileIDDictionaryStore();
            writeStore();
        } catch (IOException | ClassNotFoundException e) {
            // Serialization failed.  Create a default store.
            store = new FileIDDictionaryStore();
            writeStore();
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
     * Write the store to disk.
     */
    public synchronized void writeStore() {
        FileOutputStream fileStream = null;

        try {
            File storeFile = new File(getStoreName());
            File oldStoreFile = new File(oldStoreName);

            if (oldStoreFile.exists()) {
                oldStoreFile.delete();
            }

            if (storeFile.exists()) {
                storeFile.renameTo(oldStoreFile);
            }

            File newStoreFile = new File(getStoreName());

            // Make sure the needed directories exists
            if (!newStoreFile.getParentFile().exists()) {
                newStoreFile.getParentFile().mkdirs();
            }

            fileStream = new FileOutputStream(newStoreFile);
            try (ObjectOutputStream outStream = new ObjectOutputStream(fileStream)) {
                outStream.writeObject(store);
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
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
     * Save the association between a given file (within a project) and its owing directory.
     *
     * @param projectName the name of the project.
     * @param branchName the name of the branch.
     * @param fileID the fileID to look up
     * @param appendedPath the file's appended path.
     * @param shortFilename the file's short workfile name.
     * @param directoryID the directory id of the directory where the file is located.
     */
    public synchronized void saveFileIDInfo(String projectName, String branchName, int fileID, String appendedPath, String shortFilename, int directoryID) {
        store.saveFileIDInfo(projectName, branchName, fileID, appendedPath, shortFilename, directoryID);
        scheduleSaveOfFileIdStore();
    }

    /**
     * Lookup the directory that contains the archive file for the given fileID.
     *
     * @param projectName the name of the project.
     * @param branchName the name of the branch.
     * @param fileID file file's fileID.
     * @return the fileIDInfo for the given file.
     */
    public synchronized FileIDInfo lookupFileIDInfo(String projectName, String branchName, int fileID) {
        return store.lookupFileIDInfo(projectName, branchName, fileID);
    }

    /**
     * Remove the file id's for a given project. This should be called when a project is deleted.
     *
     * @param projectName the name of the project.
     * @return true on success (i.e. we found the project, and pruned it from the store), false otherwise.
     */
    public synchronized boolean removeIDsForProject(String projectName) {
        boolean retVal = store.removeIDsForProject(projectName);
        if (retVal) {
            scheduleSaveOfFileIdStore();
        }
        return retVal;
    }

    /**
     * Remove the file id's for the given branch for a given project. This should be called when a branch is deleted.
     *
     * @param projectName the name of the project.
     * @param branchName the name of the branch within that project.
     * @return true on success (i.e. we found the project and the branch, and pruned the branch from the store). false otherwise.
     */
    public synchronized boolean removeIDsForBranch(String projectName, String branchName) {
        boolean retVal = store.removeIDsForBranch(projectName, branchName);
        if (retVal) {
            scheduleSaveOfFileIdStore();
        }
        return retVal;
    }

    /**
     * Schedule the save of the file id dictionary store. We want to save the file id dictionary store after things are quiet for
     * the SAVE_FILEID_DICTIONARY_DELAY amount of time so that the file id dictionary will have been preserved in the case of a
     * crash.
     */
    private void scheduleSaveOfFileIdStore() {
        if (saveFileIdDictionaryStoreTimerTask != null) {
            saveFileIdDictionaryStoreTimerTask.cancel();
            saveFileIdDictionaryStoreTimerTask = null;
        }
        saveFileIdDictionaryStoreTimerTask = new SaveFileIdDictionaryStoreTimerTask();
        TimerManager.getInstance().getTimer().schedule(saveFileIdDictionaryStoreTimerTask, SAVE_FILEID_DICTIONARY_DELAY);
    }

    /**
     * Use a timer to write the file id dictionary store after a while so it will have been saved before a crash.
     */
    class SaveFileIdDictionaryStoreTimerTask extends TimerTask {

        @Override
        public void run() {
            LOGGER.info("Performing scheduled save of file id dictionary store.");
            writeStore();
        }
    }
}
