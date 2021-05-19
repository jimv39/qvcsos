/*   Copyright 2021 Jim Voris
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
package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.TestHelper;
import com.qumasoft.server.PostgresDatabaseManager;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Directory;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Directory DAO against the test Postgresql database.
 *
 * @author Jim Voris
 */
public class DirectoryDAOImplPostgresqlTest {

    private static int testProjectId = -1;
    private static int testTrunkBranchId = -1;
    private static int testTrunkRootDirectoryId = -1;
    private static int testTrunkChildDirectoryId = -1;
    private static final String TEST_SCHEMA_NAME = "qvcsetest";

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        PostgresDatabaseManager.getInstance().initializeDatabase();
        QVCSEnterpriseServer.setDatabaseManager(PostgresDatabaseManager.getInstance());
        QVCSEnterpriseServer.getDatabaseManager().initializeDatabase();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-0"));
        testProjectId = DAOTestHelper.createTestProject(TEST_SCHEMA_NAME);
        testTrunkBranchId = DAOTestHelper.createTrunkBranch(testProjectId, TEST_SCHEMA_NAME);
        testTrunkRootDirectoryId = DAOTestHelper.createBranchRootDirectory(1, testTrunkBranchId, TEST_SCHEMA_NAME);
        testTrunkChildDirectoryId = DAOTestHelper.createBranchChildDirectory(2, testTrunkRootDirectoryId, testTrunkRootDirectoryId, "childDirectory", testTrunkBranchId, TEST_SCHEMA_NAME);
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        DirectoryDAOImpl directoryDAO = new DirectoryDAOImpl(TEST_SCHEMA_NAME);
        Directory result = directoryDAO.findByAppendedPath(testTrunkBranchId, "");
        List<Directory> directoryList = directoryDAO.findByBranchId(result.getBranchId());
        for (Directory directory : directoryList) {
            directoryDAO.delete(directory);
        }
        BranchDAOImpl branchDAO = new BranchDAOImpl(TEST_SCHEMA_NAME);
        List<Branch> originalList = branchDAO.findAll();
        for (Branch branch : originalList) {
            branchDAO.delete(branch);
        }
        DAOTestHelper.deleteTestProject(TEST_SCHEMA_NAME);
        QVCSEnterpriseServer.getDatabaseManager().shutdownDatabase();
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
        DirectoryDAOImpl instance = new DirectoryDAOImpl(TEST_SCHEMA_NAME);
        Directory result = instance.findById(testTrunkBranchId, testTrunkRootDirectoryId);
        assertNotNull("Did not find directory", result);
        assertEquals("Unexpected directory id", result.getDirectoryId().intValue(), testTrunkRootDirectoryId);
        assertNotNull("Null insert date", result.getInsertDate());
        assertNotNull("Null update date", result.getUpdateDate());
    }

    /**
     * Test of findByAppendedPath method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindByAppendedPath() {
        String appendedPath = "";
        DirectoryDAOImpl instance = new DirectoryDAOImpl(TEST_SCHEMA_NAME);
        Directory result = instance.findByAppendedPath(testTrunkBranchId, appendedPath);
        assertNotNull("Did not find directory", result);
        assertEquals("Unexpected directory id", result.getDirectoryId().intValue(), testTrunkRootDirectoryId);
    }

    /**
     * Test of findByBranchId method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindByBranchId() {
        DirectoryDAOImpl instance = new DirectoryDAOImpl(TEST_SCHEMA_NAME);
        List<Directory> result = instance.findByBranchId(testTrunkBranchId);
        assertTrue("Did not find any child directories", !result.isEmpty());
    }

    /**
     * Test of findChildDirectories method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindChildDirectories() {
        DirectoryDAOImpl instance = new DirectoryDAOImpl(TEST_SCHEMA_NAME);
        List<Directory> result = instance.findChildDirectories(testTrunkBranchId, testTrunkRootDirectoryId);
        assertTrue("Did not find any child directories", !result.isEmpty());
        assertEquals("Did not find expected child", result.get(0).getDirectoryId().intValue(), 2);
    }

    /**
     * Test of findChildDirectories method, of class DirectoryDAOImpl.
     */
    @Test
    public void testFindChildDirectoriesOnOrBeforeBranchDate() {
        DirectoryDAOImpl instance = new DirectoryDAOImpl(TEST_SCHEMA_NAME);
        Calendar now = Calendar.getInstance();
        List<Directory> result = instance.findChildDirectoriesOnOrBeforeBranchDate(testTrunkBranchId, testTrunkRootDirectoryId, new Date(now.getTimeInMillis()));
        assertTrue("Did not find any child directories", !result.isEmpty());
        assertEquals("Did not find expected child", result.get(0).getDirectoryId().intValue(), 2);
        now.add(Calendar.SECOND, -10);
        result = instance.findChildDirectoriesOnOrBeforeBranchDate(testTrunkBranchId, testTrunkRootDirectoryId, new Date(now.getTimeInMillis()));
        assertTrue("Found unexpected children", result.isEmpty());
    }

    /**
     * Test the update method
     *
     * @throws SQLException if there was a problem updating the record.
     */
    @Test
    public void testUpdate() throws SQLException {
        DirectoryDAOImpl instance = new DirectoryDAOImpl(TEST_SCHEMA_NAME);
        Directory directory = instance.findById(testTrunkBranchId, testTrunkChildDirectoryId);
        directory.setAppendedPath("aDifferentChildDirectory");
        instance.update(directory, false);
    }
}
