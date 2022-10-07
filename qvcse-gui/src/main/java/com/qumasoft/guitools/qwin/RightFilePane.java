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

import static com.qumasoft.guitools.qwin.QWinUtility.logMessage;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.operation.OperationBaseClass;
import com.qumasoft.guitools.qwin.operation.OperationCheckInArchive;
import com.qumasoft.guitools.qwin.operation.OperationCompareRevisions;
import com.qumasoft.guitools.qwin.operation.OperationCreateArchive;
import com.qumasoft.guitools.qwin.operation.OperationDeleteArchive;
import com.qumasoft.guitools.qwin.operation.OperationGet;
import com.qumasoft.guitools.qwin.operation.OperationMergeFile;
import com.qumasoft.guitools.qwin.operation.OperationMoveFile;
import com.qumasoft.guitools.qwin.operation.OperationRenameFile;
import com.qumasoft.guitools.qwin.operation.OperationResolveConflictFromParentBranchForFeatureBranch;
import com.qumasoft.guitools.qwin.operation.OperationShowInContainingDirectory;
import com.qumasoft.guitools.qwin.operation.OperationView;
import com.qumasoft.guitools.qwin.operation.OperationViewRevision;
import com.qumasoft.guitools.qwin.operation.OperationVisualCompare;
import com.qumasoft.guitools.qwin.operation.OperationVisualMerge;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.CommitInfo;
import com.qumasoft.qvcslib.CommitInfoListWrapper;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultStyledDocument;

/**
 * The Right file pane.
 *
 * @author Jim Voris
 */
public final class RightFilePane extends javax.swing.JPanel implements javax.swing.event.ChangeListener {
    private static final long serialVersionUID = 5492608637891716573L;
    private final ActionGetRevision actionGetRevision = new ActionGetRevision("Get...");
    private final ActionCheckIn actionCheckIn = new ActionCheckIn("Check In...");
    private final ActionMoveFile actionMoveFile = new ActionMoveFile("Move...");
    private final ActionRenameFile actionRenameFile = new ActionRenameFile("Rename...");
    private final ActionDeleteArchive actionDeleteArchive = new ActionDeleteArchive("Delete...");
    private final ActionCompare actionCompare = new ActionCompare("Compare");
    private final ActionCompareRevisions actionCompareRevisions = new ActionCompareRevisions("Compare Revisions...");
    private final ActionMergeFile actionMergeFile = new ActionMergeFile("Merge File...");
    private final ActionVisualMerge actionVisualMerge = new ActionVisualMerge("Visual Merge...");
    private final ActionResolveConflictFromParentBranch actionResolveConflictFromParentBranch = new ActionResolveConflictFromParentBranch("Resolve conflict from parent branch...");
    private final ActionShowInContainingDir actionShowInContainingDir = new ActionShowInContainingDir("Show in Containing Directory");
    private final ActionView actionView = new ActionView("View Workfile");
    private final ActionViewRevision actionViewRevision = new ActionViewRevision("View Revision...");
    private final ActionRemoveUtilityAssociation actionRemoveUtilityAssociation = new ActionRemoveUtilityAssociation("Remove Utility Association");
    private final ActionAddArchive actionAddArchive = new ActionAddArchive("Add...");
    private final ActionDeleteWorkFile actionDeleteWorkFile = new ActionDeleteWorkFile("Delete Workfile...");
    private final ActionWorkfileReadOnly actionWorkfileReadOnly = new ActionWorkfileReadOnly("Make workfile read-only");
    private final ActionWorkfileReadWrite actionWorkfileReadWrite = new ActionWorkfileReadWrite("Make workfile read-write");
    // Where we keep our table model.
    private AbstractFileTableModel tableModel = null;
    private int focusIndex = -1;
    private final ImageIcon[] sortOrderIcons = {
        new ImageIcon(ClassLoader.getSystemResource("images/ascending.png"), "Ascending"),
        new ImageIcon(ClassLoader.getSystemResource("images/decending.png"), "Decending")
    };
    private boolean fileGroupAdjustmentsInProgressFlag = false;
    private int moveableBranchCommitId = -1;
    private DataFlavor dropDataFlavor;
    private static final Color OVERLAP_BACKGROUND_COLOR = new Color(243, 255, 15);

    /**
     * Creates new form RightFilePane.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public RightFilePane() {
        initComponents();

        // Add the popup menu actions.
        addPopupMenuItems();

        // Don't let users drag columns around.
        fileTable.getTableHeader().setReorderingAllowed(false);

        // Register as a listener of the main frame so we'll know when
        // the user changes directories.
        QWinFrame.getQWinFrame().addChangeListener(this);

        fileTable.setDefaultRenderer(javax.swing.JLabel.class, new CellRenderer());

        // Set the JTable to the main frame so other's can easily find it.
        QWinFrame.getQWinFrame().setFileTable(fileTable);

        // Set the right file pane on the main frame so others can easily find it.
        QWinFrame.getQWinFrame().setRightFilePane(this);

        // Set up a column listener.
        tableModel.addMouseListenerToHeaderInTable(fileTable);

        // Set up a different renderer for the column headers.
        fileTable.getTableHeader().setDefaultRenderer(new HeaderRenderer(fileTable.getTableHeader().getDefaultRenderer()));

        // Set up a selection listener
        initSelectionListener();

        // Init the font
        setFontSize(QWinFrame.getQWinFrame().getFontSize());

        // Hide the moveable controls to start with.
        setCommitComboBoxVisible(false, "");
    }

    /**
     * Called by Swing on a state change.
     *
     * @param e what changed.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        setDirectoryManagers(QWinFrame.getQWinFrame().getCurrentDirectoryManagers());
        setWorkfileLocationValue(QWinFrame.getQWinFrame().getUserWorkfileDirectory());
    }

    /**
     * Set the workfile location value that gets displayed at the top of the pane.
     *
     * @param workfileLocation the workfile location.
     */
    public void setWorkfileLocationValue(String workfileLocation) {
        workfileLocationValue.setText("   " + workfileLocation);
    }

    public void setCommitComboBoxVisible(boolean flag, String branchName) {
        if (flag) {
            CommitInfoListWrapper commitInfoListWrapper = QWinFrame.getQWinFrame().getCommitInfoListWrapper(branchName);
            List<CommitInfo> commitInfoList = commitInfoListWrapper.getCommitInfoList();
            CommitInfoComboBoxModel commitInfoComboBoxModel = new CommitInfoComboBoxModel(commitInfoList);
            this.moveableBranchCommitId = commitInfoListWrapper.getTagCommitId();
            for (CommitInfo commitInfo : commitInfoList) {
                if (commitInfo.getCommitId().intValue() == commitInfoListWrapper.getTagCommitId().intValue()) {
                    commitInfoComboBoxModel.setSelectedItem(commitInfo);
                    break;
                }
            }
            commitInfoComboBox.setModel(commitInfoComboBoxModel);
        }
        commitInfoComboBox.setVisible(flag);
        applyButton.setVisible(flag);
        commitLabel.setVisible(flag);
    }

    /**
     * Select the next row that has a workfile that begins with the given character. If no row begins with that character, then don't do anything.
     *
     * @param keyChar the keyboard character to match.
     */
    private void selectMatchingRow(char keyChar) {
        char uppercaseKey = Character.toUpperCase(keyChar);
        int rowCount = getModel().getRowCount();
        int focusIdx = getFocusIndex();
        int startingIndex;
        if ((focusIdx >= 0) && (rowCount > 0)) {
            startingIndex = (focusIdx + 1) % rowCount;
        } else {
            startingIndex = 0;
        }
        try {
            for (int index = startingIndex, counter = 0; counter < rowCount; index = ++index % rowCount, counter++) {
                MergedInfoInterface mergedInfo = getModel().getMergedInfo(index);
                String shortWorkfileName = mergedInfo.getShortWorkfileName();
                String upperCaseWorkfileName = shortWorkfileName.toUpperCase();
                if (upperCaseWorkfileName.startsWith(String.valueOf(uppercaseKey))) {
                    // Select this row.
                    final int indexToSelect = index;
                    // Run this on the swing thread.
                    Runnable swingTask = () -> {
                        QWinFrame.getQWinFrame().getFileTable().getSelectionModel().setSelectionInterval(indexToSelect, indexToSelect);

                        // Now we need to make sure the file is actually visible...
                        JTable jTable = QWinFrame.getQWinFrame().getFileTable();
                        Rectangle visibleRectangle = jTable.getVisibleRect();
                        Rectangle selectedRectangle = jTable.getCellRect(indexToSelect, 0, true);

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
                            jTable.scrollRectToVisible(selectedRectangle);
                        }
                    };
                    SwingUtilities.invokeLater(swingTask);
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // This could happen if they are on the last row already.
            logMessage(e.getLocalizedMessage());
        }
    }

    private void initSelectionListener() {
        ListSelectionModel rowSelectionModel = fileTable.getSelectionModel();
        rowSelectionModel.addListSelectionListener((ListSelectionEvent listSelectionEvent) -> {
            // Ignore extra messages that arise from file group adjustments.
            if (fileGroupAdjustmentsInProgressFlag) {
                return;
            }

            DefaultStyledDocument emptyDoc = new DefaultStyledDocument();

            ListSelectionModel listSelectionModel = (ListSelectionModel) listSelectionEvent.getSource();
            AbstractFileTableModel dataModel = (AbstractFileTableModel) fileTable.getModel();
            int selectedRowCount = 0;
            int fileCount = fileTable.getRowCount();
            if (!listSelectionModel.isSelectionEmpty()) {
                int[] selectedRows = fileTable.getSelectedRows();
                if (selectedRows.length == 1) {
                    setFocusIndex(selectedRows[0]);
                }
                selectedRows = getFileGroupAdjustedSelectedRows(selectedRows);
                selectedRowCount = selectedRows.length;
                MergedInfoInterface mergedInfo = dataModel.getMergedInfo(getFocusIndex());
                if ((selectedRows.length > 0) && (mergedInfo != null)) {

                    if (QWinFrame.getQWinFrame().getRightDetailPane().isRevisionInfoSelected()) {
                        // Update the revision info detail pane.
                        QWinFrame.getQWinFrame().getRevisionInfoPane().setModel(new RevisionInfoModel(mergedInfo));
                        QWinFrame.getQWinFrame().getAllRevisionInfoPane().setModel(new RevisionInfoModel());
                    } else if (QWinFrame.getQWinFrame().getRightDetailPane().isAllRevisionInfoSelected()) {
                        // Update the revision info detail pane.
                        LogfileInfo allRevisionLogfileInfo = QWinFrame.getQWinFrame().fetchAllRevisions(mergedInfo);
                        QWinFrame.getQWinFrame().getAllRevisionInfoPane().setModel(new RevisionInfoModel(mergedInfo, allRevisionLogfileInfo));
                        QWinFrame.getQWinFrame().getRevisionInfoPane().setModel(new RevisionInfoModel());
                    }
                } else {
                    QWinFrame.getQWinFrame().getTagInfoPane().setTagInfoList(new ArrayList<>());
                    QWinFrame.getQWinFrame().getRevisionInfoPane().setModel(new RevisionInfoModel());
                    QWinFrame.getQWinFrame().getAllRevisionInfoPane().setModel(new RevisionInfoModel());
                }
            } else {
                QWinFrame.getQWinFrame().getTagInfoPane().setTagInfoList(new ArrayList<>());
                QWinFrame.getQWinFrame().getRevisionInfoPane().setModel(new RevisionInfoModel());
                QWinFrame.getQWinFrame().getAllRevisionInfoPane().setModel(new RevisionInfoModel());
            }
            QWinFrame.getQWinFrame().getStatusBar().setFileCount(fileCount, selectedRowCount);
        });
    }

    /**
     * Keep track of the index of the row that has the focus.
     *
     * @param index the index of the row that has focus.
     */
    private void setFocusIndex(int index) {
        focusIndex = index;
    }

    /**
     * Get the index of the row that has focus.
     *
     * @return the index of the row that has focus.
     */
    public int getFocusIndex() {
        return focusIndex;
    }

    private int[] getFileGroupAdjustedSelectedRows(int[] selectedRows) {
        int[] returnSelectedRows = selectedRows;
        fileGroupAdjustmentsInProgressFlag = true;

        if (FileGroupManager.getInstance().getEnabledFlag()) {
            // Only look for file group files to add for the case
            // where we have not already selected all the files.
            if (selectedRows.length < fileTable.getRowCount()) {
                // First, create a set of the current indexes.
                Set<Integer> selectedIndexes = new HashSet<>();
                for (int i = 0; i < selectedRows.length; i++) {
                    selectedIndexes.add(selectedRows[i]);
                }

                // Next, put all the files in the table that are in some
                // file group into a map that is keyed by the group name and
                // the base part of the file name (note that the key must include
                // the appended path, since we only group files that are in
                // the same directory).
                Map<String, List<Integer>> fileGroupMap = new HashMap<>();
                AbstractFileTableModel dataModel = (AbstractFileTableModel) fileTable.getModel();
                for (int i = 0; i < dataModel.getRowCount(); i++) {
                    MergedInfoInterface mergedInfo = dataModel.getMergedInfo(i);
                    String shortWorkfileName = mergedInfo.getShortWorkfileName();
                    String groupName = FileGroupManager.getInstance().getGroupNameForFile(shortWorkfileName);
                    if (groupName != null) {
                        String baseWorkfileName = Utility.stripFileExtension(shortWorkfileName);
                        baseWorkfileName = baseWorkfileName.toLowerCase();
                        String key = groupName + mergedInfo.getArchiveDirManager().getAppendedPath() + baseWorkfileName;
                        List<Integer> existingIndexes = fileGroupMap.get(key);
                        if (existingIndexes != null) {
                            existingIndexes.add(i);
                        } else {
                            existingIndexes = new ArrayList<>();
                            existingIndexes.add(i);
                            fileGroupMap.put(key, existingIndexes);
                        }
                    }
                }

                // Next, go through all the currently selected files and add
                // any files that are part of that same file group.
                for (int i = 0; i < selectedRows.length; i++) {
                    int selectedIndex = selectedRows[i];
                    MergedInfoInterface mergedInfo = dataModel.getMergedInfo(selectedIndex);
                    String shortWorkfileName = mergedInfo.getShortWorkfileName();
                    String groupName = FileGroupManager.getInstance().getGroupNameForFile(shortWorkfileName);
                    if (groupName != null) {
                        String baseWorkfileName = Utility.stripFileExtension(shortWorkfileName);
                        baseWorkfileName = baseWorkfileName.toLowerCase();
                        String key = groupName + mergedInfo.getArchiveDirManager().getAppendedPath() + baseWorkfileName;
                        List existingIndexes = fileGroupMap.get(key);
                        if (existingIndexes != null) {
                            // Add all the group's indexes to the list that
                            // should be selected.
                            existingIndexes.stream().forEach((existingIndexe) -> {
                                selectedIndexes.add((Integer) existingIndexe);
                            });
                        }
                    }
                }

                // We have now collected all the indexes.  Make sure they are
                // all selected.
                if (selectedIndexes.size() > selectedRows.length) {
                    Iterator<Integer> it = selectedIndexes.iterator();
                    returnSelectedRows = new int[selectedIndexes.size()];
                    ListSelectionModel listSelectionModel = fileTable.getSelectionModel();
                    int k = 0;
                    while (it.hasNext()) {
                        Integer indexToAdd = it.next();
                        int toAdd = indexToAdd;
                        returnSelectedRows[k++] = toAdd;
                        if (!listSelectionModel.isSelectedIndex(toAdd)) {
                            listSelectionModel.addSelectionInterval(toAdd, toAdd);
                        }
                    }
                }
            }
        }

        fileGroupAdjustmentsInProgressFlag = false;
        return returnSelectedRows;
    }

    /**
     * Add the menu items to the window's File menu. This is <b>not</b> the file context menu, but the 'static' menu at the top of the main window.
     *
     * @param menu the parent File menu.
     */
    void addPopupMenuItems(javax.swing.JMenu menu) {
        Font menuFont = QWinFrame.getQWinFrame().getFont(QWinFrame.getQWinFrame().getFontSize() + 1);

        // =====================================================================
        JMenuItem menuItem = menu.add(actionGetRevision);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionCheckIn);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionMoveFile);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionRenameFile);
        menuItem.setFont(menuFont);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

        menuItem = menu.add(actionDeleteArchive);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionCompare);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionCompareRevisions);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionMergeFile);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionVisualMerge);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionResolveConflictFromParentBranch);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionShowInContainingDir);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionView);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionViewRevision);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionRemoveUtilityAssociation);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionAddArchive);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionDeleteWorkFile);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionWorkfileReadOnly);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionWorkfileReadWrite);
        menuItem.setFont(menuFont);
    }

    private void addPopupMenuItems() {
        Font menuFont = QWinFrame.getQWinFrame().getFont(QWinFrame.getQWinFrame().getFontSize() + 1);

        // =====================================================================
        JMenuItem menuItem = filePopupMenu.add(actionGetRevision);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionCheckIn);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionMoveFile);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionRenameFile);
        menuItem.setFont(menuFont);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

        menuItem = filePopupMenu.add(actionDeleteArchive);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionCompare);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionCompareRevisions);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionMergeFile);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionVisualMerge);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionResolveConflictFromParentBranch);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionShowInContainingDir);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionView);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionViewRevision);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionRemoveUtilityAssociation);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionAddArchive);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionDeleteWorkFile);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionWorkfileReadOnly);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionWorkfileReadWrite);
        menuItem.setFont(menuFont);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filePopupMenu = new javax.swing.JPopupMenu();
        headerPanel = new javax.swing.JPanel();
        workfileLocationPanel = new javax.swing.JPanel();
        workfileLocationLabel = new javax.swing.JLabel();
        workfileLocationValue = new javax.swing.JLabel();
        commitInfoPanel = new javax.swing.JPanel();
        commitLabel = new javax.swing.JLabel();
        commitInfoComboBox = new javax.swing.JComboBox<>();
        applyButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        fileTable = new javax.swing.JTable();

        filePopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        setLayout(new java.awt.BorderLayout());

        headerPanel.setLayout(new javax.swing.BoxLayout(headerPanel, javax.swing.BoxLayout.Y_AXIS));

        workfileLocationPanel.setBorder(null);
        workfileLocationPanel.setLayout(new java.awt.BorderLayout());

        workfileLocationLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        workfileLocationLabel.setText("  Workfile Location:");
        workfileLocationLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        workfileLocationLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        workfileLocationPanel.add(workfileLocationLabel, java.awt.BorderLayout.WEST);

        workfileLocationValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        workfileLocationValue.setText(" ");
        workfileLocationValue.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        workfileLocationPanel.add(workfileLocationValue, java.awt.BorderLayout.CENTER);

        headerPanel.add(workfileLocationPanel);

        commitInfoPanel.setLayout(new java.awt.BorderLayout());

        commitLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        commitLabel.setText("Branch Anchor Commit:");
        commitLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        commitInfoPanel.add(commitLabel, java.awt.BorderLayout.WEST);

        commitInfoComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        commitInfoComboBox.setModel(new CommitInfoComboBoxModel());
        commitInfoComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                commitInfoComboBoxItemStateChanged(evt);
            }
        });
        commitInfoPanel.add(commitInfoComboBox, java.awt.BorderLayout.CENTER);

        applyButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        applyButton.setText("Apply");
        applyButton.setEnabled(false);
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        commitInfoPanel.add(applyButton, java.awt.BorderLayout.EAST);

        headerPanel.add(commitInfoPanel);

        add(headerPanel, java.awt.BorderLayout.PAGE_START);

        fileTable.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        fileTable.setModel(tableModel = new FilteredFileTableModel());
        fileTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        fileTable.setDoubleBuffered(true);
        fileTable.setDragEnabled(true);
        fileTable.setShowHorizontalLines(false);
        fileTable.setShowVerticalLines(false);
        fileTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fileTableKeyPressed(evt);
            }
        });
        fileTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileTableMouseReleased(evt);
            }
        });
        scrollPane.setViewportView(fileTable);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void fileTableKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_fileTableKeyPressed
    {//GEN-HEADEREND:event_fileTableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            processMouseDoubleClickEvent();
            evt.consume();
        }
        if ((evt.getKeyCode() >= KeyEvent.VK_0) && (evt.getKeyCode() <= KeyEvent.VK_Z)) {
            selectMatchingRow(evt.getKeyChar());
        }
    }//GEN-LAST:event_fileTableKeyPressed

    private void fileTableMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_fileTableMouseReleased
    {//GEN-HEADEREND:event_fileTableMouseReleased
        // Add your handling code here:
        if (evt.isPopupTrigger() || ((evt.getButton() == MouseEvent.BUTTON3) && (0 != (evt.getModifiers() & MouseEvent.MOUSE_RELEASED)))) {
            int row = fileTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
            if (!fileTable.isRowSelected(row)) {
                fileTable.setRowSelectionInterval(row, row);
            }

            enableMenuItems();

            filePopupMenu.show(fileTable, evt.getX(), evt.getY());
        } else if (2 == evt.getClickCount()) {
            // The user double clicked.
            processMouseDoubleClickEvent();
        }
    }//GEN-LAST:event_fileTableMouseReleased

    private void commitInfoComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_commitInfoComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            // Item was just selected
            CommitInfo selection = (CommitInfo) commitInfoComboBox.getModel().getSelectedItem();
            if (selection.getCommitId() != moveableBranchCommitId) {
                // Enable the Apply button.
                applyButton.setEnabled(true);
            } else {
                // Disable the Apply button.
                applyButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_commitInfoComboBoxItemStateChanged

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        CommitInfo selection = (CommitInfo) commitInfoComboBox.getModel().getSelectedItem();
        Integer newCommitId = selection.getCommitId();
        CommitInfoListWrapper commitInfoListWrapper = QWinFrame.getQWinFrame().updateTagCommitId(QWinFrame.getQWinFrame().getBranchName(), moveableBranchCommitId, newCommitId);
        List<CommitInfo> commitInfoList = commitInfoListWrapper.getCommitInfoList();
        CommitInfoComboBoxModel commitInfoComboBoxModel = new CommitInfoComboBoxModel(commitInfoList);
        this.moveableBranchCommitId = commitInfoListWrapper.getTagCommitId();
        for (CommitInfo commitInfo : commitInfoList) {
            if (commitInfo.getCommitId().intValue() == commitInfoListWrapper.getTagCommitId().intValue()) {
                commitInfoComboBoxModel.setSelectedItem(commitInfo);
                commitInfoComboBox.setModel(commitInfoComboBoxModel);
                break;
            }
        }
        // Clear all branches. User has to manually select the branch.
        ProjectTreeNode projectTreeNode = QWinFrame.getQWinFrame().getTreeModel().findProjectTreeNode(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName());
        QWinFrame.getQWinFrame().getTreeControl().selectNode(projectTreeNode);
    }//GEN-LAST:event_applyButtonActionPerformed

    void enableMenuItems() {
        // Figure out what to enable/disable on the popup menu...
        boolean cemeteryIncludedFlag = false;
        boolean branchArchiveDirectoryIncludedFlag = false;
        List mergedInfoArray = getSelectedFiles();
        if (QWinFrame.getQWinFrame().getTreeControl().getActiveBranchNode().isReadOnlyBranch()) {
            enableReadOnlyPopUpOperations();
        } else if (!mergedInfoArray.isEmpty()) {
            Iterator it = mergedInfoArray.iterator();
            enableAllPopUpOperations();
            FilteredFileTableModel filteredFileTableModel = (FilteredFileTableModel) QWinFrame.getQWinFrame().getRightFilePane().getModel();

            int mergeFromParentCount = 0;
            while (it.hasNext()) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();
                if (mergedInfo.getIsOverlap() && (mergedInfo.getStatusIndex() == MergedInfoInterface.CURRENT_STATUS_INDEX)) {
                    mergeFromParentCount++;
                }
                if (mergedInfo.getArchiveInfo() == null) {
                    disableArchivePopUpOperations();
                } else {
                    disableArchiveExistsOperations();
                }

                if (mergedInfo.getWorkfileInfo() == null) {
                    disableWorkfilePopUpOperations();
                }
            }

            if (mergedInfoArray.size() != 1) {
                disableSingleFileOperations();
            } else {
                checkRemoveFileAssociationOperation((MergedInfoInterface) mergedInfoArray.get(0));
            }

            if (mergeFromParentCount == mergedInfoArray.size()) {
                enableMergeFromParentOperation();
            } else {
                disableMergeFromParentOperation();
            }
        } else {
            disableAllPopUpOperations();
        }
    }

    private void processMouseDoubleClickEvent() {
        // We get here by typing the enter key, or by double clicking on the
        // file.
        actionView.actionPerformed(null);
    }

    private void setDirectoryManagers(DirectoryManagerInterface[] managers) {
        AbstractFileTableModel model = (AbstractFileTableModel) fileTable.getModel();
        model.setDirectoryManagers(managers, false, false);
    }

    private void enableAllPopUpOperations() {
        actionGetRevision.setEnabled(true);

        actionCheckIn.setEnabled(true);

        actionMoveFile.setEnabled(true);
        actionRenameFile.setEnabled(true);
        actionDeleteArchive.setEnabled(true);

        actionCompare.setEnabled(true);
        actionCompareRevisions.setEnabled(true);
        actionMergeFile.setEnabled(true);
        actionVisualMerge.setEnabled(true);
        actionResolveConflictFromParentBranch.setEnabled(true);

        actionShowInContainingDir.setEnabled(true);
        actionView.setEnabled(true);
        actionViewRevision.setEnabled(true);
        actionRemoveUtilityAssociation.setEnabled(true);

        actionAddArchive.setEnabled(true);
        actionDeleteWorkFile.setEnabled(true);
        actionWorkfileReadOnly.setEnabled(true);
        actionWorkfileReadWrite.setEnabled(true);
    }

    private void disableAllPopUpOperations() {
        actionGetRevision.setEnabled(false);

        actionCheckIn.setEnabled(false);

        actionMoveFile.setEnabled(false);
        actionRenameFile.setEnabled(false);
        actionDeleteArchive.setEnabled(false);

        actionCompare.setEnabled(false);
        actionCompareRevisions.setEnabled(false);
        actionMergeFile.setEnabled(false);
        actionVisualMerge.setEnabled(false);
        actionResolveConflictFromParentBranch.setEnabled(false);

        actionShowInContainingDir.setEnabled(false);
        actionView.setEnabled(false);
        actionViewRevision.setEnabled(false);
        actionRemoveUtilityAssociation.setEnabled(false);

        actionAddArchive.setEnabled(false);
        actionDeleteWorkFile.setEnabled(false);
        actionWorkfileReadOnly.setEnabled(false);
        actionWorkfileReadWrite.setEnabled(false);
    }

    private void disableArchivePopUpOperations() {
        actionGetRevision.setEnabled(false);

        actionCheckIn.setEnabled(false);
        actionMoveFile.setEnabled(false);
        actionRenameFile.setEnabled(false);

        actionDeleteArchive.setEnabled(false);

        actionCompare.setEnabled(false);
        actionCompareRevisions.setEnabled(false);
        actionMergeFile.setEnabled(false);
        actionVisualMerge.setEnabled(false);
        actionResolveConflictFromParentBranch.setEnabled(false);

        actionViewRevision.setEnabled(false);
    }

    private void disableArchiveExistsOperations() {
        actionAddArchive.setEnabled(false);
    }

    private void disableWorkfilePopUpOperations() {
        actionCompare.setEnabled(false);
        actionView.setEnabled(false);

        actionAddArchive.setEnabled(false);
        actionDeleteWorkFile.setEnabled(false);
        actionWorkfileReadOnly.setEnabled(false);
        actionWorkfileReadWrite.setEnabled(false);
    }

    private void disableSingleFileOperations() {
        actionMoveFile.setEnabled(false);
        actionRenameFile.setEnabled(false);

        actionShowInContainingDir.setEnabled(false);
        actionView.setEnabled(false);
        actionViewRevision.setEnabled(false);
        actionRemoveUtilityAssociation.setEnabled(false);
    }

    private void enableMergeFromParentOperation() {
        actionResolveConflictFromParentBranch.setEnabled(true);
    }

    private void disableMergeFromParentOperation() {
        actionResolveConflictFromParentBranch.setEnabled(false);
    }

    private void enableReadOnlyPopUpOperations() {
        actionGetRevision.setEnabled(true);

        actionCheckIn.setEnabled(false);

        actionMoveFile.setEnabled(false);
        actionRenameFile.setEnabled(false);
        actionDeleteArchive.setEnabled(false);

        actionCompare.setEnabled(true);
        actionCompareRevisions.setEnabled(true);
        actionMergeFile.setEnabled(false);
        actionVisualMerge.setEnabled(false);
        actionResolveConflictFromParentBranch.setEnabled(false);

        actionShowInContainingDir.setEnabled(true);
        actionView.setEnabled(true);
        actionViewRevision.setEnabled(true);
        actionRemoveUtilityAssociation.setEnabled(true);

        actionAddArchive.setEnabled(false);
        actionDeleteWorkFile.setEnabled(true);
        actionWorkfileReadOnly.setEnabled(true);
        actionWorkfileReadWrite.setEnabled(true);
    }

    private void checkRemoveFileAssociationOperation(MergedInfoInterface mergedInfo) {
        String shortWorkfileName = mergedInfo.getShortWorkfileName();
        if (!ViewUtilityManager.getInstance().getHasAssociatedUtility(shortWorkfileName)) {
            actionRemoveUtilityAssociation.setEnabled(false);
        }
    }

    private List getSelectedFiles() {
        int[] selectedRows = fileTable.getSelectedRows();
        AbstractFileTableModel dataModel = (AbstractFileTableModel) fileTable.getModel();
        List<MergedInfoInterface> mergedInfoArray = new ArrayList<>();

        // Save the information from the selected files.  We do this before
        // we do any work, since the doing of work will change the selection
        // status of the files.
        for (int i = 0; i < selectedRows.length; i++) {
            // Save the names of the archives we'll work on.
            int selectedRowIndex = selectedRows[i];
            MergedInfoInterface mergedInfo = dataModel.getMergedInfo(selectedRowIndex);
            if (mergedInfo != null) {
                mergedInfoArray.add(mergedInfo);
            }
        }
        return mergedInfoArray;
    }

    /**
     * Get the table data model.
     *
     * @return the abstract file table model.
     */
    public AbstractFileTableModel getModel() {
        return tableModel;
    }

    /**
     * Get the type of drop data.
     *
     * @return the 'flavor' of drop data.
     */
    public DataFlavor getDropDataFlavor() {
        return dropDataFlavor;
    }

    /**
     * Set the font size.
     *
     * @param fontSize the font size.
     */
    public void setFontSize(int fontSize) {
        fileTable.setFont(QWinFrame.getQWinFrame().getFont(fontSize));
        workfileLocationLabel.setFont(QWinFrame.getQWinFrame().getFont(fontSize + 1));
        workfileLocationValue.setFont(QWinFrame.getQWinFrame().getFont(fontSize + 1));
        setMenuFontSize(fontSize + 1);
    }

    private void setMenuFontSize(int fontSize) {
        Font font = QWinFrame.getQWinFrame().getFont(fontSize);
        MenuElement menuElements[] = filePopupMenu.getSubElements();
        for (MenuElement menuElement : menuElements) {
            menuElement.getComponent().setFont(font);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JComboBox<CommitInfo> commitInfoComboBox;
    private javax.swing.JPanel commitInfoPanel;
    private javax.swing.JLabel commitLabel;
    private javax.swing.JPopupMenu filePopupMenu;
    private javax.swing.JTable fileTable;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel workfileLocationLabel;
    private javax.swing.JPanel workfileLocationPanel;
    private javax.swing.JLabel workfileLocationValue;
    // End of variables declaration//GEN-END:variables

    class CellRenderer extends javax.swing.table.DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel inputLabel = (JLabel) fileTable.getModel().getValueAt(row, column);
            FileTableModel fileTableModel = (FileTableModel) fileTable.getModel();
            MergedInfoInterface mergedInfo = fileTableModel.getMergedInfo(row);

            // Set the column alignment.
            switch (column) {
                case AbstractFileTableModel.FILE_STATUS_COLUMN_INDEX:
                case AbstractFileTableModel.LASTEDITBY_COLUMN_INDEX: {
                    setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    break;
                }
                case AbstractFileTableModel.FILESIZE_COLUMN_INDEX: {
                    setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                    break;
                }
                case AbstractFileTableModel.FILENAME_COLUMN_INDEX:
                case AbstractFileTableModel.APPENDED_PATH_INDEX:
                case AbstractFileTableModel.LASTCHECKIN_COLUMN_INDEX:
                default: {
                    setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    break;
                }
            }
            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
                if (row == getFocusIndex()) {
                    setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                } else {
                    setBorder(noFocusBorder);
                }
            } else {
                if ((mergedInfo != null) && mergedInfo.getIsOverlap()) {
                    super.setBackground(OVERLAP_BACKGROUND_COLOR);
                } else {
                    super.setBackground(table.getBackground());
                }
                super.setForeground(table.getForeground());
                setBorder(noFocusBorder);
            }
            if ((mergedInfo != null) && mergedInfo.getIsOverlap()) {
                setToolTipText(" Conflict with Parent");
            } else {
                setToolTipText(null);
            }

            setFont(table.getFont());

            setText(inputLabel.getText());
            if (column == 0 && inputLabel.getIcon() != null) {
                setIcon(inputLabel.getIcon());
            } else {
                setIcon(null);
            }
            return this;
        }
    }

    class HeaderRenderer extends javax.swing.table.DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private TableCellRenderer m_defaultRenderer = null;

        HeaderRenderer(TableCellRenderer defaultRenderer) {
            m_defaultRenderer = defaultRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            DefaultTableCellRenderer component = (DefaultTableCellRenderer) m_defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            AbstractFileTableModel fileTableModel = (AbstractFileTableModel) fileTable.getModel();

            if (fileTableModel.getSortColumnInteger() == column) {
                if (fileTableModel.getAscendingFlag()) {
                    component.setIcon(sortOrderIcons[0]);
                } else {
                    component.setIcon(sortOrderIcons[1]);
                }
            } else {
                component.setIcon(null);
            }
            component.setFont(table.getFont());

            return component;
        }
    }

    class ActionGetRevision extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionGetRevision(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass getOperation = new OperationGet(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties(), true);
            getOperation.executeOperation();
        }
    }

    class ActionCheckIn extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionCheckIn(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass checkinOperation = new OperationCheckInArchive(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            checkinOperation.executeOperation();
        }
    }

    class ActionMoveFile extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionMoveFile(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass moveFileOperation = new OperationMoveFile(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            moveFileOperation.executeOperation();
        }
    }

    class ActionRenameFile extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionRenameFile(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass renameFileOperation = new OperationRenameFile(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            renameFileOperation.executeOperation();
        }
    }

    class ActionDeleteArchive extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionDeleteArchive(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass deleteOperation = new OperationDeleteArchive(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            deleteOperation.executeOperation();
        }
    }

    class ActionCompare extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionCompare(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass visualCompareOperation = new OperationVisualCompare(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            visualCompareOperation.executeOperation();
        }
    }

    class ActionCompareRevisions extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionCompareRevisions(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass compareRevisionsOperation = new OperationCompareRevisions(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            compareRevisionsOperation.executeOperation();
        }
    }

    class ActionMergeFile extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionMergeFile(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass mergeFileOperation = new OperationMergeFile(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            mergeFileOperation.executeOperation();
        }
    }

    class ActionVisualMerge extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionVisualMerge(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass visualMergeOperation = new OperationVisualMerge(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            visualMergeOperation.executeOperation();
        }
    }

    class ActionResolveConflictFromParentBranch extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionResolveConflictFromParentBranch(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String serverName = QWinFrame.getQWinFrame().getServerName();
            String projectName = QWinFrame.getQWinFrame().getProjectName();
            String branchName = QWinFrame.getQWinFrame().getBranchName();
            ProjectTreeModel projectTreeModel = QWinFrame.getQWinFrame().getTreeModel();
            BranchTreeNode projectTreeNode = projectTreeModel.findProjectBranchTreeNode(serverName, projectName, branchName);
            if (branchName.equals(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                warnProblem("Attempt to resolve branch conflict on trunk!");
            } else {
                AbstractProjectProperties abstractProjectProperties = projectTreeNode.getProjectProperties();
                if (abstractProjectProperties instanceof RemoteBranchProperties) {
                    RemoteBranchProperties remoteBranchProperties = (RemoteBranchProperties) abstractProjectProperties;
                    if (remoteBranchProperties.getIsFeatureBranchFlag()) {
                        OperationBaseClass resolveConflictFromParentBranchForFeatureBranch = new OperationResolveConflictFromParentBranchForFeatureBranch(fileTable,
                                serverName, projectName, branchName, QWinFrame.getQWinFrame().getUserLocationProperties());
                        resolveConflictFromParentBranchForFeatureBranch.executeOperation();
                    }
                }
            }
        }
    }

    class ActionShowInContainingDir extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionShowInContainingDir(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass showInContainingDirectoryOperation = new OperationShowInContainingDirectory(fileTable, QWinFrame.getQWinFrame().getServerName(),
                    QWinFrame.getQWinFrame().getProjectName(), QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            showInContainingDirectoryOperation.executeOperation();
        }
    }

    class ActionView extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionView(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass viewOperation = new OperationView(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            viewOperation.executeOperation();
        }
    }

    class ActionViewRevision extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionViewRevision(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass viewRevisionOperation = new OperationViewRevision(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            viewRevisionOperation.executeOperation();
        }
    }

    class ActionRemoveUtilityAssociation extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionRemoveUtilityAssociation(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // We only do anything if there is just one file selected.
            List selectedFiles = getSelectedFiles();
            if (selectedFiles.size() == 1) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) selectedFiles.get(0);
                String fullWorkfileName = mergedInfo.getFullWorkfileName();
                ViewUtilityManager.getInstance().removeUtilityAssociation(fullWorkfileName);
            }
        }
    }

    class ActionAddArchive extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionAddArchive(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass addOperation = new OperationCreateArchive(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties(), true);
            addOperation.executeOperation();
        }
    }

    class ActionDeleteWorkFile extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionDeleteWorkFile(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final List mergedInfoArray = getSelectedFiles();

            // Run the update on the Swing thread.
            Runnable later = () -> {
                // Let the user know that the client is out of date.
                int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Delete the selected workfile(s)?", "Delete Selected Workfiles",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    Iterator it = mergedInfoArray.iterator();
                    boolean modelChanged = false;

                    while (it.hasNext()) {
                        MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();

                        if (mergedInfo.getWorkfileInfo().getWorkfile().delete()) {
                            modelChanged = true;
                            WorkfileDigestManager.getInstance().removeWorkfileDigest(mergedInfo.getWorkfileInfo());
                            logMessage("Deleted workfile: " + mergedInfo.getWorkfileInfo().getFullWorkfileName());
                        }
                    }

                    if (modelChanged) {
                        QWinFrame.getQWinFrame().refreshCurrentBranch();
                    }
                }
            };
            SwingUtilities.invokeLater(later);
        }
    }

    class ActionWorkfileReadOnly extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionWorkfileReadOnly(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List mergedInfoArray = getSelectedFiles();
            Iterator it = mergedInfoArray.iterator();

            while (it.hasNext()) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();

                if (mergedInfo.getWorkfileInfo() != null) {
                    WorkFile workFile = new WorkFile(mergedInfo.getWorkfileInfo().getFullWorkfileName());
                    workFile.setReadOnly();
                }
            }
        }
    }

    class ActionWorkfileReadWrite extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionWorkfileReadWrite(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List mergedInfoArray = getSelectedFiles();
            Iterator it = mergedInfoArray.iterator();

            while (it.hasNext()) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();

                if (mergedInfo.getWorkfileInfo() != null) {
                    WorkFile workFile = new WorkFile(mergedInfo.getWorkfileInfo().getFullWorkfileName());
                    workFile.setReadWrite();
                }
            }
        }
    }
}
