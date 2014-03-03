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
 * A class to manage filters for QVCS-Enterprise client.
 *
 * @author Jim Voris
 */
public final class FilterManager {

    private static final FilterManager FILTER_MANAGER = new FilterManager();
    private boolean isInitializedFlag = false;
    private String storeName = null;
    private String oldStoreName = null;
    private FilterStore store = null;
    /** All files filter name. */
    public static final String ALL_FILTER = "All files";

    private static final String JAVA_SOURCE_FILTER = "Java source files";
    private static final String CPP_AND_H_SOURCE_FILTER = "C++ and .h source files";
    private static final String DELPHI_SOURCE_FILTER = "Delphi source files";

    /**
     * Creates a new instance of FilterManager.
     */
    private FilterManager() {
    }

    /**
     * Get the filter manager singleton.
     * @return the filter manager singleton.
     */
    public static FilterManager getFilterManager() {
        return FILTER_MANAGER;
    }

    /**
     * Initialize the filter manager.
     * @return true if initialization was successful; false if not.
     */
    public boolean initialize() {
        if (!isInitializedFlag) {
            storeName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_USER_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_FILTER_STORE_NAME
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
            store = (FilterStore) inStream.readObject();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new FilterStore();

            // Create our default set of filter collections
            createDefaultFilterCollections();
        } catch (IOException | ClassNotFoundException e) {
            // Serialization failed.  Create a default store.
            store = new FilterStore();

            // Create our default set of filter collections
            createDefaultFilterCollections();
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
     * Write the filter collections to disk.
     */
    public void writeStore() {
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
     * Add a filter collection.
     * @param filterCollection the filter collection to add.
     */
    public void addFilterCollection(FilterCollection filterCollection) {
        store.addFilterCollection(filterCollection.getCollectionName(), filterCollection);
    }

    /**
     * Remove a filter collection.
     * @param filterCollection the filter collection to remove.
     */
    public void removeFilterCollection(FilterCollection filterCollection) {
        store.removeFilterCollection(filterCollection.getCollectionName());
    }

    /**
     * Reset the collections to the default set of filter collections.
     */
    public void resetCollections() {
        // Create a default store.
        store = new FilterStore();

        // Create our default set of filter collections
        createDefaultBuiltInCollections();
    }

    /**
     * Get the list of filter collections.
     * @return the list of filter collections.
     */
    public FilterCollection[] listFilterCollections() {
        return store.listFilterCollections();
    }

    private void createDefaultFilterCollections() {
        createDefaultBuiltInCollections();

        // Java source files only
        FilterCollection javaFileFilterCollection = new FilterCollection(JAVA_SOURCE_FILTER, false, ProjectNamesComboModel.GLOBAL_PROJECT_NAME);
        FileFilterExtensionFilter javaExtensionFilter = new FileFilterExtensionFilter("java", true);
        javaFileFilterCollection.addFilter(javaExtensionFilter);
        addFilterCollection(javaFileFilterCollection);

        // C++ source and .h files only
        FilterCollection cppFileFilterCollection = new FilterCollection(CPP_AND_H_SOURCE_FILTER, false, ProjectNamesComboModel.GLOBAL_PROJECT_NAME);
        FileFilterExtensionFilter cppExtensionFilter = new FileFilterExtensionFilter("cpp", false);
        cppFileFilterCollection.addFilter(cppExtensionFilter);
        FileFilterExtensionFilter hExtensionFilter = new FileFilterExtensionFilter("h", false);
        cppFileFilterCollection.addFilter(hExtensionFilter);
        addFilterCollection(cppFileFilterCollection);

        // Delphi source and files (.pas and .dfm)
        FilterCollection pasFileFilterCollection = new FilterCollection(DELPHI_SOURCE_FILTER, false, ProjectNamesComboModel.GLOBAL_PROJECT_NAME);
        FileFilterExtensionFilter pasExtensionFilter = new FileFilterExtensionFilter("pas", false);
        pasFileFilterCollection.addFilter(pasExtensionFilter);
        FileFilterExtensionFilter dfmExtensionFilter = new FileFilterExtensionFilter("dfm", false);
        cppFileFilterCollection.addFilter(dfmExtensionFilter);
        addFilterCollection(pasFileFilterCollection);
    }

    private void createDefaultBuiltInCollections() {
        // All files
        FilterCollection allFileFilterCollection = new FilterCollection(ALL_FILTER, true, ProjectNamesComboModel.GLOBAL_PROJECT_NAME);
        addFilterCollection(allFileFilterCollection);
    }
}
