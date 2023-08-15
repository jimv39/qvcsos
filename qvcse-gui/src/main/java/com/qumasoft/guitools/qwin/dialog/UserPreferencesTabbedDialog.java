/*   Copyright 2004-2023 Jim Voris
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

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.RemotePropertiesManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * User preferences tabbed dialog.
 * @author Jim Voris
 */
public class UserPreferencesTabbedDialog extends AbstractQWinCommandDialog {
    private static final long serialVersionUID = -3308897697629327783L;

    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int DEFAULT_AUTO_UPDATE_INTERVAL = 10;
    private String currentLookAndFeel;
    private boolean currentBypassLoginDialogFlag;
    private boolean currentUseLargeToolbarFlag;
    private boolean currentUseColoredIconsFlag;
    private boolean currentAutoUpdateFlag;
    private boolean currentIgnoreHiddenDirectoriesFlag;
    private int currentAutoUpdateInterval = DEFAULT_AUTO_UPDATE_INTERVAL;
    private int currentFontSize = DEFAULT_FONT_SIZE;
    private final LookAndFeelComboModel lookAndFeelComboModel;
    private RemotePropertiesBaseClass remoteProperties;

    /**
     * Creates new form UserPreferencesTabbedDialog.
     *
     * @param parent the parent frame.
     * @param modal flag to indicate whether this is a modal dialog or not.
     */
    public UserPreferencesTabbedDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
        remoteProperties = RemotePropertiesManager.getInstance().getRemoteProperties(QWinFrame.getQWinFrame().getLoggedInUserName(), transportProxy);
        this.lookAndFeelComboModel = new LookAndFeelComboModel();
        initComponents();
        populateComponents();
        setFont();
        center();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the
     * Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPanel = new javax.swing.JTabbedPane();
        generalPanel = new javax.swing.JPanel();
        autoUpdateCheckBox = new javax.swing.JCheckBox();
        minutesTextField = new javax.swing.JTextField();
        minutesLabel = new javax.swing.JLabel();
        ignoreHiddenDirectoriesCheckBox = new javax.swing.JCheckBox();
        appearancePanel = new javax.swing.JPanel();
        lookAndFeelLabel = new javax.swing.JLabel();
        lookAndFeelComboBox = new javax.swing.JComboBox();
        subPanel = new javax.swing.JPanel();
        useColoredIconsCheckBox = new javax.swing.JCheckBox();
        useLargeToolbarButtonsCheckBox = new javax.swing.JCheckBox();
        fontSizePanel = new javax.swing.JPanel();
        fontSizeSpinner = new javax.swing.JSpinner();
        fontSizeLabel = new javax.swing.JLabel();
        spaceLabel1 = new javax.swing.JLabel();
        spaceLabel2 = new javax.swing.JLabel();
        utilitiesPanel = new javax.swing.JPanel();
        enableExternalVisualCompareToolCheckBox = new javax.swing.JCheckBox();
        visualCompareCommandLineLabel = new javax.swing.JLabel();
        visualCompareCommandLineTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("User Preferences");
        setResizable(false);

        generalPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        autoUpdateCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        autoUpdateCheckBox.setText("Auto-update every ");
        autoUpdateCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        minutesTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        minutesTextField.setToolTipText("Enter number of minutes between screen refreshes");
        minutesTextField.setMaximumSize(new java.awt.Dimension(14, 26));

        minutesLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        minutesLabel.setText("minutes");

        ignoreHiddenDirectoriesCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ignoreHiddenDirectoriesCheckBox.setText("Ignore hidden directories.");
        ignoreHiddenDirectoriesCheckBox.setToolTipText("Ignore directories that are hidden and those that start with the '.' character.");

        org.jdesktop.layout.GroupLayout generalPanelLayout = new org.jdesktop.layout.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(generalPanelLayout.createSequentialGroup()
                        .add(autoUpdateCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(minutesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(minutesLabel))
                    .add(ignoreHiddenDirectoriesCheckBox))
                .addContainerGap(140, Short.MAX_VALUE))
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(autoUpdateCheckBox)
                    .add(minutesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(minutesLabel))
                .add(18, 18, 18)
                .add(ignoreHiddenDirectoriesCheckBox)
                .addContainerGap(128, Short.MAX_VALUE))
        );

        tabbedPanel.addTab("General", generalPanel);

        appearancePanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        lookAndFeelLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lookAndFeelLabel.setText("Look and Feel:");

        lookAndFeelComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        subPanel.setLayout(new java.awt.GridLayout(3, 0));

        useColoredIconsCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        useColoredIconsCheckBox.setText("Use colored icons in file list");
        useColoredIconsCheckBox.setToolTipText("Enable this checkbox to use colored icons in the file list to help indicate file status");
        useColoredIconsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        subPanel.add(useColoredIconsCheckBox);

        useLargeToolbarButtonsCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        useLargeToolbarButtonsCheckBox.setText("Use large toolbar buttons");
        useLargeToolbarButtonsCheckBox.setToolTipText("Enable to use large toolbar buttons.");
        subPanel.add(useLargeToolbarButtonsCheckBox);

        fontSizePanel.setLayout(new java.awt.GridLayout(1, 4, 10, 0));

        fontSizeSpinner.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        fontSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(11, 8, 20, 1));
        fontSizePanel.add(fontSizeSpinner);

        fontSizeLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        fontSizeLabel.setText("Font size");
        fontSizeLabel.setToolTipText("Change the font size for the application.");
        fontSizePanel.add(fontSizeLabel);
        fontSizePanel.add(spaceLabel1);
        fontSizePanel.add(spaceLabel2);

        subPanel.add(fontSizePanel);

        org.jdesktop.layout.GroupLayout appearancePanelLayout = new org.jdesktop.layout.GroupLayout(appearancePanel);
        appearancePanel.setLayout(appearancePanelLayout);
        appearancePanelLayout.setHorizontalGroup(
            appearancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(appearancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(appearancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, subPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lookAndFeelLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lookAndFeelComboBox, 0, 360, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        appearancePanelLayout.setVerticalGroup(
            appearancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(appearancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(lookAndFeelLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lookAndFeelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(subPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(78, 78, 78))
        );

        tabbedPanel.addTab("Appearance", appearancePanel);

        utilitiesPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        enableExternalVisualCompareToolCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        enableExternalVisualCompareToolCheckBox.setText("Use External Visual Compare Tool");
        enableExternalVisualCompareToolCheckBox.setToolTipText("Enable this checkbox to use an external tool for visual compares.");
        enableExternalVisualCompareToolCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableExternalVisualCompareToolCheckBoxActionPerformed(evt);
            }
        });

        visualCompareCommandLineLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        visualCompareCommandLineLabel.setText("Visual Compare Command Line:");
        visualCompareCommandLineLabel.setFocusable(false);

        visualCompareCommandLineTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        visualCompareCommandLineTextField.setToolTipText("Enter command line template for visual compare tool.");
        visualCompareCommandLineTextField.setEnabled(false);

        org.jdesktop.layout.GroupLayout utilitiesPanelLayout = new org.jdesktop.layout.GroupLayout(utilitiesPanel);
        utilitiesPanel.setLayout(utilitiesPanelLayout);
        utilitiesPanelLayout.setHorizontalGroup(
            utilitiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(utilitiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(utilitiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(visualCompareCommandLineTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 364, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(enableExternalVisualCompareToolCheckBox)
                    .add(visualCompareCommandLineLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        utilitiesPanelLayout.setVerticalGroup(
            utilitiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(utilitiesPanelLayout.createSequentialGroup()
                .add(19, 19, 19)
                .add(enableExternalVisualCompareToolCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(visualCompareCommandLineLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(visualCompareCommandLineTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(98, Short.MAX_VALUE))
        );

        tabbedPanel.addTab("Utilities", utilitiesPanel);

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
            .add(tabbedPanel)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tabbedPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 239, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        // ===================================
        // ===================================
        // Save the 'General' tab settings...
        // Auto-update interval.
        // Auto-update flag
        // Bypass login dialog.
        // Ignore hidden directories.
        int autoUpdateInterval;
        try {
            autoUpdateInterval = getAutoUpdateInterval();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "Invalid value for the update interval. Please enter a number.", "Invalid update interval",
                    JOptionPane.ERROR_MESSAGE);
            tabbedPanel.setSelectedIndex(0);
            minutesTextField.requestFocusInWindow();
            return;
        }

        if (autoUpdateInterval != currentAutoUpdateInterval) {
            remoteProperties.setAutoUpdateInterval("", "", autoUpdateInterval);
        }

        // We need to save the auto-update flag if the flag changes OR if
        // the interval changes, since within the if clause, we reset the
        // timer task that performs the auto-update.
        boolean autoUpdateFlag = getAutoUpdateFlag();
        if ((autoUpdateFlag != currentAutoUpdateFlag)
                || (autoUpdateInterval != currentAutoUpdateInterval)) {
            remoteProperties.setAutoUpdateFlag("", "", autoUpdateFlag);

            // Reset (reschedule) the auto-update timer task.
            QWinFrame.getQWinFrame().setAutoUpdateFlag(autoUpdateFlag);
        }

        // Save the status of the ignore hidden directories.
        boolean ignoreHiddenDirectoriesFlag = getIgnoreHiddenDirectoriesFlag();
        if (ignoreHiddenDirectoriesFlag != currentIgnoreHiddenDirectoriesFlag) {
            QWinFrame.getQWinFrame().getRemoteProperties(QWinFrame.getQWinFrame().getActiveServerProperties().getServerName()).setIgnoreHiddenDirectoriesFlag("", "", ignoreHiddenDirectoriesFlag);
        }

        // ===================================
        // ===================================
        // Save the 'Appearance' tab settings...
        // Look-and-feel
        // Large toolbar buttons
        // Colored file icons
        // Let the user know this change won't take effect until the app is re-started.
        if (!currentLookAndFeel.equals(getLookAndFeelString())) {
            QWinFrame.getQWinFrame().getCurrentRemoteProperties().setLookAndFeel("", "", getLookAndFeelClassNameString());
            JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "Look and Feel change will take effect when you next start the application.", "Look and Feel Change",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        boolean largeButtonFlag = getUseLargeButtonsFlag();
        if (largeButtonFlag != currentUseLargeToolbarFlag) {
            QWinFrame.getQWinFrame().getCurrentRemoteProperties().setUseLargeToolbarButtons("", "", largeButtonFlag);
            JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "The change in toolbar button size will take effect when you next start the application.",
                    "Toolbar Button Size Change", JOptionPane.INFORMATION_MESSAGE);
        }

        boolean coloredFileIconsFlag = getUseColoredFileIconsFlag();
        if (coloredFileIconsFlag != currentUseColoredIconsFlag) {
            QWinFrame.getQWinFrame().getCurrentRemoteProperties().setUseColoredFileIconsFlag("", "", coloredFileIconsFlag);
        }

        int fontSize = getFontSize();
        if (fontSize != currentFontSize) {
            QWinFrame.getQWinFrame().setFontSize(fontSize);
        }

        // ===================================
        // ===================================
        // Save the 'Utilities' tab settings...
        // External visual command flag
        // External visual command command line.
        boolean flag = getUseExternalVisualCompareTool();
        String commandLine = visualCompareCommandLineTextField.getText();

        QWinFrame.getQWinFrame().getRemoteProperties(QWinFrame.getQWinFrame().getActiveServerProperties().getServerName()).setUseExternalVisualCompareTool("", "", flag);
        QWinFrame.getQWinFrame().getRemoteProperties(QWinFrame.getQWinFrame().getActiveServerProperties().getServerName()).setExternalVisualCommandLine("", "", commandLine);
        setVisible(false);

        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void enableExternalVisualCompareToolCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_enableExternalVisualCompareToolCheckBoxActionPerformed
    {//GEN-HEADEREND:event_enableExternalVisualCompareToolCheckBoxActionPerformed
        boolean flag = getUseExternalVisualCompareTool();
        if (flag) {
            visualCompareCommandLineTextField.setEnabled(true);
        } else {
            visualCompareCommandLineTextField.setEnabled(false);
        }
    }//GEN-LAST:event_enableExternalVisualCompareToolCheckBoxActionPerformed

    private void populateComponents() {
        RemotePropertiesBaseClass remoteUserProperties = QWinFrame.getQWinFrame().getRemoteProperties(QWinFrame.getQWinFrame().getActiveServerProperties().getServerName());

        // ===================================
        // ===================================
        // Initialize the 'General' tab...
        // Auto-update flag
        // Auto-update interval.
        // Bypass login dialog.
        // Ignore hidden directories.
        currentAutoUpdateFlag = remoteProperties.getAutoUpdateFlag("", "");
        autoUpdateCheckBox.setSelected(currentAutoUpdateFlag);

        currentAutoUpdateInterval = remoteProperties.getAutoUpdateInterval("", "");
        minutesTextField.setText(Integer.toString(currentAutoUpdateInterval));

        currentIgnoreHiddenDirectoriesFlag = remoteUserProperties.getIgnoreHiddenDirectoriesFlag("", "");
        ignoreHiddenDirectoriesCheckBox.setSelected(currentIgnoreHiddenDirectoriesFlag);

        // ===================================
        // ===================================
        // Initialize the 'Appearance' tab...
        // Look-and-feel
        // Large toolbar buttons
        // Colored file icons

        // Set the look and feel model.
        lookAndFeelComboBox.setModel(lookAndFeelComboModel);

        // Select the current look and feel....
        currentLookAndFeel = UIManager.getLookAndFeel().getName();
        if (currentLookAndFeel != null && currentLookAndFeel.length() > 0) {
            lookAndFeelComboBox.setSelectedItem(currentLookAndFeel);
        }

        // Set the toolbar size checkbox.
        currentUseLargeToolbarFlag = remoteUserProperties.getUseLargeToolbarButtons("", "");
        useLargeToolbarButtonsCheckBox.setSelected(currentUseLargeToolbarFlag);

        // Set the colored file icons checkbox.
        currentUseColoredIconsFlag = remoteUserProperties.getUseColoredFileIconsFlag("", "");
        useColoredIconsCheckBox.setSelected(currentUseColoredIconsFlag);

        // Set the font size spinners...
        currentFontSize = remoteUserProperties.getFontSize("", "");
        fontSizeSpinner.setValue(currentFontSize);

        // ===================================
        // ===================================
        // Initialize the 'Utilities' tab...
        // External visual command flag
        // External visual command command line.
        String commandLine = remoteUserProperties.getExternalVisualCommandLine("", "");
        boolean flag = remoteUserProperties.getUseExternalVisualCompareTool("", "");
        enableExternalVisualCompareToolCheckBox.setSelected(flag);
        visualCompareCommandLineTextField.setText(commandLine);
        if (flag) {
            visualCompareCommandLineTextField.setEnabled(true);
            visualCompareCommandLineTextField.select(0, commandLine.length());
        }
    }

    private boolean getUseExternalVisualCompareTool() {
        boolean flag = false;
        Object leaveLocked = enableExternalVisualCompareToolCheckBox.getSelectedObjects();
        if (leaveLocked != null) {
            flag = true;
        }
        return flag;
    }

    String getLookAndFeelClassNameString() {
        String retVal = UIManager.getLookAndFeel().getClass().toString();
        Object selectedItem = lookAndFeelComboBox.getSelectedItem();
        if (selectedItem instanceof String) {
            String lookAndFeelName = (String) selectedItem;
            retVal = lookAndFeelComboModel.getLookAndFeelClassName(lookAndFeelName);
        }

        return retVal;
    }

    String getLookAndFeelString() {
        String retVal = UIManager.getLookAndFeel().getClass().toString();
        Object selectedItem = lookAndFeelComboBox.getSelectedItem();
        if (selectedItem instanceof String) {
            retVal = (String) selectedItem;
        }

        return retVal;
    }

    private boolean getUseLargeButtonsFlag() {
        boolean flag = false;
        Object useLargeButtons = useLargeToolbarButtonsCheckBox.getSelectedObjects();
        if (useLargeButtons != null) {
            flag = true;
        }
        return flag;
    }

    private boolean getAutoUpdateFlag() {
        boolean flag = false;
        Object autoUpdateFlag = autoUpdateCheckBox.getSelectedObjects();
        if (autoUpdateFlag != null) {
            flag = true;
        }
        return flag;
    }

    private boolean getIgnoreHiddenDirectoriesFlag() {
        return ignoreHiddenDirectoriesCheckBox.isSelected();
    }

    private boolean getUseColoredFileIconsFlag() {
        boolean flag = false;
        Object useColoredFileIcon = useColoredIconsCheckBox.getSelectedObjects();
        if (useColoredFileIcon != null) {
            flag = true;
        }
        return flag;
    }

    private int getFontSize() {
        Integer fontSizeInteger = (Integer) fontSizeSpinner.getValue();
        return fontSizeInteger.intValue();
    }

    private int getAutoUpdateInterval() throws NumberFormatException {
        int returnValue;
        String minutesString = minutesTextField.getText();
        returnValue = Integer.valueOf(minutesString).intValue();
        return returnValue;
    }

    @Override
    public void dismissDialog() {
        cancelButtonActionPerformed(null);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel appearancePanel;
    private javax.swing.JCheckBox autoUpdateCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox enableExternalVisualCompareToolCheckBox;
    private javax.swing.JLabel fontSizeLabel;
    private javax.swing.JPanel fontSizePanel;
    private javax.swing.JSpinner fontSizeSpinner;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JCheckBox ignoreHiddenDirectoriesCheckBox;
    private javax.swing.JComboBox lookAndFeelComboBox;
    private javax.swing.JLabel lookAndFeelLabel;
    private javax.swing.JLabel minutesLabel;
    private javax.swing.JTextField minutesTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel spaceLabel1;
    private javax.swing.JLabel spaceLabel2;
    private javax.swing.JPanel subPanel;
    private javax.swing.JTabbedPane tabbedPanel;
    private javax.swing.JCheckBox useColoredIconsCheckBox;
    private javax.swing.JCheckBox useLargeToolbarButtonsCheckBox;
    private javax.swing.JPanel utilitiesPanel;
    private javax.swing.JLabel visualCompareCommandLineLabel;
    private javax.swing.JTextField visualCompareCommandLineTextField;
    // End of variables declaration//GEN-END:variables
}
