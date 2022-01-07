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
package com.qvcsos.server.dataaccess.impl;

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.TestHelper;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.FileRevision;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
@Ignore
public class FileRevisionDAOImplTest {
    /**
     * Create our logger object.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FileRevisionDAOImplTest.class);

    private static DatabaseManager databaseManager;
    private static String schemaName;

    public FileRevisionDAOImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        TestHelper.resetTestDatabaseViaPsqlScript();
        TestHelper.createTestProjectViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();
        schemaName = databaseManager.getSchemaName();
    }

    @AfterClass
    public static void tearDownClass() {
        databaseManager.shutdownDatabase();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of findById method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testFindById() throws SQLException {
        Connection connection = databaseManager.getConnection();
        Integer id = 1;
        FileRevisionDAOImpl instance = new FileRevisionDAOImpl(schemaName);
        FileRevision result = instance.findById(id);
        assertEquals(id.intValue(), result.getId().intValue());
    }

    /**
     * Test of findFileRevisions method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testFindFileRevisions() {
        System.out.println("findFileRevisions");
        String branchesToSearch = "";
        Integer fileId = null;
        FileRevisionDAOImpl instance = null;
        List<FileRevision> expResult = null;
        List<FileRevision> result = instance.findFileRevisions(branchesToSearch, fileId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findNewestRevisionOnBranch method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testFindNewestRevisionOnBranch() {
        System.out.println("findNewestRevisionOnBranch");
        Integer branchId = null;
        Integer fileId = null;
        FileRevisionDAOImpl instance = null;
        FileRevision expResult = null;
        FileRevision result = instance.findNewestRevisionOnBranch(branchId, fileId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findNewestRevisionAllBranches method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testFindNewestRevisionAllBranches() {
        System.out.println("findNewestRevisionAllBranches");
        Integer fileId = null;
        FileRevisionDAOImpl instance = null;
        FileRevision expResult = null;
        FileRevision result = instance.findNewestRevisionAllBranches(fileId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findNewestBranchRevision method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testFindNewestBranchRevision() {
        System.out.println("findNewestBranchRevision");
        int branchId = 0;
        FileRevisionDAOImpl instance = null;
        FileRevision expResult = null;
        FileRevision result = instance.findNewestBranchRevision(branchId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findPromotionCandidates method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testFindPromotionCandidates() {
        System.out.println("findPromotionCandidates");
        Branch promoteFromBranch = null;
        Branch promoteToBranch = null;
        FileRevisionDAOImpl instance = null;
        List<FileRevision> expResult = null;
        List<FileRevision> result = instance.findPromotionCandidates(promoteFromBranch, promoteToBranch);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findNewestPromotedRevision method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testFindNewestPromotedRevision() {
        System.out.println("findNewestPromotedRevision");
        int promoteFromBranchId = 0;
        Integer fileId = null;
        FileRevisionDAOImpl instance = null;
        FileRevision expResult = null;
        FileRevision result = instance.findNewestPromotedRevision(promoteFromBranchId, fileId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findByBranchIdAndAncestorRevisionAndFileId method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testFindByBranchIdAndAncestorRevisionAndFileId() {
        System.out.println("findByBranchIdAndAncestorRevisionAndFileId");
        int promoteToBranchId = 0;
        Integer ancestorRevisionId = null;
        Integer fileId = null;
        FileRevisionDAOImpl instance = null;
        FileRevision expResult = null;
        FileRevision result = instance.findByBranchIdAndAncestorRevisionAndFileId(promoteToBranchId, ancestorRevisionId, fileId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of insert method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testInsert() throws Exception {
        FileRevision fileRevision = new FileRevision();
        fileRevision.setBranchId(1);
        fileRevision.setCommitId(null);
        FileRevisionDAOImpl instance = null;
        Integer expResult = null;
        Integer result = instance.insert(fileRevision);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateAncestorRevision method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testUpdateAncestorRevision() throws Exception {
        System.out.println("updateAncestorRevision");
        Integer id = null;
        Integer reverseDeltaRevisionId = null;
        byte[] reverseDeltaScript = null;
        FileRevisionDAOImpl instance = null;
        boolean expResult = false;
        boolean result = instance.updateAncestorRevision(id, reverseDeltaRevisionId, reverseDeltaScript);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of markPromoted method, of class FileRevisionDAOImpl.
     */
    @Test
    public void testMarkPromoted() throws Exception {
        System.out.println("markPromoted");
        Integer fileRevisionId = null;
        FileRevisionDAOImpl instance = null;
        boolean expResult = false;
        boolean result = instance.markPromoted(fileRevisionId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
