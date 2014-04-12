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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an entity object meant to capture the set of files that are in a directory at a given point in time.
 *
 * @author Jim Voris
 */
public class DirectoryContents {

    /**
     * Create our logger object
     */
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private static final String PROJECT_NAME = "Project Name: ";
    private static final String DIRECTORY_ID = "Directory ID: ";
    private static final String FILE_COUNT = "File Count: ";
    private static final String DIRECTORY_COUNT = "Directory Count: ";
    /**
     * The appended path is stored on the object for convenience. <b>IT IS NOT</b> stored in the logfile; its purpose is to make it
     * easier to know the appended path of this directory contents object.
     */
    private final String appendedPath;
    /**
     * The label parent flag is on the object for convenience. <b>IT IS NOT</b> stored in the logfile. Its purpose is to capture
     * whether or not a label needs to propagate up the directory tree. By default (true), it does. For opaque and translucent
     * branches, it does not.
     */
    private boolean labelParentFlag = true;
    /**
     * The parent directory id is on the object for convenience. <b>IT IS NOT</b> stored in the logfile. Its purpose is to capture
     * this directory content's parent directory id. That value is discovered at construction time.
     */
    private int parentDirectoryID = -1;
    private final int directoryID;
    private String projectName = null;
    private Map<Integer, String> fileMapByFileID = null;
    private Map<String, Integer> fileMapByFileName = null;
    private Map<Integer, String> directoryMap = null;

    /**
     * Creates a new instance of DirectoryContents.
     * @param project the project name.
     * @param dirID the directory id.
     * @param path the appended path.
     */
    DirectoryContents(final String project, final int dirID, final String path) {
        this.projectName = project;
        this.directoryID = dirID;
        this.appendedPath = path;
        fileMapByFileID = Collections.synchronizedMap(new TreeMap<Integer, String>());
        fileMapByFileName = Collections.synchronizedMap(new TreeMap<String, Integer>());
        directoryMap = Collections.synchronizedMap(new TreeMap<Integer, String>());
    }

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    String getProjectName() {
        return this.projectName;
    }

    /**
     * Get the appended path.
     *
     * @return the appended path.
     */
    public String getAppendedPath() {
        if (this.appendedPath == null) {
            LOGGER.log(Level.INFO, "null appended path!!");
        }
        return this.appendedPath;
    }

    int getDirectoryID() {
        return this.directoryID;
    }

    boolean getLabelParentFlag() {
        return this.labelParentFlag;
    }

    void setLabelParentFlag(boolean flag) {
        this.labelParentFlag = flag;
    }

    int getParentDirectoryID() {
        return this.parentDirectoryID;
    }

    void setParentDirectoryID(int parentDirID) {
        this.parentDirectoryID = parentDirID;
    }

    boolean containsFile(final String shortWorkfileName) {
        return fileMapByFileName.containsKey(shortWorkfileName);
    }

    boolean containsFileID(final int fileID) {
        return this.fileMapByFileID.containsKey(fileID);
    }

    void addFileID(final int fileID, final String shortWorkfileName) {
        this.fileMapByFileID.put(fileID, shortWorkfileName);
        this.fileMapByFileName.put(shortWorkfileName, fileID);
    }

    void removeFileID(final int fileID) {
        String shortWorkfileName = this.fileMapByFileID.get(Integer.valueOf(fileID));
        this.fileMapByFileID.remove(fileID);
        if (shortWorkfileName != null) {
            this.fileMapByFileName.remove(shortWorkfileName);
        }
    }

    void updateFileID(final int fileID, final String newShortWorkfileName) {
        String shortWorkfileName = this.fileMapByFileID.get(Integer.valueOf(fileID));
        this.fileMapByFileID.put(fileID, newShortWorkfileName);
        if (shortWorkfileName != null) {
            this.fileMapByFileName.remove(shortWorkfileName);
        }
        this.fileMapByFileName.put(newShortWorkfileName, fileID);
    }

    void addDirectoryID(final int dirID, final String directoryName) {
        this.directoryMap.put(dirID, directoryName);
    }

    void removeDirectoryID(final int dirID) {
        this.directoryMap.remove(dirID);
    }

    void updateDirectoryID(final int dirID, final String newDirectoryName) {
        this.directoryMap.put(dirID, newDirectoryName);
    }

    /**
     * Get the map of files.
     * @return the map of files.
     */
    public Map<Integer, String> getFiles() {
        return this.fileMapByFileID;
    }

    /**
     * Get the map of child directories.
     * @return the map of child directories.
     */
    public Map<Integer, String> getChildDirectories() {
        return this.directoryMap;
    }

    /**
     * A useful String representation of this object instance.
     *
     * @return A useful String representation of this object instance.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("# Directory Contents for ").append(getProjectName()).append("::").append(getAppendedPath()).append("\n");
        buffer.append("#  This file is generated by the QVCS-Enterprise server. Do not manually edit this file!\n");
        buffer.append(PROJECT_NAME).append(getProjectName()).append("\n");
        buffer.append(DIRECTORY_ID).append(Integer.toString(getDirectoryID())).append("\n");
        buffer.append(FILE_COUNT).append(this.fileMapByFileID.size()).append("\n");
        buffer.append(DIRECTORY_COUNT).append(this.directoryMap.size()).append("\n");

        // Put the file id's and file names into this contents file
        Iterator<Integer> it = this.fileMapByFileID.keySet().iterator();
        while (it.hasNext()) {
            Integer fileID = it.next();
            String shortWorkfileName = this.fileMapByFileID.get(fileID);
            buffer.append(fileID.toString()).append(":").append(shortWorkfileName).append("\n");
        }

        // Put the Directory id's into this contents file
        Iterator<Integer> directoryIterator = this.directoryMap.keySet().iterator();
        while (directoryIterator.hasNext()) {
            Integer dirID = directoryIterator.next();
            String directoryName = this.directoryMap.get(dirID);
            buffer.append(dirID.toString()).append(":").append(directoryName).append("\n");
        }

        // And report on the value of the stuff that is NOT serialized to the file:
        buffer.append("Appended Path: ").append(getAppendedPath()).append("\n");
        buffer.append("Label parent flag: ").append(getLabelParentFlag()).append("\n");
        buffer.append("Parent directory ID: ").append(getParentDirectoryID()).append("\n");
        return buffer.toString();
    }
}
