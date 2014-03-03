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
package com.qumasoft.guitools.qwin;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * File group store.
 * @author Jim Voris
 */
public class FileGroupStore implements java.io.Serializable {
    private static final long serialVersionUID = -1749902922971089780L;

    // This is what gets serialized:
    private boolean fileGroupsEnabledFlag;
    private final Map<String, String[]> fileGroupMap = Collections.synchronizedMap(new TreeMap<String, String[]>());
    private final Map<String, String> extensionToGroupMap = Collections.synchronizedMap(new TreeMap<String, String>());

    /**
     * Creates a new instance of FileGroupStore.
     */
    public FileGroupStore() {
    }

    /**
     * Get the file group enabled flag.
     * @return the file group enabled flag.
     */
    public boolean getEnabledFlag() {
        return fileGroupsEnabledFlag;
    }

    /**
     * Set the file group enabled flag.
     * @param flag the file group enabled flag.
     */
    public void setEnabledFlag(boolean flag) {
        fileGroupsEnabledFlag = flag;
    }

    void storeGroup(String groupName, String[] fileExtensions) {
        fileGroupMap.put(groupName, fileExtensions);
        for (String fileExtension : fileExtensions) {
            String extension = fileExtension.toLowerCase();
            extensionToGroupMap.put(extension, groupName);
        }
    }

    void removeAllGroups() {
        fileGroupMap.clear();
        extensionToGroupMap.clear();
    }

    int getGroupCount() {
        return fileGroupMap.size();
    }

    synchronized String getGroupName(int index) {
        int i = 0;
        Iterator it = fileGroupMap.keySet().iterator();
        String groupName = null;
        while (it.hasNext()) {
            Object key = it.next();
            if (i == index) {
                groupName = (String) key;
                break;
            }
            i++;
        }
        return groupName;
    }

    synchronized String[] getGroupFileExtensions(int index) {
        int i = 0;
        Iterator it = fileGroupMap.values().iterator();
        String[] extensions = null;
        while (it.hasNext()) {
            Object values = it.next();
            if (i == index) {
                extensions = (String[]) values;
                break;
            }
            i++;
        }
        return extensions;
    }

    String lookupGroupForExtension(String extension) {
        String lowerCaseExtension = extension.toLowerCase();
        String groupName = extensionToGroupMap.get(lowerCaseExtension);
        return groupName;
    }
}
