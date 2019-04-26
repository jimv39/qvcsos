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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to manage the checkout comments for QVCS-Enterprise client.
 *
 * @author Jim Voris
 */
public final class CheckOutCommentManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckOutCommentManager.class);
    private static final CheckOutCommentManager CHECKOUT_COMMENT_MANAGER = new CheckOutCommentManager();
    private boolean isInitializedFlag = false;
    private String storeName = null;
    private String oldStoreName = null;
    private CheckOutCommentStore store = null;

    /**
     * Creates a new instance of CheckOutCommentManager.
     */
    private CheckOutCommentManager() {
    }

    /**
     * Get the singleton instance.
     * @return the singleton instance.
     */
    public static CheckOutCommentManager getInstance() {
        return CHECKOUT_COMMENT_MANAGER;
    }

    /**
     * Initialize the checkout comment manager.
     * @return true if initialization was successful; false otherwise.
     */
    public boolean initialize() {
        if (!isInitializedFlag) {
            storeName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_USER_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_CHECKOUT_COMMENT_STORE_NAME
                    + System.getProperty("user.name")
                    + ".dat";

            oldStoreName = storeName + ".old";

            loadStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }

    private void loadStore() {
        File storeFile;
        FileInputStream fileStream = null;

        try {
            storeFile = new File(storeName);
            fileStream = new FileInputStream(storeFile);

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectInputStream inStream = new ObjectInputStream(fileStream)) {
                store = (CheckOutCommentStore) inStream.readObject();
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new CheckOutCommentStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Serialization failed.  Create a default store.
            store = new CheckOutCommentStore();

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
     * Write the checkout comments to disk.
     */
    public void writeStore() {

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

        try (FileOutputStream fileStream = new FileOutputStream(newStoreFile)) {

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectOutputStream outStream = new ObjectOutputStream(fileStream)) {
                outStream.writeObject(store);
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Get the key for the given merged info.
     * @param mergedInfo the merged info for which we want the key.
     * @return the key into the checkout comment store.
     */
    private String getKey(MergedInfoInterface mergedInfo) {
        StringBuilder keyBuffer = new StringBuilder(mergedInfo.getProjectName());
        keyBuffer.append("~");
        keyBuffer.append(mergedInfo.getArchiveDirManager().getAppendedPath());
        keyBuffer.append("~");
        keyBuffer.append(mergedInfo.getShortWorkfileName());
        return keyBuffer.toString().toUpperCase();
    }

    /**
     * Store a comment.
     * @param mergedInfo the file for which we store the comment.
     * @param comment the checkout comment to store.
     */
    public void storeComment(MergedInfoInterface mergedInfo, String comment) {
        store.storeComment(getKey(mergedInfo), comment);
    }

    /**
     * Lookup the checkout comment for the given file.
     * @param mergedInfo the file for which we want to lookup the checkout comment.
     * @return the checkout comment.
     */
    public String lookupComment(MergedInfoInterface mergedInfo) {
        return store.lookupComment(getKey(mergedInfo));
    }

    /**
     * See if a comment exists for the given file.
     * @param mergedInfo the file we're interested in.
     * @return true if there is a checkout comment; false if not.
     */
    public boolean commentExists(MergedInfoInterface mergedInfo) {
        return store.commentExists(getKey(mergedInfo));
    }

    /**
     * Remove a comment from the store.
     * @param mergedInfo the file whose comment we should remove.
     */
    public void removeComment(MergedInfoInterface mergedInfo) {
        store.removeComment(getKey(mergedInfo));
    }
}
