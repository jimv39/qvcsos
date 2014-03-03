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
//     $Date: Wednesday, March 21, 2012 10:31:03 PM $
//   $Header: BranchDAOImplTest.java Revision:1.3 Wednesday, March 21, 2012 10:31:03 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.server.DatabaseManager;
import com.qumasoft.server.datamodel.Branch;
import java.sql.SQLException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Branch DAO.
 *
 * @author $Author: JimVoris $
 */
public class BranchDAOImplTest {

    private static final String DERBY_TEST_DIRECTORY = "/temp/qvcse/branchDAOImplTest";
    private static int testProjectId = -1;

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.emptyDerbyTestDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().setDerbyHomeDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().initializeDatabase();
        testProjectId = DAOTestHelper.createTestProject();

        Branch branch = new Branch();
        branch.setBranchName(QVCSConstants.QVCS_TRUNK_VIEW);
        branch.setProjectId(testProjectId);
        branch.setBranchTypeId(1);
        BranchDAOImpl instance = new BranchDAOImpl();
        instance.insert(branch);
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
     * Test of findById method, of class BranchDAOImpl.
     */
    @Test
    public void testFindAll() {
        BranchDAOImpl instance = new BranchDAOImpl();
        String expResult = QVCSConstants.QVCS_TRUNK_VIEW;
        List<Branch> result = instance.findAll();
        Branch firstBranch = result.get(0);
        assertTrue("Empty result list", !result.isEmpty());
        assertEquals("BranchDAOImpl findAll failed", expResult, firstBranch.getBranchName());
    }

    /**
     * Test of findById method, of class BranchDAOImpl.
     */
    @Test
    public void testFindById() {
        BranchDAOImpl instance = new BranchDAOImpl();
        String expResult = QVCSConstants.QVCS_TRUNK_VIEW;
        Branch result = instance.findById(1);
        assertEquals("BranchDAOImpl findById failed", expResult, result.getBranchName());
    }

    /**
     * Test of findByProjectIdAndBranchName method, of class BranchDAOImpl.
     */
    @Test
    public void testFindByProjectIdAndBranchName() {
        BranchDAOImpl instance = new BranchDAOImpl();
        String expResult = QVCSConstants.QVCS_TRUNK_VIEW;
        Branch result = instance.findByProjectIdAndBranchName(testProjectId, QVCSConstants.QVCS_TRUNK_VIEW);
        assertEquals("BranchDAOImpl findByProjectIdAndBranchName failed", expResult, result.getBranchName());
    }

    /**
     * Test of insert method, of class BranchDAOImpl.
     *
     * @throws SQLException thrown if there is a problem.
     */
    @Test
    public void testInsert() throws SQLException {
        Branch branch = new Branch();
        branch.setBranchName("Test Branch");
        branch.setProjectId(testProjectId);
        branch.setBranchTypeId(2);
        BranchDAOImpl instance = new BranchDAOImpl();
        instance.insert(branch);
    }

    /**
     * Test of delete method, of class BranchDAOImpl.
     *
     * @throws SQLException thrown if there is a problem.
     */
    @Test
    public void testDelete() throws SQLException {
        BranchDAOImpl instance = new BranchDAOImpl();
        List<Branch> originalList = instance.findAll();
        int originalSize = originalList.size();
        Branch branch = instance.findById(2);
        if (branch != null) {
            instance.delete(branch);
        }
        List<Branch> afterDeleteList = instance.findAll();
        int afterDeleteSize = afterDeleteList.size();
        assertTrue("Failed to delete branch.", originalSize == afterDeleteSize + 1);
    }
}
