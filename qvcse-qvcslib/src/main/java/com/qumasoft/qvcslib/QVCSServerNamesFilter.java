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
 * Filename filter for server properties files.
 */
public class QVCSServerNamesFilter implements java.io.FilenameFilter {

    /**
     * Construct the filter.
     */
    public QVCSServerNamesFilter() {
    }

    /**
     * Return true if the given filename is the name of a server property file.
     * @param dir the directory containing the file.
     * @param name the name of the file.
     * @return true if the file is the name of a server property file.
     */
    @Override
    public boolean accept(java.io.File dir, String name) {
        boolean retVal = false;
        if (name.startsWith(QVCSConstants.QVCS_SERVERNAME_PROPERTIES_PREFIX)) {
            if (getServerName(name).compareToIgnoreCase(QVCSConstants.QVCS_DEFAULT_SERVER_NAME) == 0) {
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
     * Convert the property file name into the name of the associated server.
     * @param serverPropertyFileName the name of the server property file.
     * @return the name of the associated server.
     */
    public String getServerName(String serverPropertyFileName) {
        StringBuilder serverName = new StringBuilder(serverPropertyFileName.substring(QVCSConstants.QVCS_SERVERNAME_PROPERTIES_PREFIX.length()));
        serverName.delete(serverName.toString().lastIndexOf('.'), serverName.toString().length());
        return serverName.toString();
    }
}
