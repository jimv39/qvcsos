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
package com.qumasoft.guitools.qwin;

import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

/**
 * File group manager.
 * @author Jim Voris
 */
public final class FileGroupManager {

    private static final FileGroupManager FILE_GROUP_MANAGER = new FileGroupManager();
    private boolean isInitializedFlag;
    private String storeName;
    private String oldStoreName;
    private FileGroupStore fileGroupStore;

    /**
     * Creates a new instance of FileGroupManager.
     */
    private FileGroupManager() {
    }

    /**
     * Get the file group manager singleton.
     * @return the file group manager singleton.
     */
    public static FileGroupManager getInstance() {
        return FILE_GROUP_MANAGER;
    }

    boolean initialize() {
        if (!isInitializedFlag) {
            storeName = QWinFrame.getQWinFrame().getQvcsClientHomeDirectory()
                    + File.separator
                    + QVCSConstants.QVCS_USER_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_FILEGROUP_STORE_NAME
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

            // Use try with resources so we're guaranteed the object input stream is closed.
            try (ObjectInputStream inStream = new ObjectInputStream(fileStream)) {
                fileGroupStore = (FileGroupStore) inStream.readObject();
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            fileGroupStore = new FileGroupStore();
        } catch (IOException | ClassNotFoundException e) {
            warnProblem(Utility.expandStackTraceToString(e));

            // Serialization failed.  Create a default store.
            fileGroupStore = new FileGroupStore();

        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    warnProblem(Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    void writeStore() {
        FileOutputStream fileStream = null;

        try {
            File storeFile = new File(storeName);
            File oldStoreFile = new File(oldStoreName);

            if (oldStoreFile.exists()) {
                Files.delete(oldStoreFile.toPath());
            }

            if (storeFile.exists()) {
                if (!storeFile.renameTo(oldStoreFile)) {
                    throw new IOException("Failed to rename " + storeFile.getName());
                }
            }

            File newStoreFile = new File(storeName);

            // Make sure the needed directories exists
            if (!newStoreFile.getParentFile().exists()) {
                newStoreFile.getParentFile().mkdirs();
            }

            fileStream = new FileOutputStream(newStoreFile);

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectOutputStream outStream = new ObjectOutputStream(fileStream)) {
                outStream.writeObject(fileGroupStore);
            }
        } catch (IOException e) {
            warnProblem(Utility.expandStackTraceToString(e));
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    warnProblem(Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    /**
     * Get the file group enabled flag.
     * @return the file group enabled flag.
     */
    public boolean getEnabledFlag() {
        return fileGroupStore.getEnabledFlag();
    }

    /**
     * Set the file group enabled flag.
     * @param flag true to enable file groups; false to disable file groups.
     */
    public void setEnabledFlag(boolean flag) {
        fileGroupStore.setEnabledFlag(flag);
    }

    /**
     * Store the given group.
     * @param groupName the name of the file group.
     * @param fileExtensions the list of file extensions in the file group.
     */
    public void storeGroup(String groupName, String[] fileExtensions) {
        fileGroupStore.storeGroup(groupName, fileExtensions);
    }

    /**
     * Remove all file groups.
     */
    public void removeAllGroups() {
        fileGroupStore.removeAllGroups();
    }

    /**
     * Get the number of file groups.
     * @return the number of file groups.
     */
    public int getGroupCount() {
        return fileGroupStore.getGroupCount();
    }

    /**
     * Get the group name for the given index value.
     * @param index the index.
     * @return the group name for the given index value.
     */
    public String getGroupName(int index) {
        return fileGroupStore.getGroupName(index);
    }

    /**
     * Get the list of file extensions for the given index.
     * @param index the index.
     * @return the list of file extensions for the given index.
     */
    public String[] getGroupFileExtensions(int index) {
        return fileGroupStore.getGroupFileExtensions(index);
    }

    String getGroupNameForFile(String filename) {
        String extension = Utility.getFileExtension(filename);
        String groupName = fileGroupStore.lookupGroupForExtension(extension);
        return groupName;
    }
}
