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
package com.qumasoft.guitools.merge;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import java.awt.Color;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

/**
 * Merge status bar.
 * @author Jim Voris
 */
class MergeStatusBar extends Box {

    private static final int DELETE_PANE_INDEX = 0;
    private static final int INSERT_PANE_INDEX = 1;
    private static final int COLLISION_PANE_INDEX = 2;
    private static final long serialVersionUID = 1L;
    private final JLabel[] statusPanes;
    private final BevelBorder statusBorder;
    private final String[] statusBarStrings;

    MergeStatusBar() {
        super(BoxLayout.X_AXIS);
        this.statusBarStrings = new String[] {"  Deleted  Lines ", "  Inserted  Lines ", "  Collision Area "};
        this.statusBorder = new BevelBorder(BevelBorder.LOWERED);
        statusPanes = new JLabel[statusBarStrings.length];
        for (int i = 0; i < statusBarStrings.length; i++) {
            statusPanes[i] = new JLabel(statusBarStrings[i]);
            statusPanes[i].setBorder(statusBorder);
            add(statusPanes[i]);
        }
        setPaneColor(MergeStatusBar.DELETE_PANE_INDEX, ColorManager.getDeleteForegroundColor());
        setPaneColor(MergeStatusBar.INSERT_PANE_INDEX, ColorManager.getInsertForegroundColor());
        setPaneColor(MergeStatusBar.COLLISION_PANE_INDEX, ColorManager.getCollisionBackgroundColor());
    }

    final void setPaneColor(int paneIndex, Color color) {
        switch (paneIndex) {
            case DELETE_PANE_INDEX:
            case INSERT_PANE_INDEX:
                statusPanes[paneIndex].setForeground(color);
                break;
            case COLLISION_PANE_INDEX:
                statusPanes[paneIndex].setBackground(color);
                statusPanes[paneIndex].setOpaque(true);
                break;
            default:
                throw new QVCSRuntimeException("Unexpected paneIndex: " + paneIndex + "]");
        }
    }
}
