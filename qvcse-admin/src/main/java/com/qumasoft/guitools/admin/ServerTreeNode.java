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

import com.qumasoft.qvcslib.ServerProperties;

/**
 * Server tree node.
 *
 * @author Jim Voris
 */
public class ServerTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = 7809246885291679175L;

    private final ServerProperties serverProperties;

    /**
     * Creates new DefaultProjectTreeNode.
     *
     * @param argServerProperties server properties.
     */
    public ServerTreeNode(ServerProperties argServerProperties) {
        super(argServerProperties.getServerName());
        this.serverProperties = argServerProperties;
    }

    /**
     * Get the server properties.
     *
     * @return the server properties.
     */
    public ServerProperties getServerProperties() {
        return this.serverProperties;
    }
}
