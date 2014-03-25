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

import com.qumasoft.guitools.qwin.FilterCollection;
import com.qumasoft.guitools.qwin.FilteredFileTableModel;
import com.qumasoft.guitools.qwin.dialog.GetDirectoryDialog;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.QWinUtility;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetDirectoryData;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.commandargs.GetDirectoryCommandArgs;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.util.logging.Level;

/**
 * Get a directory's files.
 * @author Jim Voris
 */
public class OperationGetDirectory extends OperationBaseClass {

    private final String appendedPath;
    private final AbstractProjectProperties projectProperties;

    /**
     * Create a get directory operation.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param path the appended path.
     * @param userLocationProperties user location properties.
     * @param projProperties project properties.
     * @param currentWorkfileDirectory the current workfile directory.
     */
    public OperationGetDirectory(final String serverName, final String projectName, final String viewName, final String path,
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
        GetDirectoryDialog getDirectoryDialog = new GetDirectoryDialog(QWinFrame.getQWinFrame(), this);
        getDirectoryDialog.setVisible(true);
    }

    /**
     * Process the dialog choices.
     * @param commandArgs the command arguments.
     */
    public void completeOperation(final GetDirectoryCommandArgs commandArgs) {
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

                    ClientRequestGetDirectoryData clientRequestGetDirectoryData = new ClientRequestGetDirectoryData();
                    clientRequestGetDirectoryData.setAppendedPath(getAppendedPath());
                    clientRequestGetDirectoryData.setProjectName(getProjectName());
                    clientRequestGetDirectoryData.setViewName(getViewName());

                    // Set the workfile base directory...
                    String workfileBaseDirectory = QWinFrame.getQWinFrame().getUserLocationProperties().getWorkfileLocation(transportProxy.getServerProperties().getServerName(),
                            getProjectName(), getViewName());
                    commandArgs.setWorkfileBaseDirectory(workfileBaseDirectory);

                    // Set the ignore obsolete files flag...
                    FilteredFileTableModel filteredFileTableModel = (FilteredFileTableModel) QWinFrame.getQWinFrame().getRightFilePane().getModel();
                    FilterCollection filterCollection = filteredFileTableModel.getFilterCollection();

                    clientRequestGetDirectoryData.setCommandArgs(commandArgs);

                    // Make sure this is synchronized
                    synchronized (transportProxy) {
                        transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                        clientRequestGetDirectoryData.setTransactionID(transactionID);
                        transportProxy.write(clientRequestGetDirectoryData);
                    }
                } catch (Exception e) {
                    QWinUtility.logProblem(Level.WARNING, "operationGetDirectory caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
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
