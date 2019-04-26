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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;

/**
 * Sub project names filter. A file name filter that can 'hide' qvcs internal subdirectories.
 * @author Jim Voris
 */
public class SubProjectNamesFilter implements java.io.FilenameFilter {

    private final boolean showCemeteryFlag;
    private final boolean showBranchArchivesFlag;

    /**
     * Creates a new instance of SubProjectNamesFilter.
     *
     * @param showCemetery true to show the cemetery.
     * @param showBranch true to show the branch archive directory.
     */
    public SubProjectNamesFilter(boolean showCemetery, boolean showBranch) {
        showCemeteryFlag = showCemetery;
        showBranchArchivesFlag = showBranch;
    }

    @Override
    public boolean accept(java.io.File dir, String name) {
        java.io.File file = new java.io.File(dir.getPath(), name);

        if (file.isDirectory()) {
            if (0 == file.getName().compareTo(QVCSConstants.QVCS_DIRECTORY_METADATA_DIRECTORY)) {
                return false;
            } else if (0 == file.getName().compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                return showCemeteryFlag;
            } else if (0 == file.getName().compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                return showBranchArchivesFlag;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
