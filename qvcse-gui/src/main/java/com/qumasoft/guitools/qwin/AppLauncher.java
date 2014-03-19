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

import java.awt.SplashScreen;
import javax.swing.SwingUtilities;

/**
 * Launch the client application. Use this small class so the splash should load faster.
 * @author Jim Voris
 */
public final class AppLauncher {
    /** Loading this statically puts it on the screen pretty quick. The actual resource is specified in the manifest file by the SplashScreen-Image property. */
    private static final SplashScreen SPLASH_SCREEN = SplashScreen.getSplashScreen();

    /** Make this private so no one can use it. */
    private AppLauncher() {
    }

    /**
     * Main entry point for QVCS-Enterprise client application.
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        QWinFrame.initSplashScreen();

        // Run this on the swing thread.
        Runnable swingTask = new Runnable() {

            @Override
            public void run() {
                QWinFrame qwinFrame = new QWinFrame(args);
                QWinFrame.setQwinFrameSingleton(qwinFrame);
                qwinFrame.initialize();
                qwinFrame.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(swingTask);
    }
}
