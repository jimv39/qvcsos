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

import com.qumasoft.qvcslib.ServerProperties;

/**
 * Default server tree node.
 * @author Jim Voris
 */
public class DefaultServerTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = -7884032596226325942L;

    /**
     * Create a default server tree node.
     * @param serverProperties the server properties.
     */
    public DefaultServerTreeNode(ServerProperties serverProperties) {
        super(serverProperties);
    }
}
