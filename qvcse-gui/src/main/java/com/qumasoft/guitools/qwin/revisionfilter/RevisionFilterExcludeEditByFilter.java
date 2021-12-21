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
package com.qumasoft.guitools.qwin.revisionfilter;

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Objects;

/**
 * Exclude edit by revision filter.
 * @author Jim Voris
 */
public class RevisionFilterExcludeEditByFilter extends AbstractRevisionFilter {

    private final String filterEditBy;

    /**
     * Create an exclude edit-by revision filter.
     * @param editBy the edit by string that defines the filter.
     * @param isANDFilter is this an 'AND' filter.
     */
    public RevisionFilterExcludeEditByFilter(String editBy, boolean isANDFilter) {
        super(isANDFilter);
        filterEditBy = editBy;
    }

    @Override
    public boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo) {
        boolean retVal = true;
        MergedInfoInterface mergedInfo = filteredRevisionInfo.getMergedInfo();
        RevisionHeader filteredRevision = filteredRevisionInfo.getRevisionHeader();
        String editBy = filteredRevision.getCreator();
        if (editBy.equals(getFilterData())) {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.EXCLUDE_EDIT_BY_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.EXCLUDE_EDIT_BY_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterEditBy;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof RevisionFilterExcludeEditByFilter) {
            RevisionFilterExcludeEditByFilter filter = (RevisionFilterExcludeEditByFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.filterEditBy);
        if (this.getIsANDFilter()) {
            hash += 97;
        }
        // </editor-fold>
        return hash;
    }
}
