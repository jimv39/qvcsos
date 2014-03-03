//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.LogfileInfo;
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
 * Checked in before filter.
 * @author Jim Voris
 */
public class FileFilterCheckedInBeforeFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 5512305908643782484L;

    private transient RevisionFilterCheckedInBeforeFilter revisionFilterCheckedInBeforeFilter;
    private Date filterDate;
    private transient String filterTimeString;

    FileFilterCheckedInBeforeFilter(String date, boolean isANDFilter) {
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
        filterTimeString = Long.toString(filterDate.getTime());
        revisionFilterCheckedInBeforeFilter = new RevisionFilterCheckedInBeforeFilter(filterTimeString, isANDFilter);
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;

        if ((filterTimeString == null) && (filterDate != null)) {
            filterTimeString = Long.toString(filterDate.getTime());
        }
        if ((revisionFilterCheckedInBeforeFilter == null) && (filterTimeString != null)) {
            revisionFilterCheckedInBeforeFilter = new RevisionFilterCheckedInBeforeFilter(filterTimeString, getIsANDFilter());
        }
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        if (logfileInfo != null) {
            int revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
            Date oldestCheckInDate = logfileInfo.getRevisionInformation().getRevisionHeader(revisionCount - 1).getCheckInDate();
            int compareResult = oldestCheckInDate.compareTo(filterDate);
            if (compareResult > 0) {
                retVal = false;
            } else {
                retVal = true;

                // Trim the revision map so it contains only revisions that pass this filter
                Iterator<Integer> it = revisionHeaderMap.keySet().iterator();
                while (it.hasNext()) {
                    Integer revisionIndexInteger = it.next();
                    RevisionHeader revisionHeader = revisionHeaderMap.get(revisionIndexInteger);
                    FilteredRevisionInfo filteredRevisionInfo = new FilteredRevisionInfo(mergedInfo, revisionHeader, revisionIndexInteger.intValue());
                    boolean flag = revisionFilterCheckedInBeforeFilter.passesFilter(filteredRevisionInfo);
                    if (!flag) {
                        it.remove();
                    }
                }

                // No revisions are left to pass any filters...
                if (revisionHeaderMap.size() == 0) {
                    retVal = false;
                }
            }
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
        DateFormat simpleDateFormat = DateFormat.getDateTimeInstance();
        return simpleDateFormat.format(filterDate);
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterCheckedInBeforeFilter) {
            FileFilterCheckedInBeforeFilter filter = (FileFilterCheckedInBeforeFilter) o;
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
        hash = 13 * hash + Objects.hashCode(this.filterDate);
        // </editor-fold>
        return hash;
    }

    @Override
    public String getRawFilterData() {
        if (filterTimeString == null) {
            filterTimeString = Long.toString(filterDate.getTime());
        }
        return filterTimeString;
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return true;
    }
}
