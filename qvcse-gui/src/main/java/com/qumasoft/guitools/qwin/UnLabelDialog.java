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

import com.qumasoft.qvcslib.MergedInfoInterface;
import java.util.List;

/**
 * Unlabel dialog.
 *
 * @author Jim Voris
 */
public class UnLabelDialog extends AbstractQWinCommandDialog {

    private static final long serialVersionUID = 8318907152248712868L;

    private final List selectedFiles;
    private final OperationUnLabelArchive operationUnLabelArchive;

    /**
     * Creates new form UnLabelDialog.
     *
     * @param parent the parent frame.
     * @param files the list of selected files.
     * @param modal is this a modal dialog.
     * @param operation the unlabel operation instance that will do the work.
     */
    public UnLabelDialog(java.awt.Frame parent, List files, boolean modal, OperationUnLabelArchive operation) {
        super(parent, modal);
        this.operationUnLabelArchive = operation;
        this.selectedFiles = files;
        initComponents();
        populateComponents();
        setFont();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        labelComboBox = new javax.swing.JComboBox();
        labelLabel = new javax.swing.JLabel();

        setTitle("Remove Label");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        okButton.setText("   OK   ");
        okButton.setMaximumSize(new java.awt.Dimension(80, 25));
        okButton.setMinimumSize(new java.awt.Dimension(80, 25));
        okButton.setPreferredSize(new java.awt.Dimension(80, 25));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new java.awt.Dimension(80, 25));
        cancelButton.setMinimumSize(new java.awt.Dimension(80, 25));
        cancelButton.setPreferredSize(new java.awt.Dimension(80, 25));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        labelComboBox.setEditable(true);
        labelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelComboBox.setMaximumRowCount(10);
        labelComboBox.setToolTipText("");

        labelLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelLabel.setText("Label to Remove:");
        labelLabel.setToolTipText("Select or enter the label string that you want to remove");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(labelLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(layout.createSequentialGroup()
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(labelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(labelLabel)
                        .add(4, 4, 4)
                        .add(labelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(14, 14, 14)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(15, 15, 15))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        closeDialog(null);
        operationUnLabelArchive.processDialogResult(selectedFiles, this);
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
    {
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    @Override
    public void dismissDialog() {
        cancelButtonActionPerformed(null);
    }

    private void populateComponents() {
        if (selectedFiles.size() == 1) {
            labelComboBox.setModel(new LabelsComboModel((MergedInfoInterface) selectedFiles.get(0)));
        } else if (selectedFiles.size() > 1) {
            labelComboBox.setModel(new LabelsComboModel());
        }
    }

    String getLabelString() {
        String retVal = null;
        Object selectedItem = labelComboBox.getSelectedItem();
        if (selectedItem instanceof String) {
            retVal = (String) selectedItem;
        }

        return retVal;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox labelComboBox;
    private javax.swing.JLabel labelLabel;
    private javax.swing.JButton okButton;
// End of variables declaration//GEN-END:variables
}
