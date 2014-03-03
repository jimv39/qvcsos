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
package com.qumasoft.guitools.admin;

import com.qumasoft.guitools.AbstractQVCSCommandDialog;
import com.qumasoft.qvcslib.ServerResponseListUserRoles;
import java.awt.Frame;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Add user to project dialog.
 *
 * @author Jim Voris
 */
public class AddUserToProjectDialog extends AbstractQVCSCommandDialog implements ChangeListener {

    private static final long serialVersionUID = -6830672525018023868L;

    private DefaultListModel<String> assignedRolesModelMember = null;
    private DefaultListModel<String> availableRolesModelMember = null;
    private boolean isOKFlag = false;

    /**
     * Creates new form MaintainUserRolesDialog.
     *
     * @param parent the parent frame.
     * @param modal should the dialog be modal.
     */
    public AddUserToProjectDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        center();
    }

    // Center this dialog.  This allows us to avoid overriding the center()
    // method which has protected access.
    void centerDialog() {
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
        availableRolesList = new javax.swing.JList();
        assignedRolesList = new javax.swing.JList();
        rightFillerPanel = new javax.swing.JPanel();
        leftFillerPanel = new javax.swing.JPanel();
        availableRolesLabel = new javax.swing.JLabel();
        assignedRolesLabel = new javax.swing.JLabel();
        addRoleButton = new javax.swing.JButton();
        removeRoleButton = new javax.swing.JButton();
        usersComboBox = new javax.swing.JComboBox();
        usersLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add User to Project");
        setModal(true);
        setName("MaintainUserRoles"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

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

        availableRolesList.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        availableRolesList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        assignedRolesList.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        assignedRolesList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        availableRolesLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        availableRolesLabel.setText("Available Roles:");

        assignedRolesLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        assignedRolesLabel.setText("Assigned Roles:");

        addRoleButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        addRoleButton.setText(">>");
        addRoleButton.setToolTipText("Add selected role");
        addRoleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRoleButtonActionPerformed(evt);
            }
        });

        removeRoleButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        removeRoleButton.setText("<<");
        removeRoleButton.setToolTipText("Remove selected role");
        removeRoleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRoleButtonActionPerformed(evt);
            }
        });

        usersComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        usersComboBox.setToolTipText("Select the user who is defined for this server who is not yet associated with the current project");

        usersLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        usersLabel.setText("Select user to add to this project:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(leftFillerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(usersLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 350, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(usersComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 350, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createSequentialGroup()
                                        .add(availableRolesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(90, 90, 90)
                                        .add(assignedRolesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(layout.createSequentialGroup()
                                        .add(availableRolesList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(10, 10, 10)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(addRoleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(removeRoleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(10, 10, 10)
                                        .add(assignedRolesList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(layout.createSequentialGroup()
                                        .add(18, 18, 18)
                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(18, 18, 18)))
                        .add(rightFillerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(leftFillerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 370, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(usersLabel)
                        .add(6, 6, 6)
                        .add(usersComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(23, 23, 23)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(availableRolesLabel)
                                .add(assignedRolesLabel))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(availableRolesList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createSequentialGroup()
                                        .add(60, 60, 60)
                                        .add(addRoleButton)
                                        .add(11, 11, 11)
                                        .add(removeRoleButton))
                                .add(assignedRolesList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(cancelButton)
                                .add(okButton)))
                .add(rightFillerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 370, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowActivated(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowActivated
    {//GEN-HEADEREND:event_formWindowActivated
        populateUsers();
    }//GEN-LAST:event_formWindowActivated

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        isOKFlag = false;
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        isOKFlag = true;
        closeDialog(null);
    }//GEN-LAST:event_okButtonActionPerformed

    private void removeRoleButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeRoleButtonActionPerformed
    {//GEN-HEADEREND:event_removeRoleButtonActionPerformed
        // Remove the selected roles
        int[] selectedIndices = assignedRolesList.getSelectedIndices();
        String[] selectedRoles = new String[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++) {
            selectedRoles[i] = getAssignedRolesModel().get(selectedIndices[i]);
        }
        for (String selectedRole : selectedRoles) {
            getAssignedRolesModel().removeElement(selectedRole);
            getAvailableRolesModel().addElement(selectedRole);
        }
    }//GEN-LAST:event_removeRoleButtonActionPerformed

    private void addRoleButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addRoleButtonActionPerformed
    {//GEN-HEADEREND:event_addRoleButtonActionPerformed
        // Add the selected roles
        int[] selectedIndices = availableRolesList.getSelectedIndices();
        String[] selectedRoles = new String[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++) {
            selectedRoles[i] = getAvailableRolesModel().get(selectedIndices[i]);
        }
        for (String selectedRole : selectedRoles) {
            getAvailableRolesModel().removeElement(selectedRole);
            getAssignedRolesModel().addElement(selectedRole);
        }
    }//GEN-LAST:event_addRoleButtonActionPerformed

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

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        Object change = changeEvent.getSource();

        if (change instanceof ServerResponseListUserRoles) {
            ServerResponseListUserRoles response = (ServerResponseListUserRoles) change;

            String[] userRoles = response.getUserRoles();
            String[] availableRoles = response.getAvailableRoles();

            assignedRolesModelMember = new DefaultListModel<>();

            // All the roles that are possible...
            Set<String> availableRolesSet = new HashSet<>();
            for (String availableRole : availableRoles) {
                availableRolesSet.add(availableRole);
            }
            for (String userRole : userRoles) {
                assignedRolesModelMember.addElement(userRole);
                // Remove the roles that are already assigned to this user.
                availableRolesSet.remove(userRole);
            }

            // The roles that are left are the ones that are available.
            availableRolesModelMember = new DefaultListModel<String>();
            for (String role : availableRolesSet) {
                availableRolesModelMember.addElement(role);
            }

            availableRolesList.setModel(availableRolesModelMember);
            assignedRolesList.setModel(assignedRolesModelMember);
        }
    }

    private DefaultListModel<String> getAvailableRolesModel() {
        return availableRolesModelMember;
    }

    private DefaultListModel<String> getAssignedRolesModel() {
        return assignedRolesModelMember;
    }

    String[] getAvailableRoles() {
        int size = getAvailableRolesModel().size();
        String[] availableRoles = new String[size];
        for (int i = 0; i < size; i++) {
            availableRoles[i] = getAvailableRolesModel().get(i);
        }
        return availableRoles;
    }

    String[] getAssignedRoles() {
        int size = getAssignedRolesModel().size();
        String[] assignedRoles = new String[size];
        for (int i = 0; i < size; i++) {
            assignedRoles[i] = getAssignedRolesModel().get(i);
        }
        return assignedRoles;
    }

    boolean getIsOK() {
        return isOKFlag;
    }

    String getUserName() {
        return (String) usersComboBox.getModel().getSelectedItem();
    }

    private void populateUsers() {
        EnterpriseAdmin parentFrame = (EnterpriseAdmin) getParent();
        Set serverUsers = parentFrame.getUserModel().getServerUsers();
        Set projectUsers = parentFrame.getUserModel().getProjectUsers();
        if (serverUsers != null) {
            Vector<String> serverUsersVector = new Vector<>();
            Iterator it = serverUsers.iterator();
            while (it.hasNext()) {
                String userName = (String) it.next();
                if (!projectUsers.contains(userName)) {
                    serverUsersVector.add(userName);
                }
            }
            DefaultComboBoxModel<String> userModel;
            userModel = new DefaultComboBoxModel<>(serverUsersVector);
            usersComboBox.setModel(userModel);
            if (serverUsersVector.size() > 0) {
                usersComboBox.setSelectedIndex(0);
                okButton.setEnabled(true);
            } else {
                // Disable the OK button.
                okButton.setEnabled(false);
            }
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRoleButton;
    private javax.swing.JLabel assignedRolesLabel;
    private javax.swing.JList assignedRolesList;
    private javax.swing.JLabel availableRolesLabel;
    private javax.swing.JList availableRolesList;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel leftFillerPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton removeRoleButton;
    private javax.swing.JPanel rightFillerPanel;
    private javax.swing.JComboBox usersComboBox;
    private javax.swing.JLabel usersLabel;
// End of variables declaration//GEN-END:variables
}
