/*   Copyright 2004-2014 Jim Voris
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
 * Filename filter for served project properties files.
 */
public class QVCSServedProjectNamesFilter implements java.io.FilenameFilter {

    /**
     * Construct the filter.
     */
    public QVCSServedProjectNamesFilter() {
    }

    /**
     * Return true if the given filename is the name of a served project's property file.
     * @param dir the directory containing the file.
     * @param name the name of the file.
     * @return true if the file is the name of a served project property file.
     */
    @Override
    public boolean accept(java.io.File dir, String name) {
        boolean retVal = false;
        if (name.startsWith(QVCSConstants.QVCS_SERVED_PROJECTNAME_PREFIX)) {
            if (getProjectName(name).compareToIgnoreCase(DefaultServedProjectProperties.getInstance().getProjectName()) == 0) {
                retVal = false;
            } else {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".properties")) {
                    retVal = true;
                }
            }
        }
        return retVal;
    }

    /**
     * Convert the property file name into the name of the associated project.
     * @param projectPropertyFileName the name of the project property file.
     * @return the name of the associated project.
     */
    public String getProjectName(String projectPropertyFileName) {
        StringBuilder projectName = new StringBuilder(projectPropertyFileName.substring(QVCSConstants.QVCS_SERVED_PROJECTNAME_PREFIX.length()));
        projectName.delete(projectName.toString().lastIndexOf('.'), projectName.toString().length());
        return projectName.toString();
    }
}

