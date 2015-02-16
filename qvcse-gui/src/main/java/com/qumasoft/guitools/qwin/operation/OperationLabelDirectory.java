/*   Copyright 2004-2015 Jim Voris
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

import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.LabelDirectoryDialog;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.LabelDirectoryCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestLabelDirectoryData;
import java.io.File;

/**
 * Label a directory operation.
 * @author Jim Voris
 */
public class OperationLabelDirectory extends OperationBaseClass {

    private final String appendedPath;
    private final AbstractProjectProperties projectProperties;

    /**
     * Create a label directory operation.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param path the appended path.
     * @param userLocationProperties user location properties.
     * @param projProperties project properties.
     * @param currentWorkDirectory the current workfile directory.
     */
    public OperationLabelDirectory(final String serverName, final String projectName, final String viewName, final String path,
                                   UserLocationProperties userLocationProperties, AbstractProjectProperties projProperties, File currentWorkDirectory) {
        super(null, serverName, projectName, viewName, userLocationProperties);

        appendedPath = path;
        projectProperties = projProperties;
    }

    String getAppendedPath() {
        return appendedPath;
    }

    String getProjectType() {
        return projectProperties.getProjectType();
    }

    AbstractProjectProperties getProjectProperties() {
        return projectProperties;
    }

    @Override
    public void executeOperation() {
        LabelDirectoryDialog labelDirectoryDialog = new LabelDirectoryDialog(QWinFrame.getQWinFrame(), this);
        labelDirectoryDialog.setVisible(true);
    }

    /**
     * Process the dialog choices.
     * @param commandArgs the command arguments.
     */
    public void completeOperation(final LabelDirectoryCommandArgs commandArgs) {
        Runnable worker = () -> {
            TransportProxyInterface transportProxy = null;
            int transactionID = 0;

            try {
                DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(getServerName(), getProjectName(), getViewName(),
                        getAppendedPath(), getProjectType());
                ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) directoryManager.getArchiveDirManager();
                transportProxy = archiveDirManagerProxy.getTransportProxy();

                ClientRequestLabelDirectoryData clientRequestLabelDirectoryData = new ClientRequestLabelDirectoryData();
                clientRequestLabelDirectoryData.setAppendedPath(getAppendedPath());
                clientRequestLabelDirectoryData.setProjectName(getProjectName());
                clientRequestLabelDirectoryData.setViewName(getViewName());
                clientRequestLabelDirectoryData.setCommandArgs(commandArgs);

                // Make sure this is synchronized
                synchronized (transportProxy) {
                    transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                    transportProxy.write(clientRequestLabelDirectoryData);
                }
            } catch (Exception e) {
                warnProblem("operationLabelDirectory caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            } finally {
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }
}
