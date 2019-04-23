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

import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.operation.OperationBaseClass;
import com.qumasoft.guitools.qwin.operation.OperationBreakLock;
import com.qumasoft.guitools.qwin.operation.OperationCheckInArchive;
import com.qumasoft.guitools.qwin.operation.OperationCheckOutArchive;
import com.qumasoft.guitools.qwin.operation.OperationCompareRevisions;
import com.qumasoft.guitools.qwin.operation.OperationCreateArchive;
import com.qumasoft.guitools.qwin.operation.OperationDeleteArchive;
import com.qumasoft.guitools.qwin.operation.OperationGet;
import com.qumasoft.guitools.qwin.operation.OperationLabelArchive;
import com.qumasoft.guitools.qwin.operation.OperationLockArchive;
import com.qumasoft.guitools.qwin.operation.OperationMergeFile;
import com.qumasoft.guitools.qwin.operation.OperationRenameFile;
import com.qumasoft.guitools.qwin.operation.OperationResolveConflictFromParentBranchForTranslucentBranch;
import com.qumasoft.guitools.qwin.operation.OperationSetArchiveAttributes;
import com.qumasoft.guitools.qwin.operation.OperationSetCommentPrefix;
import com.qumasoft.guitools.qwin.operation.OperationSetModuleDescription;
import com.qumasoft.guitools.qwin.operation.OperationSetRevisionDescription;
import com.qumasoft.guitools.qwin.operation.OperationShowInContainingDirectory;
import com.qumasoft.guitools.qwin.operation.OperationUnDeleteArchive;
import com.qumasoft.guitools.qwin.operation.OperationUnLabelArchive;
import com.qumasoft.guitools.qwin.operation.OperationUndoCheckOut;
import com.qumasoft.guitools.qwin.operation.OperationView;
import com.qumasoft.guitools.qwin.operation.OperationViewRevision;
import com.qumasoft.guitools.qwin.operation.OperationVisualCompare;
import com.qumasoft.guitools.qwin.operation.OperationVisualMerge;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;

/**
 * The Right file pane.
 *
 * @author Jim Voris
 */
public final class RightFilePane extends javax.swing.JPanel implements javax.swing.event.ChangeListener {
    private static final long serialVersionUID = 5492608637891716573L;
    private final ActionGetRevision actionGetRevision = new ActionGetRevision("Get...");
    private final ActionCheckOutRevision actionCheckOutRevision = new ActionCheckOutRevision("Check Out...");
    private final ActionLockArchiveFile actionLockArchiveFile = new ActionLockArchiveFile("Lock...");
    private final ActionCheckIn actionCheckIn = new ActionCheckIn("Check In...");
    private final ActionUndoCheckOut actionUndoCheckOut = new ActionUndoCheckOut("Undo Check Out...");
    private final ActionBreakLock actionBreakLock = new ActionBreakLock("Break Lock");
    private final ActionLabel actionLabel = new ActionLabel("Label...");
    private final ActionRemoveLabel actionRemoveLabel = new ActionRemoveLabel("Remove Label...");
    private final ActionSetAttributes actionSetAttributes = new ActionSetAttributes("Set Attributes...");
    private final ActionChangeCommentPrefix actionChangeCommentPrefix = new ActionChangeCommentPrefix("Change Comment Prefix...");
    private final ActionChangeFileDescription actionChangeFileDescription = new ActionChangeFileDescription("Change File Description...");
    private final ActionChangeRevDescription actionChangeRevDescription = new ActionChangeRevDescription("Change Revision Description...");
    private final ActionRenameFile actionRenameFile = new ActionRenameFile("Rename...");
    private final ActionDeleteArchive actionDeleteArchive = new ActionDeleteArchive("Delete...");
    private final ActionUnDeleteArchive actionUnDeleteArchive = new ActionUnDeleteArchive("UnDelete...");
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

        // Set up drag n drop
        initDragAndDrop();

        // Init the font
        setFontSize(QWinFrame.getQWinFrame().getFontSize());
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
            logProblem(e.getLocalizedMessage());
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

                    // Update the revision info pane...
                    updateRevisionInfoPane(mergedInfo);

                    // Update the label info pane...
                    updateLabelInfoPane(mergedInfo);

                    // Update the revision and label info pane.
                    QWinFrame.getQWinFrame().getRevAndLabelInfoPane().setModel(new RevAndLabelInfoModel(mergedInfo));
                } else {
                    QWinFrame.getQWinFrame().getRevisionInfoPane().setDocument(emptyDoc);
                    QWinFrame.getQWinFrame().getLabelInfoPane().setDocument(emptyDoc);
                    QWinFrame.getQWinFrame().getRevAndLabelInfoPane().setModel(new RevAndLabelInfoModel());
                }
            } else {
                QWinFrame.getQWinFrame().getRevisionInfoPane().setDocument(emptyDoc);
                QWinFrame.getQWinFrame().getLabelInfoPane().setDocument(emptyDoc);
                QWinFrame.getQWinFrame().getRevAndLabelInfoPane().setModel(new RevAndLabelInfoModel());
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

    private void updateRevisionInfoPane(MergedInfoInterface workingMergedInfo) {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        RevisionInfoContent revisionInfoContent = new RevisionInfoContent(workingMergedInfo);
        DefaultStyledDocument revisionInfoDoc = new DefaultStyledDocument();
        Iterator it = revisionInfoContent.iterator();
        while (it.hasNext()) {
            try {
                revisionInfoDoc.insertString(0, (String) it.next(), attributeSet);
            } catch (BadLocationException e) {
                logProblem(e.getLocalizedMessage());
            }
        }
        QWinFrame.getQWinFrame().getRevisionInfoPane().setDocument(revisionInfoDoc);
    }

    private void updateLabelInfoPane(MergedInfoInterface workingMergedInfo) {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        LabelInfoContent labelInfoContent = new LabelInfoContent(workingMergedInfo);
        DefaultStyledDocument labelInfoDoc = new DefaultStyledDocument();
        Iterator it = labelInfoContent.iterator();
        while (it.hasNext()) {
            try {
                labelInfoDoc.insertString(0, (String) it.next(), attributeSet);
            } catch (BadLocationException e) {
                logProblem(e.getLocalizedMessage());
            }
        }
        QWinFrame.getQWinFrame().getLabelInfoPane().setDocument(labelInfoDoc);
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

        menuItem = menu.add(actionCheckOutRevision);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionLockArchiveFile);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionCheckIn);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionUndoCheckOut);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionBreakLock);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionLabel);
        menuItem.setFont(menuFont);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

        menuItem = menu.add(actionRemoveLabel);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionSetAttributes);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionChangeCommentPrefix);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionChangeFileDescription);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionChangeRevDescription);
        menuItem.setFont(menuFont);

        // =====================================================================
        menu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = menu.add(actionRenameFile);
        menuItem.setFont(menuFont);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

        menuItem = menu.add(actionDeleteArchive);
        menuItem.setFont(menuFont);

        menuItem = menu.add(actionUnDeleteArchive);
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

        menuItem = filePopupMenu.add(actionCheckOutRevision);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionLockArchiveFile);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionCheckIn);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionUndoCheckOut);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionBreakLock);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionLabel);
        menuItem.setFont(menuFont);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

        menuItem = filePopupMenu.add(actionRemoveLabel);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionSetAttributes);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionChangeCommentPrefix);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionChangeFileDescription);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionChangeRevDescription);
        menuItem.setFont(menuFont);

        // =====================================================================
        filePopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = filePopupMenu.add(actionRenameFile);
        menuItem.setFont(menuFont);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

        menuItem = filePopupMenu.add(actionDeleteArchive);
        menuItem.setFont(menuFont);

        menuItem = filePopupMenu.add(actionUnDeleteArchive);
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
        scrollPane = new javax.swing.JScrollPane();
        fileTable = new javax.swing.JTable();
        workfileLocationPanel = new javax.swing.JPanel();
        workfileLocationLabel = new javax.swing.JLabel();
        workfileLocationValue = new javax.swing.JLabel();

        filePopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        setLayout(new java.awt.BorderLayout());

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

        workfileLocationPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        workfileLocationPanel.setLayout(new javax.swing.BoxLayout(workfileLocationPanel, javax.swing.BoxLayout.Y_AXIS));

        workfileLocationLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        workfileLocationLabel.setText("  Workfile Location:");
        workfileLocationLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        workfileLocationPanel.add(workfileLocationLabel);

        workfileLocationValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        workfileLocationValue.setText(" ");
        workfileLocationValue.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        workfileLocationPanel.add(workfileLocationValue);

        add(workfileLocationPanel, java.awt.BorderLayout.NORTH);
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

    void enableMenuItems() {
        // Figure out what to enable/disable on the popup menu...
        boolean cemeteryIncludedFlag = false;
        boolean branchArchiveDirectoryIncludedFlag = false;
        List mergedInfoArray = getSelectedFiles();
        Iterator it = mergedInfoArray.iterator();
        if (mergedInfoArray.size() > 0) {
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

                if (mergedInfo.getLockCount() > 0) {
                    disableFileIsLockedOperations();
                }

                // Disable delete if any selected files are in the cemetery.
                String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                    actionDeleteArchive.setEnabled(false);
                    cemeteryIncludedFlag = true;
                }
                if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                    actionDeleteArchive.setEnabled(false);
                    branchArchiveDirectoryIncludedFlag = true;
                }
            }

            if (mergedInfoArray.size() != 1) {
                disableSingleFileOperations();
            } else {
                checkRemoveFileAssociationOperation((MergedInfoInterface) mergedInfoArray.get(0));
            }

            // Enable the undelete operation only when the cemetery is selected
            // and they have selected just one file...
            if (mergedInfoArray.size() == 1) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoArray.get(0);
                String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                if ((0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY))
                        || (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY))) {
                    disableAllPopUpOperations();
                    actionUnDeleteArchive.setEnabled(true);
                    actionCompareRevisions.setEnabled(true);
                }
            } else {
                // Multiple files have been selected. If any of the selected files are in the cemetery or
                // branch archive directory, then don't allow anything.
                if (cemeteryIncludedFlag || branchArchiveDirectoryIncludedFlag) {
                    disableAllPopUpOperations();
                }
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
        actionCheckOutRevision.setEnabled(true);
        actionLockArchiveFile.setEnabled(true);

        actionCheckIn.setEnabled(true);
        actionUndoCheckOut.setEnabled(true);
        actionBreakLock.setEnabled(true);

        actionLabel.setEnabled(true);
        actionRemoveLabel.setEnabled(true);

        actionSetAttributes.setEnabled(true);
        actionChangeCommentPrefix.setEnabled(true);
        actionChangeFileDescription.setEnabled(true);
        actionChangeRevDescription.setEnabled(true);

        actionRenameFile.setEnabled(true);
        actionDeleteArchive.setEnabled(true);
        actionUnDeleteArchive.setEnabled(false);

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
        actionCheckOutRevision.setEnabled(false);
        actionLockArchiveFile.setEnabled(false);

        actionCheckIn.setEnabled(false);
        actionUndoCheckOut.setEnabled(false);
        actionBreakLock.setEnabled(false);

        actionLabel.setEnabled(false);
        actionRemoveLabel.setEnabled(false);

        actionSetAttributes.setEnabled(false);
        actionChangeCommentPrefix.setEnabled(false);
        actionChangeFileDescription.setEnabled(false);
        actionChangeRevDescription.setEnabled(false);

        actionRenameFile.setEnabled(false);
        actionDeleteArchive.setEnabled(false);
        actionUnDeleteArchive.setEnabled(false);

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
        actionCheckOutRevision.setEnabled(false);
        actionLockArchiveFile.setEnabled(false);

        actionCheckIn.setEnabled(false);
        actionUndoCheckOut.setEnabled(false);
        actionBreakLock.setEnabled(false);

        actionLabel.setEnabled(false);
        actionRemoveLabel.setEnabled(false);

        actionSetAttributes.setEnabled(false);
        actionChangeCommentPrefix.setEnabled(false);
        actionChangeFileDescription.setEnabled(false);
        actionChangeRevDescription.setEnabled(false);

        actionDeleteArchive.setEnabled(false);
        actionUnDeleteArchive.setEnabled(false);

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
        actionChangeRevDescription.setEnabled(false);

        actionRenameFile.setEnabled(false);

        actionShowInContainingDir.setEnabled(false);
        actionView.setEnabled(false);
        actionViewRevision.setEnabled(false);
        actionRemoveUtilityAssociation.setEnabled(false);
    }

    private void disableFileIsLockedOperations() {
        actionRenameFile.setEnabled(false);
    }

    private void enableMergeFromParentOperation() {
        actionResolveConflictFromParentBranch.setEnabled(true);
    }

    private void disableMergeFromParentOperation() {
        actionResolveConflictFromParentBranch.setEnabled(false);
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
     * Initialize drag and drop.
     */
    private void initDragAndDrop() {
        fileTable.setDragEnabled(true);
        fileTable.setTransferHandler(new MyTransferHandler());
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
    private javax.swing.JPopupMenu filePopupMenu;
    private javax.swing.JTable fileTable;
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
                case AbstractFileTableModel.LOCKEDBY_COLUMN_INDEX:
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
                case AbstractFileTableModel.WORKFILEIN_COLUMN_INDEX:
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties(), true);
            getOperation.executeOperation();
        }
    }

    class ActionCheckOutRevision extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionCheckOutRevision(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass checkOutOperation = new OperationCheckOutArchive(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties(), true);
            checkOutOperation.executeOperation();
        }
    }

    class ActionLockArchiveFile extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionLockArchiveFile(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass lockOperation = new OperationLockArchive(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties(), true);
            lockOperation.executeOperation();
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            checkinOperation.executeOperation();
        }
    }

    class ActionUndoCheckOut extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionUndoCheckOut(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass undoCheckOutOperation = new OperationUndoCheckOut(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties(), true);
            undoCheckOutOperation.executeOperation();
        }
    }

    class ActionBreakLock extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionBreakLock(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO
            // This is where I need to put the break lock dialog, and only after the
            // dialog is dismissed with an OK so I actually perform the operation.
            OperationBaseClass breakLockOperation = new OperationBreakLock(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            breakLockOperation.executeOperation();
        }
    }

    class ActionLabel extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionLabel(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass labelOperation = new OperationLabelArchive(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            labelOperation.executeOperation();
        }
    }

    class ActionRemoveLabel extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionRemoveLabel(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass unLabelOperation = new OperationUnLabelArchive(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            unLabelOperation.executeOperation();
        }
    }

    class ActionSetAttributes extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionSetAttributes(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass setAttributesOperation = new OperationSetArchiveAttributes(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            setAttributesOperation.executeOperation();
        }
    }

    class ActionChangeCommentPrefix extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionChangeCommentPrefix(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass setCommentPrefix = new OperationSetCommentPrefix(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            setCommentPrefix.executeOperation();
        }
    }

    class ActionChangeFileDescription extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionChangeFileDescription(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass setModuleDescription = new OperationSetModuleDescription(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            setModuleDescription.executeOperation();
        }
    }

    class ActionChangeRevDescription extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionChangeRevDescription(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass setRevisionDescription = new OperationSetRevisionDescription(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            setRevisionDescription.executeOperation();
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            deleteOperation.executeOperation();
        }
    }

    class ActionUnDeleteArchive extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionUnDeleteArchive(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass unDeleteOperation = new OperationUnDeleteArchive(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            unDeleteOperation.executeOperation();
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
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
            String viewName = QWinFrame.getQWinFrame().getViewName();
            ProjectTreeModel projectTreeModel = QWinFrame.getQWinFrame().getTreeModel();
            ViewTreeNode projectTreeNode = projectTreeModel.findProjectViewTreeNode(serverName, projectName, viewName);
            if (viewName.equals(QVCSConstants.QVCS_TRUNK_VIEW)) {
                warnProblem("Attempt to resolve branch conflict on trunk!");
            } else {
                AbstractProjectProperties abstractProjectProperties = projectTreeNode.getProjectProperties();
                if (abstractProjectProperties instanceof RemoteViewProperties) {
                    RemoteViewProperties remoteViewProperties = (RemoteViewProperties) abstractProjectProperties;
                    if (remoteViewProperties.getIsTranslucentBranchFlag()) {
                        OperationBaseClass resolveConflictFromParentBranchForTranslucentBranch = new OperationResolveConflictFromParentBranchForTranslucentBranch(fileTable,
                                serverName, projectName, viewName, QWinFrame.getQWinFrame().getUserLocationProperties());
                        resolveConflictFromParentBranchForTranslucentBranch.executeOperation();
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
                    QWinFrame.getQWinFrame().getProjectName(), QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties());
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
                    QWinFrame.getQWinFrame().getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties(), true);
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
                            logProblem("Deleted workfile: " + mergedInfo.getWorkfileInfo().getFullWorkfileName());
                        }
                    }

                    if (modelChanged) {
                        QWinFrame.getQWinFrame().refreshCurrentView();
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

    class MyTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 1L;

        @Override
        protected Transferable createTransferable(JComponent c) {
            Transferable transferable = null;

            java.util.List selectedFiles = getSelectedFiles();
            if (selectedFiles.size() == 1) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) selectedFiles.get(0);
                String filename = null;
                if (mergedInfo.getWorkfileExists()) {
                    filename = mergedInfo.getWorkfileInfo().getFullWorkfileName();
                }
                transferable = new MyTransferable(filename, mergedInfo.getProjectName(), mergedInfo.getArchiveDirManager().getViewName(), mergedInfo.getArchiveDirManager().getAppendedPath(),
                        mergedInfo.getShortWorkfileName());
            }
            return transferable;
        }

        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            return false;
        }

        @Override
        public int getSourceActions(JComponent c) {
            int action = NONE;
            java.util.List selectedFiles = getSelectedFiles();
            if (selectedFiles.size() == 1) {
                action = COPY_OR_MOVE;
            }
            return action;
        }

        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            // Nothing to do here.
        }
    }

    final class MyTransferable implements java.awt.datatransfer.Transferable {

        private final DataFlavor[] windowsFlavors = {null, DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
        private final DataFlavor[] linuxFlavors = {null, DataFlavor.stringFlavor};
        private final List<File> fileList = new ArrayList<>();
        private String fileName = null;
        private File file = null;
        private DropTransferData dropTransferData = null;
        private boolean windowsFlag = false;

        MyTransferable(final String filename, final String projectName, final String viewName, final String appendedPath, final String shortWorkfileName) {
            if (filename != null) {
                fileList.add(new File(filename));
                fileName = filename;
                file = new File(fileName);
            }
            dropTransferData = new DropTransferData(projectName, viewName, appendedPath, shortWorkfileName);

            String OSName = System.getProperty("os.name");
            if (OSName.startsWith("Windows")) {
                windowsFlag = true;
            }
            try {
                dropDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=com.qumasoft.guitools.qwin.DropTransferData");
                windowsFlavors[0] = dropDataFlavor;
                linuxFlavors[0] = dropDataFlavor;
            } catch (ClassNotFoundException e) {
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            logProblem("DataFlavor:" + flavor.getHumanPresentableName());

            if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                if (file != null) {
                    return fileList;
                } else {
                    return null;
                }
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                // I've got to open the workfile, read its contents into
                // a String object, and return that String object.
                if (file != null && file.exists()) {
                    try {
                        // Use try with resources so we're guaranteed the file input stream is closed.
                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            byte[] buffer = new byte[(int) file.length()];
                            fileInputStream.read(buffer);
                            return new String(buffer);
                        }
                    } catch (FileNotFoundException e) {
                        warnProblem(Utility.expandStackTraceToString(e));
                        return "Caught exception: " + e.getLocalizedMessage();
                    } catch (IOException e) {
                        warnProblem(Utility.expandStackTraceToString(e));
                        return "Caught exception: " + e.getLocalizedMessage();
                    }
                } else {
                    return null;
                }
            } else if (flavor.equals(dropDataFlavor)) {
                return dropTransferData;
            } else {
                return null;
            }
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            if (windowsFlag) {
                return windowsFlavors;
            } else {
                return linuxFlavors;
            }
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                return true;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return true;
            } else {
                return flavor.equals(dropDataFlavor);
            }
        }
    }
}
