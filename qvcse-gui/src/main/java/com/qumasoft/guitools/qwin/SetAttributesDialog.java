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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.MergedInfoInterface;
import java.util.List;

/**
 * Set archive attributes dialog.  This dialog is the way a user
 * changes the QVCS archive attributes of an archive that already exists.
 *
 * @author  Jim Voris
 */
public class SetAttributesDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = 6068003730912879721L;

    private final List selectedFiles;
    private final OperationSetArchiveAttributes operationSetArchiveAttributes;

    /**
     * Create a set attributes dialog.
     * @param parent the parent frame.
     * @param files the selected files.
     * @param operation the operation that will do the work.
     */
    public SetAttributesDialog(java.awt.Frame parent, List files, OperationSetArchiveAttributes operation) {
        super(parent, true);
        selectedFiles = files;
        operationSetArchiveAttributes = operation;
        initComponents();
        populateControls();
        getRootPane().setDefaultButton(okButton);
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

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        qvcsAttributesPanel = new javax.swing.JPanel();
        checkLocksCheckBox = new javax.swing.JCheckBox();
        deleteWorkfileCheckBox = new javax.swing.JCheckBox();
        expandKeywordsCheckBox = new javax.swing.JCheckBox();
        protectArchiveCheckBox = new javax.swing.JCheckBox();
        computeDeltaCheckBox = new javax.swing.JCheckBox();
        storeLatestRevOnlyCheckBox = new javax.swing.JCheckBox();
        binaryFileCheckBox = new javax.swing.JCheckBox();
        protectWorkfileCheckBox = new javax.swing.JCheckBox();
        journalChangesCheckBox = new javax.swing.JCheckBox();
        compressArchiveCheckBox = new javax.swing.JCheckBox();
        autoMergeCheckBox = new javax.swing.JCheckBox();

        setTitle("Modify Archive Attributes");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
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

        qvcsAttributesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "QVCS Attributes: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N
        qvcsAttributesPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        checkLocksCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        checkLocksCheckBox.setText("Check Locks");

        deleteWorkfileCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        deleteWorkfileCheckBox.setText("Delete Workfile");

        expandKeywordsCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        expandKeywordsCheckBox.setText("Expand Keywords");

        protectArchiveCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        protectArchiveCheckBox.setText("Protect QVCS archive");
        protectArchiveCheckBox.setEnabled(false);

        computeDeltaCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        computeDeltaCheckBox.setText("Compute Delta");

        storeLatestRevOnlyCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        storeLatestRevOnlyCheckBox.setText("Store latest revision only");
        storeLatestRevOnlyCheckBox.setEnabled(false);

        binaryFileCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        binaryFileCheckBox.setText("Binary File");

        protectWorkfileCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        protectWorkfileCheckBox.setText("Protect Workfile");

        journalChangesCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        journalChangesCheckBox.setText("Journal Changes");
        journalChangesCheckBox.setEnabled(false);

        compressArchiveCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        compressArchiveCheckBox.setText("Compress Archive");

        autoMergeCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        autoMergeCheckBox.setText("Auto-Merge on check-in");
        autoMergeCheckBox.setEnabled(false);

        org.jdesktop.layout.GroupLayout qvcsAttributesPanelLayout = new org.jdesktop.layout.GroupLayout(qvcsAttributesPanel);
        qvcsAttributesPanel.setLayout(qvcsAttributesPanelLayout);
        qvcsAttributesPanelLayout.setHorizontalGroup(
                qvcsAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(qvcsAttributesPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(checkLocksCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(40, 40, 40)
                        .add(binaryFileCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(qvcsAttributesPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(deleteWorkfileCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(30, 30, 30)
                        .add(protectWorkfileCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(qvcsAttributesPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(expandKeywordsCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(30, 30, 30)
                        .add(protectArchiveCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(qvcsAttributesPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(journalChangesCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 160, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(20, 20, 20)
                        .add(compressArchiveCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(qvcsAttributesPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(computeDeltaCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(30, 30, 30)
                        .add(autoMergeCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(qvcsAttributesPanelLayout.createSequentialGroup()
                        .add(194, 194, 194)
                        .add(storeLatestRevOnlyCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        qvcsAttributesPanelLayout.setVerticalGroup(
                qvcsAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(qvcsAttributesPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(qvcsAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(checkLocksCheckBox)
                                .add(binaryFileCheckBox))
                        .add(7, 7, 7)
                        .add(qvcsAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(deleteWorkfileCheckBox)
                                .add(protectWorkfileCheckBox))
                        .add(7, 7, 7)
                        .add(qvcsAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(expandKeywordsCheckBox)
                                .add(protectArchiveCheckBox))
                        .add(7, 7, 7)
                        .add(qvcsAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(journalChangesCheckBox)
                                .add(compressArchiveCheckBox))
                        .add(7, 7, 7)
                        .add(qvcsAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(computeDeltaCheckBox)
                                .add(autoMergeCheckBox))
                        .add(7, 7, 7)
                        .add(storeLatestRevOnlyCheckBox))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(layout.createSequentialGroup()
                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, qvcsAttributesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(qvcsAttributesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(8, 8, 8)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(cancelButton)
                                .add(okButton))
                        .add(13, 13, 13))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        // Add your handling code here:
        closeDialog(null);
        operationSetArchiveAttributes.completeOperation(selectedFiles, getAttributes());
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

    private boolean getAttributeValue(javax.swing.JCheckBox checkBox) {
        boolean retVal = false;
        Object object = checkBox.getSelectedObjects();
        if (object != null) {
            retVal = true;
        }
        return retVal;
    }

    private ArchiveAttributes getAttributes() {
        ArchiveAttributes attributes = new ArchiveAttributes();

        attributes.setIsAutoMerge(getAttributeValue(autoMergeCheckBox));
        attributes.setIsBinaryfile(getAttributeValue(binaryFileCheckBox));
        attributes.setIsCheckLock(getAttributeValue(checkLocksCheckBox));
        attributes.setIsCompression(getAttributeValue(compressArchiveCheckBox));
        attributes.setIsComputeDelta(getAttributeValue(computeDeltaCheckBox));
        attributes.setIsDeleteWork(getAttributeValue(deleteWorkfileCheckBox));
        attributes.setIsExpandKeywords(getAttributeValue(expandKeywordsCheckBox));
        attributes.setIsJournalfile(getAttributeValue(journalChangesCheckBox));
        attributes.setIsLatestRevOnly(getAttributeValue(storeLatestRevOnlyCheckBox));
        attributes.setIsProtectArchive(getAttributeValue(protectArchiveCheckBox));
        attributes.setIsProtectWorkfile(getAttributeValue(protectWorkfileCheckBox));

        return attributes;
    }

    private void populateControls() {
        // Just one file was selected.  Set the controls to show the current
        // attributes of the selected file.
        if (selectedFiles.size() == 1) {
            MergedInfoInterface mergedInfo = (MergedInfoInterface) selectedFiles.get(0);
            ArchiveAttributes attributes = mergedInfo.getArchiveInfo().getAttributes();

            autoMergeCheckBox.setSelected(attributes.getIsAutoMerge());
            binaryFileCheckBox.setSelected(attributes.getIsBinaryfile());
            checkLocksCheckBox.setSelected(attributes.getIsCheckLock());
            compressArchiveCheckBox.setSelected(attributes.getIsCompression());
            computeDeltaCheckBox.setSelected(attributes.getIsComputeDelta());
            deleteWorkfileCheckBox.setSelected(attributes.getIsDeleteWork());
            expandKeywordsCheckBox.setSelected(attributes.getIsExpandKeywords());
            journalChangesCheckBox.setSelected(attributes.getIsJournalfile());
            storeLatestRevOnlyCheckBox.setSelected(attributes.getIsLatestRevOnly());
            protectArchiveCheckBox.setSelected(attributes.getIsProtectArchive());
            protectWorkfileCheckBox.setSelected(attributes.getIsProtectWorkfile());
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoMergeCheckBox;
    private javax.swing.JCheckBox binaryFileCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox checkLocksCheckBox;
    private javax.swing.JCheckBox compressArchiveCheckBox;
    private javax.swing.JCheckBox computeDeltaCheckBox;
    private javax.swing.JCheckBox deleteWorkfileCheckBox;
    private javax.swing.JCheckBox expandKeywordsCheckBox;
    private javax.swing.JCheckBox journalChangesCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox protectArchiveCheckBox;
    private javax.swing.JCheckBox protectWorkfileCheckBox;
    private javax.swing.JPanel qvcsAttributesPanel;
    private javax.swing.JCheckBox storeLatestRevOnlyCheckBox;
// End of variables declaration//GEN-END:variables
}