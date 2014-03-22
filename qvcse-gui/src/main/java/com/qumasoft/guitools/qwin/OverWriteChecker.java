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

import com.qumasoft.guitools.qwin.dialog.OverwriteWorkfileDialog;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.qvcslib.MergedInfoInterface;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 * Overwrite checker.
 * @author Jim Voris
 */
public class OverWriteChecker {
    // Use this flag to figure out whether we need to ask the user whether to overwrite
    // an existing workfile that the user may not want to overwrite.
    private boolean overwriteAnswerHasBeenCapturedFlag = false;
    // This is an object we use to synchronize the dialog that we may use.  The
    // dialog MUST be non-modal, but we need to make it synchronous here so
    // that we block until the dialog is dismissed.  We use this object to
    // provide that synchronization.
    private final Object overwriteHasBeenCapturedSyncObject = new Object();
    // This flag captures the user's answer so we don't need to ask them for each and
    // every file.
    private boolean overwriteAnswerFlag = false;
    // The place where we hold on to the ref of the dialog.  This is needed
    // so that the dialog ctor can run on the Swing thread.  In running on the
    // Swing thread, we need some way to get the ref back to this containing
    // class... which we do via this dialog ref object.
    private OverwriteWorkfileDialog dialogRef;

    /**
     * Creates a new instance of OverWriteChecker.
     */
    public OverWriteChecker() {
    }

    boolean getOverwriteAnswerHasBeenCaptured() {
        return overwriteAnswerHasBeenCapturedFlag;
    }

    /**
     * Set the overwrite answer has been captured flag.
     * @param flag true if we have captured the user's overwrite answer; false otherwise.
     */
    public void setOverwriteAnswerHasBeenCaptured(boolean flag) {
        overwriteAnswerHasBeenCapturedFlag = flag;
    }

    boolean getOverwriteAnswer() {
        return overwriteAnswerFlag;
    }

    /**
     * Set the overwrite answer.
     * @param flag true if we can overwrite; false if we cannot.
     */
    public void setOverwriteAnswer(boolean flag) {
        overwriteAnswerFlag = flag;
    }

    private void setDialogRef(OverwriteWorkfileDialog ref) {
        dialogRef = ref;
    }

    private OverwriteWorkfileDialog getDialogRef() {
        return dialogRef;
    }

    /**
     * Can/should we overwrite the workfile.
     * @param mergedInfo the file of interest.
     * @param progressDialog the progress dialog.
     * @return true if we can overwrite the modified workfile.
     */
    public boolean overwriteEditedWorkfile(final MergedInfoInterface mergedInfo, final ProgressDialog progressDialog) {
        if (getOverwriteAnswerHasBeenCaptured()) {
            return getOverwriteAnswer();
        } else {
            Runnable swingTask = new Runnable() {

                @Override
                public void run() {
                    // Put up a dialog to ask the user if they want to overwrite the
                    // edited workfile.
                    OverwriteWorkfileDialog overWriteDialog = new OverwriteWorkfileDialog(QWinFrame.getQWinFrame(), mergedInfo, overwriteHasBeenCapturedSyncObject);
                    setDialogRef(overWriteDialog);

                    // Make sure the progressDialog is not visible!
                    if (progressDialog != null) {
                        progressDialog.setVisible(false);
                    }

                    // The dialog is non-modal so the compare button will work.
                    overWriteDialog.setVisible(true);
                }
            };

            try {
                synchronized (overwriteHasBeenCapturedSyncObject) {
                    SwingUtilities.invokeLater(swingTask);
                    overwriteHasBeenCapturedSyncObject.wait();
                }

                // Retrieve the ref for the dialog.
                OverwriteWorkfileDialog overWriteDialog = getDialogRef();

                setOverwriteAnswerHasBeenCaptured(overWriteDialog.getDontAskAgainFlag());
                setOverwriteAnswer(overWriteDialog.getOverWriteFlag());
            } catch (InterruptedException e) {
                QWinUtility.logProblem(Level.WARNING, "Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
            }
            return getOverwriteAnswer();
        }
    }
}
