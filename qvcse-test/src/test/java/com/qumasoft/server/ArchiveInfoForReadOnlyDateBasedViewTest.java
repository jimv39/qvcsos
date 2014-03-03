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
// $FilePath$
//     $Date: Wednesday, March 21, 2012 10:31:02 PM $
//   $Header: ArchiveInfoForReadOnlyDateBasedViewTest.java Revision:1.3 Wednesday, March 21, 2012 10:31:02 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.server;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.RevisionInformation;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jim Voris
 */
public class ArchiveInfoForReadOnlyDateBasedViewTest {

    private RemoteViewProperties remoteViewProperties = null;

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        ArchiveDigestManager.getInstance().initialize(QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Set up the things common to all the tests.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @Before
    public void setUp() throws Exception {
        remoteViewProperties = new RemoteViewProperties(TestHelper.getTestProjectName(), "Test View");

        // Set the view time to January 1, 2007.
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2007, 0, 1);
        remoteViewProperties.setDateBaseDate(calendar.getTime());
        remoteViewProperties.setDateBasedViewBranch(QVCSConstants.QVCS_TRUNK_VIEW);
    }

    /**
     * We tear this down after each test.
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of getShortWorkfileName method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetShortWorkfileName() {
        System.out.println("getShortWorkfileName");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String expResult = "TestDateBasedReadOnlyView.java";
        String result = mergedInfo.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLockCount method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetLockCount() {
        System.out.println("getLockCount");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        int expResult = 0;
        int result = mergedInfo.getLockCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLockedByString method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetLockedByString() {
        System.out.println("getLockedByString");
        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String expResult = "";
        String result = mergedInfo.getLockedByString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLastCheckInDate method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetLastCheckInDate() {
        System.out.println("getLastCheckInDate");
        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        Date lastCheckInDate = mergedInfo.getLastCheckInDate();
        Calendar calendar = Calendar.getInstance();
        Date viewDate = remoteViewProperties.getDateBasedDate();
        calendar.setTime(viewDate);
        if (lastCheckInDate.getTime() > viewDate.getTime()) {
            fail("Last checkin date is after the view date.  This is wrong.!!");
        }
    }

    /**
     * Test of getWorkfileInLocation method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetWorkfileInLocation() {
        System.out.println("getWorkfileInLocation");
        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String expResult = "";
        String result = mergedInfo.getWorkfileInLocation();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLastEditBy method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetLastEditBy() {
        System.out.println("getLastEditBy");
        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String expResult = "JimVoris";
        String result = mergedInfo.getLastEditBy();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAttributes method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetAttributes() {
        System.out.println("getAttributes");
        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        ArchiveAttributes expResult = testArchive.getAttributes();
        ArchiveAttributes result = mergedInfo.getAttributes();
        assertEquals(expResult.getAttributesAsInt(), result.getAttributesAsInt());
    }

    /**
     * Test of getLogfileInfo method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetLogfileInfo() {
        System.out.println("getLogfileInfo");
        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        LogfileInfo result = mergedInfo.getLogfileInfo();
        if (result.getRevisionInformation().getRevisionHeader(0).getCheckInDate().getTime() > remoteViewProperties.getDateBasedDate().getTime()) {
            fail("LogfileInfo tip revision is newer than view date!");
        }
    }

    /**
     * Test of getRevisionDescription method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetRevisionDescription() {
        System.out.println("getRevisionDescription");
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String revisionString = "1.0";
        String result = mergedInfo.getRevisionDescription(revisionString);
        System.out.println("Revision description for " + revisionString + ": " + result);
    }

    /**
     * Test of getRevisionAsByteArray method with invalid revision string
     */
    @Test
    public void testGetInvalidRevisionAsByteArray() {
        System.out.println("getRevisionAsByteArray with invalid revision.");
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String revisionString = "1.5000";
        byte[] result = mergedInfo.getRevisionAsByteArray(revisionString);
        if (result != null) {
            fail("Retrieved invalid revision " + revisionString + ": as byte array.");
        }
    }

    /**
     * Test of getLockedRevisionString method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetLockedRevisionString() {
        System.out.println("getLockedRevisionString");
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String userName = "JimVoris";
        String expResult = "";
        String result = mergedInfo.getLockedRevisionString(userName);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDefaultRevisionString method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetDefaultRevisionString() {
        System.out.println("getDefaultRevisionString");
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String result = mergedInfo.getDefaultRevisionString();
        if (result == null) {
            fail("Unexpected null value for default revision string.");
        }
    }

    /**
     * Test of getRevisionCount method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetRevisionCount() {
        System.out.println("getRevisionCount");
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        int result = mergedInfo.getRevisionCount();
        System.out.println("\tFound a revision count of:" + result);
        if (result <= 1) {
            fail("Didn't find a useful revision count.");
        }
    }

    /**
     * Test of getFullArchiveFilename method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetFullArchiveFilename() {
        System.out.println("getFullArchiveFilename");
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        String result = mergedInfo.getFullArchiveFilename();
        System.out.println("Full archive filename: " + result);
        if (!result.contains("TestDateBasedReadOnlyView.kbwb")) {
            fail("getFullArchiveFilename returned an unexpected value:" + result);
        }
    }

    /**
     * Test of getRevisionInformation method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    @Test
    public void testGetRevisionInformation() {
        System.out.println("getRevisionInformation");
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestDateBasedReadOnlyView.kbwb");
        ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
        RevisionInformation expResult = null;
        RevisionInformation result = mergedInfo.getRevisionInformation();

        // TODO -- need to test for success.
    }
    /**
     * Test of getDefaultRevisionDigest method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    /*
     * public void testGetDefaultRevisionDigest() { System.out.println("getDefaultRevisionDigest"); ArchiveInfoForReadOnlyDateBasedView instance = null; byte[] expResult = null; byte[] result =
     * instance.getDefaultRevisionDigest(); assertEquals(expResult, result); // TODO review the generated test code and remove the default call to fail. fail("The test case is a prototype."); }
     */
    /**
     * Test of getRevision method, of class ArchiveInfoForReadOnlyDateBasedView.
     *
     * @throws Exception
     */
    /*
     * public void testGetRevision() throws Exception { System.out.println("getRevision"); LogFileOperationGetRevisionCommandArgs commandLineArgs = null; String fetchToFileName = "";
     * ArchiveInfoForReadOnlyDateBasedView instance = null; boolean expResult = false; boolean result = instance.getRevision(commandLineArgs, fetchToFileName); assertEquals(expResult, result); // TODO
     * review the generated test code and remove the default call to fail. fail("The test case is a prototype."); }
     */
    /**
     * Test of getForVisualCompare method, of class ArchiveInfoForReadOnlyDateBasedView.
     *
     * @throws Exception
     */
    /*
     * public void testGetForVisualCompare() throws Exception { System.out.println("getForVisualCompare"); LogFileOperationGetRevisionCommandArgs commandLineArgs = null; String outputFileName = "";
     * ArchiveInfoForReadOnlyDateBasedView instance = null; boolean expResult = false; boolean result = instance.getForVisualCompare(commandLineArgs, outputFileName); assertEquals(expResult, result);
     * // TODO review the generated test code and remove the default call to fail. fail("The test case is a prototype."); }
     */
    /**
     * Test of getRevisionAsByteArray method, of class ArchiveInfoForReadOnlyDateBasedView.
     */
    /*
     * public void testGetRevisionAsByteArray() { System.out.println("getRevisionAsByteArray"); LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator +
     * "TestDateBasedReadOnlyView.kbwb"); ArchiveInfoForReadOnlyDateBasedView mergedInfo = new ArchiveInfoForReadOnlyDateBasedView("TestDateBasedReadOnlyView.java", testArchive, remoteViewProperties);
     * String revisionString = "1.0"; byte[] result = mergedInfo.getRevisionAsByteArray(revisionString); if (result == null) { fail("Failed to retrieve revision " + revisionString + ": as byte
     * array."); } }
     */
}
