/*
 * Copyright 2021-2022 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.operation.OperationCreateTag;
import javax.swing.JOptionPane;

/**
 *
 * @author Jim Voris
 */
public class CreateTagDialog extends AbstractQWinCommandDialog {
    private final OperationCreateTag createTagOperation;
    private String tagText;
    private String description;
    private Boolean moveableTagFlag;

    /**
     * Creates new form CreateTagDialog.
     * @param parent QWin frame.
     * @param operation the create tag operation.
     * @param modal is this a modal dialog.
     */
    public CreateTagDialog(java.awt.Frame parent, OperationCreateTag operation, boolean modal) {
        super(parent, modal);
        this.createTagOperation = operation;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tagTextLabel = new javax.swing.JLabel();
        tagTextTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        tagDescriptionLabel = new javax.swing.JLabel();
        tagDescriptionScrollPane = new javax.swing.JScrollPane();
        tagDescriptionTextArea = new javax.swing.JTextArea();
        moveableTagCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Define Tag");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        tagTextLabel.setText("Define tag text:");

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        tagDescriptionLabel.setText("Tag Description:");

        tagDescriptionTextArea.setColumns(20);
        tagDescriptionTextArea.setLineWrap(true);
        tagDescriptionTextArea.setRows(5);
        tagDescriptionTextArea.setWrapStyleWord(true);
        tagDescriptionScrollPane.setViewportView(tagDescriptionTextArea);

        moveableTagCheckBox.setText("Moveable Tag");
        moveableTagCheckBox.setToolTipText("Enable to allow this tag to be moved forward or backward on the branch's timeline.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(tagTextLabel)
                        .addComponent(tagTextTextField)
                        .addComponent(tagDescriptionLabel)
                        .addComponent(tagDescriptionScrollPane))
                    .addComponent(moveableTagCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tagTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tagTextTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tagDescriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tagDescriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(moveableTagCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateData()) {
            closeDialog(null);
            createTagOperation.processDialogResult(getTagText(), getDescription(), getMoveableTagFlag());
        } else {
            JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "The tag name cannot be blank.", "Tag Name Required", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox moveableTagCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel tagDescriptionLabel;
    private javax.swing.JScrollPane tagDescriptionScrollPane;
    private javax.swing.JTextArea tagDescriptionTextArea;
    private javax.swing.JLabel tagTextLabel;
    private javax.swing.JTextField tagTextTextField;
    // End of variables declaration//GEN-END:variables

    @Override
    public void dismissDialog() {
        cancelButtonActionPerformed(null);
    }

    private boolean validateData() {
        boolean retVal = false;
        this.tagText = tagTextTextField.getText();
        this.description = tagDescriptionTextArea.getText();
        this.moveableTagFlag = false;
        Object flag = moveableTagCheckBox.getSelectedObjects();
        if (flag != null) {
            this.moveableTagFlag = true;
        }
        if (getTagText().length() > 0) {
            retVal = true;
        }
        return retVal;
    }

    /**
     * @return the tagText
     */
    public String getTagText() {
        return tagText;
    }

    /**
     * @return the tag description.
     */
    public String getDescription() {
        return description;
    }

    public Boolean getMoveableTagFlag() {
        return moveableTagFlag;
    }
}