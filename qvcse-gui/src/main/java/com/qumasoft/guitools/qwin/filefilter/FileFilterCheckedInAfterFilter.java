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

import com.qumasoft.guitools.qwin.revisionfilter.FilteredRevisionInfo;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterCheckedInAfterFilter;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Checked in after filter.
 * @author Jim Voris
 */
public class FileFilterCheckedInAfterFilter extends AbstractFileFilter {
    private static final long serialVersionUID = -2068967872772050356L;

    private transient RevisionFilterCheckedInAfterFilter revisionFilterCheckedInAfterFilter;
    private transient String timeString;
    private Date filterDate;

    FileFilterCheckedInAfterFilter(String date, boolean isANDFilter) {
        super(isANDFilter);
        ParsePosition parsePosition = new ParsePosition(0);
        DateFormat simpleDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        simpleDateFormat.setLenient(true);
        filterDate = simpleDateFormat.parse(date, parsePosition);
        if (filterDate == null) {
            simpleDateFormat = DateFormat.getDateTimeInstance();
            simpleDateFormat.setLenient(true);
            parsePosition = new ParsePosition(0);
            filterDate = simpleDateFormat.parse(date, parsePosition);
        }
        timeString = Long.toString(filterDate.getTime());
        revisionFilterCheckedInAfterFilter = new RevisionFilterCheckedInAfterFilter(timeString, isANDFilter);
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = true;

        if ((timeString == null) && (filterDate != null)) {
            timeString = Long.toString(filterDate.getTime());
        }
        if ((revisionFilterCheckedInAfterFilter == null) && (timeString != null)) {
            revisionFilterCheckedInAfterFilter = new RevisionFilterCheckedInAfterFilter(timeString, getIsANDFilter());
        }
        if (filterDate != null) {
            Iterator<Integer> it = revisionHeaderMap.keySet().iterator();
            while (it.hasNext()) {
                Integer revisionIndexInteger = it.next();
                RevisionHeader revisionHeader = revisionHeaderMap.get(revisionIndexInteger);
                FilteredRevisionInfo filteredRevisionInfo = new FilteredRevisionInfo(mergedInfo, revisionHeader, revisionIndexInteger.intValue());
                boolean flag = revisionFilterCheckedInAfterFilter.passesFilter(filteredRevisionInfo);
                if (!flag) {
                    it.remove();
                }
            }

            // No revisions are left to pass any filters...
            if (revisionHeaderMap.size() == 0) {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.CHECKED_IN_AFTER_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.CHECKED_IN_AFTER_FILTER;
    }

    @Override
    public String getFilterData() {
        DateFormat simpleDateFormat = DateFormat.getDateTimeInstance();
        return simpleDateFormat.format(filterDate);
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterCheckedInAfterFilter) {
            FileFilterCheckedInAfterFilter filter = (FileFilterCheckedInAfterFilter) o;
            if (filter.getFilterData().equals(getFilterData())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.filterDate);
        // </editor-fold>
        return hash;
    }

    @Override
    public String getRawFilterData() {
        if (timeString == null) {
            timeString = Long.toString(filterDate.getTime());
        }
        return timeString;
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return true;
    }
}
