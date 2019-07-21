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
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.guitools.qwin.BranchTreeNode;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.operation.OperationCheckOutArchive;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import java.util.List;

/**
 * Checkout revision dialog.
 * @author  Jim Voris
 */
public class CheckOutRevisionDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = 5674377879436461255L;

//    private boolean m_isOK;
    private final List selectedFiles;
    private final OperationCheckOutArchive operationCheckOut;

    /**
     * Create a new checkout revision dialog.
     * @param parent the parent frame.
     * @param files the list of selected files.
     * @param operation the checkout operation that will do the work.
     */
    public CheckOutRevisionDialog(java.awt.Frame parent, List files, OperationCheckOutArchive operation) {
        super(parent, true);
        operationCheckOut = operation;
        selectedFiles = files;
        initComponents();
        populateComponents();

        // If we are checking out on a writeable view, we need to disable some
        // controls
        disableReadWriteViewControls();

        setFont();
        center();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        revisionToCheckoutLabel = new javax.swing.JLabel();
        revisionToCheckoutComboBox = new javax.swing.JComboBox();
        byLabelCheckBox = new javax.swing.JCheckBox();
        byLabelComboBox = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        overwriteWorkfileComboBox = new javax.swing.JComboBox();
        commentLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        commentTextPane = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Check out revision");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        revisionToCheckoutLabel.setDisplayedMnemonic('r');
        revisionToCheckoutLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        revisionToCheckoutLabel.setLabelFor(revisionToCheckoutComboBox);
        revisionToCheckoutLabel.setText("Revision to checkout:");
        revisionToCheckoutLabel.setToolTipText("");

        revisionToCheckoutComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        revisionToCheckoutComboBox.setMaximumRowCount(10);
        revisionToCheckoutComboBox.setToolTipText("Select the revision to get");

        byLabelCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        byLabelCheckBox.setMnemonic('l');
        byLabelCheckBox.setText("Checkout by label");
        byLabelCheckBox.setToolTipText("Enable to retrieve by label instead of by revision");
        byLabelCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                byLabelCheckBoxActionPerformed(evt);
            }
        });

        byLabelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        byLabelComboBox.setMaximumRowCount(10);
        byLabelComboBox.setToolTipText("");
        byLabelComboBox.setEnabled(false);

        okButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        okButton.setText("   OK   ");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        overwriteWorkfileComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        overwriteWorkfileComboBox.setMaximumRowCount(10);
        overwriteWorkfileComboBox.setToolTipText("");

        commentLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        commentLabel.setText("Comment:");

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setDoubleBuffered(true);

        commentTextPane.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jScrollPane1.setViewportView(commentTextPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(commentLabel)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 350, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(overwriteWorkfileComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 350, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, revisionToCheckoutLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, revisionToCheckoutComboBox, 0, 350, Short.MAX_VALUE))
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(layout.createSequentialGroup()
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, byLabelCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, byLabelComboBox, 0, 350, Short.MAX_VALUE)))
                        .addContainerGap(15, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(revisionToCheckoutLabel)
                        .add(4, 4, 4)
                        .add(revisionToCheckoutComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(3, 3, 3)
                        .add(commentLabel)
                        .add(8, 8, 8)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(overwriteWorkfileComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(13, 13, 13)
                        .add(byLabelCheckBox)
                        .add(7, 7, 7)
                        .add(byLabelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(13, 13, 13)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(cancelButton)
                                .add(okButton))
                        .add(13, 13, 13))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void byLabelCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_byLabelCheckBoxActionPerformed
    {//GEN-HEADEREND:event_byLabelCheckBoxActionPerformed
        // Add your handling code here:
        Object byLabel = byLabelCheckBox.getSelectedObjects();
        if (byLabel != null) {
            byLabelComboBox.setEnabled(true);
            revisionToCheckoutComboBox.setEnabled(false);
            byLabelComboBox.requestFocusInWindow();
        } else {
            byLabelComboBox.setEnabled(false);
            revisionToCheckoutComboBox.setEnabled(true);
        }
    }//GEN-LAST:event_byLabelCheckBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        CheckOutCommandArgs commandArgs = new CheckOutCommandArgs();
        commandArgs.setRevisionString(getRevisionString());
        commandArgs.setLabel(getLabelString());
        setOverwriteAnswer();
        operationCheckOut.setCheckOutComment(getCheckOutComment());

        closeDialog(null);
        operationCheckOut.completeOperation(selectedFiles, commandArgs);
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

    private void populateComponents() {
        if (selectedFiles.size() == 1) {
            revisionToCheckoutComboBox.setModel(new RevisionsComboModel((MergedInfoInterface) selectedFiles.get(0)));
            byLabelComboBox.setModel(new LabelsComboModel((MergedInfoInterface) selectedFiles.get(0)));
        } else if (selectedFiles.size() > 1) {
            revisionToCheckoutComboBox.setModel(new RevisionsComboModel(null));
            byLabelComboBox.setModel(new LabelsComboModel());
        }
        overwriteWorkfileComboBox.setModel(new OverwriteWorkfileComboModel());
        commentTextPane.requestFocusInWindow();
    }

    private String getRevisionString() {
        String revisionString = null;
        Object byLabel = byLabelCheckBox.getSelectedObjects();
        if (byLabel == null) {
            revisionString = (String) revisionToCheckoutComboBox.getModel().getSelectedItem();
        }
        return revisionString;
    }

    private String getLabelString() {
        String labelString = null;
        Object byLabel = byLabelCheckBox.getSelectedObjects();
        if (byLabel != null) {
            labelString = (String) byLabelComboBox.getModel().getSelectedItem();
        }
        return labelString;
    }

    private void setOverwriteAnswer() {
        String answer;
        Object selectedAnswer = overwriteWorkfileComboBox.getSelectedObjects();
        if (selectedAnswer != null) {
            answer = (String) overwriteWorkfileComboBox.getModel().getSelectedItem();
        } else {
            answer = OverwriteWorkfileComboModel.ASK_BEFORE_OVERWRITE;
        }

        // Based on the selection, set the appropriate values on the operation
        // object.
        if (answer.equals(OverwriteWorkfileComboModel.DO_NOT_REPLACE_WORKFILE)) {
            operationCheckOut.setOverwriteAnswerHasBeenCaptured(true);
            operationCheckOut.setOverwriteAnswer(false);
        } else if (answer.equals(OverwriteWorkfileComboModel.REPLACE_WORKFILE)) {
            operationCheckOut.setOverwriteAnswerHasBeenCaptured(true);
            operationCheckOut.setOverwriteAnswer(true);
        }
    }

    private String getCheckOutComment() {
        String text = commentTextPane.getText();
        if (text == null) {
            text = "";
        }
        return text;
    }

    private void disableReadWriteViewControls() {
        BranchTreeNode viewTreeNode = QWinFrame.getQWinFrame().getTreeControl().getActiveBranchNode();
        String viewName = viewTreeNode.getBranchName();
        if (0 != viewName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            if (viewTreeNode.isReadWriteBranch()) {
                revisionToCheckoutComboBox.setEnabled(false);
                byLabelCheckBox.setEnabled(false);
            }
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox byLabelCheckBox;
    private javax.swing.JComboBox byLabelComboBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JTextPane commentTextPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox overwriteWorkfileComboBox;
    private javax.swing.JComboBox revisionToCheckoutComboBox;
    private javax.swing.JLabel revisionToCheckoutLabel;
// End of variables declaration//GEN-END:variables
}
