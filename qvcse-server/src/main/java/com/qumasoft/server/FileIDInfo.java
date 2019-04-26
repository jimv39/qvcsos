/*   Copyright 2004-2014 Jim Voris
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

/**
 * File Id info. Instances of this class are immutable.
 * @author Jim Voris
 */
public class FileIDInfo implements java.io.Serializable {
    private static final long serialVersionUID = -948976799497353711L;

    private final int directoryID;
    private final String appendedPath;
    private final String shortFilename;

    /**
     * Creates a new instance of FileIDInfo.
     *
     * @param dirID the directory id.
     * @param path the file's appended path.
     * @param shortName the file's short workfile name.
     */
    public FileIDInfo(int dirID, String path, String shortName) {
        directoryID = dirID;
        appendedPath = path;
        shortFilename = shortName;
    }

    /**
     * Get the directory id.
     *
     * @return the directory id.
     */
    public int getDirectoryID() {
        return directoryID;
    }

    /**
     * Get the appended path.
     *
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * Get the short workfile name.
     *
     * @return the short workfile name.
     */
    public String getShortFilename() {
        return shortFilename;
    }

    /**
     * Convenience for displaying this in the debugger.
     *
     * @return a nice string representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Directory ID: [").append(directoryID).append("]\n");
        buffer.append("Appended Path: [").append(shortFilename).append("]\n");
        buffer.append("Short workfile name: [").append(shortFilename).append("]\n");
        return buffer.toString();
    }
}
