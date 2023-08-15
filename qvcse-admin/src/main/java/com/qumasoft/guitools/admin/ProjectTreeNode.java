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
package com.qumasoft.guitools.admin;

import com.qumasoft.qvcslib.RemotePropertiesBaseClass;

/**
 * Project tree node. The node type we use for project nodes.
 *
 * @author Jim Voris
 */
public class ProjectTreeNode extends javax.swing.tree.DefaultMutableTreeNode {

    private static final long serialVersionUID = 8445789772439836183L;

    /**
     * the server name.
     */
    private final String instanceServerName;
    /**
     * the project properties.
     */
    private final RemotePropertiesBaseClass instanceProjectProperties;
    private final String projectName;

    /**
     * Creates new ProjectTreeNode.
     *
     * @param serverName the server name.
     * @param projectProperties the project properties.
     * @param projName project name.
     */
    public ProjectTreeNode(String serverName, RemotePropertiesBaseClass projectProperties, String projName) {
        super(projName);
        instanceServerName = serverName;
        instanceProjectProperties = projectProperties;
        projectName = projName;
    }

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the server name.
     *
     * @return the server name.
     */
    public String getServerName() {
        return instanceServerName;
    }

    /**
     * Get the project properties.
     *
     * @return the project properties.
     */
    public RemotePropertiesBaseClass getProjectProperties() {
        return instanceProjectProperties;
    }
}
