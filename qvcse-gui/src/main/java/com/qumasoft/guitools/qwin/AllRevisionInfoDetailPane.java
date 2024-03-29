/*
 * Copyright 2021 Jim Voris.
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
package com.qumasoft.guitools.qwin;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Jim Voris.
 */
public class AllRevisionInfoDetailPane extends javax.swing.JPanel {

    private static final ImageIcon SEPARATOR_ICON = new ImageIcon(ClassLoader.getSystemResource("images/revInfoSeparator.png"), "separator");

    /**
     * Creates new form AllRevisionInfoDetailPane.
     */
    public AllRevisionInfoDetailPane() {
        initComponents();
        getList().setCellRenderer(new AllRevisionInfoCellRenderer());
    }

    JList getList() {
        return detailsList;
    }

    void setModel(javax.swing.ListModel model) {
        getList().setModel(model);
    }

    void setFontSize(int fontSize) {
        detailsList.setFont(QWinFrame.getQWinFrame().getFont(fontSize));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        detailsScrollPane = new javax.swing.JScrollPane();
        detailsList = new javax.swing.JList<>();

        setLayout(new java.awt.BorderLayout());

        detailsList.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        detailsScrollPane.setViewportView(detailsList);

        add(detailsScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> detailsList;
    private javax.swing.JScrollPane detailsScrollPane;
    // End of variables declaration//GEN-END:variables

    static class AllRevisionInfoCellRenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = -8383608097856011969L;

        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // the list and the cell have the focus
        {
            String fullMessage = value.toString();
            int startOfText = fullMessage.indexOf("-");
            String messageType = fullMessage.substring(0, startOfText);
            String textMessage = fullMessage.substring(startOfText + 1);

            // Figure out the icon to display...
            if (messageType.equals("Separator")) {
                setIcon(SEPARATOR_ICON);
                textMessage = "";
            } else {
                setIcon(null);
            }
            setText(textMessage);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

}
