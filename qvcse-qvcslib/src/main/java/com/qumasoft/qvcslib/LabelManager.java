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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Label Manager singleton. Used on the client to hold on to those labels known to the given user. Each user gets a separate label store used to hold on to this information.
 * This is used to make it easier to populate GUI elements for applying labels, and/or fetching/filtering by label, etc. Note that this class is a sort of cosmetic helper
 * class, and does <b>NOT</b> apply labels to files... Think of it as a sort of label dictionary that helps keep track of the label strings associated with a given project.
 * <p>
 * <i>TODO -- this should probably migrate over to some package that is more clearly for client-only use.</i></p>
 * @author Jim Voris
 */
public final class LabelManager {

    private static final LabelManager LABEL_MANAGER = new LabelManager();
    private static String userName = null;
    private String storeName = null;
    private String oldStoreName = null;
    private LabelStore labelStore = null;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");

    /**
     * Creates a new instance of LabelManager.
     */
    private LabelManager() {
    }

    /**
     * Get the singleton instance of the Label Manager.
     * @return the singleton instance of the Label Manager.
     */
    public static LabelManager getInstance() {
        return LABEL_MANAGER;
    }

    /**
     * Set the user name. Used to identify which store we'll use for holding on to the label information.
     * @param user the QVCS user name of the client user.
     */
    public static void setUserName(String user) {
        userName = user;
    }

    private void addLabel(String projectName, LabelInfo labelInfo) {
        labelStore.addLabel(projectName, labelInfo);
    }

    /**
     * Add/capture the labels present in the given logfile info.
     * @param projectName the project name.
     * @param logfileInfo the logfile info that we scan for labels.
     */
    public void addLabels(String projectName, LogfileInfo logfileInfo) {
        int labelCount = logfileInfo.getLogFileHeaderInfo().getLogFileHeader().versionCount();
        if (labelCount > 0) {
            LabelInfo[] labelInfo = logfileInfo.getLogFileHeaderInfo().getLabelInfo();
            for (int i = 0; i < labelCount; i++) {
                addLabel(projectName, labelInfo[i]);
            }
        }
    }

    /**
     * Add/capture a single label.
     * @param projectName the project name.
     * @param labelString the label string.
     * @param isFloatingLabelFlag is this a floating label flag.
     */
    public void addLabel(String projectName, String labelString, boolean isFloatingLabelFlag) {
        labelStore.addLabel(projectName, labelString, isFloatingLabelFlag);
    }

    /**
     * Discard a label.
     * @param projectName the project name.
     * @param labelString the label to discard.
     */
    public void removeLabel(String projectName, String labelString) {
        labelStore.removeLabel(projectName, labelString);
    }

    /**
     * Get an iterator that can be used to enumerate the labels known for the given project.
     * @param projectName the project name.
     * @return an iterator over the collection of labels associated with the given project.
     */
    public Iterator<BriefLabelInfo> getLabels(String projectName) {
        return labelStore.getLabels(projectName);
    }

    /**
     * Initialize the label manager.
     */
    public void initialize() {
        storeName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_META_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_LABEL_STORE_NAME
                + userName
                + ".dat";

        oldStoreName = storeName + ".old";

        loadStore();
    }

    private void loadStore() {
        File storeFile;
        FileInputStream fileStream = null;

        try {
            storeFile = new File(storeName);
            fileStream = new FileInputStream(storeFile);
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            labelStore = (LabelStore) inStream.readObject();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            labelStore = new LabelStore();
        } catch (IOException | ClassNotFoundException e) {
            // Serialization failed.  Create a default store.
            labelStore = new LabelStore();
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
     * Write the label store to disk.
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
            outStream.writeObject(labelStore);
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
}
