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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User location properties. Store user specific workfile locations.
 * @author Jim Voris
 */
public final class UserLocationProperties extends QumaProperties {
    // Create our logger object

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLocationProperties.class);
    private static final String WORKFILE_LOCATION_TAG = "_QVCS_WORKFILELOCATION";
    private final String loggedInUserName;

    /**
     * Creates a new instance of UserLocationProperties.
     * @param homeDirectory root directory beneath which we'll locate the property file.
     * @param userName the logged in user name.
     */
    public UserLocationProperties(String homeDirectory, String userName) {
        loggedInUserName = userName;

        setPropertyFileName(homeDirectory
                + File.separator
                + QVCSConstants.QVCS_USER_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_USERLOCATION_PROPERTIES_PREFIX
                + loggedInUserName
                + ".properties");
        loadProperties(getPropertyFileName());
    }

    protected void loadProperties(String propertyFileName) {
        FileInputStream inStream = null;

        // Create the actual properties
        setActualProperties(new java.util.Properties());
        try {
            inStream = new FileInputStream(new File(getPropertyFileName()));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            // Catch any exception.  If the property file is missing, we'll just go
            // with the defaults.
            LOGGER.warn("Exception in loading user location properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString()
                    + ": " + e.getLocalizedMessage() + ". Using default user property values.");
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing user location properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString()
                            + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Save the property file to disk.
     */
    public void saveProperties() {
        FileOutputStream outStream = null;
        if (getActualProperties() != null) {
            try {
                File propertyFile = new File(getPropertyFileName());
                propertyFile.getParentFile().mkdirs();
                outStream = new FileOutputStream(propertyFile);
                getActualProperties().store(outStream, "QVCS User Location Properties for user: " + loggedInUserName);
            } catch (IOException e) {
                // Catch any exception.  If the property file is missing, we'll just go
                // with the defaults.
                LOGGER.warn("Exception in opening user location properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString() + ": "
                        + e.getLocalizedMessage());
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Exception in closing user location properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString() + ": "
                                + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    /**
     * Set the workfile location for a given server, project, and branch.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param location the workfile location for the above.
     */
    public void setWorkfileLocation(String serverName, String projectName, String branchName, String location) {
        String workfileLocationTag;

        if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            workfileLocationTag = serverName + "_" + projectName + "_" + WORKFILE_LOCATION_TAG;
        } else {
            workfileLocationTag = serverName + "_" + projectName + "_" + branchName + "_" + WORKFILE_LOCATION_TAG;
        }
        setStringValue(workfileLocationTag, location);
    }

    /**
     * Get the workfile location for a given server, project, and branch.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @return the workfile location for the above.
     */
    public String getWorkfileLocation(String serverName, String projectName, String branchName) {
        String workfileLocationTag;

        if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            workfileLocationTag = serverName + "_" + projectName + "_" + WORKFILE_LOCATION_TAG;
        } else {
            workfileLocationTag = serverName + "_" + projectName + "_" + branchName + "_" + WORKFILE_LOCATION_TAG;
        }

        return getStringValue(workfileLocationTag);
    }
}
