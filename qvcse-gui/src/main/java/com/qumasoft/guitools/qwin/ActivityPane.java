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

import java.awt.Component;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Activity pane. A pane that we use to display log messages.
 * @author Jim Voris
 */
public class ActivityPane extends javax.swing.JPanel {
    private static final long serialVersionUID = -8286403730780317135L;

    private static final ImageIcon SEVERE_ICON = new ImageIcon(ClassLoader.getSystemResource("images/status_severe.png"), "status severe");
    private static final ImageIcon WARNING_ICON = new ImageIcon(ClassLoader.getSystemResource("images/status_warning.png"), "status warning");
    private static final ImageIcon INFO_ICON = new ImageIcon(ClassLoader.getSystemResource("images/status_info.png"), "status info");
    private static final ImageIcon EMPTY_ICON = new ImageIcon(ClassLoader.getSystemResource("images/status_none.png"), "status none");

    /**
     * Creates new form ActivityPane.
     */
    public ActivityPane() {
        initComponents();
        getActivityList().setModel(new ActivityListModel());
        getActivityList().setCellRenderer(new ActivityListCellRenderer());
    }

    final JList getActivityList() {
        return activityList;
    }

    void setFontSize(int fontSize) {
        activityList.setFont(QWinFrame.getQWinFrame().getFont(fontSize));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        activityScrollPane = new javax.swing.JScrollPane();
        activityList = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        activityList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        activityScrollPane.setViewportView(activityList);

        add(activityScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList activityList;
    private javax.swing.JScrollPane activityScrollPane;
// End of variables declaration//GEN-END:variables

    static class ActivityListCellRenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = -2720871104427979107L;

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
            StringBuilder fullMessage = new StringBuilder(value.toString());
            int startOfText = fullMessage.indexOf(" ");
            String messageType = fullMessage.substring(0, startOfText);
            String textMessage = fullMessage.substring(startOfText + 1);

            // Figure out the icon to display...
            if (messageType.equals(Level.INFO.toString())) {
                setIcon(INFO_ICON);
            } else if (messageType.equals(Level.WARNING.toString())) {
                setIcon(WARNING_ICON);
            } else if (messageType.equals(Level.SEVERE.toString())) {
                setIcon(SEVERE_ICON);
            } else {
                setIcon(EMPTY_ICON);
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
