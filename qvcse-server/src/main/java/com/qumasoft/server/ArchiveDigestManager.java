/*   Copyright 2004-2015 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TimerManager;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to manage the collection of digests associated with a given file revision. It is a singleton.
 *
 * @author Jim Voris
 */
public final class ArchiveDigestManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDigestManager.class);

    /**
     * Wait 2 seconds before saving the latest archive digest store.
     */
    private static final long SAVE_ARCHIVE_DIGEST_DELAY = 1000L * 2L;
    private static final ArchiveDigestManager ARCHIVE_DIGEST_MANAGER = new ArchiveDigestManager();
    private boolean isInitializedFlag;
    private String storeName;
    private String oldStoreName;
    private ArchiveDigestDictionaryStore store;
    private MessageDigest messageDigest;
    private final Object messageDigestSyncObject = new Object();
    private SaveArchiveDigestStoreTimerTask saveArchiveDigestStoreTimerTask;

    /**
     * Creates a new instance of WorkfileDigestDictionary.
     */
    private ArchiveDigestManager() {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Failed to create MD5 digest instance! [{}]: [{}]", e.getClass().toString(), e.getLocalizedMessage());
        }
    }

    /**
     * Get the server archive digest manager singleton.
     * @return the server archive digest manager singleton.
     */
    public static ArchiveDigestManager getInstance() {
        return ARCHIVE_DIGEST_MANAGER;
    }

    /**
     * Initialize the server archive digest manager singleton.
     * @param type the type of project.
     * @return true if things initialized successfully; false otherwise.
     */
    public boolean initialize(String type) {
        if (!isInitializedFlag) {
            storeName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_META_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_ARCHIVE_DIGEST_STORE_NAME
                    + type
                    + ".dat";

            oldStoreName = storeName + ".old";

            loadStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }

    byte[] getArchiveDigest(LogFile logfile, String revisionString) {
        byte[] retVal = store.lookupArchiveDigest(logfile, revisionString);
        if (retVal == null) {
            // There's no digest yet.
            retVal = computeDigest(logfile, revisionString);
            store.addDigest(logfile, revisionString, retVal);
        }
        return retVal;
    }

    synchronized byte[] addRevision(LogFileImpl logfile, String revisionString, byte[] buffer) {
        scheduleSaveOfArchiveDigestStore();
        byte[] digest;
        synchronized (messageDigestSyncObject) {
            messageDigest.reset();
            digest = messageDigest.digest(buffer);
            store.addDigest(logfile, revisionString, digest);
        }
        return digest;
    }

    /**
     * Add and return the digest for the given revision string.
     * @param logfile the logfile from which we'll compute the digest.
     * @param revisionString the revision string for which we need to compute and add the digest.
     * @return the computed digest.
     */
    public synchronized byte[] addRevision(LogFile logfile, String revisionString) {
        scheduleSaveOfArchiveDigestStore();
        byte[] digest = computeDigest(logfile, revisionString);
        store.addDigest(logfile, revisionString, digest);
        return digest;
    }

    private byte[] computeDigest(LogFile logfile, String revisionString) {
        byte[] digest = null;
        FileInputStream inStream = null;
        File tempFile = null;

        try {
            synchronized (messageDigestSyncObject) {
                messageDigest.reset();
                tempFile = File.createTempFile("QVCS", "tmp");

                GetRevisionCommandArgs commandArgs = new GetRevisionCommandArgs();
                commandArgs.setOutputFileName(tempFile.getAbsolutePath());
                commandArgs.setRevisionString(revisionString);
                commandArgs.setShortWorkfileName(tempFile.getAbsolutePath());

                if (logfile.getRevision(commandArgs, tempFile.getAbsolutePath())) {
                    inStream = new FileInputStream(tempFile);
                    byte[] buffer = new byte[(int) tempFile.length()];
                    Utility.readDataFromStream(buffer, inStream);
                    LOGGER.trace("computing digest on buffer of size: [{}] for file: [{}]", buffer.length, logfile.getShortWorkfileName());
                    digest = messageDigest.digest(buffer);
                }
            }
        } catch (QVCSException | IOException e) {
            LOGGER.warn(Utility.expandStackTraceToString(e));
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("caught exception: [{}]", e.getClass().toString(), e);
                }
                if (tempFile != null) {
                    tempFile.delete();
                }
            }
        }
        return digest;
    }

    private void loadStore() {
        File storeFile;
        FileInputStream fileStream = null;

        try {
            storeFile = new File(storeName);
            fileStream = new FileInputStream(storeFile);
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            store = (ArchiveDigestDictionaryStore) inStream.readObject();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new ArchiveDigestDictionaryStore();
            writeStore();
        } catch (ClassNotFoundException | IOException e) {
            // Serialization failed.  Create a default store.
            store = new ArchiveDigestDictionaryStore();
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

    synchronized void writeStore() {
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
            LOGGER.warn(e.getLocalizedMessage(), e);
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
     * Schedule the save of the archive digest store. We want to save the archive digest store after things are quiet for the
     * SAVE_ARCHIVE_DIGEST_DELAY amount of time so that the archive digest values will have been preserved in the case of a crash.
     */
    private void scheduleSaveOfArchiveDigestStore() {
        if (saveArchiveDigestStoreTimerTask != null) {
            saveArchiveDigestStoreTimerTask.cancel();
            saveArchiveDigestStoreTimerTask = null;
        }
        saveArchiveDigestStoreTimerTask = new SaveArchiveDigestStoreTimerTask();
        TimerManager.getInstance().getTimer().schedule(saveArchiveDigestStoreTimerTask, SAVE_ARCHIVE_DIGEST_DELAY);
    }

    /**
     * Use a timer to write the digest store after a while so it will have been saved before a crash.
     */
    class SaveArchiveDigestStoreTimerTask extends TimerTask {

        @Override
        public void run() {
            LOGGER.info("Performing scheduled save of archive digest store.");
            writeStore();
        }
    }
}
