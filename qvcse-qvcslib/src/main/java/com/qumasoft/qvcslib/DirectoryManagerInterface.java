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
import java.util.Collection;
import javax.swing.event.ChangeListener;

/**
 * Directory manager interface. These are the methods that must be implemented by a client-side Directory manager.
 * @author Jim Voris
 */
public interface DirectoryManagerInterface {

    /**
     * Get the archive directory manager associated with this directory.
     * @return the archive directory manager associated with this directory.
     */
    ArchiveDirManagerInterface getArchiveDirManager();

    /**
     * Get the workfile directory manager associated with this directory.
     * @return the workfile directory manager associated with this directory.
     */
    WorkfileDirectoryManagerInterface getWorkfileDirectoryManager();

    /**
     * Merge the respective file collections of the workfile directory manager and the archive directory manager so that they can be presented as a single collection of files.
     * @throws QVCSException for any QVCS problems.
     */
    void mergeManagers() throws QVCSException;

    /**
     * Get the project properties.
     * @return the project properties.
     */
    AbstractProjectProperties getProjectProperties();

    /**
     * Get the user name.
     * @return the user name.
     */
    String getUserName();

    /**
     * Get the project name for this directory.
     * @return the project name for this directory.
     */
    String getProjectName();

    /**
     * Get the view name for this directory.
     * @return the view name for this directory.
     */
    String getViewName();

    /**
     * Get the appended path for this directory.
     * @return the appended path for this directory.
     */
    String getAppendedPath();

    /**
     * Get the number of files in the collection for this directory (includes the merged set of version controlled files and un-controlled files).
     * @return the number of files in the collection for this directory.
     */
    int getCount();

    /**
     * Create an archive in this directory.
     * @param commandLineArgs the create archive command arguments.
     * @param fullWorkfilename the full workfile name.
     * @return true if the create was successful.
     * @throws IOException for file I/O problems.
     * @throws QVCSException for a QVCS specific problem.
     */
    boolean createArchive(LogFileOperationCreateArchiveCommandArgs commandLineArgs, String fullWorkfilename) throws IOException, QVCSException;

    /**
     * Get the Collection of merged info instances for this directory.
     * @return the Collection of merged info instances for this directory.
     */
    Collection<MergedInfoInterface> getMergedInfoCollection();

    /**
     * Get the 'has changed' flag for this directory.
     * @return the 'has changed' flag for this directory.
     */
    boolean getHasChanged();

    /**
     * Set the 'has changed' flag for this directory.
     * @param flag the 'has changed' flag for this directory.
     */
    void setHasChanged(boolean flag);

    /**
     * Lookup the merged info for the given short workfile name.
     * @param shortWorkfileName the short workfile name.
     * @return the merged info for the given short workfile name; null if the not found.
     */
    MergedInfoInterface getMergedInfo(String shortWorkfileName);

    /**
     * Add a change listener.
     * @param listener a change listener.
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Remove a change listener.
     * @param listener a change listener.
     */
    void removeChangeListener(ChangeListener listener);
}
