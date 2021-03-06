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

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.MergedInfoInterface;
import java.io.File;

/**
 * Ask the user for the new workfile name.
 *
 * @author Jim Voris
 */
public class RenameWorkfileDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = 918839812162889898L;

    private final String currentShortWorkfileName;
    private final String currentWorkfileDirectoryName;
    private final String standardMessage;
    private final String inUseMessage;
    private String newShortWorkfileName;
    private final MergedInfoInterface mergedInfo;

    /**
     * Creates new form RenameWorkfileDialog.
     *
     * @param parent The parent frame. This will be the QWinFrame frame object.
     * @param info The mergedInfo object that describes the current file, i.e. the file that we are trying to rename.
     */
    public RenameWorkfileDialog(java.awt.Frame parent, MergedInfoInterface info) {
        super(parent, true);
        this.standardMessage = "Please enter a new filename.";
        this.inUseMessage = " is already in use. Please choose a different filename.";
        this.mergedInfo = info;

        String serverName = QWinFrame.getQWinFrame().getServerName();
        String projectName = QWinFrame.getQWinFrame().getProjectName();
        String branchName = QWinFrame.getQWinFrame().getBranchName();
        String workfileBaseDirectory = QWinFrame.getQWinFrame().getUserLocationProperties().getWorkfileLocation(serverName, projectName, branchName);

        currentShortWorkfileName = info.getShortWorkfileName();
        currentWorkfileDirectoryName = workfileBaseDirectory + File.separator + info.getArchiveDirManager().getAppendedPath() + File.separator;

        initComponents();
        newNameTextField.setToolTipText(standardMessage);
        newNameTextField.setText(currentShortWorkfileName);
        setFont();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newNameLabel = new javax.swing.JLabel();
        newNameTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Rename file");
        setResizable(false);

        newNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        newNameLabel.setText("Enter new name:");

        newNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        newNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                newNameTextFieldKeyReleased(evt);
            }
        });

        okButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        okButton.setText("OK");
        okButton.setEnabled(false);
        okButton.setPreferredSize(new java.awt.Dimension(71, 23));
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
                                .add(newNameLabel)
                                .add(newNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                                .add(layout.createSequentialGroup()
                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 171, Short.MAX_VALUE)
                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(newNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(cancelButton))
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newNameTextFieldKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_newNameTextFieldKeyReleased
    {//GEN-HEADEREND:event_newNameTextFieldKeyReleased
        // Make sure the new name doesn't exist yet.
        String newShortName = newNameTextField.getText();

        Object existingControlledFile = mergedInfo.getArchiveDirManager().getArchiveInfo(newShortName);
        String newFullWorkfileName = currentWorkfileDirectoryName + newShortName;
        File newFile = new File(newFullWorkfileName);
        if (existingControlledFile == null) {
            // There is no existing archive file for the new name, so we'll allow the rename,
            newNameTextField.setToolTipText(standardMessage);
            okButton.setEnabled(true);
        } else {
            // There is already an archive file that exists for the new name....
            if (newFile.exists()) {
                // A workfile already exists with the new name also, so we cannot rename this file to that
                // new name.
                okButton.setEnabled(false);
                String errorMessage = newShortName + inUseMessage;
                newNameTextField.setToolTipText(errorMessage);
            } else {
                newNameTextField.setToolTipText(standardMessage);
                okButton.setEnabled(true);
            }
        }
    }//GEN-LAST:event_newNameTextFieldKeyReleased

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        newShortWorkfileName = null;
        dismissDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        newShortWorkfileName = newNameTextField.getText();
        dismissDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close and dismiss the dialog.
     */
    @Override
    public void dismissDialog() {
        setVisible(false);
        dispose();
    }

    /**
     * Get the new short workfile name.
     *
     * @return Return the new short workfile name, or null in the case where the user has not successfully defined a new workfile name.
     */
    public String getNewShortWorkfileName() {
        return newShortWorkfileName;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel newNameLabel;
    private javax.swing.JTextField newNameTextField;
    private javax.swing.JButton okButton;
// End of variables declaration//GEN-END:variables
}
