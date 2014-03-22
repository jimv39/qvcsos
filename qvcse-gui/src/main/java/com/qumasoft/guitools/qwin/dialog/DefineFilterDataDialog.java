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
package com.qumasoft.guitools.qwin.dialog;

/**
 * Define filter data dialog.
 * @author Jim Voris
 */
public class DefineFilterDataDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = 1364213962367333755L;

    private String filterTypeString;
    private String filterDataString;
    private boolean isAndFilterFlag;

    /**
     * Create a new define filter data dialog. Use this for creating new filters.
     * @param parent the parent frame.
     * @param modal is this modal.
     */
    public DefineFilterDataDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        filterTypesComboBox.setModel(new FileFilterTypesComboModel());
        andFilterRadioButton.setSelected(true);
        setFont();
        center();
    }

    /**
     * Create a new define filter data dialog. Use this for editing existing filters.
     * @param parent the parent frame.
     * @param modal is this modal.
     * @param filterType the type of filter.
     * @param filterData the filter data.
     * @param isANDFilter is this an 'AND' filter.
     */
    public DefineFilterDataDialog(java.awt.Frame parent, boolean modal, String filterType, String filterData, boolean isANDFilter) {
        this(parent, modal);
        if (isANDFilter) {
            andFilterRadioButton.setSelected(true);
            orFilterRadioButton.setSelected(false);
            isAndFilterFlag = true;
        } else {
            orFilterRadioButton.setSelected(true);
            andFilterRadioButton.setSelected(false);
            isAndFilterFlag = false;
        }
        filterTypesComboBox.setSelectedItem(filterType);
        filterDataTextField.setText(filterData);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterTypeLabel = new javax.swing.JLabel();
        filterTypesComboBox = new javax.swing.JComboBox();
        filterDataLabel = new javax.swing.JLabel();
        filterDataTextField = new javax.swing.JTextField();
        andFilterRadioButton = new javax.swing.JRadioButton();
        orFilterRadioButton = new javax.swing.JRadioButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setTitle("Define Filter");
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        filterTypeLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterTypeLabel.setText("Filter Type:");

        filterTypesComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        filterDataLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterDataLabel.setText("Filter Data:");

        filterDataTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterDataTextField.setToolTipText("Enter the data that defines the criteria for this filter.");

        andFilterRadioButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        andFilterRadioButton.setText("AND filter");
        andFilterRadioButton.setToolTipText("AND this filter with others");
        andFilterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                andFilterRadioButtonActionPerformed(evt);
            }
        });

        orFilterRadioButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        orFilterRadioButton.setText("OR filter");
        orFilterRadioButton.setToolTipText("OR this filter with others");
        orFilterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orFilterRadioButtonActionPerformed(evt);
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(filterTypeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(filterTypesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(filterDataLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(filterDataTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(layout.createSequentialGroup()
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(layout.createSequentialGroup()
                                                .add(andFilterRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 160, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(38, 38, 38)
                                                .add(orFilterRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 160, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(filterTypeLabel)
                        .add(4, 4, 4)
                        .add(filterTypesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(3, 3, 3)
                        .add(filterDataLabel)
                        .add(3, 3, 3)
                        .add(filterDataTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(andFilterRadioButton)
                                .add(orFilterRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(okButton)
                                .add(cancelButton))
                        .add(24, 24, 24))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void orFilterRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_orFilterRadioButtonActionPerformed
    {//GEN-HEADEREND:event_orFilterRadioButtonActionPerformed
        andFilterRadioButton.setSelected(false);
    }//GEN-LAST:event_orFilterRadioButtonActionPerformed

    private void andFilterRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_andFilterRadioButtonActionPerformed
    {//GEN-HEADEREND:event_andFilterRadioButtonActionPerformed
        orFilterRadioButton.setSelected(false);
    }//GEN-LAST:event_andFilterRadioButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        filterTypeString = null;
        filterDataString = null;
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        filterTypeString = (String) filterTypesComboBox.getModel().getSelectedItem();
        filterDataString = filterDataTextField.getText();
        isAndFilterFlag = andFilterRadioButton.isSelected();

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

    public String getFilterTypeString() {
        return filterTypeString;
    }

    public String getFilterDataString() {
        return filterDataString;
    }

    public boolean getIsANDFilter() {
        return isAndFilterFlag;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton andFilterRadioButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel filterDataLabel;
    private javax.swing.JTextField filterDataTextField;
    private javax.swing.JLabel filterTypeLabel;
    private javax.swing.JComboBox filterTypesComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton orFilterRadioButton;
// End of variables declaration//GEN-END:variables
}
