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

import com.qumasoft.guitools.qwin.dialog.GetRevisionDialog;
import com.qumasoft.guitools.qwin.OverWriteChecker;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.QWinUtility;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.LogFileOperationGetRevisionCommandArgs;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JTable;

/**
 * Get a file operation.
 * @author Jim Voris
 */
public class OperationGet extends OperationBaseClass {

    private final boolean useDialogFlag;
    private int transactionID = -1;
    private final Map<Integer, OverWriteChecker> overWriteCheckerMap = new HashMap<>();

    /**
     * Create a get operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     * @param flag use the dialog or not.
     */
    public OperationGet(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties, boolean flag) {
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
                        GetRevisionDialog getDialog = new GetRevisionDialog(QWinFrame.getQWinFrame(), mergedInfoArray, this);
                        getDialog.setVisible(true);
                    } else {
                        // Don't use a dialog, just get the default revision.
                        LogFileOperationGetRevisionCommandArgs commandArgs = new LogFileOperationGetRevisionCommandArgs();
                        commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                        commandArgs.setByDateFlag(false);
                        commandArgs.setByLabelFlag(false);
                        completeOperation(mergedInfoArray, commandArgs);
                    }
                }
            } catch (Exception e) {
                QWinUtility.logProblem(Level.WARNING, "operationGet caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process the dialog choices.
     * @param mergedInfoArray the list of files to operation on.
     * @param commandArgs the command line arguments.
     */
    public void completeOperation(final List mergedInfoArray, final LogFileOperationGetRevisionCommandArgs commandArgs) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Getting revisions from QVCS Archive", mergedInfoArray.size());

        Runnable worker = new Runnable() {

            @Override
            public void run() {
                TransportProxyInterface transportProxy = null;

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
                            ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy, getTransactionID());
                        }

                        if (mergedInfo.getArchiveInfo() == null) {
                            continue;
                        }

                        // Do not request a get if the file is in the cemetery.
                        String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Get request for cemetery file ignored for " + mergedInfo.getShortWorkfileName());
                            continue;
                        }
                        // Do not request a get if the file is in the branch archives directory.
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Get request for branch archives file ignored for " + mergedInfo.getShortWorkfileName());
                            continue;
                        }

                        String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
                        String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator
                                + mergedInfo.getShortWorkfileName();

                        // Confirm with the user that they really want to overwrite
                        // any changes that there may be...
                        OverWriteChecker overWriteChecker = overWriteCheckerMap.get(getTransactionID());
                        WorkFile workFile = new WorkFile(fullWorkfileName);
                        if (workFile.exists() && workFile.canWrite()) {
                            if (!overWriteChecker.overwriteEditedWorkfile(mergedInfo, progressMonitor)) {
                                // Update the progress monitor.
                                OperationBaseClass.updateProgressDialog(i, "Skipping: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);
                                continue;
                            }
                        }

                        // Update the progress monitor.
                        OperationBaseClass.updateProgressDialog(i, "Getting revision for: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                        // If the workfile does not yet exist, make sure that the
                        // workfile directory exists...
                        if (mergedInfo.getWorkfileInfo() == null) {
                            if (!createWorkfileDirectory(mergedInfo)) {
                                break;
                            }
                        }

                        // Figure out the command line arguments for this file.
                        LogFileOperationGetRevisionCommandArgs currentCommandArgs = new LogFileOperationGetRevisionCommandArgs();
                        currentCommandArgs.setFullWorkfileName(fullWorkfileName);
                        currentCommandArgs.setLabel(commandArgs.getLabel());
                        currentCommandArgs.setOutputFileName(fullWorkfileName);
                        currentCommandArgs.setRevisionString(commandArgs.getRevisionString());
                        currentCommandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                        currentCommandArgs.setUserName(mergedInfo.getUserName());
                        currentCommandArgs.setOverwriteBehavior(Utility.OverwriteBehavior.REPLACE_WRITABLE_FILE);
                        currentCommandArgs.setTimestampBehavior(commandArgs.getTimestampBehavior());
                        currentCommandArgs.setByDateFlag(commandArgs.getByDateFlag());
                        currentCommandArgs.setByLabelFlag(commandArgs.getByLabelFlag());
                        currentCommandArgs.setByDateValue(commandArgs.getByDateValue());

                        if (mergedInfo.getIsRemote()) {
                            if (mergedInfo.getRevision(currentCommandArgs, fullWorkfileName)) {
                                // Log the success.
                                if ((commandArgs.getLabel() != null) && (commandArgs.getLabel().length() > 0)) {
                                    QWinUtility.logProblem(Level.INFO, "Sent request to get revision associated with label [" + commandArgs.getLabel() + "] for ["
                                            + fullWorkfileName + "] from server.");
                                } else {
                                    QWinUtility.logProblem(Level.INFO, "Sent request to get revision [" + commandArgs.getRevisionString() + "] for ["
                                            + fullWorkfileName + "] from server.");
                                }
                            }
                        } else {
                            QWinUtility.logProblem(Level.WARNING, "Local get operation not supported!!");
                        }
                    }
                } catch (QVCSException e) {
                    QWinUtility.logProblem(Level.WARNING, "operationGet caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                } finally {
                    progressMonitor.close();
                    ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, getTransactionID());
                }
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }

    private int getTransactionID() {
        if (transactionID == -1) {
            transactionID = ClientTransactionManager.getInstance().createTransactionIdentifier(getServerName());
            overWriteCheckerMap.put(transactionID, new OverWriteChecker());
        }
        return transactionID;
    }

    /**
     * Set the overwrite answer has been captured flag.
     * @param flag the overwrite answer has been captured flag.
     */
    public void setOverwriteAnswerHasBeenCaptured(boolean flag) {
        OverWriteChecker overWriteChecker = overWriteCheckerMap.get(getTransactionID());
        overWriteChecker.setOverwriteAnswerHasBeenCaptured(flag);
    }

    /**
     * Set the overwrite answer flag.
     * @param flag the overwrite answer flag.
     */
    public void setOverwriteAnswer(boolean flag) {
        OverWriteChecker overWriteChecker = overWriteCheckerMap.get(getTransactionID());
        overWriteChecker.setOverwriteAnswer(flag);
    }
}
