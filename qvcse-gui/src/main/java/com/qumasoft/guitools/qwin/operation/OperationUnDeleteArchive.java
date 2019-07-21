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
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteProjectProperties;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnDeleteData;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Undelete an archive.
 * @author Jim Voris
 */
public class OperationUnDeleteArchive extends OperationBaseClass {

    /**
     * Create an undelete operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     */
    public OperationUnDeleteArchive(JTable fileTable, String serverName, String projectName, String branchName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            if (getFileTable().getSelectedRowCount() == 1) {
                // Get the selected files...
                final List mergedInfoArray = getSelectedFiles();

                // Run the update on the Swing thread.
                Runnable later = () -> {
                    TransportProxyInterface transportProxy = null;
                    int transactionID = 0;

                    // Ask the user if they really want to undelete the file.
                    int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "UnDelete the selected file?", "UnDelete selected files",
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        Iterator it = mergedInfoArray.iterator();
                        int counter = 0;

                        while (it.hasNext()) {
                            MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();
                            ArchiveDirManagerInterface archiveDirManager = mergedInfo.getArchiveDirManager();
                            ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) archiveDirManager;

                            // We need to wrap this in a transaction.
                            if (counter == 0) {
                                transportProxy = archiveDirManagerProxy.getTransportProxy();
                                transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                            }

                            lookupOrCreateUnDeleteArchiveDirProxy(mergedInfo, transportProxy, archiveDirManager);

                            // Request an undelete of the file.
                            if (mergedInfo.getLockCount() == 0) {
                                ClientRequestUnDeleteData clientRequest = new ClientRequestUnDeleteData();

                                clientRequest.setProjectName(archiveDirManager.getProjectName());
                                clientRequest.setBranchName(archiveDirManager.getBranchName());
                                clientRequest.setAppendedPath(archiveDirManager.getAppendedPath());
                                clientRequest.setShortWorkfileName(mergedInfo.getShortWorkfileName());

                                if (null != transportProxy) {
                                    transportProxy.write(clientRequest);
                                }
                                // Log the success.
                                logProblem("Sent request that archive for '" + mergedInfo.getShortWorkfileName() + "' be restored from cemetery.");
                            } else {
                                warnProblem("Failed to undelete " + mergedInfo.getFullWorkfileName()
                                        + " . Cannot undelete a file that is locked.");
                            }
                            counter++;
                        }

                        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                    }
                };
                SwingUtilities.invokeLater(later);
            }
        }
    }

    private void lookupOrCreateUnDeleteArchiveDirProxy(MergedInfoInterface mergedInfo, TransportProxyInterface transportProxy,
                                                                                       ArchiveDirManagerInterface archiveDirManager) {
        // Lookup the archive dir proxy for the directory where the undelete will restore the
        // file. We do this so we're sure that it exists so that notifies from the server will
        // get delivered properly.
        String originalFileName = Utility.deduceOriginalFilenameForUndeleteFromCemetery(mergedInfo.getArchiveInfo());
        String appendedPath = "";
        if (-1 != originalFileName.lastIndexOf(QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR)) {
            int nameSegmentIndex = originalFileName.lastIndexOf(QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR);
            appendedPath = originalFileName.substring(0, nameSegmentIndex);
        }
        UserLocationProperties userLocationProperties = new UserLocationProperties(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), transportProxy.getUsername());
        String projectName = archiveDirManager.getProjectName();
        String workfileBaseDirectory = userLocationProperties.getWorkfileLocation(transportProxy.getServerProperties().getServerName(), projectName,
                QVCSConstants.QVCS_TRUNK_BRANCH);
        String workfileDirectory;
        if (appendedPath.length() > 0) {
            workfileDirectory = workfileBaseDirectory + File.separator + appendedPath;
        } else {
            workfileDirectory = workfileBaseDirectory;
        }

        RemoteProjectProperties remoteProjectProperties = new RemoteProjectProperties(archiveDirManager.getProjectName(),
                archiveDirManager.getProjectProperties().getProjectProperties());

        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(archiveDirManager.getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH, appendedPath);

        // Make sure this directory manager goes into our directory manager map.
        DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(),
                transportProxy.getServerProperties().getServerName(),
                directoryCoordinate, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, remoteProjectProperties, workfileDirectory, null, false);
    }
}
