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
package com.qumasoft.guitools.qwin.filefilter;

import com.qumasoft.guitools.qwin.revisionfilter.FilteredRevisionInfo;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterCheckedInBeforeCommitFilter;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Checked in before filter.
 * @author Jim Voris
 */
public class FileFilterCheckedInBeforeCommitFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 5512305908643782484L;

    private transient RevisionFilterCheckedInBeforeCommitFilter revisionFilterCheckedInBeforeCommitFilter;
    private final transient String commitIdString;
    private final transient Integer commitId;

    FileFilterCheckedInBeforeCommitFilter(String commitIdAsString, boolean isANDFilter) {
        super(isANDFilter);
        this.commitIdString = commitIdAsString;
        this.commitId = Integer.decode(commitIdString);
        revisionFilterCheckedInBeforeCommitFilter = new RevisionFilterCheckedInBeforeCommitFilter(commitIdAsString, isANDFilter);
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;

        if ((revisionFilterCheckedInBeforeCommitFilter == null) && (commitIdString != null)) {
            revisionFilterCheckedInBeforeCommitFilter = new RevisionFilterCheckedInBeforeCommitFilter(commitIdString, getIsANDFilter());
        }
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        if ((logfileInfo != null) && (revisionFilterCheckedInBeforeCommitFilter != null)) {
            int revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
            Integer oldestCommitId = logfileInfo.getRevisionInformation().getRevisionHeader(revisionCount - 1).getCommitId();
            if (oldestCommitId > commitId) {
                retVal = false;
            } else {
                retVal = true;

                // Trim the revision map so it contains only revisions that pass this filter
                Iterator<Integer> it = revisionHeaderMap.keySet().iterator();
                while (it.hasNext()) {
                    Integer revisionIndexInteger = it.next();
                    RevisionHeader revisionHeader = revisionHeaderMap.get(revisionIndexInteger);
                    FilteredRevisionInfo filteredRevisionInfo = new FilteredRevisionInfo(mergedInfo, revisionHeader, revisionIndexInteger);
                    boolean flag = revisionFilterCheckedInBeforeCommitFilter.passesFilter(filteredRevisionInfo);
                    if (!flag) {
                        it.remove();
                    }
                }

                // No revisions are left to pass any filters...
                if (revisionHeaderMap.isEmpty()) {
                    retVal = false;
                }
            }
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.CHECKED_IN_BEFORE_COMMIT_ID_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.CHECKED_IN_BEFORE_COMMIT_ID_FILTER;
    }

    @Override
    public String getFilterData() {
        return this.commitIdString;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterCheckedInBeforeCommitFilter) {
            FileFilterCheckedInBeforeCommitFilter filter = (FileFilterCheckedInBeforeCommitFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return computeHash(this.commitIdString, this.getIsANDFilter());
    }

    @Override
    public String getRawFilterData() {
        return this.commitIdString;
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return true;
    }
}
