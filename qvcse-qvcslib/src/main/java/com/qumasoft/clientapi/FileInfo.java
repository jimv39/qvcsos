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
package com.qumasoft.clientapi;

import java.util.Date;

/**
 * Interface that defines the content of the data elements returned from the
 * {@link ClientAPI#getFileInfoList(ClientAPIContext)} method.
 *
 * @author Jim Voris
 */
public interface FileInfo {

    /**
     * Get the attributes for the file. This is returned as a String of YES/NO
     * values that indicate the value of the QVCS attributes for this file. The
     * attributes appear in the following order: <ol>
     * <li>Check locks</li> <li>Delete workfile</li> <li>Expand keywords</li>
     * <li>Protect archive file. This attribute does not have much value/meaning
     * for the QVCS-Enterprise product, since all archive files are stored on
     * the server machine. It is a 'legacy' attribute that was included in the
     * product so that archive files are compatible with QVCS/QVCS-Pro archive
     * files.</li>
     * <li>Protect workfile</li> <li>Log actions to separate journal file.</li>
     * <li>Compress revisions</li> <li>Binary file</li> <li>Auto-merge flag (not
     * implemented)</li> <li>Do not compute a delta flag</li> <li>Store only the
     * last revision flag (not implemented)</li> </ol> For example, the value
     * returned for this might look like:
     * <b>YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO</b> which is the default
     * attribute settings for a .java file. The meaning of this string would be:
     * <ol> <li><b>YES</b> - lock checking is enabled for this file</li>
     * <li><b>NO</b> - do not delete the workfile after a checkin
     * operation.</li> <li><b>NO</b> - do not expand keywords.</li>
     * <li><b>NO</b> - do not protect the archive file.</li> <li><b>YES</b> -
     * write protect the workfile after checkin, or after a get operation.</li>
     * <li><b>YES</b> - add an entry to the server's journal file for any
     * version control operation performed on this file.</li> <li><b>YES</b> -
     * compress the revisions of the archive file.</li> <li><b>NO</b> - this is
     * not a binary file.</li> <li><b>NO</b> - do not perform an auto-merge on
     * this file. This is a feature that has <i>not yet been implemented</i>
     * (and may never be implemented, since merge is a tricky area).</li>
     * <li><b>NO</b> - compute a delta for this file. (This is a double negative
     * type flag, so a NO value means compute a delta, and a YES value means do
     * not compute a delta)</li> <li><b>NO</b> - store more than just the last
     * revision. The ability to store just a single revision has <i>not yet been
     * implemented</i>.</li> </ol>
     *
     * @return a String of YES/NO values that describe the QVCS-Enterprise
     * attributes for this file.
     */
    String getAttributes();

    /**
     * Get the Date of the most recent checkin of the tip revision for this
     * file.
     *
     * @return the Date when the tip revision was created.
     */
    Date getLastCheckInDate();

    /**
     * Get the name of the QVCS-Enterprise user who last checked in a revision
     * to this file.
     *
     * @return the name of the QVCS-Enterprise user who last checked in a
     * revision to this file.
     */
    String getLastEditBy();

    /**
     * Get the number of revisions that are locked. This will typically be 0 or
     * 1.
     *
     * @return the number of revisions that are locked.
     */
    int getLockCount();

    /**
     * Get a String that includes the name of every user that has a revision of
     * this file locked. If there are no locks on this file, then this will be
     * an empty String.
     *
     * @return a String that includes the name of every user that has a revision
     * locked.
     */
    String getLockedByString();

    /**
     * Get the number of revisions in this file.
     *
     * @return the number of revision in this file.
     */
    int getRevisionCount();

    /**
     * Get the short workfile name of this file. This will be the file name as
     * it would appear on a client workstation, without any directory elements
     * to the filename.
     *
     * @return the short workfile name of this file.
     */
    String getShortWorkfileName();

    /**
     * Get the appended path of this file. This identifies the directory that
     * contains this file.
     *
     * @return the appended path of this file; i.e. the project relative path of
     * the the directory that contains this file.
     */
    String getAppendedPath();
}
