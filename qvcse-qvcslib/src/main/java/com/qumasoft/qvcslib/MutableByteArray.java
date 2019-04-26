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

/**
 * Mutable byte array. Note that we do <b><i>not</i></b> make a copy of the byte array object; we only copy the ref.
 * @author Jim Voris
 */
public class MutableByteArray {

    private byte[] byteArray;

    /**
     * Create a mutable byte array with an initial byte array.
     * @param bytes the byte array that may change.
     */
    public MutableByteArray(byte[] bytes) {
        byteArray = bytes;
    }

    /**
     * Default constructor.
     */
    public MutableByteArray() {
    }

    /**
     * Get the byte array.
     * @return the byte array.
     */
    public byte[] getValue() {
        return byteArray;
    }

    /**
     * Set the byte array.
     * @param value the byte array.
     */
    public void setValue(byte[] value) {
        byteArray = value;
    }
}
