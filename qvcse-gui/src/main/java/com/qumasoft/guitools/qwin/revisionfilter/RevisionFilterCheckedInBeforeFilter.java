/*   Copyright 2004-2015 Jim Voris
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

import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.Utility;
import java.util.Date;
import java.util.Objects;

/**
 * Checked-in before revision filter.
 * @author Jim Voris
 */
public class RevisionFilterCheckedInBeforeFilter extends AbstractRevisionFilter {

    private Date filterDate;

    /**
     * Create a checked in before revision filter.
     * @param time the time (as a String) for the date of the filter.
     * @param isANDFilter is this an 'AND' filter.
     */
    public RevisionFilterCheckedInBeforeFilter(String time, boolean isANDFilter) {
        super(isANDFilter);
        try {
            long dateTime = Long.parseLong(time);
            filterDate = new Date(dateTime);
        } catch (NumberFormatException e) {
            warnProblem(Utility.expandStackTraceToString(e));
        }
    }

    @Override
    public boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo) {
        boolean retVal = false;
        RevisionHeader filteredRevision = filteredRevisionInfo.getRevisionHeader();
        if (filteredRevision.getCheckInDate().getTime() < filterDate.getTime()) {
            retVal = true;
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.CHECKED_IN_BEFORE_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.CHECKED_IN_BEFORE_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterDate.toString();
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof RevisionFilterCheckedInBeforeFilter) {
            RevisionFilterCheckedInBeforeFilter filter = (RevisionFilterCheckedInBeforeFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.filterDate);
        if (this.getIsANDFilter()) {
            hash += 97;
        }
        // </editor-fold>
        return hash;
    }
}
