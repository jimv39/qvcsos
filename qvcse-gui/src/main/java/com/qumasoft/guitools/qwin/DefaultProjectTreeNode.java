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

/**
 * Default project tree node.
 * @author Jim Voris
 */
public class DefaultProjectTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = -1727419389947871886L;

    /**
     * Create a default project tree node.
     * @param projectProperties the project properties.
     */
    public DefaultProjectTreeNode(RemotePropertiesBaseClass projectProperties) {
        super(projectProperties);
    }
}
