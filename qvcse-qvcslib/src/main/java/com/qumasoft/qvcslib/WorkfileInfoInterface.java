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
import java.util.Date;

/**
 * Workfile info interface. Define those operations needed to describe a workfile.
 *
 * @author Jim Voris
 */
public interface WorkfileInfoInterface {

    /**
     * Get the short workfile name (the final segment of the full workfile name).
     *
     * @return the short workfile name.
     */
    String getShortWorkfileName();

    /**
     * Get the full workfile name.
     *
     * @return the full workfile name.
     */
    String getFullWorkfileName();

    /**
     * Get the size of the workfile.
     * @return the size of the workfile.
     */
    long getWorkfileSize();

    /**
     * Get the last change date for the workfile.
     * @return the last time the workfile changed.
     */
    Date getWorkfileLastChangedDate();

    /**
     * Get the File object associated with the workfile.
     * @return the File object associated with the workfile.
     */
    File getWorkfile();

    /**
     * Get the project name.
     * @return the project name.
     */
    String getProjectName();

    /**
     * Does the workfile exist.
     *
     * @return true if the workfile exists; false otherwise.
     */
    boolean getWorkfileExists();

    /**
     * Get the time (in seconds past the epoch) when the workfile was fetched. This represents the last 'get' or 'checkout'
     * operation for the given workfile.
     * @return a long representation of the last fetch date.
     */
    long getFetchedDate();

    /**
     * Set the fetched date.
     * @param time the time (in seconds past the epoch) when the workfile was last fetched.
     */
    void setFetchedDate(long time);

    /**
     * Get the revision string associated with the workfile.
     * @return the revision string associated with the workfile.
     */
    String getWorkfileRevisionString();

    /**
     * Set the revision string associated with the workfile.
     * @param revisionString the revision string to associate with this workfile.
     */
    void setWorkfileRevisionString(String revisionString);

    /**
     * Set the archive info to associate with this workfile.
     * @param archInfo the archive information to associate with this workfile.
     */
    void setArchiveInfo(ArchiveInfoInterface archInfo);

    /**
     * Get the archive info associated with this workfile.
     * @return the archive info associated with this workfile.
     */
    ArchiveInfoInterface getArchiveInfo();

    /**
     * Return true if keyword expansion is enabled for the archive associated with this workfile.
     *
     * @return true if keyword expansion is enabled; false otherwise.
     */
    boolean getKeywordExpansionAttribute();

    /**
     * Set the keyword expansion attribute to associate with this workfile info.
     * @param flag keyword expansion attribute to associate with this workfile info.
     */
    void setKeywordExpansionAttribute(boolean flag);

    /**
     * Return true if this is a binary file.
     *
     * @return true if this is a binary file; false otherwise.
     */
    boolean getBinaryFileAttribute();

    /**
     * Set the binary flag attribute for this workfile info.
     * @param flag the binary flag attribute for this workfile info.
     */
    void setBinaryFileAttribute(boolean flag);
}
