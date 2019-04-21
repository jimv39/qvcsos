//   Copyright 2004-2019 Jim Voris
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote project properties.
 * @author Jim Voris
 */
public class RemoteProjectProperties extends AbstractProjectProperties {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteProjectProperties.class);

    /**
     * Construct remote project properties for given project.
     * @param directory directory where to find the property file directory.
     * @param projectName the project name.
     */
    public RemoteProjectProperties(String directory, String projectName) {
        super(directory, projectName, QVCSConstants.QVCS_REMOTE_PROJECTNAME_PREFIX);
        loadProperties();
    }

    /**
     * This constructor is used so the client application can construct a remote project properties object using the response message
     * from the server that is serving this project.
     * @param project the project name.
     * @param properties the project properties.
     */
    public RemoteProjectProperties(String project, Properties properties) {
        setProjectName(project);
        setPropertyFilePrefix(QVCSConstants.QVCS_REMOTE_PROJECTNAME_PREFIX);
        setActualProperties(new Properties(properties));
    }

    /**
     * Is this a remote project.
     * @return well yeah! This always returns true.
     */
    @Override
    public boolean isRemoteProject() {
        return true;
    }

    private void loadProperties() {
        FileInputStream inStream = null;
        java.util.Properties defaultProperties = DefaultRemoteProjectProperties.getInstance().getProjectProperties();

        // The default properties are from the default project's properties
        setActualProperties(new java.util.Properties(defaultProperties));
        try {
            inStream = new FileInputStream(new File(getPropertyFileName()));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            LOGGER.warn("Remote Project properties file not found: [{}]", getPropertyFileName());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing project properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString() + ": "
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Get the project type.
     * @return string that indicates this is a remote project.
     */
    @Override
    public String getProjectType() {
        return QVCSConstants.QVCS_REMOTE_PROJECT_TYPE;
    }

    /**
     * Get flag indicating if there is an alternate reference location defined.
     * @return always return false.
     */
    @Override
    public boolean getDefineAlternateReferenceLocationFlag() {
        return false;
    }
}
