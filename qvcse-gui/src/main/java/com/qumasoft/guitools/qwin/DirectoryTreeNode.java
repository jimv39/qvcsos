/*   Copyright 2004-2023 Jim Voris
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

import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import java.io.File;

/**
 * Directory tree node.
 * @author Jim Voris
 */
public class DirectoryTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = -7823829946803685850L;

    private final String projectName;
    private final String branchName;
    private final String appendedPath;
    private final RemotePropertiesBaseClass projectProperties;

    /**
     * Create a directory tree node.
     * @param projName the project name.
     * @param branch the branch name.
     * @param path the appended path.
     * @param projProperties the project properties.
     */
    public DirectoryTreeNode(String projName, String branch, String path, RemotePropertiesBaseClass projProperties) {
        super(path);
        projectName = projName;
        branchName = branch;
        appendedPath = path;
        projectProperties = projProperties;
    }

    /**
     * Get the project name.
     * @return the branch name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
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
    public RemotePropertiesBaseClass getProjectProperties() {
        return projectProperties;
    }

    @Override
    public String toString() {
        // Return just the last portion of the full path of this node.
        return appendedPath.substring(1 + appendedPath.lastIndexOf(File.separator));
    }
}
