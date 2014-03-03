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

/**
 * File ID store.
 *
 * @author Jim Voris
 */
public class FileIDStore implements Serializable {
    private static final long serialVersionUID = -8070508490401378370L;
    private int instanceMaximumFileID = 0;

    /**
     * Creates a new instance of FileIDStore.
     */
    public FileIDStore() {
    }

    /**
     * Return a fresh file ID.
     */
    synchronized int getNewFileID() {
        return ++instanceMaximumFileID;
    }

    /**
     * Return the current maximum file ID.
     */
    synchronized int getCurrentMaximumFileID() {
        return instanceMaximumFileID;
    }
}
