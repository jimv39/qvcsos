/*   Copyright 2004-2014 Jim Voris
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

import com.qumasoft.guitools.qwin.operation.OperationLabelDirectory;
import com.qumasoft.qvcslib.LogFileOperationLabelDirectoryCommandArgs;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Label directory dialog.
 * @author Jim Voris
 */
public class LabelDirectoryDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -258846426776526900L;

    private final OperationLabelDirectory operationLabelDirectory;

    /**
     * Create a label directory dialog.
     * @param parent the parent frame.
     * @param operation the label directory operation that will do the work.
     */
    public LabelDirectoryDialog(java.awt.Frame parent, OperationLabelDirectory operation) {
        super(parent, true);
        initComponents();
        this.operationLabelDirectory = operation;
        populateComponents();
        setFont();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        withLabelLabel = new javax.swing.JLabel();
        labelComboBox = new javax.swing.JComboBox();
        recurseDirectoriesCheckBox = new javax.swing.JCheckBox();
        floatingLabelCheckBox = new javax.swing.JCheckBox();
        reUseLabelCheckBox = new javax.swing.JCheckBox();
        duplicateLabelCheckBox = new javax.swing.JCheckBox();
        duplicateLabelComboBox = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Apply Label to directory");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        withLabelLabel.setDisplayedMnemonic('w');
        withLabelLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        withLabelLabel.setText("With the label:");

        labelComboBox.setEditable(true);
        labelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelComboBox.setMaximumRowCount(20);

        recurseDirectoriesCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        recurseDirectoriesCheckBox.setMnemonic('R');
        recurseDirectoriesCheckBox.setText("Recurse Directories");
        recurseDirectoriesCheckBox.setToolTipText("Enable to recurse into the directory tree.");
        recurseDirectoriesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recurseDirectoriesCheckBoxActionPerformed(evt);
            }
        });

        floatingLabelCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        floatingLabelCheckBox.setMnemonic('f');
        floatingLabelCheckBox.setText("Make this a floating label");
        floatingLabelCheckBox.setToolTipText("Floating labels always point to the tip revision");

        reUseLabelCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        reUseLabelCheckBox.setMnemonic('u');
        reUseLabelCheckBox.setText("Re-use existing label");
        reUseLabelCheckBox.setToolTipText("If the label string is already in use, have label point to selected revision instead of to the revision that it currently identifies.");

        duplicateLabelCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        duplicateLabelCheckBox.setMnemonic('d');
        duplicateLabelCheckBox.setText("Duplicate this label:");
        duplicateLabelCheckBox.setToolTipText("Select an existing label. The new label will point to the same revision as the existing label.");
        duplicateLabelCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateLabelCheckBoxActionPerformed(evt);
            }
        });

        duplicateLabelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        duplicateLabelComboBox.setMaximumRowCount(10);
        duplicateLabelComboBox.setToolTipText("");
        duplicateLabelComboBox.setEnabled(false);

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(withLabelLabel)
                                .add(labelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 342, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                        .add(duplicateLabelCheckBox)
                                        .addContainerGap())
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(recurseDirectoriesCheckBox)
                                                .addContainerGap())
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(layout.createSequentialGroup()
                                                        .add(floatingLabelCheckBox)
                                                        .addContainerGap())
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(layout.createSequentialGroup()
                                                                .add(reUseLabelCheckBox)
                                                                .addContainerGap())
                                                        .add(layout.createSequentialGroup()
                                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                                                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 135, Short.MAX_VALUE)
                                                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                                        .add(org.jdesktop.layout.GroupLayout.LEADING, duplicateLabelComboBox, 0, 335, Short.MAX_VALUE))
                                                                .addContainerGap(9, Short.MAX_VALUE)))))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(withLabelLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(recurseDirectoriesCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(floatingLabelCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(reUseLabelCheckBox)
                        .add(12, 12, 12)
                        .add(duplicateLabelCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(duplicateLabelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(okButton)
                                .add(cancelButton))
                        .add(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void recurseDirectoriesCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_recurseDirectoriesCheckBoxActionPerformed
    {//GEN-HEADEREND:event_recurseDirectoriesCheckBoxActionPerformed
        Object recurseFlag = recurseDirectoriesCheckBox.getSelectedObjects();
        if (recurseFlag != null) {
            QWinFrame.getQWinFrame().setRecurseFlag(true);
        } else {
            QWinFrame.getQWinFrame().setRecurseFlag(false);
        }
    }//GEN-LAST:event_recurseDirectoriesCheckBoxActionPerformed

    private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
    {//GEN-HEADEREND:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        try {
            validateData();
            closeDialog(null);
            LogFileOperationLabelDirectoryCommandArgs commandArgs = new LogFileOperationLabelDirectoryCommandArgs();

            commandArgs.setDuplicateFlag(getDuplicateLabelFlag());
            commandArgs.setExistingLabelString(getDuplicateLabelString());
            commandArgs.setFloatingFlag(getFloatingLabelFlag());
            commandArgs.setNewLabelString(getLabelString());
            commandArgs.setRecurseFlag(getRecurseFlag());
            commandArgs.setReuseLabelFlag(getReUseLabelFlag());
            commandArgs.setUserName(QWinFrame.getQWinFrame().getLoggedInUserName());
            operationLabelDirectory.completeOperation(commandArgs);
        } catch (final QVCSException e) {
            // Run the update on the Swing thread.
            Runnable later = new Runnable() {

                @Override
                public void run() {
                    // Let the user know that there was a problem.
                    JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), e.getLocalizedMessage(), "Invalid Choices", JOptionPane.WARNING_MESSAGE);
                }
            };
            SwingUtilities.invokeLater(later);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void duplicateLabelCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_duplicateLabelCheckBoxActionPerformed
    {//GEN-HEADEREND:event_duplicateLabelCheckBoxActionPerformed
        if (getDuplicateLabelFlag()) {
            duplicateLabelComboBox.setEnabled(true);
            duplicateLabelComboBox.requestFocusInWindow();
        } else {
            duplicateLabelComboBox.setEnabled(false);
        }
    }//GEN-LAST:event_duplicateLabelCheckBoxActionPerformed

    boolean getDuplicateLabelFlag() {
        boolean retVal = false;
        Object duplicateLabel = duplicateLabelCheckBox.getSelectedObjects();
        if (duplicateLabel != null) {
            retVal = true;
        }
        return retVal;
    }

    boolean getFloatingLabelFlag() {
        boolean retVal = false;
        Object floatingLabel = floatingLabelCheckBox.getSelectedObjects();
        if (floatingLabel != null) {
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

    boolean getRecurseFlag() {
        boolean retVal = false;
        Object recurseFlag = recurseDirectoriesCheckBox.getSelectedObjects();
        if (recurseFlag != null) {
            retVal = true;
        }
        return retVal;
    }

    String getLabelString() {
        String labelString = (String) labelComboBox.getModel().getSelectedItem();
        return labelString;
    }

    String getDuplicateLabelString() {
        String duplicateLabelString = (String) duplicateLabelComboBox.getModel().getSelectedItem();
        return duplicateLabelString;
    }

    private void validateData() throws QVCSException {
        // Make sure the label string has some length to it.
        if (getLabelString().length() == 0) {
            // The label string has to be something.
            labelComboBox.requestFocusInWindow();
            throw new QVCSException("You must define a label string.");
        }

        // Validate that if the duplicate label flag is set, then the new label string must be different than
        // the label string associated with the label they want to duplicate.
        if (getDuplicateLabelFlag()) {
            String newLabelString = getLabelString();
            String existingLabelString = getDuplicateLabelString();
            if (newLabelString.equals(existingLabelString)) {
                // The strings are the same. This is not allowed.
                labelComboBox.requestFocusInWindow();
                throw new QVCSException("When duplicating a label, the new label must be different than the existing label");
            }

            if (existingLabelString.length() == 0) {
                // The strings are the same. This is not allowed.
                duplicateLabelComboBox.requestFocusInWindow();
                throw new QVCSException("When duplicating a label, you must choose a label to duplicate.");
            }
        }

        // Make sure that the label they are applying is not an internal-use QVCS label.
        String labelString = getLabelString();
        if (labelString.startsWith(QVCSConstants.QVCS_VIEW_LABEL)
                || labelString.startsWith(QVCSConstants.QVCS_TRANSLUCENT_BRANCH_LABEL)
                || labelString.startsWith(QVCSConstants.QVCS_OPAQUE_BRANCH_LABEL)) {
            // The strings are the same. This is not allowed.
            labelComboBox.requestFocusInWindow();
            throw new QVCSException("Your label string starts with a prefix that is used for an internal-use QVCS label. Please choose a different label string.");
        }
    }

    @Override
    public void dismissDialog() {
        closeDialog(null);
    }

    private void populateComponents() {
        duplicateLabelComboBox.setModel(new LabelsComboModel());
        labelComboBox.setModel(new LabelsComboModel());
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox duplicateLabelCheckBox;
    private javax.swing.JComboBox duplicateLabelComboBox;
    private javax.swing.JCheckBox floatingLabelCheckBox;
    private javax.swing.JComboBox labelComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox reUseLabelCheckBox;
    private javax.swing.JCheckBox recurseDirectoriesCheckBox;
    private javax.swing.JLabel withLabelLabel;
// End of variables declaration//GEN-END:variables
}
