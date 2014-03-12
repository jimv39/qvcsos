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
import javax.swing.JOptionPane;

/**
 * Define new project dialog.
 *
 * @author Jim Voris
 */
public class DefineNewProjectDialog extends AbstractQVCSCommandDialog {

    private static final long serialVersionUID = 403478869894204906L;

    private String projectNameMember;
    private boolean isOKFlag = false;
    private boolean createReferenceCopyOnServerFlag = false;
    private boolean ignoreCaseFlag = true;
    private boolean defineAlternateReferenceLocationFlag = false;
    private String alternateReferenceLocation = null;

    /**
     * Creates new form DefineNewProjectDialog.
     * @param parent the parent frame.
     * @param modal should this be a modal dialog.
     */
    public DefineNewProjectDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        ignoreCaseCheckBox.setSelected(ignoreCaseFlag);
        getRootPane().setDefaultButton(okButton);
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        createReferenceCopiesCheckBox = new javax.swing.JCheckBox();
        ignoreCaseCheckBox = new javax.swing.JCheckBox();
        defineAlternateReferenceLocationCheckBox = new javax.swing.JCheckBox();
        alternateReferenceLocationTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Define New Project");
        setName("Define New Project"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        projectNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        projectNameLabel.setText("Project Name:");

        projectNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        projectNameTextField.setToolTipText("Enter project name");

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

        createReferenceCopiesCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        createReferenceCopiesCheckBox.setText("Create reference copies on server");
        createReferenceCopiesCheckBox.setToolTipText("Enable this check box if you want reference copies of the project workfiles to be created on the server.");
        createReferenceCopiesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createReferenceCopiesCheckBoxActionPerformed(evt);
            }
        });

        ignoreCaseCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ignoreCaseCheckBox.setText("Ignore case in filenames");
        ignoreCaseCheckBox.setToolTipText("Enable this check box if you want the client and server to ignore case in filenames.");

        defineAlternateReferenceLocationCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        defineAlternateReferenceLocationCheckBox.setText("Define alternate reference file location");
        defineAlternateReferenceLocationCheckBox.setToolTipText("Enable this check box if you want reference copies of the project workfiles to be created on the server.");
        defineAlternateReferenceLocationCheckBox.setEnabled(false);
        defineAlternateReferenceLocationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defineAlternateReferenceLocationCheckBoxActionPerformed(evt);
            }
        });

        alternateReferenceLocationTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        alternateReferenceLocationTextField.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                        .add(12, 12, 12)
                                        .add(projectNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(layout.createSequentialGroup()
                                        .add(10, 10, 10)
                                        .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(layout.createSequentialGroup()
                                        .add(10, 10, 10)
                                        .add(createReferenceCopiesCheckBox))
                                .add(layout.createSequentialGroup()
                                        .add(10, 10, 10)
                                        .add(defineAlternateReferenceLocationCheckBox))
                                .add(layout.createSequentialGroup()
                                        .add(10, 10, 10)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, ignoreCaseCheckBox)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, alternateReferenceLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .add(16, 16, 16))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(projectNameLabel)
                        .add(4, 4, 4)
                        .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(14, 14, 14)
                        .add(createReferenceCopiesCheckBox)
                        .add(7, 7, 7)
                        .add(defineAlternateReferenceLocationCheckBox)
                        .add(7, 7, 7)
                        .add(alternateReferenceLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(ignoreCaseCheckBox)
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void defineAlternateReferenceLocationCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_defineAlternateReferenceLocationCheckBoxActionPerformed
    {//GEN-HEADEREND:event_defineAlternateReferenceLocationCheckBoxActionPerformed
        if (defineAlternateReferenceLocationCheckBox.getSelectedObjects() != null) {
            alternateReferenceLocationTextField.setEnabled(true);
        } else {
            alternateReferenceLocationTextField.setEnabled(false);
        }
    }//GEN-LAST:event_defineAlternateReferenceLocationCheckBoxActionPerformed

    private void createReferenceCopiesCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_createReferenceCopiesCheckBoxActionPerformed
    {//GEN-HEADEREND:event_createReferenceCopiesCheckBoxActionPerformed
        if (createReferenceCopiesCheckBox.getSelectedObjects() != null) {
            defineAlternateReferenceLocationCheckBox.setEnabled(true);
            if (defineAlternateReferenceLocationCheckBox.getSelectedObjects() != null) {
                alternateReferenceLocationTextField.setEnabled(true);
            }
        } else {
            defineAlternateReferenceLocationCheckBox.setEnabled(false);
            alternateReferenceLocationTextField.setEnabled(false);
        }
    }//GEN-LAST:event_createReferenceCopiesCheckBoxActionPerformed

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
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt)
    {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    boolean getIsOK() {
        return isOKFlag;
    }

    @Override
    public void dismissDialog() {
        cancelButtonActionPerformed(null);
    }

    private boolean validateValues() {
        boolean retVal = true;
        if (projectNameTextField.getText().length() == 0) {
            projectNameTextField.requestFocusInWindow();
            JOptionPane.showConfirmDialog(EnterpriseAdmin.getInstance(), "You must define a project name.", "Project name error", JOptionPane.PLAIN_MESSAGE);
            retVal = false;
        } else {
            projectNameMember = projectNameTextField.getText();
        }

        if (retVal) {
            // Figure out if they enabled the create reference copy flag.
            Object createReferenceCopyFlag = createReferenceCopiesCheckBox.getSelectedObjects();
            if (createReferenceCopyFlag != null) {
                this.createReferenceCopyOnServerFlag = true;
            }

            Object localIgnoreCaseFlag = ignoreCaseCheckBox.getSelectedObjects();
            if (localIgnoreCaseFlag != null) {
                this.ignoreCaseFlag = true;
            }

            Object localDefineAlternateReferenceLocationFlag = defineAlternateReferenceLocationCheckBox.getSelectedObjects();
            if (localDefineAlternateReferenceLocationFlag != null) {
                this.defineAlternateReferenceLocationFlag = true;

                alternateReferenceLocation = alternateReferenceLocationTextField.getText();
                if (alternateReferenceLocation.length() == 0) {
                    alternateReferenceLocationTextField.requestFocusInWindow();
                    retVal = false;
                }
            }
        }

        return retVal;
    }

    String getProjectName() {
        return projectNameMember;
    }

    boolean getCreateReferenceCopyFlag() {
        return createReferenceCopyOnServerFlag;
    }

    boolean getIgnoreCaseFlag() {
        return ignoreCaseFlag;
    }

    boolean getDefineAlternateReferenceLocationFlag() {
        return defineAlternateReferenceLocationFlag;
    }

    String getAlternateReferenceLocation() {
        return alternateReferenceLocation;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alternateReferenceLocationTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox createReferenceCopiesCheckBox;
    private javax.swing.JCheckBox defineAlternateReferenceLocationCheckBox;
    private javax.swing.JCheckBox ignoreCaseCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
// End of variables declaration//GEN-END:variables
}