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
 * Default user tree node.
 *
 * @author Jim Voris
 */
public class DefaultUserTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = 3336774762676002399L;

    private String serverNameMember = null;

    /**
     * Creates new DefaultUserTreeNode.
     *
     * @param serverName the server name.
     */
    public DefaultUserTreeNode(String serverName) {
        super(serverName + QVCSConstants.QVCS_DEFAULT_USER_TREE_NAME);
        serverNameMember = serverName;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    String getServerName() {
        return serverNameMember;
    }
}
