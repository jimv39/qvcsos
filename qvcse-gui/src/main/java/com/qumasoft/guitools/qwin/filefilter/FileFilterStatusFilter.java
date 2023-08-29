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
 * Workfile status filter.
 * @author Jim Voris
 */
public class FileFilterStatusFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 4473535027615933228L;

    private final String filterStatusString;

    FileFilterStatusFilter(String status, boolean isANDFilter, Integer id) {
        super(isANDFilter, id, CommonFilterFile.INCLUDE_FILE_STATUS);
        filterStatusString = status;
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;
        if (0 == mergedInfo.getStatusString().compareToIgnoreCase(filterStatusString)) {
            retVal = true;
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.STATUS_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.STATUS_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterStatusString;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterStatusFilter) {
            FileFilterStatusFilter filter = (FileFilterStatusFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return computeHash(this.filterStatusString, this.getIsANDFilter());
    }

    @Override
    public String getRawFilterData() {
        return filterStatusString;
    }
}
