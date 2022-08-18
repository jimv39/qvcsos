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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QumaProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public final class DatabaseConnectionProperties extends QumaProperties {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionProperties.class);
    // The singleton instance.
    private static final DatabaseConnectionProperties POSTGRESQL_CONNECTION_PROPERTIES = new DatabaseConnectionProperties();

    private static final String POSTGRESQL_CONNECTION_PROPERTIES_FILENAME = "qvcsos.postgresql.connection";

    private static final String CONNECTION_URL_KEY = "CONNECTION_URL";
    private static final String CONNECTION_USER_KEY = "USER";
    private static final String CONNECTION_PASSWORD_KEY = "PASSWORD";
    private static final String SCHEMA_KEY = "SCHEMA";

    private static final String DEFAULT_CONNECTION_URL = "jdbc:postgresql://localhost:5432/qvcsos410test";
    private static final String DEFAULT_CONNECTION_USER = "qvcsos410test";
    private static final String DEFAULT_CONNECTION_PASSWORD = "qvcsos410testPG$Admin";
    private static final String DEFAULT_CONNECTION_SCHEMA = "qvcsos410test";

    /**
     * Get the Database connection properties singleton.
     * @return the database connection properties singleton.
     */
    public static DatabaseConnectionProperties getInstance() {
        return POSTGRESQL_CONNECTION_PROPERTIES;
    }

    /**
     * Creates a new instance of PostgresqlConnectionProperties
     */
    private DatabaseConnectionProperties() {
        setPropertyFileName(System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_BEHAVIOR_PROPERTIES_DIRECTORY
                + File.separator
                + POSTGRESQL_CONNECTION_PROPERTIES_FILENAME + ".properties");

        try {
            loadProperties(getPropertyFileName());
        } catch (QVCSException e) {
            LOGGER.warn("Failure loading postgresql connection properties: [{}] [{}]", e.getClass().toString(), e.getLocalizedMessage());
        }
    }

    private void loadProperties(String propertyFilename) throws QVCSException {
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        defaultProperties.put(CONNECTION_URL_KEY, DEFAULT_CONNECTION_URL);
        defaultProperties.put(CONNECTION_USER_KEY, DEFAULT_CONNECTION_USER);
        defaultProperties.put(CONNECTION_PASSWORD_KEY, DEFAULT_CONNECTION_PASSWORD);
        defaultProperties.put(SCHEMA_KEY, DEFAULT_CONNECTION_SCHEMA);

        setActualProperties(defaultProperties);
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(new File(propertyFilename));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            LOGGER.warn("Postgresql connection properties file not found: [{}]", propertyFilename);
            // We need to create the property file.
            saveProperties();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing Postgresql connection properties file: [{}]. Exception: [{}], Message: [{}]", propertyFilename, e.getClass().toString(), e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Get the connection URL.
     * @return the connection URL.
     */
    public String getConnectionUrl() {
        return getStringValue(CONNECTION_URL_KEY);
    }

    /**
     * Get the username for the postgresql connection.
     * @return the username for the postgresql connection.
     */
    String getUsername() {
        return getStringValue(CONNECTION_USER_KEY);
    }

    /**
     * Get the password for the postgresql connection.
     * @return the password for the postgresql connection.
     */
    String getPassword() {
        return getStringValue(CONNECTION_PASSWORD_KEY);
    }

    /**
     * Get the schema for the postgresql connection.
     * @return the schema for the postgresql connection.
     */
    public String getSchema() {
        return getStringValue(SCHEMA_KEY);
    }

    /**
     * Save the property file to disk.
     */
    private void saveProperties() {
        FileOutputStream outStream = null;
        if (getActualProperties() != null) {
            try {
                File propertyFile = new File(getPropertyFileName());
                propertyFile.getParentFile().mkdirs();
                outStream = new FileOutputStream(propertyFile);
                getActualProperties().store(outStream, "Postgresql connection properties for server");
                LOGGER.info("Postgresql connection properties created: [{}]", getPropertyFileName());
            } catch (IOException e) {
                // Catch any exception.  If the property file is missing, we'll just go
                // with the defaults.
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Exception in closing Postgresql connection properties file: [{}], Exception: [{}]: [{}]", getPropertyFileName(), e.getClass().toString(), e.getLocalizedMessage());
                    }
                }
            }
        }
    }
}
