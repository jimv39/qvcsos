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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import java.io.File;

/**
 * Directory tree node.
 * @author Jim Voris
 */
public class DirectoryTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = -7823829946803685850L;

    private final String fullDirectoryName;
    private final String viewName;
    private final String appendedPath;
    private final String projectRootDirectory;
    private final AbstractProjectProperties projectProperties;

    /**
     * Create a directory tree node.
     * @param view the view name.
     * @param path the appended path.
     * @param projProperties the project properties.
     */
    public DirectoryTreeNode(String view, String path, AbstractProjectProperties projProperties) {
        super(path);
        viewName = view;
        appendedPath = path;
        projectProperties = projProperties;
        projectRootDirectory = projProperties.getArchiveLocation() + File.separator;
        fullDirectoryName = projectRootDirectory + path;
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

    /**
     * Get the project properties.
     * @return the project properties.
     */
    public AbstractProjectProperties getProjectProperties() {
        return projectProperties;
    }

    @Override
    public String toString() {
        // Return just the last portion of the full path of this node.
        return fullDirectoryName.substring(1 + fullDirectoryName.lastIndexOf(File.separator));
    }
}
