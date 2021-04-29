/*   Copyright 2004-2019 Jim Voris
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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class that handles database access.
 *
 * @author Jim Voris
 */
public final class DatabaseManager {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    /**
     * A date based branch.
     */
    public static final int DATE_BASED_BRANCH_TYPE = 2;
    /**
     * A feature branch.
     */
    public static final int FEATURE_BRANCH_TYPE = 3;
    /**
     * An opaque branch.
     */
    public static final int OPAQUE_BRANCH_TYPE = 4;
    private static final int EXPECTED_SQL_ERROR_CODE = 50_000;

    /**
     * Our singleton databaseManager instance
     */
    private static final DatabaseManager DATABASE_MANAGER = new DatabaseManager();
    /**
     * The control connection to the database
     */
    private Connection controlConnection = null;
    /**
     * Derby home directory
     */
    private String derbyHomeDirectory;
    /**
     * Thread local storage for database connections
     */
    private final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();
    /**
     * Flag we use to indicate whether we are initialized
     */
    private boolean initializedFlag;
    private static final String DATABASE_URL = "jdbc:derby:qvcsedb";

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
            + "CONSTRAINT DIRECTORY_PK PRIMARY KEY (DIRECTORY_ID, BRANCH_ID, DELETED_FLAG))";
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
            + "CONSTRAINT FILE_PK PRIMARY KEY (FILE_ID, BRANCH_ID, DELETED_FLAG))";
    private static final String CREATE_FILE_HISTORY_TABLE_SQL = "CREATE TABLE QVCSE.FILE_HISTORY ("
            + "ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT ID_PK PRIMARY KEY,"
            + "FILE_ID INT,"
            + "BRANCH_ID INT NOT NULL,"
            + "DIRECTORY_ID INT NOT NULL,"
            + "FILE_NAME VARCHAR(256) NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL,"
            + "UPDATE_DATE TIMESTAMP NOT NULL,"
            + "DELETED_FLAG BOOLEAN NOT NULL)";
    private static final String CREATE_REVISION_TABLE_SQL = "CREATE TABLE QVCSE.REVISION ("
            + "ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT REVISION_PK PRIMARY KEY,"
            + "BRANCH_ID INT NOT NULL,"
            + "FILE_ID INT NOT NULL,"
            + "REVISION_STRING VARCHAR(256) NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL)";
    private static final String CREATE_PROMOTION_CANDIDATE_TABLE_SQL = "CREATE TABLE QVCSE.PROMOTION_CANDIDATE ("
            + "FILE_ID INT NOT NULL,"
            + "BRANCH_ID INT NOT NULL,"
            + "INSERT_DATE TIMESTAMP NOT NULL,"
            + "CONSTRAINT PROMOTION_PK PRIMARY KEY (FILE_ID, BRANCH_ID))";
    private static final String CREATE_COMMIT_HISTORY_SQL = "CREATE TABLE QVCSE.COMMIT_HISTORY ("
            + "ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT COMMIT_HISTORY_PK PRIMARY KEY,"
            + "COMMIT_DATE TIMESTAMP NOT NULL,"
            + "COMMIT_MESSAGE VARCHAR(2048) NOT NULL)";

    /*
     * ================================================= ADD CONSTRAINTS ====================================================
     */
    private static final String ALTER_PROJECT_TABLE = "ALTER TABLE QVCSE.PROJECT ADD CONSTRAINT "
            + "PROJECT_NAME_UNIQUE UNIQUE (PROJECT_NAME)";
    private static final String ALTER_BRANCH_TABLE1 = "ALTER TABLE QVCSE.BRANCH ADD CONSTRAINT "
            + "BRANCH_TYPE_FK FOREIGN KEY (BRANCH_TYPE_ID) REFERENCES QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID)";
    private static final String ALTER_BRANCH_TABLE2 = "ALTER TABLE QVCSE.BRANCH ADD CONSTRAINT "
            + "PROJECT_FK FOREIGN KEY (PROJECT_ID) REFERENCES QVCSE.PROJECT (PROJECT_ID)";
    private static final String ALTER_BRANCH_TABLE3 = "CREATE UNIQUE INDEX BRANCH_IDX ON QVCSE.BRANCH(PROJECT_ID, BRANCH_NAME)";
    private static final String ALTER_DIRECTORY_TABLE = "ALTER TABLE QVCSE.DIRECTORY ADD CONSTRAINT "
            + "BRANCH_FK FOREIGN KEY (BRANCH_ID) REFERENCES QVCSE.BRANCH (BRANCH_ID)";
    private static final String ALTER_DIRECTORY_TABLE2 = "CREATE INDEX DIRECTORY_IDX ON QVCSE.DIRECTORY(DIRECTORY_ID)";
    private static final String ALTER_FILE_TABLE1 = "ALTER TABLE QVCSE.FILE ADD CONSTRAINT "
            + "BRANCH_FK2 FOREIGN KEY (BRANCH_ID) REFERENCES QVCSE.BRANCH (BRANCH_ID)";

    /*
     * ================================================== CREATE TRIGGERS ====================================================
     */
    private static final String DIRECTORY_TRIGGER = "CREATE TRIGGER QVCSE.DIRECTORY_TRIGGER AFTER UPDATE ON QVCSE.DIRECTORY REFERENCING OLD AS OLDDIR "
            + "FOR EACH ROW INSERT INTO QVCSE.DIRECTORY_HISTORY(DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, BRANCH_ID, APPENDED_PATH, INSERT_DATE, "
            + "UPDATE_DATE, DELETED_FLAG) "
            + "VALUES (OLDDIR.DIRECTORY_ID, OLDDIR.ROOT_DIRECTORY_ID, OLDDIR.PARENT_DIRECTORY_ID, OLDDIR.BRANCH_ID, OLDDIR.APPENDED_PATH, OLDDIR.INSERT_DATE, "
            + "OLDDIR.UPDATE_DATE, OLDDIR.DELETED_FLAG)";
    private static final String FILE_TRIGGER = "CREATE TRIGGER QVCSE.FILE_TRIGGER AFTER UPDATE ON QVCSE.FILE REFERENCING OLD AS OLDFILE "
            + "FOR EACH ROW INSERT INTO QVCSE.FILE_HISTORY(FILE_ID, BRANCH_ID, DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG) "
            + "VALUES (OLDFILE.FILE_ID, OLDFILE.BRANCH_ID, OLDFILE.DIRECTORY_ID, OLDFILE.FILE_NAME, "
            + "OLDFILE.INSERT_DATE, OLDFILE.UPDATE_DATE, OLDFILE.DELETED_FLAG)";

    /*
     * ================================================== INSERT DATA ====================================================
     */
    private static final String INSERT_BRANCH_TYPE_DATA1
            = "INSERT INTO QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID, BRANCH_TYPE_NAME) VALUES (1, 'Trunk')";
    private static final String INSERT_BRANCH_TYPE_DATA2
            = "INSERT INTO QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID, BRANCH_TYPE_NAME) VALUES (2, 'Read Only Date Based Branch')";
    private static final String INSERT_BRANCH_TYPE_DATA3
            = "INSERT INTO QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID, BRANCH_TYPE_NAME) VALUES (3, 'Feature Branch')";
    private static final String INSERT_BRANCH_TYPE_DATA4
            = "INSERT INTO QVCSE.BRANCH_TYPE (BRANCH_TYPE_ID, BRANCH_TYPE_NAME) VALUES (4, 'Release Branch')";

    /**
     * Private constructor, so no one else can make a DatabaseManager object.
     */
    private DatabaseManager() {
    }

    /**
     * Get the one and only database manager.
     *
     * @return the singleton database manager.
     */
    public static DatabaseManager getInstance() {
        return DATABASE_MANAGER;
    }

    /**
     * This method initializes the database.If the database does not exist, create it. If it does already exist, then connect to it, and
     * hold on to the connection as the 'control' connection. We use the connection we acquire here to shut down the database. The
     * connection acquired here is private to this class, and is used solely as the 'control' connection to the database.
     *
     * @throws java.sql.SQLException if we cannot open the database.
     * @throws java.lang.ClassNotFoundException if we can't load the derby embedded driver.
     */
    public synchronized void initializeDatabase() throws SQLException, ClassNotFoundException {
        if (!isInitializedFlag()) {
            System.getProperties().setProperty("derby.system.home", getDerbyHomeDirectory());
            System.getProperties().setProperty("derby.language.logQueryPlan", "true");
            System.getProperties().setProperty("derby.database.sqlAuthorization", "false");
            System.getProperties().setProperty("derby.infolog.append", "true");
            System.getProperties().setProperty("derby.language.logStatementText", "true");
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            if (databaseAlreadyExists()) {
                controlConnection = DriverManager.getConnection(DATABASE_URL);
                controlConnection.setAutoCommit(true);
            } else {
                controlConnection = DriverManager.getConnection(DATABASE_URL + ";create=true");
                controlConnection.setAutoCommit(true);
                createDbSchema(controlConnection);
            }
            setInitializedFlag(true);
        }
    }

    /**
     * This method shuts down the database.
     */
    public synchronized void shutdownDatabase() {
        if (controlConnection != null) {
            try {
                controlConnection.close();
                controlConnection = null;
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (SQLException e) {
                if (e.getErrorCode() != EXPECTED_SQL_ERROR_CODE) {
                    LOGGER.info(e.getLocalizedMessage(), e);
                }
            }
        }
        setInitializedFlag(false);
    }

    /**
     * Get a database connection.
     *
     * @return a database connection for the calling Thread.
     * @throws SQLException if we cannot get a db connection.
     */
    public synchronized Connection getConnection() throws SQLException {
        Connection connection = null;
        if (isInitializedFlag()) {
            connection = threadLocalConnection.get();
            if (connection == null) {
                connection = DriverManager.getConnection(DATABASE_URL);
                connection.setAutoCommit(true);
                threadLocalConnection.set(connection);
                LOGGER.info("Thread [{}]: got database connection.", Thread.currentThread().getName());
            } else {
                LOGGER.trace("Thread [{}]: reuse thread's database connection.", Thread.currentThread().getName());
            }
        } else {
            // This is a RuntimeException, so we don't have to declare it in our signature.
            throw new IllegalStateException("DatabaseManager has not been initialized!");
        }
        return connection;
    }

    /**
     * Close the connection for the calling Thread.
     *
     * @throws SQLException if we cannot close this thread's db connection.
     */
    public void closeConnection() throws SQLException {
        Connection thisThreadsDbConnection = threadLocalConnection.get();
        if (thisThreadsDbConnection != null) {
            thisThreadsDbConnection.close();
            threadLocalConnection.set(null);
            LOGGER.info("Thread [{}]: closed database connection.", Thread.currentThread().getName());
        }
    }

    /**
     * Get the derby home directory.
     *
     * @return the derbyHomeDirectory
     */
    public String getDerbyHomeDirectory() {
        return derbyHomeDirectory;
    }

    /**
     * Set the derby home directory.
     *
     * @param derbyDirectory the derbyHomeDirectory to set
     */
    public void setDerbyHomeDirectory(String derbyDirectory) {
        this.derbyHomeDirectory = derbyDirectory;
    }

    /**
     * Check to see if the database has already been created.
     *
     * @return true if the database already exists; false if it doesn't exist yet.
     */
    private boolean databaseAlreadyExists() {
        boolean existsFlag = false;
        String dbDirectoryName = getDerbyHomeDirectory() + File.separator + "qvcsedb";
        File dbDirectory = new File(dbDirectoryName);
        if (dbDirectory.exists() && dbDirectory.isDirectory()) {
            boolean servicePropertiesFoundFlag = false;
            boolean logDirectoryFoundFlag = false;
            boolean seg0DirectoryFoundFlag = false;
            File[] fileList = dbDirectory.listFiles();
            for (File file : fileList) {
                if (file.getName().equals("service.properties")) {
                    servicePropertiesFoundFlag = true;
                }
                if (file.getName().equals("log")) {
                    logDirectoryFoundFlag = true;
                }
                if (file.getName().equals("seg0")) {
                    seg0DirectoryFoundFlag = true;
                }
            }
            if (servicePropertiesFoundFlag && logDirectoryFoundFlag && seg0DirectoryFoundFlag) {
                existsFlag = true;
            }
        }
        return existsFlag;
    }

    /**
     * Get the initializedFlag value;
     *
     * @return the initializedFlag
     */
    private boolean isInitializedFlag() {
        return initializedFlag;
    }

    /**
     * Set the initializedFlag value.
     *
     * @param flag the initializedFlag to set
     */
    private void setInitializedFlag(boolean flag) {
        this.initializedFlag = flag;
    }

    /**
     * Create the database schema.
     *
     * @param connection the db connection to use to create the schema.
     * @throws SQLException if there was a problem creating the schema.
     */
    private void createDbSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_QVCSE_SCHEMA_SQL);
            statement.execute(CREATE_BRANCH_TYPE_TABLE_SQL);
            statement.execute(CREATE_PROJECT_TABLE_SQL);
            statement.execute(CREATE_BRANCH_TABLE_SQL);
            statement.execute(CREATE_FILE_TABLE_SQL);
            statement.execute(CREATE_FILE_HISTORY_TABLE_SQL);
            statement.execute(CREATE_DIRECTORY_TABLE_SQL);
            statement.execute(CREATE_DIRECTORY_HISTORY_TABLE_SQL);
            statement.execute(CREATE_REVISION_TABLE_SQL);
            statement.execute(CREATE_PROMOTION_CANDIDATE_TABLE_SQL);
            statement.execute(CREATE_COMMIT_HISTORY_SQL);

            statement.execute(ALTER_PROJECT_TABLE);
            statement.execute(ALTER_BRANCH_TABLE1);
            statement.execute(ALTER_BRANCH_TABLE2);
            statement.execute(ALTER_BRANCH_TABLE3);
            statement.execute(ALTER_DIRECTORY_TABLE);
            statement.execute(ALTER_DIRECTORY_TABLE2);
            statement.execute(ALTER_FILE_TABLE1);

            statement.execute(DIRECTORY_TRIGGER);
            statement.execute(FILE_TRIGGER);

            // Create branch types
            statement.execute(INSERT_BRANCH_TYPE_DATA1);
            statement.execute(INSERT_BRANCH_TYPE_DATA2);
            statement.execute(INSERT_BRANCH_TYPE_DATA3);
            statement.execute(INSERT_BRANCH_TYPE_DATA4);
        }
    }
}
