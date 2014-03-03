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
 * Promote from child branch dialog. Allow the user to choose which child branch should get promoted to the parent view.
 *
 * @author Jim Voris
 */
public class PromoteFromChildBranchDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = 182719609830078945L;

    private boolean isOkFlag;
    private final ChildBranchComboBoxModel childBranchComboBoxModel;
    private String childBranchName;

    /**
     * Constructor for promote from child branch dialog.
     *
     * @param parent the parent frame.
     * @param modal is this a modal dialog.
     * @param parentViewName the name of the parent view.
     */
    public PromoteFromChildBranchDialog(java.awt.Frame parent, boolean modal, String parentViewName) {
        super(parent, modal);
        initComponents();
        childBranchComboBoxModel = new ChildBranchComboBoxModel(parentViewName);
        chooseChildBranchComboBox.setModel(childBranchComboBoxModel);
        center();
    }

    boolean isOk() {
        return isOkFlag;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the
     * Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chooseChildBranchLabel = new javax.swing.JLabel();
        chooseChildBranchComboBox = new javax.swing.JComboBox();
        OkButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Promote from child branch");
        setModal(true);
        setResizable(false);

        chooseChildBranchLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        chooseChildBranchLabel.setText("Choose child branch to promote:");

        chooseChildBranchComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        chooseChildBranchComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        OkButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        OkButton.setText("   OK   ");
        OkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OkButtonActionPerformed(evt);
            }
        });

        CancelButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(chooseChildBranchLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chooseChildBranchComboBox, 0, 337, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(OkButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(CancelButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chooseChildBranchLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chooseChildBranchComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OkButton)
                    .addComponent(CancelButton))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OkButtonActionPerformed
        setChildBranchName((String) childBranchComboBoxModel.getSelectedItem());
        isOkFlag = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_OkButtonActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        isOkFlag = false;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_CancelButtonActionPerformed

    @Override
    public void dismissDialog() {
        CancelButtonActionPerformed(null);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton OkButton;
    private javax.swing.JComboBox chooseChildBranchComboBox;
    private javax.swing.JLabel chooseChildBranchLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Get the child branch name.
     * @return the child branch name.
     */
    public String getChildBranchName() {
        return childBranchName;
    }

    /**
     * Set the child branch name.
     * @param branchName the child branch name.
     */
    public void setChildBranchName(String branchName) {
        this.childBranchName = branchName;
    }
}
