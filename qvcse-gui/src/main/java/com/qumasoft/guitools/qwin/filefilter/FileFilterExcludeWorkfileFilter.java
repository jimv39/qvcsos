/*   Copyright 2004-2023 Jim Voris
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
package com.qumasoft.guitools.qwin.filefilter;

import com.qumasoft.qvcslib.CommonFilterFile;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.TreeMap;

/**
 * Exclude workfile filter (Exclude those files that are not under source control).
 * @author Jim Voris
 */
public class FileFilterExcludeWorkfileFilter extends AbstractFileFilter {
    private static final long serialVersionUID = -8878807313466428864L;

    FileFilterExcludeWorkfileFilter(boolean isANDFilter, Integer id) {
        super(isANDFilter, id, CommonFilterFile.EXCLUDE_UNCONTROLLED_FILES);
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = true;

        // We exclude files that are not under version control
        if (mergedInfo.getArchiveInfo() == null) {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER;
    }

    @Override
    public String getFilterData() {
        return QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterExcludeWorkfileFilter) {
            FileFilterExcludeWorkfileFilter filter = (FileFilterExcludeWorkfileFilter) o;
            if (filter.getIsANDFilter() == this.getIsANDFilter()) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public String getRawFilterData() {
        return QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        if (this.getIsANDFilter()) {
            hash += 97;
        }
        // </editor-fold>
        return hash;
    }
}
