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

import com.qumasoft.guitools.qwin.BranchComboModel;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.TagComboModel;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Maintain branch properties dialog.
 * @author Jim Voris
 */
public class MaintainBranchPropertiesDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -906025674426107443L;

    private boolean isOKFlag;
    private String branchName;
    private boolean isTagBasedBranchFlag;
    private boolean isFeatureBranchFlag;
    private boolean isReleaseBranchFlag;
    private boolean isReadOnlyBranchFlag;
    private String tagString;
    private String parentBranchName;
    private final BranchComboModel branchComboModel;
    private final TagComboModel tagComboModel;
    private static final String TRUNK_BRANCH_DESCRIPTION
            = "The Trunk branch is the default branch. You can perform checkins on this branch, and create child branches.";
    private static final String READ_ONLY_TAG_BASED_BRANCH_DESCRIPTION =
            "A read only tag based branch requires you to choose a tag that serves as the basis for the branch.  "
            + "The basis branch will be the current branch. You are not allowed to "
            + "perform any checkins on this new branch. It is read-only. "
            + "This branch type is a way to see what this parent branch looked like at the time the branch tag was created.";
    private static final String FEATURE_BRANCH_DESCRIPTION =
            "A feature branch allows checkins made on the feature branch's parent to flow through to the feature branch.  "
            + "This approach makes it a good alternative for working on adding features, since changes "
            + "on the parent are immediately visible on the feature branch. When you are satisfied that your work for "
            + "the feature is complete, you can promote your changes back to the parent branch (typically the Trunk).  "
            + "The current branch will serve as the parent of this branch.";
    private static final String RELEASE_BRANCH_DESCRIPTION
            = "On a release branch, checkins made on the branch's parent do not flow through to the branch.  "
            + "This approach makes sense when creating a patch release, or when creating a branch that will serve as the "
            + "anchor for a release. The current branch will serve as the parent of this branch.  Typically, "
            + "this is the Trunk. A release branch is NOT allowed to have child branches.";

    /**
     * Creates new form MaintainBranchPropertiesDialog.
     *
     * @param parent the parent frame window.
     * @param pBranchName the parent branch name.
     * @param tagList the list of tags for the current project/branch.
     * @param modal flag to indicate that dialog is modal.
     */
    public MaintainBranchPropertiesDialog(java.awt.Frame parent, String pBranchName, List<String> tagList, boolean modal) {
        super(parent, modal);
        this.branchComboModel = new BranchComboModel();
        // Remove the Trunk...
        branchComboModel.removeElementAt(0);

        this.tagComboModel = new TagComboModel(tagList);
        this.parentBranchName = pBranchName;

        initComponents();

        branchComboModel.setSelectedItem(BranchComboModel.FEATURE_BRANCH);
        branchTypeComboBox.setModel(branchComboModel);
        describBranchTextArea.setText(FEATURE_BRANCH_DESCRIPTION);

        populateComponents();

        setFont();
        center();
    }

    /**
     * Creates new form MaintainBranchPropertiesDialog. This constructor is used
     * to display the properties of an existing branch.
     *
     * @param parent the parent frame window.
     * @param pBranchName the parent branch name.
     * @param tagList the list of tags for the current project/branch.
     * @param modal flag to indicate whether dialog should be modal.
     * @param branch the name of the branch whose properties we will display.
     * @param remoteBranchProperties the remote branch properties of the branch that we will display.
     */
    public MaintainBranchPropertiesDialog(java.awt.Frame parent, String pBranchName, List<String> tagList, boolean modal, String branch, RemoteBranchProperties remoteBranchProperties) {
        super(parent, modal);
        this.branchComboModel = new BranchComboModel();
        this.tagComboModel = new TagComboModel(tagList);
        this.branchName = branch;
        this.parentBranchName = pBranchName;
        isReadOnlyBranchFlag = remoteBranchProperties.getIsReadOnlyBranchFlag();

        isTagBasedBranchFlag = remoteBranchProperties.getIsTagBasedBranchFlag();
        isFeatureBranchFlag = remoteBranchProperties.getIsFeatureBranchFlag();
        isReleaseBranchFlag = remoteBranchProperties.getIsReleaseBranchFlag();

        initComponents();
        branchTypeComboBox.setModel(branchComboModel);
        populateComponents();

        if (isTagBasedBranchFlag) {
            tagString = remoteBranchProperties.getTagBasedTag();
            this.tagComboModel.addElement(tagString);
            branchTypeComboBox.setSelectedItem(BranchComboModel.READ_ONLY_TAG_BASED_BRANCH);
            chooseTagComboBox.setSelectedItem(tagString);
            chooseTagLabel.setText("Tag for this tag based branch:");
        } else if (isFeatureBranchFlag) {
            isReadOnlyBranchFlag = false;
            branchTypeComboBox.setSelectedItem(BranchComboModel.FEATURE_BRANCH);
        } else if (isReleaseBranchFlag) {
            isReadOnlyBranchFlag = false;
            branchTypeComboBox.setSelectedItem(BranchComboModel.RELEASE_BRANCH);
        } else {
            isReadOnlyBranchFlag = false;
            branchTypeComboBox.setSelectedItem(BranchComboModel.TRUNK_BRANCH);
        }

        // They can look, but cannot change anything.
        branchNameTextField.setEditable(false);
        branchTypeComboBox.setEnabled(false);
        chooseTagComboBox.setEnabled(false);

        okButton.setEnabled(false);
        branchNameLabel.setText("Branch Name:");
        cancelButton.setText("Close");

        branchTypeComboBoxActionPerformed(null);
        setFont();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        branchNameLabel = new javax.swing.JLabel();
        branchNameTextField = new javax.swing.JTextField();
        branchTypeLabel = new javax.swing.JLabel();
        branchTypeComboBox = new javax.swing.JComboBox();
        describBranchTextArea = new javax.swing.JTextArea();
        chooseTagLabel = new javax.swing.JLabel();
        chooseTagComboBox = new javax.swing.JComboBox<>();
        parentBranchLabel = new javax.swing.JLabel();
        parentBranchTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Branch Properties");
        setName("MaintainBranchPropertiesDialog"); // NOI18N
        setPreferredSize(new java.awt.Dimension(500, 425));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        branchNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        branchNameLabel.setText("Define Branch Name:");
        branchNameLabel.setToolTipText("");

        branchNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        branchNameTextField.setToolTipText("Enter branch name");

        branchTypeLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        branchTypeLabel.setText("Choose Branch Type:");

        branchTypeComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        branchTypeComboBox.setToolTipText("Choose the type of branch.");
        branchTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                branchTypeComboBoxActionPerformed(evt);
            }
        });

        describBranchTextArea.setEditable(false);
        describBranchTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("InternalFrame.background"));
        describBranchTextArea.setColumns(20);
        describBranchTextArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        describBranchTextArea.setLineWrap(true);
        describBranchTextArea.setRows(5);
        describBranchTextArea.setTabSize(4);
        describBranchTextArea.setWrapStyleWord(true);
        describBranchTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        chooseTagLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        chooseTagLabel.setText("Choose tag for this tag based branch:");
        chooseTagLabel.setEnabled(false);

        chooseTagComboBox.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        chooseTagComboBox.setModel(this.tagComboModel);
        chooseTagComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chooseTagComboBoxItemStateChanged(evt);
            }
        });
        chooseTagComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseTagComboBoxActionPerformed(evt);
            }
        });

        parentBranchLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        parentBranchLabel.setText("Parent branch:");

        parentBranchTextField.setEditable(false);
        parentBranchTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(describBranchTextArea)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(branchNameLabel)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, branchNameTextField)
                            .add(branchTypeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(branchTypeComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(parentBranchLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(chooseTagLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(chooseTagComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(44, 44, 44))
                    .add(layout.createSequentialGroup()
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(parentBranchTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 240, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(branchNameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(branchNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(branchTypeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(branchTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(describBranchTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooseTagLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooseTagComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(parentBranchLabel)
                .add(3, 3, 3)
                .add(parentBranchTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
    {//GEN-HEADEREND:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        isOKFlag = false;
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        try {
            validateUserData();
            isOKFlag = true;
            closeDialog(null);
        } catch (final QVCSException e) {
            // Run the update on the Swing thread.
            Runnable later = new Runnable() {

                @Override
                public void run() {
                    // Let the user know that there was a problem.
                    JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), e.getLocalizedMessage(), "Invalid Choices", JOptionPane.WARNING_MESSAGE);
                }
            };
            SwingUtilities.invokeLater(later);
        }
    }//GEN-LAST:event_okButtonActionPerformed

private void branchTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_branchTypeComboBoxActionPerformed
    // Display different text in the text area to describe the selected branch type.
    String selectedBranch = (String) branchTypeComboBox.getSelectedItem();
    int selectedBranchType = branchComboModel.getBranchType(selectedBranch);
    clearFlags();

    switch (selectedBranchType) {
        case BranchComboModel.READ_ONLY_TAG_BASED_BRANCH_TYPE: {
            describBranchTextArea.setText(READ_ONLY_TAG_BASED_BRANCH_DESCRIPTION);
            isReadOnlyBranchFlag = true;
            isTagBasedBranchFlag = true;
            enableFeatureBranchControls(false);
            enableReleaseBranchControls(false);
            enableReadOnlyTagBasedBranchControls(true);
            break;
        }
        case BranchComboModel.FEATURE_BRANCH_TYPE: {
            describBranchTextArea.setText(FEATURE_BRANCH_DESCRIPTION);
            isFeatureBranchFlag = true;
            isReadOnlyBranchFlag = false;
            enableReadOnlyTagBasedBranchControls(false);
            enableReleaseBranchControls(false);
            enableFeatureBranchControls(true);
            break;
        }
        case BranchComboModel.RELEASE_BRANCH_TYPE: {
            describBranchTextArea.setText(RELEASE_BRANCH_DESCRIPTION);
            isReleaseBranchFlag = true;
            enableReadOnlyTagBasedBranchControls(false);
            enableFeatureBranchControls(false);
            enableReleaseBranchControls(true);
            break;
        }
        default: {
            describBranchTextArea.setText(TRUNK_BRANCH_DESCRIPTION);
            isReadOnlyBranchFlag = false;
            enableReadOnlyTagBasedBranchControls(false);
            enableReleaseBranchControls(false);
            enableFeatureBranchControls(true);
            break;
        }
    }
}//GEN-LAST:event_branchTypeComboBoxActionPerformed

    private void chooseTagComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseTagComboBoxActionPerformed
        // Get the tag string.
        String selectedTag = (String) chooseTagComboBox.getSelectedItem();
        this.tagString = selectedTag;
    }//GEN-LAST:event_chooseTagComboBoxActionPerformed

    private void chooseTagComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chooseTagComboBoxItemStateChanged
        chooseTagComboBoxActionPerformed(null);
    }//GEN-LAST:event_chooseTagComboBoxItemStateChanged

    private void enableReadOnlyTagBasedBranchControls(boolean flag) {
        chooseTagLabel.setEnabled(flag);
        chooseTagComboBox.setEnabled(flag);
    }

    private void enableFeatureBranchControls(boolean flag) {
    }

    private void enableReleaseBranchControls(boolean flag) {
    }

    @Override
    public void dismissDialog() {
        cancelButtonActionPerformed(null);
    }

    public boolean getIsOK() {
        return isOKFlag;
    }

    private void populateComponents() {
        branchNameTextField.setText(branchName);
        parentBranchTextField.setText(parentBranchName);
    }

    private void clearFlags() {
        isTagBasedBranchFlag = false;
        isFeatureBranchFlag = false;
        isReleaseBranchFlag = false;

        isReadOnlyBranchFlag = false;
    }

    public boolean getIsReadOnlyBranchFlag() {
        return isReadOnlyBranchFlag;
    }

    public boolean getIsTagBasedBranchFlag() {
        return isTagBasedBranchFlag;
    }

    public boolean getIsFeatureBranchFlag() {
        return isFeatureBranchFlag;
    }

    public boolean getIsReleaseBranchFlag() {
        return isReleaseBranchFlag;
    }

    public String getParentBranchName() {
        return parentBranchName;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getTag() {
        return tagString;
    }

    private void validateUserData() throws QVCSException {
        String branch = branchNameTextField.getText();
        String selectedTag = (String) chooseTagComboBox.getSelectedItem();
        this.tagString = selectedTag;
        if (branch == null || branch.length() == 0) {
            branchNameTextField.requestFocusInWindow();
            throw new QVCSException("You must define a branch name");
        } else {
            this.branchName = branch;
            if (0 == branch.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                branchNameTextField.requestFocusInWindow();
                throw new QVCSException("You cannot use '" + QVCSConstants.QVCS_TRUNK_BRANCH + "' as a branch name.");
            }
        }

        if (getIsTagBasedBranchFlag()) {
            if (this.tagString == null) {
                throw new QVCSException("You must choose a tag.");
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel branchNameLabel;
    private javax.swing.JTextField branchNameTextField;
    private javax.swing.JComboBox branchTypeComboBox;
    private javax.swing.JLabel branchTypeLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox<String> chooseTagComboBox;
    private javax.swing.JLabel chooseTagLabel;
    private javax.swing.JTextArea describBranchTextArea;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel parentBranchLabel;
    private javax.swing.JTextField parentBranchTextField;
    // End of variables declaration//GEN-END:variables
}
