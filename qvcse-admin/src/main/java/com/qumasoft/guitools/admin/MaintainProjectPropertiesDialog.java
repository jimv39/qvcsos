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
import com.qumasoft.qvcslib.AbstractProjectProperties;

/**
 * Maintain project properties dialog.
 *
 * @author Jim Voris
 */
public class MaintainProjectPropertiesDialog extends AbstractQVCSCommandDialog {
    private static final long serialVersionUID = -1121182472592102734L;

    private boolean isOKFlag = false;
    private boolean createReferenceCopyOnServerFlag = false;
    private boolean createOrDeleteCurrentReferenceFilesFlag = false;
    private boolean ignoreCaseFlag = false;
    private boolean defineAlternateReferenceLocationFlag = false;
    private String alternateReferenceLocation = null;
    private String projectName = null;
    private AbstractProjectProperties projectProperties = null;

    /**
     * Creates new form MaintainProjectPropertiesDialog.
     * @param parent the parent frame.
     * @param modal should this be modal.
     * @param argProjectProperties the project properties.
     * @param argProjectName the project name.
     */
    public MaintainProjectPropertiesDialog(java.awt.Frame parent, boolean modal, String argProjectName, AbstractProjectProperties argProjectProperties) {
        super(parent, modal);

        this.projectName = argProjectName;
        this.projectProperties = argProjectProperties;

        initComponents();
        populateComponents();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        createReferenceCopiesCheckBox = new javax.swing.JCheckBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        createReferenceFilesCheckBox = new javax.swing.JCheckBox();
        deleteReferenceCopiesCheckBox = new javax.swing.JCheckBox();
        ignoreCaseCheckBox = new javax.swing.JCheckBox();
        defineAlternateReferenceLocationCheckBox = new javax.swing.JCheckBox();
        alternateReferenceLocationTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Maintain Project Properties");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
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

        projectNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        projectNameLabel.setText("Project Name:");

        projectNameTextField.setEditable(false);
        projectNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        projectNameTextField.setToolTipText("This is the project's name");

        createReferenceFilesCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        createReferenceFilesCheckBox.setText("Create reference files on server now.");
        createReferenceFilesCheckBox.setToolTipText("Enable this check box if you want to create reference copies of the project workfiles on the server right now.");

        deleteReferenceCopiesCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        deleteReferenceCopiesCheckBox.setText("Delete existing reference files on server now.");
        deleteReferenceCopiesCheckBox.setToolTipText("Enable this check box if you want to delete any existing reference copies of the project workfiles located on the server right now.");

        ignoreCaseCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ignoreCaseCheckBox.setText("Ignore case in filenames");

        defineAlternateReferenceLocationCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        defineAlternateReferenceLocationCheckBox.setText("Define alternate reference file location");
        defineAlternateReferenceLocationCheckBox.setToolTipText("Enable this checkbox if you want to define an alternate (non-default) location for this project's reference files.");
        defineAlternateReferenceLocationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defineAlternateReferenceLocationCheckBoxActionPerformed(evt);
            }
        });

        alternateReferenceLocationTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        alternateReferenceLocationTextField.setToolTipText("Enter the full path of the root directory (on the server) to be used for this project's reference files.");
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
                                        .add(20, 20, 20)
                                        .add(createReferenceCopiesCheckBox))
                                .add(layout.createSequentialGroup()
                                        .add(20, 20, 20)
                                        .add(createReferenceFilesCheckBox))
                                .add(layout.createSequentialGroup()
                                        .add(20, 20, 20)
                                        .add(deleteReferenceCopiesCheckBox))
                                .add(layout.createSequentialGroup()
                                        .add(20, 20, 20)
                                        .add(defineAlternateReferenceLocationCheckBox))
                                .add(layout.createSequentialGroup()
                                        .add(20, 20, 20)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(layout.createSequentialGroup()
                                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(110, 110, 110)
                                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .add(ignoreCaseCheckBox)
                                                .add(alternateReferenceLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .add(28, 28, 28))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(projectNameLabel)
                        .add(4, 4, 4)
                        .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(createReferenceCopiesCheckBox)
                        .add(7, 7, 7)
                        .add(createReferenceFilesCheckBox)
                        .add(7, 7, 7)
                        .add(deleteReferenceCopiesCheckBox)
                        .add(7, 7, 7)
                        .add(defineAlternateReferenceLocationCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(alternateReferenceLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(ignoreCaseCheckBox)
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton)
                                .add(cancelButton))
                        .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void defineAlternateReferenceLocationCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_defineAlternateReferenceLocationCheckBoxActionPerformed
    {//GEN-HEADEREND:event_defineAlternateReferenceLocationCheckBoxActionPerformed
        if (defineAlternateReferenceLocationCheckBox.isSelected()) {
            alternateReferenceLocationTextField.setEnabled(true);
        } else {
            alternateReferenceLocationTextField.setEnabled(false);
        }
    }//GEN-LAST:event_defineAlternateReferenceLocationCheckBoxActionPerformed

    private void createReferenceCopiesCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_createReferenceCopiesCheckBoxActionPerformed
    {//GEN-HEADEREND:event_createReferenceCopiesCheckBoxActionPerformed
        // Figure out if they enabled the create reference copy flag.
        Object createReferenceCopyFlag = createReferenceCopiesCheckBox.getSelectedObjects();
        if (createReferenceCopyFlag != null) {
            createReferenceFilesCheckBox.setEnabled(true);
            deleteReferenceCopiesCheckBox.setEnabled(false);
            deleteReferenceCopiesCheckBox.setSelected(false);
            defineAlternateReferenceLocationCheckBox.setEnabled(true);
            alternateReferenceLocationTextField.setEnabled(true);
        } else {
            createReferenceFilesCheckBox.setEnabled(false);
            createReferenceFilesCheckBox.setSelected(false);
            deleteReferenceCopiesCheckBox.setEnabled(true);
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
        isOKFlag = true;

        // Figure out if they enabled the create reference copy flag.
        Object createReferenceCopyFlag = createReferenceCopiesCheckBox.getSelectedObjects();
        if (createReferenceCopyFlag != null) {
            createReferenceCopyOnServerFlag = true;
        }

        Object createReferenceFilesFlag = createReferenceFilesCheckBox.getSelectedObjects();
        Object deleteReferenceFilesFlag = deleteReferenceCopiesCheckBox.getSelectedObjects();
        if ((createReferenceFilesFlag != null) || (deleteReferenceFilesFlag != null)) {
            createOrDeleteCurrentReferenceFilesFlag = true;
        }

        Object ignoreCaseFlagFromDialog = ignoreCaseCheckBox.getSelectedObjects();
        if (ignoreCaseFlagFromDialog != null) {
            this.ignoreCaseFlag = true;
        }
        defineAlternateReferenceLocationFlag = defineAlternateReferenceLocationCheckBox.isSelected();

        if (defineAlternateReferenceLocationFlag) {
            alternateReferenceLocation = alternateReferenceLocationTextField.getText();
        } else {
            alternateReferenceLocation = null;
        }

        closeDialog(null);
    }//GEN-LAST:event_okButtonActionPerformed

    private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
    {//GEN-HEADEREND:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    @Override
    public void dismissDialog() {
        cancelButtonActionPerformed(null);
    }

    private void populateComponents() {
        projectNameTextField.setText(projectName);

        // Figure out whether to enable the reference copy check box...
        createReferenceCopiesCheckBox.setSelected(projectProperties.getCreateReferenceCopyFlag());

        if (projectProperties.getCreateReferenceCopyFlag()) {
            createReferenceFilesCheckBox.setEnabled(true);
            defineAlternateReferenceLocationCheckBox.setEnabled(true);
            deleteReferenceCopiesCheckBox.setEnabled(false);
        } else {
            createReferenceFilesCheckBox.setEnabled(false);
            defineAlternateReferenceLocationCheckBox.setEnabled(false);
            deleteReferenceCopiesCheckBox.setEnabled(true);
        }

        ignoreCaseCheckBox.setSelected(projectProperties.getIgnoreCaseFlag());
        defineAlternateReferenceLocationCheckBox.setSelected(projectProperties.getDefineAlternateReferenceLocationFlag());

        if (projectProperties.getDefineAlternateReferenceLocationFlag()) {
            alternateReferenceLocationTextField.setText(projectProperties.getReferenceLocation());
            if (projectProperties.getCreateReferenceCopyFlag()) {
                alternateReferenceLocationTextField.setEnabled(true);
            }
        } else {
            alternateReferenceLocationTextField.setEnabled(false);
        }
    }

    boolean getIsOK() {
        return isOKFlag;
    }

    boolean getCreateReferenceCopiesFlag() {
        return createReferenceCopyOnServerFlag;
    }

    boolean getCreateOrDeleteCurrentReferenceFilesFlag() {
        return createOrDeleteCurrentReferenceFilesFlag;
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
    private javax.swing.JCheckBox createReferenceFilesCheckBox;
    private javax.swing.JCheckBox defineAlternateReferenceLocationCheckBox;
    private javax.swing.JCheckBox deleteReferenceCopiesCheckBox;
    private javax.swing.JCheckBox ignoreCaseCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
// End of variables declaration//GEN-END:variables
}
