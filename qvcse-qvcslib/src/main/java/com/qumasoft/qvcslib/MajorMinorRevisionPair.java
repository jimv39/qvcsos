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

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class used to capture revision major.minor revision couplets. Instances are immutable.
 * @author Jim Voris
 */
public final class MajorMinorRevisionPair implements java.io.Serializable {
    private static final long serialVersionUID = 5725948044937218276L;

    private final CommonShort minorNumber;
    private final CommonShort minorAttributes;
    private final CommonShort majorNumber;
    private final CommonShort majorAttributes;

    /**
     * Default constructor. Will create a pair 0.0
     */
    public MajorMinorRevisionPair() {
        majorAttributes = new CommonShort();
        majorNumber = new CommonShort();
        minorAttributes = new CommonShort();
        minorNumber = new CommonShort();
    }

    /**
     * Create a major.minor pair using the supplied major.minor values.
     * @param major the major number.
     * @param minor the minor number.
     */
    public MajorMinorRevisionPair(int major, int minor) {
        majorAttributes = new CommonShort();
        majorNumber = new CommonShort(major);
        minorAttributes = new CommonShort();
        minorNumber = new CommonShort(minor);
    }

    /**
     * Get the minor number.
     * @return the minor number.
     */
    public int getMinorNumber() {
        return minorNumber.getValue();
    }

    /**
     * Get the major number.
     * @return the major number.
     */
    public int getMajorNumber() {
        return majorNumber.getValue();
    }

    /**
     * A useful string representation of this major.minor pair.
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
     * Read the major/minor pair part of a a revision descriptor contained in the archive header. Assume the stream is positioned correctly.
     * @param inStream the random access stream to read from.
     * @throws java.io.IOException for a read error.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        majorAttributes.read(inStream);
        majorNumber.read(inStream);
        minorAttributes.read(inStream);
        minorNumber.read(inStream);
    }

    /**
     * Write the major/minor pair part of a a revision descriptor contained in the archive header. Assume the stream is positioned correctly.
     * @param outStream the random access stream to write to.
     * @throws java.io.IOException for a write error.
     */
    public void write(RandomAccessFile outStream) throws IOException {
        majorAttributes.write(outStream);
        majorNumber.write(outStream);
        minorAttributes.write(outStream);
        minorNumber.write(outStream);
    }
}
