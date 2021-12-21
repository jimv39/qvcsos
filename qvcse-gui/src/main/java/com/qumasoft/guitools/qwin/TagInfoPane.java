/*   Copyright 2004-2021 Jim Voris
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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.TagInfoData;
import java.awt.Component;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * The tag info pane.
 *
 * @author Jim Voris
 */
public class TagInfoPane extends javax.swing.JPanel {
    private static final long serialVersionUID = 4001044130443757642L;
    private final TagInfoTableModel tagInfoModel;
    private final TagInfoTableCellRenderer tagInfoTableCellRenderer = new TagInfoTableCellRenderer();

    /**
     * Creates new form LoggingPane.
     */
    public TagInfoPane() {
        tagInfoModel = new TagInfoTableModel();

        initComponents();

        // Define the renderer for the table's cells.
        tagInfoTable.setDefaultRenderer(javax.swing.JLabel.class, tagInfoTableCellRenderer);
    }

    public void setTagInfoList(List<TagInfoData> tagDataList) {
        tagInfoModel.setTagDataList(tagDataList);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tagInfoScrollPane = new javax.swing.JScrollPane();
        tagInfoTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        tagInfoTable.setModel(tagInfoModel);
        tagInfoScrollPane.setViewportView(tagInfoTable);

        add(tagInfoScrollPane);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane tagInfoScrollPane;
    private javax.swing.JTable tagInfoTable;
    // End of variables declaration//GEN-END:variables


    class TagInfoTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {

        private static final long serialVersionUID = 8361575430460451520L;

        TagInfoTableCellRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TagInfoTableModel tagInfoTableModel = (TagInfoTableModel) tagInfoTable.getModel();
            JLabel inputLabel = (JLabel) tagInfoTableModel.getValueAt(row, column);

            setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                super.setForeground(table.getForeground());
                super.setBackground(table.getBackground());
                setBorder(noFocusBorder);
            }
            setFont(table.getFont());
            setText(inputLabel.getText());
            setToolTipText(inputLabel.getToolTipText());
            setIcon(null);
            return this;
        }
    }

}
