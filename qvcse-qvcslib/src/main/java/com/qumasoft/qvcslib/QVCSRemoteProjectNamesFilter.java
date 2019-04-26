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
 * Filename filter for remote project properties files.
 */
public class QVCSRemoteProjectNamesFilter implements java.io.FilenameFilter {

    /**
     * Construct the filter.
     */
    public QVCSRemoteProjectNamesFilter() {
    }

    /**
     * Return true if the given filename is the name of a remote project's property file.
     * @param dir the directory containing the file.
     * @param name the name of the file.
     * @return true if the file is a property file for a remote project.
     */
    @Override
    public boolean accept(java.io.File dir, String name) {
        boolean retVal = false;
        if (name.startsWith(QVCSConstants.QVCS_REMOTE_PROJECTNAME_PREFIX)) {
            // We're not interested in the default remote project properties file...
            if (getProjectName(name).compareToIgnoreCase(DefaultRemoteProjectProperties.getInstance().getProjectName()) != 0) {
                retVal = true;
            }
        }
        return retVal;
    }

    /**
     * Convert the property file name into the name of the associated project.
     * @param projectPropertyFileName the property file name.
     * @return the name of the associated project.
     */
    public String getProjectName(String projectPropertyFileName) {
        StringBuilder projectName = new StringBuilder(projectPropertyFileName.substring(QVCSConstants.QVCS_REMOTE_PROJECTNAME_PREFIX.length()));
        projectName.delete(projectName.toString().lastIndexOf('.'), projectName.toString().length());
        return projectName.toString();
    }
}

