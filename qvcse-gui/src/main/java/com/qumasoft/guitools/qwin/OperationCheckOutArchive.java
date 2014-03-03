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
import com.qumasoft.qvcslib.CheckOutCommentManager;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.LogFileOperationCheckOutCommandArgs;
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
 * Checkout archive operation.
 * @author Jim Voris
 */
public class OperationCheckOutArchive extends OperationBaseClass {

    private final boolean useDialogFlag;
    private final OverWriteChecker overWriteChecker;
    private String checkOutComment;

    /**
     * Create a checkout archive operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     * @param flag use the dialog or not.
     */
    OperationCheckOutArchive(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties,
                             boolean flag) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
        useDialogFlag = flag;
        overWriteChecker = new OverWriteChecker();
    }

    @Override
    void executeOperation() {
        if (getFileTable() != null) {
            try {
                List mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() > 0) {
                    if (useDialogFlag) {
                        CheckOutRevisionDialog checkOutDialog = new CheckOutRevisionDialog(QWinFrame.getQWinFrame(), mergedInfoArray, this);
                        checkOutDialog.setVisible(true);
                    } else {
                        // Don't use a dialog, just get the default revision.
                        LogFileOperationCheckOutCommandArgs commandArgs = new LogFileOperationCheckOutCommandArgs();
                        commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                        completeOperation(mergedInfoArray, commandArgs);
                    }
                }
            } catch (Exception e) {
                QWinUtility.logProblem(Level.WARNING, "operationCheckOut caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }
    }

    void completeOperation(final List mergedInfoArray, final LogFileOperationCheckOutCommandArgs commandArgs) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Checking out revisions from QVCS Archive", mergedInfoArray.size());

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

                        // Don't bother unless we have an archive file.
                        if (mergedInfo.getArchiveInfo() == null) {
                            continue;
                        }

                        // Don't bother if the file is obsolete.
                        if (mergedInfo.getIsObsolete()) {
                            continue;
                        }

                        // Do not request a checkout if the file is in the cemetery.
                        String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Checkout request for cemetery file ignored for " + mergedInfo.getShortWorkfileName());
                            continue;
                        }
                        // Do not request a checkout if the file is in the branch archive directory.
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Checkout request for branch archive file ignored for " + mergedInfo.getShortWorkfileName());
                            continue;
                        }

                        int statusIndex = mergedInfo.getStatusIndex();
                        if ((statusIndex == MergedInfoInterface.DIFFERENT_STATUS_INDEX)
                                || (statusIndex == MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX)
                                || (statusIndex == MergedInfoInterface.YOUR_COPY_CHANGED_STATUS_INDEX)) {
                            // Confirm with the user that they really want to overwrite
                            // any changes that there may be...
                            if (!overWriteChecker.overwriteEditedWorkfile(mergedInfo, progressMonitor)) {
                                // Update the progress monitor.
                                OperationBaseClass.updateProgressDialog(i, "Skipping: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);
                                continue;
                            }
                        }

                        // Update the progress monitor.
                        OperationBaseClass.updateProgressDialog(i, "Checking out revision for: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                        if (i == 0) {
                            if (!createWorkfileDirectory(mergedInfo)) {
                                break;
                            }
                        }

                        String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
                        String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator
                                + mergedInfo.getShortWorkfileName();

                        // Create the command args
                        LogFileOperationCheckOutCommandArgs currentCommandArgs = new LogFileOperationCheckOutCommandArgs();
                        currentCommandArgs.setUserName(mergedInfo.getUserName());
                        currentCommandArgs.setRevisionString(commandArgs.getRevisionString());
                        currentCommandArgs.setLabel(commandArgs.getLabel());
                        currentCommandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                        currentCommandArgs.setFullWorkfileName(fullWorkfileName);
                        currentCommandArgs.setOutputFileName(fullWorkfileName);

                        if (mergedInfo.getIsRemote()) {
                            if (mergedInfo.checkOutRevision(currentCommandArgs, fullWorkfileName)) {
                                // Log the success.
                                QWinUtility.logProblem(Level.INFO, "Sent request to check out revision: '" + commandArgs.getRevisionString() + "' for '" + fullWorkfileName
                                        + "' from server.");

                                // Store the checkout comment if there is one.
                                if (getCheckOutComment() != null) {
                                    if (getCheckOutComment().length() > 0) {
                                        CheckOutCommentManager.getInstance().storeComment(mergedInfo, getCheckOutComment());
                                    }
                                }
                            }
                        } else {
                            QWinUtility.logProblem(Level.WARNING, "Local check-out operation not supported!!");
                        }
                    }
                } catch (QVCSException e) {
                    QWinUtility.logProblem(Level.WARNING, "operationCheckOut caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
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

    void setOverwriteAnswerHasBeenCaptured(boolean flag) {
        overWriteChecker.setOverwriteAnswerHasBeenCaptured(flag);
    }

    void setOverwriteAnswer(boolean flag) {
        overWriteChecker.setOverwriteAnswer(flag);
    }

    void setCheckOutComment(String comment) {
        this.checkOutComment = comment;
    }

    String getCheckOutComment() {
        return checkOutComment;
    }
}
