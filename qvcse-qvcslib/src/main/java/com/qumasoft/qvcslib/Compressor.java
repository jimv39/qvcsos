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

import java.io.ByteArrayInputStream;

/**
 * Compressor interface. This helps us abstract away the type of compression that we use.
 * @author Jim Voris
 */
public interface Compressor {

    /**
     * Compress a buffer.
     * @param inputBuffer the input buffer.
     * @return true if compression succeeded in producing something smaller than what was passed in; false otherwise.
     */
    boolean compress(byte[] inputBuffer);

    /**
     * Expand a compressed buffer.
     * @param compressionHeader the compression header.
     * @param inputBuffer the compressed buffer, including the RevisionCompressionHeader at its start.
     * @return the expanded, uncompressed buffer.
     */
    byte[] expand(RevisionCompressionHeader compressionHeader, byte[] inputBuffer);

    /**
     * Expand a compressed buffer.
     * @param inputBuffer the compressed buffer that includes the RevisionCompressionHeader at its start.
     * @return the expanded, uncompressed buffer.
     */
    byte[] expand(byte[] inputBuffer);

    /**
     * Get the uncompressed buffer.
     * @return the uncompressed buffer.
     */
    byte[] getUncompressedBuffer();

    /**
     * Set the uncompressed buffer.
     * @param uncompressedBuffer the uncompressed buffer.
     */
    void setUncompressedBuffer(byte[] uncompressedBuffer);

    /**
     * Get the compressed buffer.
     * @return the compressed buffer.
     */
    byte[] getCompressedBuffer();

    /**
     * Get the compressed stream.
     * @return the compressed stream.
     */
    ByteArrayInputStream getCompressedStream();

    /**
     * Get the buffer is compressed flag.
     * @return the buffer is compressed flag.
     */
    boolean getBufferIsCompressedFlag();
}
