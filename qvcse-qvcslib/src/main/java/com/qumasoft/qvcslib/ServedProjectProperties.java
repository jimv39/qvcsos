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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Served project properties. Used by the server to identify a project served by the server.
 * @author Jim Voris
 */
public class ServedProjectProperties extends AbstractProjectProperties {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");

    /**
     * Construct a ServedProjectProperties instance.
     * @param projectName the name of the project.
     * @throws QVCSException if the property file cannot be found.
     */
    public ServedProjectProperties(String projectName) throws QVCSException {
        super(projectName, QVCSConstants.QVCS_SERVED_PROJECTNAME_PREFIX);
        loadProperties(getPropertyFileName());
    }

    /**
     * This ctor is used so the Admin app can construct a served project properties
     * object using the response message from the server that is serving this
     * project.
     * @param projectName the project name.
     * @param properties existing properties to use.
     */
    public ServedProjectProperties(String projectName, Properties properties) {
        setProjectName(projectName);
        setPropertyFilePrefix(QVCSConstants.QVCS_SERVED_PROJECTNAME_PREFIX);
        setActualProperties(properties);
    }

    /**
     * Get the flag that identifies if this is a served project.
     * @return true.
     */
    @Override
    public boolean isServedProject() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDefineAlternateReferenceLocationFlag() {
        return getBooleanValue(getDefineAlternateReferenceLocationFlagTag());
    }

    private void loadProperties(String propertyFilename) throws QVCSException {
        java.util.Properties defaultProperties = DefaultServedProjectProperties.getInstance().getProjectProperties();
        FileInputStream inStream = null;

        // The default properties are from the default project's properties
        setActualProperties(new java.util.Properties(defaultProperties));
        try {
            inStream = new FileInputStream(new File(propertyFilename));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Served Project properties file not found: [" + propertyFilename + "]");
            throw new QVCSException("Served Project properties file not found: [" + propertyFilename + "]");
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Exception in closing project properties file: " + propertyFilename + ". Exception: " + e.getClass().toString()
                            + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectType() {
        return QVCSConstants.QVCS_SERVED_PROJECT_TYPE;
    }

    /**
     * Get the project properties.
     * @return the project properties.
     */
    public Properties getProperties() {
        return getActualProperties();
    }
}
