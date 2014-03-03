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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.LogFileOperationUnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTable;

/**
 * Undo checkout operation.
 * @author Jim Voris
 */
public class OperationUndoCheckOut extends OperationBaseClass {

    private final boolean useDialogFlag;

    /**
     * Create an undo checkout operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     * @param flag use the dialog or not.
     */
    public OperationUndoCheckOut(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties,
                                 boolean flag) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
        useDialogFlag = flag;
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                List mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() > 0) {
                    if (useDialogFlag) {
                        UnDoCheckoutDialog unDoCheckoutDialog = new UnDoCheckoutDialog(QWinFrame.getQWinFrame(), mergedInfoArray, this);
                        unDoCheckoutDialog.setVisible(true);
                    } else {
                        // Don't use a dialog, just unlock the file(s).
                        LogFileOperationUnlockRevisionCommandArgs commandArgs = new LogFileOperationUnlockRevisionCommandArgs();

                        // TODO -- If there are user preferences, this would be where
                        // I would use the preferences to determine what kind of
                        // behavior to provide.
                        commandArgs.setUndoCheckoutBehavior(Utility.UndoCheckoutBehavior.JUST_UNLOCK_ARCHIVE);
                        completeOperation(mergedInfoArray, commandArgs);
                    }
                }
            } catch (Exception e) {
                QWinUtility.logProblem(Level.WARNING, "operationGet caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }
    }

    void completeOperation(final List mergedInfoArray, final LogFileOperationUnlockRevisionCommandArgs dialogCommandArgs) {
        if (mergedInfoArray.size() >= 1) {
            // Display the progress dialog.
            final ProgressDialog progressMonitor = createProgressDialog("UnLocking revisions from QVCS Archive", mergedInfoArray.size());

            Runnable worker = new Runnable() {

                @Override
                public void run() {
                    TransportProxyInterface transportProxy = null;
                    int transactionID = 0;

                    try {
                        int size = mergedInfoArray.size();
                        for (int i = 0; i < size; i++) {
                            if (progressMonitor.getIsCancelled()) {
                                break;
                            }

                            MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoArray.get(i);

                            if (i == 0) {
                                ArchiveDirManagerInterface archiveDirManager = mergedInfo.getArchiveDirManager();
                                ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) archiveDirManager;
                                transportProxy = archiveDirManagerProxy.getTransportProxy();
                                transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                            }

                            if (mergedInfo.getArchiveInfo() == null) {
                                continue;
                            }

                            // Do not request an undo checkout if the file is in the cemetery.
                            String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                                QWinUtility.logProblem(Level.INFO, "Undo checkout request for cemetery file ignored for " + mergedInfo.getShortWorkfileName());
                                continue;
                            }
                            // Do not request an undo checkout if the file is in the branch archives directory.
                            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                                QWinUtility.logProblem(Level.INFO, "Undo checkout request for branch archives file ignored for " + mergedInfo.getShortWorkfileName());
                                continue;
                            }

                            // Update the progress monitor.
                            OperationBaseClass.updateProgressDialog(i, "Undo checkout for: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                            String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
                            String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator
                                    + mergedInfo.getShortWorkfileName();

                            // Create the command args
                            LogFileOperationUnlockRevisionCommandArgs commandArgs = new LogFileOperationUnlockRevisionCommandArgs();
                            commandArgs.setUserName(mergedInfo.getUserName());
                            commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                            commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                            commandArgs.setFullWorkfileName(fullWorkfileName);
                            commandArgs.setOutputFileName(fullWorkfileName);
                            commandArgs.setUndoCheckoutBehavior(dialogCommandArgs.getUndoCheckoutBehavior());

                            if (mergedInfo.getIsRemote()) {
                                if (mergedInfo.unlockRevision(commandArgs)) {
                                    // Log the success.
                                    QWinUtility.logProblem(Level.INFO, "Requested unlock of revision " + commandArgs.getRevisionString() + " for " + fullWorkfileName
                                            + " from server.");
                                }
                            } else {
                                QWinUtility.logProblem(Level.WARNING, "Local undo checkout operation not supported!!");
                            }
                        }
                    } catch (QVCSException e) {
                        QWinUtility.logProblem(Level.WARNING, "operationUnlockRevision caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                        QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                    } finally {
                        progressMonitor.close();
                        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                    }
                }
            };

            // Put all this on a separate worker thread.
            new Thread(worker).start();
        }
    }
}
