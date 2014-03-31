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

import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Locked by filter.
 * @author Jim Voris
 */
public class FileFilterLockedByFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 9177861212158108578L;

    private final String filterLockedBy;

    FileFilterLockedByFilter(String lockedBy, boolean isANDFilter) {
        super(isANDFilter);
        filterLockedBy = lockedBy;
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        if (logfileInfo != null) {
            if (logfileInfo.getLogFileHeaderInfo().getLogFileHeader().lockCount() > 0) {
                // Search the remaining revisions to see if the revision is locked by
                // m_LockedBy
                Iterator<Integer> it = revisionHeaderMap.keySet().iterator();
                while (it.hasNext()) {
                    Integer revisionIndexInteger = it.next();
                    RevisionHeader revisionHeader = revisionHeaderMap.get(revisionIndexInteger);
                    if (revisionHeader.isLocked()) {
                        retVal = false;
                        int lockerIndex = revisionHeader.getLockerIndex();
                        AccessList accessList = new AccessList(logfileInfo.getLogFileHeaderInfo().getModifierList());
                        String lockedBy = accessList.indexToUser(lockerIndex);
                        if (lockedBy.equals(getFilterData())) {
                            retVal = true;
                            break;
                        } else if (0 == getFilterData().compareTo("*")) {
                            retVal = true;
                            break;
                        }
                    }
                }
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
        if (o instanceof FileFilterLockedByFilter) {
            FileFilterLockedByFilter filter = (FileFilterLockedByFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return computeHash(this.filterLockedBy, this.getIsANDFilter());
    }

    @Override
    public String getRawFilterData() {
        return filterLockedBy;
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return true;
    }
}
