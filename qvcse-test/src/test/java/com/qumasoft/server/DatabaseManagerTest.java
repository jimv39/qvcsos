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
//   $Header: DatabaseManagerTest.java Revision:1.3 Wednesday, March 21, 2012 10:31:03 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.server;

import com.qumasoft.TestHelper;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Database manager class.
 *
 * @author $Author: JimVoris $
 */
public class DatabaseManagerTest {

    private static final String DERBY_TEST_DIRECTORY = "/temp/qvcse/dbManagerUnitTest";

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.emptyDerbyTestDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().setDerbyHomeDirectory(DERBY_TEST_DIRECTORY);
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
     * Test of initializeDatabase method, of class DatabaseManager.
     *
     * @throws SQLException if there was a db problem.
     */
    @Test
    public void testInitializeDatabase() throws SQLException {
        System.out.println("initializeDatabase");
        DatabaseManager.getInstance().initializeDatabase();
    }

    /**
     * Test of getConnection method, of class DatabaseManager.
     *
     * @throws SQLException if there was a db problem.
     */
    @Test
    public void testGetConnection() throws SQLException {
        System.out.println("getConnection");
        Connection result = DatabaseManager.getInstance().getConnection();
        assertNotNull("Got unexpected null for db connection", result);
    }

    /**
     * Test of closeConnection method, of class DatabaseManager.
     *
     * @throws SQLException if there was a db problem.
     */
    @Test
    public void testCloseConnection() throws SQLException {
        System.out.println("closeConnection");
        DatabaseManager.getInstance().closeConnection();
    }
}
