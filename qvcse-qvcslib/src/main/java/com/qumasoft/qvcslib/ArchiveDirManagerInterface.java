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

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.swing.event.ChangeListener;

/**
 * Archive director manager interface.
 * @author Jim Voris
 */
public interface ArchiveDirManagerInterface {

    /**
     * Associate the given directory manager with this archive directory manager.
     * @param directoryManager the directory manager to associate with this archive directory manager. They should both be associated with the same project/view/appendedPath.
     */
    void setDirectoryManager(DirectoryManagerInterface directoryManager);

    /**
     * Get the appended path for this archive directory manager.
     * @return the appended path for this archive directory manager.
     */
    String getAppendedPath();

    /**
     * Get the project name for this archive directory manager.
     * @return the project name for this archive directory manager.
     */
    String getProjectName();

    /**
     * Get the view name for this archive directory manager.
     * @return the view name for this archive directory manager.
     */
    String getViewName();

    /**
     * Get the user name.
     * @return the user name.
     */
    String getUserName();

    /**
     * Get the project properties.
     * @return the project properties.
     */
    AbstractProjectProperties getProjectProperties();

    /**
     * Lookup the archive info for a given short workfile name.
     * @param shortWorkfileName the short workfile name.
     * @return the archive info for a given short workfile name.
     */
    ArchiveInfoInterface getArchiveInfo(String shortWorkfileName);

    /**
     * Create a QVCS archive.
     * @param commandLineArgs the command arguments.
     * @param fullWorkfilename the full workfile name.
     * @param response used on the server to identify the client connection.
     * @return true if things worked; false otherwise.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS problems.
     */
    boolean createArchive(LogFileOperationCreateArchiveCommandArgs commandLineArgs, String fullWorkfilename, ServerResponseFactoryInterface response)
            throws IOException, QVCSException;

    /**
     * Create a reference copy. This really is only meaningful on the server implementations.
     * @param projectProperties the project properties.
     * @param logfile the archive information (needed for keyword expansion).
     * @param buffer the bytes of the file revision.
     */
    void createReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile, byte[] buffer);

    /**
     * Delete a reference copy.
     * @param projectProperties the project properties.
     * @param logfile the archive information.
     */
    void deleteReferenceCopy(AbstractProjectProperties projectProperties, ArchiveInfoInterface logfile);

    /**
     * Move an archive file from one directory within a project to a different directory within that same project.
     * @param userName the user name of the QVCS user making the request.
     * @param shortWorkfileName the short workfile name.
     * @param targetArchiveDirManager the destination directory.
     * @param response used on the server to identify the client connection.
     * @return true if things worked; false otherwise.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS problems.
     */
    boolean moveArchive(String userName, String shortWorkfileName, final ArchiveDirManagerInterface targetArchiveDirManager, ServerResponseFactoryInterface response)
            throws IOException, QVCSException;

    /**
     * Rename an archive. This renames a file without moving it from its current directory.
     * @param userName the user name of the QVCS user making the request.
     * @param oldShortWorkfileName the original short workfile name.
     * @param newShortWorkfileName the new short workfile name.
     * @param response used on the server to identify the client connection.
     * @return true if things worked; false otherwise.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS problems.
     */
    boolean renameArchive(String userName, String oldShortWorkfileName, String newShortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException;

    /**
     * Move an archive to the cemetery.
     * @param userName the user name of the QVCS user making the request.
     * @param shortWorkfileName the short workfile name.
     * @param response used on the server to identify the client connection.
     * @return true if things worked; false otherwise.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS problems.
     */
    boolean deleteArchive(String userName, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException;

    /**
     * Restore an archive from the cemetery.
     * @param userName the user name of the user making the request.
     * @param shortWorkfileName the short workfile name.
     * @param response used on the server to identify the client connection.
     * @return true if things worked; false otherwise.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS problems.
     */
    boolean unDeleteArchive(String userName, String shortWorkfileName, ServerResponseFactoryInterface response) throws IOException, QVCSException;

    /**
     * Create the archive directory.
     * @return true if things work; false otherwise.
     */
    boolean createDirectory();

    /**
     * Add a change listener.
     * @param listener the listener to add.
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Remove a change listener.
     * @param listener the listener to remove.
     */
    void removeChangeListener(ChangeListener listener);

    /**
     * Start the directory manager. Used for 2 stage initialization so we can get things in place before actually doing anything.
     */
    void startDirectoryManager();

    /**
     * Notify listeners that things have changed.
     */
    void notifyListeners();

    /**
     * Set the fast notify flag.
     * @param flag new flag value.
     */
    void setFastNotify(boolean flag);

    /**
     * Get the state of the fast notify flag.
     * @return the state of the fast notify flag.
     */
    boolean getFastNotify();

    /**
     * Get a Map of the archive info objects for this directory.
     * @return a Map of the archive info objects for this directory.
     */
    Map<String, ArchiveInfoInterface> getArchiveInfoCollection();

    /**
     * Get the oldest revision. This was used to enforce the evaluation period.
     * @return the oldest revision, in seconds past the epoch.
     * @deprecated we don't need this any more since everything is free.
     */
    long getOldestRevision();

    /**
     * Get the most recent activity date, i.e. the most recent checkin within this directory.
     * @return the most recent activity date, i.e. the most recent checkin within this directory.
     */
    Date getMostRecentActivityDate();

    /**
     * Get the directory id.
     * @return the directory id.
     */
    int getDirectoryID();

    /**
     * Add a logfile listener.
     * @param response the listener.
     */
    void addLogFileListener(ServerResponseFactoryInterface response);

    /**
     * Remove a logfile listener.
     * @param response the listener.
     */
    void removeLogFileListener(ServerResponseFactoryInterface response);
}
