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

/**
 * Default served project properties.
 * @author Jim Voris
 */
public final class DefaultServedProjectProperties extends AbstractProjectProperties {
    private static final DefaultServedProjectProperties SINGLETON_PROPERTIES = new DefaultServedProjectProperties();

    /**
     * Creates new DefaultServedProjectProperties.
     */
    private DefaultServedProjectProperties() {
        super(null, QVCSConstants.QWIN_DEFAULT_PROJECT_PROPERTIES_NAME, QVCSConstants.QVCS_SERVED_PROJECTNAME_PREFIX);
        loadProperties();
    }

    /**
     * Load the properties for the default project.
     */
    private void loadProperties() {
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        ArchiveAttributes defaultAttributes = new ArchiveAttributes();
        defaultProperties.put(getReferenceLocationTag(), "");
        defaultProperties.put(getAttributesTag(), defaultAttributes.toPropertyString());
        defaultProperties.put(getTempfilePathTag(), System.getProperty("user.home") + System.getProperty("file.separator") + "qvcs_tempfiles");
        defaultProperties.put(getServerNameTag(), QVCSConstants.QVCS_DEFAULT_SERVER_NAME);
        setActualProperties(new java.util.Properties(defaultProperties));
    }

    /**
     * Get the singleton object that defines our default project properties.
     * @return the singleton object that defines our default project properties.
     */
    public static DefaultServedProjectProperties getInstance() {
        return SINGLETON_PROPERTIES;
    }

    @Override
    public String toString() {
        return QVCSConstants.QWIN_DEFAULT_PROJECT_NAME;
    }

    @Override
    public String getArchiveLocation() {
        return null;
    }

    @Override
    public String getProjectType() {
        return QVCSConstants.QVCS_SERVED_PROJECT_TYPE;
    }

    @Override
    public boolean getDefineAlternateReferenceLocationFlag() {
        return getBooleanValue(getDefineAlternateReferenceLocationFlagTag());
    }
}
