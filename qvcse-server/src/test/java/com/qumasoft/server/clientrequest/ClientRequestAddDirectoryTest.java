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
package com.qumasoft.server.clientrequest;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.BogusResponseObject;
import com.qvcsos.server.DatabaseManager;
import java.sql.SQLException;
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
public class ClientRequestAddDirectoryTest {
    /**
     * Create our logger object.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClientRequestAddDirectoryTest.class);
    private static DatabaseManager databaseManager;

    public ClientRequestAddDirectoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws SQLException {
        TestHelper.resetTestDatabaseViaPsqlScript();
        TestHelper.resetQvcsosTestDatabaseViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();
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

    /**
     * Test of buildAddedDirectoryLocationId method, of class ClientRequestAddDirectory.
     */
    @Test
    public void testBuildAddedDirectoryLocationId() {
        LOGGER.info("testBuildAddedDirectoryLocationId");
        int projectId = 1;
        int branchId = 1;
        String appendedPath = "foo/bar/test/these/segments/dude";
        ClientRequestAddDirectory instance = new ClientRequestAddDirectory(null);

        // This should not return a null.
        instance.getSourceControlBehaviorManager().setUserAndResponse("ScriptedTestUser", new BogusResponseObject());
        Integer result = instance.buildAddedDirectoryLocationId("ScriptedTestUser", projectId, branchId, appendedPath);
        LOGGER.info("Location id for created directory: [{}]", result);
        assertNotNull("Unexpected null result.", result);
    }

}
