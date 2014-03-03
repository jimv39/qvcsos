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

/**
 * Copy filter collection dialog.
 * @author Jim Voris
 */
public class CopyFilterCollectionDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -8086615411641643407L;

    private String collectionName;
    private String projectName;
    private boolean isOKFlag;

    /**
     * Create a copy filter collection dialog.
     * @param parent the parent frame.
     * @param existingCollection the collection to copy.
     */
    public CopyFilterCollectionDialog(java.awt.Frame parent, FilterCollection existingCollection) {
        super(parent, true);
        initComponents();
        populateComponents(existingCollection);
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
        fromThisCollectionPanel = new javax.swing.JPanel();
        filterCollectionNameLabel = new javax.swing.JLabel();
        filterCollectionNameValueLabel = new javax.swing.JLabel();
        filterCollectionProjectLabel = new javax.swing.JLabel();
        filterCollectionProjectValueLabel = new javax.swing.JLabel();
        toThisCollectionPanel = new javax.swing.JPanel();
        newFilterCollectionNameLabel = new javax.swing.JLabel();
        newFilterCollectionProjectLabel = new javax.swing.JLabel();
        newCollectionNameTextArea = new javax.swing.JTextArea();
        newProjectNameComboBox = new javax.swing.JComboBox();

        setTitle("Copy Filter Collection");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setModal(true);
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

        fromThisCollectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " From Filter Collection: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N
        fromThisCollectionPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        filterCollectionNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterCollectionNameLabel.setText("Existing filter collection name:");

        filterCollectionNameValueLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterCollectionNameValueLabel.setText("test");
        filterCollectionNameValueLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        filterCollectionProjectLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterCollectionProjectLabel.setText("Existing filter collection is associated with this project:");

        filterCollectionProjectValueLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterCollectionProjectValueLabel.setText("test");
        filterCollectionProjectValueLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout fromThisCollectionPanelLayout = new org.jdesktop.layout.GroupLayout(fromThisCollectionPanel);
        fromThisCollectionPanel.setLayout(fromThisCollectionPanelLayout);
        fromThisCollectionPanelLayout.setHorizontalGroup(
                fromThisCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, fromThisCollectionPanelLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(fromThisCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, filterCollectionProjectLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, filterCollectionNameValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                                .add(filterCollectionProjectValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, filterCollectionNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE))
                        .addContainerGap())
        );
        fromThisCollectionPanelLayout.setVerticalGroup(
                fromThisCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(fromThisCollectionPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(filterCollectionNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterCollectionNameValueLabel)
                        .add(12, 12, 12)
                        .add(filterCollectionProjectLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterCollectionProjectValueLabel)
                        .add(26, 26, 26))
        );

        toThisCollectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " To Filter Collection: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N
        toThisCollectionPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        newFilterCollectionNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        newFilterCollectionNameLabel.setText("New filter collection name:");

        newFilterCollectionProjectLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        newFilterCollectionProjectLabel.setText("Associate new filter collection with this project:");

        newCollectionNameTextArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        newCollectionNameTextArea.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        newCollectionNameTextArea.setMaximumSize(new java.awt.Dimension(2, 17));

        org.jdesktop.layout.GroupLayout toThisCollectionPanelLayout = new org.jdesktop.layout.GroupLayout(toThisCollectionPanel);
        toThisCollectionPanel.setLayout(toThisCollectionPanelLayout);
        toThisCollectionPanelLayout.setHorizontalGroup(
                toThisCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(toThisCollectionPanelLayout.createSequentialGroup()
                        .add(toThisCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(toThisCollectionPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .add(newCollectionNameTextArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE))
                                .add(toThisCollectionPanelLayout.createSequentialGroup()
                                        .add(4, 4, 4)
                                        .add(newFilterCollectionNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
                                .add(toThisCollectionPanelLayout.createSequentialGroup()
                                        .add(4, 4, 4)
                                        .add(newFilterCollectionProjectLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
                                .add(toThisCollectionPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .add(newProjectNameComboBox, 0, 373, Short.MAX_VALUE)))
                        .addContainerGap())
        );
        toThisCollectionPanelLayout.setVerticalGroup(
                toThisCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(toThisCollectionPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(newFilterCollectionNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newCollectionNameTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(14, 14, 14)
                        .add(newFilterCollectionProjectLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newProjectNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(17, 17, 17))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(fromThisCollectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(layout.createSequentialGroup()
                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 197, Short.MAX_VALUE)
                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(toThisCollectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(fromThisCollectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(10, 10, 10)
                        .add(toThisCollectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        collectionName = null;
        isOKFlag = false;
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        collectionName = newCollectionNameTextArea.getText();
        projectName = (String) newProjectNameComboBox.getSelectedItem();
        isOKFlag = true;
        closeDialog(null);
    }//GEN-LAST:event_okButtonActionPerformed

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

    public String getNewCollectionName() {
        return collectionName;
    }

    public String getNewProjectName() {
        return projectName;
    }

    public boolean getIsOK() {
        return isOKFlag;
    }

    private void populateComponents(FilterCollection existingCollection) {
        filterCollectionNameValueLabel.setText(existingCollection.getCollectionName());
        filterCollectionProjectValueLabel.setText(existingCollection.getAssociatedProjectName());
        newProjectNameComboBox.setModel(new ProjectNamesComboModel());
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel filterCollectionNameLabel;
    private javax.swing.JLabel filterCollectionNameValueLabel;
    private javax.swing.JLabel filterCollectionProjectLabel;
    private javax.swing.JLabel filterCollectionProjectValueLabel;
    private javax.swing.JPanel fromThisCollectionPanel;
    private javax.swing.JTextArea newCollectionNameTextArea;
    private javax.swing.JLabel newFilterCollectionNameLabel;
    private javax.swing.JLabel newFilterCollectionProjectLabel;
    private javax.swing.JComboBox newProjectNameComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel toThisCollectionPanel;
// End of variables declaration//GEN-END:variables
}
