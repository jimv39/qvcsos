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

import com.qumasoft.guitools.qwin.OverWriteChecker;
import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.logMessage;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.GetRevisionDialog;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * @param branchName the branch name.
     * @param remoteProperties user location properties.
     * @param flag use the dialog or not.
     */
    public OperationGet(JTable fileTable, final String serverName, final String projectName, final String branchName, RemotePropertiesBaseClass remoteProperties, boolean flag) {
        super(fileTable, serverName, projectName, branchName, remoteProperties);
        useDialogFlag = flag;
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                List<MergedInfoInterface> mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() > 0) {
                    if (useDialogFlag) {
                        GetRevisionDialog getDialog = new GetRevisionDialog(QWinFrame.getQWinFrame(), mergedInfoArray, this);
                        getDialog.setVisible(true);
                    } else {
                        // Don't use a dialog, just get the default revision.
                        GetRevisionCommandArgs commandArgs = new GetRevisionCommandArgs();
                        commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
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
     * @param mergedInfoArray the list of files to operation on.
     * @param commandArgs the command line arguments.
     */
    public void completeOperation(final List<MergedInfoInterface> mergedInfoArray, final GetRevisionCommandArgs commandArgs) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Getting revisions from QVCS Archive", mergedInfoArray.size());

        Runnable worker = () -> {
            TransportProxyInterface transportProxy = null;

            try {
                int size = mergedInfoArray.size();
                for (int i = 0; i < size; i++) {
                    if (progressMonitor.getIsCancelled()) {
                        break;
                    }

                    MergedInfoInterface mergedInfo = mergedInfoArray.get(i);

                    if (i == 0) {
                        ArchiveDirManagerInterface archiveDirManager = mergedInfo.getArchiveDirManager();
                        ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) archiveDirManager;
                        transportProxy = archiveDirManagerProxy.getTransportProxy();
                        ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy, getTransactionID());
                    }

                    if (mergedInfo.getArchiveInfo() == null) {
                        continue;
                    }

                    String workfileBase = getRemoteProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
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
                    GetRevisionCommandArgs currentCommandArgs = new GetRevisionCommandArgs();
                    currentCommandArgs.setFullWorkfileName(fullWorkfileName);
                    currentCommandArgs.setOutputFileName(fullWorkfileName);
                    currentCommandArgs.setRevisionString(commandArgs.getRevisionString());
                    currentCommandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                    currentCommandArgs.setUserName(mergedInfo.getUserName());
                    currentCommandArgs.setOverwriteBehavior(Utility.OverwriteBehavior.REPLACE_WRITABLE_FILE);
                    currentCommandArgs.setTimestampBehavior(commandArgs.getTimestampBehavior());

                    if (mergedInfo.getIsRemote()) {
                        if (mergedInfo.getRevision(currentCommandArgs, fullWorkfileName)) {
                            // Log the success.
                            logMessage("Sent request to get revision [" + commandArgs.getRevisionString() + "] for ["
                                    + fullWorkfileName + "] from server.");
                        }
                    } else {
                        warnProblem("Local get operation not supported!!");
                    }
                }
            } catch (QVCSException e) {
                warnProblem("operationGet caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            } finally {
                progressMonitor.close();
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, getTransactionID());
                setWorkCompleted();
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
