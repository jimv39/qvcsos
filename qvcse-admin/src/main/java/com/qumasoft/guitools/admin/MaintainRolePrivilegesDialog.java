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
import com.qumasoft.qvcslib.ServerManager;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteRoleData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerGetRolePrivilegesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerUpdatePrivilegesData;
import com.qumasoft.qvcslib.response.ServerResponseListRoleNames;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Maintain role privileges dialog.
 *
 * @author Jim Voris
 */
public class MaintainRolePrivilegesDialog extends AbstractQVCSCommandDialog implements ChangeListener, ListSelectionListener {
    private static final long serialVersionUID = -3268164986047874194L;

    private DefaultListModel<String> rolesModel = null;
    private String serverName = null;

    /**
     * Creates new form MaintainRolePrivilegesDialog.
     * @param parent parent frame.
     * @param modal should this be a modal dialog.
     */
    public MaintainRolePrivilegesDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        center();

        roleList.getSelectionModel().addListSelectionListener(this);
    }

    // Center this dialog.  This allows us to avoid overriding the center()
    // method which has protected access.
    void centerDialog() {
        center();
    }

    void setServerName(final String argServerName) {
        this.serverName = argServerName;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        roleLabel = new javax.swing.JLabel();
        m_ScrollPane = new javax.swing.JScrollPane();
        roleList = new javax.swing.JList();
        newButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Maintain Role Privileges Dialog");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setName("Maintain Role Privileges Dialog"); // NOI18N
        setResizable(false);

        roleLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        roleLabel.setText("Roles");

        roleList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

            public int getSize() {
                return strings.length;
            }

            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        m_ScrollPane.setViewportView(roleList);

        newButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        newButton.setText("New...");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        editButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        editButton.setText("Edit...");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        deleteButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        deleteButton.setText("Delete...");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        closeButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, roleLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, m_ScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(deleteButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .add(editButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .add(newButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .add(closeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .add(20, 20, 20))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(roleLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(m_ScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createSequentialGroup()
                                        .add(newButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(editButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(deleteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(closeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
    {//GEN-HEADEREND:event_deleteButtonActionPerformed
        int selectedIndex = roleList.getSelectedIndex();
        if (selectedIndex != -1) {
            String selectedRole = rolesModel.get(selectedIndex);
            if (serverName != null) {
                // Lookup the transport proxy for this server.
                TransportProxyInterface transportProxy = EnterpriseAdmin.getInstance().getTransportProxyInterface(serverName);

                // We can only maintain roles if we're logged in, etc.
                if (transportProxy != null) {
                    ClientRequestServerDeleteRoleData clientRequestServerDeleteRoleData = new ClientRequestServerDeleteRoleData();
                    clientRequestServerDeleteRoleData.setRole(selectedRole);
                    clientRequestServerDeleteRoleData.setServerName(serverName);
                    clientRequestServerDeleteRoleData.setUserName(transportProxy.getUsername());
                    SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestServerDeleteRoleData);
                }
            }
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editButtonActionPerformed
    {//GEN-HEADEREND:event_editButtonActionPerformed
        int selectedIndex = roleList.getSelectedIndex();
        if (selectedIndex != -1) {
            String selectedRole = rolesModel.get(selectedIndex);
            MaintainPrivilegesDialog maintainPrivilegesDialog = new MaintainPrivilegesDialog(EnterpriseAdmin.getInstance(), true, this);
            ServerManager.getServerManager().addChangeListener(maintainPrivilegesDialog);
            maintainPrivilegesDialog.setRoleName(selectedRole);
            maintainPrivilegesDialog.setEditRoleNameFlag(false);
            if (serverName != null) {
                // Lookup the transport proxy for this server.
                TransportProxyInterface transportProxy = EnterpriseAdmin.getInstance().getTransportProxyInterface(serverName);

                // We can only maintain roles if we're logged in, etc.
                if (transportProxy != null) {
                    ClientRequestServerGetRolePrivilegesData clientRequestServerGetRolePrivilegesData = new ClientRequestServerGetRolePrivilegesData();
                    clientRequestServerGetRolePrivilegesData.setServerName(serverName);
                    clientRequestServerGetRolePrivilegesData.setRole(selectedRole);
                    clientRequestServerGetRolePrivilegesData.setUserName(transportProxy.getUsername());
                    SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestServerGetRolePrivilegesData);

                    maintainPrivilegesDialog.centerDialog();
                    maintainPrivilegesDialog.setVisible(true);

                    if (maintainPrivilegesDialog.getIsOK()) {
                        String[] privileges = maintainPrivilegesDialog.getPrivileges();
                        Boolean[] privilegesFlags = maintainPrivilegesDialog.getPrivilegesFlags();
                        updatePrivileges(transportProxy, selectedRole, privileges, privilegesFlags);
                    }
                }
            }
            ServerManager.getServerManager().removeChangeListener(maintainPrivilegesDialog);
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newButtonActionPerformed
    {//GEN-HEADEREND:event_newButtonActionPerformed
        MaintainPrivilegesDialog maintainPrivilegesDialog = new MaintainPrivilegesDialog(EnterpriseAdmin.getInstance(), true, this);
        ServerManager.getServerManager().addChangeListener(maintainPrivilegesDialog);
        maintainPrivilegesDialog.setRoleName("");
        maintainPrivilegesDialog.setEditRoleNameFlag(true);

        if (serverName != null) {
            // Lookup the transport proxy for this server.
            TransportProxyInterface transportProxy = EnterpriseAdmin.getInstance().getTransportProxyInterface(serverName);

            // We can only maintain roles if we're logged in, etc.
            if (transportProxy != null) {
                ClientRequestServerGetRolePrivilegesData clientRequestServerGetRolePrivilegesData = new ClientRequestServerGetRolePrivilegesData();
                clientRequestServerGetRolePrivilegesData.setServerName(serverName);

                // Use the ADMIN role, since we know that it should always exist.
                // The goal of the request to the server is to get the list of
                // privileges that we can use to define the new Role's privileges.
                clientRequestServerGetRolePrivilegesData.setRole("ADMIN");
                clientRequestServerGetRolePrivilegesData.setUserName(transportProxy.getUsername());
                SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestServerGetRolePrivilegesData);

                maintainPrivilegesDialog.centerDialog();
                maintainPrivilegesDialog.setVisible(true);

                if (maintainPrivilegesDialog.getIsOK()) {
                    String role = maintainPrivilegesDialog.getRole();

                    if (!isRoleInUse(role)) {
                        String[] privileges = maintainPrivilegesDialog.getPrivileges();
                        Boolean[] privilegesFlags = maintainPrivilegesDialog.getPrivilegesFlags();
                        updatePrivileges(transportProxy, role, privileges, privilegesFlags);
                    } else {
                        // Let user know the role is already in use.
                        final String message = role + " is already in use. Please choose a different role name.";
                        Runnable later = new Runnable() {

                            @Override
                            public void run() {
                                // Let the user know that the password change worked.
                                JOptionPane.showConfirmDialog(null, message, "Role already defined!", JOptionPane.PLAIN_MESSAGE);
                            }
                        };
                        SwingUtilities.invokeLater(later);
                    }
                }
            }
        }
        ServerManager.getServerManager().removeChangeListener(maintainPrivilegesDialog);
    }//GEN-LAST:event_newButtonActionPerformed

    @Override
    public void dismissDialog() {
        closeButtonActionPerformed(null);
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        Object change = changeEvent.getSource();

        if (change instanceof ServerResponseListRoleNames) {
            ServerResponseListRoleNames response = (ServerResponseListRoleNames) change;

            String[] roles = response.getRoleList();

            rolesModel = new DefaultListModel<>();
            for (String role : roles) {
                rolesModel.addElement(role);
            }

            roleList.setModel(rolesModel);
            roleList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    private void updatePrivileges(TransportProxyInterface transportProxy, final String role, final String[] privileges, final Boolean[] privilegesFlags) {
        ClientRequestServerUpdatePrivilegesData clientRequestServerUpdatePrivilegesData = new ClientRequestServerUpdatePrivilegesData();
        clientRequestServerUpdatePrivilegesData.setServerName(serverName);
        clientRequestServerUpdatePrivilegesData.setUserName(transportProxy.getUsername());
        clientRequestServerUpdatePrivilegesData.setRole(role);
        clientRequestServerUpdatePrivilegesData.setPrivileges(privileges);
        clientRequestServerUpdatePrivilegesData.setPrivilegesFlags(privilegesFlags);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestServerUpdatePrivilegesData);
    }

    boolean isRoleInUse(final String role) {
        boolean returnFlag = false;
        int size = rolesModel.size();
        for (int i = 0; i < size; i++) {
            String existingRole = rolesModel.getElementAt(i);
            if (0 == existingRole.compareTo(role)) {
                returnFlag = true;
                break;
            }
        }
        return returnFlag;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedIndex = roleList.getSelectedIndex();
        if (selectedIndex != -1) {
            String selectedRole = rolesModel.get(selectedIndex);
            if (0 == selectedRole.compareTo("ADMIN")) {
                deleteButton.setEnabled(false);
                editButton.setText("View...");
            } else {
                deleteButton.setEnabled(true);
                editButton.setText("Edit...");
            }
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane m_ScrollPane;
    private javax.swing.JButton newButton;
    private javax.swing.JLabel roleLabel;
    private javax.swing.JList roleList;
// End of variables declaration//GEN-END:variables
}
