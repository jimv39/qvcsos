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
package com.qumasoft.server;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Directory Contents manager factory. A factory class to create directory contents managers.
 *
 * @author Jim Voris
 */
public final class DirectoryContentsManagerFactory {

    /**
     * This is a singleton
     */
    private static final DirectoryContentsManagerFactory DIRECTORY_CONTENTS_MANAGER_FACTORY = new DirectoryContentsManagerFactory();
    /**
     * Map for the DirectoryContentsManager objects. One per project
     */
    private final Map<String, DirectoryContentsManager> directoryContentsManagerMap = Collections.synchronizedMap(new TreeMap<String, DirectoryContentsManager>());

    /**
     * Creates a new instance of DirectoryContentsManagerFactory.
     */
    private DirectoryContentsManagerFactory() {
    }

    /**
     * Get the Directory contents manager factory singleton.
     *
     * @return the Directory contents manager factory singleton.
     */
    public static DirectoryContentsManagerFactory getInstance() {
        return DIRECTORY_CONTENTS_MANAGER_FACTORY;
    }

    /**
     * Get the directory contents manager for a given project.
     *
     * @param projectName the project name.
     * @return the directory contents manager for the given project.
     */
    public DirectoryContentsManager getDirectoryContentsManager(final String projectName) {
        if (directoryContentsManagerMap.containsKey(projectName)) {
            return directoryContentsManagerMap.get(projectName);
        } else {
            DirectoryContentsManager directoryContentsManager = new DirectoryContentsManager(projectName);
            directoryContentsManagerMap.put(projectName, directoryContentsManager);
            return directoryContentsManager;
        }
    }
}
