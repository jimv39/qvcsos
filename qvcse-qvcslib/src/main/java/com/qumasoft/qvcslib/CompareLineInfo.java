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
 * Capture the information that we need about a line of text. Instances of this class are immutable.
 * @author Jim Voris
 */
public class CompareLineInfo {

    private final String lineString;
    private final int lineSeekPosition;

    /**
     * Create an instance using seek position, and line contents.
     * @param seekPos the line's seek position.
     * @param lineStr the line's String.
     */
    public CompareLineInfo(int seekPos, String lineStr) {
        this.lineSeekPosition = seekPos;
        this.lineString = lineStr;
    }

    /**
     * Get the line's String.
     * @return the line's String.
     */
    public String getLineString() {
        return this.lineString;
    }

    /**
     * Get the line's seek position.
     * @return the line's seek position.
     */
    public int getLineSeekPosition() {
        return this.lineSeekPosition;
    }

    @Override
    public int hashCode() {
        return this.lineString.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        boolean equalsFlag = false;
        if (object instanceof CompareLineInfo) {
            CompareLineInfo lineInfo = (CompareLineInfo) object;
            String otherString = lineInfo.getLineString();
            equalsFlag = lineString.equals(otherString);
        }
        return equalsFlag;
    }
}
