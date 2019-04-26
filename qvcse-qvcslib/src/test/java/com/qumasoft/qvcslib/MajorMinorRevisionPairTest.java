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

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * MajorMinor revision pair test.
 *
 * @author Jim Voris
 */
public class MajorMinorRevisionPairTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getMinorNumber method, of class com.qumasoft.qvcslib.MajorMinorRevisionPair.
     */
    @Test
    public void testGetMinorNumber() {
        System.out.println("testGetMinorNumber");

        MajorMinorRevisionPair testPair = new MajorMinorRevisionPair(1, 0);
        if (0 != testPair.getMinorNumber()) {
            fail("testGetMinorNumber failed 1st test.");
        }
    }

    /**
     * Test of getMajorNumber method, of class com.qumasoft.qvcslib.MajorMinorRevisionPair.
     */
    @Test
    public void testGetMajorNumber() {
        System.out.println("testGetMajorNumber");

        MajorMinorRevisionPair testPair = new MajorMinorRevisionPair(1, 0);
        if (1 != testPair.getMajorNumber()) {
            fail("testGetMajorNumber failed 1st test.");
        }
    }

    /**
     * Test of toString method, of class com.qumasoft.qvcslib.MajorMinorRevisionPair.
     */
    @Test
    public void testToString() {
        System.out.println("testToString");

        MajorMinorRevisionPair testPair1 = new MajorMinorRevisionPair(1, 0);
        if (0 != testPair1.toString().compareTo("1.0")) {
            fail("testToString failed 1st test.");
        }

        MajorMinorRevisionPair testPair2 = new MajorMinorRevisionPair(1, 1);
        if (0 != testPair2.toString().compareTo("1.1")) {
            fail("testToString failed 2nd test.");
        }

        MajorMinorRevisionPair testPair3 = new MajorMinorRevisionPair(1, -1);
        if (0 != testPair3.toString().compareTo("1")) {
            fail("testToString failed 3rd test.");
        }
    }

    /**
     * Test of read method, of class com.qumasoft.qvcslib.MajorMinorRevisionPair.
     */
    @Test
    public void testReadAndWrite() {
        System.out.println("testRead");

        java.io.File testFile = null;
        java.io.RandomAccessFile testStream = null;

        try {
            String testFileName = System.getProperty("user.dir")
                    + File.separator
                    + "MajorMinorRevisionPairTest.test";
            testFile = new java.io.File(testFileName);
            testStream = new java.io.RandomAccessFile(testFile, "rw");

            MajorMinorRevisionPair testPair1 = new MajorMinorRevisionPair(1, 0);
            testPair1.write(testStream);

            MajorMinorRevisionPair testPair2 = new MajorMinorRevisionPair(1, 1);
            testPair2.write(testStream);

            MajorMinorRevisionPair testPair3 = new MajorMinorRevisionPair(1, -1);
            testPair3.write(testStream);

            testStream.seek(0);

            MajorMinorRevisionPair testPairRead = new MajorMinorRevisionPair();

            testPairRead.read(testStream);
            if ((1 != testPairRead.getMajorNumber())
                    || (0 != testPairRead.getMinorNumber())) {
                fail("testReadAndWrite failed 1st read");
            }

            testPairRead.read(testStream);
            if ((1 != testPairRead.getMajorNumber())
                    || (1 != testPairRead.getMinorNumber())) {
                fail("testReadAndWrite failed 2nd read");
            }

            testPairRead.read(testStream);
            if ((1 != testPairRead.getMajorNumber())
                    || (-1 != testPairRead.getMinorNumber())) {
                fail("testReadAndWrite failed 3rd read");
            }
        } catch (IOException e) {
        } finally {
            try {
                if (testStream != null) {
                    testStream.close();
                    if (testFile != null) {
                        testFile.delete();
                    }
                }
            } catch (IOException e) {
            }
        }
    }
}
