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
package com.qumasoft.server;

import com.qumasoft.TestHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test derby trigger creation.
 * @author Jim Voris
 */
public class DerbyTriggerSequenceTest {

    private static final String DERBY_TEST_DIRECTORY = "/tmp/qvcse/derbyTriggerTest";

    /*
     * ============================================ CREATE SCHEMA ===========================================================
     */
    private static final String CREATE_SCHEMA = "CREATE SCHEMA TEST";

    /*
     * =========================================== CREATE SEQUENCE =========================================================
     */
    private static final String CREATE_TEST_SEQUENCE = "CREATE SEQUENCE TEST.TEST_SEQ_ID AS INT START WITH 1 INCREMENT BY 1";

    /*
     * =========================================== CREATE TABLES ============================================================
     */
    private static final String CREATE_TEST_TABLE_SQL = "CREATE TABLE TEST.TEST ("
            + "TEST_ID INT NOT NULL,"
            + "TEST1_ID INT NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL,"
            + "UPDATE_DATE TIMESTAMP,"
            + "CONSTRAINT TEST_PK PRIMARY KEY (TEST_ID, TEST1_ID))";
    private static final String CREATE_TEST_HISTORY_TABLE_SQL = "CREATE TABLE TEST.TEST_HISTORY ("
            + "ID BIGINT GENERATED ALWAYS AS IDENTITY CONSTRAINT TEST_HISTORY_PK PRIMARY KEY,"
            + "TEST_ID INT NOT NULL,"
            + "TEST1_ID INT NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL,"
            + "UPDATE_DATE TIMESTAMP)";

    /*
     * ================================================== CREATE TRIGGER ====================================================
     */
    private static final String TEST_TRIGGER =
            "CREATE TRIGGER TEST.TEST_TRIGGER1 AFTER UPDATE ON TEST.TEST REFERENCING OLD AS OLD "
            + "FOR EACH ROW INSERT INTO TEST.TEST_HISTORY(TEST_ID, TEST1_ID, INSERT_DATE, UPDATE_DATE) "
            + "VALUES (OLD.TEST_ID, OLD.TEST1_ID, OLD.INSERT_DATE, OLD.UPDATE_DATE)";
    /*
     * ================================================== INSERT DATA ====================================================
     */
    private static final String INSERT_TEST =
            "INSERT INTO TEST.TEST (TEST_ID, TEST1_ID, INSERT_DATE) VALUES (1, 1, CURRENT_TIMESTAMP)";
    /*
     * ================================================== UPDATE DATA ====================================================
     */
    private static final String UPDATE_TEST1 =
            "UPDATE TEST.TEST SET TEST1_ID = 2, UPDATE_DATE = CURRENT_TIMESTAMP WHERE TEST_ID = 1";
    private static final String UPDATE_TEST2 =
            "UPDATE TEST.TEST SET TEST1_ID = 3, UPDATE_DATE = CURRENT_TIMESTAMP WHERE TEST_ID = 1";

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.emptyDerbyTestDirectory(DERBY_TEST_DIRECTORY);
        System.getProperties().setProperty("derby.system.home", DERBY_TEST_DIRECTORY);
        System.getProperties().setProperty("derby.language.logQueryPlan", "true");
        System.getProperties().setProperty("derby.infolog.append", "true");
        System.getProperties().setProperty("derby.language.logStatementText", "true");
        System.getProperties().setProperty("derby.database.sqlAuthorization", "false");
        DriverManager.getConnection("jdbc:derby:testdb;create=true");
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            assertTrue("Not expected sql exception", e.getErrorCode() == 50000);
        }
    }

    /**
     * Test trigger creation.
     */
    @Test
    public void testTriggerCreation() {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:derby:testdb");
            statement = connection.createStatement();

            statement.execute(CREATE_SCHEMA);
            statement.execute(CREATE_TEST_SEQUENCE);
            statement.execute(CREATE_TEST_TABLE_SQL);
            statement.execute(CREATE_TEST_HISTORY_TABLE_SQL);
            statement.execute(TEST_TRIGGER);
            statement.execute(INSERT_TEST);
            statement.execute(UPDATE_TEST1);
            statement.execute(UPDATE_TEST2);
        } catch (SQLException e) {
            fail("Unexpected SQL Exception: " + e.getLocalizedMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                fail("Caught unexpected sql exception" + e.getLocalizedMessage());
            }
        }
    }
}
