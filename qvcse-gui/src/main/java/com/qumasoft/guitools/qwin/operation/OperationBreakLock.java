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

import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import java.io.File;
import java.util.List;
import javax.swing.JTable;

/**
 * Break lock operation.
 * @author Jim Voris
 */
public class OperationBreakLock extends OperationBaseClass {

    /**
     * Create a break lock operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     */
    public OperationBreakLock(JTable fileTable, final String serverName, final String projectName, final String branchName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() == null) {
            return;
        }

        final List mergedInfoArray = getSelectedFiles();

        if (mergedInfoArray.size() >= 1) {
            // Display the progress dialog.
            final ProgressDialog progressMonitor = createProgressDialog("Breaking locks for QVCS Archive", mergedInfoArray.size());

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

                        if (mergedInfo.getArchiveInfo() == null) {
                            continue;
                        }

                        if (mergedInfo.getArchiveInfo().getLockCount() == 0) {
                            continue;
                        }

                        // We will only break one lock at a time.
                        String[] lockers = mergedInfo.getArchiveInfo().getLockedByString().split(",");
                        String lockedRevision = mergedInfo.getArchiveInfo().getLockedRevisionString(lockers[0]);

                        // Update the progress monitor.
                        OperationBaseClass.updateProgressDialog(i, "Break lock for: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                        String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
                        String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator
                                + mergedInfo.getShortWorkfileName();

                        // Create the command args
                        UnlockRevisionCommandArgs commandArgs = new UnlockRevisionCommandArgs();
                        commandArgs.setUserName(mergedInfo.getUserName());
                        commandArgs.setRevisionString(lockedRevision);
                        commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                        commandArgs.setFullWorkfileName(fullWorkfileName);
                        commandArgs.setOutputFileName(fullWorkfileName);

                        if (mergedInfo.getIsRemote()) {
                            if (mergedInfo.breakLock(commandArgs)) {
                                // Log the success.
                                logProblem("Requested break lock of revision " + commandArgs.getRevisionString() + " for " + fullWorkfileName
                                        + " from server.");
                            }
                        } else {
                            warnProblem("Local break lock operation not supported!!");
                        }
                    }
                } catch (QVCSException e) {
                    warnProblem("operationBreakLock caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    warnProblem(Utility.expandStackTraceToString(e));
                } finally {
                    progressMonitor.close();
                    ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                }
            };

            // Put all this on a separate worker thread.
            new Thread(worker).start();
        }
    }
}
