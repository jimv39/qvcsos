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

import com.qumasoft.qvcslib.QVCSRuntimeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class LegacyZlibCompressor implements LegacyCompressor {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyZlibCompressor.class);
    private static final int OUTPUT_BUFFER_PADDING_SIZE = 16;
    private byte[] unCompressedBuffer;
    private byte[] compressedBuffer;
    private boolean bufferIsCompressedFlag;

    /**
     * Default constructor.
     */
    public LegacyZlibCompressor() {
    }

    @Override
    public boolean compress(byte[] inputBuffer) {
        boolean retVal = true;
        unCompressedBuffer = inputBuffer;

        // Compress the bytes
        byte[] output = new byte[inputBuffer.length + OUTPUT_BUFFER_PADDING_SIZE];
        Deflater compresser = new Deflater();
        compresser.setInput(inputBuffer);
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        compresser.end();
        if ((LegacyRevisionCompressionHeader.getHeaderSize() + compressedDataLength) >= inputBuffer.length) {
            // Compression didn't save anything.
            retVal = false;
        } else {
            try {
                LegacyRevisionCompressionHeader compressionHeader = new LegacyRevisionCompressionHeader();
                compressionHeader.setInputSize(inputBuffer.length);
                compressionHeader.setCompressedSize(LegacyRevisionCompressionHeader.getHeaderSize() + compressedDataLength);
                compressionHeader.setCompressionType(LegacyRevisionCompressionHeader.COMPRESS_ALGORITHM_2);

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                DataOutputStream outStream = new DataOutputStream(byteStream);
                compressionHeader.write(outStream);
                outStream.write(output, 0, compressedDataLength);
                outStream.close();
                byteStream.close();
                compressedBuffer = byteStream.toByteArray();
                bufferIsCompressedFlag = true;
            } catch (IOException e) {
                LOGGER.info("ZLib Compression failure: " + e.getLocalizedMessage());
                retVal = false;
                bufferIsCompressedFlag = false;
            }
        }
        return retVal;
    }

    @Override
    public byte[] expand(LegacyRevisionCompressionHeader compressionHeader, byte[] inputBuffer) {
        /*
         * Allocate memory for the output (decompressed buffer)
         */
        byte[] outputBuffer = new byte[(int) compressionHeader.getInputSize()];
        try {
            // Decompress the bytes
            Inflater decompresser = new Inflater();
            decompresser.setInput(inputBuffer, LegacyRevisionCompressionHeader.getHeaderSize(), inputBuffer.length - LegacyRevisionCompressionHeader.getHeaderSize());
            int resultLength = decompresser.inflate(outputBuffer);
            decompresser.end();
            if (resultLength != compressionHeader.getInputSize()) {
                throw new QVCSRuntimeException("Decompressed size mismatch");
            }
        } catch (DataFormatException e) {
            LOGGER.info("ZLib decompression failure: " + e.getLocalizedMessage());
        }
        return outputBuffer;
    }

    @Override
    public byte[] expand(byte[] inputBuffer) {
        byte[] retVal = null;
        ByteArrayInputStream inStream = new ByteArrayInputStream(inputBuffer);
        DataInputStream dataInStream = new DataInputStream(inStream);
        LegacyRevisionCompressionHeader compressionHeader = new LegacyRevisionCompressionHeader();
        try {
            compressionHeader.read(dataInStream);
            retVal = expand(compressionHeader, inputBuffer);

            dataInStream.close();
            inStream.close();
        } catch (IOException e) {
            LOGGER.warn("Caught IOException in expand: " + e.getLocalizedMessage());
        }
        return retVal;
    }

    @Override
    public byte[] getUncompressedBuffer() {
        return this.unCompressedBuffer;
    }

    @Override
    public void setUncompressedBuffer(byte[] uncompressedBuffer) {
        this.unCompressedBuffer = uncompressedBuffer;
    }

    @Override
    public byte[] getCompressedBuffer() {
        return this.compressedBuffer;
    }

    @Override
    public ByteArrayInputStream getCompressedStream() {
        ByteArrayInputStream byteArrayInputStream;
        if (bufferIsCompressedFlag) {
            byteArrayInputStream = new ByteArrayInputStream(compressedBuffer);
        } else {
            byteArrayInputStream = new ByteArrayInputStream(unCompressedBuffer);
        }
        return byteArrayInputStream;
    }

    @Override
    public boolean getBufferIsCompressedFlag() {
        return this.bufferIsCompressedFlag;
    }

}
