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

/**
 * Right detail pane.
 *
 * @author Jim Voris
 */
public class RightDetailPane extends javax.swing.JPanel {

    private static final long serialVersionUID = 6740774331225407499L;

    private static final int REVISION_AND_LABEL_INFO_TAB_INDEX = 0;
    private static final int REVISION_INFO_TAB_INDEX = 1;
    private static final int LABEL_INFO_TAB_INDEX = 2;
    private static final int ACTIVITY_LOG_TAB_INDEX = 3;
    private final LoggingPane revisionInfoPane = new LoggingPane();
    private final LoggingPane labelInfoPane = new LoggingPane();
    private final ActivityPane activityPane = new ActivityPane();
    private final RevAndLabelInfoDetailPane revAndLabelInfoPane = new RevAndLabelInfoDetailPane();

    /**
     * Creates new form RightDetailPane.
     */
    public RightDetailPane() {
        initComponents();
        addPages();
        QWinFrame.getQWinFrame().setRightDetailPane(this);
    }

    private void addPages() {
        tabbedPane.add("Revision And Label Info", revAndLabelInfoPane);
        tabbedPane.add("Revision Information", revisionInfoPane);
        tabbedPane.add("Label Information", labelInfoPane);
        tabbedPane.add("Activity Log", activityPane);

        // Let the main frame window know we exist
        QWinFrame.getQWinFrame().setRevAndLabelInfoPane(revAndLabelInfoPane);
        QWinFrame.getQWinFrame().setRevisionInfoPane(revisionInfoPane);
        QWinFrame.getQWinFrame().setLabelInfoPane(labelInfoPane);
        QWinFrame.getQWinFrame().setActivityPane(activityPane);
    }

    void setFontSize(int fontSize) {
        tabbedPane.setFont(QWinFrame.getQWinFrame().getFont(fontSize + 1));

        revisionInfoPane.setFontSize(fontSize + 1);
        labelInfoPane.setFontSize(fontSize + 1);
        activityPane.setFontSize(fontSize + 1);
        revAndLabelInfoPane.setFontSize(fontSize + 1);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the
     * FormEditor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.BorderLayout());

        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        tabbedPane.setDoubleBuffered(true);
        tabbedPane.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        add(tabbedPane);
    }// </editor-fold>//GEN-END:initComponents

    boolean isRevAndLabelInfoSelected() {
        boolean retVal = false;
        if (tabbedPane.getSelectedIndex() == REVISION_AND_LABEL_INFO_TAB_INDEX) {
            retVal = true;
        }
        return retVal;
    }

    boolean isRevisionInfoSelected() {
        boolean retVal = false;
        if (tabbedPane.getSelectedIndex() == REVISION_INFO_TAB_INDEX) {
            retVal = true;
        }
        return retVal;
    }

    boolean isLabelInfoSelected() {
        boolean retVal = false;
        if (tabbedPane.getSelectedIndex() == LABEL_INFO_TAB_INDEX) {
            retVal = true;
        }
        return retVal;
    }

    boolean isActivityLogSelected() {
        boolean retVal = false;
        if (tabbedPane.getSelectedIndex() == ACTIVITY_LOG_TAB_INDEX) {
            retVal = true;
        }
        return retVal;
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
// End of variables declaration//GEN-END:variables
}
