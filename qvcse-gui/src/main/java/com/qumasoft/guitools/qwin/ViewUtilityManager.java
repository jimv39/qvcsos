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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A class to manage the utilities used to view workfiles for QVCS-Enterprise client.
 *
 * @author Jim Voris
 */
public final class ViewUtilityManager {

    private static final ViewUtilityManager VIEW_UTILITY_MANAGER = new ViewUtilityManager();
    private boolean isInitializedFlag;
    private String storeName;
    private String oldStoreName;
    private ViewUtilityStore store;

    /**
     * Creates a new instance of ViewUtilityManager.
     */
    private ViewUtilityManager() {
    }

    /**
     * Get the view utility manager singleton.
     * @return the view utility manager singleton.
     */
    public static ViewUtilityManager getInstance() {
        return VIEW_UTILITY_MANAGER;
    }

    /**
     * Initialize the view utility manager.
     * @return true if initialization was successful; false if not.
     */
    public boolean initialize() {
        if (!isInitializedFlag) {
            storeName = QWinFrame.getQWinFrame().getQvcsClientHomeDirectory()
                    + File.separator
                    + QVCSConstants.QVCS_USER_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_VIEW_UTILITY_STORE_NAME
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

        storeFile = new File(storeName);

        // Use try with resources so we're guaranteed the file input stream is closed.
        try (FileInputStream fileStream = new FileInputStream(storeFile)) {

            // Use try with resources so we're guaranteed the object input stream is closed.
            try (ObjectInputStream inStream = new ObjectInputStream(fileStream)) {
                store = (ViewUtilityStore) inStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            // Something failed.  Create a default store.
            store = new ViewUtilityStore();
        } finally {
            store.dumpMap();
        }
    }

    void writeStore() {
        FileOutputStream fileStream = null;

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

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectOutputStream outStream = new ObjectOutputStream(fileStream)) {
                outStream.writeObject(store);
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
     * Lookup the utility to be used for viewing the given workfile.
     * @param fullWorkfileName the full workfile name.
     * @return the command line to use to view the given workfile.
     */
    public String[] getViewUtilityCommandLine(String fullWorkfileName) {
        return store.getViewUtilityCommandLine(fullWorkfileName);
    }

    /**
     * Return true if there is a utility associated with the given workfile.
     * @param fullWorkfileName the full workfile name.
     * @return true if there is a utility known for the given workfile; false otherwise.
     */
    public boolean getHasAssociatedUtility(final String fullWorkfileName) {
        boolean retVal = false;
        String utilityCommand = store.getAssociatedUtility(fullWorkfileName);
        if (utilityCommand != null) {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Remove the association between the file extension type, and a utility.
     * @param fullWorkfileName the full workfile name.
     */
    public void removeUtilityAssociation(final String fullWorkfileName) {
        store.removeUtilityAssociation(fullWorkfileName);
    }
}
