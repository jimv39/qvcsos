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
package com.qumasoft.guitools.qwin;

/**
 * The right parent pane. The file list and the activity panels are its children.
 * @author Jim Voris
 */
public class RightParentPane extends javax.swing.JPanel {
    private static final long serialVersionUID = 3563503666702613576L;

    /**
     * Creates new form RightParentPane.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public RightParentPane() {
        initComponents();
        QWinFrame.getQWinFrame().setRightParentPane(this);
    }

    void initSplitter() {
        int height = QWinFrame.getQWinFrame().getCurrentRemoteProperties().getFileListHeight("", "");
        if (height > 0) {
            horizontalSplitter.setDividerLocation(height);
        } else {
            horizontalSplitter.setDividerLocation(horizontalSplitter.getMaximumDividerLocation());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the
     * FormEditor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        horizontalSplitter = new javax.swing.JSplitPane();

        setLayout(new java.awt.BorderLayout());

        horizontalSplitter.setDividerSize(8);
        horizontalSplitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        horizontalSplitter.setOneTouchExpandable(true);
        horizontalSplitter.setBottomComponent(new RightDetailPane());
        horizontalSplitter.setTopComponent(new RightFilePane());

        add(horizontalSplitter);
    }// </editor-fold>//GEN-END:initComponents
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane horizontalSplitter;
// End of variables declaration//GEN-END:variables
}
