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

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.TimerManager;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.awt.Rectangle;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Show in containing directory operation.
 * @author Jim Voris
 */
public class OperationShowInContainingDirectory extends OperationBaseClass {

    /**
     * Create a show in containing directory operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    public OperationShowInContainingDirectory(JTable fileTable, final String serverName, final String projectName, final String viewName,
                                              UserLocationProperties userLocationProperties) {
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
                    showContainingDirectory(mergedInfo);
                } catch (Exception e) {
                    QWinUtility.logProblem(Level.WARNING, "OperationShowInContainingDirectory caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    private void showContainingDirectory(final MergedInfoInterface mergedInfo) {
        // Put this on the swing thread.
        Runnable worker = new Runnable() {

            @Override
            public void run() {
                try {
                    // Navigate to the containing directory...
                    final String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                    final String projectName = mergedInfo.getArchiveDirManager().getProjectName();
                    final String serverName = QWinFrame.getQWinFrame().getServerName();
                    final String viewName = mergedInfo.getArchiveDirManager().getViewName();
                    DefaultMutableTreeNode directoryNode = QWinFrame.getQWinFrame().getTreeModel().findContainingDirectoryTreeNode(serverName, projectName, viewName, appendedPath);
                    ProjectTreeControl.getInstance().selectNode(directoryNode);
                    QWinFrame.getQWinFrame().getRightFilePane().getModel().fireTableDataChanged();

                    // And re-select the file that had been selected.  We have
                    // to delay the work here so that it occurs after the delayed
                    // refresh... so we queue this to run after the refresh should
                    // have happened.
                    TimerTask notifyTask = new TimerTask() {

                        @Override
                        public void run() {
                            // Run this on the swing thread.
                            Runnable swingTask = new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        // And select the item in the file list.
                                        AbstractFileTableModel abstractFileTableModel = QWinFrame.getQWinFrame().getRightFilePane().getModel();
                                        int rowCount = abstractFileTableModel.getRowCount();
                                        int fileIndex = 0;
                                        for (int i = 0; i < rowCount; i++) {
                                            MergedInfoInterface rowMergedInfo = abstractFileTableModel.getMergedInfo(i);
                                            if ((0 == rowMergedInfo.getShortWorkfileName().compareTo(mergedInfo.getShortWorkfileName()))
                                                    && (0 == rowMergedInfo.getArchiveDirManager().getAppendedPath().compareTo(mergedInfo.getArchiveDirManager()
                                                            .getAppendedPath()))) {
                                                fileIndex = i;
                                                break;
                                            }
                                        }
                                        QWinFrame.getQWinFrame().getFileTable().getSelectionModel().setSelectionInterval(fileIndex, fileIndex);

                                        // Now we need to make sure the file is actually visible...
                                        JTable fileTable = QWinFrame.getQWinFrame().getFileTable();
                                        Rectangle visibleRectangle = fileTable.getVisibleRect();
                                        Rectangle selectedRectangle = fileTable.getCellRect(fileIndex, 0, true);

                                        // Test to see if the selected file is visible.
                                        if ((selectedRectangle.y > (visibleRectangle.y + visibleRectangle.height))
                                                || (selectedRectangle.y < visibleRectangle.y)) {
                                            int centerY = visibleRectangle.y + visibleRectangle.height / 2;
                                            if (centerY < selectedRectangle.y) {
                                                // Need to scroll up
                                                selectedRectangle.y = selectedRectangle.y - visibleRectangle.y + centerY;
                                            } else {
                                                // Need to scroll down
                                                selectedRectangle.y = selectedRectangle.y + visibleRectangle.y - centerY;
                                            }
                                            fileTable.scrollRectToVisible(selectedRectangle);
                                        }
                                    } catch (Exception e) {
                                        QWinUtility.logProblem(Level.WARNING, "OperationShowInContainingDirectory timer task caught exception: " + e.getClass().toString()
                                                + " " + e.getLocalizedMessage());
                                        QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                                    }
                                }
                            };
                            SwingUtilities.invokeLater(swingTask);
                        }
                    };
                    TimerManager.getInstance().getTimer().schedule(notifyTask, QWinFrame.REFRESH_DELAY + QWinFrame.REFRESH_DELAY / 2);
                } catch (Exception e) {
                    QWinUtility.logProblem(Level.WARNING, "OperationShowInContainingDirectory worker caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        };

        // Put all this on the swing thread.
        SwingUtilities.invokeLater(worker);
    }
}
