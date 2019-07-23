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

import com.qumasoft.guitools.qwin.BranchComboModel;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Maintain branch properties dialog.
 * @author Jim Voris
 */
public class MaintainBranchPropertiesDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -906025674426107443L;

    private final ImageIcon byDateImageIcon;
    private final ImageIcon byDateFocusImageIcon;
    private boolean isOKFlag;
    private String branchName;
    private boolean isDateBasedBranchFlag;
    private boolean isFeatureBranchFlag;
    private boolean isOpaqueBranchFlag;
    private boolean isReadOnlyBranchFlag;
    private Date dateTimeValue;
    private String parentBranchName;
    private final BranchComboModel branchComboModel;
    private static final String READ_ONLY_DATE_BASED_BRANCH_DESCRIPTION =
            "A read only date based branch requires you to choose a date that serves as the basis for the branch.  "
            + "You must also choose the basis branch, or trunk so that the branch can figure out which revision "
            + "to associate with the branch in the case where a file has multiple branches. You are not allowed to "
            + "perform any checkins or checkouts on this branch. It is read-only. "
            + "This branch type is a way to see what a project looked like at some time in the past.";
    private static final String FEATURE_BRANCH_DESCRIPTION =
            "A feature branch allows checkins made on the feature branch's parent to flow through to the feature branch.  "
            + "This approach makes it a good alternative for working on adding features, since changes "
            + "on the parent are immediately visible on the feature branch. When you are satisfied that your work for "
            + "the feature is complete, you can promote your changes back to the parent branch (typically the Trunk).  "
            + "You need to specify what branch will serve as the parent of this branch. Typically, this is the Trunk.";
    private static final String OPAQUE_BRANCH_DESCRIPTION =
            "NOT YET IMPLEMENTED.  "
            + "On an opaque branch, checkins made on the branch's parent do not flow through to the branch.  "
            + "This approach makes sense when creating a patch release, or when creating a branch that will serve as the "
            + "anchor for a release. You need to specify what branch will serve as the parent of this branch.  Typically, "
            + "this is the Trunk";

    /**
     * Creates new form MaintainBranchPropertiesDialog.
     *
     * @param parent the parent frame window.
     * @param modal flag to indicate that dialog is modal.
     */
    public MaintainBranchPropertiesDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.byDateFocusImageIcon = new ImageIcon(ClassLoader.getSystemResource("images/calendarButton_focus.png"));
        this.byDateImageIcon = new ImageIcon(ClassLoader.getSystemResource("images/calendarButton_small.png"));
        this.branchComboModel = new BranchComboModel();
        initComponents();
        populateComponents();

        branchTypeComboBoxActionPerformed(null);
        setFont();
        center();
    }

    /**
     * Creates new form MaintainBranchPropertiesDialog. This constructor is used to display the properties of an existing branch.
     *
     * @param parent the parent frame window.
     * @param modal flag to indicate whether dialog should be modal.
     * @param branch the name of the branch whose properties we will display.
     * @param remoteBranchProperties the remote branch properties of the branch that we will display.
     */
    public MaintainBranchPropertiesDialog(java.awt.Frame parent, boolean modal, String branch, RemoteBranchProperties remoteBranchProperties) {
        super(parent, modal);
        this.byDateFocusImageIcon = new ImageIcon(ClassLoader.getSystemResource("images/calendarButton_focus.png"));
        this.byDateImageIcon = new ImageIcon(ClassLoader.getSystemResource("images/calendarButton_small.png"));
        this.branchComboModel = new BranchComboModel();
        this.branchName = branch;
        isReadOnlyBranchFlag = remoteBranchProperties.getIsReadOnlyBranchFlag();

        isDateBasedBranchFlag = remoteBranchProperties.getIsDateBasedBranchFlag();
        isFeatureBranchFlag = remoteBranchProperties.getIsFeatureBranchFlag();
        isOpaqueBranchFlag = remoteBranchProperties.getIsOpaqueBranchFlag();

        initComponents();
        populateComponents();

        if (isDateBasedBranchFlag) {
            dateTimeValue = remoteBranchProperties.getDateBasedDate();
            dateTextField.setText(dateTimeValue.toString());
            dateTextField.setEditable(false);
            branchTypeComboBox.setSelectedItem(BranchComboModel.READ_ONLY_DATE_BASED_BRANCH);
        } else if (isFeatureBranchFlag) {
            isReadOnlyBranchFlag = false;
            branchTypeComboBox.setSelectedItem(BranchComboModel.FEATURE_BRANCH);
        } else if (isOpaqueBranchFlag) {
            isReadOnlyBranchFlag = false;
            branchTypeComboBox.setSelectedItem(BranchComboModel.OPAQUE_BRANCH);
        }

        // They can look, but cannot change anything.
        branchNameTextField.setEditable(false);
        branchTypeComboBox.setEnabled(false);

        okButton.setEnabled(false);
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
        chooseDateLabel = new javax.swing.JLabel();
        dateTextField = new javax.swing.JTextField();
        byDateButton = new javax.swing.JButton(this.byDateImageIcon);
        chooseParentBranchLabel = new javax.swing.JLabel();
        chooseParentBranchComboBox = new javax.swing.JComboBox();
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
        branchTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        branchTypeComboBox.setToolTipText("Choose the type of branch.");
        branchTypeComboBox.setDoubleBuffered(true);
        branchTypeComboBox.setEditor(null);
        branchTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                branchTypeComboBoxItemStateChanged(evt);
            }
        });
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

        chooseDateLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        chooseDateLabel.setText("Choose Date for this date based branch:");
        chooseDateLabel.setEnabled(false);

        dateTextField.setEditable(false);
        dateTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        byDateButton.setIcon(byDateImageIcon);
        byDateButton.setToolTipText("Click to select date/time");
        byDateButton.setAlignmentY(0.0F);
        byDateButton.setBorderPainted(false);
        byDateButton.setContentAreaFilled(false);
        byDateButton.setDefaultCapable(false);
        byDateButton.setDisabledIcon(byDateImageIcon);
        byDateButton.setDisabledSelectedIcon(byDateImageIcon);
        byDateButton.setDoubleBuffered(true);
        byDateButton.setEnabled(false);
        byDateButton.setFocusPainted(false);
        byDateButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        byDateButton.setIconTextGap(0);
        byDateButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        byDateButton.setPressedIcon(byDateFocusImageIcon);
        byDateButton.setRolloverIcon(byDateFocusImageIcon);
        byDateButton.setRolloverSelectedIcon(byDateFocusImageIcon);
        byDateButton.setSelectedIcon(byDateFocusImageIcon);
        byDateButton.setVerifyInputWhenFocusTarget(false);
        byDateButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        byDateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                byDateButtonActionPerformed(evt);
            }
        });

        chooseParentBranchLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        chooseParentBranchLabel.setText("Choose Parent branch:");
        chooseParentBranchLabel.setEnabled(false);

        chooseParentBranchComboBox.setEnabled(false);

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
                            .add(branchNameLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, branchNameTextField)
                            .add(branchTypeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(branchTypeComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(chooseDateLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(44, 44, 44))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(chooseParentBranchComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(dateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 358, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(byDateButton)
                        .addContainerGap())
                    .add(chooseParentBranchLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(branchNameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(branchNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(branchTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(branchTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(describBranchTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(chooseDateLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(byDateButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooseParentBranchLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooseParentBranchComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void byDateButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_byDateButtonActionPerformed
    {//GEN-HEADEREND:event_byDateButtonActionPerformed
        DateTimeChooserDialog dateTimeChooserDialog;
        if (dateTimeValue == null) {
            dateTimeChooserDialog = new DateTimeChooserDialog(QWinFrame.getQWinFrame());
        } else {
            dateTimeChooserDialog = new DateTimeChooserDialog(QWinFrame.getQWinFrame(), dateTimeValue);
        }

        int x = byDateButton.getX() + byDateButton.getWidth() + 5;
        int y = byDateButton.getY() + byDateButton.getHeight() + 9;
        int dialogX = getX();
        int dialogY = getY();
        dateTimeChooserDialog.setLocation(dialogX + x, dialogY + y);
        dateTimeChooserDialog.setVisible(true);
        if (dateTimeChooserDialog.getIsOK()) {
            dateTimeValue = dateTimeChooserDialog.getDate();
            dateTextField.setText(dateTimeValue.toString());
        }
    }//GEN-LAST:event_byDateButtonActionPerformed

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
        case BranchComboModel.READ_ONLY_DATE_BASED_BRANCH_TYPE: {
            describBranchTextArea.setText(READ_ONLY_DATE_BASED_BRANCH_DESCRIPTION);
            isReadOnlyBranchFlag = true;
            isDateBasedBranchFlag = true;
            enableReadOnlyLabelBasedBranchControls(false);
            enableReadWriteLabelBasedBranchControls(false);
            enableFeatureBranchControls(false);
            enableOpaqueBranchControls(false);
            enableReadOnlyDateBasedBranchControls(true);
            break;
        }
        case BranchComboModel.FEATURE_BRANCH_TYPE: {
            describBranchTextArea.setText(FEATURE_BRANCH_DESCRIPTION);
            isFeatureBranchFlag = true;
            enableReadOnlyDateBasedBranchControls(false);
            enableReadOnlyLabelBasedBranchControls(false);
            enableReadWriteLabelBasedBranchControls(false);
            enableOpaqueBranchControls(false);
            enableFeatureBranchControls(true);
            break;
        }
        case BranchComboModel.OPAQUE_BRANCH_TYPE:
        default: {
            describBranchTextArea.setText(OPAQUE_BRANCH_DESCRIPTION);
            isOpaqueBranchFlag = true;
            enableReadOnlyDateBasedBranchControls(false);
            enableReadOnlyLabelBasedBranchControls(false);
            enableReadWriteLabelBasedBranchControls(false);
            enableFeatureBranchControls(false);
            enableOpaqueBranchControls(true);
            break;
        }
    }
}//GEN-LAST:event_branchTypeComboBoxActionPerformed

private void branchTypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_branchTypeComboBoxItemStateChanged
    branchTypeComboBoxActionPerformed(null);
}//GEN-LAST:event_branchTypeComboBoxItemStateChanged

    private void enableReadOnlyDateBasedBranchControls(boolean flag) {
        chooseDateLabel.setEnabled(flag);
        byDateButton.setEnabled(flag);
    }

    private void enableReadOnlyLabelBasedBranchControls(boolean flag) {
    }

    private void enableReadWriteLabelBasedBranchControls(boolean flag) {
    }

    private void enableFeatureBranchControls(boolean flag) {
        chooseParentBranchLabel.setEnabled(flag);
        chooseParentBranchComboBox.setEnabled(flag);
    }

    private void enableOpaqueBranchControls(boolean flag) {
        chooseParentBranchLabel.setEnabled(flag);
        chooseParentBranchComboBox.setEnabled(flag);
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

        branchTypeComboBox.setModel(branchComboModel);
        chooseParentBranchComboBox.setModel(new ParentBranchComboModel());
    }

    private void clearFlags() {
        isDateBasedBranchFlag = false;
        isFeatureBranchFlag = false;
        isOpaqueBranchFlag = false;

        isReadOnlyBranchFlag = false;
    }

    public boolean getIsReadOnlyBranchFlag() {
        return isReadOnlyBranchFlag;
    }

    public boolean getIsDateBasedBranchFlag() {
        return isDateBasedBranchFlag;
    }

    public boolean getIsFeatureBranchFlag() {
        return isFeatureBranchFlag;
    }

    public boolean getIsOpaqueBranchFlag() {
        return isOpaqueBranchFlag;
    }

    public String getParentBranchName() {
        return parentBranchName;
    }

    public String getBranchName() {
        return branchName;
    }

    public Date getDate() {
        return dateTimeValue;
    }

    private void validateUserData() throws QVCSException {
        String branch = branchNameTextField.getText();
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

        if (getIsDateBasedBranchFlag()) {
            if (dateTimeValue == null) {
                throw new QVCSException("You must define a date.");
            }
        }
        parentBranchName = (String) chooseParentBranchComboBox.getModel().getSelectedItem();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel branchNameLabel;
    private javax.swing.JTextField branchNameTextField;
    private javax.swing.JComboBox branchTypeComboBox;
    private javax.swing.JLabel branchTypeLabel;
    private javax.swing.JButton byDateButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel chooseDateLabel;
    private javax.swing.JComboBox chooseParentBranchComboBox;
    private javax.swing.JLabel chooseParentBranchLabel;
    private javax.swing.JTextField dateTextField;
    private javax.swing.JTextArea describBranchTextArea;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
