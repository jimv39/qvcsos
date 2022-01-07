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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A database manager used for connections to a Postgres database.
 *
 * @author Jim Voris
 */
public final class DatabaseManager implements DatabaseManagerInterface {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    /**
     * Our singleton databaseManager instance
     */
    private static final DatabaseManager DATABASE_MANAGER = new DatabaseManager();
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

    private String databaseUrl;
    private String username;
    private String password;
    private String schemaName;

    /**
     * Private constructor, so no one else can make a PostgresDatabaseManager
     * object.
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
     * Set the username. We use this for unit tests only.
     * @param uname the user name for the test database.
     */
    public void setUsername(String uname) {
        this.username = uname;
    }

    /**
     * Set the password. We use this for unit tests only.
     * @param pword the password for the test database.
     */
    public void setPassword(String pword) {
        this.password = pword;
    }

    /**
     * Set the db URL. We use this for unit tests only.
     * @param url the URL for the test database.
     */
    public void setUrl(String url) {
        this.databaseUrl = url;
    }

    @Override
    public String getSchemaName() {
        // Guarantee that we have initialized the schema name.
        initializeDatabase();
        return this.schemaName;
    }

    /**
     * Set the schema name. We use this for testing.
     * @param name the name of the schema.
     */
    public void setSchemaName(String name) {
        this.schemaName = name;
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
        Connection connection;

        // Make sure the database is initialized.
        initializeDatabase();
        connection = threadLocalConnection.get();
        if (connection == null) {
            connection = DriverManager.getConnection(databaseUrl, username, password);
            connection.setAutoCommit(true);
            threadLocalConnection.set(connection);
            LOGGER.info("Thread [{}]: got database connection.", Thread.currentThread().getName());
        } else {
            LOGGER.trace("Thread [{}]: reuse thread's database connection.", Thread.currentThread().getName());
        }
        return connection;
    }

    @Override
    public synchronized void initializeDatabase() {
        if (!isInitializedFlag()) {
            try {
                initializeConnectionProperties();
                controlConnection = DriverManager.getConnection(databaseUrl, username, password);
                setInitializedFlag(true);
                LOGGER.info("Connected to Postgres!");
            } catch (SQLException e) {
                LOGGER.warn("Failed to connect to Postgres!", e);
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void initializeToMigrationDatabase() {
        try {
            initializeMigrationConnectionProperties();
            controlConnection = DriverManager.getConnection(databaseUrl, username, password);
            setInitializedFlag(true);
            LOGGER.info("Connected to Postgres!");
        } catch (SQLException e) {
            LOGGER.warn("Failed to connect to Postgres!", e);
            throw new RuntimeException(e);
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

    /**
     * Read the database connection properties from the postgresql.properties file. If that file is not found, create it, and populate it with our default values.
     * The user can manually edit the result to define the connection properties that they need for their postgresql server.
     */
    private void initializeConnectionProperties() {
        DatabaseConnectionProperties connectionProperties = DatabaseConnectionProperties.getInstance();
        setUrl(connectionProperties.getConnectionUrl());
        setUsername(connectionProperties.getUsername());
        setPassword(connectionProperties.getPassword());
        setSchemaName(connectionProperties.getSchema());
    }

    private void initializeMigrationConnectionProperties() {
        MigrationDatabaseConnectionProperties connectionProperties = MigrationDatabaseConnectionProperties.getInstance();
        setUrl(connectionProperties.getConnectionUrl());
        setUsername(connectionProperties.getUsername());
        setPassword(connectionProperties.getPassword());
        setSchemaName(connectionProperties.getSchema());
    }

}
