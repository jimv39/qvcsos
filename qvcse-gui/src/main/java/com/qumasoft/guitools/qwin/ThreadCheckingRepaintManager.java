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

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * Thread checking repaint manager.
 * @author Jim Voris
 */
public class ThreadCheckingRepaintManager extends RepaintManager {

    @Override
    public synchronized void addInvalidComponent(JComponent jComponent) {
        checkThread();
        super.addInvalidComponent(jComponent);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void checkThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            System.out.println("Wrong Thread");
            Thread.dumpStack();
        }
    }

    @Override
    public synchronized void addDirtyRegion(JComponent jComponent, int i, int i1, int i2, int i3) {
        checkThread();
        super.addDirtyRegion(jComponent, i, i1, i2, i3);
    }
}
