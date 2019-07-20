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

import java.util.Date;

/**
 * Date time chooser dialog.
 * @author Jim Voris
 */
public class DateTimeChooserDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 5396991646594243700L;

    private JDateTimeChooser dateTimeChooserPanel;
    private final Date dateToUse;
    private boolean isOKFlag;

    /**
     * Create a new date time chooser dialog (date unknown). Defaults to the current date/time.
     * @param parent the parent frame.
     */
    public DateTimeChooserDialog(java.awt.Frame parent) {
        super(parent, true);
        dateToUse = new Date();
        initComponents();
        dateTimeChooserPanel.setVisible(true);
        initEscapeKey();
    }

    /**
     * Create a new date time chooser dialog using supplied date.
     * @param parent the parent frame.
     * @param date the date to set it to (to begin with).
     */
    public DateTimeChooserDialog(java.awt.Frame parent, Date date) {
        super(parent, true);
        dateToUse = date;
        initComponents();
        dateTimeChooserPanel.setVisible(true);
        initEscapeKey();
    }

    Date getDate() {
        return dateTimeChooserPanel.getDate();
    }

    void setDate(Date date) {
        dateTimeChooserPanel.setDate(date);
    }

    boolean getIsOK() {
        return isOKFlag;
    }

    private void initEscapeKey() {
        javax.swing.KeyStroke escapeKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, false);
        javax.swing.AbstractAction escapeAction = new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelButtonActionPerformed(e);
            }
        };

        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateTimePanel = new javax.swing.JPanel();
        okCancelButtonPanel = new javax.swing.JPanel();
        okCancelInnerPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(400, 500));

        dateTimeChooserPanel = new JDateTimeChooser(dateToUse);
        dateTimePanel.add(dateTimeChooserPanel, java.awt.BorderLayout.NORTH);
        dateTimePanel.setLayout(new java.awt.BorderLayout(5, 5));

        okCancelButtonPanel.setLayout(new java.awt.BorderLayout(5, 5));

        okCancelInnerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

        okButton.setText("OK");
        okButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        okButton.setMaximumSize(new java.awt.Dimension(65, 23));
        okButton.setMinimumSize(new java.awt.Dimension(65, 23));
        okButton.setPreferredSize(new java.awt.Dimension(65, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        okCancelInnerPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.setDoubleBuffered(true);
        cancelButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        okCancelInnerPanel.add(cancelButton);

        okCancelButtonPanel.add(okCancelInnerPanel, java.awt.BorderLayout.CENTER);

        dateTimePanel.add(okCancelButtonPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(dateTimePanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
        isOKFlag = false;
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        setVisible(false);
        dispose();
        isOKFlag = true;
    }//GEN-LAST:event_okButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dateTimePanel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel okCancelButtonPanel;
    private javax.swing.JPanel okCancelInnerPanel;
    // End of variables declaration//GEN-END:variables
}
