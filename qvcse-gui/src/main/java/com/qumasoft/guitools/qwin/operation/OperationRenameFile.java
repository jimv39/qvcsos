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

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.QWinUtility;
import com.qumasoft.guitools.qwin.RenameWorkfileDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import java.awt.HeadlessException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Rename file operation.
 * @author Jim Voris
 */
public class OperationRenameFile extends OperationBaseClass {

    /**
     * Create a rename file operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    public OperationRenameFile(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            if (getFileTable().getSelectedRowCount() == 1) {
                try {
                    // Get the selected files...
                    List mergedInfoArray = getSelectedFiles();
                    MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoArray.get(0);

                    if (((mergedInfo.getStatusIndex() != MergedInfoInterface.NOT_CONTROLLED_STATUS_INDEX) && (mergedInfo.getLockCount() == 0))
                            || (mergedInfo.getStatusIndex() == MergedInfoInterface.NOT_CONTROLLED_STATUS_INDEX)) {
                        rename(mergedInfo);
                    } else {
                        // Renames of locked files is not allowed.
                        String message = "Rename of locked file is not allowed. Please unlock the file and try again.";
                        JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), message, "Rename not allowed.", JOptionPane.PLAIN_MESSAGE);
                    }
                } catch (HeadlessException e) {
                    QWinUtility.logProblem(Level.WARNING, "OperationRenameFile caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    void rename(final MergedInfoInterface mergedInfo) {
        try {
            // Get the prospective new workfile name.
            final String newShortWorkfileName = getNewShortWorkfileName(mergedInfo);

            // If we have a valid new workfile name...
            if (newShortWorkfileName != null) {
                // See if there is already an archive with the requested name. If there is not, then we need to
                // ask the server to rename the archive...
                Object existingControlledFile = mergedInfo.getArchiveDirManager().getArchiveInfo(newShortWorkfileName);
                if ((existingControlledFile == null)
                        && (mergedInfo.getStatusIndex() != MergedInfoInterface.NOT_CONTROLLED_STATUS_INDEX)) {
                    // Create the rename request to send to the server. If/When
                    // we receive the server response, we will rename the local
                    // workfile at that time (not now).

                    // Run the update on the Swing thread.
                    Runnable later = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                mergedInfo.getArchiveDirManager().renameArchive(mergedInfo.getArchiveDirManager().getUserName(), mergedInfo.getShortWorkfileName(),
                                        newShortWorkfileName, null);
                            } catch (QVCSException | IOException e) {
                                QWinUtility.logProblem(Level.WARNING, "OperationRenameFile caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                                QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                            }
                        }
                    };
                    SwingUtilities.invokeLater(later);
                } else {
                    // The file is not controlled, or there is already an archive with the requested name.
                    // In this case, we'll just rename the local workfile so that it syncs up with the already
                    // existing archive file.
                    String workfileDirectory = mergedInfo.getWorkfile().getParent();
                    WorkFile.renameFile(workfileDirectory, mergedInfo.getShortWorkfileName(), newShortWorkfileName);
                    ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) mergedInfo.getArchiveDirManager();
                    archiveDirManagerProxy.getDirectoryManager().getWorkfileDirectoryManager().refresh();
                    archiveDirManagerProxy.notifyListeners();
                }
            }
        } catch (Exception e) {
            QWinUtility.logProblem(Level.WARNING, "OperationRenameFile caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    private String getNewShortWorkfileName(MergedInfoInterface mergedInfo) {
        RenameWorkfileDialog renameDialog = new RenameWorkfileDialog(QWinFrame.getQWinFrame(), mergedInfo);
        renameDialog.setVisible(true);
        return renameDialog.getNewShortWorkfileName();
    }
}
