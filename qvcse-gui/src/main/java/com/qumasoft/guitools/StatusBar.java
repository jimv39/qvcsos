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

package com.qumasoft.guitools;

import com.qumasoft.qvcslib.TimerManager;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.TimerTask;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 * A status bar.
 * @author Jim Voris
 */
public class StatusBar extends JPanel {

    private static final long serialVersionUID = 8941822665380657639L;

    private final JLabel[] statusPanes;
    private final BevelBorder bevelBorder;
    private final JPanel progressPanel;
    private final JProgressBar progressBar;
    private final Box infoBox;
    private final Font statusFont;
    private boolean progressVisibleFlag = false;
    private TimerTask refreshTask;
    private static final long REFRESH_DELAY = 200; // 2/10 a second
    private static final int FONT_SIZE = 12;

    /**
     * Create a status bar, one pane per string.
     * @param panes the strings to populate the status bar.
     */
    public StatusBar(String[] panes) {
        super(new BorderLayout());
        this.statusFont = new Font("SansSerif", Font.PLAIN, FONT_SIZE);
        this.bevelBorder = new BevelBorder(BevelBorder.LOWERED);
        progressPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        infoBox = new Box(BoxLayout.X_AXIS);
        statusPanes = new JLabel[panes.length];
        for (int i = 0; i < panes.length; i++) {
            statusPanes[i] = new JLabel(panes[i]);
            statusPanes[i].setBorder(bevelBorder);
            statusPanes[i].setOpaque(true);
            statusPanes[i].setFont(statusFont);
            infoBox.add(statusPanes[i]);
        }
        add(infoBox, BorderLayout.EAST);
        add(progressPanel, BorderLayout.WEST);
    }

    /**
     * Set the status bar text.
     * @param paneIndex the pane index.
     * @param text the text to show in that pane.
     */
    public void setPaneText(int paneIndex, java.lang.String text) {
        statusPanes[paneIndex].setText(text);
    }

    /**
     * Indicate progress.
     * @param flag visible (true) or not (false).
     */
    public void indicateProgress(final boolean flag) {
        if (flag) {
            // Cancel pending task.
            if (refreshTask != null) {
                refreshTask.cancel();
                refreshTask = null;
            }

            if (flag != progressVisibleFlag) {
                progressBar.setVisible(flag);
                progressBar.setIndeterminate(flag);
                progressVisibleFlag = flag;
            }
        } else {
            // Cancel pending task.
            if (refreshTask != null) {
                refreshTask.cancel();
                refreshTask = null;
            }

            // Put the refresh on the Timer thread.  We only want a single refresh
            // to be going on at the same time.  Putting all refresh activities on
            // the timer thread guarantees that only a single refresh is active at
            // any one time.
            refreshTask = new TimerTask() {

                @Override
                public void run() {
                    // Run this on the swing thread.
                    Runnable swingTask = () -> {
                        progressBar.setVisible(flag);
                        progressBar.setIndeterminate(flag);
                        progressVisibleFlag = flag;
                    };
                    SwingUtilities.invokeLater(swingTask);
                }
            };
            TimerManager.getInstance().getTimer().schedule(refreshTask, REFRESH_DELAY);
        }
    }

    /**
     * Get the status panes.
     * @return the status panes.
     */
    public JLabel[] getStatusPanes() {
        return statusPanes;
    }
}
