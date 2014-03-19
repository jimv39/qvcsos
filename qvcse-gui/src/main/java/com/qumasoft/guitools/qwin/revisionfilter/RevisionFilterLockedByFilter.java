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

import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Objects;

/**
 * Locked by revision filter.
 * @author Jim Voris
 */
public class RevisionFilterLockedByFilter extends AbstractRevisionFilter {

    private final String filterLockedBy;

    /**
     * Create a locked by revision filter.
     * @param lockedBy the locked-by user name that defines the filter.
     * @param isANDFilter is this an 'AND' filter.
     */
    public RevisionFilterLockedByFilter(String lockedBy, boolean isANDFilter) {
        super(isANDFilter);
        filterLockedBy = lockedBy;
    }

    @Override
    public boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo) {
        boolean retVal = false;
        MergedInfoInterface mergedInfo = filteredRevisionInfo.getMergedInfo();
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        RevisionHeader filteredRevision = filteredRevisionInfo.getRevisionHeader();
        if (filteredRevision.isLocked()) {
            int lockerIndex = filteredRevision.getLockerIndex();
            AccessList accessList = new AccessList(logfileInfo.getLogFileHeaderInfo().getModifierList());
            String lockedBy = accessList.indexToUser(lockerIndex);
            if (lockedBy.equals(getFilterData())) {
                retVal = true;
            } else if (0 == getFilterData().compareTo("*")) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.LOCKED_BY_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.LOCKED_BY_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterLockedBy;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof RevisionFilterLockedByFilter) {
            RevisionFilterLockedByFilter filter = (RevisionFilterLockedByFilter) o;
            if (filter.getFilterData().equals(getFilterData())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.filterLockedBy);
        // </editor-fold>
        return hash;
    }
}
