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
package com.qumasoft.guitools.admin;

/**
 * User Role Tree Node.
 *
 * @author Jim Voris
 */
public class UserRoleTreeNode extends javax.swing.tree.DefaultMutableTreeNode {

    private static final long serialVersionUID = -3868339000191246048L;

    /**
     * Creates new UserRoleTreeNode.
     *
     * @param userRole The role associated with this node.
     */
    public UserRoleTreeNode(String userRole) {
        super(userRole);
    }
}
