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
import static com.qumasoft.guitools.qwin.QWinUtility.logMessage;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.CheckInDialog;
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
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Check in a new revision.
 * @author Jim Voris
 */
public class OperationCheckInArchive extends OperationBaseClass {

    /**
     * Create a checkin archive operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     */
    public OperationCheckInArchive(JTable fileTable, final String serverName, final String projectName, final String branchName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                List<MergedInfoInterface> mergedInfoArray = getSelectedFiles();

                if (mergedInfoArray.size() > 0) {
                    // Make sure that none of the non-binary files require a merge...
                    for (MergedInfoInterface mergedInfo : mergedInfoArray) {
                        if ((mergedInfo.getStatusIndex() == MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX)
                                && (!mergedInfo.getAttributes().getIsBinaryfile())) {
                            final String message = String.format("You must merge your changes to [%s] before checkin!", mergedInfo.getShortWorkfileName());
                            Runnable later = () -> {
                                JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), message, "Merge Required!", JOptionPane.PLAIN_MESSAGE);
                            };
                            SwingUtilities.invokeLater(later);

                            return;
                        }
                    }
                    // Ask for the latest commit comments.
                    List<String> commitCommentsList = QWinFrame.getQWinFrame().getCheckinComments();

                    CheckInDialog checkInDialog = new CheckInDialog(QWinFrame.getQWinFrame(), mergedInfoArray, commitCommentsList, false, this);
                    checkInDialog.setVisible(true);
                }
            } catch (Exception e) {
                warnProblem("Caught exception in operationCheckIn: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process the dialog choices.
     * @param mergedInfoArray the array of files to operate on.
     * @param checkIn the dialog from which we may get additional information.
     */
    public void processDialogResult(final List<MergedInfoInterface> mergedInfoArray, final CheckInDialog checkIn) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Checking in revisions to QVCS Archive", mergedInfoArray.size());

        Runnable worker = () -> {
            TransportProxyInterface transportProxy = null;
            int transactionID = 0;

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
                        transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                    }

                    // Don't bother unless we have an archive file.
                    if (mergedInfo.getArchiveInfo() == null) {
                        continue;
                    }

                    // Do not request a checkin if the file is in the cemetery.
                    String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                    if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                        logMessage("Checkin request for cemetery file ignored for [" + mergedInfo.getShortWorkfileName() + "]");
                        continue;
                    }
                    // Do not request a checkin if the file is in the branch archive directory.
                    if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                        logMessage("Checkin request for branch archive directory file ignored for [" + mergedInfo.getShortWorkfileName() + "]");
                        continue;
                    }

                    // Update the progress monitor.
                    OperationBaseClass.updateProgressDialog(i, "Checking in: [" + mergedInfo.getArchiveInfo().getShortWorkfileName() + "]", progressMonitor);

                    String fullWorkfileName = mergedInfo.getWorkfileInfo().getFullWorkfileName();

                    // The command args
                    CheckInCommandArgs commandArgs = new CheckInCommandArgs();
                    commandArgs.setUserName(mergedInfo.getUserName());

                    if (checkIn.getChangesDescription().length() > 0) {
                        commandArgs.setCheckInComment(checkIn.getChangesDescription());
                    } else {
                        commandArgs.setCheckInComment("No Comment");
                    }

                    // Create a File associated with the File we check in
                    File checkInFile = mergedInfo.getWorkfile();

                    // If the user defined an alternate checkin file, we need
                    // to use that instead...
                    if ((mergedInfoArray.size() == 1) && (checkIn.getWorkfileLocation().length() > 0)) {
                        checkInFile = new File(checkIn.getWorkfileLocation());
                    }

                    commandArgs.setInputfileTimeStamp(new Date(checkInFile.lastModified()));
                    commandArgs.setFullWorkfileName(mergedInfo.getFullWorkfileName());
                    commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());

                    // Set flags;
                    commandArgs.setCreateNewRevisionIfEqual(checkIn.getCreateNewRevisionIfEqual());
                    commandArgs.setNoExpandKeywordsFlag(checkIn.getNoExpandKeywordsFlag());
                    commandArgs.setProtectWorkfileFlag(checkIn.getProtectWorkfileFlag());

                    // Set some other values
                    commandArgs.setProjectName(getProjectName());
                    commandArgs.setBranchName(getBranchName());

                    String checkInFilename;
                    try {
                        checkInFilename = checkInFile.getCanonicalPath();
                    } catch (IOException e) {
                        logMessage(Utility.expandStackTraceToString(e));
                        checkInFilename = null;
                    }

                    // The checkInFilename will be null if we are not able to read it.
                    if (checkInFilename != null) {
                        if (mergedInfo.checkInRevision(commandArgs, checkInFilename, false)) {
                            // This is where I would log the success to the status pane.
                            logMessage("Sent request to check in: [" + fullWorkfileName + "] to server.");
                        }
                    }
                }
            } catch (QVCSException e) {
                warnProblem("Caught exception in operationCheckIn: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
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
