/*
 * Copyright 2021 Jim Voris.
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
package com.qvcsos.server.archivemigration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Jim Voris
 */
public class LegacyCommon32Long {
    private int longValue = 0;

    private static final int FIRST_BYTE = 0x000000FF;
    private static final int SECOND_BYTE = 0x0000FF00;
    private static final int THIRD_BYTE = 0x00FF0000;
    private static final int FOURTH_BYTE = 0xFF000000;
    private static final int ALL_BITS_MASK = 0xFFFFFFFF;
    private static final int SHIFT_24 = 24;
    private static final int SHIFT_8 = 8;

    /**
     * Initialize to the given value.
     *
     * @param value initial value.
     */
    public LegacyCommon32Long(int value) {
        longValue = value;
    }

    /**
     * Default constructor. Initialize to a value of 0.
     */
    public LegacyCommon32Long() {
    }

    /**
     * Get the value.
     *
     * @return the value.
     */
    public int getValue() {
        return longValue;
    }

    /**
     * Set the value.
     *
     * @param newValue the value.
     */
    public void setValue(int newValue) {
        longValue = newValue;
    }

    /**
     * Read from a file.
     *
     * @param inStream the stream to read from.
     * @throws java.io.IOException on a read problem.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        int inInt = inStream.readInt();
        int in = (ALL_BITS_MASK & inInt);

        // swap the bytes around.
        int lsb = ((in & FOURTH_BYTE) >>> SHIFT_24) & FIRST_BYTE;
        int nlsb1 = ((in & THIRD_BYTE) >>> SHIFT_8) & SECOND_BYTE;
        int nlsb2 = ((in & SECOND_BYTE) << SHIFT_8) & THIRD_BYTE;
        int nlsb3 = ((in & FIRST_BYTE) << SHIFT_24) & FOURTH_BYTE;
        longValue = lsb + nlsb1 + nlsb2 + nlsb3;
    }

    /**
     * Write to a file.
     *
     * @param outStream the file to write to.
     * @throws java.io.IOException on a write problem.
     */
    public void write(RandomAccessFile outStream) throws IOException {
        // swap the bytes around.
        int lsb = ((longValue & FOURTH_BYTE) >>> SHIFT_24) & FIRST_BYTE;
        int nlsb1 = ((longValue & THIRD_BYTE) >>> SHIFT_8) & SECOND_BYTE;
        int nlsb2 = ((longValue & SECOND_BYTE) << SHIFT_8) & THIRD_BYTE;
        int nlsb3 = ((longValue & FIRST_BYTE) << SHIFT_24) & FOURTH_BYTE;
        int outValue = lsb + nlsb1 + nlsb2 + nlsb3;
        outStream.writeInt(outValue);
    }

    /**
     * Read from a data input stream.
     *
     * @param inStream the stream to read from.
     * @throws java.io.IOException on a read problem.
     */
    public void read(DataInputStream inStream) throws IOException {
        int inInt = inStream.readInt();
        int in = (ALL_BITS_MASK & inInt);

        // swap the bytes around.
        int lsb = ((in & FOURTH_BYTE) >>> SHIFT_24) & FIRST_BYTE;
        int nlsb1 = ((in & THIRD_BYTE) >>> SHIFT_8) & SECOND_BYTE;
        int nlsb2 = ((in & SECOND_BYTE) << SHIFT_8) & THIRD_BYTE;
        int nlsb3 = ((in & FIRST_BYTE) << SHIFT_24) & FOURTH_BYTE;
        longValue = lsb + nlsb1 + nlsb2 + nlsb3;
    }

    /**
     * Write to a data output stream.
     *
     * @param outStream the stream to write to.
     * @throws java.io.IOException on a write problem.
     */
    public void write(DataOutputStream outStream) throws IOException {
        // swap the bytes around.
        int lsb = ((longValue & FOURTH_BYTE) >>> SHIFT_24) & FIRST_BYTE;
        int nlsb1 = ((longValue & THIRD_BYTE) >>> SHIFT_8) & SECOND_BYTE;
        int nlsb2 = ((longValue & SECOND_BYTE) << SHIFT_8) & THIRD_BYTE;
        int nlsb3 = ((longValue & FIRST_BYTE) << SHIFT_24) & FOURTH_BYTE;
        int outValue = lsb + nlsb1 + nlsb2 + nlsb3;
        outStream.writeInt((outValue));
    }

}
