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
//     $Date: Wednesday, March 21, 2012 10:31:05 PM $
//   $Header: DirectoryDAOImplTest.java Revision:1.3 Wednesday, March 21, 2012 10:31:05 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.TestHelper;
import com.qumasoft.server.DatabaseManager;
import com.qumasoft.server.datamodel.Directory;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Directory DAO.
 *
 * @author $Author: JimVoris $
 */
public class DirectoryDAOImplTest {

    private static final String DERBY_TEST_DIRECTORY = "/temp/qvcse/directoryDAOImplTest";
    private static int testProjectId = -1;
    private static int testTrunkBranchId = -1;
    private static int testTrunkRootDirectoryId = -1;
    private static int testTrunkChildDirectoryId = -1;

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-0"));
        TestHelper.emptyDerbyTestDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().setDerbyHomeDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().initializeDatabase();
        testProjectId = DAOTestHelper.createTestProject();
        testTrunkBranchId = DAOTestHelper.createTrunkBranch(testProjectId);
        testTrunkRootDirectoryId = DAOTestHelper.createBranchRootDirectory(1, testTrunkBranchId);
        testTrunkChildDirectoryId = DAOTestHelper.createBranchChildDirectory(2, testTrunkRootDirectoryId, testTrunkRootDirectoryId, "childDirectory", testTrunkBranchId);
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        DatabaseManager.getInstance().shutdownDatabase();
    }

    /**
     * Set up the things common to all the tests.
     */
    @Before
    public void setUp() {
    }

    /**
     * We tear this down after each test.
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of findById method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindById() {
        DirectoryDAOImpl instance = new DirectoryDAOImpl();
        Directory result = instance.findById(testTrunkBranchId, testTrunkRootDirectoryId);
        assertNotNull("Did not find directory", result);
        assertEquals("Unexpected directory id", result.getDirectoryId().intValue(), testTrunkRootDirectoryId);
    }

    /**
     * Test of findByAppendedPath method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindByAppendedPath() {
        String appendedPath = "";
        DirectoryDAOImpl instance = new DirectoryDAOImpl();
        Directory result = instance.findByAppendedPath(testTrunkBranchId, appendedPath);
        assertNotNull("Did not find directory", result);
        assertEquals("Unexpected directory id", result.getDirectoryId().intValue(), testTrunkRootDirectoryId);
    }

    /**
     * Test of findByBranchId method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindByBranchId() {
        DirectoryDAOImpl instance = new DirectoryDAOImpl();
        List<Directory> result = instance.findByBranchId(testTrunkBranchId);
        assertTrue("Did not find any child directories", !result.isEmpty());
    }

    /**
     * Test of findChildDirectories method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindChildDirectories() {
        DirectoryDAOImpl instance = new DirectoryDAOImpl();
        List<Directory> result = instance.findChildDirectories(testTrunkBranchId, testTrunkRootDirectoryId);
        assertTrue("Did not find any child directories", !result.isEmpty());
        assertEquals("Did not find expected child", result.get(0).getDirectoryId().intValue(), 2);
    }

    /**
     * Test of findChildDirectories method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindChildDirectoriesOnOrBeforeViewDate() {
        DirectoryDAOImpl instance = new DirectoryDAOImpl();
        Calendar now = Calendar.getInstance();
        List<Directory> result = instance.findChildDirectoriesOnOrBeforeViewDate(testTrunkBranchId, testTrunkRootDirectoryId, new Date(now.getTimeInMillis()));
        assertTrue("Did not find any child directories", !result.isEmpty());
        assertEquals("Did not find expected child", result.get(0).getDirectoryId().intValue(), 2);
        now.add(Calendar.SECOND, -1);
        result = instance.findChildDirectoriesOnOrBeforeViewDate(testTrunkBranchId, testTrunkRootDirectoryId, new Date(now.getTimeInMillis()));
        assertTrue("Found unexpected children", result.isEmpty());
    }

    /**
     * Test the update method
     *
     * @throws SQLException if there was a problem updating the record.
     */
    @Test
    public void testUpdate() throws SQLException {
        DirectoryDAOImpl instance = new DirectoryDAOImpl();
        Directory directory = instance.findById(testTrunkBranchId, testTrunkChildDirectoryId);
        directory.setAppendedPath("aDifferentChildDirectory");
        instance.update(directory, false);
    }
}
