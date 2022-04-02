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
package com.qvcsos.server;

import com.qvcsos.CommonTestHelper;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class DatabaseManagerTest {

    /**
     * Create our logger object.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DatabaseManagerTest.class);

    public DatabaseManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        CommonTestHelper.getCommonTestHelper().acquireSyncObject();
        CommonTestHelper.getCommonTestHelper().resetTestDatabaseViaPsqlScript();
    }

    @AfterClass
    public static void tearDownClass() {
        CommonTestHelper.getCommonTestHelper().releaseSyncObject();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class DatabaseManager.
     */
    @Test
    public void testGetInstance() {
        LOGGER.info("getInstance");
        DatabaseManager result = DatabaseManager.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of setUsername method, of class DatabaseManager.
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    @Test
    public void testSetUsername() throws SQLException, ClassNotFoundException {
        LOGGER.info("setUsername");
        String uname = "qvcsos410test";
        DatabaseManager instance = DatabaseManager.getInstance();
        instance.setUsername(uname);
        String pword = "qvcsos410testPG$Admin";
        instance.setPassword(pword);
        String url = "jdbc:postgresql://localhost:5433/qvcsos410test";
        instance.setUrl(url);
        instance.initializeDatabase();
        Connection result = instance.getConnection();
        assertNotNull(result);
        instance.closeConnection();
        instance.shutdownDatabase();
    }

    /**
     * Test of getSchemaName method, of class DatabaseManager.
     */
    @Test
    public void testGetAndSetSchemaName() {
        LOGGER.info("getSchemaName");
        DatabaseManager instance = DatabaseManager.getInstance();
        String schemaName = "qvcsos410test";
        instance.setSchemaName(schemaName);
        String expResult = schemaName;
        String result = instance.getSchemaName();
        assertEquals(expResult, result);
    }

}
