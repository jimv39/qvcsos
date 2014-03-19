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
package com.qumasoft.guitools.qwin;

import java.util.Set;

/**
 * The about dialog. Show some info about this version of the application.
 * @author  Jim Voris
 */
public class AboutDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = 3973952590285403399L;

    /**
     * Creates new form AboutDialog.
     * @param parent the parent frame.
     * @param modal whether the dialog is modal.
     */
    public AboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        populateComponents();
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

        tabbedPane = new javax.swing.JTabbedPane();
        aboutPanel = new SplashBackgroundPanel();
        allSystemInfoPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        systemPropertiesTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About QVCS Enterprise Client");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        aboutPanel.setFont(new java.awt.Font("Lucida Grande", 3, 18)); // NOI18N
        tabbedPane.addTab("About", aboutPanel);

        systemPropertiesTextArea.setColumns(20);
        systemPropertiesTextArea.setRows(5);
        jScrollPane2.setViewportView(systemPropertiesTextArea);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 394, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 266, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        org.jdesktop.layout.GroupLayout allSystemInfoPanelLayout = new org.jdesktop.layout.GroupLayout(allSystemInfoPanel);
        allSystemInfoPanel.setLayout(allSystemInfoPanelLayout);
        allSystemInfoPanelLayout.setHorizontalGroup(
                allSystemInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 401, Short.MAX_VALUE)
                .add(allSystemInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(allSystemInfoPanelLayout.createSequentialGroup()
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 398, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 3, Short.MAX_VALUE)))
        );
        allSystemInfoPanelLayout.setVerticalGroup(
                allSystemInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 276, Short.MAX_VALUE)
                .add(allSystemInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(allSystemInfoPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(jScrollPane1)))
        );

        tabbedPane.addTab("System Info", allSystemInfoPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(tabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 422, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .add(tabbedPane)
                        .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt)
    {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    @Override
    public void dismissDialog() {
        closeDialog(null);
    }

    private void populateComponents() {
        // Populate all the system properties.
        Set<String> systemProperties = System.getProperties().stringPropertyNames();
        StringBuilder properties = new StringBuilder();
        for (String systemProperty : systemProperties) {
            properties.append(systemProperty)
                      .append(": ")
                      .append(System.getProperty(systemProperty))
                      .append("\n");
        }
        properties.append("Total Memory: ").append(Long.toString(Runtime.getRuntime().totalMemory())).append("\n");
        properties.append("Free Memory: ").append(Long.toString(Runtime.getRuntime().freeMemory())).append("\n");
        properties.append("Max Memory: ").append(Long.toString(Runtime.getRuntime().maxMemory())).append("\n");
        systemPropertiesTextArea.setText(properties.toString());
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aboutPanel;
    private javax.swing.JPanel allSystemInfoPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea systemPropertiesTextArea;
    private javax.swing.JTabbedPane tabbedPane;
// End of variables declaration//GEN-END:variables
}
