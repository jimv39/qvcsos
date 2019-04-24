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
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Delete archive operation.
 * @author Jim Voris
 */
public class OperationDeleteArchive extends OperationBaseClass {

    /**
     * Create a delete archive operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    public OperationDeleteArchive(JTable fileTable, String serverName, String projectName, String viewName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        // Mark the file obsolete.
        if (getFileTable() != null) {
            if (getFileTable().getSelectedRowCount() >= 1) {
                // Get the selected files...
                final List<MergedInfoInterface> mergedInfoArray = getSelectedFiles();

                // Run the update on the Swing thread.
                Runnable later = () -> {
                    TransportProxyInterface transportProxy = null;
                    int transactionID = 0;

                    // Ask the user if they really want to delete the file.
                    int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Delete the selected file(s)?", "Delete selected files", JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        int workfileDeleteAnswer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Delete the associated workfiles also (if they exist)?",
                                "Delete associated workfiles files", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        boolean modelChanged = false;
                        int counter = 0;

                        for (MergedInfoInterface mergedInfo : mergedInfoArray) {
                            try {

                                // We need to wrap this in a transaction.
                                if (counter == 0) {
                                    ArchiveDirManagerInterface archiveDirManager = mergedInfo.getArchiveDirManager();
                                    ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) archiveDirManager;
                                    transportProxy = archiveDirManagerProxy.getTransportProxy();
                                    transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                                }

                                // Move the archive to the cemetery.
                                if (mergedInfo.getLockCount() == 0) {
                                    if (mergedInfo.deleteArchive(mergedInfo.getUserName())) {
                                        // Log the success.
                                        logProblem("Sent request that archive for [" + mergedInfo.getShortWorkfileName() + "] be marked obsolete.");
                                    }

                                    // Delete the workfile also.
                                    if ((workfileDeleteAnswer == JOptionPane.YES_OPTION) && (mergedInfo.getWorkfileExists())) {
                                        if (mergedInfo.getWorkfileInfo().getWorkfile().delete()) {
                                            modelChanged = true;
                                            WorkfileDigestManager.getInstance().removeWorkfileDigest(mergedInfo.getWorkfileInfo());
                                            logProblem("Deleted workfile: " + mergedInfo.getWorkfileInfo().getFullWorkfileName());
                                        }
                                    }
                                } else {
                                    warnProblem("Failed to delete " + mergedInfo.getFullWorkfileName() + " . Cannot delete a file that is locked");
                                }
                            } catch (QVCSException e) {
                                warnProblem("OperationDeleteArchive caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                                warnProblem(Utility.expandStackTraceToString(e));
                            }
                            counter++;
                        }
                        if (modelChanged) {
                            QWinFrame.getQWinFrame().refreshCurrentView();
                        }

                        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                    }
                };
                SwingUtilities.invokeLater(later);
            }
        }
    }
}
