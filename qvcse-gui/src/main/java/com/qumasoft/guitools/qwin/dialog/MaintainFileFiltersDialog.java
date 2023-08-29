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

import com.qumasoft.guitools.qwin.FilterCollection;
import com.qumasoft.guitools.qwin.filefilter.FileFilterInterface;
import com.qumasoft.guitools.qwin.filefilter.FilterFactory;
import javax.swing.JOptionPane;

/**
 * Maintain file filters dialog.
 * @author Jim Voris
 */
public class MaintainFileFiltersDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -6021390712338076126L;

    private final java.awt.Frame parentFrame;
    private FilterCollection selectedCollection;
    private boolean isOKFlag;

    /**
     * Create a maintain file filters dialog.
     * @param parent the parent frame.
     * @param modal is this modal.
     */
    public MaintainFileFiltersDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        parentFrame = parent;
        initComponents();
        getRootPane().setDefaultButton(okButton);
        filterCollectionNamesComboBox.setModel(new FileFiltersMutableComboModel());
        filterCollectionNamesComboBox.setSelectedIndex(0);
        setFont();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newCollectionButton = new javax.swing.JButton();
        copyToButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        filterCollectionLabel = new javax.swing.JLabel();
        filterCollectionNamesComboBox = new javax.swing.JComboBox();
        filterCollectionPanel = new javax.swing.JPanel();
        filterDataTextArea = new javax.swing.JTextArea();
        filterDataLabel = new javax.swing.JLabel();
        filtersInCollectionLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        isANDLabel = new javax.swing.JLabel();
        isAND_ORTextArea = new javax.swing.JTextArea();
        associatedProjectNameLabel = new javax.swing.JLabel();
        associatedProjectNameValueLabel = new javax.swing.JLabel();
        filtersCollectionListScrollPane = new javax.swing.JScrollPane();
        filtersCollectionList = new javax.swing.JList();

        setTitle("Maintain Filter Collections");
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        newCollectionButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        newCollectionButton.setText("New Collection...");
        newCollectionButton.setToolTipText("Create a new filter collection.");
        newCollectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCollectionButtonActionPerformed(evt);
            }
        });

        copyToButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        copyToButton.setText("Copy Collection...");
        copyToButton.setToolTipText("Copy the selected collection to a new filter collection.");
        copyToButton.setActionCommand("Copy Collection");
        copyToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyToButtonActionPerformed(evt);
            }
        });

        deleteButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        deleteButton.setText("Delete Collection...");
        deleteButton.setToolTipText("Delete the selected filter collection.");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
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

        filterCollectionLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterCollectionLabel.setText("Filter Collection Name:");

        filterCollectionNamesComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterCollectionNamesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterCollectionNamesComboBoxActionPerformed(evt);
            }
        });

        filterCollectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filter Collection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N

        filterDataTextArea.setEditable(false);
        filterDataTextArea.setBackground(new java.awt.Color(204, 204, 204));
        filterDataTextArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterDataTextArea.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        filterDataTextArea.setMaximumSize(new java.awt.Dimension(2, 17));

        filterDataLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterDataLabel.setLabelFor(filterDataTextArea);
        filterDataLabel.setText("Filter Data:");

        filtersInCollectionLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filtersInCollectionLabel.setText("Filters in this collection:");

        addButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        addButton.setText("Add...");
        addButton.setToolTipText("Add filter type to filter collection");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        editButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        editButton.setText("Edit...");
        editButton.setToolTipText("Edit the selected filter");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        removeButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        removeButton.setText("Remove");
        removeButton.setToolTipText("Remove selected filter type from filter collection");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        isANDLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        isANDLabel.setLabelFor(isAND_ORTextArea);
        isANDLabel.setText("AND/OR:");

        isAND_ORTextArea.setEditable(false);
        isAND_ORTextArea.setBackground(new java.awt.Color(204, 204, 204));
        isAND_ORTextArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        isAND_ORTextArea.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        isAND_ORTextArea.setMaximumSize(new java.awt.Dimension(2, 17));

        associatedProjectNameLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        associatedProjectNameLabel.setText("Associated with project:");

        associatedProjectNameValueLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        associatedProjectNameValueLabel.setText("Test");
        associatedProjectNameValueLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        filtersCollectionList.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        filtersCollectionList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filtersCollectionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        filtersCollectionList.setMaximumSize(new java.awt.Dimension(200, 200));
        filtersCollectionList.setMinimumSize(new java.awt.Dimension(200, 200));
        filtersCollectionList.setPreferredSize(new java.awt.Dimension(200, 200));
        filtersCollectionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                filtersCollectionListValueChanged(evt);
            }
        });
        filtersCollectionListScrollPane.setViewportView(filtersCollectionList);

        org.jdesktop.layout.GroupLayout filterCollectionPanelLayout = new org.jdesktop.layout.GroupLayout(filterCollectionPanel);
        filterCollectionPanel.setLayout(filterCollectionPanelLayout);
        filterCollectionPanelLayout.setHorizontalGroup(
                filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(filterCollectionPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(filterCollectionPanelLayout.createSequentialGroup()
                                        .add(associatedProjectNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 184, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(associatedProjectNameValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 270, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(filterCollectionPanelLayout.createSequentialGroup()
                                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, filtersInCollectionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, filtersCollectionListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                                        .add(20, 20, 20)
                                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(editButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(filterCollectionPanelLayout.createSequentialGroup()
                                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                                .add(filterDataLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(filterDataTextArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
                                        .add(20, 20, 20)
                                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(isAND_ORTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(org.jdesktop.layout.GroupLayout.TRAILING, isANDLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .add(14, 14, 14))
        );
        filterCollectionPanelLayout.setVerticalGroup(
                filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(filterCollectionPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(associatedProjectNameValueLabel)
                                .add(associatedProjectNameLabel))
                        .add(12, 12, 12)
                        .add(filtersInCollectionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(filtersCollectionListScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(filterCollectionPanelLayout.createSequentialGroup()
                                        .add(addButton)
                                        .add(1, 1, 1)
                                        .add(editButton)
                                        .add(1, 1, 1)
                                        .add(removeButton)))
                        .add(10, 10, 10)
                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(isANDLabel)
                                .add(filterDataLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterCollectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(filterDataTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(isAND_ORTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                        .add(366, 366, 366)
                                        .add(copyToButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(layout.createSequentialGroup()
                                        .add(366, 366, 366)
                                        .add(deleteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(layout.createSequentialGroup()
                                        .add(10, 10, 10)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                                .add(layout.createSequentialGroup()
                                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, filterCollectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(layout.createSequentialGroup()
                                        .add(12, 12, 12)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, filterCollectionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, filterCollectionNamesComboBox, 0, 350, Short.MAX_VALUE))
                                        .add(4, 4, 4)
                                        .add(newCollectionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(11, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(filterCollectionLabel)
                        .add(4, 4, 4)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(filterCollectionNamesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(newCollectionButton))
                        .add(1, 1, 1)
                        .add(copyToButton)
                        .add(1, 1, 1)
                        .add(deleteButton)
                        .add(11, 11, 11)
                        .add(filterCollectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void copyToButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_copyToButtonActionPerformed
    {//GEN-HEADEREND:event_copyToButtonActionPerformed
        CopyFilterCollectionDialog copyFilterCollectionDialog = new CopyFilterCollectionDialog(parentFrame, selectedCollection);
        copyFilterCollectionDialog.setVisible(true);
        if (copyFilterCollectionDialog.getIsOK()) {
            String collectionName = copyFilterCollectionDialog.getNewCollectionName();
            String projectName = copyFilterCollectionDialog.getNewProjectName();
            boolean alreadyUsedFlag = false;

            // Make sure the collection name is not already in use...
            FileFiltersMutableComboModel model = (FileFiltersMutableComboModel) filterCollectionNamesComboBox.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                FilterCollection filterCollection = model.getElementAt(i);
                if (filterCollection.getCollectionName().equals(collectionName)) {
                    alreadyUsedFlag = true;
                    break;
                }
            }
            if (alreadyUsedFlag == false) {
                FilterCollection newCollection = new FilterCollection(null, collectionName, projectName, selectedCollection);

                model.addElement(newCollection);
                model.setSelectedItem(newCollection);
            } else {
                JOptionPane.showMessageDialog(this, "The collection name you defined is already in use.", "Collection Name Already Used", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_copyToButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editButtonActionPerformed
    {//GEN-HEADEREND:event_editButtonActionPerformed
        int selectedIndex = filtersCollectionList.getSelectedIndex();
        if (selectedIndex != -1) {
            // We need to put up the edit flavor of the dialog...
            FileFilterInterface filter = (FileFilterInterface) filtersCollectionList.getModel().getElementAt(selectedIndex);
            DefineFilterDataDialog defineFilterDataDialog = new DefineFilterDataDialog(parentFrame, true, filter.getFilterType(), filter.getFilterData(), filter.getIsANDFilter());
            defineFilterDataDialog.setVisible(true);
            String filterType = defineFilterDataDialog.getFilterTypeString();
            String filterData = defineFilterDataDialog.getFilterDataString();
            boolean isANDFilter = defineFilterDataDialog.getIsANDFilter();

            if (filterType != null && filterData != null) {
                // Remove the old flavor of the filter...
                selectedCollection.removeFilter(filter);

                // Make the new flavor of the filter...
                FileFilterInterface newFilter = FilterFactory.buildFilter(filterType, filterData, isANDFilter, null);
                selectedCollection.addFilter(newFilter);
                filtersCollectionList.setModel(new FileFiltersListModel(selectedCollection));
                filterDataTextArea.setText("");
                isAND_ORTextArea.setText("");
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void filtersCollectionListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_filtersCollectionListValueChanged
    {//GEN-HEADEREND:event_filtersCollectionListValueChanged
        int selectedIndex = filtersCollectionList.getSelectedIndex();
        if (selectedIndex != -1) {
            FileFilterInterface filter = (FileFilterInterface) filtersCollectionList.getModel().getElementAt(selectedIndex);
            filterDataTextArea.setText(filter.getFilterData());
            isAND_ORTextArea.setText(filter.getIsANDFilter()
                    ? "AND"
                    : "OR");
        }
    }//GEN-LAST:event_filtersCollectionListValueChanged

    private void newCollectionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newCollectionButtonActionPerformed
    {//GEN-HEADEREND:event_newCollectionButtonActionPerformed
        DefineFilterCollectionNameDialog defineFilterCollectionNameDialog = new DefineFilterCollectionNameDialog(parentFrame, true);
        defineFilterCollectionNameDialog.setVisible(true);
        if (defineFilterCollectionNameDialog.getCollectionName() != null) {
            String collectionName = defineFilterCollectionNameDialog.getCollectionName();
            String projectName = defineFilterCollectionNameDialog.getProjectName();
            boolean alreadyUsedFlag = false;

            // Make sure the collection name is not already in use...
            FileFiltersMutableComboModel model = (FileFiltersMutableComboModel) filterCollectionNamesComboBox.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                FilterCollection filterCollection = model.getElementAt(i);
                if (filterCollection.getCollectionName().equals(collectionName)) {
                    alreadyUsedFlag = true;
                    break;
                }
            }
            if (alreadyUsedFlag == false) {
                FilterCollection newCollection = new FilterCollection(null, collectionName, false, projectName);
                model.addElement(newCollection);
                model.setSelectedItem(newCollection);
            } else {
                JOptionPane.showMessageDialog(this, "The collection name you defined is already in use.", "Collection Name Already Used", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_newCollectionButtonActionPerformed

    private void filterCollectionNamesComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_filterCollectionNamesComboBoxActionPerformed
    {//GEN-HEADEREND:event_filterCollectionNamesComboBoxActionPerformed
        selectedCollection = (FilterCollection) filterCollectionNamesComboBox.getModel().getSelectedItem();
        if (selectedCollection.getIsBuiltInCollection()) {
            setForUserDefinedFilterCollection(false);
        } else {
            setForUserDefinedFilterCollection(true);
        }

        // Clear the filter data edit box.
        filterDataTextArea.setText("");
        isAND_ORTextArea.setText("");

        // Set the project that this collection is associated with
        associatedProjectNameValueLabel.setText(selectedCollection.getAssociatedProjectName());

        // Set the model for the list box.
        filtersCollectionList.setModel(new FileFiltersListModel(selectedCollection));
    }//GEN-LAST:event_filterCollectionNamesComboBoxActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
    {//GEN-HEADEREND:event_deleteButtonActionPerformed
        // The user wants to delete a filter collection.
        // Ask them before we do it.
        FilterCollection collectionToDelete = (FilterCollection) filterCollectionNamesComboBox.getModel().getSelectedItem();
        FileFiltersMutableComboModel model = (FileFiltersMutableComboModel) filterCollectionNamesComboBox.getModel();
        String collectionName = collectionToDelete.getCollectionName();
        int result = JOptionPane.showConfirmDialog(null, "Delete the '" + collectionName + "' collection", "Please Choose", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            model.removeElement(collectionToDelete);
            filterCollectionNamesComboBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        isOKFlag = true;
        closeDialog(null);
    }//GEN-LAST:event_okButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
    {//GEN-HEADEREND:event_removeButtonActionPerformed
        int selectedIndex = filtersCollectionList.getSelectedIndex();
        if (selectedIndex != -1) {
            FileFilterInterface filter = (FileFilterInterface) filtersCollectionList.getModel().getElementAt(selectedIndex);
            selectedCollection.removeFilter(filter);
            filtersCollectionList.setModel(new FileFiltersListModel(selectedCollection));
            filterDataTextArea.setText("");
            isAND_ORTextArea.setText("");
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
    {//GEN-HEADEREND:event_addButtonActionPerformed
        DefineFilterDataDialog defineFilterDataDialog = new DefineFilterDataDialog(parentFrame, true);
        defineFilterDataDialog.setVisible(true);
        String filterType = defineFilterDataDialog.getFilterTypeString();
        String filterData = defineFilterDataDialog.getFilterDataString();
        boolean isANDFilter = defineFilterDataDialog.getIsANDFilter();
        if (filterType != null && filterData != null) {
            FileFilterInterface filter = FilterFactory.buildFilter(filterType, filterData, isANDFilter, null);
            selectedCollection.addFilter(filter);
            filtersCollectionList.setModel(new FileFiltersListModel(selectedCollection));
            filterDataTextArea.setText("");
            isAND_ORTextArea.setText("");
        }
    }//GEN-LAST:event_addButtonActionPerformed

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

    private void setForUserDefinedFilterCollection(boolean flag) {
        deleteButton.setEnabled(flag);
        removeButton.setEnabled(flag);
        addButton.setEnabled(flag);
        editButton.setEnabled(flag);
    }

    public boolean getIsOK() {
        return isOKFlag;
    }

    public FilterCollection[] getFilterCollections() {
        FileFiltersMutableComboModel model = (FileFiltersMutableComboModel) filterCollectionNamesComboBox.getModel();
        int size = model.getSize();
        FilterCollection[] filterCollections = new FilterCollection[size];
        for (int i = 0; i < model.getSize(); i++) {
            filterCollections[i] = model.getElementAt(i);
        }
        return filterCollections;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel associatedProjectNameLabel;
    private javax.swing.JLabel associatedProjectNameValueLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton copyToButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel filterCollectionLabel;
    private javax.swing.JComboBox filterCollectionNamesComboBox;
    private javax.swing.JPanel filterCollectionPanel;
    private javax.swing.JLabel filterDataLabel;
    private javax.swing.JTextArea filterDataTextArea;
    private javax.swing.JList filtersCollectionList;
    private javax.swing.JScrollPane filtersCollectionListScrollPane;
    private javax.swing.JLabel filtersInCollectionLabel;
    private javax.swing.JLabel isANDLabel;
    private javax.swing.JTextArea isAND_ORTextArea;
    private javax.swing.JButton newCollectionButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton removeButton;
// End of variables declaration//GEN-END:variables
}
