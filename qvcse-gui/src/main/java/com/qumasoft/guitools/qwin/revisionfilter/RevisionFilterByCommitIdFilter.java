/*   Copyright 2022 Jim Voris
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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Objects;

/**
 * Checked in with commit id revision filter.
 *
 * @author Jim Voris
 */
public class RevisionFilterByCommitIdFilter extends AbstractRevisionFilter {

    private static Integer filterCommitId = 1;

    /**
     * Create a checked in after revision filter.
     * @param commitIdString the commit id of the filter.
     * @param andFilter is this an 'AND' filter.
     */
    public RevisionFilterByCommitIdFilter(String commitIdString, boolean andFilter) {
        super(andFilter);
    }

    @Override
    public boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo) {
        boolean retVal = false;
        RevisionHeader filteredRevision = filteredRevisionInfo.getRevisionHeader();
        if (filteredRevision.getCommitId() == filterCommitId) {
            retVal = true;
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.BY_COMMIT_ID_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.BY_COMMIT_ID_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterCommitId.toString();
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof RevisionFilterByCommitIdFilter filter) {
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
        hash = 97 * hash + Objects.hashCode(RevisionFilterByCommitIdFilter.filterCommitId);
        if (this.getIsANDFilter()) {
            hash += 97;
        }
        // </editor-fold>
        return hash;
    }

    public static void setFilterCommitId(Integer commitId) {
        filterCommitId = commitId;
    }
}
