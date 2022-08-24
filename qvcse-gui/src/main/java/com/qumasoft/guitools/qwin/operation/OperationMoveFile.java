/*
 * Copyright 2021 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.MoveFileDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestMoveFileData;
import java.awt.HeadlessException;
import java.io.File;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jim Voris
 */
public class OperationMoveFile extends OperationBaseClass {
    private boolean okFlag = false;

    public OperationMoveFile(JTable ft, String server, String project, String branch, UserLocationProperties userLocationProps) {
        super(ft, server, project, branch, userLocationProps);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            if (getFileTable().getSelectedRowCount() == 1) {
                try {
                    // Get the selected files.
                    List mergedInfoArray = getSelectedFiles();
                    MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoArray.get(0);

                    if (((mergedInfo.getStatusIndex() != MergedInfoInterface.NOT_CONTROLLED_STATUS_INDEX))
                            || (mergedInfo.getStatusIndex() == MergedInfoInterface.NOT_CONTROLLED_STATUS_INDEX)) {
                        moveFile(mergedInfo);
                    }
                } catch (HeadlessException e) {
                    warnProblem("OperationRenameFile caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    warnProblem(Utility.expandStackTraceToString(e));
                }
            }
        }
    }
    void moveFile(final MergedInfoInterface mergedInfo) {
        try {
            // Get the prospective new workfile name.
            final String newAppendedPath = getNewAppendedPath(mergedInfo);
            if (okFlag) {
                String workfileBaseDirectory = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
                String oldAppendedPath;
                if (mergedInfo.getArchiveDirManager() != null) {
                    oldAppendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                } else {
                    File workfile = mergedInfo.getWorkfileInfo().getWorkfile();
                    oldAppendedPath = workfile.getCanonicalPath().substring(workfileBaseDirectory.length(), workfile.getCanonicalPath().length());
                }
                if (0 == oldAppendedPath.compareTo(newAppendedPath)) {
                    warnProblem("You cannot move a file to the same directory.");
                    Runnable later = () -> {
                        // Verify that the user wants to move here...
                        String message = "You cannot move a file to the same directory.";
                        JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), message, "Move Not Allowed", JOptionPane.INFORMATION_MESSAGE);
                    };
                    SwingUtilities.invokeLater(later);
                } else {
                    warnProblem("Moving file to " + newAppendedPath);
                    Runnable later = () -> {
                        TransportProxyInterface transportProxy = null;
                        int transactionID = 0;

                        // Verify that the user wants to drop here...
                        int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Do you want to move " + mergedInfo.getShortWorkfileName() + " to this directory?\n"
                                + newAppendedPath, "Confirm File Move", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (answer == JOptionPane.YES_OPTION) {
                            ClientRequestMoveFileData clientRequestMoveFileData = new ClientRequestMoveFileData();
                            clientRequestMoveFileData.setOriginalAppendedPath(oldAppendedPath);
                            clientRequestMoveFileData.setProjectName(getProjectName());
                            clientRequestMoveFileData.setBranchName(getBranchName());
                            clientRequestMoveFileData.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                            clientRequestMoveFileData.setNewAppendedPath(newAppendedPath);

                            try {
                                ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) mergedInfo.getArchiveDirManager();
                                transportProxy = archiveDirManagerProxy.getTransportProxy();
                                transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                                SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestMoveFileData);
                            } finally {
                                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                            }
                        }
                    };
                    SwingUtilities.invokeLater(later);
                }
            }
        } catch (Exception e) {
            warnProblem("OperationMoveFile caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            warnProblem(Utility.expandStackTraceToString(e));
        }
    }

    private String getNewAppendedPath(MergedInfoInterface mergedInfo) {
        MoveFileDialog moveFileDialog = new MoveFileDialog(QWinFrame.getQWinFrame(), getFileTable().getModel(), mergedInfo, getServerName(), getProjectName(), getBranchName());
        moveFileDialog.setVisible(true);
        okFlag = moveFileDialog.getIsOKFlag();
        return moveFileDialog.getNewAppendedPath();
    }
}
