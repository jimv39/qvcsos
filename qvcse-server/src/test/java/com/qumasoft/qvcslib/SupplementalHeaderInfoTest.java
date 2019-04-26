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

import com.qumasoft.server.LogFile;
import java.io.File;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Supplemental header info test.
 * @author Jim Voris
 */
public class SupplementalHeaderInfoTest {

    /**
     * Our test archive
     */
    private LogFile testArchive = null;

    /**
     * Default ctor.
     */
    public SupplementalHeaderInfoTest() {
    }

    /**
     * Run once to set things up for all these tests.
     *
     * @throws java.lang.Exception if something goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * Run once after all tests are finished. Used for cleanup.
     *
     * @throws java.lang.Exception if something goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Run before <i>each</i> test.
     */
    @Before
    public void setUp() {
        // Create the archive file object.
        testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "QVCSEnterpriseServer.kbwb");
    }

    /**
     * Run after each test.
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of getWorkfileCheckedOutToLocation method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testGetWorkfileCheckedOutToLocation() {
        System.out.println("getWorkfileCheckedOutToLocation");
        String workfileName = testArchive.getLogFileHeaderInfo().getSupplementalHeaderInfo().getWorkfileCheckedOutToLocation();
        String expResult = "/Users/JimVoris/qRoot/QVCSEnterpriseSource/QumaProjects/com/qumasoft/server/QVCSEnterpriseServer.java";
        assertEquals("Found unexpected workfile location.", workfileName, expResult);
    }

    /**
     * Test of setWorkfileCheckedOutToLocation method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testSetWorkfileCheckedOutToLocation() {
        System.out.println("setWorkfileCheckedOutToLocation");
        String checkedOutToLocation = "Test checkout location.";
        testArchive.getLogFileHeaderInfo().getSupplementalHeaderInfo().setWorkfileCheckedOutToLocation(checkedOutToLocation);
        String verifyCheckedOutToLocation = testArchive.getLogFileHeaderInfo().getWorkfileName();
        assertEquals("Failed to verify setting workfile location.", verifyCheckedOutToLocation, checkedOutToLocation);
    }

    /**
     * Test of getLastModifierIndex method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testGetLastModifierIndex() {
        System.out.println("getLastModifierIndex");
        SupplementalHeaderInfo instance = testArchive.getLogFileHeaderInfo().getSupplementalHeaderInfo();
        int expResult = 0;
        int result = instance.getLastModifierIndex();
        assertEquals("Unexpected modifier index.", expResult, result);
    }

    /**
     * Test of setLastModifierIndex method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testSetLastModifierIndex() {
        System.out.println("setLastModifierIndex");
        int modifierIndex = 1;
        SupplementalHeaderInfo instance = testArchive.getLogFileHeaderInfo().getSupplementalHeaderInfo();
        instance.setLastModifierIndex(modifierIndex);
        int modifiedModifierIndex = instance.getLastModifierIndex();
        assertEquals("Unexpected modified modifier index", modifierIndex, modifiedModifierIndex);
    }

    /**
     * Test of getLastArchiveUpdateDate method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testGetLastArchiveUpdateDate() {
        System.out.println("getLastArchiveUpdateDate");
        SupplementalHeaderInfo instance = testArchive.getLogFileHeaderInfo().getSupplementalHeaderInfo();
        long expResult = 1212888393000L;
        Date result = instance.getLastArchiveUpdateDate();
        long actualDateTime = result.getTime();
        assertEquals("Unexpected archive update date.", expResult, actualDateTime);
    }

    /**
     * Test of setLastArchiveUpdateDate method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testSetLastArchiveUpdateDate() {
        System.out.println("setLastArchiveUpdateDate");
        Date date = new Date();
        SupplementalHeaderInfo instance = testArchive.getLogFileHeaderInfo().getSupplementalHeaderInfo();
        instance.setLastArchiveUpdateDate(date);
        long modifiedDate = instance.getLastArchiveUpdateDate().getTime();
        assertEquals("Unexpected modified archive update date.", date.getTime(), modifiedDate);
    }

    /**
     * Test of getLastWorkfileSize method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testGetLastWorkfileSize() {
        System.out.println("getLastWorkfileSize");
        SupplementalHeaderInfo instance = testArchive.getLogFileHeaderInfo().getSupplementalHeaderInfo();
        long expResult = 61483L;
        long result = instance.getLastWorkfileSize();
        assertEquals("Unexpected last workfile size.", expResult, result);
    }

    /**
     * Test of setLastWorkfileSize method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testSetLastWorkfileSize() {
        System.out.println("setLastWorkfileSize");
        long workfileSize = 12345678L;
        SupplementalHeaderInfo instance = testArchive.getLogFileHeaderInfo().getSupplementalHeaderInfo();
        instance.setLastWorkfileSize(workfileSize);
        long fetchedLastWorkfileSize = instance.getLastWorkfileSize();
        assertEquals("Unexpected modified last workfile size.", workfileSize, fetchedLastWorkfileSize);
    }

    /**
     * Test of read method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testRead() {
        // Skipping this test as there is code coverage elsewhere.
    }

    /**
     * Test of write method, of class SupplementalHeaderInfo.
     */
    @Test
    public void testWrite() {
        // Skipping this test as there is code coverage elsewhere.
    }
}
