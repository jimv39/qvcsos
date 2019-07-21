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
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.operation.OperationLabelArchive;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Label dialog.
 * @author  Jim Voris
 */
public class LabelDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -2030330278123502397L;

    private final List selectedFiles;
    private final OperationLabelArchive operationLabelArchive;

    /**
     * Create a label dialog.
     * @param parent the parent frame.
     * @param files the list of selected files.
     * @param modal is this modal.
     * @param operation the label operation that will do the work.
     */
    public LabelDialog(java.awt.Frame parent, List files, boolean modal, OperationLabelArchive operation) {
        super(parent, modal);
        operationLabelArchive = operation;
        selectedFiles = files;
        initComponents();
        populateComponents();
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

        revisionToLabelLabel = new javax.swing.JLabel();
        revisionToLabelComboBox = new javax.swing.JComboBox();
        withLabelLabel = new javax.swing.JLabel();
        labelComboBox = new javax.swing.JComboBox();
        floatingLabelCheckBox = new javax.swing.JCheckBox();
        reUseLabelCheckBox = new javax.swing.JCheckBox();
        duplicateLabelCheckBox = new javax.swing.JCheckBox();
        duplicateLabelComboBox = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setTitle("Apply Label");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        revisionToLabelLabel.setDisplayedMnemonic('r');
        revisionToLabelLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        revisionToLabelLabel.setLabelFor(revisionToLabelComboBox);
        revisionToLabelLabel.setText("Revision to Label:");
        revisionToLabelLabel.setToolTipText("");

        revisionToLabelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        revisionToLabelComboBox.setMaximumRowCount(15);
        revisionToLabelComboBox.setToolTipText("Select the revision to label");

        withLabelLabel.setDisplayedMnemonic('w');
        withLabelLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        withLabelLabel.setLabelFor(labelComboBox);
        withLabelLabel.setText("With the label:");

        labelComboBox.setEditable(true);
        labelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelComboBox.setMaximumRowCount(15);
        labelComboBox.setToolTipText("Select or define the label to apply");

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
        duplicateLabelComboBox.setMaximumRowCount(15);
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
                        .add(19, 19, 19)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(revisionToLabelComboBox, 0, 340, Short.MAX_VALUE)
                                        .add(labelComboBox, 0, 340, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(revisionToLabelLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(withLabelLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, duplicateLabelComboBox, 0, 340, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, duplicateLabelCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, reUseLabelCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, floatingLabelCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))
                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(revisionToLabelLabel)
                        .add(4, 4, 4)
                        .add(revisionToLabelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(3, 3, 3)
                        .add(withLabelLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(7, 7, 7)
                        .add(floatingLabelCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(reUseLabelCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(duplicateLabelCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(duplicateLabelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(3, 3, 3)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(cancelButton)
                                .add(okButton))
                        .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void duplicateLabelCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_duplicateLabelCheckBoxActionPerformed
    {//GEN-HEADEREND:event_duplicateLabelCheckBoxActionPerformed
        if (getDuplicateLabelFlag()) {
            duplicateLabelComboBox.setEnabled(true);
            duplicateLabelComboBox.requestFocusInWindow();
        } else {
            duplicateLabelComboBox.setEnabled(false);
        }
    }//GEN-LAST:event_duplicateLabelCheckBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        try {
            validateData();
            closeDialog(null);
            operationLabelArchive.processDialogResult(selectedFiles, this);
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

    /**
     * Get the revision string.
     * @return the revision string.
     */
    public String getRevisionString() {
        String revisionString = (String) revisionToLabelComboBox.getModel().getSelectedItem();
        return revisionString;
    }

    /**
     * Get the label string.
     * @return the label string.
     */
    public String getLabelString() {
        String labelString = (String) labelComboBox.getModel().getSelectedItem();
        return labelString;
    }

    /**
     * Get the duplicate label string.
     * @return the duplicate label string.
     */
    public String getDuplicateLabelString() {
        String duplicateLabelString = (String) duplicateLabelComboBox.getModel().getSelectedItem();
        return duplicateLabelString;
    }

    /**
     * Get the floating label flag.
     * @return the floating label flag.
     */
    public boolean getFloatingLabelFlag() {
        boolean retVal = false;
        Object floatingLabel = floatingLabelCheckBox.getSelectedObjects();
        if (floatingLabel != null) {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Get the re-use label flag.
     * @return the re-use label flag.
     */
    public boolean getReUseLabelFlag() {
        boolean retVal = false;
        Object reUseLabel = reUseLabelCheckBox.getSelectedObjects();
        if (reUseLabel != null) {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Get the duplicate label flag.
     * @return the duplicate label flag.
     */
    public boolean getDuplicateLabelFlag() {
        boolean retVal = false;
        Object duplicateLabel = duplicateLabelCheckBox.getSelectedObjects();
        if (duplicateLabel != null) {
            retVal = true;
        }
        return retVal;
    }

    private void populateComponents() {
        if (selectedFiles.size() == 1) {
            revisionToLabelComboBox.setModel(new RevisionsComboModel((MergedInfoInterface) selectedFiles.get(0)));
            duplicateLabelComboBox.setModel(new LabelsComboModel((MergedInfoInterface) selectedFiles.get(0)));
        } else if (selectedFiles.size() > 1) {
            revisionToLabelComboBox.setModel(new RevisionsComboModel(null));
            duplicateLabelComboBox.setModel(new LabelsComboModel());
        }
        labelComboBox.setModel(new LabelsComboModel());
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
        if (labelString.startsWith(QVCSConstants.QVCS_FEATURE_BRANCH_LABEL) || labelString.startsWith(QVCSConstants.QVCS_OPAQUE_BRANCH_LABEL)) {
            // The strings are the same. This is not allowed.
            labelComboBox.requestFocusInWindow();
            throw new QVCSException("Your label string starts with a prefix that is used for an internal-use QVCS label. Please choose a different label string.");
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox duplicateLabelCheckBox;
    private javax.swing.JComboBox duplicateLabelComboBox;
    private javax.swing.JCheckBox floatingLabelCheckBox;
    private javax.swing.JComboBox labelComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox reUseLabelCheckBox;
    private javax.swing.JComboBox revisionToLabelComboBox;
    private javax.swing.JLabel revisionToLabelLabel;
    private javax.swing.JLabel withLabelLabel;
// End of variables declaration//GEN-END:variables
}
