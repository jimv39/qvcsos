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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default compressor implementation. This is based on an algorithm I discovered a long time ago (in C++), and ported to Java.
 * @author Jim Voris
 */
public class DefaultCompressor implements Compressor {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
    private static final int ITEMMAX = 16;
    private byte[] inputBuffer;
    private byte[] outputBuffer;
    private int inputIndex;
    private int outputIndex;
    private int walkerIndexA;
    private int walkerIndexB;
    private static final int ONE_NIBBLE_BIT_COUNT = 4;
    private static final int TWO_NIBBLE_BIT_COUNT = 8;
    private static final int FOUR_NIBBLE_BIT_COUNT = 16;
    private static final int FOUR_K = 4096;
    private static final int INCREMENTOR_SHIFT_COUNT = 13;
    private static final int LO_ONE_NIBBLE_BITS = 0x000F;
    private static final int LO_SECOND_NIBBLE_BITS = 0x00F0;
    private static final int LO_TWO_NIBBLE_BITS = 0x00FF;
    private static final int LO_THIRD_NIBBLE_BITS = 0x0F00;
    private static final int LO_THREE_NIBBLE_BITS = 0x0FFF;
    private static final int MOST_SIGNIFICANT_BIT = 0x08000;
    private static final int MAGIC_PRIME_NUMBER = 40543;
    private byte[] unCompressedBuffer;
    private byte[] compressedBuffer;
    private boolean bufferIsCompressedFlag;

    /**
     * Default constructor.
     */
    public DefaultCompressor() {
    }

    /**
     * Compress the input buffer. Return true if we compressed the data, false otherwise. If compression is successful, the outputBuffer will contain the compressed result. The
     * compressed result includes a RevisionCompressionHeader as the first 10 bytes.
     *
     * Note that this method is not re-entrant, so we have to synchronize it -- it uses some object level variables that make it unworkable for it to be called again (on the same
     * object).
     * @param inBuffer to buffer that we want to compress.
     * @return true if we were able to compress the input buffer; false if not.
     */
    @Override
    public synchronized boolean compress(byte[] inBuffer) {
        boolean returnVal = true;
        bufferIsCompressedFlag = false;
        unCompressedBuffer = inBuffer;
        RevisionCompressionHeader compressionHeader = new RevisionCompressionHeader();

        // Allocate memory for the hash table.
        int[] hash = new int[FOUR_K];

        inputIndex = 0;
        outputIndex = 0;
        inputBuffer = inBuffer;

        int sourcePostIndex = inBuffer.length - 1;
        int sourceMax1Index = sourcePostIndex - ITEMMAX;
        int sourceMax16Index = sourcePostIndex - (ITEMMAX * ITEMMAX);

        int control = 0;
        int controlBits = 0;

        int controlIndex = outputIndex;
        outputIndex += 2;

        boolean literalFlag = false;

        try {
            // Allocate memory for the target (compressed) output buffer
            outputBuffer = new byte[inputBuffer.length + ITEMMAX];

            while (true) {
                short unroll = FOUR_NIBBLE_BIT_COUNT;
                walkerIndexA = 0;
                walkerIndexB = 0;
                int offset = 0;
                int len;

                if (outputIndex > inputBuffer.length) {
                    throw new CompressionRuntimeException("compression overrun, compression failed");
                }
                if (inputIndex > sourceMax16Index) {
                    unroll = 1;
                    if (inputIndex > sourceMax1Index) {
                        if (inputIndex == sourcePostIndex + 1) {
                            break;
                        }
                        literalFlag = true;
                    }
                }
                beginUnrolledLoop:
                while (true) {
                    if (!literalFlag) {
                        long index = ((MAGIC_PRIME_NUMBER * ((((inputBuffer[inputIndex] << ONE_NIBBLE_BIT_COUNT) ^ inputBuffer[inputIndex + 1]) << ONE_NIBBLE_BIT_COUNT)
                                ^ inputBuffer[inputIndex + 2])) >>> ONE_NIBBLE_BIT_COUNT) & LO_THREE_NIBBLE_BITS;
                        walkerIndexB = hash[(int) index];
                        walkerIndexA = inputIndex;
                        hash[(int) index] = inputIndex;
                        offset = walkerIndexA - walkerIndexB;
                    }
                    if (literalFlag || (offset > (FOUR_K - 1)) || (walkerIndexB < 0) || (offset == 0) || walkAndMatch() || walkAndMatch() || walkAndMatch()) {
                        literalFlag = false;
                        outputBuffer[outputIndex++] = inputBuffer[inputIndex++];
                        control >>>= 1;
                        controlBits++;
                    } else {
                        incrementor();
                        len = walkerIndexA - inputIndex - 1;
                        outputBuffer[outputIndex++] = (byte) (((offset & LO_THIRD_NIBBLE_BITS) >>> ONE_NIBBLE_BIT_COUNT) + (len - 1));
                        outputBuffer[outputIndex++] = (byte) (offset & LO_TWO_NIBBLE_BITS);
                        inputIndex += len;
                        control = (control >>> 1) | MOST_SIGNIFICANT_BIT;
                        controlBits++;
                    }
                    if (--unroll != 0) {
                        continue beginUnrolledLoop;
                    } else {
                        break;
                    }
                }
                if (controlBits == FOUR_NIBBLE_BIT_COUNT) {
                    outputBuffer[controlIndex] = (byte) (control & LO_TWO_NIBBLE_BITS);
                    outputBuffer[controlIndex + 1] = (byte) (control >>> TWO_NIBBLE_BIT_COUNT);
                    controlIndex = outputIndex;
                    outputIndex += 2;
                    control = 0;
                    controlBits = 0;
                }
            }

            control >>>= FOUR_NIBBLE_BIT_COUNT - controlBits;
            outputBuffer[controlIndex++] = (byte) (control & LO_TWO_NIBBLE_BITS);
            outputBuffer[controlIndex++] = (byte) (control >>> TWO_NIBBLE_BIT_COUNT);
            if (controlIndex == outputIndex) {
                outputIndex -= 2;
            }
            compressionHeader.setInputSize(inBuffer.length);
            compressionHeader.setCompressedSize(RevisionCompressionHeader.getHeaderSize() + outputIndex);
            compressionHeader.setCompressionType(RevisionCompressionHeader.COMPRESS_ALGORITHM_1);

            if (RevisionCompressionHeader.getHeaderSize() + outputIndex >= inputBuffer.length) {
                // Compression didn't save anything.
                returnVal = false;
                bufferIsCompressedFlag = false;
            } else {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                DataOutputStream outStream = new DataOutputStream(byteStream);
                compressionHeader.write(outStream);
                outStream.write(outputBuffer, 0, outputIndex);
                outStream.close();
                byteStream.close();
                compressedBuffer = byteStream.toByteArray();
                bufferIsCompressedFlag = true;
            }
        } catch (java.lang.OutOfMemoryError e) {
            // If they are trying to create an archive for a really big file,
            // we might have problems.
            LOGGER.log(Level.WARNING, "Out of memory trying to compress data.");
            System.gc();
            returnVal = false;
        } catch (CompressionRuntimeException e) {
            returnVal = false;
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            LOGGER.log(Level.FINE, "Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            returnVal = false;
        } catch (java.io.IOException e) {
            LOGGER.log(Level.WARNING, "Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            returnVal = false;
        }
        return returnVal;
    }

    /**
     * The input buffer has the RevisionCompressionHeader at the beginning.
     *
     * @param inputBufferLocal compressed input buffer that has a RevisionCompressionHeader at its beginning.
     * @return the expanded buffer.
     */
    @Override
    public byte[] expand(byte[] inputBufferLocal) {
        byte[] retVal = null;
        ByteArrayInputStream inStream = new ByteArrayInputStream(inputBufferLocal);
        DataInputStream dataInStream = new DataInputStream(inStream);
        RevisionCompressionHeader compressionHeader = new RevisionCompressionHeader();
        try {
            compressionHeader.read(dataInStream);
            retVal = expand(compressionHeader, inputBufferLocal);

            dataInStream.close();
            inStream.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Caught IOException in expand: " + e.getLocalizedMessage());
        }
        return retVal;
    }

    @Override
    public byte[] expand(RevisionCompressionHeader compressionHeader, byte[] inputBufferLocal) {
        int controlbits = 0;
        int control = 0;
        int srcIndex;
        int dstIndex;
        int junk;
        byte[] outputBufferLocal;

        /*
         * Allocate memory for the output (decompressed buffer)
         */
        outputBufferLocal = new byte[(int) compressionHeader.getInputSize()];

        dstIndex = 0;
        srcIndex = RevisionCompressionHeader.getHeaderSize();

        while (srcIndex < inputBufferLocal.length) {
            if (controlbits == 0) {
                junk = inputBufferLocal[srcIndex++];
                junk &= LO_TWO_NIBBLE_BITS;
                control = junk;
                junk = inputBufferLocal[srcIndex++];
                junk &= LO_TWO_NIBBLE_BITS;
                control |= junk << TWO_NIBBLE_BIT_COUNT;
                controlbits = FOUR_NIBBLE_BIT_COUNT;
            }
            if ((control & 1) == 1) {
                int offset;
                int len;
                int p;

                junk = inputBufferLocal[srcIndex];
                junk &= LO_TWO_NIBBLE_BITS;
                offset = (junk & LO_SECOND_NIBBLE_BITS) << ONE_NIBBLE_BIT_COUNT;
                junk = inputBufferLocal[srcIndex++];
                junk &= LO_TWO_NIBBLE_BITS;
                len = 1 + (junk & LO_ONE_NIBBLE_BITS);
                junk = inputBufferLocal[srcIndex++];
                junk &= LO_TWO_NIBBLE_BITS;
                offset += junk & LO_TWO_NIBBLE_BITS;
                p = dstIndex - offset;

                while (len-- > 0) {
                    outputBufferLocal[dstIndex++] = outputBufferLocal[p++];
                }
            } else {
                outputBufferLocal[dstIndex++] = inputBufferLocal[srcIndex++];
            }
            control >>>= 1;
            controlbits--;
        }

        if (dstIndex != (int) compressionHeader.getInputSize()) {
            throw new QVCSRuntimeException("Decompression failed in DefaultCompressor");
        }

        return outputBufferLocal;
    }

    private boolean walkAndMatch() {
        boolean retVal = inputBuffer[walkerIndexA++] != inputBuffer[walkerIndexB++];
        return retVal;
    }

    private void incrementor() {
        boolean bResult = false;
        for (int i = 0; i < INCREMENTOR_SHIFT_COUNT; i++) {
            if (walkAndMatch()) {
                bResult = true;
                break;
            }
        }
        if (!bResult) {
            walkerIndexA++;
        }
    }

    @Override
    public void setUncompressedBuffer(byte[] uncompressedBuffer) {
        this.unCompressedBuffer = uncompressedBuffer;
    }

    @Override
    public byte[] getUncompressedBuffer() {
        return this.unCompressedBuffer;
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
