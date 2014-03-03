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
package com.qumasoft.qvcslib;

/**
 * Set obsolete action.
 * @deprecated We shouldn't be marking files as obsolete anymore; we move them to the cemetery instead.
 * @author Jim Voris
 */
public class LogfileActionSetIsObsolete extends LogfileActionType {

    private final boolean isObsoleteFlag;

    /**
     * Creates a new instance of LogfileActionSetIsObsolete.
     * @param flag true to indicate obsolete; false to indicate not obsolete.
     */
    public LogfileActionSetIsObsolete(boolean flag) {
        super("Delete", LogfileActionType.SET_OBSOLETE);
        isObsoleteFlag = flag;
    }

    /**
     * Get the is obsolete flag.
     * @return the is obsolete flag.
     */
    boolean getFlag() {
        return isObsoleteFlag;
    }
}
