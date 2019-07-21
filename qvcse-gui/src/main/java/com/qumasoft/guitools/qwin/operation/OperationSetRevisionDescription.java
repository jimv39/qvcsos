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
import static com.qumasoft.guitools.qwin.QWinUtility.traceProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.SetRevisionDescriptionDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import java.io.File;
import java.util.List;
import javax.swing.JTable;

/**
 * Set revision description operation.
 * @author Jim Voris
 */
public class OperationSetRevisionDescription extends OperationBaseClass {

    /**
     * Create a set revision description operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     */
    public OperationSetRevisionDescription(JTable fileTable, final String serverName, final String projectName, final String branchName,
                                           UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                List mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() == 1) {
                    SetRevisionDescriptionDialog setRevisionDescriptionDialog = new SetRevisionDescriptionDialog(QWinFrame.getQWinFrame(), mergedInfoArray,
                            QWinFrame.getQWinFrame().getCheckinComments(), this);
                    setRevisionDescriptionDialog.setVisible(true);
                }
            } catch (Exception e) {
                warnProblem("Caught exception in OperationSetRevisionDescription: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process the dialog choices.
     * @param mergedInfoArray the list of files to operate on.
     * @param setRevisionDescriptionDialog the dialog that has user choice information.
     */
    public void processDialogResult(List mergedInfoArray, SetRevisionDescriptionDialog setRevisionDescriptionDialog) {
        TransportProxyInterface transportProxy = null;
        int transactionID = 0;

        try {
            MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoArray.get(0);
            if (mergedInfo.getArchiveInfo() == null) {
                return;
            }

            ArchiveDirManagerInterface archiveDirManager = mergedInfo.getArchiveDirManager();
            ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) archiveDirManager;
            transportProxy = archiveDirManagerProxy.getTransportProxy();
            transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);

            String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
            String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator + mergedInfo.getShortWorkfileName();

            // The command args
            SetRevisionDescriptionCommandArgs commandArgs = new SetRevisionDescriptionCommandArgs();
            commandArgs.setRevisionString(setRevisionDescriptionDialog.getRevisionString());
            commandArgs.setRevisionDescription(setRevisionDescriptionDialog.getNewRevisionDescription());
            commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
            commandArgs.setUserName(mergedInfo.getUserName());
            if (mergedInfo.setRevisionDescription(commandArgs)) {
                if (mergedInfo.getIsRemote()) {
                    // Log the request.
                    traceProblem("Sent set revision description for " + fullWorkfileName + " to server.");
                } else {
                    warnProblem("Local set revision descriptionoperation not supported!!");
                }
            }
        } catch (QVCSException e) {
            warnProblem("Caught exception in OperationSetRevisionDescription: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            warnProblem(Utility.expandStackTraceToString(e));
        } finally {
            ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
        }
    }
}
