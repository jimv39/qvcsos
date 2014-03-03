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

import com.qumasoft.qvcslib.QVCSConstants;
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
 * This class manages the creation of directory ID's. It must make sure that the directory ID's that it provides via the
 * getNewDirectoryID() is unique across multiple invocations of the server. The directory ID must be unique within the scope of the
 * server. This class is a singleton.
 *
 * @author Jim Voris
 */
public final class DirectoryIDManager {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");

    /**
     * Wait 2 seconds before saving the latest file id
     */
    private static final long SAVE_DIRECTORYID_DELAY = 1000L * 2L;
    private static final DirectoryIDManager DIRECTORY_ID_MANAGER = new DirectoryIDManager();
    private boolean isInitializedFlag;
    private String storeName;
    private String oldStoreName;
    private DirectoryIDStore store;
    private SaveDirectoryIdStoreTimerTask saveDirectoryIdStoreTimerTask;

    /**
     * Creates a new instance of DirectoryIDManager.
     */
    private DirectoryIDManager() {
    }

    /**
     * Get the directory id manager singleton.
     * @return the directory id manager singleton.
     */
    public static DirectoryIDManager getInstance() {
        return DIRECTORY_ID_MANAGER;
    }

    /**
     * Initialize the directory id manager.
     * @return true if initialization was successful; false otherwise.
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
                    + QVCSConstants.QVCS_DIRECTORYID_STORE_NAME
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
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            store = (DirectoryIDStore) inStream.readObject();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new DirectoryIDStore();
            writeStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            if (fileStream != null) {
                try {
                    fileStream.close();
                    fileStream = null;
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(ex));
                }
            }

            // Serialization failed.  Create a default store.
            store = new DirectoryIDStore();
            writeStore();
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
            store.dump();
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
     * Get a new directory id.
     * @return a new directory id.
     */
    public synchronized int getNewDirectoryID() {
        scheduleSaveOfDirectoryIdStore();
        return store.getNewDirectoryID();
    }

    /**
     * Get the current maximum directory id.
     * @return the current maximum directory id.
     */
    public synchronized int getCurrentMaximumDirectoryID() {
        return store.getCurrentMaximumDirectoryID();
    }

    /**
     * Set the maximum directory id. This sets what the current maximum directory id is. It doesn't mean that the id's won't go higher than this... this becomes the new
     * floor of directory ids, so that any directories created after setting this value will have a value higher than the number set here.
     * @param maximumDirectoryId the maximum directory id.
     */
    public synchronized void setMaximumDirectoryID(int maximumDirectoryId) {
        scheduleSaveOfDirectoryIdStore();
        store.setMaximumDirectoryID(maximumDirectoryId);
    }

    /**
     * Schedule the save of the directory id store. We want to save the directory id store after things are quiet for the
     * SAVE_DIRECTORYID_DELAY amount of time so that the directory ids will have been preserved in the case of a crash.
     */
    private void scheduleSaveOfDirectoryIdStore() {
        if (saveDirectoryIdStoreTimerTask != null) {
            saveDirectoryIdStoreTimerTask.cancel();
            saveDirectoryIdStoreTimerTask = null;
        }
        saveDirectoryIdStoreTimerTask = new SaveDirectoryIdStoreTimerTask();
        TimerManager.getInstance().getTimer().schedule(saveDirectoryIdStoreTimerTask, SAVE_DIRECTORYID_DELAY);
    }

    /**
     * Use a timer to write the directory id store after a while so it will have been saved before a crash.
     */
    class SaveDirectoryIdStoreTimerTask extends TimerTask {

        @Override
        public void run() {
            LOGGER.log(Level.INFO, "Performing scheduled save of directory id store.");
            writeStore();
        }
    }
}
