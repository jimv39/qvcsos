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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.ProjectTreeModel;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.UserLocationProperties;
import javax.swing.JOptionPane;

/**
 * Remove a server operation. Note that this does not round-trip to the server in question. It just basically deletes the associated server's property file, and then does
 * refresh of the display.
 * @author Jim Voris
 */
public final class OperationRemoveServer extends OperationBaseClass {

    /**
     * Create a remove server operation.
     * @param serverName the server name.
     * @param userLocationProperties user location properties.
     */
    public OperationRemoveServer(String serverName, UserLocationProperties userLocationProperties) {
        super(null, serverName, null, null, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        String serverMessage = "Delete server '" + getServerName() + "' ?";
        int choice = JOptionPane.showConfirmDialog(null, serverMessage, "Delete Server Definition", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            ServerProperties serverProperties = new ServerProperties(getServerName());
            serverProperties.removePropertiesFile();

            // Re-load the server/project tree.
            ProjectTreeModel treeModel = QWinFrame.getQWinFrame().getTreeModel();
            treeModel.reloadServerNodes();
        }
    }
}
