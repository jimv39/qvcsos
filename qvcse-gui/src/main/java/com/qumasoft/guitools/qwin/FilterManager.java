/*   Copyright 2004-2022 Jim Voris
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
import com.qumasoft.guitools.qwin.filefilter.FileFilterInterface;
import com.qumasoft.guitools.qwin.filefilter.FilterFactory;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
    /** Filter by commit id. */
    public static final String BY_COMMIT_ID_FILTER = "By Commit id";

    private static final String JAVA_SOURCE_FILTER = "Java source files";
    private static final String CPP_AND_H_SOURCE_FILTER = "C++ and .h source files";
    private static final String JAVASCRIPT_FILTER = "Javascript files";

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
            storeName = QWinFrame.getQWinFrame().getQvcsClientHomeDirectory()
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

            // Use try with resources so we're guaranteed the object input stream is closed.
            try (ObjectInputStream inStream = new ObjectInputStream(fileStream)) {
                store = (FilterStore) inStream.readObject();
            }
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

            // Guarantee to include the All and commit id filter collections.
            createDefaultBuiltInCollections();

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

        // Java source files only
        FilterCollection javaFileFilterCollection = new FilterCollection(JAVA_SOURCE_FILTER, false, QWinFrame.GLOBAL_PROJECT_NAME);
        FileFilterInterface javaExtensionFilter = FilterFactory.buildFilter(QVCSConstants.EXTENSION_FILTER, "java", true);
        javaFileFilterCollection.addFilter(javaExtensionFilter);
        addFilterCollection(javaFileFilterCollection);

        // C++ source and .h files only
        FilterCollection cppFileFilterCollection = new FilterCollection(CPP_AND_H_SOURCE_FILTER, false, QWinFrame.GLOBAL_PROJECT_NAME);
        FileFilterInterface cppExtensionFilter = FilterFactory.buildFilter(QVCSConstants.EXTENSION_FILTER, "cpp", false);
        cppFileFilterCollection.addFilter(cppExtensionFilter);
        FileFilterInterface hExtensionFilter = FilterFactory.buildFilter(QVCSConstants.EXTENSION_FILTER, "h", false);
        cppFileFilterCollection.addFilter(hExtensionFilter);
        addFilterCollection(cppFileFilterCollection);

        // Javascript files.
        FilterCollection jsFileFilterCollection = new FilterCollection(JAVASCRIPT_FILTER, false, QWinFrame.GLOBAL_PROJECT_NAME);
        FileFilterInterface jsExtensionFilter = FilterFactory.buildFilter(QVCSConstants.EXTENSION_FILTER, "js", true);
        jsFileFilterCollection.addFilter(jsExtensionFilter);
        addFilterCollection(jsFileFilterCollection);
    }

    private void createDefaultBuiltInCollections() {
        // All files
        FilterCollection allFileFilterCollection = new FilterCollection(ALL_FILTER, true, QWinFrame.GLOBAL_PROJECT_NAME);
        addFilterCollection(allFileFilterCollection);

        // By commit id.
        FilterCollection byCommitIdFileFilterCollection = new FilterCollection(BY_COMMIT_ID_FILTER, true, QWinFrame.GLOBAL_PROJECT_NAME);
        FileFilterInterface byCommitIdFilter = FilterFactory.buildFilter(QVCSConstants.BY_COMMIT_ID_FILTER, null, true);
        byCommitIdFileFilterCollection.addFilter(byCommitIdFilter);
        addFilterCollection(byCommitIdFileFilterCollection);
    }
}
