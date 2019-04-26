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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project properties factory.
 * @author Jim Voris
 */
public final class ProjectPropertiesFactory {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectPropertiesFactory.class);
    private static final ProjectPropertiesFactory PROJECT_PROPERTIES_FACTORY = new ProjectPropertiesFactory();

    /**
     * Creates new ProjectPropertiesFactory.
     */
    private ProjectPropertiesFactory() {
    }

    /**
     * Get the Project Properties factory singleton.
     * @return the Project Properties factory singleton.
     */
    public static ProjectPropertiesFactory getProjectPropertiesFactory() {
        return PROJECT_PROPERTIES_FACTORY;
    }

    /**
     * Build the project properties for the given project name and project type.
     * @param directory directory where to find the property file directory.
     * @param projectName the project name.
     * @param projectType the type of project.
     * @return the project properties.
     */
    public AbstractProjectProperties buildProjectProperties(String directory, String projectName, String projectType) {
        AbstractProjectProperties projectProperties = null;
        try {
            if (0 == projectType.compareTo(QVCSConstants.QVCS_REMOTE_PROJECT_TYPE)) {
                projectProperties = new RemoteProjectProperties(directory, projectName);
            } else if (0 == projectType.compareTo(QVCSConstants.QVCS_SERVED_PROJECT_TYPE)) {
                projectProperties = new ServedProjectProperties(directory, projectName);
            }
        } catch (QVCSException e) {
            LOGGER.warn("ProjectPropertiesFactory.buildProjectProperties failed to build [{}] project properties for project: [{}]",
                    projectType, projectName);
            LOGGER.warn(e.getLocalizedMessage(), e);
        }

        return projectProperties;
    }
}
