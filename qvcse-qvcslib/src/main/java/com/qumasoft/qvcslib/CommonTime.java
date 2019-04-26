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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

/**
 * A class we use to read/write a C++ time_t value to/from an I/O stream.
 *
 * @author Jim Voris
 * @see Common32Long
 * @see Common32Short
 */
public final class CommonTime implements java.io.Serializable {
    private static final long serialVersionUID = -7285763794565705969L;

    /**
     * The internal long value for the time_t value, converted to the Java representation.
     */
    private final Common32Long timeValue;
    private static final long MILLISECONDS_PER_SECOND = 1000L;

    /**
     * Default constructor. Default the time to now.
     */
    public CommonTime() {
        Date now = new Date();
        this.timeValue = new Common32Long((int) (now.getTime() / MILLISECONDS_PER_SECOND));
    }

    /**
     * Constructor that to initialize the time using the given number of seconds past the epoch.
     * @param value new time value (second past the epoch).
     */
    public CommonTime(int value) {
        this.timeValue = new Common32Long(value);
    }

    /**
     * Return a long value that represents the common time -- i.e. the number of seconds since Jan 1, 1970.
     * @return A long.
     */
    public long getValue() {
        return timeValue.getValue();
    }

    /**
     * Set the value of this timestamp.
     * @param value the number of seconds past Jan 1, 1970.
     */
    public void setValue(int value) {
        timeValue.setValue(value);
    }

    /**
     * Return a Date that represents this timestamp.
     * @return A Date object that represents this timestamp.
     */
    public java.util.Date getValueAsDate() {
        return new Date(MILLISECONDS_PER_SECOND * timeValue.getValue());
    }

    /**
     * Read a time_t value from the random access file in the byte order native to a Windows/Intel time_t value.
     * @param inStream The random access stream to read from. Note that we should be positioned at the bytes we want to read -- ie. this object doesn't seek in the file at all.
     * @exception java.io.IOException Throws an IOException if the read fails.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        timeValue.read(inStream);
    }

    /**
     * Write to a file.
     * @param outStream the stream to write to.
     * @throws IOException on a write problem.
     */
    public void write(RandomAccessFile outStream) throws IOException {
        timeValue.write(outStream);
    }

    /**
     * Read from a data input stream.
     * @param inStream the stream to read from.
     * @throws IOException on a read problem.
     */
    public void read(DataInputStream inStream) throws IOException {
        timeValue.read(inStream);
    }

    /**
     * Write the time out to the data stream in a byte order compatible with Windows byte order for a C++ time_t value.
     * @param outStream The output stream that we write to.
     * @exception java.io.IOException Throw an IOException if the write fails.
     */
    public void write(DataOutputStream outStream) throws IOException {
        timeValue.write(outStream);
    }
}
