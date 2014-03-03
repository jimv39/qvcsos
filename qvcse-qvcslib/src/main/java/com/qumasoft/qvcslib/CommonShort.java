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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * Common short.
 * @author Jim Voris
 */
public final class CommonShort implements Serializable {
    private static final long serialVersionUID = -2637065448431951225L;

    // These first 2 bytes get serialized.  The short "value" exists just for convenience.
    private int shortValue;
    private static final int MINUS_ONE = 65534;
    private static final int MINUS_ONE_MASK = 0xFFFF;

    /**
     * Default Constructor. Defaults the value to 0.
     */
    public CommonShort() {
        shortValue = 0;
    }

    /**
     * Constructor to initialize to given value.
     * @param val new value.
     */
    public CommonShort(int val) {
        shortValue = val;
    }

    /**
     * Set the value.
     * @param inValue new inValue.
     */
    public void setValue(int inValue) {
        shortValue = inValue;
    }

    /**
     * Get the value.
     * @return the value.
     */
    public int getValue() {
        if (shortValue == MINUS_ONE) {
            return -1;
        } else if ((shortValue & MINUS_ONE_MASK) == MINUS_ONE_MASK) {
            return -1;
        } else {
            return shortValue;
        }
    }

    /**
     * Public method to read from a file.
     * @param inStream the stream to read from.
     * @throws java.io.IOException on a read problem.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        byte[] input = new byte[2];
        inStream.read(input);

        byte[] convertedInput = new byte[2];
        convertedInput[0] = input[1];
        convertedInput[1] = input[0];
        short inShort;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(convertedInput); DataInputStream dataInStream = new DataInputStream(inputStream)) {
            inShort = dataInStream.readShort();
        }

        shortValue = inShort;
    }

    /**
     * Write to a file.
     * @param outStream the stream to write to.
     * @throws java.io.IOException on a write problem.
     */
    public void write(RandomAccessFile outStream) throws IOException {
        byte[] convertedOutput;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2); DataOutputStream dataOutStream = new DataOutputStream(outputStream)) {
            dataOutStream.writeShort(shortValue);
            byte[] buffer = outputStream.toByteArray();
            convertedOutput = new byte[2];
            convertedOutput[0] = buffer[1];
            convertedOutput[1] = buffer[0];
        }

        outStream.write(convertedOutput);
    }

    /**
     * Read from a data input stream.
     * @param inStream the stream to read from.
     * @throws java.io.IOException on a read problem.
     */
    public void read(DataInputStream inStream) throws IOException {
        byte[] input = new byte[2];
        inStream.read(input);

        byte[] convertedInput = new byte[2];
        convertedInput[0] = input[1];
        convertedInput[1] = input[0];
        short inShort;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(convertedInput); DataInputStream dataInStream = new DataInputStream(inputStream)) {
            inShort = dataInStream.readShort();
        }

        shortValue = inShort;
    }

    /**
     * Write to a data output stream.
     * @param outStream the stream to write to.
     * @throws java.io.IOException on a write problem.
     */
    public void write(DataOutputStream outStream) throws IOException {
        byte[] convertedOutput;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2); DataOutputStream dataOutStream = new DataOutputStream(outputStream)) {
            dataOutStream.writeShort(shortValue);
            byte[] buffer = outputStream.toByteArray();
            convertedOutput = new byte[2];
            convertedOutput[0] = buffer[1];
            convertedOutput[1] = buffer[0];
        }

        outStream.write(convertedOutput);
    }
}
