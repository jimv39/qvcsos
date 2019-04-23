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

import com.qumasoft.qvcslib.Utility;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;

/**
 * Get view utility command dialog. Used to define what utility to use for viewing files with a given file extension.
 * @author Jim Voris
 */
public class GetViewUtilityCommandDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -4521896383884385457L;

    private String selectedUtility;
    private boolean isOKFlag;
    private boolean useForFilesOfThisExtensionFlag;
    private final DefaultComboBoxModel<String> utilityComboModel;

    /**
     * Create a new get view utility command dialog.
     * @param parent the parent frame.
     * @param modal is this modal.
     * @param existingCommands the list of existing commands.
     */
    public GetViewUtilityCommandDialog(java.awt.Frame parent, boolean modal, String[] existingCommands) {
        super(parent, modal);
        initComponents();
        utilityComboModel = new DefaultComboBoxModel<>(existingCommands);
        viewUtilityComboBox.setModel(utilityComboModel);

        // If there is anything in the combo, select the first element.
        if (existingCommands.length > 0) {
            utilityComboModel.setSelectedItem(utilityComboModel.getElementAt(0));
            selectedUtility = (String) viewUtilityComboBox.getModel().getSelectedItem();
        }
        setFont();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        viewUtilityLabel = new javax.swing.JLabel();
        viewUtilityComboBox = new javax.swing.JComboBox();
        useForThisExtensionCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Choose View Utility");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        okButton.setText("   OK   ");
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

        browseButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        browseButton.setText("Browse...");
        browseButton.setMaximumSize(new java.awt.Dimension(80, 25));
        browseButton.setMinimumSize(new java.awt.Dimension(80, 25));
        browseButton.setPreferredSize(new java.awt.Dimension(80, 25));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        viewUtilityLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        viewUtilityLabel.setLabelFor(viewUtilityComboBox);
        viewUtilityLabel.setText("Choose the program to use for viewing this file.");

        viewUtilityComboBox.setEditable(true);
        viewUtilityComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        viewUtilityComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewUtilityComboBoxActionPerformed(evt);
            }
        });

        useForThisExtensionCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        useForThisExtensionCheckBox.setText("Always use the selected program to open this kind of file.");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(viewUtilityLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 500, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createSequentialGroup()
                                        .add(viewUtilityComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 500, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(browseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(layout.createSequentialGroup()
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(useForThisExtensionCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 500, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(56, 56, 56))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(viewUtilityLabel)
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(viewUtilityComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(browseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(4, 4, 4)
                        .add(useForThisExtensionCheckBox)
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void viewUtilityComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewUtilityComboBoxActionPerformed
    {//GEN-HEADEREND:event_viewUtilityComboBoxActionPerformed
        selectedUtility = (String) viewUtilityComboBox.getModel().getSelectedItem();
    }//GEN-LAST:event_viewUtilityComboBoxActionPerformed

    private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
    {//GEN-HEADEREND:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        isOKFlag = true;
        useForFilesOfThisExtensionFlag = getUseForFilesOfThisTypeFlag();
        closeDialog(null);
    }//GEN-LAST:event_okButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseButtonActionPerformed
    {//GEN-HEADEREND:event_browseButtonActionPerformed
        String viewUtility = selectUtility("Select the utility for viewing this file");
        if (viewUtility.length() > 0) {
            this.selectedUtility = viewUtility;
            utilityComboModel.addElement(viewUtility);
            utilityComboModel.setSelectedItem(viewUtility);
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    @Override
    public void dismissDialog() {
        cancelButtonActionPerformed(null);
    }

    public boolean getUseForFilesOfThisExtensionFlag() {
        return useForFilesOfThisExtensionFlag;
    }

    public boolean getIsOK() {
        return isOKFlag;
    }

    private boolean getUseForFilesOfThisTypeFlag() {
        boolean retVal = false;
        Object flag = useForThisExtensionCheckBox.getSelectedObjects();
        if (flag != null) {
            retVal = true;
        }
        return retVal;
    }

    private String selectUtility(String dialogTitle) {
        String viewUtility;
        String initialDirectory = System.getProperty("user.dir");
        JFileChooser chooser = new JFileChooser();
        UtilityFileFilter filter = new UtilityFileFilter();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(dialogTitle);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setApproveButtonText("Select");
        if (initialDirectory.length() > 0) {
            chooser.setCurrentDirectory(new File(initialDirectory));
        }
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            viewUtility = chooser.getSelectedFile().getAbsolutePath();
            if (Utility.isMacintosh()) {
                viewUtility = adjustUtilityPath(viewUtility);
            }
        } else {
            viewUtility = "";
        }
        return viewUtility;
    }

    public String getSelectedUtility() {
        return selectedUtility;
    }

    private String adjustUtilityPath(String viewUtility) {
        String returnViewUtility = viewUtility;
        if (viewUtility.endsWith(".app")) {
            int endIndex = viewUtility.lastIndexOf('.');
            int startIndex = 1 + viewUtility.lastIndexOf('/');
            String shortUtilityName = viewUtility.substring(startIndex, endIndex);
            returnViewUtility = viewUtility + "/Contents/MacOS/" + shortUtilityName;
        }
        return returnViewUtility;
    }

    static class UtilityFileFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(java.io.File file) {
            return file.isFile() || file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Show all files";
        }
    }

// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox useForThisExtensionCheckBox;
    private javax.swing.JComboBox viewUtilityComboBox;
    private javax.swing.JLabel viewUtilityLabel;
// End of variables declaration//GEN-END:variables
}
