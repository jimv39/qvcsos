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
import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import com.qumasoft.guitools.qwin.operation.OperationMaintainServerBaseClass;
import com.qumasoft.qvcslib.ServerProperties;
import java.net.InetSocketAddress;
import javax.swing.JOptionPane;

/**
 * Server properties dialog.
 * @author  Jim Voris
 */
public class ServerPropertiesDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -8550829456563294477L;

    private String serverName;
    private String serverIPAddress;
    private int clientPort;
    private final OperationMaintainServerBaseClass operationMaintainServerBaseClass;

    private boolean isOKFlag = false;

    /**
     * Create a server properties dialog.
     * @param parent the parent frame.
     * @param operation the operation that will do the work.
     * @param modal is this a modal dialog.
     * @param server the server name.
     */
    public ServerPropertiesDialog(java.awt.Frame parent, OperationMaintainServerBaseClass operation, boolean modal, String server) {
        super(parent, modal);
        initComponents();
        getRootPane().setDefaultButton(okButton);
        this.operationMaintainServerBaseClass = operation;

        // Load the dialog with existing data.
        loadData(server);

        // If the server name is supplied, make it so they can't edit that.
        if (server != null) {
            serverNameValue.setEditable(false);
        }
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

        serverNameLabel = new javax.swing.JLabel();
        serverNameValue = new javax.swing.JTextField();
        serverIPAddressLabel = new javax.swing.JLabel();
        serverIPAddressValue = new javax.swing.JTextField();
        clientPortLabel = new javax.swing.JLabel();
        clientPortValue = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setTitle("Server Properties");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setName("Server Properties"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        serverNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        serverNameLabel.setText("Server Name:");

        serverNameValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        serverNameValue.setToolTipText("Enter server name");

        serverIPAddressLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        serverIPAddressLabel.setText("Server IP Address:");

        serverIPAddressValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        serverIPAddressValue.setToolTipText("Enter the server IP address");

        clientPortLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        clientPortLabel.setText("Client Port:");

        clientPortValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        clientPortValue.setToolTipText("Enter server listener port for client connections.");

        okButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        okButton.setText("OK");
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
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(serverNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(serverIPAddressLabel)
                                                .add(layout.createSequentialGroup()
                                                        .add(6, 6, 6)
                                                        .add(serverIPAddressValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(132, 132, 132)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(layout.createSequentialGroup()
                                                        .add(6, 6, 6)
                                                        .add(clientPortValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .add(clientPortLabel)
                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(serverNameValue))
                        .add(18, 18, 18))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(serverNameLabel)
                        .add(4, 4, 4)
                        .add(serverNameValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(serverIPAddressLabel)
                                .add(clientPortLabel))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(serverIPAddressValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(clientPortValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(29, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        isOKFlag = false;
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        if (validateValues()) {
            isOKFlag = true;
            closeDialog(null);
        }
        operationMaintainServerBaseClass.processDialogResult(this);
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
     * Did the user click the OK button.
     * @return true if they did; false otherwise.
     */
    public boolean getIsOK() {
        return isOKFlag;
    }

    private boolean validateValues() {
        boolean retVal = true;
        serverName = serverNameValue.getText();
        if (serverName.length() == 0) {
            serverNameValue.requestFocusInWindow();
            JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "You must define a server name.", "Server name error", JOptionPane.PLAIN_MESSAGE);
            retVal = false;
        }
        if (retVal) {
            try {
                serverIPAddress = serverIPAddressValue.getText();

                // Use this to validate the entered address.
                InetSocketAddress address = new InetSocketAddress(serverIPAddress, 0);
            } catch (IllegalArgumentException e) {
                serverIPAddressValue.requestFocusInWindow();
                retVal = false;
            }
        }
        if (retVal) {
            try {
                clientPort = Integer.decode(clientPortValue.getText());
            } catch (NumberFormatException e) {
                clientPortValue.requestFocusInWindow();
                retVal = false;
            }
        }
        return retVal;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Get the client port.
     * @return the client port.
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * Get the server IP address.
     * @return the server IP address.
     */
    public String getServerIPAddress() {
        return serverIPAddress;
    }

    private void loadData(String serverName) {
        try {
            if (serverName != null) {
                ServerProperties serverProperties = new ServerProperties(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), serverName);
                if (serverName.equals(serverProperties.getServerName())) {
                    serverNameValue.setText(serverName);
                    serverIPAddressValue.setText(serverProperties.getServerIPAddress());
                    clientPortValue.setText(Integer.toString(serverProperties.getClientPort()));
                }
            } else {
                clientPortValue.setText("9889");
            }
        } catch (Exception e) {
            logProblem("Caught exception trying to load server properties. Exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel clientPortLabel;
    private javax.swing.JTextField clientPortValue;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel serverIPAddressLabel;
    private javax.swing.JTextField serverIPAddressValue;
    private javax.swing.JLabel serverNameLabel;
    private javax.swing.JTextField serverNameValue;
// End of variables declaration//GEN-END:variables
}
