//   Copyright 2004-2019 Jim Voris
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
 * This class manages the creation of file ID's. It must make sure that the file ID's that it provides via the getNewFileID() is
 * unique across multiple invocations of the server. The file ID is unique across the entire server. This class is a singleton.
 *
 * @author Jim Voris
 */
public final class FileIDManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(FileIDManager.class);

    /**
     * Wait 2 seconds before saving the latest file id.
     */
    private static final long SAVE_FILEID_DELAY = 1000L * 2L;
    private static final FileIDManager FILE_ID_MANAGER = new FileIDManager();
    private boolean isInitializedFlag;
    private String storeName;
    private String oldStoreName;
    private FileIDStore store;
    private boolean fileIDResetRequiredFlag;
    private SaveFileIdStoreTimerTask saveFileIdStoreTimerTask;

    /**
     * Creates a new instance of FileIDManager.
     */
    private FileIDManager() {
    }

    /**
     * Get the file id manager singleton.
     * @return the file id manager singleton.
     */
    public static FileIDManager getInstance() {
        return FILE_ID_MANAGER;
    }

    /**
     * Initialize the file id manager.
     * @return true if we are initialized.
     */
    public boolean initialize() {
        if (!isInitializedFlag) {
            oldStoreName = getStoreName() + ".old";

            loadStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
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
                    + QVCSConstants.QVCS_FILEID_STORE_NAME
                    + ".dat";
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
                store = (FileIDStore) inStream.readObject();
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new FileIDStore();

            // We need to scan for the maximum existing file ID.
            setFileIDResetRequiredFlag(true);
            LOGGER.warn("Must reset all file IDs.");

            // And write the store file.
            writeStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            if (fileStream != null) {
                try {
                    fileStream.close();
                    fileStream = null;
                } catch (IOException ex) {
                    LOGGER.warn(ex.getLocalizedMessage(), ex);
                }
            }

            // Serialization failed.  Create a default store.
            store = new FileIDStore();
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
     * Write the file id store.
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

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectOutputStream outStream = new ObjectOutputStream(fileStream)) {
                outStream.writeObject(store);
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } catch (RuntimeException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw e;
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
     * Get the next file id.
     *
     * @return the next file id.
     */
    public synchronized int getNewFileID() {
        scheduleSaveOfFileIdStore();
        return store.getNewFileID();
    }

    synchronized int getCurrentMaximumFileID() {
        return store.getCurrentMaximumFileID();
    }

    /**
     * Get the file id reset required flag.
     * @return the file id reset required flag.
     */
    public synchronized boolean getFileIDResetRequiredFlag() {
        return fileIDResetRequiredFlag;
    }

    /**
     * Set the file id reset required flag.
     * @param flag the file id reset required flag.
     */
    public synchronized void setFileIDResetRequiredFlag(boolean flag) {
        fileIDResetRequiredFlag = flag;
    }

    /**
     * Schedule the save of the file id store. We want to save the file id store after things are quiet for the SAVE_FILEID_DELAY
     * amount of time so that the file id will have been preserved in the case of a crash.
     */
    private void scheduleSaveOfFileIdStore() {
        if (saveFileIdStoreTimerTask != null) {
            saveFileIdStoreTimerTask.cancel();
            saveFileIdStoreTimerTask = null;
        }
        saveFileIdStoreTimerTask = new SaveFileIdStoreTimerTask();
        TimerManager.getInstance().getTimer().schedule(saveFileIdStoreTimerTask, SAVE_FILEID_DELAY);
    }

    /**
     * Use a timer to write the file id store after a while so it will have been saved before a crash.
     */
    class SaveFileIdStoreTimerTask extends TimerTask {

        @Override
        public void run() {
            LOGGER.info("Performing scheduled save of file id store.");
            writeStore();
        }
    }
}
