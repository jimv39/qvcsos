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

import com.qumasoft.guitools.AbstractQVCSCommandDialog;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

/**
 * Abstract QWin command dialog. Abstract base class for QWin command dialogs.
 * @author Jim Voris
 */
public abstract class AbstractQWinCommandDialog extends AbstractQVCSCommandDialog {
    private static final long serialVersionUID = -1119151311825976492L;

    private static final int MAXIMUM_DIALOG_FONT_SIZE = 14;

    /**
     * Creates a new instance of AbstractQVCSCommandDialog.
     *
     * @param parent the parent frame.
     * @param modal is this modal.
     */
    public AbstractQWinCommandDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }

    /**
     * Set the font for the controls of the dialog to be larger (or smaller) based on the current setting for the application.
     */
    public void setFont() {
        // Limit the maximum font size we use for this dialog.
        int fontSize = QWinFrame.getQWinFrame().getFontSize() + 1;
        if (fontSize > MAXIMUM_DIALOG_FONT_SIZE) {
            fontSize = MAXIMUM_DIALOG_FONT_SIZE;
        }
        Font font = QWinFrame.getQWinFrame().getFont(fontSize);
        Component[] components = getContentPane().getComponents();
        for (Component component : components) {
            component.setFont(font);
            if (component instanceof Container) {
                Container container = (Container) component;
                setContentsFont(font, container);
            }
        }
    }

    /**
     * Recursive method to traverse into any containers so we set the fonts on all the components.
     *
     * @param font the font we are going to use.
     * @param container a container of components.
     */
    private void setContentsFont(Font font, Container container) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setFont(font);
            if (component instanceof Container) {
                Container childContainer = (Container) component;
                setContentsFont(font, childContainer);
            }
        }
    }
}
