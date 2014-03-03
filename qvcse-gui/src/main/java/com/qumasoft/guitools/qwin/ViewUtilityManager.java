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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

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
            storeName = System.getProperty("user.dir")
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
        FileInputStream fileStream = null;

        try {
            storeFile = new File(storeName);
            fileStream = new FileInputStream(storeFile);
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            store = (ViewUtilityStore) inStream.readObject();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new ViewUtilityStore();
        } catch (IOException | ClassNotFoundException e) {
            // Serialization failed.  Create a default store.
            store = new ViewUtilityStore();
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
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
            ObjectOutputStream outStream = new ObjectOutputStream(fileStream);
            outStream.writeObject(store);
        } catch (IOException e) {
            QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
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
