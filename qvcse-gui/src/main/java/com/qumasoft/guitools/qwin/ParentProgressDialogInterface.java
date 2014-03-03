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
 * Parent progress dialog interface.
 * @author Jim Voris
 */
public interface ParentProgressDialogInterface {

    /**
     * Initialize the parent progress bar.
     * @param min the minimum value of the progress bar.
     * @param max the maximum value of the progress bar.
     */
    void initParentProgressBar(int min, int max);

    /**
     * Set the parent progress of the progress bar.
     * @param progress how far along are we. This value should be &gt; min and &lt; max values used to initialize the progress bar.
     */
    void setParentProgress(final int progress);

    /**
     * What parent action is happening.
     * @param action describe the action.
     */
    void setParentAction(final String action);

    /**
     * Describe the parent activity.
     * @param activity describe the activity.
     */
    void setParentActivity(final String activity);
}
