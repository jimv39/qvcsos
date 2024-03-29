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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Right detail pane.
 *
 * @author Jim Voris
 */
public class RightDetailPane extends javax.swing.JPanel {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RightDetailPane.class);

    private static final long serialVersionUID = 6740774331225407499L;

    private static final int REVISION_INFO_TAB_INDEX = 0;
    private static final int ALL_REVISION_INFO_TAB_INDEX = 1;
    private static final int TAG_INFO_TAB_INDEX = 2;
    private static final int ACTIVITY_LOG_TAB_INDEX = 3;

    private final TagInfoPane tagInfoPane = new TagInfoPane();
    private final ActivityPane activityPane = new ActivityPane();
    private final RevisionInfoDetailPane revisionInfoPane = new RevisionInfoDetailPane();
    private final AllRevisionInfoDetailPane allRevisionInfoPane = new AllRevisionInfoDetailPane();

    /**
     * Creates new form RightDetailPane.
     */
    public RightDetailPane() {
        initComponents();
        addPages();
        QWinFrame.getQWinFrame().setRightDetailPane(this);
    }

    private void addPages() {
        tabbedPane.add("Revision Info", revisionInfoPane);
        tabbedPane.add("All Revision Info", allRevisionInfoPane);
        tabbedPane.add("Tag Information", tagInfoPane);
        tabbedPane.add("Activity Log", activityPane);

        // Let the main frame window know we exist
        QWinFrame.getQWinFrame().setRevisionInfoPane(revisionInfoPane);
        QWinFrame.getQWinFrame().setAllRevisionInfoPane(allRevisionInfoPane);
        QWinFrame.getQWinFrame().setTagInfoPane(tagInfoPane);
        QWinFrame.getQWinFrame().setActivityPane(activityPane);
    }

    void setFontSize(int fontSize) {
        tabbedPane.setFont(QWinFrame.getQWinFrame().getFont(fontSize + 1));

        activityPane.setFontSize(fontSize + 1);
        revisionInfoPane.setFontSize(fontSize + 1);
        allRevisionInfoPane.setFontSize(fontSize + 1);
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
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
        add(tabbedPane);
    }// </editor-fold>//GEN-END:initComponents

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        if (evt.getSource() instanceof javax.swing.JTabbedPane) {
            javax.swing.JTabbedPane pane = (javax.swing.JTabbedPane) evt.getSource();
            LOGGER.debug("Selected index: [{}]", pane.getSelectedIndex());
            if (pane.getSelectedIndex() == TAG_INFO_TAB_INDEX) {
                List<TagInfoData> tagInfoList = QWinFrame.getQWinFrame().getTagInfoList();
                tagInfoPane.setTagInfoList(tagInfoList);
            }

            // Clear any file selections...
            if (QWinFrame.getQWinFrame().getFileTable() != null) {
                QWinFrame.getQWinFrame().getFileTable().getSelectionModel().clearSelection();
            }
        }
    }//GEN-LAST:event_tabbedPaneStateChanged

    public boolean isRevisionInfoSelected() {
        boolean retVal = false;
        if (tabbedPane.getSelectedIndex() == REVISION_INFO_TAB_INDEX) {
            retVal = true;
        }
        return retVal;
    }

    public boolean isAllRevisionInfoSelected() {
        boolean retVal = false;
        if (tabbedPane.getSelectedIndex() == ALL_REVISION_INFO_TAB_INDEX) {
            retVal = true;
        }
        return retVal;
    }

    public boolean isTagInfoSelected() {
        boolean retVal = false;
        if (tabbedPane.getSelectedIndex() == TAG_INFO_TAB_INDEX) {
            retVal = true;
        }
        return retVal;
    }

    public boolean isActivityLogSelected() {
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
