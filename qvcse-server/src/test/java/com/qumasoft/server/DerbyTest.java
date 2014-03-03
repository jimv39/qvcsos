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
package com.qumasoft.server;

import com.qumasoft.TestHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test derby.
 * @author Jim Voris
 */
public class DerbyTest {

    private static final String DERBY_TEST_DIRECTORY = "/tmp/qvcse/derbyTest";

    /*
     * ============================================ CREATE SCHEMA ===========================================================
     */
    private static final String CREATE_QVCSE_SCHEMA_SQL = "CREATE SCHEMA QVCSE";

    /*
     * =========================================== CREATE TABLES ============================================================
     */
    private static final String CREATE_BRANCH_TYPE_TABLE_SQL = "CREATE TABLE QVCSE.BRANCH_TYPE ("
            + "BRANCH_TYPE_ID INT NOT NULL CONSTRAINT BRANCH_TYPE_PK PRIMARY KEY,"
            + "BRANCH_TYPE_NAME VARCHAR(256) NOT NULL)";
    private static final String CREATE_PROJECT_TABLE_SQL = "CREATE TABLE QVCSE.PROJECT ("
            + "PROJECT_ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT PROJECT_PK PRIMARY KEY,"
            + "PROJECT_NAME VARCHAR(256) NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL)";
    private static final String CREATE_BRANCH_TABLE_SQL = "CREATE TABLE QVCSE.BRANCH ("
            + "BRANCH_ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT BRANCH_PK PRIMARY KEY,"
            + "PROJECT_ID INT NOT NULL,"
            + "BRANCH_NAME VARCHAR(256) NOT NULL,"
            + "BRANCH_TYPE_ID INT NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL)";
    private static final String CREATE_DIRECTORY_TABLE_SQL = "CREATE TABLE QVCSE.DIRECTORY ("
            + "DIRECTORY_ID INT NOT NULL,"
            + "ROOT_DIRECTORY_ID INT NOT NULL,"
            + "PARENT_DIRECTORY_ID INT,"
            + "BRANCH_ID INT NOT NULL,"
            + "APPENDED_PATH VARCHAR(2048) NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL,"
            + "UPDATE_DATE TIMESTAMP NOT NULL,"
            + "DELETED_FLAG BOOLEAN NOT NULL,"
            + "CONSTRAINT DIRECTORY_PK PRIMARY KEY (DIRECTORY_ID, BRANCH_ID))";
    private static final String CREATE_DIRECTORY_HISTORY_TABLE_SQL = "CREATE TABLE QVCSE.DIRECTORY_HISTORY ("
            + "ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT DIRECTORY_HISTORY_PK PRIMARY KEY,"
            + "DIRECTORY_ID INT NOT NULL,"
            + "ROOT_DIRECTORY_ID INT NOT NULL,"
            + "PARENT_DIRECTORY_ID INT,"
            + "BRANCH_ID INT NOT NULL,"
            + "APPENDED_PATH VARCHAR(2048) NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL,"
            + "UPDATE_DATE TIMESTAMP NOT NULL,"
            + "DELETED_FLAG BOOLEAN NOT NULL)";
    private static final String CREATE_FILE_TABLE_SQL = "CREATE TABLE QVCSE.FILE ("
            + "FILE_ID INT NOT NULL,"
            + "BRANCH_ID INT NOT NULL,"
            + "DIRECTORY_ID INT NOT NULL,"
            + "FILE_NAME VARCHAR(256) NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL,"
            + "UPDATE_DATE TIMESTAMP NOT NULL,"
            + "DELETED_FLAG BOOLEAN NOT NULL,"
            + "CONSTRAINT FILE_PK PRIMARY KEY (FILE_ID, BRANCH_ID))";
    private static final String CREATE_FILE_HISTORY_TABLE_SQL = "CREATE TABLE QVCSE.FILE_HISTORY ("
            + "ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT ID_PK PRIMARY KEY,"
            + "FILE_ID INT,"
            + "BRANCH_ID INT NOT NULL,"
            + "DIRECTORY_ID INT NOT NULL,"
            + "FILE_NAME VARCHAR(256) NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL,"
            + "UPDATE_DATE TIMESTAMP NOT NULL,"
            + "DELETED_FLAG BOOLEAN NOT NULL)";

    /*
     * ================================================= ADD CONSTRAINTS ====================================================
     */
    private static final String ALTER_BRANCH_TABLE = "ALTER TABLE QVCSE.BRANCH ADD CONSTRAINT "
            + "BRANCH_TYPE_FK FOREIGN KEY (BRANCH_TYPE_ID) REFERENCES QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID)";
    private static final String ALTER_DIRECTORY_TABLE = "ALTER TABLE QVCSE.DIRECTORY ADD CONSTRAINT "
            + "BRANCH_FK FOREIGN KEY (BRANCH_ID) REFERENCES QVCSE.BRANCH (BRANCH_ID)";
    private static final String ALTER_FILE_TABLE1 = "ALTER TABLE QVCSE.FILE ADD CONSTRAINT "
            + "BRANCH_FK2 FOREIGN KEY (BRANCH_ID) REFERENCES QVCSE.BRANCH (BRANCH_ID)";
    private static final String ALTER_FILE_TABLE2 = "ALTER TABLE QVCSE.FILE ADD CONSTRAINT "
            + "DIRECTORY_FK FOREIGN KEY (DIRECTORY_ID,BRANCH_ID) REFERENCES QVCSE.DIRECTORY (DIRECTORY_ID,BRANCH_ID)";

    /*
     * ================================================== CREATE TRIGGERS ====================================================
     */
    private static final String DIRECTORY_TRIGGER = "CREATE TRIGGER QVCSE.DIRECTORY_TRIGGER AFTER UPDATE ON QVCSE.DIRECTORY REFERENCING OLD AS OLDDIR "
            + "FOR EACH ROW INSERT INTO QVCSE.DIRECTORY_HISTORY(DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, BRANCH_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG) "
            + "VALUES (OLDDIR.DIRECTORY_ID, OLDDIR.ROOT_DIRECTORY_ID, OLDDIR.PARENT_DIRECTORY_ID, OLDDIR.BRANCH_ID, OLDDIR.APPENDED_PATH, OLDDIR.INSERT_DATE, OLDDIR.UPDATE_DATE, OLDDIR.DELETED_FLAG)";
    private static final String FILE_TRIGGER = "CREATE TRIGGER QVCSE.FILE_TRIGGER AFTER UPDATE ON QVCSE.FILE REFERENCING OLD AS OLDFILE "
            + "FOR EACH ROW INSERT INTO QVCSE.FILE_HISTORY(FILE_ID, BRANCH_ID, DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG) "
            + "VALUES (OLDFILE.FILE_ID, OLDFILE.BRANCH_ID, OLDFILE.DIRECTORY_ID, OLDFILE.FILE_NAME, "
            + "OLDFILE.INSERT_DATE, OLDFILE.UPDATE_DATE, OLDFILE.DELETED_FLAG)";
    /*
     * ================================================== INSERT LOOKUP DATA =================================================
     */
    private static final String INSERT_BRANCH_TYPE_DATA1 =
            "INSERT INTO QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID, BRANCH_TYPE_NAME) VALUES (1, 'Trunk')";
    private static final String INSERT_BRANCH_TYPE_DATA2 =
            "INSERT INTO QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID, BRANCH_TYPE_NAME) VALUES (2, 'Read Only Date Based Branch')";
    private static final String INSERT_BRANCH_TYPE_DATA3 =
            "INSERT INTO QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID, BRANCH_TYPE_NAME) VALUES (3, 'Feature Branch')";
    private static final String INSERT_BRANCH_TYPE_DATA4 =
            "INSERT INTO QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID, BRANCH_TYPE_NAME) VALUES (4, 'Release Branch')";
    /*
     * ================================================== INSERT TEST DATA ==================================================
     */
    private static final String INSERT_PROJECT1 =
            "INSERT INTO QVCSE.PROJECT (PROJECT_NAME, INSERT_DATE) VALUES ('Test Project', CURRENT_TIMESTAMP)";
    private static final String INSERT_BRANCH =
            "INSERT INTO QVCSE.BRANCH (BRANCH_NAME, BRANCH_TYPE_ID, PROJECT_ID, INSERT_DATE) VALUES ('Trunk', 1, 1, CURRENT_TIMESTAMP)";
    private static final String INSERT_DIRECTORY =
            "INSERT INTO QVCSE.DIRECTORY (DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, BRANCH_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG) "
            + "VALUES (1, 1, NULL, 1, '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)";
    private static final String INSERT_FILE =
            "INSERT INTO QVCSE.FILE (FILE_ID, BRANCH_ID, DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG) VALUES (1, 1, 1, 'Test File', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)";
    /*
     * ============================================== UPDATE TEST DATA ====================================================
     */
    private static final String UPDATE_DIRECTORY =
            "UPDATE QVCSE.DIRECTORY SET APPENDED_PATH = 'Moved', UPDATE_DATE = CURRENT_TIMESTAMP WHERE DIRECTORY_ID = 1";
    private static final String UPDATE_FILE =
            "UPDATE QVCSE.FILE SET FILE_NAME = 'Renamed Test File', UPDATE_DATE = CURRENT_TIMESTAMP WHERE FILE_ID = 1";

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
     * Test creating our schema.
     */
    @Test
    public void testCreateSchema() {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:derby:testdb");
            statement = connection.createStatement();

            statement.execute(CREATE_QVCSE_SCHEMA_SQL);
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

    /**
     * Test creating our tables.
     */
    @Test
    public void testCreateTables() {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:derby:testdb");
            statement = connection.createStatement();

            statement.execute(CREATE_BRANCH_TYPE_TABLE_SQL);
            statement.execute(CREATE_PROJECT_TABLE_SQL);
            statement.execute(CREATE_BRANCH_TABLE_SQL);
            statement.execute(CREATE_FILE_TABLE_SQL);
            statement.execute(CREATE_FILE_HISTORY_TABLE_SQL);
            statement.execute(CREATE_DIRECTORY_TABLE_SQL);
            statement.execute(CREATE_DIRECTORY_HISTORY_TABLE_SQL);
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

    /**
     * Test adding constraints.
     */
    @Test
    public void testAddConstraints() {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:derby:testdb");
            statement = connection.createStatement();

            statement.execute(ALTER_BRANCH_TABLE);
            statement.execute(ALTER_DIRECTORY_TABLE);
            statement.execute(ALTER_FILE_TABLE1);
            statement.execute(ALTER_FILE_TABLE2);
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

    /**
     * Test defining triggers.
     */
    @Test
    public void testAddTriggers() {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:derby:testdb");
            statement = connection.createStatement();

            statement.execute(DIRECTORY_TRIGGER);
            statement.execute(FILE_TRIGGER);
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

    /**
     * Test inserting branch type data, and branch data.
     */
    @Test
    public void testInsertData() {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:derby:testdb");
            statement = connection.createStatement();

            // Create branch types
            statement.execute(INSERT_BRANCH_TYPE_DATA1);
            statement.execute(INSERT_BRANCH_TYPE_DATA2);
            statement.execute(INSERT_BRANCH_TYPE_DATA3);
            statement.execute(INSERT_BRANCH_TYPE_DATA4);

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

    /**
     * Test inserting branch type data, and branch data.
     */
    @Test
    public void testInsertTestData() {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:derby:testdb");
            statement = connection.createStatement();

            // Some test data.
            statement.execute(INSERT_PROJECT1);
            statement.execute(INSERT_BRANCH);
            statement.execute(INSERT_DIRECTORY);
            statement.execute(INSERT_FILE);
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

    /**
     * Test inserting branch type data, and branch data.
     */
    @Test
    public void testUpdateTestData() {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:derby:testdb");
            statement = connection.createStatement();

            // Some test data.
            statement.execute(UPDATE_DIRECTORY);
            statement.execute(UPDATE_FILE);
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
