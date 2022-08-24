/*   Copyright 2004-2021 Jim Voris
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
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.response.AbstractServerResponsePromoteFile;
import com.qumasoft.qvcslib.response.ServerResponseListFilesToPromote;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Promote to parent table model. This is the model behind the JTable that 'lives' on the promote to parent dialog.
 *
 * @author Jim Voris
 */
public final class PromoteToParentTableModel extends javax.swing.table.AbstractTableModel implements javax.swing.event.ChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromoteToParentTableModel.class);

    private static final long serialVersionUID = 6778821405618179622L;

    private final String promoteFromBranchName;
    private final String promoteToBranchName;
    private final TransportProxyInterface transportProxy;
    private List<FilePromotionInfo> filesToPromoteList;
    private final Map<String, DirectoryManagerInterface> directoryManagerMap;
    private final Map<Integer, MergedInfoInterface> mergedInfoFromBranchMap;
    private final Map<Integer, MergedInfoInterface> mergedInfoToBranchMap;
    private final Map<String, Map<Integer, MergedInfoInterface>> mergedInfoMapOfMaps;
    private final JLabel cellLabel = new JLabel();
    private final String[] columnTitleStrings = {
        "  File name  ",
        "  Appended Path  ",
        "  Type of Change "
    };
    static final int FILENAME_COLUMN_INDEX = 0;
    static final int APPENDED_PATH_INDEX = 1;
    static final int TYPE_INDEX = 2;

    /**
     * Constructor.
     *
     * @param branchName the name of the branch that we are promoting from.
     */
    public PromoteToParentTableModel(String branchName) {
        this.mergedInfoFromBranchMap = new HashMap<>();
        this.mergedInfoToBranchMap = new HashMap<>();
        this.mergedInfoMapOfMaps = new TreeMap<>();
        this.directoryManagerMap = new TreeMap<>();
        this.promoteFromBranchName = branchName;
        this.promoteToBranchName = QWinFrame.getQWinFrame().getBranchName();
        this.mergedInfoMapOfMaps.put(promoteFromBranchName, mergedInfoFromBranchMap);
        this.mergedInfoMapOfMaps.put(promoteToBranchName, mergedInfoToBranchMap);
        transportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
    }

    /**
     * Initialize things. This sends the request to the server to get the list of files eligible for promotion.
     */
    public void initialize() {
        TransportProxyFactory.getInstance().addChangeListener(this);
        int transactionId = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);

        ClientRequestListFilesToPromoteData clientRequestListFilesToPromoteData = new ClientRequestListFilesToPromoteData();
        clientRequestListFilesToPromoteData.setProjectName(QWinFrame.getQWinFrame().getProjectName());
        clientRequestListFilesToPromoteData.setBranchName(promoteFromBranchName);
        clientRequestListFilesToPromoteData.setPromoteToBranchName(promoteToBranchName);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestListFilesToPromoteData);
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
    public int getRowCount() {
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        cellLabel.setText("");
        cellLabel.setIcon(null);
        if (rowIndex <= filesToPromoteList.size()) {
            FilePromotionInfo filePromotionInfo = filesToPromoteList.get(rowIndex);
            switch (columnIndex) {
                case FILENAME_COLUMN_INDEX:
                    cellLabel.setText(filePromotionInfo.getPromotedFromShortWorkfileName());
                    cellLabel.setToolTipText(filePromotionInfo.getDescribeTypeOfPromotion());
                    break;
                case APPENDED_PATH_INDEX:
                    cellLabel.setText(filePromotionInfo.getPromotedFromAppendedPath());
                    cellLabel.setToolTipText(filePromotionInfo.getDescribeTypeOfPromotion());
                    break;
                case TYPE_INDEX:
                    cellLabel.setText(filePromotionInfo.getDescribeTypeOfPromotion());
                    break;
                default:
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
    public FilePromotionInfo getFilePromotionInfo(int rowIndex) {
        FilePromotionInfo filePromotionInfo = null;
        if (rowIndex >= 0 && rowIndex < filesToPromoteList.size()) {
            filePromotionInfo = filesToPromoteList.get(rowIndex);
        }
        return filePromotionInfo;
    }

    /**
     * Lookup the merged info for the given file id.
     *
     * @param branchName the name of the branch.
     * @param fileId the file id.
     * @return the associated merged info.
     */
    public synchronized MergedInfoInterface getMergedInfo(String branchName, Integer fileId) {
        return mergedInfoMapOfMaps.get(branchName).get(fileId);
    }

    /**
     * Called when we receive a message from the server.
     *
     * @param changeEvent the change event that captures the message from the server.
     */
    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        Object change = changeEvent.getSource();
        boolean somethingChanged = false;
        String serverName = QWinFrame.getQWinFrame().getServerName();
        String projectName = QWinFrame.getQWinFrame().getProjectName();

        if (change instanceof ServerResponseListFilesToPromote) {
            LOGGER.debug("PromoteToParentTableModel: stateChanged; ServerResponseListFilesToPromote");
            ServerResponseListFilesToPromote serverResponseListFilesToPromote = (ServerResponseListFilesToPromote) change;
            filesToPromoteList = new ArrayList<>(serverResponseListFilesToPromote.getFilesToPromoteList().size());
            AbstractProjectProperties projectProperties = ProjectTreeControl.getInstance().getActiveProject();
            // This could take some time, so wrap it in a client transaction so we'll put up the hourglass if we need to.
            int transactionId = ClientTransactionManager.getInstance().beginTransaction(serverName);
            for (FilePromotionInfo filePromotionInfo : serverResponseListFilesToPromote.getFilesToPromoteList()) {
                guaranteeExistenceOfDirectoryManagers(serverName, projectName, filePromotionInfo);
                filesToPromoteList.add(filePromotionInfo);
            }
            ClientTransactionManager.getInstance().endTransaction(serverName, transactionId);
            somethingChanged = true;
        } else if (change instanceof AbstractServerResponsePromoteFile) {
            LOGGER.debug("PromoteToParentTableModel: stateChanged; ServerResponsePromoteFile");
            AbstractServerResponsePromoteFile serverResponsePromoteFile = (AbstractServerResponsePromoteFile) change;

            // Find the file in the list of files, and remove it.
            for (int i = 0; i < filesToPromoteList.size(); i++) {
                FilePromotionInfo filePromotionInfo = getFilePromotionInfo(i);
                if ((filePromotionInfo != null) && filePromotionInfo.getFileId().equals(serverResponsePromoteFile.getPromotedFromSkinnyLogfileInfo().getFileID())) {
                    filesToPromoteList.remove(i);
                    somethingChanged = true;
                    break;
                }
            }
        } else if (change instanceof ArchiveDirManagerProxy) {
            LOGGER.debug("PromoteToParentTableModel: stateChanged; ArchiveDirManagerProxy");
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
                        if (filePromotionInfo.getPromotedFromAppendedPath().equals(directoryAppendedPath)) {
                            MergedInfoInterface mergedInfo = directoryManager.getMergedInfo(filePromotionInfo.getPromotedFromShortWorkfileName());

                            switch (filePromotionInfo.getTypeOfPromotion()) {
                                case FILE_CREATED_PROMOTION_TYPE:
                                    DirectoryManagerInterface parentDirectoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(serverName,
                                            projectName, this.promoteToBranchName,
                                            filePromotionInfo.getPromotedFromAppendedPath());
                                    // Make sure that the parent branch doesn't already contain a file with the same name.
                                    if (null != parentDirectoryManager.getWorkfileDirectoryManager().lookupWorkfileInfo(filePromotionInfo.getPromotedFromShortWorkfileName())) {
                                        // There is a workfile with the same name as the file we created on the branch...
                                        filePromotionInfo.setDescribeTypeOfPromotion("Promotion deferred because a workfile of the same name exists on the parent branch.");
                                    } else {
                                        if ((mergedInfo != null) && (mergedInfo.getStatusIndex() == MergedInfoInterface.CURRENT_STATUS_INDEX)) {
                                            mergedInfoMapOfMaps.get(archiveDirManagerProxy.getBranchName()).put(filePromotionInfo.getFileId(), mergedInfo);
                                        } else {
                                            filePromotionInfo.setDescribeTypeOfPromotion("Cannot promote file until status is 'Current'.");
                                        }
                                    }
                                    break;
                                default:
                                    // Only allow promotions for files that are 'current'.
                                    if ((mergedInfo != null) && (mergedInfo.getStatusIndex() == MergedInfoInterface.CURRENT_STATUS_INDEX)) {
                                        mergedInfoMapOfMaps.get(archiveDirManagerProxy.getBranchName()).put(filePromotionInfo.getFileId(), mergedInfo);
                                    } else {
                                        filePromotionInfo.setDescribeTypeOfPromotion("Cannot promote file until parent status is 'Current'.");
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
     * @param filePromotionInfo the file promotion info.
     * @throws QVCSException if there is a problem.
     */
    private void guaranteeExistenceOfDirectoryManagers(String serverName, String projectName, FilePromotionInfo filePromotionInfo) {
        // Need to build the directory manager from scratch, since there is no guarantee that it has been built yet.
        String promoteFromWorkfileBase = QWinFrame.getQWinFrame().getUserLocationProperties().getWorkfileLocation(serverName, projectName, promoteFromBranchName);
        String promoteToWorkfileBase = QWinFrame.getQWinFrame().getUserLocationProperties().getWorkfileLocation(serverName, projectName, promoteToBranchName);
        String appendedPath = filePromotionInfo.getPromotedFromAppendedPath();
        String promoteFromWorkfileDirectory;
        String promoteToWorkfileDirectory;
        if (appendedPath.length() > 0) {
            promoteFromWorkfileDirectory = promoteFromWorkfileBase + File.separator + appendedPath;
            promoteToWorkfileDirectory = promoteToWorkfileBase + File.separator + appendedPath;
        } else {
            promoteFromWorkfileDirectory = promoteFromWorkfileBase;
            promoteToWorkfileDirectory = promoteToWorkfileBase;
        }

        // See if the 'from' directory manager has already been created...
        DirectoryManagerInterface promoteFromDirectoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(serverName, projectName, promoteFromBranchName,
                filePromotionInfo.getPromotedFromAppendedPath());
        if (promoteFromDirectoryManager != null) {
            // The directory manager already existed.
            promoteFromDirectoryManager.addChangeListener(this);
        } else {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, promoteFromBranchName, filePromotionInfo.getPromotedFromAppendedPath());
            promoteFromDirectoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), serverName, directoryCoordinate,
                    promoteFromWorkfileDirectory, this, true, true);
            ArchiveDirManagerProxy promoteFromArchiveDirManager = (ArchiveDirManagerProxy) promoteFromDirectoryManager.getArchiveDirManager();
            final ArchiveDirManagerProxy fpromoteFromArchiveDirManager = promoteFromArchiveDirManager;
            Runnable waitOnDifferentThread = () -> {
                fpromoteFromArchiveDirManager.waitForInitToComplete();
            };
            SwingUtilities.invokeLater(waitOnDifferentThread);
        }

        // See if the 'to' directory manager has already been created...
        DirectoryManagerInterface promoteToDirectoryManager = DirectoryManagerFactory.getInstance().lookupDirectoryManager(serverName, projectName, promoteToBranchName,
                filePromotionInfo.getPromotedFromAppendedPath());
        if (promoteToDirectoryManager != null) {
            // The directory manager already existed.
            promoteToDirectoryManager.addChangeListener(this);
        } else {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, promoteToBranchName, filePromotionInfo.getPromotedFromAppendedPath());
            promoteToDirectoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), serverName, directoryCoordinate,
                    promoteToWorkfileDirectory, this, true, true);
            ArchiveDirManagerProxy promoteToArchiveDirManager = (ArchiveDirManagerProxy) promoteToDirectoryManager.getArchiveDirManager();
            final ArchiveDirManagerProxy fpromoteToArchiveDirManager = promoteToArchiveDirManager;
            Runnable waitOnDifferentThread = () -> {
                fpromoteToArchiveDirManager.waitForInitToComplete();
            };
            SwingUtilities.invokeLater(waitOnDifferentThread);
        }

        directoryManagerMap.put(createDirectoryManagerMapKey(promoteFromBranchName, filePromotionInfo.getPromotedFromAppendedPath()), promoteFromDirectoryManager);
        directoryManagerMap.put(createDirectoryManagerMapKey(promoteToBranchName, filePromotionInfo.getPromotedFromAppendedPath()), promoteToDirectoryManager);
    }

    /**
     * Create the key for the directory manager map.
     * @param branchName the branch name.
     * @param appendedPath the appended path.
     * @return the map key.
     */
    public String createDirectoryManagerMapKey(String branchName, String appendedPath) {
        return branchName + ":" + appendedPath;
    }

    /**
     * @return the directoryManagerMap
     */
    public Map<String, DirectoryManagerInterface> getDirectoryManagerMap() {
        return directoryManagerMap;
    }
}
