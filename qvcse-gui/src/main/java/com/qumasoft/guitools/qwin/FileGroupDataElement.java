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
package com.qumasoft.guitools.qwin;

/**
 * File group data element. Instances of this class are immutable.
 * @author Jim Voris
 */
public class FileGroupDataElement {

    private final String fileGroupName;
    private final String[] fileGroupExtensions;

    /**
     * Create a new file group data element.
     * @param groupName the group name.
     * @param extensions the list of extensions that are members of this file group.
     */
    public FileGroupDataElement(String groupName, String[] extensions) {
        fileGroupName = groupName;
        fileGroupExtensions = extensions;
    }

    /**
     * Get the file group name.
     * @return the file group name.
     */
    public String getGroupName() {
        return fileGroupName;
    }

    /**
     * Get the list of file extensions in the file group.
     * @return the list of file extensions in the file group.
     */
    public String[] getExtensions() {
        return fileGroupExtensions;
    }
}
