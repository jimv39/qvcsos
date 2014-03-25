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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.logfileaction.ActionType;

/**
 * Implement this interface to be notified of LogFile changes. This is typically used by the directory managers so they can be notified by the LogFile objects when things
 * change (and the change was initiated by some other user).
 * @author Jim Voris
 */
public interface LogfileListenerInterface {

    /**
     * Notify the listener of some change to the LogFile.
     * @param subject the object that changed.
     * @param action the type of change.
     */
    void notifyLogfileListener(ArchiveInfoInterface subject, ActionType action);
}
