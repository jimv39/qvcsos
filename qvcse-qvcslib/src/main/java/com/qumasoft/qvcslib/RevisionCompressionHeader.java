/*   Copyright 2004-2021 Jim Voris
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

/**
 * Revision compression header. An optional compression header that is present for those revisions that have been compressed.
 * @author Jim Voris
 */
public class RevisionCompressionHeader implements java.io.Serializable {
    private static final long serialVersionUID = 382929972339129666L;

    private int compressionType;
    private int compressedSize;
    private int inputSize;
    /** The first compression algorithm that QVCS supports. */
    public static final int COMPRESS_ALGORITHM_1 = 1;
    /** The second compression algorithm that QVCS supports -- the JDK included zlib compression. */
    public static final int COMPRESS_ALGORITHM_2 = 2;
    private static final int COMPRESSION_HEADER_SIZE = 12;

    /**
     * Default constructor.
     */
    public RevisionCompressionHeader() {
    }

    /**
     * A copy constructor.
     * @param revisionCompressionHeaderToCopy compression header to copy.
     */
    public RevisionCompressionHeader(RevisionCompressionHeader revisionCompressionHeaderToCopy) {
        compressionType = revisionCompressionHeaderToCopy.compressionType;
        compressedSize = revisionCompressionHeaderToCopy.compressedSize;
        inputSize = revisionCompressionHeaderToCopy.inputSize;
    }

    /**
     * Get the input size (the size before compression).
     * @return the input size.
     */
    public long getInputSize() {
        return inputSize;
    }

    /**
     * Set the input size.
     * @param value the input size.
     */
    public void setInputSize(int value) {
        inputSize = value;
    }

    /**
     * Get the compressed size.
     * @return the compressed size.
     */
    public long getCompressedSize() {
        return compressedSize;
    }

    /**
     * Set the compressed size.
     * @param value the compressed size.
     */
    public void setCompressedSize(int value) {
        compressedSize = value;
    }

    /**
     * Read the compression header from the given random access file stream.
     * @param inStream the random access file stream.
     * @throws IOException for read problems.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        inputSize = inStream.readInt();
        compressedSize = inStream.readInt();
        compressionType = inStream.readInt();
    }

    /**
     * Read the compression header (as an object) from a data input stream.
     * @param inStream the data input stream.
     * @throws IOException for read problems.
     */
    public void read(DataInputStream inStream) throws IOException {
        inputSize = inStream.readInt();
        compressedSize = inStream.readInt();
        compressionType = inStream.readInt();
    }

    /**
     * Write this compression header to a data output stream.
     * @param outStream the data output stream.
     * @throws IOException for write problems.
     */
    public void write(DataOutputStream outStream) throws IOException {
        outStream.writeInt(inputSize);
        outStream.writeInt(compressedSize);
        outStream.writeInt(compressionType);
    }

    /**
     * Get the type of compression. We only support the one type at this time.
     * @return the type of compression.
     */
    public int getCompressionType() {
        return compressionType;
    }

    /**
     * Set the compression type.
     * @param type the compression type.
     */
    public void setCompressionType(int type) {
        QumaAssert.isTrue((type == COMPRESS_ALGORITHM_1) || (type == COMPRESS_ALGORITHM_2));
        compressionType = type;
    }

    /**
     * Get the size of the compression header.
     * @return the size of the compression header.
     */
    public static int getHeaderSize() {
        return COMPRESSION_HEADER_SIZE;
    }
}
