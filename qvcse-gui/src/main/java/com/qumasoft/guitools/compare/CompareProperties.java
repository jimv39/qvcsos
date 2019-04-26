/*   Copyright 2004-2015 Jim Voris
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
package com.qumasoft.guitools.compare;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QumaProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compare properties.
 * @author Jim Voris
 */
public final class CompareProperties extends QumaProperties {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareProperties.class);
    private static final String IGNORE_LEADING_WHITE_SPACE_TAG = "IgnoreLeadingWhitespace";
    private static final String IGNORE_ALL_WHITE_SPACE_TAG = "IgnoreAllWhitespace";
    private static final String IGNORE_CASE_TAG = "IgnoreCase";
    private static final String IGNORE_EOL_CHANGES_TAG = "IgnoreEOLChanges";
    private static final String MRU_FILE1_NAME_TAG = "MRUFile1Name";
    private static final String MRU_FILE2_NAME_TAG = "MRUFile2Name";

    /**
     * Creates new CompareProperties.
     * @param homeDirectory directory location for property file.
     * @param userName the user name.
     */
    public CompareProperties(String homeDirectory, String userName) {
        setPropertyFileName(homeDirectory
                + File.separator
                + QVCSConstants.QVCS_USER_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_COMPARE_PROPERTIES_PREFIX
                + userName
                + ".properties");
        loadProperties(getPropertyFileName());
    }

    private void loadProperties(String propertyFileName) {
        FileInputStream inStream = null;
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        defaultProperties.put(getIgnoreLeadingWhitespaceTag(), QVCSConstants.QVCS_NO);
        defaultProperties.put(getIgnoreAllWhitespaceTag(), QVCSConstants.QVCS_NO);
        defaultProperties.put(getIgnoreCaseTag(), QVCSConstants.QVCS_NO);
        defaultProperties.put(getIgnoreEOLChangesTag(), QVCSConstants.QVCS_NO);

        // Create the actual properties
        setActualProperties(new java.util.Properties(defaultProperties));
        try {
            inStream = new FileInputStream(new File(propertyFileName));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            // Catch any exception.  If the property file is missing, we'll just go
            // with the defaults.
            LOGGER.info("Failed to load compare properties: [{}]", e.getLocalizedMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing visual compare properties file: " + propertyFileName + ". Exception: "
                            + e.getClass().toString() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    void saveProperties() {
        FileOutputStream outStream = null;
        if (getActualProperties() != null) {
            try {
                File propertyFile = new File(getPropertyFileName());
                propertyFile.getParentFile().mkdirs();
                outStream = new FileOutputStream(propertyFile);
                getActualProperties().store(outStream, "QVCS User Visual Compare Properties for user: " + System.getProperty("user.name"));
            } catch (IOException e) {
                // Catch any exception.  If the property file is missing, we'll just go
                // with the defaults.
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Exception in closing visual compare properties file: " + getPropertyFileName() + ". Exception: "
                                + e.getClass().toString() + ": " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
    static String getIgnoreLeadingWhitespaceTag() {
        return IGNORE_LEADING_WHITE_SPACE_TAG;
    }

    static String getIgnoreAllWhitespaceTag() {
        return IGNORE_ALL_WHITE_SPACE_TAG;
    }

    static String getIgnoreCaseTag() {
        return IGNORE_CASE_TAG;
    }

    static String getIgnoreEOLChangesTag() {
        return IGNORE_EOL_CHANGES_TAG;
    }

    static String getMRUFile1NameTag() {
        return MRU_FILE1_NAME_TAG;
    }

    static String getMRUFile2NameTag() {
        return MRU_FILE2_NAME_TAG;
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    void setIgnoreLeadingWhitespace(boolean flag) {
        setBooleanValue(getIgnoreLeadingWhitespaceTag(), flag);
    }

    boolean getIgnoreLeadingWhitespace() {
        return getBooleanValue(getIgnoreLeadingWhitespaceTag());
    }

    void setIgnoreAllWhitespace(boolean flag) {
        setBooleanValue(getIgnoreAllWhitespaceTag(), flag);
    }

    boolean getIgnoreAllWhitespace() {
        return getBooleanValue(getIgnoreAllWhitespaceTag());
    }

    void setIgnoreCase(boolean flag) {
        setBooleanValue(getIgnoreCaseTag(), flag);
    }

    boolean getIgnoreCase() {
        return getBooleanValue(getIgnoreCaseTag());
    }

    void setIgnoreEOLChanges(boolean flag) {
        setBooleanValue(getIgnoreEOLChangesTag(), flag);
    }

    boolean getIgnoreEOLChanges() {
        return getBooleanValue(getIgnoreEOLChangesTag());
    }

    void setMRUFile1Name(String mruFilename) {
        setStringValue(getMRUFile1NameTag(), mruFilename);
    }

    String getMRUFile1Name() {
        return getStringValue(getMRUFile1NameTag());
    }

    void setMRUFile2Name(String mruFilename) {
        setStringValue(getMRUFile2NameTag(), mruFilename);
    }

    String getMRUFile2Name() {
        return getStringValue(getMRUFile2NameTag());
    }
}

