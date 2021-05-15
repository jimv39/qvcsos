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
package com.qumasoft.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A database manager used for connections to a Postgres database.
 *
 * This is a work-in-progress.
 *
 * @author Jim Voris
 */
public final class PostgresDatabaseManager implements DatabaseManagerInterface {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDatabaseManager.class);

    /**
     * Our singleton databaseManager instance
     */
    private static final PostgresDatabaseManager POSTGRES_DATABASE_MANAGER = new PostgresDatabaseManager();
    /**
     * The control connection to the database
     */
    private Connection controlConnection = null;
    /**
     * Thread local storage for database connections
     */
    private final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();
    /**
     * Flag we use to indicate whether we are initialized
     */
    private boolean initializedFlag;
    private static String postgresDatabaseUrl = "jdbc:postgresql://localhost:5433/qvcse";
    private static String username = "qvcse";
    private static String password = "qvcsePG$Admin";
    private static String postgresSchemaName = "qvcse";

    /**
     * Private constructor, so no one else can make a PostgresDatabaseManager
     * object.
     */
    private PostgresDatabaseManager() {
    }

    /**
     * Get the one and only database manager.
     *
     * @return the singleton database manager.
     */
    public static PostgresDatabaseManager getInstance() {
        return POSTGRES_DATABASE_MANAGER;
    }

    /**
     * Set the username. We use this for unit tests only.
     * @param uname the user name for the test database.
     */
    public static void setUsername(String uname) {
        username = uname;
    }

    /**
     * Set the password. We use this for unit tests only.
     * @param pword the password for the test database.
     */
    public static void setPassword(String pword) {
        password = pword;
    }

    /**
     * Set the db URL. We use this for unit tests only.
     * @param url the URL for the test database.
     */
    public static void setUrl(String url) {
        postgresDatabaseUrl = url;
    }

    @Override
    public String getSchemaName() {
        return postgresSchemaName;
    }

    /**
     * Set the schema name. We use this for testing.
     * @param schemaName the name of the (test) schema.
     */
    public static void setSchemaName(String schemaName) {
        postgresSchemaName = schemaName;
    }

    @Override
    public synchronized void closeConnection() throws SQLException {
        Connection thisThreadsDbConnection = threadLocalConnection.get();
        if (thisThreadsDbConnection != null) {
            thisThreadsDbConnection.close();
            threadLocalConnection.set(null);
            LOGGER.info("Thread [{}]: closed database connection.", Thread.currentThread().getName());
        }
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        Connection connection = null;
        if (isInitializedFlag()) {
            connection = threadLocalConnection.get();
            if (connection == null) {
                connection = DriverManager.getConnection(postgresDatabaseUrl, username, password);
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

    @Override
    public synchronized void initializeDatabase() throws SQLException, ClassNotFoundException {
        try {
            controlConnection = DriverManager.getConnection(postgresDatabaseUrl, username, password);
            setInitializedFlag(true);
            LOGGER.info("Connected to Postgres!");
        } catch (SQLException e) {
            LOGGER.warn("Failed to connect to Postgres!", e);
            throw e;
        }
    }

    @Override
    public synchronized void shutdownDatabase() {
        if (controlConnection != null) {
            try {
                controlConnection.close();
                controlConnection = null;
            } catch (SQLException e) {
                LOGGER.info(e.getLocalizedMessage(), e);
            }
        }
        setInitializedFlag(false);
    }

    /**
     * Get the initializedFlag value;
     *
     * @return the initializedFlag
     */
    private synchronized boolean isInitializedFlag() {
        return initializedFlag;
    }

    /**
     * Set the initializedFlag value.
     *
     * @param flag the initializedFlag to set
     */
    private synchronized void setInitializedFlag(boolean flag) {
        this.initializedFlag = flag;
    }

}
