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
package com.qumasoft.guitools.admin;

import com.qumasoft.guitools.AbstractQVCSCommandDialog;

/**
 * Add server user dialog.
 *
 * @author Jim Voris
 */
public class AddServerUserDialog extends AbstractQVCSCommandDialog {

    private static final long serialVersionUID = -679278933570258702L;

    private String passwordMember = null;

    /**
     * Creates new form AddServerUserDialog.
     *
     * @param parent parent frame.
     * @param modal should the dialog be modal.
     */
    public AddServerUserDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        // Make the OK button work with the return key.
        getRootPane().setDefaultButton(okButton);

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
        userNameLabel = new javax.swing.JLabel();
        userNameTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        userPasswordField = new javax.swing.JPasswordField();
        confirmPasswordLabel = new javax.swing.JLabel();
        confirmUserPasswordField = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add User to Server");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setModal(true);
        setName("Add User to Server"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        okButton.setText("   OK   ");
        okButton.setEnabled(false);
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

        userNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        userNameLabel.setText("User Name:");

        userNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        userNameTextField.setToolTipText("Enter the new user's name ");

        passwordLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        passwordLabel.setText("Password:");

        userPasswordField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        confirmPasswordLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        confirmPasswordLabel.setText("Confirm Password:");

        confirmUserPasswordField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        confirmUserPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                m_ConfirmKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(userNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 260, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(userNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 280, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(passwordLabel)
                                .add(userPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 280, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(confirmPasswordLabel)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(layout.createSequentialGroup()
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(confirmUserPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 280, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(15, 15, 15))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(userNameLabel)
                        .add(4, 4, 4)
                        .add(userNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(passwordLabel)
                        .add(6, 6, 6)
                        .add(userPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(confirmPasswordLabel)
                        .add(6, 6, 6)
                        .add(confirmUserPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(14, 14, 14)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton)
                                .add(cancelButton))
                        .add(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void m_ConfirmKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_m_ConfirmKeyReleased
    {//GEN-HEADEREND:event_m_ConfirmKeyReleased
        char[] password = userPasswordField.getPassword();
        passwordMember = new String(password);

        // Get the confirmed password.
        char[] confirmedPassword = confirmUserPasswordField.getPassword();
        String confirmPassword = new String(confirmedPassword);

        // If both have been selected, then OK can dismiss the dialog.
        if ((passwordMember.length() > 0) && passwordMember.equals(confirmPassword)) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }//GEN-LAST:event_m_ConfirmKeyReleased

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        // Get the password.
        char[] password = userPasswordField.getPassword();
        passwordMember = new String(password);

        // Get the confirmed password.
        char[] confirmedPassword = confirmUserPasswordField.getPassword();
        String confirmPassword = new String(confirmedPassword);

        // If both have been selected, then OK can dismiss the dialog.
        if ((passwordMember.length() > 0) && passwordMember.equals(confirmPassword)) {
            closeDialog(null);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        passwordMember = null;
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

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

    String getUserName() {
        return userNameTextField.getText();
    }

    String getPassword() {
        return passwordMember;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel confirmPasswordLabel;
    private javax.swing.JPasswordField confirmUserPasswordField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTextField;
    private javax.swing.JPasswordField userPasswordField;
// End of variables declaration//GEN-END:variables
}
