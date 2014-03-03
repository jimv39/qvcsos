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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.util.logging.Level;

/**
 * Maintain server operation base class.
 * @author Jim Voris
 */
public class OperationMaintainServerBaseClass extends OperationBaseClass {

    /**
     * Create a maintain server operation base class.
     * @param serverName the server name.
     * @param userLocationProperties user location properties.
     */
    public OperationMaintainServerBaseClass(String serverName, UserLocationProperties userLocationProperties) {
        super(null, serverName, null, null, userLocationProperties);
    }

    @Override
    void executeOperation() {
        ServerPropertiesDialog addServerDialog = new ServerPropertiesDialog(QWinFrame.getQWinFrame(), this, true, getServerName());
        addServerDialog.setVisible(true);
    }

    void processDialogResult(ServerPropertiesDialog addServerDialog) {
        try {
            if (addServerDialog.getIsOK()) {
                // Save the information to the server properties file.
                ServerProperties serverProperties = new ServerProperties(addServerDialog.getServerName());
                serverProperties.setServerName(addServerDialog.getServerName());
                serverProperties.setServerIPAddress(addServerDialog.getServerIPAddress());
                serverProperties.setClientPort(addServerDialog.getClientPort());
                serverProperties.saveProperties();

                // Re-load the server/project tree.
                ProjectTreeModel treeModel = QWinFrame.getQWinFrame().getTreeModel();
                treeModel.reloadServerNodes();
            }
        } catch (Exception e) {
            QWinUtility.logProblem(Level.WARNING, e.getMessage());
            QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }
}
