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
package com.qumasoft.server;

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.TimerManager;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Directory Id dictionary. This is a singleton.
 * @author Jim Voris
 */
public final class DirectoryIDDictionary {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");

    private static final DirectoryIDDictionary DIRECTORY_ID_DICTIONARY = new DirectoryIDDictionary();
    /**
     * Wait 5 seconds before saving the file store
     */
    private static final long SAVE_DELAY = 1000L * 5L;
    private boolean isInitializedFlag;
    private String storeName;
    private String oldStoreName;
    private DirectoryIDDictionaryStore store;
    private SaveFileStoreTimerTask saveFileStoreTimerTask;

    /**
     * Creates a new instance of DirectoryIDDictionary
     */
    private DirectoryIDDictionary() {
    }

    /**
     * Get the directory id dictionary singleton.
     * @return the directory id dictionary singleton.
     */
    public static DirectoryIDDictionary getInstance() {
        return DIRECTORY_ID_DICTIONARY;
    }

    /**
     * Initialize the directory id dictionary singleton.
     */
    public void initialize() {
        if (!isInitializedFlag) {
            oldStoreName = getStoreName() + ".old";
            loadStore();
            isInitializedFlag = true;
        }
    }

    void resetStore() {
        File storeFile = new File(getStoreName());
        if (storeFile.exists()) {
            storeFile.delete();
        }
    }

    private String getStoreName() {
        if (storeName == null) {
            storeName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_META_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_DIRECTORYID_DICT_STORE_NAME
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
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            store = (DirectoryIDDictionaryStore) inStream.readObject();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new DirectoryIDDictionaryStore();
            writeStore();
        } catch (IOException | ClassNotFoundException e) {
            // Serialization failed.  Create a default store.
            store = new DirectoryIDDictionaryStore();
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    /**
     * Write the backing store to disk.
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
            ObjectOutputStream outStream = new ObjectOutputStream(fileStream);
            outStream.writeObject(store);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    /**
     * Associate a directory id to an archive directory manager.
     * @param directoryID the directory id.
     * @param archiveDirManager the archive directory manager.
     */
    public synchronized void put(int directoryID, ArchiveDirManager archiveDirManager) {
        DictionaryIDInfo dictionaryIDInfo = new DictionaryIDInfo(archiveDirManager.getProjectName(), archiveDirManager.getAppendedPath());
        store.saveDictionaryIDInfo(directoryID, dictionaryIDInfo);
        scheduleSaveOfFileStore();
    }

    /**
     * Lookup the archive directory manager associated with the given parameters.
     * @param projectName the project name.
     * @param directoryID the directory id.
     * @param response link to the client.
     * @param discardObsoleteFilesFlag true if we should discard obsolete files (i.e. move obsolete files to the cemetery).
     * @return the associated archive directory manager.
     * @throws QVCSException if something goes wrong.
     */
    public synchronized ArchiveDirManager lookupArchiveDirManager(String projectName, int directoryID, ServerResponseFactoryInterface response,
            boolean discardObsoleteFilesFlag) throws QVCSException {
        ArchiveDirManager archiveDirManager = null;
        DictionaryIDInfo dictionaryIDInfo = store.retrieveDictionaryIDInfo(projectName, directoryID);
        if (dictionaryIDInfo != null) {
            archiveDirManager = buildArchiveDirManager(dictionaryIDInfo, QVCSConstants.QVCS_SERVER_USER, response, discardObsoleteFilesFlag);
        }
        return archiveDirManager;
    }

    private ArchiveDirManager buildArchiveDirManager(DictionaryIDInfo dictionaryIDInfo, String userName, ServerResponseFactoryInterface response,
            boolean discardObsoleteFilesFlag) throws QVCSException {
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(dictionaryIDInfo.getProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, dictionaryIDInfo.getAppendedPath());
        return (ArchiveDirManager) ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, directoryCoordinate,
                QVCSConstants.QVCS_SERVED_PROJECT_TYPE, userName, response, discardObsoleteFilesFlag);
    }

    /**
     * Schedule the save of directory id dictionary store. We want to save the directory id dictionary store after things are quiet
     * for the SAVE_DELAY amount of time so that the file will have been preserved in the case of a crash.
     */
    private void scheduleSaveOfFileStore() {
        if (saveFileStoreTimerTask != null) {
            saveFileStoreTimerTask.cancel();
            saveFileStoreTimerTask = null;
        }
        saveFileStoreTimerTask = new SaveFileStoreTimerTask();
        TimerManager.getInstance().getTimer().schedule(saveFileStoreTimerTask, SAVE_DELAY);
    }

    /**
     * Use a timer to write the file store after a while so it will have been saved before a crash.
     */
    class SaveFileStoreTimerTask extends TimerTask {

        @Override
        public void run() {
            LOGGER.log(Level.INFO, "Performing scheduled save of directory id dictionary.");
            writeStore();
        }
    }
}
