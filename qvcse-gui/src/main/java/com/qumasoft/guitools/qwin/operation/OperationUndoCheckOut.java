/*   Copyright 2004-2019 Jim Voris
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
import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.guitools.qwin.dialog.UnDoCheckoutDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import java.io.File;
import java.util.List;
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
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     * @param flag use the dialog or not.
     */
    public OperationUndoCheckOut(JTable fileTable, final String serverName, final String projectName, final String branchName, UserLocationProperties userLocationProperties,
                                 boolean flag) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
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
                        UnlockRevisionCommandArgs commandArgs = new UnlockRevisionCommandArgs();

                        // TODO -- If there are user preferences, this would be where
                        // I would use the preferences to determine what kind of
                        // behavior to provide.
                        commandArgs.setUndoCheckoutBehavior(Utility.UndoCheckoutBehavior.JUST_UNLOCK_ARCHIVE);
                        completeOperation(mergedInfoArray, commandArgs);
                    }
                }
            } catch (Exception e) {
                warnProblem("operationGet caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process the dialog choices.
     * @param mergedInfoArray the list of files to operate on.
     * @param dialogCommandArgs the command arguments.
     */
    public void completeOperation(final List mergedInfoArray, final UnlockRevisionCommandArgs dialogCommandArgs) {
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
                                logProblem("Undo checkout request for cemetery file ignored for " + mergedInfo.getShortWorkfileName());
                                continue;
                            }
                            // Do not request an undo checkout if the file is in the branch archives directory.
                            if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                                logProblem("Undo checkout request for branch archives file ignored for " + mergedInfo.getShortWorkfileName());
                                continue;
                            }

                            // Update the progress monitor.
                            OperationBaseClass.updateProgressDialog(i, "Undo checkout for: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                            String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
                            String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator
                                    + mergedInfo.getShortWorkfileName();

                            // Create the command args
                            UnlockRevisionCommandArgs commandArgs = new UnlockRevisionCommandArgs();
                            commandArgs.setUserName(mergedInfo.getUserName());
                            commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                            commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                            commandArgs.setFullWorkfileName(fullWorkfileName);
                            commandArgs.setOutputFileName(fullWorkfileName);
                            commandArgs.setUndoCheckoutBehavior(dialogCommandArgs.getUndoCheckoutBehavior());

                            if (mergedInfo.getIsRemote()) {
                                if (mergedInfo.unlockRevision(commandArgs)) {
                                    // Log the success.
                                    logProblem("Requested unlock of revision " + commandArgs.getRevisionString() + " for " + fullWorkfileName
                                            + " from server.");
                                }
                            } else {
                                warnProblem("Local undo checkout operation not supported!!");
                            }
                        }
                    } catch (QVCSException e) {
                        warnProblem("operationUnlockRevision caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                        warnProblem(Utility.expandStackTraceToString(e));
                    } finally {
                        progressMonitor.close();
                        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                    }
                }

                private void warnProblem(String expandStackTraceToString) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };

            // Put all this on a separate worker thread.
            new Thread(worker).start();
        }
    }
}
