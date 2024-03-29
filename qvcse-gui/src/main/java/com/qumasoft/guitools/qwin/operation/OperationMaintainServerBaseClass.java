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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.ProjectTreeModel;
import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.ServerPropertiesDialog;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.Utility;

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
    public OperationMaintainServerBaseClass(String serverName, RemotePropertiesBaseClass userLocationProperties) {
        super(null, serverName, null, null, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        ServerPropertiesDialog addServerDialog = new ServerPropertiesDialog(QWinFrame.getQWinFrame(), this, true, getServerName());
        addServerDialog.setVisible(true);
    }

    /**
     * Process the dialog choices.
     * @param addServerDialog the add server dialog.
     */
    public void processDialogResult(ServerPropertiesDialog addServerDialog) {
        try {
            if (addServerDialog.getIsOK()) {
                // Save the information to the server properties file.
                ServerProperties serverProperties = new ServerProperties(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), addServerDialog.getServerName());
                serverProperties.setServerName(addServerDialog.getServerName());
                serverProperties.setServerIPAddress(addServerDialog.getServerIPAddress());
                serverProperties.setClientPort(addServerDialog.getClientPort());
                serverProperties.saveProperties();

                // Re-load the server/project tree.
                ProjectTreeModel treeModel = QWinFrame.getQWinFrame().getTreeModel();
                treeModel.reloadServerNodes();
            }
        } catch (Exception e) {
            warnProblem(e.getMessage());
            warnProblem(Utility.expandStackTraceToString(e));
        }
    }
}
