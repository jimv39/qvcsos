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

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Jim Voris
 */
public class LegacyMajorMinorRevisionPair {
    private final LegacyCommonShort minorNumber;
    private final LegacyCommonShort minorAttributes;
    private final LegacyCommonShort majorNumber;
    private final LegacyCommonShort majorAttributes;

    /**
     * Default constructor. Will create a pair 0.0
     */
    public LegacyMajorMinorRevisionPair() {
        majorAttributes = new LegacyCommonShort();
        majorNumber = new LegacyCommonShort();
        minorAttributes = new LegacyCommonShort();
        minorNumber = new LegacyCommonShort();
    }

    /**
     * Create a major.minor pair using the supplied major.minor values.
     *
     * @param major the major number.
     * @param minor the minor number.
     */
    public LegacyMajorMinorRevisionPair(int major, int minor) {
        majorAttributes = new LegacyCommonShort();
        majorNumber = new LegacyCommonShort(major);
        minorAttributes = new LegacyCommonShort();
        minorNumber = new LegacyCommonShort(minor);
    }

    /**
     * Get the minor number.
     *
     * @return the minor number.
     */
    public int getMinorNumber() {
        return minorNumber.getValue();
    }

    /**
     * Get the major number.
     *
     * @return the major number.
     */
    public int getMajorNumber() {
        return majorNumber.getValue();
    }

    /**
     * A useful string representation of this major.minor pair.
     *
     * @return A useful string representation of this major.minor pair.
     */
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();
        returnString.append(Integer.toString(majorNumber.getValue()));
        short sMinor = (short) minorNumber.getValue();

        // Only report the minor number if it's != -1
        if (sMinor != (short) -1) {
            returnString.append(".").append(Integer.toString(minorNumber.getValue()));
        }
        return returnString.toString();
    }

    /**
     * Read the major/minor pair part of a a revision descriptor contained in
     * the archive header. Assume the stream is positioned correctly.
     *
     * @param inStream the random access stream to read from.
     * @throws java.io.IOException for a read error.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        majorAttributes.read(inStream);
        majorNumber.read(inStream);
        minorAttributes.read(inStream);
        minorNumber.read(inStream);
    }

}
