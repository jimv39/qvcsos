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
package com.qumasoft.guitools.qwin;

import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.response.ServerResponseListFilesToPromote;
import com.qumasoft.qvcslib.response.ServerResponsePromoteFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

/**
 * Promote to parent table model. This is the model behind the JTable that 'lives' on the promote to parent dialog.
 *
 * @author Jim Voris
 */
public final class PromoteToParentTableModel extends javax.swing.table.AbstractTableModel implements javax.swing.event.ChangeListener {
    private static final long serialVersionUID = 6778821405618179622L;

    private final String branchToPromoteFromName;
    private final String branchToPromoteToName;
    private final TransportProxyInterface transportProxy;
    private List<FilePromotionInfo> filesToPromoteList;
    private Map<String, DirectoryManagerInterface> directoryManagerMap;
    private Map<Integer, MergedInfoInterface> mergedInfoMap;
    private final JLabel cellLabel = new JLabel();
    private final String[] columnTitleStrings = {
        "  File name  ",
        "  Appended Path  "
    };
    static final int FILENAME_COLUMN_INDEX = 0;
    static final int APPENDED_PATH_INDEX = 1;

    /**
     * Constructor.
     *
     * @param branchName the name of the branch that we are promoting from.
     */
    public PromoteToParentTableModel(String branchName) {
        this.mergedInfoMap = new HashMap<>();
        this.directoryManagerMap = new TreeMap<>();
        this.branchToPromoteFromName = branchName;
        this.branchToPromoteToName = QWinFrame.getQWinFrame().getBranchName();
        transportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
    }

    /**
     * Initialize things. This sends the request to the server to get the list of files eligible for promotion.
     */
    public void initialize() {
        directoryManagerMap = new TreeMap<>();
        mergedInfoMap = new HashMap<>();
        TransportProxyFactory.getInstance().addChangeListener(this);
        int transactionId = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);

        ClientRequestListFilesToPromoteData clientRequestListFilesToPromoteData = new ClientRequestListFilesToPromoteData();
        clientRequestListFilesToPromoteData.setProjectName(QWinFrame.getQWinFrame().getProjectName());
        clientRequestListFilesToPromoteData.setBranchName(branchToPromoteFromName);
        synchronized (transportProxy) {
            transportProxy.write(clientRequestListFilesToPromoteData);
        }
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionId);
    }

    /**
     * Close the model so we don't listen to change events anymore.
     */
    public void closeModel() {
        TransportProxyFactory.getInstance().removeChangeListener(this);
        Collection<DirectoryManagerInterface> directoryManagerCollection = directoryManagerMap.values();
        directoryManagerCollection.stream().forEach((directoryManager) -> {
            directoryManager.removeChangeListener(this);
        });
    }

    /**
     * Get the column name for the given column index.
     *
     * @param columnIndex the column index.
     * @return the name of the given column.
     */
    @Override
    public String getColumnName(int columnIndex) {
        return columnTitleStrings[columnIndex];
    }

    /**
     * Get the class for the given column. We've got to supply this method in order for our cell renderer to work correctly.
     *
     * @param columnIndex the column index.
     * @return the JLabel class, since that's the kind of component we'll always be rendering.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
        return javax.swing.JLabel.class;
    }

    /**
     * Get the row count.
     *
     * @return the number of row in the model.
     */
    @Override
    public synchronized int getRowCount() {
        int rowCount = 0;
        if (filesToPromoteList != null) {
            rowCount = filesToPromoteList.size();
        }
        return rowCount;
    }

    /**
     * Get the column count.
     *
     * @return the number of columns in the model.
     */
    @Override
    public int getColumnCount() {
        return columnTitleStrings.length;
    }

    /**
     * Get the value for the given cell coordinates.
     *
     * @param rowIndex the row index.
     * @param columnIndex the column index.
     * @return the JLabel representation for the given cell coordinates.
     */
    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        cellLabel.setText("");
        cellLabel.setIcon(null);
        if (rowIndex <= filesToPromoteList.size()) {
            FilePromotionInfo filePromotionInfo = filesToPromoteList.get(rowIndex);
            switch (columnIndex) {
                case FILENAME_COLUMN_INDEX:
                    cellLabel.setText(filePromotionInfo.getShortWorkfileName());
                    cellLabel.setToolTipText(filePromotionInfo.getDescribeTypeOfMerge());
                    break;
                default:
                case APPENDED_PATH_INDEX:
                    cellLabel.setText(filePromotionInfo.getAppendedPath());
                    cellLabel.setToolTipText(filePromotionInfo.getDescribeTypeOfMerge());
                    break;
            }
        }
        return cellLabel;
    }

    /**
     * Get the file promotion info at the given index.
     *
     * @param rowIndex the index we're interested in.
     * @return the file promotion info at the given index, or null if the index is not valid.
     */
    public synchronized FilePromotionInfo getFilePromotionInfo(int rowIndex) {
        FilePromotionInfo filePromotionInfo = null;
        if (rowIndex >= 0 && rowIndex < filesToPromoteList.size()) {
            filePromotionInfo = filesToPromoteList.get(rowIndex);
        }
        return filePromotionInfo;
    }

    /**
     * Lookup the merged info for the given file id.
     *
     * @param fileId the file id.
     * @return the associated merged info.
     */
    public synchronized MergedInfoInterface getMergedInfo(Integer fileId) {
        return mergedInfoMap.get(fileId);
    }

    /**
     * Called when we receive a message from the server.
     *
     * @param changeEvent the change event that captures the message from the server.
     */
    @Override
    public synchronized void stateChanged(ChangeEvent changeEvent) {
        Object change = changeEvent.getSource();
        boolean somethingChanged = false;
        String serverName = QWinFrame.getQWinFrame().getServerName();
        String projectName = QWinFrame.getQWinFrame().getProjectName();

        if (change instanceof ServerResponseListFilesToPromote) {
            ServerResponseListFilesToPromote serverResponseListFilesToPromote = (ServerResponseListFilesToPromote) change;
            filesToPromoteList = new ArrayList<>(serverResponseListFilesToPromote.getFilesToPromoteList().size());
            AbstractProjectProperties projectProperties = ProjectTreeControl.getInstance().getActiveProject();
            // This could take some time, so wrap it in a client transaction so we'll put up the hourglass if we need to.
            int transactionId = ClientTransactionManager.getInstance().beginTransaction(serverName);
            for (FilePromotionInfo filePromotionInfo : serverResponseListFilesToPromote.getFilesToPromoteList()) {
                switch (filePromotionInfo.getTypeOfMerge()) {
                    case CHILD_CREATED_MERGE_TYPE:
                        guaranteeExistenceOfDirectoryManager(serverName, projectName, this.branchToPromoteFromName, projectProperties, filePromotionInfo, true,
                                this.branchToPromoteToName);
                        filesToPromoteList.add(filePromotionInfo);
                        break;
                    default:
                        guaranteeExistenceOfDirectoryManager(serverName, projectName, this.branchToPromoteToName, projectProperties, filePromotionInfo, false, null);
                        filesToPromoteList.add(filePromotionInfo);
                        break;
                }
            }
            ClientTransactionManager.getInstance().endTransaction(serverName, transactionId);
            somethingChanged = true;
        } else if (change instanceof ServerResponsePromoteFile) {
            ServerResponsePromoteFile serverResponsePromoteFile = (ServerResponsePromoteFile) change;

            // Find the file in the list of files, and remove it.
            for (int i = 0; i < filesToPromoteList.size(); i++) {
                FilePromotionInfo filePromotionInfo = getFilePromotionInfo(i);
                if ((filePromotionInfo != null) && filePromotionInfo.getFileId().equals(serverResponsePromoteFile.getSkinnyLogfileInfo().getFileID())) {
                    filesToPromoteList.remove(i);
                    somethingChanged = true;
                    break;
                }
            }
        } else if (change instanceof ArchiveDirManagerProxy) {
            ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) change;
            DirectoryManagerInterface directoryManager = directoryManagerMap.get(createDirectoryManagerMapKey(archiveDirManagerProxy.getBranchName(),
                    archiveDirManagerProxy.getAppendedPath()));
            if (directoryManager != null) {
                try {
                    directoryManager.mergeManagers();
                    String directoryAppendedPath = archiveDirManagerProxy.getAppendedPath();
                    FilePromotionInfo filePromotionInfo;
                    Iterator<FilePromotionInfo> iterator = filesToPromoteList.iterator();
                    while (iterator.hasNext()) {
                        filePromotionInfo = iterator.next();
                        if (filePromotionInfo.getAppendedPath().equals(directoryAppendedPath)) {
                            MergedInfoInterface mergedInfo = directoryManager.getMergedInfo(filePromotionInfo.getShortWorkfileName());

                            switch (filePromotionInfo.getTypeOfMerge()) {
                                case CHILD_CREATED_MERGE_TYPE:
                                    DirectoryManagerInterface parentDirectoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(serverName,
                                            projectName, this.branchToPromoteToName,
                                            filePromotionInfo.getAppendedPath(), QVCSConstants.QVCS_REMOTE_PROJECT_TYPE);
                                    // Make sure that the parent branch doesn't already contain a file with the same name.
                                    if (null != parentDirectoryManager.getWorkfileDirectoryManager().lookupWorkfileInfo(filePromotionInfo.getShortWorkfileName())) {
                                        // There is a workfile with the same name as the file we created on the branch...
                                        filePromotionInfo.setDescribeTypeOfMerge("Promotion deferred because a workfile of the same name exists on the parent branch.");
                                    } else {
                                        if ((mergedInfo != null) && (mergedInfo.getStatusIndex() == MergedInfoInterface.CURRENT_STATUS_INDEX)) {
                                            mergedInfoMap.put(filePromotionInfo.getFileId(), mergedInfo);
                                        } else {
                                            filePromotionInfo.setDescribeTypeOfMerge("Cannot promote file until status is 'Current'.");
                                        }
                                    }
                                    break;
                                default:
                                    // Only allow promotions for files that are 'current'.
                                    if ((mergedInfo != null) && (mergedInfo.getStatusIndex() == MergedInfoInterface.CURRENT_STATUS_INDEX)) {
                                        mergedInfoMap.put(filePromotionInfo.getFileId(), mergedInfo);
                                    } else {
                                        filePromotionInfo.setDescribeTypeOfMerge("Cannot promote file until parent status is 'Current'.");
                                    }
                                    break;
                            }

                        }
                    }
                    somethingChanged = true;
                } catch (QVCSException e) {
                    warnProblem(Utility.expandStackTraceToString(e));
                }
            }
        }
        if (somethingChanged) {
            final PromoteToParentTableModel fThis = this;
            Runnable fireChange = () -> {
                fireTableChanged(new javax.swing.event.TableModelEvent(fThis));
            };
            SwingUtilities.invokeLater(fireChange);
        }
    }

    /**
     * Guarantee that we have a directory manager for each of the files that we may promote. We need to do this so that we'll be able to look up the mergedInfo for a file that is
     * selected for promotion. (We use the mergedInfo to make a synchronous call to the server to promote the file).
     *
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name where we will find the merged info. Note that for branch creates, the branch name should be the name of the branch from which the
     * promotion is being made. For other types of promotions, the branchName will be the name of the branch (usually trunk) to which the promotion is being made.
     * @param projectProperties the project's properties.
     * @param filePromotionInfo the file promotion info.
     * @throws QVCSException if there is a problem.
     */
    private void guaranteeExistenceOfDirectoryManager(String serverName, String projectName, String branchName, AbstractProjectProperties projectProperties,
                                                      FilePromotionInfo filePromotionInfo, boolean createFlag, String parentBranchName) {
        // Need to build the directory manager from scratch, since there is no guarantee that it has been built yet.
        String workfileBase = QWinFrame.getQWinFrame().getUserLocationProperties().getWorkfileLocation(serverName, projectName, branchName);
        String appendedPath = filePromotionInfo.getAppendedPath();
        String workfileDirectory;
        if (appendedPath.length() > 0) {
            workfileDirectory = workfileBase + File.separator + appendedPath;
        } else {
            workfileDirectory = workfileBase;
        }

        // See if the directory manager has already been created...
        DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(serverName, projectName, branchName,
                filePromotionInfo.getAppendedPath(),
                QVCSConstants.QVCS_REMOTE_PROJECT_TYPE);
        DirectoryManagerInterface parentDirectoryManager = null;

        // If the promotion type is for create, then we need to look at the parent branch (typically the trunk) to make sure that the file
        // of the same name doesn't already exist in the parent branch's workfile directory (i.e. prevent the overwrite of a file of the same name
        // that the user may have created but not checked in)... Note that this is NOT a lookup, but creates the directory manager.
        if (createFlag) {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, parentBranchName, filePromotionInfo.getAppendedPath());
            parentDirectoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), serverName,
                    directoryCoordinate, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, projectProperties, workfileDirectory, null, true);
            ArchiveDirManagerProxy archiveDirManager = (ArchiveDirManagerProxy) parentDirectoryManager.getArchiveDirManager();
            archiveDirManager.waitForInitToComplete();
        }
        if (directoryManager == null) {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, filePromotionInfo.getAppendedPath());
            directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), serverName, directoryCoordinate,
                    QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, projectProperties, workfileDirectory, this, true);
            ArchiveDirManagerProxy archiveDirManager = (ArchiveDirManagerProxy) directoryManager.getArchiveDirManager();
            archiveDirManager.waitForInitToComplete();
        } else {
            // The directory manager already existed.
            directoryManager.addChangeListener(this);
            MergedInfoInterface mergedInfo = directoryManager.getMergedInfo(filePromotionInfo.getShortWorkfileName());

            // Only allow promotions for files that are 'current'.
            if ((mergedInfo != null) && (mergedInfo.getStatusIndex() == MergedInfoInterface.CURRENT_STATUS_INDEX)) {
                if (createFlag && (parentDirectoryManager != null)) {
                    // Make sure that the parent branch doesn't already contain a file with the same name.
                    if (null != parentDirectoryManager.getWorkfileDirectoryManager().lookupWorkfileInfo(filePromotionInfo.getShortWorkfileName())) {
                        // There is a workfile with the same name as the file we created on the branch...
                        filePromotionInfo.setDescribeTypeOfMerge("Promotion deferred because a workfile of the same name exists on the parent branch.");
                    } else {
                        mergedInfoMap.put(filePromotionInfo.getFileId(), mergedInfo);
                    }
                } else {
                    mergedInfoMap.put(filePromotionInfo.getFileId(), mergedInfo);
                }
            } else {
                filePromotionInfo.setDescribeTypeOfMerge("Cannot promote file unless status is 'Current' and parent status is 'Current'.");
            }
        }
        directoryManagerMap.put(createDirectoryManagerMapKey(branchName, filePromotionInfo.getAppendedPath()), directoryManager);
    }

    private String createDirectoryManagerMapKey(String branchName, String appendedPath) {
        return branchName + ":" + appendedPath;
    }
}
