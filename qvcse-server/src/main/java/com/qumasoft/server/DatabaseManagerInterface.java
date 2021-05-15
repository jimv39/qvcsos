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
import java.sql.SQLException;

/**
 * Define the interface that must be implemented for any database.
 *
 * @author Jim Voris
 */
public interface DatabaseManagerInterface {

    /**
     * Close the connection for the calling Thread.
     *
     * @throws SQLException if we cannot close this thread's db connection.
     */
    void closeConnection() throws SQLException;

    /**
     * Get a database connection.
     *
     * @return a database connection for the calling Thread.
     * @throws SQLException if we cannot get a db connection.
     */
    Connection getConnection() throws SQLException;

    /**
     * Get the name of the db schema that the database manager will use.
     * @return the name of the db schema that the database manager will use.
     */
    String getSchemaName();

    /**
     * This method initializes the database. The initialization strategy depends on the implementation.
     *
     * @throws java.sql.SQLException if we cannot open the database.
     * @throws java.lang.ClassNotFoundException if we can't load the database driver.
     */
    void initializeDatabase() throws SQLException, ClassNotFoundException;

    /**
     * Close all database connections.
     */
    void shutdownDatabase();
}
