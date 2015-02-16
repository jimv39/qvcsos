//   Copyright 2004-2015 Jim Voris
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default remote project properties.
 * @author Jim Voris
 */
public final class DefaultRemoteProjectProperties extends AbstractProjectProperties {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRemoteProjectProperties.class);
    private static final DefaultRemoteProjectProperties SINGLETON_PROPERTIES = new DefaultRemoteProjectProperties();

    /**
     * Creates new DefaultServedProjectProperties.
     */
    private DefaultRemoteProjectProperties() {
        super(QVCSConstants.QWIN_DEFAULT_PROJECT_PROPERTIES_NAME, QVCSConstants.QVCS_REMOTE_PROJECTNAME_PREFIX);
        loadProperties();
    }

    /**
     * Load the properties for the default project.
     */
    private void loadProperties() {
        FileInputStream inStream = null;
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        ArchiveAttributes defaultAttributes = new ArchiveAttributes();
        defaultProperties.put(getReferenceLocationTag(), "");
        defaultProperties.put(getAttributesTag(), defaultAttributes.toPropertyString());
        defaultProperties.put(getTempfilePathTag(), System.getProperty("user.home") + System.getProperty("file.separator") + "qvcs_tempfiles");
        defaultProperties.put(getServerNameTag(), QVCSConstants.QVCS_DEFAULT_SERVER_NAME);
        setActualProperties(new java.util.Properties(defaultProperties));
        try {
            inStream = new FileInputStream(new File(getPropertyFileName()));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            // Catch any exception.  If the property file is missing, we'll just go
            // with the defaults.
            LOGGER.warn("[" + getPropertyFileName() + "] not found. Using hard-coded default values.");
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing project properties file: [" + getPropertyFileName() + "]. Exception: " + e.getClass().toString() + ": "
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Get the singleton object that defines our default project properties.
     * @return the singleton object that defines our default project properties.
     */
    public static DefaultRemoteProjectProperties getInstance() {
        return SINGLETON_PROPERTIES;
    }

    /**
     * Return the default project name.
     * @return the default project name.
     */
    @Override
    public String toString() {
        return QVCSConstants.QWIN_DEFAULT_PROJECT_NAME;
    }

    /**
     * Default to a null value for the archive location.
     * @return Default to a null value for the archive location.
     */
    @Override
    public String getArchiveLocation() {
        return null;
    }

    @Override
    public String getProjectType() {
        return QVCSConstants.QVCS_REMOTE_PROJECT_TYPE;
    }

    @Override
    public boolean getDefineAlternateReferenceLocationFlag() {
        return getBooleanValue(getDefineAlternateReferenceLocationFlagTag());
    }
}
