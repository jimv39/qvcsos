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

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Directory ID store.
 * @author Jim Voris
 */
public class DirectoryIDStore implements Serializable {
    private static final long serialVersionUID = -6263643751412006708L;

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private int instanceMaximumDirectoryID = 0;

    /**
     * Creates a new instance of DirectoryIDStore.
     */
    public DirectoryIDStore() {
    }

    synchronized void dump() {
        LOGGER.log(Level.INFO, ": Max directory ID: " + instanceMaximumDirectoryID);
    }

    /**
     * Return a fresh directory ID for the given project.
     * @return a fresh directory ID for the given project.
     */
    synchronized int getNewDirectoryID() {
        return ++instanceMaximumDirectoryID;
    }

    /**
     * Return the current maximum directory ID.
     * @return the current maximum directory ID.
     */
    synchronized int getCurrentMaximumDirectoryID() {
        return instanceMaximumDirectoryID;
    }

    /**
     * Set the maximum directory id.
     * @param maximumDirectoryId the value to use as the current maximum directory id.
     */
    public synchronized void setMaximumDirectoryID(int maximumDirectoryId) {
        instanceMaximumDirectoryID = maximumDirectoryId;
    }
}
