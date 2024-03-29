/*   Copyright 2004-2023 Jim Voris
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

import com.qumasoft.guitools.qwin.DirectoryTreeNode;
import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.AddDirectoryDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkfileDirectoryManagerInterface;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Operation add directory.
 * @author Jim Voris
 */
public final class OperationAddDirectory extends OperationBaseClass {

    private final String appendedPath;
    private final File currentWorkfileDirectory;
    private final String standardAppendedPathBase;

    /**
     * Create an add directory operation.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param path the appended path.
     * @param remoteProperties project properties.
     * @param currWorkfileDirectory the current workfile directory.
     */
    public OperationAddDirectory(String serverName, String projectName, String branchName, String path, RemotePropertiesBaseClass remoteProperties, File currWorkfileDirectory) {
        super(null, serverName, projectName, branchName, remoteProperties);

        appendedPath = path;
        currentWorkfileDirectory = currWorkfileDirectory;
        standardAppendedPathBase = Utility.convertToStandardPath(appendedPath);
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return this.appendedPath;
    }

    /**
     * Get the current workfile directory.
     * @return the current workfile directory.
     */
    public File getCurrentWorkfileDirectory() {
        return this.currentWorkfileDirectory;
    }

    @Override
    public void executeOperation() {
        String[] existingDirectories = getExistingDirectories();
        AddDirectoryDialog addDirectoryDialog = new AddDirectoryDialog(QWinFrame.getQWinFrame(), true, existingDirectories, this);
        addDirectoryDialog.setFont();
        addDirectoryDialog.center();
        addDirectoryDialog.setVisible(true);
    }

    /**
     * Process the dialog choices.
     * @param newDirectoryName the new directory name.
     */
    public void processDialogResult(String newDirectoryName) {
        TransportProxyInterface transportProxy = null;
        int transactionID = -1;

        try {
            String fullWorkfilePath = currentWorkfileDirectory.getCanonicalPath() + File.separator + newDirectoryName;
            String appendPath;
            if (this.appendedPath.length() > 0) {
                appendPath = this.appendedPath + File.separator + newDirectoryName;
            } else {
                appendPath = newDirectoryName;
            }

            // We need to wrap all this within a transaction.
            transportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
            transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);

            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), appendPath);
            DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(),
                    getServerName(), directoryCoordinate,
                    fullWorkfilePath, null, false, false);
            ArchiveDirManagerInterface archiveDirManager = directoryManager.getArchiveDirManager();
            archiveDirManager.createDirectory();
            WorkfileDirectoryManagerInterface workfileDirManager = directoryManager.getWorkfileDirectoryManager();
            workfileDirManager.createDirectory();
            archiveDirManager.startDirectoryManager();
        } catch (java.io.IOException e) {
            warnProblem("Failed to create directory: " + newDirectoryName);
            warnProblem(e.getMessage());
            warnProblem(Utility.expandStackTraceToString(e));
        } finally {
            if (transportProxy != null) {
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        }
    }

    private String[] getExistingDirectories() {
        String[] existingDirectories = null;
        if (currentWorkfileDirectory != null) {
            List<String> directoryList = new ArrayList<>();

            // Populate the combo with any directories visible from the current
            // directory.
            DirectoryNamesFilter directoryNameFilter = new DirectoryNamesFilter();

            // Add all the sub-projects.
            java.io.File[] directories = currentWorkfileDirectory.listFiles(directoryNameFilter);
            if (directories != null) {
                for (File directory : directories) {
                    directoryList.add(directory.getName());
                }
            }

            existingDirectories = new String[directoryList.size()];
            for (int i = 0; i < existingDirectories.length; i++) {
                existingDirectories[i] = directoryList.get(i);
            }
        }
        return existingDirectories;
    }

    class DirectoryNamesFilter implements java.io.FilenameFilter {

        DirectoryNamesFilter() {
        }

        @Override
        public boolean accept(java.io.File dir, String name) {
            boolean returnValue = false;
            java.io.File file = new java.io.File(dir.getPath(), name);

            if (file.isDirectory()) {
                String appendPath;
                if (standardAppendedPathBase.length() > 0) {
                    appendPath = standardAppendedPathBase + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + name;
                } else {
                    appendPath = name;
                }
                boolean foundAsChild = false;
                // We only want to include in the list those directories that
                // are not already under version control.
                DefaultMutableTreeNode treeNode = QWinFrame.getQWinFrame().getTreeControl().getSelectedNode();
                Enumeration enumeration = treeNode.children();
                while (enumeration.hasMoreElements()) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) enumeration.nextElement();
                    if (childNode instanceof DirectoryTreeNode child) {
                        String childAppendedPath = child.getAppendedPath();
                        if (0 == appendPath.compareTo(childAppendedPath)) {
                            foundAsChild = true;
                            break;
                        }
                    }
                }
                returnValue = !foundAsChild;
            }
            return returnValue;
        }
    }
}
