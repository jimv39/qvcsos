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
package com.qumasoft.qvcslib.commandargs;

/**
 * Set revision description command arguments.
 * @author Jim Voris
 */
public final class SetRevisionDescriptionCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = -8216880257257446980L;

    private String userName;
    private String shortWorkfileName;
    private String newRevisionDescription;
    private String revisionString;

    /**
     * Creates a new instance of LogFileOperationSetRevisionDescriptionCommandArgs.
     */
    public SetRevisionDescriptionCommandArgs() {
    }

    /**
     * Get the user name.
     * @return the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name.
     * @param user the user name.
     */
    public void setUserName(String user) {
        userName = user;
    }

    /**
     * Get the revision string.
     * @return the revision string.
     */
    public String getRevisionString() {
        return revisionString;
    }

    /**
     * Set the revision string. This identifies which revision's checkin comment we will be changing.
     * @param revString the revision string.
     */
    public void setRevisionString(String revString) {
        revisionString = revString;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setShortWorkfileName(final String shortName) {
        shortWorkfileName = shortName;
    }

    /**
     * Set the new revision description. This becomes the new checkin comment for the given revision.
     * @param newRevDescription the new revision description.
     */
    public void setRevisionDescription(final String newRevDescription) {
        newRevisionDescription = newRevDescription;
    }

    /**
     * Get the new revision description.
     * @return the new revision description.
     */
    public String getRevisionDescription() {
        return newRevisionDescription;
    }
}
