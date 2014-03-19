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
package com.qumasoft.guitools.qwin.filefilter;

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Last edit by filter.
 * @author Jim Voris
 */
public class FileFilterLastEditByFilter extends AbstractFileFilter {
    private static final long serialVersionUID = -7471099479468616419L;

    private final String filterLastEditBy;

    FileFilterLastEditByFilter(String lastEditBy, boolean isANDFilter) {
        super(isANDFilter);
        filterLastEditBy = lastEditBy;
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;
        if (mergedInfo.getLastEditBy().equals(filterLastEditBy)) {
            retVal = true;
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.LAST_EDIT_BY_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.LAST_EDIT_BY_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterLastEditBy;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterLastEditByFilter) {
            FileFilterLastEditByFilter filter = (FileFilterLastEditByFilter) o;
            if (filter.getFilterData().equals(getFilterData())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.filterLastEditBy);
        // </editor-fold>
        return hash;
    }

    @Override
    public String getRawFilterData() {
        return filterLastEditBy;
    }
}
