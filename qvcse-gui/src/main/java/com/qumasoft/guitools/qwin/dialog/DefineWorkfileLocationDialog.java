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

import com.qumasoft.guitools.qwin.ProjectTreeControl;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.UserLocationProperties;
import java.io.File;
import javax.swing.JFileChooser;

/**
 * Dialog to allow the user to define the location of their workfiles.
 *
 * @author  Jim Voris
 */
public class DefineWorkfileLocationDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = 2083655786319949604L;

    private String workfileLocation;
    private boolean isOKFlag;
    private UserLocationProperties userLocationProperties;

    /**
     * Create a new define workfile location dialog.
     * @param parent the parent frame.
     */
    public DefineWorkfileLocationDialog(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        populateComponents();
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

        locationOfYourFilesLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        workfilesButton = new javax.swing.JButton();
        workfileLocationValue = new javax.swing.JTextField();

        setTitle("Define Workfile Location");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        locationOfYourFilesLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        locationOfYourFilesLabel.setText("Location of your files:");
        locationOfYourFilesLabel.setToolTipText("");

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

        workfilesButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        workfilesButton.setText("...");
        workfilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                workfilesButtonActionPerformed(evt);
            }
        });

        workfileLocationValue.setEditable(false);
        workfileLocationValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(locationOfYourFilesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(layout.createSequentialGroup()
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(layout.createSequentialGroup()
                                                .add(workfileLocationValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(8, 8, 8)
                                                .add(workfilesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(locationOfYourFilesLabel)
                        .add(4, 4, 4)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(workfileLocationValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(workfilesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(okButton)
                                .add(cancelButton))
                        .add(15, 15, 15))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void workfilesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_workfilesButtonActionPerformed
    {//GEN-HEADEREND:event_workfilesButtonActionPerformed
        // Add your handling code here:
        String selectedDirectory = selectDirectory("Select your workfile directory", workfileLocationValue.getText());
        if (selectedDirectory.length() > 0) {
            workfileLocation = selectedDirectory;
            workfileLocationValue.setText(workfileLocation);
        }
    }//GEN-LAST:event_workfilesButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        closeDialog(null);
        isOKFlag = true;
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

    private void populateComponents() {
        userLocationProperties = QWinFrame.getQWinFrame().getUserLocationProperties();
        String activeServerName = ProjectTreeControl.getInstance().getActiveServerName();
        String activeProject = QWinFrame.getQWinFrame().getProjectName();
        String activeBranch = QWinFrame.getQWinFrame().getBranchName();
        workfileLocation = userLocationProperties.getWorkfileLocation(activeServerName, activeProject, activeBranch);
        if (workfileLocation == null) {
            workfileLocation = "";
        }
        workfileLocationValue.setText(workfileLocation);
    }

    public boolean getIsOK() {
        return isOKFlag;
    }

    public String getWorkfileLocation() {
        return workfileLocation;
    }

    protected String selectDirectory(String dialogTitle, String initialDirectory) {
        String returnDirectory;
        JFileChooser chooser = new JFileChooser();
        DirectoryFileFilter filter = new DirectoryFileFilter();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(dialogTitle);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonText("Select");
        if (initialDirectory.length() > 0) {
            chooser.setCurrentDirectory(new File(initialDirectory));
        }
        int returnVal = chooser.showDialog(this, "Select");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            returnDirectory = chooser.getSelectedFile().getAbsolutePath();
        } else {
            returnDirectory = "";
        }
        return returnDirectory;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel locationOfYourFilesLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField workfileLocationValue;
    private javax.swing.JButton workfilesButton;
// End of variables declaration//GEN-END:variables

    static class DirectoryFileFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(java.io.File file) {
            return file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Show only directories";
        }
    }

}
