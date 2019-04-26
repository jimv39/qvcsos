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

/**
 * Dictionary id info. Instances of this class are immutable.
 * @author Jim Voris
 */
public class DictionaryIDInfo implements java.io.Serializable {
    private static final long serialVersionUID = 3494485674894027367L;

    private final String projectName;
    private final String appendedPath;

    /**
     * Creates a new instance of DictionaryIDInfo.
     *
     * @param project the project name.
     * @param path the appended path.
     */
    public DictionaryIDInfo(String project, String path) {
        projectName = project;
        appendedPath = path;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }
}
