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

import com.qumasoft.guitools.StatusBar;
import com.qumasoft.qvcslib.TransactionInProgressListenerInterface;
import javax.swing.SwingUtilities;

/**
 * The status bar for the QWinFrame.
 * @author Jim Voris
 */
public class QWinStatusBar extends StatusBar implements TransactionInProgressListenerInterface {
    private static final long serialVersionUID = 6858254757325119524L;

    private static final int FILE_COUNT_PANE_INDEX = 0;
    private static final int USER_PANE_INDEX = 1;
    private static final int PROJECT_NAME_PANE_INDEX = 2;
    private int fileCount;

    /**
     * Creates new QWinStatusBar.
     * @param panes the strings to populate into the status bar.
     */
    public QWinStatusBar(String[] panes) {
        super(panes);
    }

    void setUserName(String userName) {
        setPaneText(USER_PANE_INDEX, "  User Name: " + userName + "  ");
    }

    String getUserName() {
        return getStatusPanes()[USER_PANE_INDEX].getText();
    }

    void setFileCount(int fileCnt) {
        setFileCount(fileCnt, 0);
    }

    void setFileCount(int fileCnt, int selectedCount) {
        fileCount = fileCnt;
        if (selectedCount == 0) {
            setPaneText(FILE_COUNT_PANE_INDEX, "  File Count: " + fileCnt + "  ");
        } else {
            setPaneText(FILE_COUNT_PANE_INDEX, "  File Count: " + fileCnt + ", Selected:  " + selectedCount + " ");
        }
    }

    int getFileCount() {
        return fileCount;
    }

    void setProjectName(String projectName) {
        setPaneText(PROJECT_NAME_PANE_INDEX, "  Project Name: " + projectName + "  ");
    }

    String getProjectName() {
        return getStatusPanes()[PROJECT_NAME_PANE_INDEX].getText();
    }

    void updateStatusInfo() {
        // Run the update on the Swing thread.
        Runnable fireChange = new Runnable() {

            @Override
            public void run() {
                // Update the file count.
                javax.swing.JTable table = QWinFrame.getQWinFrame().getFileTable();
                FileTableModel fileTableModel = (FileTableModel) table.getModel();
                if (fileTableModel != null) {
                    setFileCount(fileTableModel.getRowCount(), table.getSelectedRowCount());
                } else {
                    setFileCount(0, 0);
                }

                setProjectName(QWinFrame.getQWinFrame().getProjectName());
                setUserName(QWinFrame.getQWinFrame().getLoggedInUserName());
            }
        };
        SwingUtilities.invokeLater(fireChange);
    }

    @Override
    public void setTransactionInProgress(final boolean flag) {
        // Run the update on the Swing thread.
        Runnable update = new Runnable() {

            @Override
            public void run() {
                indicateProgress(flag);
                if (!flag) {
                    updateStatusInfo();
                }
            }
        };
        SwingUtilities.invokeLater(update);
    }
}
