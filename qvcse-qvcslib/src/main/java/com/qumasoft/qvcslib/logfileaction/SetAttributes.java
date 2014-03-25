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
package com.qumasoft.qvcslib.logfileaction;

import com.qumasoft.qvcslib.ArchiveAttributes;

/**
 * Set attributes action.
 * @author Jim Voris
 */
public class SetAttributes extends ActionType {

    private final ArchiveAttributes attributes;

    /**
     * Creates a new instance of LogfileActionSetAttributes.
     * @param attribs the archive attributes.
     */
    public SetAttributes(ArchiveAttributes attribs) {
        super("Set Attributes", ActionType.SET_ATTRIBUTES);
        attributes = attribs;
    }

    /**
     * Get the archive attributes.
     * @return the archive attributes.
     */
    ArchiveAttributes getAttributes() {
        return attributes;
    }
}
