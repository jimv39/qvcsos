//   Copyright 2004-2019 Jim Voris
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Abstract QVCS Command dialog. A common base class for most of our dialogs.
 *
 * @author Jim Voris
 */
public abstract class AbstractQVCSCommandDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 6526088994689118161L;

    /**
     * Creates a new instance of AbstractQVCSCommandDialog.
     *
     * @param parent the parent frame.
     * @param modal whether the dialog is modal or not.
     */
    public AbstractQVCSCommandDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
    }

    protected void center() {
        Container parent = getParent();
        Dimension parentSize = parent.getSize();
        Point parentLocation = parent.getLocation();
        Dimension size = getSize();

        // Figure out the left boundaries
        double left = (parentSize.getWidth() - size.getWidth()) / 2.;
        double top = (parentSize.getHeight() - size.getHeight()) / 2.;

        Point myLocation = new Point();
        double x = parentLocation.getX() + left;
        double y = parentLocation.getY() + top;
        myLocation.setLocation(x, y);
        setLocation(myLocation);

        // Init the escape key code.
        initEscapeKey();
    }

    protected String selectFile(final String initialFileName, final String title) {
        String returnFullFileName = initialFileName;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setApproveButtonText("Select");
        if (initialFileName.length() > 0) {
            File initialFile = new File(initialFileName);
            chooser.setCurrentDirectory(initialFile.getParentFile());
            chooser.setSelectedFile(initialFile);
        }
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            returnFullFileName = chooser.getSelectedFile().getAbsolutePath();
        }
        return returnFullFileName;
    }

    protected void initEscapeKey() {
        javax.swing.KeyStroke escapeKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, false);
        javax.swing.AbstractAction escapeAction = new AbstractActionImpl();

        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    /**
     * Sub-classes must implement the dismissDialog() method to kill the dialog. Any dialog specific cleanup needs to be done in the sub-class.
     */
    public abstract void dismissDialog();

    private class AbstractActionImpl extends AbstractAction {

        private static final long serialVersionUID = 1L;

        AbstractActionImpl() {
        }

        @Override
        public void actionPerformed(final java.awt.event.ActionEvent e) {
            dismissDialog();
        }
    }

}
