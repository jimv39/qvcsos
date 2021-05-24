/*   Copyright 2021 Jim Voris
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
 * A singleton that defines the derby connection properties file.
 *
 * @author Jim Voris
 */
public final class DerbyConnectionProperties extends QumaProperties {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(DerbyConnectionProperties.class);
    // The singleton instance.
    private static final DerbyConnectionProperties DERBY_CONNECTION_PROPERTIES = new DerbyConnectionProperties();

    private static final String DERBY_CONNECTION_PROPERTIES_FILENAME = "qvcs.derby.connection";

    private static final String CONNECTION_URL_KEY = "CONNECTION_URL";
    private static final String SCHEMA_KEY = "SCHEMA";

    private static final String DEFAULT_CONNECTION_URL = "jdbc:derby:qvcsedb";
    private static final String DEFAULT_CONNECTION_SCHEMA = "qvcse";

    /**
     * Get the Derby connection properties singleton.
     * @return the Derby connection properties singleton.
     */
    public static DerbyConnectionProperties getInstance() {
        return DERBY_CONNECTION_PROPERTIES;
    }

    /**
     * Creates a new instance of DerbyConnectionProperties
     */
    private DerbyConnectionProperties() {
        setPropertyFileName(System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_BEHAVIOR_PROPERTIES_DIRECTORY
                + File.separator
                + DERBY_CONNECTION_PROPERTIES_FILENAME + ".properties");

        try {
            loadProperties(getPropertyFileName());
        } catch (QVCSException e) {
            LOGGER.warn("Failure loading derby connection properties: [{}] [{}]", e.getClass().toString(), e.getLocalizedMessage());
        }
    }

    private void loadProperties(String propertyFilename) throws QVCSException {
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        defaultProperties.put(CONNECTION_URL_KEY, DEFAULT_CONNECTION_URL);
        defaultProperties.put(SCHEMA_KEY, DEFAULT_CONNECTION_SCHEMA);

        setActualProperties(defaultProperties);
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(new File(propertyFilename));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            LOGGER.warn("Derby connection properties file not found: [{}]", propertyFilename);
            // We need to create the property file.
            saveProperties();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing Derby connection properties file: [{}]. Exception: [{}], Message: [{}]", propertyFilename, e.getClass().toString(), e.getLocalizedMessage());
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
     * Get the schema for the Derby connection.
     * @return the schema for the Derby connection.
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
                getActualProperties().store(outStream, "Derby connection properties for server");
                LOGGER.info("Derby connection properties created: [{}]", getPropertyFileName());
            } catch (IOException e) {
                // Catch any exception.  If the property file is missing, we'll just go
                // with the defaults.
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Exception in closing Derby connection properties file: [{}], Exception: [{}]: [{}]", getPropertyFileName(), e.getClass().toString(), e.getLocalizedMessage());
                    }
                }
            }
        }
    }
}
