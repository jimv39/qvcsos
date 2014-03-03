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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * This creates a borderless, frameless, splash screen. It will be centered on-screen and will stay shown for a set number of seconds. It can also be set to allow clicking on it to
 * dismiss it.
 * @author Brian Pipa - http://www.filenabber.com
 * @version 1.0
 */
public class Splash extends JWindow {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     *
     * @param image The image to load.
     * @param theWaitTime The time (in seconds) to display the splash screen
     * @param allowClick Whether or not to allow clicking the screen to dismiss it
     */
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public Splash(ImageIcon image, int theWaitTime, boolean allowClick) {
        super();

        //<editor-fold> convert to seconds (1000 miliseconds = 1 second)
        final int waitTime = 1000 * theWaitTime;
        // </editor-fold>

        JLabel label = new JLabel(image);
        getContentPane().add(label, BorderLayout.CENTER);
        pack();

        //now, center it on-screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = label.getPreferredSize();
        setLocation((screenSize.width / 2) - (labelSize.width / 2),
                (screenSize.height / 2) - (labelSize.height / 2));
        //if allowing clicks, add a Mouse listener
        if (allowClick) {
            addMouseListener(new MouseAdapter() {

                /**
                 * Used to detect a click on the splash screen.
                 *
                 * @param e The mouse event.
                 */
                @Override
                public void mousePressed(MouseEvent e) {
                    setVisible(false);
                    dispose();
                }
            });
        }

        final Runnable closerRunner = new Runnable() {

            @Override
            public void run() {
                setVisible(false);
                dispose();
            }
        };

        Runnable waitRunner = new Runnable() {

            @Override
            @SuppressWarnings("CallToThreadDumpStack")
            public void run() {
                try {
                    Thread.sleep(waitTime);
                    SwingUtilities.invokeAndWait(closerRunner);
                } catch (InvocationTargetException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread splashThread = new Thread(waitRunner, "SplashThread");
        splashThread.start();
    }
}
