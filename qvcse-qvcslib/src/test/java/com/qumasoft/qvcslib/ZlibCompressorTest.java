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

import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test our Zlib compressor.
 *
 * @author Jim Voris
 */
public class ZlibCompressorTest {

    public ZlibCompressorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of compress method, of class ZlibCompressor.
     * @throws java.io.UnsupportedEncodingException if we cannot support UTF-8.
     */
    @Test
    public void testCompressAndExpand() throws UnsupportedEncodingException {
        System.out.println("test zlib compress and expand");
        String stringToCompress;
        stringToCompress = "This is a string that we should be able to compress, but it needs to be pretty long in order for compression to actually work. Is this long enough?";
        byte[] inputBuffer = stringToCompress.getBytes("UTF-8");
        MutableByteArray outputBuffer = new MutableByteArray();
        ZlibCompressor instance = new ZlibCompressor();
        boolean expResult = true;
        boolean result = instance.compress(inputBuffer);
        assertEquals(expResult, result);
        outputBuffer.setValue(instance.getCompressedBuffer());

        // Now expand it back out.
        System.out.println("de-compress");
        byte[] reexpandedBuffer = instance.expand(outputBuffer.getValue());
        String unCompressedString = new String(reexpandedBuffer, "UTF-8");
        assertEquals(stringToCompress, unCompressedString);
    }
}
