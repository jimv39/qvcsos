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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.DirectoryManagerForRoot;
import com.qumasoft.qvcslib.DirectoryManagerInterface;

/**
 * Project tree panel.
 * @author Jim Voris
 */
public class ProjectTreePanel extends javax.swing.JPanel implements javax.swing.event.ChangeListener {
    private static final long serialVersionUID = -6495528810501736582L;
    private final ProjectTreeControl treeControl;

    /**
     * Creates new form ProjectTreePanel.
     */
    public ProjectTreePanel() {
        initComponents();
        treeControl = ProjectTreeControl.getInstance();
        projectLocationLabel.setText("Project Name: ");
        projectLocationValue.setText(" ");
        add(treeControl, java.awt.BorderLayout.CENTER);

        projectLocationLabel.setFont(QWinFrame.getQWinFrame().getFont(QWinFrame.getQWinFrame().getFontSize() + 1));
        projectLocationValue.setFont(QWinFrame.getQWinFrame().getFont(QWinFrame.getQWinFrame().getFontSize() + 1));
    }

    /**
     * Set the font size.
     * @param fontSize the font size.
     */
    public void setFontSize(int fontSize) {
        projectLocationLabel.setFont(QWinFrame.getQWinFrame().getFont(fontSize + 1));
        projectLocationValue.setFont(QWinFrame.getQWinFrame().getFont(fontSize + 1));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the FormEditor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectLocationPanel = new javax.swing.JPanel();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationValue = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        projectLocationPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        projectLocationPanel.setLayout(new javax.swing.BoxLayout(projectLocationPanel, javax.swing.BoxLayout.Y_AXIS));

        projectLocationLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        projectLocationLabel.setText("Project Name:");
        projectLocationPanel.add(projectLocationLabel);

        projectLocationValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        projectLocationPanel.add(projectLocationValue);

        add(projectLocationPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void stateChanged(final javax.swing.event.ChangeEvent p1) {
        DirectoryManagerInterface[] directoryManagers = QWinFrame.getQWinFrame().getCurrentDirectoryManagers();
        if (directoryManagers != null) {
            DirectoryManagerInterface directoryManager = directoryManagers[0];
            if (directoryManager instanceof DirectoryManagerForRoot) {
                projectLocationLabel.setText("Project Name: ");
                projectLocationValue.setText(" ");
            } else if (directoryManager != null) {
                String projectLocationPrefix = "";
                AbstractProjectProperties projectProperties = directoryManager.getProjectProperties();
                if (projectProperties.isRemoteProject()) {
                    String serverName = ProjectTreeControl.getInstance().getActiveServerName();
                    projectLocationPrefix = serverName + ": " + projectProperties.getProjectName();
                } else if (projectProperties.isLocalProject()) {
                    projectLocationPrefix = "Local: " + projectProperties.getProjectName();
                }
                projectLocationLabel.setText(projectLocationPrefix);
                String appendedPath = directoryManager.getAppendedPath();
                if (appendedPath.length() == 0) {
                    appendedPath = " ";
                }
                projectLocationValue.setText(appendedPath);
            } else {
                projectLocationLabel.setText("Project Name: ");
                projectLocationValue.setText(" ");
            }
        } else {
            projectLocationLabel.setText("Project Name: ");
            projectLocationValue.setText(" ");
        }
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JPanel projectLocationPanel;
    private javax.swing.JLabel projectLocationValue;
// End of variables declaration//GEN-END:variables
}
