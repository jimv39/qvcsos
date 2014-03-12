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

import com.qumasoft.qvcslib.CheckInCommentProperties;
import com.qumasoft.qvcslib.CheckOutCommentManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Check in dialog.
 * @author  Jim Voris
 */
public final class CheckInDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = 4724888555214698512L;

    private boolean isOKFlag;
    private int checkInCommentIndex = 0;
    private final QWinFrame parentFrame;
    private final List selectedFiles;
    private final OperationCheckInArchive operationCheckInArchive;
    private final CheckInCommentProperties checkInCommentProperties;

    /**
     * Create a check in dialog.
     * @param parent the parent frame.
     * @param files the list of selected files.
     * @param checkInComments the check in comment properties
     * @param modal is this modal.
     * @param operation the check in operation that will do the work.
     */
    public CheckInDialog(java.awt.Frame parent, List files, CheckInCommentProperties checkInComments, boolean modal, OperationCheckInArchive operation) {
        super(parent, modal);
        selectedFiles = files;
        parentFrame = (QWinFrame) parent;
        operationCheckInArchive = operation;
        initComponents();
        getRootPane().setDefaultButton(okButton);
        checkInCommentProperties = checkInComments;
        descriptionOfChangesTextArea.requestFocusInWindow();
        initCheckInComment(files, checkInComments);

        // Decide whether to enable the compare button.  Only enabled if
        // we have a single file that we are checking in.
        if (files.size() == 1) {
            compareButton.setEnabled(true);
            workfileLocationButton.setEnabled(true);
        } else {
            compareButton.setEnabled(false);
            workfileLocationButton.setEnabled(false);
        }

        // If we are checking in on a writeable view, we need to disable some
        // controls
        disableReadWriteViewControls();

        labelComboBox.setModel(new LabelsComboModel());
        setFont();
        center();
    }

    private void initCheckInComment(List fileList, CheckInCommentProperties checkInComments) {
        String checkInComment;
        if (fileList.size() == 1) {
            MergedInfoInterface mergedInfo = (MergedInfoInterface) fileList.get(0);
            if (CheckOutCommentManager.getInstance().commentExists(mergedInfo)) {
                checkInComment = CheckOutCommentManager.getInstance().lookupComment(mergedInfo);
            } else {
                checkInComment = checkInComments.getCheckInComment(0);
            }
        } else {
            checkInComment = checkInComments.getCheckInComment(0);
        }

        if (checkInComment != null) {
            descriptionOfChangesTextArea.setText(checkInComment);
            descriptionOfChangesTextArea.select(0, checkInComment.length());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        parentPanel = new javax.swing.JPanel();
        northPanel = new javax.swing.JPanel();
        workfileLocationLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        workfileLocation = new javax.swing.JTextField();
        workfileLocationButton = new javax.swing.JButton();
        eastPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        compareButton = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        descriptionOfChangesLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionOfChangesTextArea = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        southPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        leaveCheckedOutCheckBox = new javax.swing.JCheckBox();
        protectWorkfileCheckBox = new javax.swing.JCheckBox();
        forceBranchCheckBox = new javax.swing.JCheckBox();
        noExpandKeywordsCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        checkInBehaviorComboBox = new javax.swing.JComboBox();
        applyLabelCheckBox = new javax.swing.JCheckBox();
        floatLabelCheckBox = new javax.swing.JCheckBox();
        reUseLabelCheckBox = new javax.swing.JCheckBox();
        labelComboBox = new javax.swing.JComboBox();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();

        setTitle("Check In");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setName("CheckIn"); // NOI18N

        parentPanel.setLayout(new java.awt.BorderLayout(5, 5));

        northPanel.setLayout(new java.awt.GridLayout(2, 0, 0, 5));

        workfileLocationLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        workfileLocationLabel.setText("From the workfile");
        northPanel.add(workfileLocationLabel);

        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));

        workfileLocation.setEditable(false);
        jPanel5.add(workfileLocation);

        workfileLocationButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        workfileLocationButton.setText("...");
        workfileLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                workfileLocationButtonActionPerformed(evt);
            }
        });
        jPanel5.add(workfileLocationButton);

        northPanel.add(jPanel5);

        parentPanel.add(northPanel, java.awt.BorderLayout.NORTH);

        eastPanel.setLayout(new javax.swing.BoxLayout(eastPanel, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        okButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        okButton.setText("   OK   ");
        okButton.setMaximumSize(new java.awt.Dimension(95, 29));
        okButton.setMinimumSize(new java.awt.Dimension(95, 29));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        jPanel4.add(okButton);

        cancelButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        cancelButton.setText("Cancel");
        cancelButton.setDefaultCapable(false);
        cancelButton.setMaximumSize(new java.awt.Dimension(95, 29));
        cancelButton.setMinimumSize(new java.awt.Dimension(95, 29));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel4.add(cancelButton);

        compareButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        compareButton.setMnemonic('p');
        compareButton.setText("Compare");
        compareButton.setDefaultCapable(false);
        compareButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compareButtonActionPerformed(evt);
            }
        });
        jPanel4.add(compareButton);

        eastPanel.add(jPanel4);

        parentPanel.add(eastPanel, java.awt.BorderLayout.EAST);

        centerPanel.setLayout(new javax.swing.BoxLayout(centerPanel, javax.swing.BoxLayout.Y_AXIS));

        jPanel7.setLayout(new java.awt.GridLayout(1, 0));

        descriptionOfChangesLabel.setDisplayedMnemonic('d');
        descriptionOfChangesLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        descriptionOfChangesLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        descriptionOfChangesLabel.setLabelFor(descriptionOfChangesTextArea);
        descriptionOfChangesLabel.setText("Description of changes:");
        descriptionOfChangesLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        descriptionOfChangesLabel.setDoubleBuffered(true);
        descriptionOfChangesLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        descriptionOfChangesLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel7.add(descriptionOfChangesLabel);

        centerPanel.add(jPanel7);

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setDoubleBuffered(true);

        descriptionOfChangesTextArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        descriptionOfChangesTextArea.setLineWrap(true);
        descriptionOfChangesTextArea.setRows(10);
        descriptionOfChangesTextArea.setTabSize(4);
        descriptionOfChangesTextArea.setWrapStyleWord(true);
        descriptionOfChangesTextArea.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        descriptionOfChangesTextArea.setDoubleBuffered(true);
        descriptionOfChangesTextArea.setMaximumSize(new java.awt.Dimension(102, 120));
        descriptionOfChangesTextArea.setMinimumSize(new java.awt.Dimension(102, 120));
        jScrollPane1.setViewportView(descriptionOfChangesTextArea);

        centerPanel.add(jScrollPane1);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 162, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 29, Short.MAX_VALUE)
        );

        jPanel3.add(jPanel8);

        nextButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        nextButton.setMnemonic('x');
        nextButton.setText("Next");
        nextButton.setToolTipText("Next CheckIn Comment");
        nextButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        jPanel3.add(nextButton);

        prevButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        prevButton.setMnemonic('v');
        prevButton.setText("Prev");
        prevButton.setToolTipText("Previous CheckIn Comment");
        prevButton.setEnabled(false);
        prevButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        jPanel3.add(prevButton);

        clearButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        clearButton.setMnemonic('c');
        clearButton.setText("Clear");
        clearButton.setToolTipText("Clear CheckIn Comment");
        clearButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        jPanel3.add(clearButton);

        centerPanel.add(jPanel3);

        parentPanel.add(centerPanel, java.awt.BorderLayout.CENTER);

        southPanel.setLayout(new java.awt.GridLayout(1, 0));

        jPanel1.setLayout(new java.awt.GridLayout(4, 0));

        leaveCheckedOutCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        leaveCheckedOutCheckBox.setMnemonic('o');
        leaveCheckedOutCheckBox.setText("Leave checked out");
        jPanel1.add(leaveCheckedOutCheckBox);

        protectWorkfileCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        protectWorkfileCheckBox.setMnemonic('w');
        protectWorkfileCheckBox.setText("Protect work file");
        protectWorkfileCheckBox.setToolTipText("Make the workfile readonly after checking in this revision.");
        jPanel1.add(protectWorkfileCheckBox);

        forceBranchCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        forceBranchCheckBox.setMnemonic('b');
        forceBranchCheckBox.setText("Force a branch");
        jPanel1.add(forceBranchCheckBox);

        noExpandKeywordsCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        noExpandKeywordsCheckBox.setMnemonic('n');
        noExpandKeywordsCheckBox.setText("Do not expand keywords after checkin");
        noExpandKeywordsCheckBox.setToolTipText("Enable to leave keywords in their unexpanded state.");
        jPanel1.add(noExpandKeywordsCheckBox);

        southPanel.add(jPanel1);

        jPanel2.setLayout(new java.awt.GridLayout(5, 0));

        checkInBehaviorComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        checkInBehaviorComboBox.setMaximumRowCount(3);
        checkInBehaviorComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Add a new revision only if different", "Always add a new revision"}));
        jPanel2.add(checkInBehaviorComboBox);

        applyLabelCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        applyLabelCheckBox.setMnemonic('l');
        applyLabelCheckBox.setText("Apply label");
        applyLabelCheckBox.setToolTipText("Apply a label to this new revision");
        applyLabelCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyLabelCheckBoxActionPerformed(evt);
            }
        });
        jPanel2.add(applyLabelCheckBox);

        floatLabelCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        floatLabelCheckBox.setMnemonic('f');
        floatLabelCheckBox.setText("Float label");
        floatLabelCheckBox.setToolTipText("Make this label a floating label");
        floatLabelCheckBox.setEnabled(false);
        jPanel2.add(floatLabelCheckBox);

        reUseLabelCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        reUseLabelCheckBox.setMnemonic('u');
        reUseLabelCheckBox.setText("Re-use existing label");
        reUseLabelCheckBox.setToolTipText("If the label string is already in use, have label point to selected revision instead of to the revision that it currently identifies.");
        reUseLabelCheckBox.setEnabled(false);
        jPanel2.add(reUseLabelCheckBox);

        labelComboBox.setEditable(true);
        labelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelComboBox.setMaximumRowCount(10);
        labelComboBox.setToolTipText("Select or define the label to apply");
        labelComboBox.setEnabled(false);
        jPanel2.add(labelComboBox);

        southPanel.add(jPanel2);

        parentPanel.add(southPanel, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(parentPanel, java.awt.BorderLayout.CENTER);
        getContentPane().add(jPanel9, java.awt.BorderLayout.NORTH);
        getContentPane().add(jPanel10, java.awt.BorderLayout.SOUTH);
        getContentPane().add(jPanel11, java.awt.BorderLayout.WEST);
        getContentPane().add(jPanel12, java.awt.BorderLayout.EAST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void applyLabelCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyLabelCheckBoxActionPerformed
    {//GEN-HEADEREND:event_applyLabelCheckBoxActionPerformed
        // Enable/disable the text are used for the label.
        Object selectedObject = applyLabelCheckBox.getSelectedObjects();
        if (selectedObject == null) {
            labelComboBox.setEnabled(false);
            floatLabelCheckBox.setEnabled(false);
            reUseLabelCheckBox.setEnabled(false);
        } else {
            labelComboBox.setEnabled(true);
            floatLabelCheckBox.setEnabled(true);
            reUseLabelCheckBox.setEnabled(true);
        }
    }//GEN-LAST:event_applyLabelCheckBoxActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearButtonActionPerformed
    {//GEN-HEADEREND:event_clearButtonActionPerformed
        // Clear the checkin comment.
        setChangesDescription(null);
        descriptionOfChangesTextArea.requestFocusInWindow();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevButtonActionPerformed
    {//GEN-HEADEREND:event_prevButtonActionPerformed
        // Display the previous checkin comment.
        if (checkInCommentIndex > 0) {
            checkInCommentIndex--;
            setChangesDescription(checkInCommentProperties.getCheckInComment(checkInCommentIndex));
            nextButton.setEnabled(true);
            descriptionOfChangesTextArea.requestFocusInWindow();
        }
        if (checkInCommentIndex == 0) {
            // Disable the next button.
            prevButton.setEnabled(false);
        }
    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextButtonActionPerformed
    {//GEN-HEADEREND:event_nextButtonActionPerformed
        // Display the next check-in comment.
        int knownComments = checkInCommentProperties.getCommentCount();
        if (checkInCommentIndex < (knownComments - 1)) {
            checkInCommentIndex++;
            setChangesDescription(checkInCommentProperties.getCheckInComment(checkInCommentIndex));
            prevButton.setEnabled(true);
            descriptionOfChangesTextArea.requestFocusInWindow();
        }
        if (checkInCommentIndex == (knownComments - 1)) {
            // Disable the next button.
            nextButton.setEnabled(false);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void workfileLocationButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_workfileLocationButtonActionPerformed
    {//GEN-HEADEREND:event_workfileLocationButtonActionPerformed
        setWorkfileLocation(selectFile(getWorkfileLocation(), "Select the workfile to check-in:"));
    }//GEN-LAST:event_workfileLocationButtonActionPerformed

    private void compareButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compareButtonActionPerformed
    {//GEN-HEADEREND:event_compareButtonActionPerformed
        parentFrame.operationVisualCompare();
    }//GEN-LAST:event_compareButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        isOKFlag = false;
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        if (getApplyLabelFlag()) {
            if (getLabelString() == null || getLabelString().length() == 0) {
                // Let the user know that a label string is required.
                JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "Please enter a label.",  "Label String Required", JOptionPane.WARNING_MESSAGE);
                labelComboBox.requestFocusInWindow();
                return;
            }
        }
        isOKFlag = true;
        checkInCommentProperties.addCheckInComment(descriptionOfChangesTextArea.getText());
        closeDialog(null);
        operationCheckInArchive.processDialogResult(selectedFiles, this);
    }//GEN-LAST:event_okButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt)
    {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    @Override
    public void dismissDialog() {
        cancelButtonActionPerformed(null);
    }

    void setWorkfileLocation(String location) {
        this.workfileLocation.setText(location);
    }

    String getWorkfileLocation() {
        return workfileLocation.getText();
    }

    void setChangesDescription(String changes) {
        descriptionOfChangesTextArea.setText(changes);
    }

    String getChangesDescription() {
        return descriptionOfChangesTextArea.getText();
    }

    String getLabelString() {
        String labelString = (String) labelComboBox.getModel().getSelectedItem();
        return labelString;
    }

    boolean getIsOK() {
        return isOKFlag;
    }

    boolean getLockFlag() {
        boolean retVal = false;
        Object leaveLocked = leaveCheckedOutCheckBox.getSelectedObjects();
        if (leaveLocked != null) {
            retVal = true;
        }
        return retVal;
    }

    boolean getForceBranchFlag() {
        boolean retVal = false;
        Object forceBranch = forceBranchCheckBox.getSelectedObjects();
        if (forceBranch != null) {
            retVal = true;
        }
        return retVal;
    }

    boolean getApplyLabelFlag() {
        boolean retVal = false;
        Object applyLabel = applyLabelCheckBox.getSelectedObjects();
        if (applyLabel != null) {
            retVal = true;
        }
        return retVal;
    }

    boolean getFloatLabelFlag() {
        boolean retVal = false;
        Object floatLabel = floatLabelCheckBox.getSelectedObjects();
        if (floatLabel != null) {
            retVal = true;
        }
        return retVal;
    }

    boolean getReUseLabelFlag() {
        boolean retVal = false;
        Object reUseLabel = reUseLabelCheckBox.getSelectedObjects();
        if (reUseLabel != null) {
            retVal = true;
        }
        return retVal;
    }

    boolean getProtectWorkfileFlag() {
        boolean retVal = false;
        Object protectWorkfile = protectWorkfileCheckBox.getSelectedObjects();
        if (protectWorkfile != null) {
            retVal = true;
        }
        return retVal;
    }

    boolean getNoExpandKeywordsFlag() {
        boolean retVal = false;
        Object noExpandKeywords = noExpandKeywordsCheckBox.getSelectedObjects();
        if (noExpandKeywords != null) {
            retVal = true;
        }
        return retVal;
    }

    boolean getCreateNewRevisionIfEqual() {
        boolean retVal = false;
        String selection = (String) checkInBehaviorComboBox.getModel().getSelectedItem();
        if (selection.equals("Always add a new revision")) {
            retVal = true;
        }
        return retVal;
    }

    // Disable some controls for a non-Trunk read-write view.
    private void disableReadWriteViewControls() {
        ViewTreeNode viewTreeNode = parentFrame.getTreeControl().getActiveViewNode();
        String viewName = viewTreeNode.getViewName();
        if (0 != viewName.compareTo(QVCSConstants.QVCS_TRUNK_VIEW)) {
            if (viewTreeNode.isReadWriteView()) {
                applyLabelCheckBox.setEnabled(false);
                forceBranchCheckBox.setEnabled(false);
            }
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox applyLabelCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JComboBox checkInBehaviorComboBox;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton compareButton;
    private javax.swing.JLabel descriptionOfChangesLabel;
    private javax.swing.JTextArea descriptionOfChangesTextArea;
    private javax.swing.JPanel eastPanel;
    private javax.swing.JCheckBox floatLabelCheckBox;
    private javax.swing.JCheckBox forceBranchCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox labelComboBox;
    private javax.swing.JCheckBox leaveCheckedOutCheckBox;
    private javax.swing.JButton nextButton;
    private javax.swing.JCheckBox noExpandKeywordsCheckBox;
    private javax.swing.JPanel northPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel parentPanel;
    private javax.swing.JButton prevButton;
    private javax.swing.JCheckBox protectWorkfileCheckBox;
    private javax.swing.JCheckBox reUseLabelCheckBox;
    private javax.swing.JPanel southPanel;
    private javax.swing.JTextField workfileLocation;
    private javax.swing.JButton workfileLocationButton;
    private javax.swing.JLabel workfileLocationLabel;
// End of variables declaration//GEN-END:variables
}