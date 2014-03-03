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

import com.qumasoft.qvcslib.QVCSException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Edit file group dialog.
 * @author Jim Voris
 */
public class EditFileGroupDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -2443800542461977104L;

    private String groupName;
    private String[] extensions;
    private final DefineFileGroupsDialog parentDialog;

    /**
     * Create an edit file group dialog. Use this one for creating a new file group's extensions.
     * @param parent the parent frame.
     * @param parentDlg the parent dialog.
     */
    public EditFileGroupDialog(java.awt.Frame parent, DefineFileGroupsDialog parentDlg) {
        super(parent, true);
        parentDialog = parentDlg;
        initComponents();
        setFont();
        center();
    }

    /**
     * Create an edit file group dialog. Use this one for editing an existing file group's extensions.
     * @param parent the parent frame.
     * @param parentDlg the parent dialog.
     * @param group the group name.
     * @param extns the set of extensions for the given group.
     */
    public EditFileGroupDialog(java.awt.Frame parent, DefineFileGroupsDialog parentDlg, String group, String[] extns) {
        super(parent, true);
        parentDialog = parentDlg;
        groupName = group;
        extensions = extns;
        initComponents();
        populateComponents();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileGroupNameLabel = new javax.swing.JLabel();
        fileGroupNameTextField = new javax.swing.JTextField();
        fileExtensionsLabel = new javax.swing.JLabel();
        fileExtensionsTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Define/Edit File Group");
        setResizable(false);

        fileGroupNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        fileGroupNameLabel.setText("File Group Name:");

        fileGroupNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        fileGroupNameTextField.setToolTipText("Enter the name of the file group.");

        fileExtensionsLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        fileExtensionsLabel.setText("File Group file extensions:");

        fileExtensionsTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        fileExtensionsTextField.setToolTipText("Enter the file extensions for this group.");

        okButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        okButton.setText("OK");
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
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(fileGroupNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                                        .add(fileExtensionsTextField)
                                        .add(fileGroupNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(fileExtensionsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(okButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                                        .add(84, 84, 84)
                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(fileGroupNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileGroupNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileExtensionsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileExtensionsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton)
                                .add(cancelButton))
                        .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        dismissDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        try {
            String newGroupName = fileGroupNameTextField.getText();
            if ((groupName != null) && (newGroupName.compareTo(groupName) != 0)) {
                parentDialog.deleteGroup(groupName);
            }
            parentDialog.updateGroup(newGroupName, getExtensions());
            dismissDialog();
        } catch (final QVCSException e) {
            // Run the update on the Swing thread.
            Runnable later = new Runnable() {

                @Override
                public void run() {
                    // Let the user know that the password change worked.
                    JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), e.getLocalizedMessage(), "File extension in use", JOptionPane.WARNING_MESSAGE);
                }
            };
            SwingUtilities.invokeLater(later);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    @Override
    public void dismissDialog() {
        setVisible(false);
        dispose();
    }

    private void populateComponents() {
        fileGroupNameTextField.setText(groupName);
        fileExtensionsTextField.setText(FileGroupDialogTableModel.getExtensionsString(extensions));
    }

    private String[] getExtensions() throws QVCSException {
        // Parse the string into a String array.
        String[] extensionList = null;
        String extns = fileExtensionsTextField.getText();

        try {
            if (extns != null) {
                extensionList = extns.split("[,\\s]");
            }
        } catch (Exception e) {
            // Catch any parsing problems.
            extensionList = null;
        }

        if (extensionList == null) {
            throw new QVCSException("invalid value for extension string.");
        }

        return extensionList;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel fileExtensionsLabel;
    private javax.swing.JTextField fileExtensionsTextField;
    private javax.swing.JLabel fileGroupNameLabel;
    private javax.swing.JTextField fileGroupNameTextField;
    private javax.swing.JButton okButton;
// End of variables declaration//GEN-END:variables
}
