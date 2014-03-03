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
package com.qumasoft.guitools.compare;

import java.awt.Color;

import javax.swing.border.BevelBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

/**
 * Status bar for compare frame.
 * @author Jim Voris
 */
class StatusBar extends Box {
    private static final long serialVersionUID = 6799815513668803945L;

    private final JLabel[] statusPanes;
    private final BevelBorder statusBorder;

    StatusBar(String[] panes) {
        super(BoxLayout.X_AXIS);
        this.statusBorder = new BevelBorder(BevelBorder.LOWERED);
        statusPanes = new JLabel[panes.length];
        for (int i = 0; i < panes.length; i++) {
            statusPanes[i] = new JLabel(panes[i]);
            statusPanes[i].setBorder(statusBorder);
            add(statusPanes[i]);
        }
    }

    void setPaneColor(int paneIndex, Color color) {
        statusPanes[paneIndex].setForeground(color);
    }
}
