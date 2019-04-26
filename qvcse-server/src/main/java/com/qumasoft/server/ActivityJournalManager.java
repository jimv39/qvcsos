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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activity journal manager. This singleton writes to the activity journal.
 * @author Jim Voris
 */
public final class ActivityJournalManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityJournalManager.class);

    private static final ActivityJournalManager ACTIVITY_JOURNAL_MANAGER = new ActivityJournalManager();
    private boolean isInitializedFlag;
    private String journalFileName;
    private File journalFile;
    private FileOutputStream fileOutputStream;
    private PrintWriter printWriter;

    /**
     * Creates a new instance of ActivityJournalManager.
     */
    private ActivityJournalManager() {
    }

    /**
     * Get the activity journal manager singleton.
     * @return the activity journal manager singleton.
     */
    public static ActivityJournalManager getInstance() {
        return ACTIVITY_JOURNAL_MANAGER;
    }

    /**
     * Initialize the activity journal manager.
     * @return true if things initialized successfully; false otherwise.
     */
    public boolean initialize() {
        if (!isInitializedFlag) {
            journalFileName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_ACTIVITY_JOURNAL_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_ACTIVITY_JOURNAL_NAME;

            isInitializedFlag = openJournal();
        }
        return isInitializedFlag;
    }

    private synchronized boolean openJournal() {
        boolean initialized = false;
        try {
            LOGGER.info("Opening journal file: [{}]", journalFileName);

            journalFile = new File(journalFileName);

            // Make sure the needed directories exists
            if (!journalFile.getParentFile().exists()) {
                journalFile.getParentFile().mkdirs();
            }

            fileOutputStream = new FileOutputStream(journalFile, true);
            printWriter = new PrintWriter(fileOutputStream, true);
            initialized = true;
        } catch (FileNotFoundException e) {
            LOGGER.warn("Caught exception: [{}]: [{}]", e.getClass().toString(), e.getLocalizedMessage());
            printWriter = null;
        }
        return initialized;
    }

    /**
     * Close the journal file.
     */
    public synchronized void closeJournal() {
        if (printWriter != null) {
            addJournalEntry("server is exiting -- closing journal file.");
            printWriter.close();
            printWriter = null;
        }
    }

    /**
     * Add an entry to the journal file.
     * @param journalEntry the string that will get written to the journal file.
     */
    public synchronized void addJournalEntry(final String journalEntry) {
        if (printWriter != null) {
            Date now = new Date();
            printWriter.println(now.toString() + " " + journalEntry);
        }
    }
}
