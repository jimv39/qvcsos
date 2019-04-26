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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton that captures the attributes that we store in a properties file. This class loads those attributes from the property file so an administrator can define the default
 * set of attributes that will be associated with a given file extension.
 *
 * @author Jim Voris
 */
public final class ExtensionAttributeProperties extends QumaProperties {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionAttributeProperties.class);
    // The singleton instance.
    private static final ExtensionAttributeProperties EXTENSION_ATTRIBUTE_PROPERTIES = new ExtensionAttributeProperties();

    private static final String EXTENSION_ATTRIBUTES_PROPERTIES_FILENAME = "qvcs.extension.attributes";
    // These are the default extensions that we support.
    private static final String JAVA_KEY = "java";
    private static final String JAVA_SCRIPT_KEY = "js";
    private static final String CPP_KEY = "cpp";
    private static final String BAS_KEY = "bas";
    private static final String SQL_KEY = "sql";
    private static final String C_KEY = "c";
    private static final String H_KEY = "h";
    private static final String PAS_KEY = "pas";
    private static final String TXT_KEY = "txt";
    private static final String FOR_KEY = "for";
    private static final String DOC_KEY = "doc";
    private static final String OTHER_KEY = "other";
    // These are the default attributes for the default extensions
    /**
     * Check locks delete workfile expand keywords protect archive protect workfile journal file compression binary file auto-merge no compute delta (YES means don't compute a
     * delta; NO means to compute a delta) latest revision only
     */
    private static final String JAVA_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String JAVA_SCRIPT_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String CPP_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String BAS_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String SQL_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String C_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String H_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String PAS_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String TXT_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String FOR_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";
    private static final String DOC_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,YES,NO,YES,NO";
    private static final String OTHER_ATTRIBUTES = "YES,NO,NO,NO,YES,YES,YES,NO,NO,NO,NO";


    /**
     * Get the extension attribute properties singleton.
     * @return the extension attribute properties singleton.
     */
    public static ExtensionAttributeProperties getInstance() {
        return EXTENSION_ATTRIBUTE_PROPERTIES;
    }

    /**
     * Creates a new instance of ExtensionAttributeProperties
     */
    private ExtensionAttributeProperties() {
        setPropertyFileName(System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_BEHAVIOR_PROPERTIES_DIRECTORY
                + File.separator
                + EXTENSION_ATTRIBUTES_PROPERTIES_FILENAME + ".properties");

        try {
            loadProperties(getPropertyFileName());
        } catch (QVCSException e) {
            LOGGER.warn("Failure loading archive attribute properties: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }
    }

    private void loadProperties(String propertyFilename) throws QVCSException {
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        defaultProperties.put(JAVA_KEY, JAVA_ATTRIBUTES);
        defaultProperties.put(JAVA_SCRIPT_KEY, JAVA_SCRIPT_ATTRIBUTES);
        defaultProperties.put(CPP_KEY, CPP_ATTRIBUTES);
        defaultProperties.put(BAS_KEY, BAS_ATTRIBUTES);
        defaultProperties.put(SQL_KEY, SQL_ATTRIBUTES);
        defaultProperties.put(C_KEY, C_ATTRIBUTES);
        defaultProperties.put(H_KEY, H_ATTRIBUTES);
        defaultProperties.put(PAS_KEY, PAS_ATTRIBUTES);
        defaultProperties.put(TXT_KEY, TXT_ATTRIBUTES);
        defaultProperties.put(FOR_KEY, FOR_ATTRIBUTES);
        defaultProperties.put(DOC_KEY, DOC_ATTRIBUTES);
        defaultProperties.put(OTHER_KEY, OTHER_ATTRIBUTES);

        setActualProperties(new java.util.Properties(defaultProperties));
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(new File(propertyFilename));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            LOGGER.warn("Archive sttribute properties file not found: " + propertyFilename);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing extension attributes properties file: " + propertyFilename + ". Exception: "
                            + e.getClass().toString() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Get the attributes for a file. This method determines the attributes based on file extension.
     *
     * @param filename the file for which we want to get the default attributes.
     * @return the default attributes for the given file.
     */
    public ArchiveAttributes getAttributes(String filename) {
        ArchiveAttributes attributes;
        String extension = Utility.getFileExtension(filename);
        String lowercaseExtension = extension.toLowerCase();

        String attributeString = getStringValue(lowercaseExtension);

        if (attributeString.length() > 0) {
            attributes = new ArchiveAttributes();
            attributes.fromPropertyString(attributeString);
        } else {
            // Use the default attributes from the file, if it exists....
            attributeString = getStringValue(OTHER_KEY);
            if (attributeString.length() > 0) {
                attributes = new ArchiveAttributes();
                attributes.fromPropertyString(attributeString);
            } else {
                // No default attributes are defined in the file, use these
                // hard coded defaults.
                attributes = new ArchiveAttributes();
                attributes.fromPropertyString(OTHER_ATTRIBUTES);
            }
        }

        return attributes;
    }
}
