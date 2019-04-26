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

/**
 * The node we use for users.
 *
 * @author Jim Voris
 */
public class UserTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = 4531738038201201989L;

    /**
     * Creates new UserTreeNode.
     *
     * @param userName the QVCS user name that gets displayed for the user node.
     */
    public UserTreeNode(String userName) {
        super(userName);
    }
}
