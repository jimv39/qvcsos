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

import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.CheckOutCommentManager;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.KeywordExpansionContext;
import com.qumasoft.qvcslib.KeywordManagerFactory;
import com.qumasoft.qvcslib.KeywordManagerInterface;
import com.qumasoft.qvcslib.LogFileOperationCheckInCommandArgs;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Checkin a new revision.
 * @author Jim Voris
 */
public class OperationCheckInArchive extends OperationBaseClass {

    /**
     * Create a checkin archive operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    OperationCheckInArchive(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    void executeOperation() {
        if (getFileTable() != null) {
            try {
                List<MergedInfoInterface> mergedInfoArray = getSelectedFiles();

                if (mergedInfoArray.size() > 0) {
                    // Make sure that none of the non-binary files require a merge...
                    for (MergedInfoInterface mergedInfo : mergedInfoArray) {
                        if ((mergedInfo.getStatusIndex() == MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX)
                                && (!mergedInfo.getAttributes().getIsBinaryfile())) {
                            final String message = String.format("You must merge your changes to [%s] before checkin!", mergedInfo.getShortWorkfileName());
                            Runnable later = new Runnable() {

                                @Override
                                public void run() {
                                    JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), message, "Merge Required!", JOptionPane.PLAIN_MESSAGE);
                                }
                            };
                            SwingUtilities.invokeLater(later);

                            return;
                        }
                    }

                    CheckInDialog checkInDialog = new CheckInDialog(QWinFrame.getQWinFrame(), mergedInfoArray, QWinFrame.getQWinFrame().getCheckinComments(), false, this);
                    checkInDialog.setVisible(true);
                }
            } catch (Exception e) {
                QWinUtility.logProblem(Level.WARNING, "Caught exception in operationCheckIn: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
                QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }
    }

    void processDialogResult(final List mergedInfoArray, final CheckInDialog checkIn) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Checking in revisions to QVCS Archive", mergedInfoArray.size());

        Runnable worker = new Runnable() {

            @Override
            public void run() {
                TransportProxyInterface transportProxy = null;
                int transactionID = 0;

                try {
                    int size = mergedInfoArray.size();
                    KeywordManagerInterface keywordManager = KeywordManagerFactory.getInstance().getNewKeywordManager();
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

                        // Do not request a checkin if the file is in the cemetery.
                        String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Checkin request for cemetery file ignored for [" + mergedInfo.getShortWorkfileName() + "]");
                            continue;
                        }
                        // Do not request a checkin if the file is in the branch archive directory.
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Checkin request for branch archive directory file ignored for [" + mergedInfo.getShortWorkfileName() + "]");
                            continue;
                        }

                        // Update the progress monitor.
                        OperationBaseClass.updateProgressDialog(i, "Checking in: [" + mergedInfo.getArchiveInfo().getShortWorkfileName() + "]", progressMonitor);

                        String shortWorkfilename = mergedInfo.getShortWorkfileName();
                        String fullWorkfileName = mergedInfo.getWorkfileInfo().getFullWorkfileName();

                        // The command args
                        LogFileOperationCheckInCommandArgs commandArgs = new LogFileOperationCheckInCommandArgs();
                        commandArgs.setUserName(mergedInfo.getUserName());

                        String lockedRevision;
                        if (mergedInfo.getAttributes().getIsCheckLock()) {
                            lockedRevision = mergedInfo.getLockedRevisionString(mergedInfo.getUserName());
                            if (lockedRevision == null) {
                                throw new QVCSException("User [" + mergedInfo.getUserName() + "] has no revisions locked for [" + shortWorkfilename + "]");
                            }
                            commandArgs.setLockedRevisionString(lockedRevision);
                        } else {
                            commandArgs.setLockedRevisionString(mergedInfo.getArchiveInfo().getDefaultRevisionString());
                        }

                        if (checkIn.getChangesDescription().length() > 0) {
                            commandArgs.setCheckInComment(checkIn.getChangesDescription());
                        } else {
                            if (CheckOutCommentManager.getInstance().commentExists(mergedInfo)) {
                                commandArgs.setCheckInComment(CheckOutCommentManager.getInstance().lookupComment(mergedInfo));
                            } else {
                                commandArgs.setCheckInComment("No Comment");
                            }
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
                        commandArgs.setLockFlag(checkIn.getLockFlag());
                        commandArgs.setForceBranchFlag(checkIn.getForceBranchFlag());
                        commandArgs.setApplyLabelFlag(checkIn.getApplyLabelFlag());
                        commandArgs.setFloatLabelFlag(checkIn.getFloatLabelFlag());
                        commandArgs.setReuseLabelFlag(checkIn.getReUseLabelFlag());
                        commandArgs.setCreateNewRevisionIfEqual(checkIn.getCreateNewRevisionIfEqual());
                        commandArgs.setNoExpandKeywordsFlag(checkIn.getNoExpandKeywordsFlag());
                        commandArgs.setProtectWorkfileFlag(checkIn.getProtectWorkfileFlag());

                        // Set some other values
                        commandArgs.setLabel(checkIn.getLabelString());
                        commandArgs.setProjectName(getProjectName());

                        // Contract keywords if needed.
                        String checkInFilename = contractKeywords(checkInFile, mergedInfo, commandArgs, keywordManager);

                        // The checkInFilename will be null if we are not able to read it.
                        if (checkInFilename != null) {
                            if (mergedInfo.checkInRevision(commandArgs, checkInFilename, false)) {
                                // This is where I would log the success to the status pane.
                                QWinUtility.logProblem(Level.INFO, "Sent request to check in: [" + fullWorkfileName + "] to server.");

                                // Remove any checkout comment.
                                if (CheckOutCommentManager.getInstance().commentExists(mergedInfo)) {
                                    CheckOutCommentManager.getInstance().removeComment(mergedInfo);
                                }
                            }
                        }
                    }
                } catch (QVCSException e) {
                    QWinUtility.logProblem(Level.WARNING, "Caught exception in operationCheckIn: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
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

    private String contractKeywords(File checkInFile, MergedInfoInterface mergedInfo, LogFileOperationCheckInCommandArgs commandArgs, KeywordManagerInterface keywordManager) {
        String returnFilename = null;
        try {
            returnFilename = checkInFile.getCanonicalPath();
            ArchiveAttributes attributes = mergedInfo.getAttributes();

            if (attributes.getIsExpandKeywords()) {
                FileInputStream inStream = null;
                FileOutputStream outStream = null;
                try {
                    if (checkInFile.canWrite()) {
                        // We need to contract keywords before creating the new revision.
                        inStream = new FileInputStream(checkInFile);
                        File contractedOutputFile = File.createTempFile("QVCS", "tmp");
                        contractedOutputFile.deleteOnExit();
                        outStream = new FileOutputStream(contractedOutputFile);
                        returnFilename = contractedOutputFile.getCanonicalPath();
                        KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outStream,
                                contractedOutputFile,
                                mergedInfo.getArchiveInfo().getLogfileInfo(),
                                0,
                                "",
                                "",
                                mergedInfo.getArchiveDirManager().getProjectProperties());

                        if (attributes.getIsBinaryfile()) {
                            keywordExpansionContext.setBinaryFileFlag(true);
                            keywordManager.expandKeywords(inStream, keywordExpansionContext);
                        } else {
                            keywordExpansionContext.setBinaryFileFlag(false);
                            AtomicReference<String> checkInComment = new AtomicReference<>();
                            keywordManager.contractKeywords(inStream, outStream, checkInComment, mergedInfo.getArchiveDirManager().getProjectProperties(), false);
                            // Snag any contractions of the Comment keyword.
                            if (checkInComment.get() != null) {
                                String newComment = commandArgs.getCheckInComment() + "; " + checkInComment.get();
                                commandArgs.setCheckInComment(newComment);
                            }
                        }
                    } else {
                        QWinUtility.logProblem(Level.WARNING, "Cannot write '" + returnFilename + "' to expand keywords. Checkin failed.");
                        returnFilename = null;
                    }
                } catch (QVCSException | IOException e) {
                    QWinUtility.logProblem(Level.WARNING, e.getLocalizedMessage());
                    returnFilename = null;
                } finally {
                    try {
                        if (inStream != null) {
                            inStream.close();
                        }
                        if (outStream != null) {
                            outStream.close();
                        }
                    } catch (IOException e) {
                        QWinUtility.logProblem(Level.WARNING, e.getLocalizedMessage());
                    }
                }
            } else {
                if (!checkInFile.canRead()) {
                    QWinUtility.logProblem(Level.WARNING, "Cannot read [" + returnFilename + "]. Checkin failed.");
                    returnFilename = null;
                }
            }
        } catch (IOException e) {
            QWinUtility.logProblem(Level.WARNING, e.getLocalizedMessage());
        }
        return returnFilename;
    }
}
