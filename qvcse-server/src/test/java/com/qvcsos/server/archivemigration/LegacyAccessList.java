/*
 * Copyright 2021 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qvcsos.server.archivemigration;

import com.qumasoft.qvcslib.QVCSRuntimeException;

/**
 *
 * @author Jim Voris
 */
public class LegacyAccessList {
    /**
     * An array of users that are on this access list.
     */
    private String[] accessList;

    public LegacyAccessList(String accessLst) {
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

    public String indexToUser(int index) {
        if (index >= 0 && index < accessList.length) {
            return accessList[index];
        } else {
            throw new QVCSRuntimeException("bad user index in AccessList.indexToUser(): " + index);
        }
    }
}
