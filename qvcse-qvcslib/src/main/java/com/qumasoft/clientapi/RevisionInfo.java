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
package com.qumasoft.clientapi;

import java.util.Date;

/**
 * This interface defines the data returned from the
 * {@link ClientAPI#getRevisionInfoList(com.qumasoft.clientapi.ClientAPIContext) getRevisionInfoList}
 * method defined by the ClientAPI interface.
 *
 * @author Jim Voris
 */
public interface RevisionInfo {

    /**
     * Get the check in Date for this revision. This is the time on the server
     * when the checkin of this revision occurred.
     *
     * @return the server time when the checkin occurred.
     */
    Date getCheckInDate();

    /**
     * Get the edit date for this revision. This is the timestamp from the
     * client workstation's copy of the file that was checked in to create this
     * revision.
     *
     * @return the edit date for this revision.
     */
    Date getEditDate();

    /**
     * Get the name of the author of this revision. This is the name of the
     * QVCS-Enterprise user who created (checked in) this revision.
     *
     * @return the name of the author of this revision.
     */
    String getRevisionAuthor();

    /**
     * Get the name of the QVCS-Enterprise user who has locked this revision.
     *
     * @return the name of the QVCS-Enterprise user who has locked this
     * revision, or an empty string if this revision is not locked.
     */
    String getRevisionLocker();

    /**
     * Get this revision's revision string. This will always be an even number
     * of elements, like 1.5, or 1.5.1.3. A revision number is always unique for
     * a file; i.e. there is always only one revision 1.0 in a file; there is
     * only one revision 1.1 in a file, etc.
     *
     * @return the revision number of this revision.
     */
    String getRevisionString();

    /**
     * Get the revision description. This is the revision comment that was
     * entered by the user as the checkin comment when they created this
     * revision.
     *
     * @return the revision description.
     */
    String getRevisionDescription();

    /**
     * Get a boolean to indicate whether this revision is locked or not.
     *
     * @return true if the revision is locked; false if the revision is not
     * locked.
     */
    boolean getIsRevisionLocked();

    /**
     * Get a boolean to indicate whether this revision is a tip revision. A tip
     * revision is the most recent revision on a given branch.
     *
     * @return true if this is a tip revision; false if the revision is not the
     * tip revision.
     */
    boolean getIsTipRevision();
}
