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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract project properties.
 * @author Jim Voris
 */
public abstract class AbstractProjectProperties extends QumaProperties {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
    private static final String REFERENCE_LOCATION_TAG = "QVCS_REFERENCELOCATION";
    private static final String CREATE_REFERENCE_COPY_FLAG_TAG = "QVCS_CREATEREFERENCECOPYFLAG";
    private static final String IGNORE_CASE_FLAG_TAG = "QVCS_IGNORECASEFLAG";
    private static final String ATTRIBUTES_TAG = "QVCS_ATTRIBUTES";
    private static final String TEMP_FILE_PATH_TAG = "QVCS_TMPPREFIX";
    private static final String ARCHIVE_LOCATION_TAG = "QVCS_ARCHIVELOCATION";
    private static final String PARENT_PROJECT_NAME_TAG = "QVCS_PARENTPROJECTNAME";
    private static final String SERVER_NAME_TAG = "QVCS_SERVERNAME";
    private static final String INITIAL_ARCHIVE_ACCESS_LIST_TAG = "QVCS_INITIAL_ARCHIVE_ACCESSLIST";
    private static final String DEFINE_ALTERNATE_REFERENCE_LOCATION_FLAG_TAG = "QVCS_DEFINE_ALTERNATE_REFERENCE_LOCATION_FLAG";
    private static final String DIRECTORY_CONTENTS_INITIALIZED_FLAG_TAG = "QVCS_DIRECTORY_CONTENTS_INITIALIZED_FLAG";
    private String projectName;
    private String propertyFilePrefix;

    /**
     * Get the project type.
     * @return the type of project.
     */
    public abstract String getProjectType();

    /**
     * Get the flag that defines whether this project uses an alternate location for reference files.
     * @return true if an alternate location is used; false otherwise.
     */
    public abstract boolean getDefineAlternateReferenceLocationFlag();

    /**
     * Get the flag that identifies if this as a local project.
     * @return true if this is a local project; false otherwise.
     */
    public boolean isLocalProject() {
        return false;
    }

    /**
     * Get the flag that identifies if this is a remote project.
     * @return true if this is a remote project; false otherwise.
     */
    public boolean isRemoteProject() {
        return false;
    }

    /**
     * Get the flag that identifies if this is a served project.
     * @return true if this is a served project; false otherwise.
     */
    public boolean isServedProject() {
        return false;
    }

    /**
     * Creates new ProjectProperties.
     * @param project the project name.
     * @param propertyFilePfx the property file prefix.
     */
    public AbstractProjectProperties(String project, String propertyFilePfx) {
        this.projectName = project;
        this.propertyFilePrefix = propertyFilePfx;

        setPropertyFileName(System.getProperty("user.dir")
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_PROPERTIES_DIRECTORY
                + System.getProperty("file.separator")
                + propertyFilePfx + project + ".properties");
    }

    protected AbstractProjectProperties() {
    }

    /**
     * Save the properties.
     */
    public void saveProperties() {
        FileOutputStream outStream = null;

        if (getActualProperties() != null) {
            try {
                outStream = new FileOutputStream(new File(getPropertyFileName()));
                getActualProperties().store(outStream, "QVCS Project Properties for project: " + projectName);
            } catch (IOException e) {
                // Catch any exception.  If the property file is missing, we'll just go
                // with the defaults.
                LOGGER.log(Level.WARNING, e.getMessage());
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Exception in closing project properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString()
                                + ": " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    /**
     * Get the reference location tag.
     * @return the reference location tag.
     */
    public static String getReferenceLocationTag() {
        return REFERENCE_LOCATION_TAG;
    }

    /**
     * Get the create reference copy flag tag.
     * @return the create reference copy flag tag.
     */
    public static String getCreateReferenceCopyFlagTag() {
        return CREATE_REFERENCE_COPY_FLAG_TAG;
    }

    /**
     * Get the ignore case flag tag.
     * @return the ignore case flag tag.
     */
    public static String getIgnoreCaseFlagTag() {
        return IGNORE_CASE_FLAG_TAG;
    }

    /**
     * Get the attributes tag.
     * @return the attributes tag.
     */
    public static String getAttributesTag() {
        return ATTRIBUTES_TAG;
    }

    /**
     * Get the temp file path tag.
     * @return the temp file path tag.
     */
    public static String getTempfilePathTag() {
        return TEMP_FILE_PATH_TAG;
    }

    /**
     * Get the archive location tag.
     * @return the archive location tag.
     */
    public static String getArchiveLocationTag() {
        return ARCHIVE_LOCATION_TAG;
    }

    /**
     * Get the parent project name tag.
     * @return the parent project name tag.
     */
    public static String getParentProjectNameTag() {
        return PARENT_PROJECT_NAME_TAG;
    }

    /**
     * Get the server name tag.
     * @return the server name tag.
     */
    public static String getServerNameTag() {
        return SERVER_NAME_TAG;
    }

    /**
     * Get the initial archive access list tag.
     * @return the initial archive access list tag.
     */
    public static String getInitialArchiveAccessListTag() {
        return INITIAL_ARCHIVE_ACCESS_LIST_TAG;
    }

    /**
     * Get the define alternate reference location flag tag.
     * @return the define alternate reference location flag tag.
     */
    public static String getDefineAlternateReferenceLocationFlagTag() {
        return DEFINE_ALTERNATE_REFERENCE_LOCATION_FLAG_TAG;
    }

    /**
     * Get the directory contents initialized flag tag.
     * @return the directory contents initialized flag tag.
     */
    public static String getDirectoryContentsInitializedFlagTag() {
        return DIRECTORY_CONTENTS_INITIALIZED_FLAG_TAG;
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    /**
     * Get the reference location.
     * @return the reference location.
     */
    public String getReferenceLocation() {
        return getStringValue(getReferenceLocationTag());
    }

    /**
     * Set the reference location.
     * @param referenceLocation the reference location.
     */
    public void setReferenceLocation(String referenceLocation) {
        setStringValue(getReferenceLocationTag(), referenceLocation);
    }

    /**
     * Get the create reference copy flag.
     * @return the create reference copy flag.
     */
    public boolean getCreateReferenceCopyFlag() {
        return getBooleanValue(getCreateReferenceCopyFlagTag());
    }

    /**
     * Set the create reference copy flag.
     * @param flag the create reference copy flag.
     */
    public void setCreateReferenceCopyFlag(boolean flag) {
        setBooleanValue(getCreateReferenceCopyFlagTag(), flag);
    }

    /**
     * Get the ignore case flag.
     * @return the ignore case flag.
     */
    public boolean getIgnoreCaseFlag() {
        return getBooleanValue(getIgnoreCaseFlagTag());
    }

    /**
     * Set the ignore case flag.
     * @param flag the ignore case flag.
     */
    public void setIgnoreCaseFlag(boolean flag) {
        setBooleanValue(getIgnoreCaseFlagTag(), flag);
    }

    /**
     * Get the archive attributes.
     * @return the archive attributes.
     */
    public ArchiveAttributes getAttributes() {
        ArchiveAttributes archiveAttributes = new ArchiveAttributes();
        String attributesString = getStringValue(getAttributesTag());
        archiveAttributes.fromPropertyString(attributesString);
        return archiveAttributes;
    }

    /**
     * Set the archive attributes.
     * @param attributes the archive attributes.
     */
    public void setAttributes(ArchiveAttributes attributes) {
        String attributeString = attributes.toPropertyString();
        setStringValue(getAttributesTag(), attributeString);
    }

    /**
     * Get the archive location.
     * @return the archive location.
     */
    public String getArchiveLocation() {
        return getStringValue(getArchiveLocationTag());
    }

    /**
     * Set the archive location.
     * @param location the archive location.
     */
    public void setArchiveLocation(String location) {
        setStringValue(getArchiveLocationTag(), location);
    }

    /**
     * Get the parent project name.
     * @return the parent project name.
     */
    public String getParentProjectName() {
        return getStringValue(getParentProjectNameTag());
    }

    /**
     * Set the parent project name.
     * @param parentProjectName the parent project name.
     */
    public void setParentProjectName(String parentProjectName) {
        setStringValue(getParentProjectNameTag(), parentProjectName);
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return getStringValue(getServerNameTag());
    }

    /**
     * Set the server name.
     * @param serverName the server name.
     */
    public void setServerName(String serverName) {
        setStringValue(getServerNameTag(), serverName);
    }

    /**
     * Get the initial archive access list.
     * @return the initial archive access list.
     */
    public AccessList getInitialArchiveAccessList() {
        AccessList accessList = new AccessList(getStringValue(getInitialArchiveAccessListTag()));
        return accessList;
    }

    /**
     * Set the initial archive access list.
     * @param accessList the initial archive access list.
     */
    public void setInitialArchiveAccessList(AccessList accessList) {
        setStringValue(getInitialArchiveAccessListTag(), accessList.getAccessListAsCommaSeparatedString());
    }

    /**
     * Get the project properties.
     * @return the project properties.
     */
    public java.util.Properties getProjectProperties() {
        return getActualProperties();
    }

    /**
     * Get the directory contents initialized flag.
     * @return the directory contents initialized flag.
     */
    public boolean getDirectoryContentsInitializedFlag() {
        return getBooleanValue(getDirectoryContentsInitializedFlagTag());
    }

    /**
     * Set the directory contents initialized flag.
     * @param flag the directory contents initialized flag.
     */
    public void setDirectoryContentsInitializedFlag(boolean flag) {
        setBooleanValue(getDirectoryContentsInitializedFlagTag(), flag);
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     * @param project the project name.
     */
    public void setProjectName(String project) {
        this.projectName = project;
        setPropertyFileName(System.getProperty("user.dir")
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_PROPERTIES_DIRECTORY
                + System.getProperty("file.separator")
                + propertyFilePrefix + project + ".properties");
    }

    /**
     * Get the property file prefix.
     * @return the property file prefix.
     */
    public String getPropertyFilePrefix() {
        return propertyFilePrefix;
    }

    /**
     * Set the property file prefix.
     * @param propertyFilePfx the property file prefix.
     */
    public void setPropertyFilePrefix(String propertyFilePfx) {
        this.propertyFilePrefix = propertyFilePfx;
    }
}
