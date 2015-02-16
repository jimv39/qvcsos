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
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.UserProperties;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Server login dialog.
 * @author  Jim Voris
 */
public class ServerLoginDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -8500809436969521088L;

    private boolean currentBypassLoginDialogFlag = false;
    private String serverNameString;
    private boolean isOKFlag = false;
    private String passwordString;
    private String userNameString;

    /**
     * Create a server login dialog.
     * @param parent the parent frame.
     * @param modal is this a modal dialog.
     * @param serverName the server name.
     */
    public ServerLoginDialog(java.awt.Frame parent, boolean modal, String serverName) {
        super(parent, modal);
        initComponents();
        setServerName(serverName);
        populateUserName();
        populateBypassCheckBox();
        getRootPane().setDefaultButton(okButton);
        passwordField.requestFocusInWindow();
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

        loginToServerLabel = new javax.swing.JLabel();
        serverNameLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        userNameTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        byPassLoginCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Server Login");
        setModal(true);
        setName("ServerLoginDialog"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        loginToServerLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        loginToServerLabel.setText("Login to server:");
        loginToServerLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        serverNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        serverNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        serverNameLabel.setText("servername");
        serverNameLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        userNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        userNameLabel.setText("User Name:");

        userNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        passwordLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        passwordLabel.setText("Password:");

        passwordField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        okButton.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
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

        byPassLoginCheckBox.setText("Bypass this login next time.");
        byPassLoginCheckBox.setToolTipText("Enable this checkbox to use these values to login automatically the next time.");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 100, Short.MAX_VALUE)
                                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, serverNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, userNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, loginToServerLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, userNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, passwordLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                                        .add(20, 20, 20))
                                .add(layout.createSequentialGroup()
                                        .add(byPassLoginCheckBox)
                                        .addContainerGap(113, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(loginToServerLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(serverNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(10, 10, 10)
                        .add(userNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(userNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(8, 8, 8)
                        .add(passwordLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byPassLoginCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 22, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton)
                                .add(cancelButton))
                        .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        String userName = userNameTextField.getText();

        if (0 == userName.compareToIgnoreCase(QVCSConstants.QVCS_ADMIN_USER)) {
            // The ADMIN user is NOT allowed to login to the client application!!
            warnProblem("User attempted to login to client as ADMIN user");

            // Run the message box on the Swing thread.
            Runnable later = () -> {
                // They tried to login as the ADMIN user... that's not allowed...
                JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), QVCSConstants.QVCS_ADMIN_USER + " user is not allowed to login to client application.", QVCSConstants.QVCS_ADMIN_USER
                        + " user error", JOptionPane.PLAIN_MESSAGE);
            };
            SwingUtilities.invokeLater(later);

            // Select the user name so they can easily change it.
            userNameTextField.requestFocusInWindow();
            userNameTextField.selectAll();
            isOKFlag = false;
        } else {
            userNameString = userNameTextField.getText();
            passwordString = new String(passwordField.getPassword());
            isOKFlag = true;

            // Save the status of bypassing the login dialog.
            boolean bypassLoginDialogFlag = byPassLoginCheckBox.isSelected();
            if (bypassLoginDialogFlag != currentBypassLoginDialogFlag) {
                QWinFrame.getQWinFrame().getUserProperties().setBypassLoginDialogFlag(bypassLoginDialogFlag);
            }
            // If we are to bypass the login dialog, then save these values for next time.
            if (bypassLoginDialogFlag) {
                QWinFrame.getQWinFrame().getUserProperties().setBypassServerName(serverNameString);
                QWinFrame.getQWinFrame().getUserProperties().setBypassPassword(passwordString);
                QWinFrame.getQWinFrame().getUserProperties().setBypassUserName(userNameString);
            }
            closeDialog(null);
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

    public boolean getIsOK() {
        return isOKFlag;
    }

    public String getUserName() {
        return userNameString;
    }

    public String getPassword() {
        return passwordString;
    }

    private void setServerName(String serverName) {
        serverNameLabel.setText(serverName);
        serverNameString = serverName;
    }

    private void populateBypassCheckBox() {
        UserProperties userProperties = QWinFrame.getQWinFrame().getUserProperties();
        currentBypassLoginDialogFlag = userProperties.getBypassLoginDialogFlag();
        byPassLoginCheckBox.setSelected(currentBypassLoginDialogFlag);
    }

    private void populateUserName() {
        userNameTextField.setText(System.getProperty("user.name"));
        userNameTextField.selectAll();
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox byPassLoginCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel loginToServerLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel serverNameLabel;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTextField;
// End of variables declaration//GEN-END:variables
}
