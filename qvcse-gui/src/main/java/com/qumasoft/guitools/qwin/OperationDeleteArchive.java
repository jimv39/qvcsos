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
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Delete archive operation.
 * @author Jim Voris
 */
class OperationDeleteArchive extends OperationBaseClass {

    /**
     * Create a delete archive operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    OperationDeleteArchive(JTable fileTable, String serverName, String projectName, String viewName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    void executeOperation() {
        // Mark the file obsolete.
        if (getFileTable() != null) {
            if (getFileTable().getSelectedRowCount() >= 1) {
                // Get the selected files...
                final List mergedInfoArray = getSelectedFiles();

                // Run the update on the Swing thread.
                Runnable later = new Runnable() {

                    @Override
                    public void run() {
                        TransportProxyInterface transportProxy = null;
                        int transactionID = 0;

                        // Ask the user if they really want to delete the file.
                        int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Delete the selected file(s)?", "Delete selected files", JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                        if (answer == JOptionPane.YES_OPTION) {
                            int workfileDeleteAnswer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Delete the associated workfiles also (if they exist)?",
                                    "Delete associated workfiles files", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                            Iterator it = mergedInfoArray.iterator();
                            boolean modelChanged = false;
                            int counter = 0;

                            while (it.hasNext()) {
                                try {
                                    MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();

                                    // We need to wrap this in a transaction.
                                    if (counter == 0) {
                                        ArchiveDirManagerInterface archiveDirManager = mergedInfo.getArchiveDirManager();
                                        ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) archiveDirManager;
                                        transportProxy = archiveDirManagerProxy.getTransportProxy();
                                        transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                                    }

                                    // Mark the file obsolete.
                                    if (mergedInfo.getLockCount() == 0) {
                                        if (mergedInfo.setIsObsolete(mergedInfo.getUserName(), true)) {
                                            // Log the success.
                                            QWinUtility.logProblem(Level.INFO, "Sent request that archive for '" + mergedInfo.getShortWorkfileName() + "' be marked obsolete.");
                                        }

                                        // Delete the workfile also.
                                        if ((workfileDeleteAnswer == JOptionPane.YES_OPTION) && (mergedInfo.getWorkfileExists())) {
                                            if (mergedInfo.getWorkfileInfo().getWorkfile().delete()) {
                                                modelChanged = true;
                                                WorkfileDigestManager.getInstance().removeWorkfileDigest(mergedInfo.getWorkfileInfo());
                                                QWinUtility.logProblem(Level.INFO, "Deleted workfile: " + mergedInfo.getWorkfileInfo().getFullWorkfileName());
                                            }
                                        }
                                    } else {
                                        QWinUtility.logProblem(Level.WARNING, "Failed to delete " + mergedInfo.getFullWorkfileName() + " . Cannot delete a file that is locked");
                                    }
                                } catch (QVCSException e) {
                                    QWinUtility.logProblem(Level.WARNING, "OperationDeleteArchive caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                                }
                                counter++;
                            }
                            if (modelChanged) {
                                QWinFrame.getQWinFrame().refreshCurrentView();
                            }

                            ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                        }
                    }
                };
                SwingUtilities.invokeLater(later);
            }
        }
    }
}
