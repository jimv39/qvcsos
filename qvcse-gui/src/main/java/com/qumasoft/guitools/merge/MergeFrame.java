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
package com.qumasoft.guitools.merge;

import com.qumasoft.guitools.qwin.operation.OperationVisualMerge;
import com.qumasoft.qvcslib.CompareFilesEditHeader;
import com.qumasoft.qvcslib.CompareFilesEditInformation;
import com.qumasoft.qvcslib.CompareFilesWithApacheDiff;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.WorkfileInfo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merge frame window.
 *
 * @author Jim Voris
 */
public class MergeFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = -7484535954027054720L;

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(MergeFrame.class);
    private JFrame parentFrame;
    private OperationVisualMerge operationCaller;
    private MergedInfoInterface mergedInfo;
    private TreeMap<String, EditInfo> firstDecendentEditScript;
    private TreeMap<String, EditInfo> secondDecendentEditScript;
    private final ImageIcon frameIcon;
    private ChangeMarkerPanel firstDecendentMarkerPanel;
    private ChangeMarkerPanel secondDecendentMarkerPanel;
    private JViewport ancestorScrollPaneViewPort;
    private JViewport leftScrollPaneViewPort;
    private JViewport rightScrollPaneViewPort;
    private int rowHeight;
    private Font frameFont;
    private final MergeStatusBar firstDecendentStatusBar;
    private final MergeStatusBar secondDecendentStatusBar;
    private final String emptyString;
    private String eolSequence = System.getProperty(System.getProperty("line.separator"));
    private final OurViewportChangeListener viewportChangeListener;
    private final EscapeAction escapeAction;
    private DescendentFileContentsListModel ancestorFileContentsListModel;
    private WorkfileInfo defaultRevisionWorkfileInfo;
    private byte[] defaultRevisionBuffer;
    private int currentRowIndex = 0;
    private int verticalLinesInViewPort;
    private final ImageIcon moveToPreviousLeftEditButtonImage;
    private final ImageIcon applyLeftEditButtonImage;
    private final ImageIcon moveToNextLeftEditButtonImage;
    private final ImageIcon moveToPreviousRightEditButtonImage;
    private final ImageIcon applyRightEditButtonImage;
    private final ImageIcon moveToNextRightEditButtonImage;
    private final ImageIcon applyNonCollidingEditsButtonImage;
    private final ImageIcon saveChangesButtonImage;
    private final ImageIcon saveAsButtonImage;
    private static final int DEFAULT_FONT_SIZE = 12;

    /**
     * Creates new MergeFrame.
     */
    public MergeFrame() {
        this.saveAsButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/saveas.png"));
        this.saveChangesButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/save.png"));
        this.applyNonCollidingEditsButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/remainingedits.png"));
        this.moveToNextRightEditButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/moveToNextRightMerge.png"));
        this.applyRightEditButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/applyRightMerge.png"));
        this.moveToPreviousRightEditButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/moveToPreviousRightMerge.png"));
        this.moveToNextLeftEditButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/moveToNextLeftMerge.png"));
        this.applyLeftEditButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/applyLeftMerge.png"));
        this.moveToPreviousLeftEditButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/moveToPreviousLeftMerge.png"));
        this.escapeAction = new EscapeAction();
        this.viewportChangeListener = new OurViewportChangeListener();
        this.emptyString = "";
        this.secondDecendentStatusBar = new MergeStatusBar();
        this.firstDecendentStatusBar = new MergeStatusBar();
        this.frameIcon = new ImageIcon(ClassLoader.getSystemResource("images/qwin16.png"), "Quma Software, Inc.");
        frameFont = new Font("Courier", Font.PLAIN, DEFAULT_FONT_SIZE);
        initComponents();

        m_FirstDecendentStatusPanel.add(firstDecendentStatusBar, java.awt.BorderLayout.WEST);
        m_SecondDecendentStatusPanel.add(secondDecendentStatusBar, java.awt.BorderLayout.EAST);

        javax.swing.KeyStroke keyEscape = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyEscape, "EscapeKeyAction");
        getRootPane().getActionMap().put("EscapeKeyAction", escapeAction);

        // Set the frame icon to the Quma standard icon.
        this.setIconImage(frameIcon.getImage());
    }

    /**
     * Create a merge frame window.
     *
     * @param pFrame the parent frame window.
     */
    public MergeFrame(javax.swing.JFrame pFrame) {
        this();
        parentFrame = pFrame;
    }

    int getCurrentRowIndex() {
        return currentRowIndex;
    }

    /**
     * Set the display name for the output file.
     *
     * @param outputFileDisplayName the display name for the output file.
     */
    public void setOutputFileDisplayName(final String outputFileDisplayName) {
        m_MergedResultLabel.setText("    " + outputFileDisplayName);
    }

    /**
     * Set the display name for the 1st descendent file.
     *
     * @param firstDecendentDisplayName the display name for the 1st descendent file.
     */
    public void setFirstDecendentDisplayName(final String firstDecendentDisplayName) {
        m_LeftFileLabel.setText("    " + firstDecendentDisplayName);
    }

    /**
     * Set the display name for the 2nd descendent file.
     *
     * @param secondDecendentDisplayName the display name for the 2nd descendent file.
     */
    public void setSecondDecendentDisplayName(final String secondDecendentDisplayName) {
        m_RightFileLabel.setText("    " + secondDecendentDisplayName);
    }

    /**
     * Deduce the EOL sequence for this file. The algorithm traverses the lines of the file (as captured in the linked list passed as an
     * argument), and examines the number of characters that are used as the EOL sequence. If the same number of characters are always used
     * (either 1 or 2), then we can conclude the the EOL sequence is consistent throughout the file, and we can use that sequence. If the
     * EOL sequence varies, then we'll use the current platform's EOL sequence.
     *
     * @param baseFileLinkedList the linked list of row objects for the ancestor file.
     */
    private void deduceEOLSequence(LinkedList<MergedDescendentFileContentRow> baseFileLinkedList) {
        Iterator<MergedDescendentFileContentRow> it = baseFileLinkedList.iterator();
        long computedEOLLength = 0;
        int index = 0;
        long previousRowEnd = 0L;
        boolean eolLengthDefinedFlag = false;
        while (it.hasNext()) {
            MergedDescendentFileContentRow decendentRow = it.next();
            if (index > 0) {
                long currentComputedEOLLength = decendentRow.getAncestorSeekPosition() - previousRowEnd;
                if (index == 1) {
                    computedEOLLength = currentComputedEOLLength;
                } else {
                    if (currentComputedEOLLength != computedEOLLength) {
                        // We found a different length EOL sequence. Default
                        // back to the current platform EOL sequence.
                        eolSequence = System.getProperty("line.separator");
                        eolLengthDefinedFlag = true;

                        // No point in examining the rest of the file.
                        break;
                    }
                }
            }
            previousRowEnd = decendentRow.getAncestorSeekPosition() + decendentRow.getAncestorText().length();
            index++;
        }

        if (!eolLengthDefinedFlag) {
            if (computedEOLLength == 1) {
                eolSequence = "\n";
            } else {
                assert (computedEOLLength == 2);
                eolSequence = "\r\n";
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_MergeFrameToolBar = new javax.swing.JToolBar();
        m_MoveToPreviousLeftEditButton = new javax.swing.JButton();
        m_ApplyLeftEditButton = new javax.swing.JButton();
        m_MoveToNextLeftEditButton = new javax.swing.JButton();
        m_FirstToolbarSeparator = new javax.swing.JToolBar.Separator();
        m_MoveToPreviousRightEditButton = new javax.swing.JButton();
        m_ApplyRightEditButton = new javax.swing.JButton();
        m_MoveToNextRightEditButton = new javax.swing.JButton();
        m_SecondToolbarSeparator = new javax.swing.JToolBar.Separator();
        m_ApplyNonCollidingEditsButton = new javax.swing.JButton();
        m_ThirdToolbarSeparator = new javax.swing.JToolBar.Separator();
        m_SaveChangesButton = new javax.swing.JButton();
        m_SaveAsButton = new javax.swing.JButton();
        m_ParentSplitPane = new javax.swing.JSplitPane();
        m_MergeResultParentPanel = new javax.swing.JPanel();
        m_MergedResultLabel = new javax.swing.JLabel();
        m_MergedResultScrollPane = new javax.swing.JScrollPane();
        m_MergedResultPanel = new javax.swing.JPanel();
        m_LeftRightSplitPane = new javax.swing.JSplitPane();
        m_LeftFileParentPanel = new javax.swing.JPanel();
        m_LeftFileLabel = new javax.swing.JLabel();
        m_LeftFileScrollPane = new javax.swing.JScrollPane();
        m_LeftFilePanel = new javax.swing.JPanel();
        m_FirstDecendentStatusPanel = new javax.swing.JPanel();
        m_RightFileParentPanel = new javax.swing.JPanel();
        m_RightFileScrollPane = new javax.swing.JScrollPane();
        m_RightFilePanel = new javax.swing.JPanel();
        m_RightFileLabel = new javax.swing.JLabel();
        m_SecondDecendentStatusPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("QVCS Enterprise Visual Merge");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                frameResized(evt);
            }
        });

        m_MergeFrameToolBar.setFloatable(false);
        m_MergeFrameToolBar.setRollover(true);

        m_MoveToPreviousLeftEditButton.setIcon(moveToPreviousLeftEditButtonImage);
        m_MoveToPreviousLeftEditButton.setToolTipText("Move to previous edit from file on left.");
        m_MoveToPreviousLeftEditButton.setFocusable(false);
        m_MoveToPreviousLeftEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_MoveToPreviousLeftEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_MoveToPreviousLeftEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToPreviousLeftEditButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_MoveToPreviousLeftEditButton);

        m_ApplyLeftEditButton.setIcon(applyLeftEditButtonImage);
        m_ApplyLeftEditButton.setToolTipText("Apply next edit from file on left.");
        m_ApplyLeftEditButton.setFocusable(false);
        m_ApplyLeftEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_ApplyLeftEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_ApplyLeftEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftEditButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_ApplyLeftEditButton);

        m_MoveToNextLeftEditButton.setIcon(moveToNextLeftEditButtonImage);
        m_MoveToNextLeftEditButton.setToolTipText("Move to next edit from file on left.");
        m_MoveToNextLeftEditButton.setFocusable(false);
        m_MoveToNextLeftEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_MoveToNextLeftEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_MoveToNextLeftEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToNextLeftEditButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_MoveToNextLeftEditButton);
        m_MergeFrameToolBar.add(m_FirstToolbarSeparator);

        m_MoveToPreviousRightEditButton.setIcon(moveToPreviousRightEditButtonImage);
        m_MoveToPreviousRightEditButton.setToolTipText("Move to previous edit from file on right.");
        m_MoveToPreviousRightEditButton.setFocusable(false);
        m_MoveToPreviousRightEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_MoveToPreviousRightEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_MoveToPreviousRightEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToPreviousRightEditButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_MoveToPreviousRightEditButton);

        m_ApplyRightEditButton.setIcon(applyRightEditButtonImage);
        m_ApplyRightEditButton.setToolTipText("Apply next edit from file on right.");
        m_ApplyRightEditButton.setFocusable(false);
        m_ApplyRightEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_ApplyRightEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_ApplyRightEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightEditButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_ApplyRightEditButton);

        m_MoveToNextRightEditButton.setIcon(moveToNextRightEditButtonImage);
        m_MoveToNextRightEditButton.setToolTipText("Move to next edit from file on right.");
        m_MoveToNextRightEditButton.setFocusable(false);
        m_MoveToNextRightEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_MoveToNextRightEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_MoveToNextRightEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToNextRightEditButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_MoveToNextRightEditButton);
        m_MergeFrameToolBar.add(m_SecondToolbarSeparator);

        m_ApplyNonCollidingEditsButton.setIcon(applyNonCollidingEditsButtonImage);
        m_ApplyNonCollidingEditsButton.setToolTipText("<html>Apply <i>all</i> non-colliding edits from both files");
        m_ApplyNonCollidingEditsButton.setFocusable(false);
        m_ApplyNonCollidingEditsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_ApplyNonCollidingEditsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_ApplyNonCollidingEditsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyNonCollidingEditsButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_ApplyNonCollidingEditsButton);
        m_MergeFrameToolBar.add(m_ThirdToolbarSeparator);

        m_SaveChangesButton.setIcon(saveChangesButtonImage);
        m_SaveChangesButton.setToolTipText("Save merged output file.");
        m_SaveChangesButton.setFocusable(false);
        m_SaveChangesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_SaveChangesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_SaveChangesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveChangesButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_SaveChangesButton);

        m_SaveAsButton.setIcon(saveAsButtonImage);
        m_SaveAsButton.setToolTipText("Save output file as...");
        m_SaveAsButton.setFocusable(false);
        m_SaveAsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_SaveAsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_SaveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsButtonActionPerformed(evt);
            }
        });
        m_MergeFrameToolBar.add(m_SaveAsButton);

        getContentPane().add(m_MergeFrameToolBar, java.awt.BorderLayout.NORTH);

        m_ParentSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        m_MergeResultParentPanel.setLayout(new java.awt.BorderLayout());

        m_MergedResultLabel.setText("Merged Result:");
        m_MergedResultLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        m_MergeResultParentPanel.add(m_MergedResultLabel, java.awt.BorderLayout.NORTH);

        m_MergedResultPanel.setLayout(new java.awt.BorderLayout());
        m_MergedResultScrollPane.setViewportView(m_MergedResultPanel);

        m_MergeResultParentPanel.add(m_MergedResultScrollPane, java.awt.BorderLayout.CENTER);

        m_ParentSplitPane.setLeftComponent(m_MergeResultParentPanel);

        m_LeftFileParentPanel.setLayout(new java.awt.BorderLayout());

        m_LeftFileLabel.setText("Left File Info");
        m_LeftFileLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        m_LeftFileParentPanel.add(m_LeftFileLabel, java.awt.BorderLayout.NORTH);

        m_LeftFilePanel.setLayout(new java.awt.BorderLayout());
        m_LeftFileScrollPane.setViewportView(m_LeftFilePanel);

        m_LeftFileParentPanel.add(m_LeftFileScrollPane, java.awt.BorderLayout.CENTER);

        m_FirstDecendentStatusPanel.setLayout(new java.awt.BorderLayout());
        m_LeftFileParentPanel.add(m_FirstDecendentStatusPanel, java.awt.BorderLayout.SOUTH);

        m_LeftRightSplitPane.setLeftComponent(m_LeftFileParentPanel);

        m_RightFileParentPanel.setLayout(new java.awt.BorderLayout());

        m_RightFilePanel.setLayout(new java.awt.BorderLayout());
        m_RightFileScrollPane.setViewportView(m_RightFilePanel);

        m_RightFileParentPanel.add(m_RightFileScrollPane, java.awt.BorderLayout.CENTER);

        m_RightFileLabel.setText("Right File Info");
        m_RightFileLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        m_RightFileParentPanel.add(m_RightFileLabel, java.awt.BorderLayout.NORTH);

        m_SecondDecendentStatusPanel.setLayout(new java.awt.BorderLayout());
        m_RightFileParentPanel.add(m_SecondDecendentStatusPanel, java.awt.BorderLayout.SOUTH);

        m_LeftRightSplitPane.setRightComponent(m_RightFileParentPanel);

        m_ParentSplitPane.setBottomComponent(m_LeftRightSplitPane);

        getContentPane().add(m_ParentSplitPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void positionViewPort(int rowIndex) {
        captureViewPortSize();
        int midScreenRow = verticalLinesInViewPort / 2;
        int topRow;
        if ((ancestorFileContentsListModel.size() - rowIndex) < midScreenRow) {
            // We're positioning near the end of the file, so there's no place to scroll down.
            // Just show the last full screen of lines.
            topRow = ancestorFileContentsListModel.size() - verticalLinesInViewPort;
        } else {
            // We're able to scroll around.
            topRow = rowIndex - midScreenRow;
        }
        if (topRow < 0) {
            topRow = 0;
        }
        leftScrollPaneViewPort.setViewPosition(new Point(0, topRow * rowHeight));
    }

    private void captureViewPortSize() {
        if (leftScrollPaneViewPort != null) {
            Dimension newSize = leftScrollPaneViewPort.getSize();
            int height = newSize.height;
            verticalLinesInViewPort = height / rowHeight;
        }
    }

    /**
     * Save the changes (merged result) to the given File.
     *
     * @param workfile
     */
    private void saveChanges(File workfile) {
        try {
            try (PrintWriter printWriter = new PrintWriter(workfile)) {
                ancestorFileContentsListModel.writeChanges(printWriter, eolSequence);
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    private void frameResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_frameResized
    {//GEN-HEADEREND:event_frameResized
        centerSplitters();
    }//GEN-LAST:event_frameResized

    private void applyLeftEdit(ActionEvent evt) {
        if (currentRowIndex >= 0) {
            MergedDescendentFileContentRow row = ancestorFileContentsListModel.elementAt(currentRowIndex);
            if (row.getFirstDecendentCheckBoxVisibleFlag()) {
                JCheckBox checkBox = row.getFirstDecendentEditInfo().getCheckBox();
                JCheckBox otherCheckBox = null;
                if (row.getSecondDecendentEditInfo() != null) {
                    otherCheckBox = row.getSecondDecendentEditInfo().getCheckBox();
                }

                // Check for collisions...
                if (otherCheckBox == null || !otherCheckBox.isSelected()) {
                    Rectangle checkBoxRect = checkBox.getVisibleRect();
                    final int x = checkBoxRect.x + (checkBoxRect.width / 2);
                    final int y = checkBoxRect.y + (checkBoxRect.height / 2);
                    int modifiers = MouseEvent.BUTTON1_MASK;
                    checkBox.dispatchEvent(new MouseEvent(checkBox, MouseEvent.MOUSE_PRESSED, 1, modifiers, x, y, 1, false));
                    checkBox.dispatchEvent(new MouseEvent(checkBox, MouseEvent.MOUSE_RELEASED, 1, modifiers, x, y, 1, false));
                    reNumberMergedResult();
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "In an overlap region, you can choose an edit from only one file!", "Collision Area!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    private void moveToNextLeftEditButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_moveToNextLeftEditButtonActionPerformed
    {//GEN-HEADEREND:event_moveToNextLeftEditButtonActionPerformed
        boolean foundNextEditFlag = false;
        int i;
        MergedDescendentFileContentRow row = null;
        for (i = currentRowIndex; i < ancestorFileContentsListModel.size(); i++) {
            row = ancestorFileContentsListModel.elementAt(i);
            if (row.getFirstDecendentCheckBoxVisibleFlag() && (i > currentRowIndex)) {
                foundNextEditFlag = true;
                break;
            }
        }

        if (foundNextEditFlag) {
            currentRowIndex = i;
            positionViewPort(currentRowIndex);
            updateToolbarButtons(row);
        }
    }//GEN-LAST:event_moveToNextLeftEditButtonActionPerformed

    private void applyRightEdit(ActionEvent evt) {
        if (currentRowIndex >= 0) {
            MergedDescendentFileContentRow row = ancestorFileContentsListModel.elementAt(currentRowIndex);
            if (row.getSecondDecendentCheckBoxVisibleFlag()) {
                JCheckBox checkBox = row.getSecondDecendentEditInfo().getCheckBox();
                JCheckBox otherCheckBox = null;
                if (row.getFirstDecendentEditInfo() != null) {
                    otherCheckBox = row.getFirstDecendentEditInfo().getCheckBox();
                }

                // Check for collisions...
                if (otherCheckBox == null || !otherCheckBox.isSelected()) {
                    Rectangle checkBoxRect = checkBox.getVisibleRect();
                    final int x = checkBoxRect.x + (checkBoxRect.width / 2);
                    final int y = checkBoxRect.y + (checkBoxRect.height / 2);
                    int modifiers = MouseEvent.BUTTON1_MASK;
                    checkBox.dispatchEvent(new MouseEvent(checkBox, MouseEvent.MOUSE_PRESSED, 1, modifiers, x, y, 1, false));
                    checkBox.dispatchEvent(new MouseEvent(checkBox, MouseEvent.MOUSE_RELEASED, 1, modifiers, x, y, 1, false));
                    reNumberMergedResult();
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "In an overlap region, you can choose an edit from only one file!", "Collision Area!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    private void rightEditButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rightEditButtonActionPerformed
    {//GEN-HEADEREND:event_rightEditButtonActionPerformed
        applyRightEdit(evt);
    }//GEN-LAST:event_rightEditButtonActionPerformed

    private void moveToNextRightEditButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_moveToNextRightEditButtonActionPerformed
    {//GEN-HEADEREND:event_moveToNextRightEditButtonActionPerformed
        boolean foundNextEditFlag = false;
        int i;
        MergedDescendentFileContentRow row = null;
        for (i = currentRowIndex; i < ancestorFileContentsListModel.size(); i++) {
            row = ancestorFileContentsListModel.elementAt(i);
            if (row.getSecondDecendentCheckBoxVisibleFlag() && (i > currentRowIndex)) {
                foundNextEditFlag = true;
                break;
            }
        }

        if (foundNextEditFlag) {
            currentRowIndex = i;
            positionViewPort(currentRowIndex);
            updateToolbarButtons(row);
        }
    }//GEN-LAST:event_moveToNextRightEditButtonActionPerformed

    /**
     * Apply all edits from both files, provided they do not collide.
     *
     * @param evt ignored.
     */
    private void applyNonCollidingEditsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyNonCollidingEditsButtonActionPerformed
    {//GEN-HEADEREND:event_applyNonCollidingEditsButtonActionPerformed
        ancestorFileContentsListModel.applyNonCollidingEdits();
        repaint();
    }//GEN-LAST:event_applyNonCollidingEditsButtonActionPerformed

    private void saveChangesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveChangesButtonActionPerformed
    {//GEN-HEADEREND:event_saveChangesButtonActionPerformed
        // Write the merged result to the workfile.
        saveChanges(mergedInfo.getWorkfile());

        operationCaller.updateWorkfileInfo(mergedInfo, defaultRevisionWorkfileInfo, defaultRevisionBuffer);
    }//GEN-LAST:event_saveChangesButtonActionPerformed

    private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAsButtonActionPerformed
    {//GEN-HEADEREND:event_saveAsButtonActionPerformed
        // Save the changes to a file different than the current workfile
        // (Though it would be possible for them to save to the workfile as
        // well, so we need to check for that).
        FileDialog fileDialog = new FileDialog(this, "Save merged result as...", FileDialog.SAVE);
        fileDialog.setDirectory(mergedInfo.getWorkfile().getParent());
        fileDialog.setVisible(true);

        if (fileDialog.getFile() != null) {
            try {
                String fileNameToSaveTo = fileDialog.getFile();
                String fileNameToSaveToDirectory = fileDialog.getDirectory();
                String fullFileNameToSaveTo = fileNameToSaveToDirectory + fileNameToSaveTo;
                File fileToSaveTo = new File(fullFileNameToSaveTo);
                if (0 == fileToSaveTo.getCanonicalPath().compareTo(mergedInfo.getWorkfile().getCanonicalPath())) {
                    // They are saving to the same file as the default...
                    // We need to make sure to update the workfile status.
                    saveChangesButtonActionPerformed(null);
                } else {
                    // They are saving to some other file...
                    saveChanges(fileToSaveTo);
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
    }//GEN-LAST:event_saveAsButtonActionPerformed

    private void moveToPreviousLeftEditButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_moveToPreviousLeftEditButtonActionPerformed
    {//GEN-HEADEREND:event_moveToPreviousLeftEditButtonActionPerformed
        boolean foundPreviousEditFlag = false;
        int i;
        MergedDescendentFileContentRow row = null;
        for (i = currentRowIndex; i >= 0; i--) {
            row = ancestorFileContentsListModel.elementAt(i);
            if (row.getFirstDecendentCheckBoxVisibleFlag() && (i < currentRowIndex)) {
                foundPreviousEditFlag = true;
                break;
            }
        }

        if (foundPreviousEditFlag) {
            currentRowIndex = i;
            positionViewPort(currentRowIndex);
            updateToolbarButtons(row);
        }
    }//GEN-LAST:event_moveToPreviousLeftEditButtonActionPerformed

    private void moveToPreviousRightEditButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_moveToPreviousRightEditButtonActionPerformed
    {//GEN-HEADEREND:event_moveToPreviousRightEditButtonActionPerformed
        boolean foundPreviousEditFlag = false;
        int i;
        MergedDescendentFileContentRow row = null;
        for (i = currentRowIndex; i >= 0; i--) {
            row = ancestorFileContentsListModel.elementAt(i);
            if (row.getSecondDecendentCheckBoxVisibleFlag() && (i < currentRowIndex)) {
                foundPreviousEditFlag = true;
                break;
            }
        }

        if (foundPreviousEditFlag) {
            currentRowIndex = i;
            positionViewPort(currentRowIndex);
            updateToolbarButtons(row);
        }
    }//GEN-LAST:event_moveToPreviousRightEditButtonActionPerformed

    private void leftEditButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_leftEditButtonActionPerformed
    {//GEN-HEADEREND:event_leftEditButtonActionPerformed
        applyLeftEdit(evt);
    }//GEN-LAST:event_leftEditButtonActionPerformed

    private void updateToolbarButtons(MergedDescendentFileContentRow row) {
        if (currentRowIndex < ancestorFileContentsListModel.getMaximumLeftEditIndex()) {
            m_MoveToNextLeftEditButton.setEnabled(true);
        } else {
            m_MoveToNextLeftEditButton.setEnabled(false);
        }
        if (currentRowIndex > ancestorFileContentsListModel.getMinimumLeftEditIndex()) {
            m_MoveToPreviousLeftEditButton.setEnabled(true);
        } else {
            m_MoveToPreviousLeftEditButton.setEnabled(false);
        }
        if (currentRowIndex < ancestorFileContentsListModel.getMaximumRightEditIndex()) {
            m_MoveToNextRightEditButton.setEnabled(true);
        } else {
            m_MoveToNextRightEditButton.setEnabled(false);
        }
        if (currentRowIndex > ancestorFileContentsListModel.getMinimumRightEditIndex()) {
            m_MoveToPreviousRightEditButton.setEnabled(true);
        } else {
            m_MoveToPreviousRightEditButton.setEnabled(false);
        }
        if (row != null) {
            if (row.getFirstDecendentCheckBoxVisibleFlag()) {
                m_ApplyLeftEditButton.setEnabled(true);
            } else {
                m_ApplyLeftEditButton.setEnabled(false);
            }
            if (row.getSecondDecendentCheckBoxVisibleFlag()) {
                m_ApplyRightEditButton.setEnabled(true);
            } else {
                m_ApplyRightEditButton.setEnabled(false);
            }
        } else {
            m_ApplyLeftEditButton.setEnabled(false);
            m_ApplyRightEditButton.setEnabled(false);
        }
        repaint();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new MergeFrame().setVisible(true);
        });
    }

    private void centerSplitters() {
        Dimension currentSizeForHorizontal = this.getContentPane().getSize();
        Dimension currentSizeForVertical = m_ParentSplitPane.getSize();
        m_ParentSplitPane.setDividerLocation((currentSizeForVertical.height - m_MergeFrameToolBar.getHeight() - m_MergedResultLabel.getHeight()) / 2);
        m_LeftRightSplitPane.setDividerLocation(currentSizeForHorizontal.width / 2);
    }

    private void hookScrollBars() {
        ancestorScrollPaneViewPort = m_MergedResultScrollPane.getViewport();
        leftScrollPaneViewPort = m_LeftFileScrollPane.getViewport();
        rightScrollPaneViewPort = m_RightFileScrollPane.getViewport();

        ancestorScrollPaneViewPort.setScrollMode(JViewport.BLIT_SCROLL_MODE);
        leftScrollPaneViewPort.setScrollMode(JViewport.BLIT_SCROLL_MODE);
        rightScrollPaneViewPort.setScrollMode(JViewport.BLIT_SCROLL_MODE);

        ancestorScrollPaneViewPort.addChangeListener(viewportChangeListener);
        leftScrollPaneViewPort.addChangeListener(viewportChangeListener);
        rightScrollPaneViewPort.addChangeListener(viewportChangeListener);

        m_LeftFileScrollPane.getVerticalScrollBar().setUnitIncrement(rowHeight);
        firstDecendentMarkerPanel.setScrollBar(m_LeftFileScrollPane.getVerticalScrollBar());
        m_RightFileScrollPane.getVerticalScrollBar().setUnitIncrement(rowHeight);
        secondDecendentMarkerPanel.setScrollBar(m_RightFileScrollPane.getVerticalScrollBar());
        m_MergedResultScrollPane.getVerticalScrollBar().setUnitIncrement(rowHeight);

        m_LeftFileScrollPane.getHorizontalScrollBar().setUnitIncrement(rowHeight);
        m_RightFileScrollPane.getHorizontalScrollBar().setUnitIncrement(rowHeight);
        m_MergedResultScrollPane.getHorizontalScrollBar().setUnitIncrement(rowHeight);
    }

    void fitToScreen() {
        if (parentFrame == null) {
            Toolkit screenToolkit = java.awt.Toolkit.getDefaultToolkit();
            Dimension screenSize = screenToolkit.getScreenSize();
            setLocation(0, 20);
            screenSize.setSize((screenSize.width * 90) / 100, (screenSize.height * 90) / 100);
            setSize(screenSize);
        } else {
            setLocation(parentFrame.getLocation());
            setSize(parentFrame.getSize());
        }
    }

    /**
     * Merge the two decendent files of the base file into a single output file.
     *
     * @param baseFileName the common ancestor of the two decendent files.
     * @param firstDecendentFileName the filename of the first decendent file.
     * @param firstDecendentDisplayName the String to show for the first decendent file.
     * @param secondDecendentFileName the filename of the second decendent file.
     * @param secondDecendentDisplayName the String to show for the second decendent file.
     * @param outputFileName the filename of the output file that we create.
     * @param outputFileDisplayName the String to show for the output file.
     * @param caller string identifying the caller.
     * @param mergedInfo merged info for the file we are merging.
     * @param workfileInfo the workfileInfo object for the default revision.
     * @param defaultBuffer the unexpanded array of bytes for the default revision.
     * @throws IOException
     * @throws com.qumasoft.qvcslib.QVCSOperationException for a QVCS operation problem.
     */
    public void mergeFiles(final String baseFileName,
            final String firstDecendentFileName, final String firstDecendentDisplayName,
            final String secondDecendentFileName, final String secondDecendentDisplayName,
            final String outputFileName, final String outputFileDisplayName,
            final OperationVisualMerge caller,
            final MergedInfoInterface mergedInfo,
            final WorkfileInfo workfileInfo,
            final byte[] defaultBuffer) throws IOException, QVCSOperationException {
        operationCaller = caller;
        this.mergedInfo = mergedInfo;
        defaultRevisionWorkfileInfo = workfileInfo;
        defaultRevisionBuffer = defaultBuffer;
        setOutputFileDisplayName(outputFileDisplayName);
        setFirstDecendentDisplayName(firstDecendentDisplayName);
        setSecondDecendentDisplayName(secondDecendentDisplayName);

        // Create the models, etc. for the decendent files...
        firstDecendentEditScript = new TreeMap<>();
        computeEditScript(baseFileName, firstDecendentFileName, firstDecendentEditScript);

        secondDecendentEditScript = new TreeMap<>();
        computeEditScript(baseFileName, secondDecendentFileName, secondDecendentEditScript);

        LinkedList<MergedDescendentFileContentRow> baseFileLinkedList = populateFilePanels(baseFileName);

        firstDecendentMarkerPanel = new ChangeMarkerPanel(baseFileLinkedList, 1, rowHeight);
        secondDecendentMarkerPanel = new ChangeMarkerPanel(baseFileLinkedList, 2, rowHeight);

        m_LeftFileParentPanel.add(firstDecendentMarkerPanel, BorderLayout.EAST);
        m_RightFileParentPanel.add(secondDecendentMarkerPanel, BorderLayout.EAST);
        pack();

        // Create the model for the merged result and add it to the merged result
        // panel.
        ancestorFileContentsListModel = new DescendentFileContentsListModel(baseFileLinkedList, 0);
        reNumberMergedResult();
        updateToolbarButtons(null);
        DescendentFileContentsList ancestorFileContentsList = new DescendentFileContentsList(ancestorFileContentsListModel, 0, this, frameFont);
        m_MergedResultPanel.add(ancestorFileContentsList, BorderLayout.CENTER);

        fitToScreen();
        centerSplitters();
        hookScrollBars();
        setVisible(true);
    }

    void reNumberMergedResult() {
        ancestorFileContentsListModel.reNumberMergedResult();
    }

    private void computeEditScript(String baseFileName, String decendentFileName, TreeMap<String, EditInfo> editScript) throws IOException, QVCSOperationException {
        // Compare the first decendent to the base file.
        String[] compareArgs = new String[3];
        compareArgs[0] = baseFileName;
        compareArgs[1] = decendentFileName;
        File compareOutputTempFile = File.createTempFile("QVCS", ".tmp");
        compareOutputTempFile.deleteOnExit();
        compareArgs[2] = compareOutputTempFile.getCanonicalPath();

        LOGGER.info("Comparing [" + baseFileName + "] to [" + decendentFileName + "]");
        CompareFilesWithApacheDiff compareFilesOperator = new CompareFilesWithApacheDiff(compareArgs);

        // Ignore any differences in EOL sequences.
        compareFilesOperator.setIgnoreEOLChangesFlag(true);

        if (!compareFilesOperator.execute()) {
            throw new QVCSOperationException("Failed to compare " + baseFileName + " to file revision " + decendentFileName);
        }
        addFileToEditScript(compareOutputTempFile, editScript);
    }

    private void addFileToEditScript(File editScriptToAdd, TreeMap<String, EditInfo> editScript) throws QVCSOperationException {
        try {
            byte[] fileData = new byte[(int) editScriptToAdd.length()];

            // Use try with resources so we're guaranteed the file input stream is closed.
            try (FileInputStream fileInputStream = new FileInputStream(editScriptToAdd)) {
                fileInputStream.read(fileData);
            }
            DataInputStream editStream = new DataInputStream(new ByteArrayInputStream(fileData));
            CompareFilesEditInformation cfei = new CompareFilesEditInformation();

            // Skip over the header bytes that are a prefix at the beginning of the
            // edit script.
            byte[] headerBytesToSkip = new byte[CompareFilesEditHeader.getEditHeaderSize()];
            editStream.read(headerBytesToSkip);

            while (editStream.available() > 0) {
                byte[] insertBuffer = null;
                cfei.read(editStream);
                if ((cfei.getEditType() == CompareFilesEditInformation.QVCS_EDIT_INSERT)
                        || (cfei.getEditType() == CompareFilesEditInformation.QVCS_EDIT_REPLACE)) {
                    insertBuffer = new byte[(int) cfei.getInsertedBytesCount()];
                    editStream.read(insertBuffer);
                }

                EditInfo editInfo = new EditInfo(cfei.getSeekPosition(),
                        cfei.getEditType(),
                        cfei.getDeletedBytesCount(),
                        cfei.getInsertedBytesCount(),
                        insertBuffer);
                editScript.put(String.format("%015d", cfei.getSeekPosition()), editInfo);
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new QVCSOperationException(e.getLocalizedMessage());
        }
    }

    private boolean populateBaseFileLinkedList(final String baseFileName, LinkedList<MergedDescendentFileContentRow> baseFileLinkedList) {
        boolean retVal = false;

        // Read the base file into a linked list of row objects...
        File baseFile = new File(baseFileName);
        RandomAccessFile randomAccessFile = null;
        if (baseFile.canRead()) {
            try {
                randomAccessFile = new RandomAccessFile(baseFile, "r");
                int lineIndex = 0;
                long seekPosition = 0;
                while (true) {
                    String line = randomAccessFile.readLine();
                    if (line == null) {
                        break;
                    }
                    MergedDescendentFileContentRow row = new MergedDescendentFileContentRow(frameFont);
                    String formattedLine = formatLine(line);
                    if (line.length() > 0) {
                        row.setAncestorRowType(MergedDescendentFileContentRow.ROWTYPE_NORMAL);
                    } else {
                        row.setAncestorRowType(MergedDescendentFileContentRow.ROWTYPE_BLANK);
                    }
                    row.setAncestorLineNumber(lineIndex + 1);
                    row.setAncestorOriginalLineNumber(lineIndex + 1);
                    row.setAncestorText(formattedLine);
                    row.setAncestorSeekPosition(seekPosition);
                    baseFileLinkedList.add(row);
                    lineIndex++;
                    seekPosition = randomAccessFile.getFilePointer();
                }
            } catch (java.io.FileNotFoundException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            } catch (java.io.IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                try {
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                }
            }
            retVal = true;
        }
        return retVal;
    }

    private void populateDecendentLinkedList(LinkedList<MergedDescendentFileContentRow> baseFileLinkedList, LinkedList<DescendentFileContentRow> decendentFileLinkedList) {
        assert (decendentFileLinkedList.isEmpty());
        ListIterator<MergedDescendentFileContentRow> rowIterator = baseFileLinkedList.listIterator();
        while (rowIterator.hasNext()) {
            MergedDescendentFileContentRow mergedRow = rowIterator.next();
            DescendentFileContentRow decendentRow = new DescendentFileContentRow(mergedRow);
            decendentFileLinkedList.add(decendentRow);
        }
    }

    private void applyDescendentEdits(LinkedList<DescendentFileContentRow> descendentFileLinkedList, TreeMap<String, EditInfo> editScript) throws QVCSOperationException {
        Iterator<EditInfo> editInfoIterator = editScript.values().iterator();
        ListIterator<DescendentFileContentRow> rowIterator = descendentFileLinkedList.listIterator();
        int descendentLineNumber = 1;
        DescendentFileContentRow existingRow;
        while (editInfoIterator.hasNext()) {
            existingRow = null;
            EditInfo editInfo = editInfoIterator.next();
            long editSeekPosition = editInfo.getSeekPosition();
            while (rowIterator.hasNext()) {
                existingRow = rowIterator.next();
                long existingRowStartPosition = existingRow.getAncestorSeekPosition();
                if (existingRowStartPosition < editSeekPosition) {
                    existingRow.setDescendentLineNumber(descendentLineNumber++);
                } else {
                    break;
                }
            }

            if (existingRow != null) {
                // We're positioned on the row where the edit takes place...
                switch (editInfo.getEditType()) {
                    case CompareFilesEditInformation.QVCS_EDIT_DELETE: {
                        long endOfEditSeekPosition = editInfo.getSeekPosition() + editInfo.getDeletedBytesCount();
                        int rowCounter = 0;
                        do {
                            existingRow.setDescendentRowType(MergedDescendentFileContentRow.ROWTYPE_DELETE);
                            existingRow.setDescendentEditInfo(editInfo);
                            if (rowCounter == 0) {
                                existingRow.setDescendentFirstRowOfEditFlag(true);
                            }
                            existingRow = rowIterator.next();
                            if (existingRow.getAncestorSeekPosition() >= endOfEditSeekPosition) {
                                rowIterator.previous();
                                break;
                            }
                            rowCounter++;
                        } while (rowIterator.hasNext());
                        break;
                    }
                    case CompareFilesEditInformation.QVCS_EDIT_INSERT: {
                        // If we're appending to the end of the file, we do not
                        // want to reposition the iterator...
                        boolean appendToFileEndFlag = false;
                        if (rowIterator.hasNext()) {
                            rowIterator.previous();
                        } else {
                            appendToFileEndFlag = true;
                        }

                        String[] insertionText = convertInsertToStringArray(editInfo, appendToFileEndFlag);
                        int rowCounter = 0;
                        for (int i = 0; i < insertionText.length; i++, rowCounter++) {
                            DescendentFileContentRow newRow = new DescendentFileContentRow();
                            newRow.setDescendentLineNumber(descendentLineNumber++);
                            newRow.setDescendentRowType(MergedDescendentFileContentRow.ROWTYPE_INSERT);
                            newRow.setDescendentEditInfo(editInfo);
                            newRow.setDescendentText(insertionText[i]);
                            if (rowCounter == 0) {
                                newRow.setDescendentFirstRowOfEditFlag(true);
                            }
                            rowIterator.add(newRow);
                        }
                        break;
                    }

                    case CompareFilesEditInformation.QVCS_EDIT_REPLACE: {
                        rowIterator.previous();
                        long endOfEditSeekPosition = editInfo.getSeekPosition() + editInfo.getDeletedBytesCount();
                        int rowCounter = 0;
                        do {
                            existingRow.setDescendentRowType(MergedDescendentFileContentRow.ROWTYPE_DELETE);
                            existingRow.setDescendentEditInfo(editInfo);
                            if (rowCounter == 0) {
                                existingRow.setDescendentFirstRowOfEditFlag(true);
                            }
                            existingRow.setDescendentLineNumber(0);
                            existingRow = rowIterator.next();
                            if (existingRow.getAncestorSeekPosition() >= endOfEditSeekPosition) {
                                rowIterator.previous();
                                break;
                            }
                            rowCounter++;
                        } while (rowIterator.hasNext());
                        String[] insertionText = convertInsertToStringArray(editInfo, false);
                        for (String insertionText1 : insertionText) {
                            DescendentFileContentRow newRow = new DescendentFileContentRow();
                            newRow.setDescendentLineNumber(descendentLineNumber++);
                            newRow.setDescendentRowType(MergedDescendentFileContentRow.ROWTYPE_INSERT);
                            newRow.setDescendentEditInfo(editInfo);
                            newRow.setDescendentText(insertionText1);
                            rowIterator.add(newRow);
                        }
                        break;
                    }
                    default: {
                        throw new QVCSOperationException("Internal error. Unknown edit type in MergeFrame.java");
                    }
                }
            } else {
                throw new QVCSRuntimeException("Internal error. Existing row not found.");
            }
        }

        // Copy the rest of the file...
        while (rowIterator.hasNext()) {
            existingRow = rowIterator.next();
            existingRow.setDescendentLineNumber(descendentLineNumber++);
        }
    }

    private LinkedList<MergedDescendentFileContentRow> populateFilePanels(final String baseFileName) {
        LinkedList<MergedDescendentFileContentRow> baseFileLinkedList = new LinkedList<>();
        LinkedList<DescendentFileContentRow> firstDecendentLinkedList = new LinkedList<>();
        LinkedList<DescendentFileContentRow> secondDecendentLinkedList = new LinkedList<>();

        try {
            // Populate the linked list that represents the base file...
            if (populateBaseFileLinkedList(baseFileName, baseFileLinkedList)) {
                // Deduce EOL sequence.
                deduceEOLSequence(baseFileLinkedList);

                // Apply the edits from the first decendent to the contents of
                // the ancestor file.
                populateDecendentLinkedList(baseFileLinkedList, firstDecendentLinkedList);
                applyDescendentEdits(firstDecendentLinkedList, firstDecendentEditScript);

                // Apply the edits from the second decendent...
                populateDecendentLinkedList(baseFileLinkedList, secondDecendentLinkedList);
                applyDescendentEdits(secondDecendentLinkedList, secondDecendentEditScript);

                // Merge the edits from the first and second decendents to the contents of
                // the ancestor file.
                mergeDescendentEdits(baseFileLinkedList, firstDecendentLinkedList, secondDecendentLinkedList);

                DescendentFileContentsListModel firstDecendentFileContentsListModel = new DescendentFileContentsListModel(baseFileLinkedList, 1);
                DescendentFileContentsList firstDecendentFileContentsList = new DescendentFileContentsList(firstDecendentFileContentsListModel, 1, this, frameFont);
                rowHeight = firstDecendentFileContentsList.getRowHeight();
                m_LeftFilePanel.add(firstDecendentFileContentsList, BorderLayout.CENTER);

                DescendentFileContentsListModel secondDecendentFileContentsListModel = new DescendentFileContentsListModel(baseFileLinkedList, 2);
                DescendentFileContentsList secondDecendentFileContentsList = new DescendentFileContentsList(secondDecendentFileContentsListModel, 2, this, frameFont);
                m_RightFilePanel.add(secondDecendentFileContentsList, BorderLayout.CENTER);
            } else {
                LOGGER.warn("Unable to read: [{}]", baseFileName);
            }
        } catch (QVCSOperationException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return baseFileLinkedList;
    }

    /**
     * Merge the two descendents into the parent base file.
     *
     * @param resultLinkedList
     * @param firstDecendentLinkedList
     * @param secondDecendentLinkedList
     */
    void mergeDescendentEdits(LinkedList<MergedDescendentFileContentRow> resultLinkedList, LinkedList<DescendentFileContentRow> firstDecendentLinkedList,
            LinkedList<DescendentFileContentRow> secondDecendentLinkedList) {
        mergeFirstDescendent(resultLinkedList, firstDecendentLinkedList);
        mergeSecondDescendent(resultLinkedList, secondDecendentLinkedList);
    }

    void mergeFirstDescendent(LinkedList<MergedDescendentFileContentRow> resultLinkedList, LinkedList<DescendentFileContentRow> firstDecendentLinkedList) {
        ListIterator<MergedDescendentFileContentRow> resultIterator = resultLinkedList.listIterator();
        ListIterator<DescendentFileContentRow> firstDecendentIterator = firstDecendentLinkedList.listIterator();

        try {
            int firstDecendentLineNumber = 1;
            while (resultIterator.hasNext() && firstDecendentIterator.hasNext()) {
                MergedDescendentFileContentRow mergedRow = resultIterator.next();
                DescendentFileContentRow decendentRow = firstDecendentIterator.next();

                switch (decendentRow.getDescendentRowType()) {
                    case MergedDescendentFileContentRow.ROWTYPE_DELETE: {
                        mergedRow.setFirstDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_DELETE);
                        mergedRow.setFirstDecendentText(mergedRow.getAncestorText());
                        mergedRow.setFirstDecendentLineNumber(firstDecendentLineNumber++);
                        mergedRow.setFirstDecendentEditInfo(decendentRow.getDescendentEditInfo());
                        mergedRow.setFirstDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                        break;
                    }
                    case MergedDescendentFileContentRow.ROWTYPE_INSERT: {
                        // Position back to where the insert starts.
                        resultIterator.previous();
                        do {
                            MergedDescendentFileContentRow newRow = new MergedDescendentFileContentRow(frameFont);
                            newRow.setFirstDecendentEditInfo(decendentRow.getDescendentEditInfo());
                            newRow.setFirstDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                            newRow.setFirstDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_INSERT);
                            newRow.setFirstDecendentText(decendentRow.getDescendentText());
                            newRow.setFirstDecendentLineNumber(firstDecendentLineNumber++);
                            newRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_EMPTY);
                            newRow.setSecondDecendentText(emptyString);
                            resultIterator.add(newRow);
                            decendentRow = firstDecendentIterator.next();
                        } while (decendentRow.getDescendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);

                        // Position back one so the subsequent loop call to next() will position us
                        // on the non-Insert row that follows the insert.
                        firstDecendentIterator.previous();
                        break;
                    }
                    default: {
                        mergedRow.setFirstDecendentText(mergedRow.getAncestorText());
                        mergedRow.setFirstDecendentRowType(mergedRow.getAncestorRowType());
                        mergedRow.setFirstDecendentLineNumber(firstDecendentLineNumber++);
                        break;
                    }
                }
            }

            // There are still rows to process from the original file. So the first decendent lines
            // should be blanks. (I'm not sure that this can ever happen for the first decendent, but it
            // definitely can happen for the 2nd decendent, so in order to make the methods symetrical, I include
            // the similar code fragment for the 1st decendent).
            while (resultIterator.hasNext()) {
                MergedDescendentFileContentRow mergedRow = resultIterator.next();
                mergedRow.setFirstDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_EMPTY);
                mergedRow.setFirstDecendentText(emptyString);
            }

            // And add any rows at the end that are inserts from the first decendent...
            while (firstDecendentIterator.hasNext()) {
                DescendentFileContentRow decendentRow = firstDecendentIterator.next();
                MergedDescendentFileContentRow newRow = new MergedDescendentFileContentRow(frameFont);
                newRow.setFirstDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                newRow.setFirstDecendentEditInfo(decendentRow.getDescendentEditInfo());
                newRow.setFirstDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_INSERT);
                newRow.setFirstDecendentText(decendentRow.getDescendentText());
                newRow.setFirstDecendentLineNumber(firstDecendentLineNumber++);
                resultIterator.add(newRow);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    void mergeSecondDescendent(LinkedList<MergedDescendentFileContentRow> resultLinkedList, LinkedList<DescendentFileContentRow> secondDecendentLinkedList) {
        ListIterator<MergedDescendentFileContentRow> resultIterator = resultLinkedList.listIterator();
        ListIterator<DescendentFileContentRow> secondDecendentIterator = secondDecendentLinkedList.listIterator();

        try {
            int secondDecendentLineNumber = 1;
            while (resultIterator.hasNext() && secondDecendentIterator.hasNext()) {
                MergedDescendentFileContentRow mergedRow = resultIterator.next();
                DescendentFileContentRow decendentRow = secondDecendentIterator.next();

                int firstDecendentRowType = mergedRow.getFirstDecendentRowType();
                int secondDecendentRowType = decendentRow.getDescendentRowType();

                if ((secondDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_DELETE)
                        && (firstDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_DELETE)) {
                    assert (decendentRow.getMergedRow().getAncestorOriginalLineNumber() == mergedRow.getAncestorOriginalLineNumber());
                    mergedRow.setSecondDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                    mergedRow.setSecondDecendentEditInfo(decendentRow.getDescendentEditInfo());
                    mergedRow.setSecondDecendentText(decendentRow.getDescendentText());
                    mergedRow.setSecondDecendentLineNumber(secondDecendentLineNumber++);
                    mergedRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_DELETE);
                    mergedRow.setOverlapFlag(true);
                } else if ((secondDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_DELETE)
                        && (firstDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_INSERT)) {
                    // Skip over the inserted rows until we find a non-inserted row, and that should be the row
                    // we can mark as deleted.
                    while (resultIterator.hasNext()) {
                        mergedRow = resultIterator.next();
                        if (mergedRow.getFirstDecendentRowType() != MergedDescendentFileContentRow.ROWTYPE_INSERT) {
                            break;
                        }
                    }
                    assert (decendentRow.getMergedRow().getAncestorOriginalLineNumber() == mergedRow.getAncestorOriginalLineNumber());
                    mergedRow.setSecondDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                    mergedRow.setSecondDecendentEditInfo(decendentRow.getDescendentEditInfo());
                    mergedRow.setSecondDecendentText(decendentRow.getDescendentText());
                    mergedRow.setSecondDecendentLineNumber(secondDecendentLineNumber++);
                    mergedRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_DELETE);
                    mergedRow.setOverlapFlag(true);
                } else if ((secondDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_INSERT)
                        && (firstDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_DELETE)) {
                    // Position back to where the insert occurs.
                    mergedRow = resultIterator.previous();
                    assert (decendentRow.getMergedRow().getAncestorOriginalLineNumber() == mergedRow.getAncestorOriginalLineNumber());
                    do {
                        MergedDescendentFileContentRow newRow = new MergedDescendentFileContentRow(frameFont);
                        newRow.setSecondDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                        newRow.setSecondDecendentEditInfo(decendentRow.getDescendentEditInfo());
                        newRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_INSERT);
                        newRow.setSecondDecendentText(decendentRow.getDescendentText());
                        newRow.setSecondDecendentLineNumber(secondDecendentLineNumber++);
                        newRow.setFirstDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_EMPTY);
                        newRow.setFirstDecendentText(emptyString);
                        newRow.setOverlapFlag(true);
                        resultIterator.add(newRow);
                        decendentRow = secondDecendentIterator.next();
                    } while (decendentRow.getDescendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);

                    // Position back one so the subsequent loop call to next() will position us
                    // on the non-Insert row that follows the insert.
                    secondDecendentIterator.previous();
                } else if ((secondDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_INSERT)
                        && (firstDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_INSERT)) {
                    mergedRow.setSecondDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                    mergedRow.setSecondDecendentEditInfo(decendentRow.getDescendentEditInfo());
                    mergedRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_INSERT);
                    mergedRow.setSecondDecendentText(decendentRow.getDescendentText());
                    mergedRow.setSecondDecendentLineNumber(secondDecendentLineNumber++);
                    mergedRow.setOverlapFlag(true);
                } else {
                    // No collision...

                    // We have to skip over any insertion rows.
                    if (firstDecendentRowType == MergedDescendentFileContentRow.ROWTYPE_INSERT) {
                        while (resultIterator.hasNext()) {
                            mergedRow = resultIterator.next();
                            if (mergedRow.getFirstDecendentRowType() != MergedDescendentFileContentRow.ROWTYPE_INSERT) {
                                break;
                            }
                        }
                    }
                    switch (decendentRow.getDescendentRowType()) {
                        case MergedDescendentFileContentRow.ROWTYPE_DELETE: {
                            assert (decendentRow.getMergedRow().getAncestorOriginalLineNumber() == mergedRow.getAncestorOriginalLineNumber());
                            mergedRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_DELETE);
                            mergedRow.setSecondDecendentEditInfo(decendentRow.getDescendentEditInfo());
                            mergedRow.setSecondDecendentText(mergedRow.getAncestorText());
                            mergedRow.setSecondDecendentLineNumber(secondDecendentLineNumber++);
                            mergedRow.setSecondDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                            break;
                        }
                        case MergedDescendentFileContentRow.ROWTYPE_INSERT: {
                            // Position back to where the insert starts.
                            mergedRow = resultIterator.previous();
                            assert (decendentRow.getMergedRow().getAncestorOriginalLineNumber() == mergedRow.getAncestorOriginalLineNumber());
                            do {
                                MergedDescendentFileContentRow newRow = new MergedDescendentFileContentRow(frameFont);
                                newRow.setSecondDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                                newRow.setSecondDecendentEditInfo(decendentRow.getDescendentEditInfo());
                                newRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_INSERT);
                                newRow.setSecondDecendentText(decendentRow.getDescendentText());
                                newRow.setSecondDecendentLineNumber(secondDecendentLineNumber++);
                                newRow.setFirstDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_EMPTY);
                                newRow.setFirstDecendentText(emptyString);
                                resultIterator.add(newRow);
                                decendentRow = secondDecendentIterator.next();
                            } while (decendentRow.getDescendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);

                            // Position back one so the subsequent loop call to next() will position us
                            // on the non-Insert row that follows the insert.
                            secondDecendentIterator.previous();
                            break;
                        }
                        default: {
                            mergedRow.setSecondDecendentText(mergedRow.getAncestorText());
                            mergedRow.setSecondDecendentRowType(mergedRow.getAncestorRowType());
                            mergedRow.setSecondDecendentLineNumber(secondDecendentLineNumber++);
                            break;
                        }
                    }
                }
            }

            // There are still some rows left in the 'original' file. We need to mark the 2nd
            // decendent of these rows as empty.
            while (resultIterator.hasNext()) {
                MergedDescendentFileContentRow mergedRow = resultIterator.next();
                mergedRow.setSecondDecendentText(emptyString);
                mergedRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_EMPTY);
            }

            // And add any rows at the end that are inserts from the second decendent...
            while (secondDecendentIterator.hasNext()) {
                DescendentFileContentRow decendentRow = secondDecendentIterator.next();
                MergedDescendentFileContentRow newRow = new MergedDescendentFileContentRow(frameFont);
                newRow.setSecondDecendentCheckBoxVisibleFlag(decendentRow.getDescendentFirstRowOfEditFlag());
                newRow.setSecondDecendentEditInfo(decendentRow.getDescendentEditInfo());
                newRow.setSecondDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_INSERT);
                newRow.setSecondDecendentText(decendentRow.getDescendentText());
                newRow.setSecondDecendentLineNumber(secondDecendentLineNumber++);
                newRow.setFirstDecendentText(emptyString);
                newRow.setFirstDecendentRowType(MergedDescendentFileContentRow.ROWTYPE_EMPTY);
                resultIterator.add(newRow);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    String[] convertInsertToStringArray(EditInfo editInfo, boolean appendToFileEndFlag) {
        String stringToConvert = new String(editInfo.getInsertedBytes());
        String[] stringArray = stringToConvert.split("\r\n|\r|\n", -1);

        if (!appendToFileEndFlag && stringArray[stringArray.length - 1].length() == 0) {
            // We need to trim off the last array element because the split
            // puts the trailing newline as a separate element.
            String[] shortenedStringArray = new String[stringArray.length - 1];
            System.arraycopy(stringArray, 0, shortenedStringArray, 0, shortenedStringArray.length);
            stringArray = shortenedStringArray;
        }

        return stringArray;
    }

    String formatLine(String line) {
        if (line.length() == 0) {
            return emptyString; // So something shows up on the list control.

        } else {
            return line;
        }
    }

    class EscapeAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        EscapeAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
        }
    }

    class OurViewportChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == leftScrollPaneViewPort) {
                rightScrollPaneViewPort.repaint();
                rightScrollPaneViewPort.setViewPosition(leftScrollPaneViewPort.getViewPosition());
                ancestorScrollPaneViewPort.repaint();
                ancestorScrollPaneViewPort.setViewPosition(leftScrollPaneViewPort.getViewPosition());
            } else if (e.getSource() == rightScrollPaneViewPort) {
                leftScrollPaneViewPort.repaint();
                leftScrollPaneViewPort.setViewPosition(rightScrollPaneViewPort.getViewPosition());
                ancestorScrollPaneViewPort.repaint();
                ancestorScrollPaneViewPort.setViewPosition(rightScrollPaneViewPort.getViewPosition());
            } else if (e.getSource() == ancestorScrollPaneViewPort) {
                rightScrollPaneViewPort.repaint();
                rightScrollPaneViewPort.setViewPosition(ancestorScrollPaneViewPort.getViewPosition());
                leftScrollPaneViewPort.repaint();
                leftScrollPaneViewPort.setViewPosition(ancestorScrollPaneViewPort.getViewPosition());
            }
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JButton m_ApplyLeftEditButton;
    private javax.swing.JButton m_ApplyNonCollidingEditsButton;
    private javax.swing.JButton m_ApplyRightEditButton;
    private javax.swing.JPanel m_FirstDecendentStatusPanel;
    private javax.swing.JToolBar.Separator m_FirstToolbarSeparator;
    private javax.swing.JLabel m_LeftFileLabel;
    private javax.swing.JPanel m_LeftFilePanel;
    private javax.swing.JPanel m_LeftFileParentPanel;
    private javax.swing.JScrollPane m_LeftFileScrollPane;
    private javax.swing.JSplitPane m_LeftRightSplitPane;
    private javax.swing.JToolBar m_MergeFrameToolBar;
    private javax.swing.JPanel m_MergeResultParentPanel;
    private javax.swing.JLabel m_MergedResultLabel;
    private javax.swing.JPanel m_MergedResultPanel;
    private javax.swing.JScrollPane m_MergedResultScrollPane;
    private javax.swing.JButton m_MoveToNextLeftEditButton;
    private javax.swing.JButton m_MoveToNextRightEditButton;
    private javax.swing.JButton m_MoveToPreviousLeftEditButton;
    private javax.swing.JButton m_MoveToPreviousRightEditButton;
    private javax.swing.JSplitPane m_ParentSplitPane;
    private javax.swing.JLabel m_RightFileLabel;
    private javax.swing.JPanel m_RightFilePanel;
    private javax.swing.JPanel m_RightFileParentPanel;
    private javax.swing.JScrollPane m_RightFileScrollPane;
    private javax.swing.JButton m_SaveAsButton;
    private javax.swing.JButton m_SaveChangesButton;
    private javax.swing.JPanel m_SecondDecendentStatusPanel;
    private javax.swing.JToolBar.Separator m_SecondToolbarSeparator;
    private javax.swing.JToolBar.Separator m_ThirdToolbarSeparator;
// End of variables declaration//GEN-END:variables
}
