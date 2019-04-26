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
 * Keyword properties.
 * @author Jim Voris
 */
public final class KeywordProperties extends com.qumasoft.qvcslib.QumaProperties {
    // Create our logger object

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordProperties.class);
    private static final String KEYWORD_PROPERTIES_FILENAME = "qvcs.keywords";
    private static final String DEFAULT_WORD_WRAP_COLUMN = "72";

    /** The author key for the author keyword. */
    public static final String AUTHOR_KEY = "Author";
    /** The comment key for the comment keyword. */
    public static final String COMMENT_KEY = "Comment";
    /** The copyright key for the copyright keyword. */
    public static final String COPYRIGHT_KEY = "Copyright";
    /** The date key for the date keyword. */
    public static final String DATE_KEY = "Date";
    /** The endlog key for the endlog keyword. */
    public static final String ENDLOG_KEY = "Endlog";
    /** The filename key for the filename keyword. */
    public static final String FILENAME_KEY = "Filename";
    /** The filepath key for the filepath keyword. */
    public static final String FILEPATH_KEY = "FilePath";
    /** The header key for the header keyword. */
    public static final String HEADER_KEY = "Header";
    /** The headerpath key for the headerpath keyword. */
    public static final String HEADERPATH_KEY = "HeaderPath";
    /** The log key for the log keyword. */
    public static final String LOG_KEY = "Log";
    /** The logfile key for the logfile keyword. */
    public static final String LOGFILE_KEY = "Logfile";
    /** The owner key for the owner keyword. */
    public static final String OWNER_KEY = "Owner";
    /** The revision key for the revision keyword. */
    public static final String REVISION_KEY = "Revision";
    /** The ver key for the ver keyword. */
    public static final String VER_KEY = "VER";
    /** The version key for the version keyword. */
    public static final String VERSION_KEY = "Version";
    /** The label key for the label keyword. */
    public static final String LABEL_KEY = "Label";
    /** The project key for the project keyword. */
    public static final String PROJECT_KEY = "Project";
    /** The marker key for the marker character. The marker character is $ by default. */
    public static final String MARKER_TAG = "MarkerCharacter";
    /** The terminator key for the terminator marker. The terminator marker is $ by default. */
    public static final String TERMINATOR_MARKER = "TerminatorMarker";

    /** The wordwrap column tag. */
    public static final String WORDWRAP_COLUMN_TAG = "WordWrapColumn";
    /** The no label key for the no label message. */
    public static final String NO_LABEL_LABEL_TAG = "NoLabelLabel";
    /** The multiple labels tag for the multiple labels string. */
    public static final String MULTIPLE_LABELS_TAG = "MultipleLabels";
    /** The eol tag for the eol sequence. */
    public static final String EOL_TAG = "EOLSequence";
    /** The copyright message tag for the copyright message. */
    public static final String COPYRIGHT_MESSAGE_TAG = "CopyrightMessage";
    /** The use unix path separator tag for the use unix path separator flag. */
    public static final String USE_UNIX_PATH_SEPARATOR_TAG = "UseUnixPathSeparator";

    /**
     * Creates a new instance of KeywordProperties.
     */
    public KeywordProperties() {
        setPropertyFileName(System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_BEHAVIOR_PROPERTIES_DIRECTORY
                + File.separator
                + KEYWORD_PROPERTIES_FILENAME + ".properties");

        loadProperties(getPropertyFileName());
    }

    private void loadProperties(String propertyFilename) {
        FileInputStream inStream = null;
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        defaultProperties.put(AUTHOR_KEY, AUTHOR_KEY);
        defaultProperties.put(COMMENT_KEY, COMMENT_KEY);
        defaultProperties.put(COPYRIGHT_KEY, COPYRIGHT_KEY);
        defaultProperties.put(DATE_KEY, DATE_KEY);
        defaultProperties.put(ENDLOG_KEY, ENDLOG_KEY);
        defaultProperties.put(FILENAME_KEY, FILENAME_KEY);
        defaultProperties.put(HEADER_KEY, HEADER_KEY);
        defaultProperties.put(LOG_KEY, LOG_KEY);
        defaultProperties.put(LOGFILE_KEY, LOGFILE_KEY);
        defaultProperties.put(OWNER_KEY, OWNER_KEY);
        defaultProperties.put(REVISION_KEY, REVISION_KEY);
        defaultProperties.put(VER_KEY, VER_KEY);
        defaultProperties.put(VERSION_KEY, VERSION_KEY);
        defaultProperties.put(LABEL_KEY, LABEL_KEY);
        defaultProperties.put(MARKER_TAG, "$");
        defaultProperties.put(TERMINATOR_MARKER, ":");
        defaultProperties.put(WORDWRAP_COLUMN_TAG, DEFAULT_WORD_WRAP_COLUMN);
        defaultProperties.put(NO_LABEL_LABEL_TAG, "NONE");
        defaultProperties.put(MULTIPLE_LABELS_TAG, "MULTIPLE LABELS");
        defaultProperties.put(COPYRIGHT_MESSAGE_TAG, "Define this string in the qvcs.keywords.properties property file");
        defaultProperties.put(USE_UNIX_PATH_SEPARATOR_TAG, QVCSConstants.QVCS_YES);

        setActualProperties(new java.util.Properties(defaultProperties));
        try {
            inStream = new FileInputStream(new File(propertyFilename));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            LOGGER.warn("Keyword properties file not found: [{}]", propertyFilename);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing keyword properties file: " + propertyFilename + ". Exception: " + e.getClass().toString() + ": "
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Get the word wrap column.
     * @return the word wrap column.
     */
    public int getWordWrapColumn() {
        return getIntegerValue(KeywordProperties.WORDWRAP_COLUMN_TAG);
    }

    /**
     * Use the unix path separator.
     * @return true if we use the unix path separator; false otherwise.
     */
    public boolean getUseUnixPathSeparator() {
        return getBooleanValue(KeywordProperties.USE_UNIX_PATH_SEPARATOR_TAG);
    }

    /**
     * Get the end-of-line sequence.. (CRLF for Windows; LF for *nix)
     * @return the end-of-line sequence.
     */
    public byte[] getEOLSequence() {
        byte[] eolSequence;

        String eolString = getStringValue(EOL_TAG);
        if (eolString.length() == 0) {
            eolSequence = new byte[2];
            eolSequence[0] = '\r';
            eolSequence[1] = '\n';
        } else {
            eolSequence = eolString.getBytes();
        }
        return eolSequence;
    }
}
