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
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.operation.OperationGetDirectory;
import com.qumasoft.qvcslib.commandargs.GetDirectoryCommandArgs;
import com.qumasoft.qvcslib.Utility;

/**
 * Get directory dialog.
 * @author Jim Voris
 */
public class GetDirectoryDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -8486091819481601982L;

    private final OperationGetDirectory operationGetDirectory;

    /**
     * Create a get directory dialog.
     * @param parent the parent frame.
     * @param operation the get directory operation that will do the work.
     */
    public GetDirectoryDialog(java.awt.Frame parent, OperationGetDirectory operation) {
        super(parent, true);
        initComponents();
        this.operationGetDirectory = operation;
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

        recurseDirectoriesCheckBox = new javax.swing.JCheckBox();
        overwriteWorkfileComboBox = new javax.swing.JComboBox();
        timestampComboBox = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Get Directory");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        recurseDirectoriesCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        recurseDirectoriesCheckBox.setText("Recurse Directories");
        recurseDirectoriesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recurseDirectoriesCheckBoxActionPerformed(evt);
            }
        });

        overwriteWorkfileComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        overwriteWorkfileComboBox.setMaximumRowCount(10);
        overwriteWorkfileComboBox.setToolTipText("");

        timestampComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, overwriteWorkfileComboBox, 0, 422, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(recurseDirectoriesCheckBox)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 222, Short.MAX_VALUE)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, timestampComboBox, 0, 422, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(recurseDirectoriesCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(overwriteWorkfileComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(timestampComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 143, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        setVisible(false);
        dispose();
    }//GEN-LAST:event_formWindowClosing

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        formWindowClosing(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        formWindowClosing(null); // Run the update on the Swing thread.
        // Let the user know that the password change worked.
        GetDirectoryCommandArgs commandArgs = new GetDirectoryCommandArgs();
        commandArgs.setRecurseFlag((recurseDirectoriesCheckBox.getSelectedObjects() != null));
        commandArgs.setOverwriteBehavior(getOverwriteBehavior());
        commandArgs.setTimeStampBehavior(getTimestampBehavior());
        commandArgs.setUserName(QWinFrame.getQWinFrame().getLoggedInUserName());
        operationGetDirectory.completeOperation(commandArgs);
    }//GEN-LAST:event_okButtonActionPerformed

    @Override
    public void dismissDialog() {
        formWindowClosing(null);
    }

    private Utility.OverwriteBehavior getOverwriteBehavior() {
        Utility.OverwriteBehavior overwriteBehavior = null;
        Object overwriteString = overwriteWorkfileComboBox.getSelectedObjects();
        if (overwriteString != null) {
            OverwriteWorkfileComboModel model = (OverwriteWorkfileComboModel) overwriteWorkfileComboBox.getModel();
            overwriteBehavior = model.getSelectedOverwriteBehavior();
        }
        return overwriteBehavior;
    }

    private Utility.TimestampBehavior getTimestampBehavior() {
        Utility.TimestampBehavior timeStampBehavior = null;
        Object timeStampString = timestampComboBox.getSelectedObjects();
        if (timeStampString != null) {
            TimestampComboModel model = (TimestampComboModel) timestampComboBox.getModel();
            timeStampBehavior = model.getSelectedTimeStampBehavior();
        }
        return timeStampBehavior;
    }

    private void populateComponents() {
        timestampComboBox.setModel(new TimestampComboModel());
        overwriteWorkfileComboBox.setModel(new OverwriteWorkfileComboModel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox overwriteWorkfileComboBox;
    private javax.swing.JCheckBox recurseDirectoriesCheckBox;
    private javax.swing.JComboBox timestampComboBox;
    // End of variables declaration//GEN-END:variables
}
