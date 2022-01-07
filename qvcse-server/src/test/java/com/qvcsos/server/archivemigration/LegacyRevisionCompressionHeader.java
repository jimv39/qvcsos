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
public class LegacyRevisionCompressionHeader {

    private final LegacyCommonShort compressionType = new LegacyCommonShort();
    private final LegacyCommon32Long compressedSize = new LegacyCommon32Long();
    private final LegacyCommon32Long inputSize = new LegacyCommon32Long();
    /**
     * The first compression algorithm that QVCS supports.
     */
    public static final int COMPRESS_ALGORITHM_1 = 1;
    /**
     * The second compression algorithm that QVCS supports -- the JDK included
     * zlib compression.
     */
    public static final int COMPRESS_ALGORITHM_2 = 2;
    private static final int COMPRESSION_HEADER_SIZE = 10;

    /**
     * Default constructor.
     */
    public LegacyRevisionCompressionHeader() {
    }

    /**
     * A copy constructor.
     *
     * @param revisionCompressionHeaderToCopy compression header to copy.
     */
    public LegacyRevisionCompressionHeader(LegacyRevisionCompressionHeader revisionCompressionHeaderToCopy) {
        compressionType.setValue(revisionCompressionHeaderToCopy.compressionType.getValue());
        compressedSize.setValue(revisionCompressionHeaderToCopy.compressedSize.getValue());
        inputSize.setValue(revisionCompressionHeaderToCopy.inputSize.getValue());
    }

    public int getCompressionType() {
        return compressionType.getValue();
    }

    public static int getHeaderSize() {
        return COMPRESSION_HEADER_SIZE;
    }

    public long getInputSize() {
        return inputSize.getValue();
    }

    public void setInputSize(int value) {
        inputSize.setValue(value);
    }

    public void setCompressedSize(int value) {
        compressedSize.setValue(value);
    }

    public void setCompressionType(int type) {
        compressionType.setValue(type);
    }

    public void read(DataInputStream inStream) throws IOException {
        inputSize.read(inStream);
        compressedSize.read(inStream);
        compressionType.read(inStream);
    }

    public void read(RandomAccessFile inStream) throws IOException {
        inputSize.read(inStream);
        compressedSize.read(inStream);
        compressionType.read(inStream);
    }

    public void write(DataOutputStream outStream) throws IOException {
        inputSize.write(outStream);
        compressedSize.write(outStream);
        compressionType.write(outStream);
    }

}
