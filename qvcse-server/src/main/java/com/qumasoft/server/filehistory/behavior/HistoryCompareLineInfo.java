/*
 * Copyright 2014 JimVoris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.server.filehistory.behavior;

import java.util.Arrays;

/**
 * Capture the information that we need about a line of text. Instances of this class are immutable.
 * @author Jim Voris
 */
public class HistoryCompareLineInfo {
    private static final int HASH_PRIME_1 = 7;
    private static final int HASH_PRIME_2 = 79;

    private final byte[] lineBuffer;
    private final int lineSeekPosition;

    /**
     * Create an instance using seek position, and line contents.
     * @param seekPos the line's seek position.
     * @param buffer the line's buffer.
     */
    public HistoryCompareLineInfo(int seekPos, byte[] buffer) {
        this.lineSeekPosition = seekPos;
        this.lineBuffer = buffer;
    }

    /**
     * Get the line's String.
     * @return the line's String.
     */
    public byte[] getLineBuffer() {
        return this.lineBuffer;
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
        int hash = HASH_PRIME_1;
        hash = HASH_PRIME_2 * hash + Arrays.hashCode(this.lineBuffer);
        hash = HASH_PRIME_2 * hash + this.lineSeekPosition;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        boolean equalsFlag = false;
        if (object instanceof HistoryCompareLineInfo) {
            HistoryCompareLineInfo lineInfo = (HistoryCompareLineInfo) object;
            if (lineInfo.lineBuffer.length == lineBuffer.length) {
                equalsFlag = true;
                for (int index = 0; index < lineBuffer.length; index++) {
                    if (lineInfo.lineBuffer[index] != lineBuffer[index]) {
                        equalsFlag = false;
                        break;
                    }
                }
            }
        }
        return equalsFlag;
    }
}
