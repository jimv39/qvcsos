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

/**
 * A class to handle QVCS archive access lists.
 *
 * @author Jim Voris
 */
public class AccessList implements java.io.Serializable {
    private static final long serialVersionUID = 1980697418386410481L;

    /**
     * An array of users that are on this access list.
     */
    private String[] accessList;

    /**
     * Construct an access list. The string should be a comma separated list of users.
     *
     * @param accessLst A comma separated string of users on the access list.
     */
    public AccessList(String accessLst) {
        // Count the number of users on the access list.
        int iStartIndex = 0;
        int i;
        for (i = 0;; i++) {
            int iCommaIndex = accessLst.indexOf(",", iStartIndex);
            if (iCommaIndex == -1) {
                break;
            }
            iStartIndex = iCommaIndex + 1;
        }

        // Create our string array for holding the users.
        accessList = new String[i + 1];

        // Add the users to the array.
        for (i = 0, iStartIndex = 0; i < accessList.length; i++) {
            int iCommaIndex = accessLst.indexOf(",", iStartIndex);
            if (iCommaIndex == -1) {
                accessList[i] = accessLst.substring(iStartIndex).trim();
            } else {
                accessList[i] = accessLst.substring(iStartIndex, iCommaIndex).trim();
                iStartIndex = iCommaIndex + 1;
            }
        }
    }

    /**
     * Build an access list using the passed in array of user names.
     *
     * @param accessLst an array of user names.
     */
    public AccessList(String[] accessLst) {
        // Create our string array for holding the users.
        accessLst = new String[accessLst.length];
        System.arraycopy(accessLst, 0, accessLst, 0, accessLst.length);
    }

    /**
     * Return true if the user is on the access list, false if not
     *
     * @param user A string representing the user to check for.
     * @return true if the user is on the access list, false if not.
     */
    private boolean isOnAccessList(String user) {
        boolean bReturnValue = false;
        for (String accessListMember : accessList) {
            if (user.compareTo(accessListMember) == 0) {
                bReturnValue = true;
                break;
            }
        }
        return bReturnValue;
    }

    /**
     * Convert an index into the user name associated with that index.
     *
     * @return A String that represents the given user.
     * @param index The index (0 based) to convert to a string.
     */
    public String indexToUser(int index) {
        if (index >= 0 && index < accessList.length) {
            return accessList[index];
        } else {
            throw new QVCSRuntimeException("bad user index in AccessList.indexToUser(): " + index);
        }
    }

    /**
     * Convert a user string to an index within the access list.
     *
     * @param user A String representing the user name.
     * @return Returns an integer index (0 based) for that user within the access list, or -1 if the user isn't on the access list.
     */
    public int userToIndex(String user) {
        int iReturnValue = -1;
        for (int i = 0; i < accessList.length; i++) {
            if (user.compareTo(accessList[i]) == 0) {
                iReturnValue = i;
            }
        }
        return iReturnValue;
    }

    String[] getAccessListAsStringArray() {
        return accessList;
    }

    /**
     * Return a comma separated list of users that compose this access list object.
     *
     * @return a comma separated list of users that compose this access list.
     */
    public String getAccessListAsCommaSeparatedString() {
        StringBuilder accessStringBuffer = new StringBuilder();
        for (int i = 0; i < accessList.length; i++) {
            accessStringBuffer.append(accessList[i]);
            if (i < accessList.length - 1) {
                accessStringBuffer.append(',');
            }
        }
        return accessStringBuffer.toString();
    }

    /**
     * Add a user to the access list.
     *
     * @param userName The user name to add to the access list.
     * @return return true if the access list was modified as a result of the request to add the user. Return false if the access list was not changed.
     */
    public boolean addUser(String userName) {
        boolean flag = false;

        if (!isOnAccessList(userName)) {
            if ((accessList.length == 1) && (accessList[0].length() == 0)) {
                accessList[0] = userName;
            } else {
                String[] newAccessListArray = new String[accessList.length + 1];
                int i;
                for (i = 0; i < accessList.length; i++) {
                    newAccessListArray[i] = accessList[i];
                }
                newAccessListArray[i] = userName;
                accessList = newAccessListArray;
            }

            // Provide indication that we modified the access list.
            flag = true;
        }
        return flag;
    }
}
