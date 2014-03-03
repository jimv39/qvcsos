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
 * Directory coordinate. Simple class to capture the coordinates of a directory. Instances of this class are immutable.
 *
 * @author Jim Voris
 */
public class DirectoryCoordinate {

    private final String projectName;
    private final String viewName;
    private final String appendedPath;

    /**
     * Create an instance, using the given parameters.
     * @param project the project name.
     * @param view the view name.
     * @param path the appended path;
     */
    public DirectoryCoordinate(String project, String view, String path) {
        projectName = project;
        viewName = view;
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
     * Get the view name.
     * @return the view name.
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }
}
