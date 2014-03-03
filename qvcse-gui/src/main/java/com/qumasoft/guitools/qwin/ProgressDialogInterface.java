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

/**
 * Progress dialog interface.
 * @author Jim Voris
 */
public interface ProgressDialogInterface {

    /**
     * Initialize the progress bar.
     * @param min the minimum value of the progress bar.
     * @param max the maximum value of the progress bar.
     */
    void initProgressBar(int min, int max);

    /**
     * Set the progress of the progress bar.
     * @param progress how far along are we. This value should be &gt; min and &lt; max values used to initialize the progress bar.
     */
    void setProgress(final int progress);

    /**
     * What action is happening.
     * @param action describe the action.
     */
    void setAction(final String action);

    /**
     * Describe the activity.
     * @param activity describe the activity.
     */
    void setActivity(final String activity);

    /**
     * Should the progress bar be visible.
     * @param flag true for visible; false for not visible.
     */
    void setVisible(boolean flag);

    /**
     * Get the visible flag.
     * @return the visible flag.
     */
    boolean getProgressDialogVisibleFlag();

    /**
     * Set the visible flag.
     * @param flag the visible flag.
     */
    void setProgressDialogVisibleFlag(boolean flag);

    /**
     * Has the dialog been canceled.
     * @return true if canceled; false if not canceled.
     */
    boolean getIsCancelled();
}
