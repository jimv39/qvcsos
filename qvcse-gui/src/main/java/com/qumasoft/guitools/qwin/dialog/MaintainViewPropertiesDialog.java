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
import com.qumasoft.guitools.qwin.ViewOrBranchComboModel;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Maintain view properties dialog.
 * @author Jim Voris
 */
public class MaintainViewPropertiesDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -906025674426107443L;

    private final ImageIcon byDateImageIcon;
    private final ImageIcon byDateFocusImageIcon;
    private boolean isOKFlag;
    private String viewName;
    private boolean isDateBasedViewFlag;
    private boolean isTranslucentBranchFlag;
    private boolean isOpaqueBranchFlag;
    private boolean isReadOnlyViewFlag;
    private Date dateTimeValue;
    private String dateBasedViewBranch;
    private String parentBranchName;
    private final ViewOrBranchComboModel viewOrBranchComboModel;
    private static final String READ_ONLY_DATE_BASED_VIEW_DESCRIPTION =
            "A read only date based view requires you to choose a date that serves as the basis for the view.  "
            + "You must also choose the basis label, branch, or trunk so that the view can figure out which revision "
            + "to associate with the view in the case where a file has multiple branches. You are not allowed to "
            + "perform any checkins or checkouts on this view. It is read-only. "
            + "This view type is a way to see what a project looked like at some time in the past.";
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
            + "anchor for a release.  You need to specify what branch will serve as the parent of this branch.  Typically, "
            + "this is the Trunk";

    /**
     * Creates new form MaintainViewPropertiesDialog.
     *
     * @param parent the parent frame window.
     * @param modal flag to indicate that dialog is modal.
     */
    public MaintainViewPropertiesDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.byDateFocusImageIcon = new ImageIcon(ClassLoader.getSystemResource("images/calendarButton_focus.png"));
        this.byDateImageIcon = new ImageIcon(ClassLoader.getSystemResource("images/calendarButton_small.png"));
        this.viewOrBranchComboModel = new ViewOrBranchComboModel();
        initComponents();
        populateComponents();

        viewOrBranchTypeComboBoxActionPerformed(null);
        setFont();
        center();
    }

    /**
     * Creates new form MaintainViewPropertiesDialog. This constructor is used to display the properties of an existing branch/view.
     *
     * @param parent the parent frame window.
     * @param modal flag to indicate whether dialog should be modal.
     * @param view the name of the view/branch whose properties we will display.
     * @param remoteViewProperties the remote view properties of the view/branch that we will display.
     */
    public MaintainViewPropertiesDialog(java.awt.Frame parent, boolean modal, String view, RemoteViewProperties remoteViewProperties) {
        super(parent, modal);
        this.byDateFocusImageIcon = new ImageIcon(ClassLoader.getSystemResource("images/calendarButton_focus.png"));
        this.byDateImageIcon = new ImageIcon(ClassLoader.getSystemResource("images/calendarButton_small.png"));
        this.viewOrBranchComboModel = new ViewOrBranchComboModel();
        this.viewName = view;
        isReadOnlyViewFlag = remoteViewProperties.getIsReadOnlyViewFlag();

        isDateBasedViewFlag = remoteViewProperties.getIsDateBasedViewFlag();
        isTranslucentBranchFlag = remoteViewProperties.getIsTranslucentBranchFlag();
        isOpaqueBranchFlag = remoteViewProperties.getIsOpaqueBranchFlag();

        initComponents();
        populateComponents();

        if (isDateBasedViewFlag) {
            dateTimeValue = remoteViewProperties.getDateBasedDate();
            dateBasedViewBranch = remoteViewProperties.getDateBasedViewBranch();
            dateTextField.setText(dateTimeValue.toString());
            dateBasedViewLabelComboBox.setSelectedItem(dateBasedViewBranch);
            dateTextField.setEditable(false);
            viewOrBranchTypeComboBox.setSelectedItem(ViewOrBranchComboModel.READ_ONLY_DATE_BASED_VIEW);
        } else if (isTranslucentBranchFlag) {
            isReadOnlyViewFlag = false;
            viewOrBranchTypeComboBox.setSelectedItem(ViewOrBranchComboModel.FEATURE_BRANCH);
        } else if (isOpaqueBranchFlag) {
            isReadOnlyViewFlag = false;
            viewOrBranchTypeComboBox.setSelectedItem(ViewOrBranchComboModel.OPAQUE_BRANCH);
        }

        // They can look, but cannot change anything.
        viewNameTextField.setEditable(false);
        labelComboBox.setEnabled(false);
        dateBasedViewLabelComboBox.setEnabled(false);
        viewOrBranchTypeComboBox.setEnabled(false);

        okButton.setEnabled(false);
        cancelButton.setText("Close");

        viewOrBranchTypeComboBoxActionPerformed(null);
        setFont();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewNameLabel = new javax.swing.JLabel();
        viewNameTextField = new javax.swing.JTextField();
        viewOrBranchTypeLabel = new javax.swing.JLabel();
        viewOrBranchTypeComboBox = new javax.swing.JComboBox();
        describeViewOrBranchTextArea = new javax.swing.JTextArea();
        chooseLabelLabel = new javax.swing.JLabel();
        labelComboBox = new javax.swing.JComboBox();
        chooseDateLabel = new javax.swing.JLabel();
        dateTextField = new javax.swing.JTextField();
        byDateButton = new javax.swing.JButton(this.byDateImageIcon);
        dateBasedViewLabelLabel = new javax.swing.JLabel();
        dateBasedViewLabelComboBox = new javax.swing.JComboBox();
        chooseParentBranchLabel = new javax.swing.JLabel();
        chooseParentBranchComboBox = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("View or Branch Properties");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setName("MaintainViewPropertiesDialog"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        viewNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        viewNameLabel.setText("Define View or Branch Name:");

        viewNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        viewOrBranchTypeLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        viewOrBranchTypeLabel.setText("Choose View or Branch Type:");

        viewOrBranchTypeComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        viewOrBranchTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        viewOrBranchTypeComboBox.setToolTipText("Choose the type of view or type of branch.");
        viewOrBranchTypeComboBox.setDoubleBuffered(true);
        viewOrBranchTypeComboBox.setEditor(null);
        viewOrBranchTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                viewOrBranchTypeComboBoxItemStateChanged(evt);
            }
        });
        viewOrBranchTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewOrBranchTypeComboBoxActionPerformed(evt);
            }
        });

        describeViewOrBranchTextArea.setEditable(false);
        describeViewOrBranchTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("InternalFrame.background"));
        describeViewOrBranchTextArea.setColumns(20);
        describeViewOrBranchTextArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        describeViewOrBranchTextArea.setLineWrap(true);
        describeViewOrBranchTextArea.setRows(5);
        describeViewOrBranchTextArea.setWrapStyleWord(true);
        describeViewOrBranchTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        chooseLabelLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        chooseLabelLabel.setText("Choose Label for this label based view:");
        chooseLabelLabel.setEnabled(false);

        labelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelComboBox.setToolTipText("Choose  label for this label based view");
        labelComboBox.setEnabled(false);

        chooseDateLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        chooseDateLabel.setText("Choose Date for this date based view:");
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

        dateBasedViewLabelLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        dateBasedViewLabelLabel.setText("Choose Label (or Trunk) for this date based view:");
        dateBasedViewLabelLabel.setEnabled(false);

        dateBasedViewLabelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        dateBasedViewLabelComboBox.setEnabled(false);

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
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(describeViewOrBranchTextArea)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(viewNameLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, viewNameTextField)
                            .add(viewOrBranchTypeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(viewOrBranchTypeComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(chooseLabelLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, labelComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(dateTextField)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byDateButton)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(dateBasedViewLabelLabel)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(chooseDateLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(44, 44, 44))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, chooseParentBranchComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, chooseParentBranchLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(dateBasedViewLabelComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(viewNameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(viewNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(viewOrBranchTypeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(viewOrBranchTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(describeViewOrBranchTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooseLabelLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooseDateLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(dateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dateBasedViewLabelLabel))
                    .add(byDateButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dateBasedViewLabelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooseParentBranchLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooseParentBranchComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap(41, Short.MAX_VALUE))
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

private void viewOrBranchTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewOrBranchTypeComboBoxActionPerformed
    // Display different text in the text area to describe the selected view/branch type.
    String selectedView = (String) viewOrBranchTypeComboBox.getSelectedItem();
    int selectedViewType = viewOrBranchComboModel.getViewOrBranchType(selectedView);
    clearFlags();

    switch (selectedViewType) {
        case ViewOrBranchComboModel.READ_ONLY_DATE_BASED_VIEW_TYPE: {
            describeViewOrBranchTextArea.setText(READ_ONLY_DATE_BASED_VIEW_DESCRIPTION);
            isReadOnlyViewFlag = true;
            isDateBasedViewFlag = true;
            enableReadOnlyLabelBasedViewControls(false);
            enableReadWriteLabelBasedViewControls(false);
            enableTranslucentBranchControls(false);
            enableOpaqueBranchControls(false);
            enableReadOnlyDateBasedViewControls(true);
            break;
        }
        case ViewOrBranchComboModel.FEATURE_BRANCH_TYPE: {
            describeViewOrBranchTextArea.setText(FEATURE_BRANCH_DESCRIPTION);
            isTranslucentBranchFlag = true;
            enableReadOnlyDateBasedViewControls(false);
            enableReadOnlyLabelBasedViewControls(false);
            enableReadWriteLabelBasedViewControls(false);
            enableOpaqueBranchControls(false);
            enableTranslucentBranchControls(true);
            break;
        }
        case ViewOrBranchComboModel.OPAQUE_BRANCH_TYPE:
        default: {
            describeViewOrBranchTextArea.setText(OPAQUE_BRANCH_DESCRIPTION);
            isOpaqueBranchFlag = true;
            enableReadOnlyDateBasedViewControls(false);
            enableReadOnlyLabelBasedViewControls(false);
            enableReadWriteLabelBasedViewControls(false);
            enableTranslucentBranchControls(false);
            enableOpaqueBranchControls(true);
            break;
        }
    }
}//GEN-LAST:event_viewOrBranchTypeComboBoxActionPerformed

private void viewOrBranchTypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_viewOrBranchTypeComboBoxItemStateChanged
    viewOrBranchTypeComboBoxActionPerformed(null);
}//GEN-LAST:event_viewOrBranchTypeComboBoxItemStateChanged

    private void enableReadOnlyDateBasedViewControls(boolean flag) {
        chooseDateLabel.setEnabled(flag);
        dateBasedViewLabelLabel.setEnabled(flag);
        dateBasedViewLabelComboBox.setEnabled(flag);
        byDateButton.setEnabled(flag);
    }

    private void enableReadOnlyLabelBasedViewControls(boolean flag) {
        chooseLabelLabel.setEnabled(flag);
        labelComboBox.setEnabled(flag);
    }

    private void enableReadWriteLabelBasedViewControls(boolean flag) {
        chooseLabelLabel.setEnabled(flag);
        labelComboBox.setEnabled(flag);
    }

    private void enableTranslucentBranchControls(boolean flag) {
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
        labelComboBox.setModel(new LabelsComboModel());
        viewNameTextField.setText(viewName);

        dateBasedViewLabelComboBox.setModel(new LabelsComboModel(true));

        viewOrBranchTypeComboBox.setModel(viewOrBranchComboModel);
        chooseParentBranchComboBox.setModel(new ParentBranchComboModel());
    }

    private void clearFlags() {
        isDateBasedViewFlag = false;
        isTranslucentBranchFlag = false;
        isOpaqueBranchFlag = false;

        isReadOnlyViewFlag = false;
    }

    public boolean getIsReadOnlyViewFlag() {
        return isReadOnlyViewFlag;
    }

    public boolean getIsDateBasedViewFlag() {
        return isDateBasedViewFlag;
    }

    public boolean getIsTranslucentBranchFlag() {
        return isTranslucentBranchFlag;
    }

    public boolean getIsOpaqueBranchFlag() {
        return isOpaqueBranchFlag;
    }

    public String getParentBranchName() {
        return parentBranchName;
    }

    public String getViewName() {
        return viewName;
    }

    public String getDateBasedViewBranch() {
        return dateBasedViewBranch;
    }

    private String getDateBasedBranchString() {
        String labelString = null;
        if (isDateBasedViewFlag) {
            labelString = (String) dateBasedViewLabelComboBox.getModel().getSelectedItem();
        }
        return labelString;
    }

    public Date getDate() {
        return dateTimeValue;
    }

    private void validateUserData() throws QVCSException {
        String view = viewNameTextField.getText();
        if (view == null || view.length() == 0) {
            viewNameTextField.requestFocusInWindow();
            throw new QVCSException("You must define a view name");
        } else {
            this.viewName = view;
            if (0 == view.compareTo(QVCSConstants.QVCS_TRUNK_VIEW)) {
                viewNameTextField.requestFocusInWindow();
                throw new QVCSException("You cannot use '" + QVCSConstants.QVCS_TRUNK_VIEW + "' as a view name.");
            }
        }

        if (getIsDateBasedViewFlag()) {
            if (dateTimeValue == null) {
                throw new QVCSException("You must define a date.");
            }

            if (getDateBasedBranchString() == null) {
                dateBasedViewLabelComboBox.requestFocusInWindow();
                throw new QVCSException("You must select a label.");
            } else {
                dateBasedViewBranch = getDateBasedBranchString();
            }
        } else if (getIsTranslucentBranchFlag() || getIsOpaqueBranchFlag()) {
            parentBranchName = (String) chooseParentBranchComboBox.getModel().getSelectedItem();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton byDateButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel chooseDateLabel;
    private javax.swing.JLabel chooseLabelLabel;
    private javax.swing.JComboBox chooseParentBranchComboBox;
    private javax.swing.JLabel chooseParentBranchLabel;
    private javax.swing.JComboBox dateBasedViewLabelComboBox;
    private javax.swing.JLabel dateBasedViewLabelLabel;
    private javax.swing.JTextField dateTextField;
    private javax.swing.JTextArea describeViewOrBranchTextArea;
    private javax.swing.JComboBox labelComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel viewNameLabel;
    private javax.swing.JTextField viewNameTextField;
    private javax.swing.JComboBox viewOrBranchTypeComboBox;
    private javax.swing.JLabel viewOrBranchTypeLabel;
    // End of variables declaration//GEN-END:variables
}
