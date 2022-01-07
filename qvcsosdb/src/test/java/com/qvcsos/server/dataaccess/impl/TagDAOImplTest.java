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

import com.qumasoft.qvcslib.BogusResponseObject;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.TestHelper;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.datamodel.Tag;
import static java.lang.Boolean.TRUE;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class TagDAOImplTest {
    /**
     * Create our logger object.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TagDAOImplTest.class);

    private static DatabaseManager databaseManager;

    public TagDAOImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        TestHelper.resetTestDatabaseViaPsqlScript();
        TestHelper.createTestProjectViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        instance.setResponse(new BogusResponseObject());
        instance.setUserId(2);
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

//    /**
//     * Test of findById method, of class TagDAOImpl.
//     */
//    @Test
//    public void testFindById() {
//        System.out.println("findById");
//        Integer tagId = null;
//        TagDAOImpl instance = null;
//        Tag expResult = null;
//        Tag result = instance.findById(tagId);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findAll method, of class TagDAOImpl.
//     */
//    @Test
//    public void testFindAll() {
//        System.out.println("findAll");
//        TagDAOImpl instance = null;
//        List<Tag> expResult = null;
//        List<Tag> result = instance.findAll();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findByBranchId method, of class TagDAOImpl.
//     */
//    @Test
//    public void testFindByBranchId() {
//        System.out.println("findByBranchId");
//        Integer branchId = null;
//        TagDAOImpl instance = null;
//        List<Tag> expResult = null;
//        List<Tag> result = instance.findByBranchId(branchId);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findByBranchIdAndTagText method, of class TagDAOImpl.
//     */
//    @Test
//    public void testFindByBranchIdAndTagText() {
//        System.out.println("findByBranchIdAndTagText");
//        Integer branchId = null;
//        String tagText = "";
//        TagDAOImpl instance = null;
//        Tag expResult = null;
//        Tag result = instance.findByBranchIdAndTagText(branchId, tagText);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of insert method, of class TagDAOImpl.
     */
    @Test
    public void testInsert() throws Exception {
        LOGGER.info("insert");
        SourceControlBehaviorManager scbm = SourceControlBehaviorManager.getInstance();
        AtomicBoolean performCommit = new AtomicBoolean(TRUE);
        Integer commitId = scbm.getCommitId(null, "Testing tag insert", performCommit);
        Tag tag = new Tag();
        tag.setBranchId(1);
        tag.setCommitId(commitId);
        tag.setTagText("Test Tag Text 1");
        tag.setDescription(("Tag description for grins"));
        TagDAO instance = new TagDAOImpl(databaseManager.getSchemaName());
        Integer result = instance.insert(tag);
        databaseManager.getConnection().commit();
        assertNotNull("", result);
    }

}
