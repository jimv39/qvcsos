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
 * Drop transfer data. (for drag-and-drop). Instances of this class are immutable.
 * @author Jim Voris
 */
public class DropTransferData implements java.io.Serializable {
    private static final long serialVersionUID = 5979005555955171526L;

    private final String projectName;
    private final String viewName;
    private final String appendedPath;
    private final String shortWorkfileName;

    /**
     * Create a new drop transfer data instance.
     * @param project the project name.
     * @param view the view name.
     * @param path the appended path.
     * @param shortName the short workfile name.
     */
    public DropTransferData(final String project, final String view, final String path, final String shortName) {
        projectName = project;
        viewName = view;
        appendedPath = path;
        shortWorkfileName = shortName;
    }

    String getProjectName() {
        return projectName;
    }

    String getViewName() {
        return viewName;
    }

    String getAppendedPath() {
        return appendedPath;
    }

    String getShortWorkfileName() {
        return shortWorkfileName;
    }
}
