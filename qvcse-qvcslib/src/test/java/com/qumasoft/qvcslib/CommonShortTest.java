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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CommonShort test.
 * @author Jim Voris
 */
public class CommonShortTest {

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
     * Test of setValue method, of class com.qumasoft.qvcslib.CommonShort.
     */
    @Test
    public void testSetValue() {
        System.out.println("testSetValue");

        // Add your test code below by replacing the default call to fail.
        CommonShort testValue = new CommonShort();
        testValue.setValue(1);
        if (1 != testValue.getValue()) {
            fail("testSetValue failed 1st test");
        }

        testValue.setValue(100);
        if (100 != testValue.getValue()) {
            fail("testSetValue failed 2nd test");
        }

        testValue.setValue(10000);
        if (10000 != testValue.getValue()) {
            fail("testSetValue failed 3rd test");
        }

        testValue.setValue(33000);
        if (33000 != testValue.getValue()) {
            fail("testSetValue failed 4th test");
        }

        testValue.setValue(-1);
        if (-1 != testValue.getValue()) {
            fail("testSetValue failed 5th test");
        }

        testValue.setValue(-2);
        if (-2 != testValue.getValue()) {
            fail("testSetValue failed 6th test");
        }
    }

    /**
     * Test of read and write methods, of class com.qumasoft.qvcslib.CommonShort.
     */
    @Test
    public void testReadAndWriteRandomAccessFile() {
        System.out.println("testReadAndWriteRandomAccessFile");

        java.io.File testFile = null;
        java.io.RandomAccessFile testStream = null;

        try {
            String testFileName = System.getProperty("user.dir")
                    + File.separator
                    + "CommonShortTest.test";
            testFile = new java.io.File(testFileName);
            testStream = new java.io.RandomAccessFile(testFile, "rw");
            CommonShort testShort = new CommonShort();
            testShort.setValue(10);

            testShort.write(testStream);

            testShort.setValue(100);
            testShort.write(testStream);

            testShort.setValue(200);
            testShort.write(testStream);

            testShort.setValue(-1);
            testShort.write(testStream);

            testShort.setValue(-2);
            testShort.write(testStream);

            testStream.seek(0);

            CommonShort testShortRead = new CommonShort();

            testShortRead.read(testStream);
            if (10 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 1st read");
            }

            testShortRead.read(testStream);
            if (100 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 2nd read");
            }

            testShortRead.read(testStream);
            if (200 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 3rd read");
            }

            testShortRead.read(testStream);
            if (-1 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 4th read");
            }

            testShortRead.read(testStream);
            if (-2 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 5th read");
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

    /**
     * Test of read and write methods, of class com.qumasoft.qvcslib.CommonShort.
     */
    @Test
    public void testReadAndWriteDataIOStreams() {
        System.out.println("testReadAndWriteDataIOStreams");

        java.io.File testFile = null;
        java.io.FileOutputStream testOutputStream;
        java.io.DataOutputStream testDataOutputStream;
        java.io.FileInputStream testInputStream;
        java.io.DataInputStream testDataInputStream = null;

        try {
            String testFileName = System.getProperty("user.dir")
                    + File.separator
                    + "CommonShortTest.test";
            testFile = new java.io.File(testFileName);
            testOutputStream = new java.io.FileOutputStream(testFile);
            testDataOutputStream = new java.io.DataOutputStream(testOutputStream);
            CommonShort testShort = new CommonShort();
            testShort.setValue(10);

            testShort.write(testDataOutputStream);

            testShort.setValue(100);
            testShort.write(testDataOutputStream);

            testShort.setValue(200);
            testShort.write(testDataOutputStream);

            testShort.setValue(-1);
            testShort.write(testDataOutputStream);

            testShort.setValue(-2);
            testShort.write(testDataOutputStream);
            testDataOutputStream.close();

            testInputStream = new java.io.FileInputStream(testFile);
            testDataInputStream = new java.io.DataInputStream(testInputStream);

            CommonShort testShortRead = new CommonShort();

            testShortRead.read(testDataInputStream);
            if (10 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 1st read");
            }

            testShortRead.read(testDataInputStream);
            if (100 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 2nd read");
            }

            testShortRead.read(testDataInputStream);
            if (200 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 3rd read");
            }

            testShortRead.read(testDataInputStream);
            if (-1 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 4th read");
            }

            testShortRead.read(testDataInputStream);
            if (-2 != testShortRead.getValue()) {
                fail("testReadAndWrite failed 5th read");
            }
        } catch (IOException e) {
        } finally {
            try {
                if (testDataInputStream != null) {
                    testDataInputStream.close();
                    if (testFile != null) {
                        testFile.delete();
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Test of calendar object to see if it makes sense
     */
    @Test
    public void testCalendarBehavior() {
        Calendar calendar1 = Calendar.getInstance();
        System.out.println("Time: [" + calendar1.getTimeInMillis() + "]");
        int currentHourOfDay = calendar1.get(Calendar.HOUR_OF_DAY);

        calendar1.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println("Time: [" + calendar1.getTimeInMillis() + "]");

        calendar1.set(Calendar.HOUR_OF_DAY, 16);
        System.out.println("Time: [" + calendar1.getTimeInMillis() + "]");

        calendar1.setTimeZone(TimeZone.getDefault());
        System.out.println("Time: [" + calendar1.getTimeInMillis() + "]");

        calendar1.set(Calendar.HOUR_OF_DAY, currentHourOfDay);
        System.out.println("Time: [" + calendar1.getTimeInMillis() + "]");

        System.out.println("==============================================");
        Date now = new Date();
        calendar1.setTime(now);

        System.out.println("Time: [" + calendar1.getTimeInMillis() + "]");

        calendar1.setTime(now);
        calendar1.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println("Time: [" + calendar1.getTimeInMillis() + "]");

        calendar1.setTimeZone(TimeZone.getDefault());
        calendar1.setTime(now);
        System.out.println("Time: [" + calendar1.getTimeInMillis() + "]");
    }
}
