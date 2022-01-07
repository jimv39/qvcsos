/*   Copyright 2004-2021 Jim Voris
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
import static com.qumasoft.guitools.qwin.QWinUtility.logMessage;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.AddFileDialog;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import java.io.IOException;
import java.util.List;
import javax.swing.JTable;

/**
 * Create an archive file; populate with first revision.
 * @author Jim Voris
 */
public class OperationCreateArchive extends OperationBaseClass {

    private final boolean useDialogFlag;

    /**
     * Create a create archive operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     * @param flag use the dialog or not.
     */
    public OperationCreateArchive(JTable fileTable, final String serverName, final String projectName, final String branchName, UserLocationProperties userLocationProperties,
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
                        AddFileDialog addFileDialog = new AddFileDialog(QWinFrame.getQWinFrame(), mergedInfoArray, QWinFrame.getQWinFrame().getCheckinComments(), this);
                        addFileDialog.setVisible(true);
                    } else {
                        // Don't use a dialog.  Just create the archive file.
                        CreateArchiveCommandArgs commandArgs = new CreateArchiveCommandArgs();
                        commandArgs.setArchiveDescription("");
                        commandArgs.setCommentPrefix("");
                        completeOperation(mergedInfoArray, commandArgs);
                    }
                }
            } catch (Exception e) {
                warnProblem("Error creating archive. Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process the dialog choices.
     * @param mergedInfoArray the list of files to operate on.
     * @param commandArgs the command arguments.
     */
    public void completeOperation(final List mergedInfoArray, final CreateArchiveCommandArgs commandArgs) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Creating QVCS archives ", mergedInfoArray.size());

        Runnable worker = () -> {
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

                    if (mergedInfo.getWorkfileInfo() == null) {
                        logMessage("Workfile does not exist: " + mergedInfo.getShortWorkfileName());
                        continue;
                    }

                    // Do not request an add if the file is in the cemetery.
                    String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                    if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                        logMessage("Create archive request for cemetery file ignored for " + mergedInfo.getShortWorkfileName());
                        continue;
                    }
                    // Do not request an add if the file is in branch archives directory.
                    if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                        logMessage("Create archive request for branch archives directory file ignored for " + mergedInfo.getShortWorkfileName());
                        continue;
                    }

                    // Update the progress monitor.
                    OperationBaseClass.updateProgressDialog(i, "Creating archive for: " + mergedInfo.getShortWorkfileName(), progressMonitor);

                    String fullWorkfileName = mergedInfo.getWorkfileInfo().getFullWorkfileName();

                    // It only makes sense to create an archive if one doesn't already exist
                    if (mergedInfo.getArchiveInfo() == null) {
                        // The command args
                        CreateArchiveCommandArgs currentCommandArgs = new CreateArchiveCommandArgs();
                        currentCommandArgs.setArchiveDescription(commandArgs.getArchiveDescription());
                        currentCommandArgs.setCommentPrefix(commandArgs.getCommentPrefix());
                        currentCommandArgs.setAttributes(commandArgs.getAttributes());

                        currentCommandArgs.setInputfileTimeStamp(mergedInfo.getWorkfileInfo().getWorkfileLastChangedDate());
                        currentCommandArgs.setUserName(mergedInfo.getUserName());
                        currentCommandArgs.setWorkfileName(fullWorkfileName);

                        if (mergedInfo.getIsRemote()) {
                            if (mergedInfo.getArchiveDirManager().createArchive(currentCommandArgs, fullWorkfileName, null)) {
                                // This is where I would log the success to the status pane.
                                logMessage(mergedInfo.getUserName() + " requested creation of archive for '" + fullWorkfileName + "' on server.");
                            }
                        } else {
                            warnProblem("Local create archive operation not supported!!");
                        }
                    }
                }
            } catch (QVCSException | IOException e) {
                warnProblem("Caught exception in operationAdd: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            } finally {
                progressMonitor.close();
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }
}
