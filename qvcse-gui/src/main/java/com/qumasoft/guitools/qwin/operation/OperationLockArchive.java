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

import com.qumasoft.guitools.qwin.LockRevisionDialog;
import com.qumasoft.guitools.qwin.ProgressDialog;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.QWinUtility;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.CheckOutCommentManager;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileOperationLockRevisionCommandArgs;
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
 * Lock archive operation.
 * @author Jim Voris
 */
public class OperationLockArchive extends OperationBaseClass {

    private final boolean useDialogFlag;
    private String checkOutComment;

    /**
     * Create a lock archive operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     * @param flag use the dialog or not.
     */
    public OperationLockArchive(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties,
                         boolean flag) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
        this.useDialogFlag = flag;
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                List mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() > 0) {
                    if (useDialogFlag) {
                        LockRevisionDialog lockDialog = new LockRevisionDialog(QWinFrame.getQWinFrame(), mergedInfoArray, this);
                        lockDialog.setVisible(true);
                    } else {
                        // Don't use a dialog, just get the default revision.
                        LogFileOperationLockRevisionCommandArgs commandArgs = new LogFileOperationLockRevisionCommandArgs();
                        commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                        completeOperation(mergedInfoArray, commandArgs);
                    }
                }
            } catch (Exception e) {
                QWinUtility.logProblem(Level.WARNING, "Caught exception when trying to lock revision: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process the dialog choices.
     * @param mergedInfoArray the list of files to operate on.
     * @param commandArgs the command arguments.
     */
    public void completeOperation(final List mergedInfoArray, final LogFileOperationLockRevisionCommandArgs commandArgs) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Locking revisions from QVCS Archive", mergedInfoArray.size());

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

                        // Don't bother if the file is obsolete.
                        if (mergedInfo.getIsObsolete()) {
                            continue;
                        }

                        // Do not request a lock if the file is in the cemetery.
                        String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Lock request for cemetery file ignored for " + mergedInfo.getShortWorkfileName());
                            continue;
                        }
                        // Do not request a lock if the file is in the branch archives directory.
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Lock request for branch archives file ignored for " + mergedInfo.getShortWorkfileName());
                            continue;
                        }

                        // Update the progress monitor.
                        OperationBaseClass.updateProgressDialog(i, "Locking revision for: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                        String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
                        String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator
                                + mergedInfo.getShortWorkfileName();

                        // The command args
                        LogFileOperationLockRevisionCommandArgs currentCommandArgs = new LogFileOperationLockRevisionCommandArgs();

                        currentCommandArgs.setRevisionString(commandArgs.getRevisionString());
                        currentCommandArgs.setLabel(commandArgs.getLabel());
                        boolean tryToLockFlag = true;

                        if ((commandArgs.getRevisionString() == null) && (commandArgs.getLabel() != null)) {
                            // A lock by label request. Figure out the revision to lock, since the server
                            // doesn't know how to do that (it could, it's just easier to do here).
                            LabelInfo[] labelInfo = mergedInfo.getArchiveInfo().getLogfileInfo().getLogFileHeaderInfo().getLabelInfo();
                            tryToLockFlag = false;
                            for (LabelInfo labelInfo1 : labelInfo) {
                                if (0 == labelInfo1.getLabelString().compareTo(commandArgs.getLabel())) {
                                    tryToLockFlag = true;
                                    currentCommandArgs.setRevisionString(labelInfo1.getLabelRevisionString());
                                    break;
                                }
                            }
                        }

                        if (tryToLockFlag) {
                            currentCommandArgs.setUserName(mergedInfo.getUserName());
                            currentCommandArgs.setFullWorkfileName(mergedInfo.getFullWorkfileName());
                            currentCommandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                            currentCommandArgs.setOutputFileName(fullWorkfileName);
                            if (mergedInfo.lockRevision(currentCommandArgs)) {
                                // Log the request.
                                QWinUtility.logProblem(Level.FINE, "Sent revision lock request for " + fullWorkfileName + " to server.");

                                // Store the checkout comment if there is one.
                                if (getCheckOutComment() != null) {
                                    if (getCheckOutComment().length() > 0) {
                                        CheckOutCommentManager.getInstance().storeComment(mergedInfo, getCheckOutComment());
                                    }
                                }
                            }
                        } else {
                            QWinUtility.logProblem(Level.INFO, "Skipping lock request for " + fullWorkfileName + ". It does not have label '" + commandArgs.getLabel() + "'.");
                        }
                    }
                } catch (QVCSException e) {
                    QWinUtility.logProblem(Level.WARNING, "Caught exception when trying to lock revision: " + e.getClass().toString() + " " + e.getLocalizedMessage());
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

    /**
     * Set the check out comment.
     * @param comment the check out comment.
     */
    public void setCheckOutComment(String comment) {
        this.checkOutComment = comment;
    }

    /**
     * Get the check out comment.
     * @return the check out comment.
     */
    public String getCheckOutComment() {
        return checkOutComment;
    }
}
