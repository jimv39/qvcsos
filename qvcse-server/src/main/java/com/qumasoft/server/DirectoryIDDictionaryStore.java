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
package com.qumasoft.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Directory id dictionary store.
 * @author Jim Voris
 */
public final class DirectoryIDDictionaryStore implements java.io.Serializable {
    private static final long serialVersionUID = 6268214174444891947L;

    // This is the map of dictionary ID to DictionaryIDInfo objects.
    private final Map<String, DictionaryIDInfo> dictionaryIdInfoMap = Collections.synchronizedMap(new HashMap<String, DictionaryIDInfo>());

    /**
     * Creates a new instance of DirectoryIDDictionaryStore.
     */
    public DirectoryIDDictionaryStore() {
    }

    void saveDictionaryIDInfo(int dictionaryID, DictionaryIDInfo dictionaryIDInfo) {
        String key = createKeyValue(dictionaryIDInfo.getProjectName(), dictionaryID);
        dictionaryIdInfoMap.put(key, dictionaryIDInfo);
    }

    DictionaryIDInfo retrieveDictionaryIDInfo(String projectName, int dictionaryID) {
        String key = createKeyValue(projectName, dictionaryID);
        return dictionaryIdInfoMap.get(key);
    }

    private String createKeyValue(String projectName, int dictionaryID) {
        return projectName + ":" + dictionaryID;
    }
}
