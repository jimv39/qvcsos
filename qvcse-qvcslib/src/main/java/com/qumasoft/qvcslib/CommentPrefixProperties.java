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
 * A singleton that captures the comment prefixes that we store in a properties file. This class loads those comment prefixes from the property file so an administrator
 * can define the default set of comment prefixes that will be associated with a given file extension.
 *
 * @author Jim Voris
 */
public final class CommentPrefixProperties extends QumaProperties {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentPrefixProperties.class);
    private static final String COMMENT_PREFIX_ATTRIBUTES_PROPERTIES_FILENAME = "qvcs.comment.prefix";
    // These are the default extensions that we support.
    private static final String JAVA_KEY = "java";
    private static final String CPP_KEY = "cpp";
    private static final String BAS_KEY = "bas";
    private static final String SQL_KEY = "sql";
    private static final String C_KEY = "c";
    private static final String H_KEY = "h";
    private static final String PAS_KEY = "pas";
    private static final String TXT_KEY = "txt";
    private static final String FOR_KEY = "for";
    private static final String OTHER_KEY = "other";
    // These are the default comment prefixes for the default extensions
    private static final String JAVA_COMMENT_PREFIX = "//  ";
    private static final String CPP_COMMENT_PREFIX = "//  ";
    private static final String BAS_COMMENT_PREFIX = "'  ";
    private static final String SQL_COMMENT_PREFIX = "#  ";
    private static final String C_COMMENT_PREFIX = "//  ";
    private static final String H_COMMENT_PREFIX = "//  ";
    private static final String PAS_COMMENT_PREFIX = " * ";
    private static final String TXT_COMMENT_PREFIX = "    ";
    private static final String FOR_COMMENT_PREFIX = "C   ";
    private static final String OTHER_COMMENT_PREFIX = "    ";
    private static final CommentPrefixProperties COMMENT_PREFIX_PROPERTIES = new CommentPrefixProperties();

    /**
     * Get the singleton instance.
     * @return the singleton instance.
     */
    public static CommentPrefixProperties getInstance() {
        return COMMENT_PREFIX_PROPERTIES;
    }

    /**
     * Creates a new instance of CommentPrefixProperties.
     */
    private CommentPrefixProperties() {
        setPropertyFileName(System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_BEHAVIOR_PROPERTIES_DIRECTORY
                + File.separator
                + COMMENT_PREFIX_ATTRIBUTES_PROPERTIES_FILENAME + ".properties");

        try {
            loadProperties(getPropertyFileName());
        } catch (QVCSException e) {
            LOGGER.warn("Failure loading comment prefix properties: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }
    }

    private void loadProperties(String propertyFilename) throws QVCSException {
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        defaultProperties.put(JAVA_KEY, JAVA_COMMENT_PREFIX);
        defaultProperties.put(CPP_KEY, CPP_COMMENT_PREFIX);
        defaultProperties.put(BAS_KEY, BAS_COMMENT_PREFIX);
        defaultProperties.put(SQL_KEY, SQL_COMMENT_PREFIX);
        defaultProperties.put(C_KEY, C_COMMENT_PREFIX);
        defaultProperties.put(H_KEY, H_COMMENT_PREFIX);
        defaultProperties.put(PAS_KEY, PAS_COMMENT_PREFIX);
        defaultProperties.put(TXT_KEY, TXT_COMMENT_PREFIX);
        defaultProperties.put(FOR_KEY, FOR_COMMENT_PREFIX);
        defaultProperties.put(OTHER_KEY, OTHER_COMMENT_PREFIX);

        setActualProperties(new java.util.Properties(defaultProperties));
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(new File(propertyFilename));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            LOGGER.warn("Comment prefix properties file not found: " + propertyFilename);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing comment prefix properties file: " + propertyFilename + ". Exception: " + e.getClass().toString() + ": "
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Get the comment prefix for a file. This method determines the comment prefix based on the file extension.
     * @param filename the filename that we want the comment prefix for.
     * @return the associated comment prefix.
     */
    public String getCommentPrefix(String filename) {
        String retVal;
        String extension = Utility.getFileExtension(filename);

        String commentPrefix = getStringValue(extension);

        if (commentPrefix.length() > 0) {
            retVal = commentPrefix;
        } else {
            retVal = getStringValue(OTHER_KEY);
        }
        return retVal;
    }
}
