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

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.QWinUtility;
import com.qumasoft.guitools.qwin.dialog.UnLabelDirectoryDialog;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientRequestUnLabelDirectoryData;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.LabelManager;
import com.qumasoft.qvcslib.LogFileOperationUnLabelDirectoryCommandArgs;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.util.logging.Level;

/**
 * Unlabel directory operation.
 * @author Jim Voris
 */
public class OperationUnLabelDirectory extends OperationBaseClass {

    private final String appendedPath;
    private final AbstractProjectProperties projectProperties;

    /**
     * Create an unlabel directory operation.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param path the appended path.
     * @param userLocationProperties user location properties.
     * @param projProperties project properties.
     * @param currentWorkfileDirectory the current workfile directory.
     */
    public OperationUnLabelDirectory(final String serverName, final String projectName, final String viewName, final String path,
                                     UserLocationProperties userLocationProperties, AbstractProjectProperties projProperties, File currentWorkfileDirectory) {
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
        UnLabelDirectoryDialog unlabelDirectoryDialog = new UnLabelDirectoryDialog(QWinFrame.getQWinFrame(), this);
        unlabelDirectoryDialog.setVisible(true);
    }

    /**
     * Process the dialog choices.
     * @param commandArgs the command arguments.
     */
    public void completeOperation(final LogFileOperationUnLabelDirectoryCommandArgs commandArgs) {
        Runnable worker = new Runnable() {

            @Override
            public void run() {
                TransportProxyInterface transportProxy = null;
                int transactionID = 0;

                try {
                    DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(getServerName(), getProjectName(), getViewName(),
                            getAppendedPath(), getProjectType());
                    ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) directoryManager.getArchiveDirManager();
                    transportProxy = archiveDirManagerProxy.getTransportProxy();

                    ClientRequestUnLabelDirectoryData clientRequestUnLabelDirectoryData = new ClientRequestUnLabelDirectoryData();
                    clientRequestUnLabelDirectoryData.setAppendedPath(getAppendedPath());
                    clientRequestUnLabelDirectoryData.setProjectName(getProjectName());
                    clientRequestUnLabelDirectoryData.setViewName(getViewName());
                    clientRequestUnLabelDirectoryData.setCommandArgs(commandArgs);

                    // If we are removing the label from the root of the project, then
                    // delete the label from the label manager.
                    if (commandArgs.getRecurseFlag() && (getAppendedPath().length() == 0)) {
                        LabelManager.getInstance().removeLabel(getProjectName(), commandArgs.getLabelString());
                    }

                    transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                    synchronized (transportProxy) {
                        transportProxy.write(clientRequestUnLabelDirectoryData);
                    }
                } catch (Exception e) {
                    QWinUtility.logProblem(Level.WARNING, "operationUnLabelDirectory caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                } finally {
                    ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                }
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }
}
