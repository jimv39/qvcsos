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
package com.qumasoft.guitools.admin;

import com.qumasoft.qvcslib.QVCSConstants;

/**
 * Default project user tree node.
 *
 * @author Jim Voris
 */
public class DefaultProjectUserTreeNode extends javax.swing.tree.DefaultMutableTreeNode {

    private static final long serialVersionUID = -7604678611120523153L;

    private String projectNameMember = null;
    private String serverNameMember = null;

    /**
     * Creates new DefaultProjectUserTreeNode.
     * @param projectName the project name.
     * @param serverName the server name.
     */
    public DefaultProjectUserTreeNode(String projectName, String serverName) {
        super(projectName + QVCSConstants.QVCS_DEFAULT_USER_TREE_NAME);
        projectNameMember = projectName;
        serverNameMember = serverName;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectNameMember;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverNameMember;
    }
}
